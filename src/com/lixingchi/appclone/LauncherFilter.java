package com.lixingchi.appclone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Keeps a clone's launcher to the apps the user picked for it.
 *
 * <p>Installing an app into a clone, or creating the clone at all, brings AOSP's
 * required packages along: the app drawer for that user then lists things like
 * Settings even though the user only wanted to clone one or two apps. This
 * narrows what the launcher is allowed to see, without uninstalling anything, so
 * the hidden app keeps its data and still opens from a shortcut or from another
 * app.
 *
 * <p>Runs inside system_server, so it is installed from the {@code android}
 * process and not from the App Clone UI.
 */
final class LauncherFilter {
    private static final String COMPUTER_ENGINE = "com.android.server.pm.ComputerEngine";
    private static final String LAUNCHER = CloneVisibility.LAUNCHER_SCOPE;
    private static final String ACTION_MAIN = "android.intent.action.MAIN";
    private static final String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";
    /** First line of every process's command line on Android 10+. */
    private static final Pattern PROCESS_NAME =
            Pattern.compile("(?m)^([\\w.:]+)$");

    private static volatile int launcherUid = -1;
    private static volatile Thread resolvedOn;

    private LauncherFilter() {}

    /**
     * Installs both hooks. Each is independent: a firmware that moved or renamed
     * one of the two methods still gets the other.
     *
     * @return how many hooks were installed.
     */
    static int install(ClassLoader cl) {
        int ok = 0;
        ok += hook(cl, "queryIntentActivitiesInternal", new String[]{
                Intent.class.getName(), String.class.getName(), "long", "int", "int", "int", "int",
                "boolean", "boolean"}, 0, 5, 8);
        ok += hook(cl, "queryIntentContentProvidersInternal", new String[]{
                Intent.class.getName(), String.class.getName(), "long", "int", "int"},
                0, 4, -1);
        return ok;
    }

    /**
     * Hooks one resolve method.
     *
     * @param intentArg index of the {@link Intent}, used to require a launcher query
     * @param userArg   index of the target user
     * @param startArg  index of {@code resolveForStart}, or -1 when absent. Start
     *                  requests must never be filtered or shortcuts would break.
     */
    private static int hook(ClassLoader cl, String name, String[] parameters, int intentArg,
            int userArg, int startArg) {
        try {
            XposedHelpers.findAndHookMethod(COMPUTER_ENGINE, cl, name,
                    XposedHelpers.findClass(COMPUTER_ENGINE, cl), parameters,
                    new XC_MethodHook() {
                        @Override protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                if (param.getThrowable() != null) return;
                                if (startArg >= 0 && Boolean.TRUE.equals(param.args[startArg])) return;
                                if (param.args[intentArg] instanceof Intent
                                        && !isLauncherQuery((Intent) param.args[intentArg])) return;
                                Object result = param.getResult();
                                if (!(result instanceof Collection)) return;
                                narrow(param.thisObject, (Collection<?>) result,
                                        (int) param.args[userArg], (long) param.args[2]);
                            } catch (Throwable t) {
                                XposedBridge.log("AppClone: filter skipped: " + t);
                            }
                        }
                    });
            return 1;
        } catch (Throwable t) {
            XposedBridge.log("AppClone: cannot hook " + name + ": " + t);
            return 0;
        }
    }

    /** True for the query a launcher makes to build its app list. */
    private static boolean isLauncherQuery(Intent intent) {
        if (!ACTION_MAIN.equals(intent.getAction())) return false;
        if (!intent.hasCategory(CATEGORY_LAUNCHER)) return false;
        // An explicit package or component is the launcher asking about one known
        // app (shortcut, widget search result); that answer must stay intact.
        return intent.getPackage() == null && intent.getComponent() == null;
    }

    private static void narrow(Object engine, Collection<?> results, int userId, long flags) {
        if (results.isEmpty()) return;
        // MATCH_ANY_USER asks for every user, which is not a launcher listing.
        if ((flags & 0x00000040L) != 0) return;
        // Cheap check first: only a launcher inside a clone is filtered at all.
        if (Binder.getCallingUid() != launcherUid()) return;
        int parent = profileParent(engine, userId);
        if (parent < 0 || userId == parent) return;
        Context context = currentApplication();
        if (context == null) return;
        Iterator<?> each = results.iterator();
        while (each.hasNext()) {
            String pkg = packageName(each.next());
            if (pkg != null && CloneVisibility.hidden(context, userId, pkg)) each.remove();
        }
    }

    /** Package of a {@code ResolveInfo} or similar result, or null. */
    private static String packageName(Object info) {
        try {
            if (info instanceof String) return (String) info;
            Object activity = XposedHelpers.getObjectField(info, "activityInfo");
            if (activity == null) {
                activity = XposedHelpers.getObjectField(info, "providerInfo");
            }
            if (activity == null) return null;
            Object name = XposedHelpers.getObjectField(activity, "packageName");
            return name instanceof String ? (String) name : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Parent of a clone profile, or -1 when {@code userId} is not a profile. */
    private static int profileParent(Object engine, int userId) {
        try {
            Object userManager = XposedHelpers.getObjectField(engine, "mUserManager");
            if (userManager == null) return -1;
            Method getParent = userManager.getClass().getMethod("getProfileParentId", int.class);
            Object parent = getParent.invoke(userManager, userId);
            return parent instanceof Integer ? (Integer) parent : -1;
        } catch (Throwable ignored) {
            return -1;
        }
    }

    /** UID of the launcher app, looked up once per process. */
    private static int launcherUid() {
        int known = launcherUid;
        if (known >= 0 || resolvedOn == Thread.currentThread()) return known;
        synchronized (LauncherFilter.class) {
            if (launcherUid >= 0) return launcherUid;
            resolvedOn = Thread.currentThread();
            try {
                if (!isLauncherProcess()) {
                    XposedBridge.log("AppClone: launcher process not recognised, filter off");
                    return -1;
                }
                Class<?> app = XposedHelpers.findClass("android.app.ApplicationPackageManager",
                        null);
                Method uid = app.getDeclaredMethod("getPackageUidAsUser", String.class,
                        int.class);
                uid.setAccessible(true);
                Object value = uid.invoke(null, LAUNCHER, currentUserId());
                if (value instanceof Integer) launcherUid = (Integer) value;
            } catch (Throwable t) {
                XposedBridge.log("AppClone: launcher uid lookup failed: " + t);
            }
            return launcherUid;
        }
    }

    /** Application context of this process, or null before one exists. */
    private static Context currentApplication() {
        try {
            return (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication");
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** User this process runs as. */
    private static int currentUserId() {
        try {
            Object id = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.os.UserHandle", null), "myUserId");
            return id instanceof Integer ? (Integer) id : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** True when this system_server process hosts the launcher package. */
    private static boolean isLauncherProcess() throws java.io.IOException {
        String cmdline = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("/proc/self/cmdline")), "UTF-8").replace('\0', '\n');
        java.util.regex.Matcher match = PROCESS_NAME.matcher(cmdline);
        while (match.find()) {
            if (LAUNCHER.equals(match.group(1))) return true;
        }
        return false;
    }
}
