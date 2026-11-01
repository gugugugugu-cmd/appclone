package com.lixingchi.appclone;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Objects;
import java.util.function.Function;

/** Saves the previous package AppOp before mutation; never writes UID-wide policy. */
final class BackgroundPolicy {
    static final int RESTRICTED = android.app.AppOpsManager.MODE_IGNORED;
    final SharedPreferences prefs;
    private final Function<String, Shell.Result> shell;

    static BackgroundPolicy get(Context context) {
        return new BackgroundPolicy(context.getSharedPreferences("background", Context.MODE_PRIVATE), Shell::root);
    }

    BackgroundPolicy(SharedPreferences prefs, Function<String, Shell.Result> shell) {
        this.prefs = prefs; this.shell = shell;
    }

    static final class State {
        final int uid, mode, uidMode;
        final long installed;
        State(int uid, long installed, int mode, int uidMode) {
            if (uid < 100000 || installed < 0 || mode < 0 || mode > 4 || uidMode < -1 || uidMode > 4)
                throw new IllegalArgumentException("不支持的后台设置。");
            this.uid = uid; this.installed = installed; this.mode = mode; this.uidMode = uidMode;
        }
        State withMode(int mode) { return new State(uid, installed, mode, uidMode); }
        String encode() { return uid + ":" + installed + ":" + mode + ":" + uidMode; }
        static State parse(String text) {
            String[] fields = text.split(":", -1);
            if (fields.length != 4) throw new IllegalArgumentException("无法读取准确的后台设置。");
            return new State(Integer.parseInt(fields[0]), Long.parseLong(fields[1]),
                    Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof State)) return false;
            State state = (State) other;
            return uid == state.uid && installed == state.installed && mode == state.mode && uidMode == state.uidMode;
        }
        @Override public int hashCode() { return Objects.hash(uid, installed, mode, uidMode); }
    }

    static String name(int mode) {
        switch (mode) {
            case -1: return "无";
            case 0: return "允许（由系统管理）";
            case 1: return "受限";
            case 2: return "已拒绝";
            case 3: return "系统默认";
            case 4: return "仅前台";
            default: throw new IllegalArgumentException("未知的后台模式。");
        }
    }

    private String key(int user, long serial, String pkg) {
        if (user <= 0 || serial < 0 || !Apps.validPackage(pkg)) throw new IllegalArgumentException("无效的分身应用标识。");
        return user + ":" + serial + ":" + pkg;
    }

    State previous(int user, long serial, String pkg) {
        String saved = prefs.getString(key(user, serial, pkg), null);
        return saved == null ? null : State.parse(saved);
    }

    State read(int user, long serial, String pkg) {
        key(user, serial, pkg);
        State state = result(shell.apply(AppCommand.command("background-get", user, serial, pkg)));
        if (state.uid / 100000 != user) throw new IllegalStateException("后台设置属于另一个分身。");
        return state;
    }

    void restrict(int user, long serial, String pkg, State preview) {
        synchronized (Clones.class) {
            State current = fresh(user, serial, pkg, preview);
            if (previous(user, serial, pkg) != null) throw new IllegalStateException("请先恢复或清除已保存的设置。");
            if (current.uidMode != -1) throw new IllegalStateException("A UID-wide override is controlled elsewhere; nothing changed.");
            if (current.mode == RESTRICTED) throw new IllegalStateException("已受限，没有改动。");
            // Persist before root so interruption or a partial failure cannot erase the restore point.
            save(key(user, serial, pkg), current.encode());
            set(user, serial, pkg, current, RESTRICTED);
        }
    }

    void restore(int user, long serial, String pkg, State preview) {
        synchronized (Clones.class) {
            State current = fresh(user, serial, pkg, preview);
            State saved = previous(user, serial, pkg);
            if (saved == null) throw new IllegalStateException("没有保存过之前的设置。");
            if (!current.equals(saved)) {
                if (!current.equals(saved.withMode(RESTRICTED)))
                    throw new IllegalStateException("应用或其设置已在别处改变。未做任何恢复；你可以直接清除该记录而不改动系统。");
                set(user, serial, pkg, current, saved.mode);
            }
            save(key(user, serial, pkg), null);
        }
    }

    void forget(int user, long serial, String pkg, State expected) {
        synchronized (Clones.class) {
            if (!Objects.equals(previous(user, serial, pkg), expected))
                throw new IllegalStateException("已保存的设置发生了变化，请重新打开后台控制。");
            save(key(user, serial, pkg), null);
        }
    }

    private State fresh(int user, long serial, String pkg, State preview) {
        State current = read(user, serial, pkg);
        if (!current.equals(preview)) throw new IllegalStateException("后台设置已改变，请重新打开控制面板。");
        return current;
    }

    private void set(int user, long serial, String pkg, State current, int mode) {
        Shell.Result response = shell.apply(AppCommand.command("background-set", user, serial, pkg,
                current.encode(), Integer.toString(mode)));
        if (!response.ok()) throw new IllegalStateException(response.trim()
                + "\nPrevious setting is saved. Reopen Background activity to restore or inspect it.");
        if (!result(response).equals(current.withMode(mode)))
            throw new IllegalStateException("无法确认该设置，之前的模式仍已保存。");
    }

    private static State result(Shell.Result result) {
        if (!result.ok()) throw new IllegalStateException(result.trim().isEmpty() ? "无法读取后台设置。" : result.trim());
        return State.parse(result.trim());
    }

    private void save(String key, String value) {
        String previous = prefs.getString(key, null);
        if (!edit(key, value).commit()) {
            edit(key, previous).commit();
            throw new IllegalStateException("无法保存后台恢复记录，后续改动均未执行。");
        }
    }

    private SharedPreferences.Editor edit(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        return value == null ? editor.remove(key) : editor.putString(key, value);
    }
}
