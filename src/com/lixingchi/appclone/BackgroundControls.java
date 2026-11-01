package com.lixingchi.appclone;

import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Reads policy only when requested, not during inventory scans or navigation. */
final class BackgroundControls {
    static void open(InventoryActivity activity, Inventory.Entry entry, Inventory.Profile profile) {
        BackgroundPolicy policy = BackgroundPolicy.get(activity);
        BackgroundPolicy.State[] state = new BackgroundPolicy.State[1];
        activity.work("正在读取后台设置…", () -> {
            synchronized (Clones.class) {
                state[0] = policy.read(profile.clone.id, profile.serial, entry.app.pkg);
                policy.previous(profile.clone.id, profile.serial, entry.app.pkg);
            }
            return new Shell.Result(0, "");
        }, () -> show(activity, entry, profile, policy, state[0]), false);
    }

    static void show(InventoryActivity activity, Inventory.Entry entry, Inventory.Profile profile,
                     BackgroundPolicy policy, BackgroundPolicy.State state) {
        int user = profile.clone.id;
        long serial = profile.serial;
        String pkg = entry.app.pkg;
        BackgroundPolicy.State saved = policy.previous(user, serial, pkg);
        String target = entry.app.label + "\n" + Inventory.location(profile);
        String message = target + "\n\nCurrent: " + BackgroundPolicy.name(state.mode)
                + (state.uidMode == -1 ? "" : "\nUID-wide override: " + BackgroundPolicy.name(state.uidMode) + " (managed elsewhere)")
                + (saved == null ? "\nNo previous setting saved." : "\nPrevious: " + BackgroundPolicy.name(saved.mode))
                + "\n\nRestrict limits Android background activity (RUN_ANY_IN_BACKGROUND). Notifications and jobs may be delayed. "
                + "这不会停止分身、停用应用或冻结其进程。在你选择一个操作前不会改动任何设置。";
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity).setTitle("后台活动")
                .setMessage(message).setNegativeButton("保持当前", null);
        if (saved != null) {
            dialog.setPositiveButton("恢复之前设置", (d, which) -> activity.work("正在恢复后台设置…", () -> {
                policy.restore(user, serial, pkg, state);
                return new Shell.Result(0, "");
            }, () -> Toast.makeText(activity, "已恢复之前的后台设置", Toast.LENGTH_LONG).show(), false));
            dialog.setNeutralButton("忘记已保存的设置", (d, which) -> new MaterialAlertDialogBuilder(activity)
                    .setTitle("忘记之前的设置？")
                    .setMessage(target + "\n\nAndroid's current setting stays unchanged. This removes only App Clone's restore record; Restore will no longer be available.")
                    .setNegativeButton("取消", null).setPositiveButton("忘记", (confirm, button) -> activity.work("正在清除已保存设置…", () -> {
                        policy.forget(user, serial, pkg, saved);
                        return new Shell.Result(0, "");
                    }, null, false)).show());
        } else if (state.mode != BackgroundPolicy.RESTRICTED && state.uidMode == -1) {
            dialog.setPositiveButton("限制", (d, which) -> activity.work("正在限制后台活动…", () -> {
                policy.restrict(user, serial, pkg, state);
                return new Shell.Result(0, "");
            }, () -> Toast.makeText(activity, "后台已受限，之前的设置已保存", Toast.LENGTH_LONG).show(), false));
        }
        dialog.show();
    }

    private BackgroundControls() { }
}
