package com.lixingchi.appclone;

import android.content.Context;

import java.util.Set;

/**
 * Hides apps the user did not choose from a clone's launcher.
 *
 * <p>Installing an app into a clone (or creating the clone) brings AOSP's own
 * required packages along, e.g. the Settings app, so the launcher's app drawer
 * for that user lists them too. This holds the per-clone allow list of apps the
 * user picked and the launcher scope that consults it.
 *
 * <p>Nothing is removed: the app stays installed and keeps its data in the
 * clone. Only its launcher activity is put out of the launcher's reach; anything
 * opened from a shortcut, another app, or a {@code content://} insert request
 * still resolves. The main user is never affected.
 */
final class CloneVisibility {
    /** Launcher package whose queries are narrowed to the allow list. */
    static final String LAUNCHER_SCOPE = "com.google.android.apps.nexuslauncher";
    /**
     * Android requires a launcher package; without one a clone cannot start.
     * Always exposed even when it was never explicitly chosen.
     */
    static final String REQUIRED_LAUNCHER = "com.google.android.apps.nexuslauncher";
    static final String SETTINGS = "com.android.settings";

    private CloneVisibility() {}

    /** Hidden for this clone, apart from the launcher it needs to start. */
    static boolean hidden(Context context, int userId, String pkg) {
        if (userId < 0 || pkg == null || pkg.isEmpty()) return false;
        if (pkg.equals(REQUIRED_LAUNCHER) || pkg.equals(context.getPackageName())) return false;
        Set<String> chosen = Settings.visibleApps(context, userId);
        return chosen != null && !chosen.contains(pkg);
    }
}
