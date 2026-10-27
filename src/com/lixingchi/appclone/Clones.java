package com.lixingchi.appclone;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Clone-profile users, read from {@code cmd user list -v}. */
final class Clones {
    static final class Clone {
        final int id;
        final String name;
        final boolean running;
        private final String details;

        Clone(int id, String name, String details) {
            this.id = id;
            this.name = name;
            this.running = details.contains("(running)");
            this.details = details;
        }
    }

    /** Set by {@link #list()} when the underlying command failed. */
    static String lastError;

    /** Command and output of the last failed {@link #create(String)} call. */
    static volatile String lastCreateError;

    private static final Pattern LINE =
            Pattern.compile("\\d+: id=(\\d+), name=(.*), type=([^,]+),(.*)");
    private static final Pattern CREATED = Pattern.compile("created user id (\\d+)");

    static List<Clone> list() {
        lastError = null;
        Shell.Result result = Shell.root("cmd user list -v");
        if (!result.ok()) {
            lastError = result.trim().isEmpty()
                    ? "需要 Root：请在 Root 管理器里允许 App Clone" : result.trim();
            return new ArrayList<>();
        }
        return parse(result.out);
    }

    static List<Clone> parse(String output) {
        List<Clone> clones = new ArrayList<>();
        for (String line : output.split("\n")) {
            Matcher matcher = LINE.matcher(line.trim());
            if (!matcher.matches()) continue;
            String type = matcher.group(3).trim();
            if (!ClonePolicy.CLONE_TYPE.equals(type) && !"profile.CLONE".equals(type)) continue;
            try {
                clones.add(new Clone(Integer.parseInt(matcher.group(1)), matcher.group(2),
                        matcher.group(4)));
            } catch (NumberFormatException ignored) {
                // Reject malformed ids rather than passing them to a privileged command.
            }
        }
        Collections.sort(clones, new Comparator<Clone>() {
            @Override public int compare(Clone a, Clone b) {
                return Integer.compare(a.id, b.id);
            }
        });
        return clones;
    }

    /** Single-quotes a value so spaces and metacharacters survive {@code su -c}. */
    static String quote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    /** Creates one clone and starts it so it becomes initialized. Returns its id, or -1. */
    static int create(String name) {
        lastCreateError = null;
        String command = "pm create-user --profileOf 0 --user-type "
                + ClonePolicy.CLONE_TYPE + " " + quote(name);
        Shell.Result created = Shell.root(command);
        Matcher matcher = CREATED.matcher(created.out);
        if (!matcher.find()) {
            lastCreateError = "$ " + command + "\nexit=" + created.code + "\n"
                    + (created.trim().isEmpty() ? "(no output)" : created.trim());
            Log.w("AppClone", "create-user failed: " + lastCreateError);
            return -1;
        }
        int id = Integer.parseInt(matcher.group(1));
        Shell.root("am start-user " + id);
        return id;
    }

    static synchronized Shell.Result setRunning(int id, boolean running) {
        return setRunning(id, running, Shell::root);
    }

    static Shell.Result setRunning(int id, boolean running, Function<String, Shell.Result> run) {
        return setRunning(id, running, run, Clones::pause);
    }

    /** Poll interval while waiting for a state change, in milliseconds. */
    static final long POLL_MS = 700;
    /** How long a start may sit in RUNNING_LOCKED before it is called a failure. */
    static final long UNLOCK_WAIT_MS = 45000;
    /** How long a stop may keep the user started before it is called a failure. */
    static final long STOP_WAIT_MS = Shell.STOP_TIMEOUT_SECONDS * 1000;

    /** Default nap between polls. */
    static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Starts or stops one clone and verifies the result.
     *
     * <p>Neither direction blocks on {@code -w}: stopping can take over a minute
     * while vold waits for app files to close, and a start that cannot unlock its
     * storage never finishes at all. Polling the state instead lets each outcome
     * be reported for what it is.
     *
     * @param nap called between polls; tests pass a no-op.
     */
    static Shell.Result setRunning(int id, boolean running, Function<String, Shell.Result> run,
            LongConsumer nap) {
        // Reject impossible ids before spending a root command on them.
        if (id <= 0) return new Shell.Result(-1, "不是分身：" + id);
        String stateCommand = running ? "am get-started-user-state " + id : "am is-user-stopped " + id;
        String expected = running ? "RUNNING_UNLOCKED" : "true";
        Shell.Result users = run.apply("cmd user list -v");
        if (!users.ok()) return users;
        Clone target = null;
        for (Clone clone : parse(users.out)) if (clone.id == id) target = clone;
        if (target == null) return new Shell.Result(-1, "分身已不存在：" + id);
        if (!target.details.contains("(parentId=0)")) {
            return new Shell.Result(-1, "只能启动或停止属于主用户（用户 0）的分身。");
        }
        if (target.details.contains("EPHEMERAL")) {
            return new Shell.Result(-1, "无法控制临时分身：停止它可能删除其数据。");
        }
        if (running) {
            Shell.Result pre = run.apply(stateCommand);
            // A failed query must not be read as "not started yet".
            if (!pre.ok()) return pre;
            if (expected.equals(pre.trim())) {
                return new Shell.Result(0, "分身已在运行。");
            }
        }
        // -f stops only this profile when its related user 0 cannot be stopped (Android 13+).
        // Without -w so a stuck storage lock cannot hold the caller past its budget.
        String command = running ? "am start-user " + id : "am stop-user -f " + id;
        Shell.Result issued = run.apply(command);
        if (!issued.ok()) return issued;
        return await(run, stateCommand, expected, id, running, nap);
    }

    /** Polls until the clone reaches the wanted state, or explains why it did not. */
    private static Shell.Result await(Function<String, Shell.Result> run, String stateCommand,
            String expected, int id, boolean running, LongConsumer nap) {
        long budget = running ? UNLOCK_WAIT_MS : STOP_WAIT_MS;
        long waited = 0;
        Shell.Result state = new Shell.Result(-1, "");
        while (waited <= budget) {
            if (waited > 0) nap.accept(POLL_MS);
            state = run.apply(stateCommand);
            if (state.ok() && expected.equals(state.trim())) return state;
            waited += POLL_MS;
        }
        String now = state.out.trim();
        if (running && "RUNNING_LOCKED".equals(now)) {
            // Storage never unlocked. Retrying is useless until the device reboots:
            // the key stayed loaded after an aborted stop, see Shell#STOP_TIMEOUT_SECONDS.
            return new Shell.Result(-1, "分身 " + id + " 已启动但存储无法解锁（RUNNING_LOCKED）。\n"
                    + "这通常源于上一次「停止」没能锁住存储。请重启手机后再试；"
                    + "重启仍然如此时，可在分身详情里删除该分身重建。");
        }
        if (!running && "RUNNING_LOCKED".equals(now)) {
            return new Shell.Result(-1, "分身 " + id + " 已停止应用，但存储仍未锁定完成。\n"
                    + "等一会儿再试启动即可，不必反复点停止。");
        }
        return new Shell.Result(-1, "分身 " + id + (running ? " 启动超时，当前状态：" : " 停止超时，当前状态：")
                + (now.isEmpty() ? "未知" : now));
    }

    static Shell.Result remove(int id) {
        return Shell.root("pm remove-user " + id);
    }

    private Clones() {}
}
