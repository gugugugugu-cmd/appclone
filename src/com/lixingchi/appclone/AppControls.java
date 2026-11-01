package com.lixingchi.appclone;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.PopupMenu;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** The same explicitly profile-scoped actions in both navigation directions. */
final class AppControls {
    static void bind(InventoryActivity activity, Inventory.Entry entry, Inventory.Profile profile,
                     Button open, View more) {
        open.setContentDescription("打开 " + entry.app.label + " in " + Inventory.location(profile));
        open.setEnabled(!profile.disabled.contains(entry.app.pkg));
        open.setOnClickListener(v -> activity.startActivity(new Intent(activity, LaunchActivity.class)
                .putExtra(LaunchActivity.EXTRA_USER, profile.clone.id)
                .putExtra(LaunchActivity.EXTRA_SERIAL, profile.serial)
                .putExtra(LaunchActivity.EXTRA_PKG, entry.app.pkg)
                .putExtra(LaunchActivity.EXTRA_ACTIVITY, entry.app.activity)
                .putExtra(LaunchActivity.EXTRA_LABEL, entry.app.label)));
        more.setContentDescription("操作：" + entry.app.label + " in " + Inventory.location(profile));
        more.setTooltipText("应用操作");
        more.setOnClickListener(v -> menu(activity, entry, profile, more).show());
    }

    static PopupMenu menu(InventoryActivity activity, Inventory.Entry entry, Inventory.Profile profile, View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        boolean disabled = profile.disabled.contains(entry.app.pkg);
        menu.getMenu().add(0, 1, 0, disabled ? "启用应用" : "停用应用（冻结）");
        menu.getMenu().add(0, 2, 1, "使用清单默认状态");
        menu.getMenu().add(0, 3, 2, "强制停止应用");
        menu.getMenu().add(0, 4, 3, "固定快捷方式");
        menu.getMenu().add(0, 6, 4, "后台活动");
        menu.getMenu().add(0, 7, 5, "重命名分身");
        menu.getMenu().add(0, 8, 6, "查看分身");
        menu.getMenu().add(0, 5, 7, "从这个分身移除");
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 7) { ProfileControls.rename(activity, profile); return true; }
            if (item.getItemId() == 8) { ProfileControls.open(activity, profile); return true; }
            if (item.getItemId() == 6) { BackgroundControls.open(activity, entry, profile); return true; }
            if (item.getItemId() == 4) {
                Shortcuts.pin(activity, new CloneApps.Target(profile.clone.id, profile.clone.name, entry.app, profile.serial));
                return true;
            }
            Apps.Action action;
            switch (item.getItemId()) {
                case 1: action = disabled ? Apps.Action.ENABLE : Apps.Action.DISABLE; break;
                case 2: action = Apps.Action.DEFAULT; break;
                case 3: action = Apps.Action.FORCE_STOP; break;
                case 5: action = Apps.Action.REMOVE; break;
                default: return false;
            }
            confirm(activity, entry, profile, action);
            return true;
        });
        return menu;
    }

    static void confirm(InventoryActivity activity, Inventory.Entry entry, Inventory.Profile profile, Apps.Action action) {
        String title;
        String detail;
        switch (action) {
            case DISABLE:
                title = "停用应用";
                detail = "停止该应用及其通知，直到你手动启用。数据保留，分身里的其他应用继续运行。";
                break;
            case ENABLE:
                title = "启用应用";
                detail = "手动启用该应用。不会自动启动已停止的分身。";
                break;
            case DEFAULT:
                title = "使用清单默认状态";
                detail = "清除启用/停用的覆盖设置，回到应用清单的默认值。不会恢复之前某个覆盖值。";
                break;
            case FORCE_STOP:
                title = "强制停止应用";
                detail = "立即停止该应用，不停用也不删数据。通知会暂停直到再次打开它。这不是永久冻结。";
                break;
            default:
                title = "移除分身应用";
                detail = "删除该应用在这个分身里的数据。分身、其他应用和主用户的副本都保留。";
        }
        new MaterialAlertDialogBuilder(activity).setTitle(title + "?")
                .setMessage(entry.app.label + "\n" + Inventory.location(profile) + "\n\n" + detail
                        + (entry.system ? "\n\nSystem app: changing it can break other apps in this profile." : ""))
                .setNegativeButton("取消", null)
                .setPositiveButton(title, (dialog, which) -> activity.work(title + "…",
                        () -> Apps.change(profile.clone.id, profile.serial, entry.app.pkg, action), null)).show();
    }

    static void power(InventoryActivity activity, Inventory.Profile profile) {
        boolean running = profile.clone.running;
        Runnable change = () -> activity.work(running ? "正在停止分身…" : "正在启动分身…", () -> {
            synchronized (Clones.class) {
                Shell.Result checked = Clones.verify(profile.clone.id, profile.serial, Shell::root);
                return checked.ok() ? Clones.setRunning(profile.clone.id, !running) : checked;
            }
        }, null);
        if (!running) { change.run(); return; }
        new MaterialAlertDialogBuilder(activity).setTitle("停止分身？")
                .setMessage(Inventory.location(profile) + "\n\nStops ALL apps and notifications in this profile, keeping data. "
                        + "打开应用就会重新启动。重启后的行为可在设置里控制。")
                .setNegativeButton("取消", null).setPositiveButton("停止", (dialog, which) -> change.run()).show();
    }
}
