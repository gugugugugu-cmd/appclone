package com.lixingchi.appclone;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Profile-wide actions always identify the profile and preserve names verbatim. */
final class ProfileControls {
    static void open(InventoryActivity activity, Inventory.Profile profile) {
        activity.startActivity(new Intent(activity, CloneDetailActivity.class)
                .putExtra(CloneDetailActivity.EXTRA_ID, profile.clone.id)
                .putExtra(CloneDetailActivity.EXTRA_SERIAL, profile.serial));
    }

    static void menu(InventoryActivity activity, Inventory.Profile profile, View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(0, 1, 0, "查看应用");
        menu.getMenu().add(0, 2, 1, "添加应用");
        menu.getMenu().add(0, 3, 2, "重命名分身");
        menu.getMenu().add(0, 4, 3, profile.clone.running ? "停止分身" : "启动分身");
        menu.getMenu().add(0, 5, 4, "删除分身");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: open(activity, profile); break;
                case 2: AddClone.chooseApp(activity, profile); break;
                case 3: rename(activity, profile); break;
                case 4: AppControls.power(activity, profile); break;
                case 5: remove(activity, profile, null); break;
                default: return false;
            }
            return true;
        });
        menu.show();
    }

    static void rename(InventoryActivity activity, Inventory.Profile profile) {
        LinearLayout form = Ui.column(activity);
        form.setPadding(Ui.dp(activity, 24), 0, Ui.dp(activity, 24), Ui.dp(activity, 8));
        form.addView(Ui.text(activity, "用户 " + profile.clone.id + " · Serial " + profile.serial
                + "\nChanges the profile name for all its apps. Data and running state stay unchanged. "
                + "已有快捷方式的名称不变，想要新名称请重新固定。", 14, R.color.muted, false));
        EditText name = Ui.input(activity, form, "分身名称");
        name.setText(profile.clone.name);
        name.selectAll();
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setTitle("重命名分身")
                .setView(scroll).setNegativeButton("取消", null).setPositiveButton("保存", null).create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String typed = name.getText().toString();
            try { ProfileRename.validate(typed); }
            catch (IllegalArgumentException error) { name.setError(error.getMessage()); return; }
            dialog.dismiss();
            if (typed.equals(profile.clone.name)) return;
            activity.work("正在重命名…", () -> Clones.rename(profile.clone.id, profile.serial, typed), null);
        }));
        dialog.show();
    }

    static void remove(InventoryActivity activity, Inventory.Profile profile, Runnable success) {
        new MaterialAlertDialogBuilder(activity).setTitle("删除分身？")
                .setMessage(Inventory.location(profile) + "\n\nDeletes ALL app data in this profile, including system apps hidden by the filter. Other profiles and the main user are untouched.")
                .setNegativeButton("取消", null).setPositiveButton("删除", (dialog, which) ->
                        activity.work("正在删除分身…", () -> Clones.remove(profile.clone.id, profile.serial), success)).show();
    }

    private ProfileControls() { }
}
