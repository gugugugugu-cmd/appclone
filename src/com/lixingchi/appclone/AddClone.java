package com.lixingchi.appclone;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AddClone {
    static void chooseApp(InventoryActivity activity, Inventory.Profile destination) {
        if (activity.inventory == null) return;
        final List<Apps.App> apps = new ArrayList<>();
        activity.work("正在读取可用应用…", () -> {
            PackageManager pm = activity.getPackageManager();
            for (ApplicationInfo info : pm.getInstalledApplications(0)) {
                if (info.packageName.equals(activity.getPackageName())) continue;
                if (!Settings.systemApps(activity) && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                if (destination != null && destination.packages.contains(info.packageName)) continue;
                apps.add(new Apps.App(String.valueOf(pm.getApplicationLabel(info)), info.packageName, ""));
            }
            apps.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
            return new Shell.Result(0, "");
        }, () -> picker(activity, apps, destination), false);
    }

    private static void picker(InventoryActivity activity, List<Apps.App> apps, Inventory.Profile destination) {
        LinearLayout form = Ui.column(activity);
        form.setPadding(Ui.dp(activity, 24), 0, Ui.dp(activity, 24), 0);
        EditText search = Ui.input(activity, form, "搜索应用或包名");
        ListView list = new ListView(activity);
        form.addView(list, new LinearLayout.LayoutParams(-1, Ui.dp(activity, 320)));
        List<Apps.App> shown = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
        list.setAdapter(adapter);
        Runnable filter = () -> {
            String query = search.getText().toString().toLowerCase(Locale.ROOT);
            shown.clear(); adapter.clear();
            for (Apps.App app : apps) if ((app.label + " " + app.pkg).toLowerCase(Locale.ROOT).contains(query)) {
                shown.add(app); adapter.add(app.label + "\n" + app.pkg);
            }
        };
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { filter.run(); }
            public void afterTextChanged(Editable s) {}
        });
        filter.run();
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setTitle("添加分身应用")
                .setView(form).setNegativeButton("取消", null).create();
        list.setOnItemClickListener((parent, view, position, id) -> {
            Apps.App app = shown.get(position);
            dialog.dismiss();
            if (destination == null) chooseProfile(activity, app);
            else install(activity, app, destination);
        });
        dialog.show();
        if (apps.isEmpty()) search.setHint("没有可用应用（系统筛选在菜单里）");
    }

    static void chooseProfile(InventoryActivity activity, Apps.App app) {
        if (activity.inventory == null) { activity.refresh(); return; }
        List<Inventory.Profile> choices = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Inventory.Profile profile : activity.inventory.profiles) if (!profile.packages.contains(app.pkg)) {
            choices.add(profile);
            labels.add(Inventory.location(profile) + (profile.clone.running ? " · Running" : " · Stopped"));
        }
        new MaterialAlertDialogBuilder(activity).setTitle("添加 " + app.label + " to…")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> install(activity, app, choices.get(which)))
                .setNeutralButton("新建分身", (dialog, which) -> create(activity, app))
                .setNegativeButton("取消", null).show();
    }

    private static void install(InventoryActivity activity, Apps.App app, Inventory.Profile profile) {
        new MaterialAlertDialogBuilder(activity).setTitle("添加分身？")
                .setMessage(app.label + "\n" + Inventory.location(profile)
                        + "\n\nUses the existing profile. Other apps and the profile's running state are unchanged.")
                .setNegativeButton("取消", null).setPositiveButton("添加", (dialog, which) -> activity.work("正在添加…",
                        () -> {
                            Shell.Result installed =
                                    Apps.change(profile.clone.id, profile.serial, app.pkg, Apps.Action.INSTALL);
                            // The chosen app is the one the user wants on this clone's
                            // home screen; anything else that got installed alongside it
                            // stays out of the drawer.
                            if (installed.ok()) CloneVisibility.choose(activity, profile.clone.id, app.pkg, true);
                            return installed;
                        },
                        () -> autoPin(activity, app, profile.clone.id, profile.clone.name, profile.serial))).show();
    }

    static void create(InventoryActivity activity, Apps.App app) {
        LinearLayout form = Ui.column(activity);
        form.setPadding(Ui.dp(activity, 24), 0, Ui.dp(activity, 24), 0);
        EditText name = Ui.input(activity, form, "分身名称");
        name.setText("分身");
        EditText count = Ui.input(activity, form, "分身数量（1–500）");
        count.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        count.setText("1");
        com.google.android.material.checkbox.MaterialCheckBox append = new com.google.android.material.checkbox.MaterialCheckBox(activity);
        append.setText("在名称后追加 -1、-2 …");
        append.setChecked(false);
        form.addView(append);
        form.addView(Ui.text(activity, "创建并启动指定数量的分身。名称按输入原样保留，除非勾选了自动编号。"
                + (app == null ? "" : "\nThen installs " + app.label + ". If installation fails, the new profile is kept."),
                14, R.color.muted, false));
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.addView(form);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setTitle("新建分身")
                .setView(scroll).setNegativeButton("取消", null).setPositiveButton("创建", null).create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String typed = name.getText().toString();
            List<String> names;
            try {
                List<Clones.Clone> existing = new ArrayList<>();
                if (activity.inventory == null) throw new IllegalArgumentException("请先刷新分身列表。");
                for (Inventory.Profile profile : activity.inventory.profiles) existing.add(profile.clone);
                names = Clones.names(typed, Integer.parseInt(count.getText().toString()), append.isChecked(), existing);
            } catch (IllegalArgumentException error) { count.setError(error.getMessage()); return; }
            dialog.dismiss();
            activity.work("正在创建分身…", () -> {
                int made = 0;
                for (String profileName : names) {
                    int id = Clones.create(profileName);
                    if (id < 0) return new Shell.Result(-1, made + " profiles created.\n" + Clones.lastCreateError
                            + "\nUse Export diagnostics from the main menu for details.");
                    made++;
                    try {
                        long serial = Inventory.serial(activity, id);
                        synchronized (Clones.class) {
                            Shell.Result checked = Clones.verify(id, serial, Shell::root);
                            if (!checked.ok()) throw new IllegalStateException(checked.out);
                            Shell.Result started = Clones.setRunning(id, true);
                            if (!started.ok()) throw new IllegalStateException(started.out);
                        }
                        if (app != null) {
                            Shell.Result installed = Apps.change(id, serial, app.pkg, Apps.Action.INSTALL);
                            if (!installed.ok()) throw new IllegalStateException(installed.out);
                            CloneVisibility.choose(activity, id, app.pkg, true);
                            if (Settings.autoPin(activity)) activity.runOnUiThread(() -> {
                                if (!activity.isFinishing() && !activity.isDestroyed()) autoPin(activity, app, id, profileName, serial);
                            });
                        }
                    } catch (RuntimeException error) {
                        return new Shell.Result(-1, made + " profiles created. User " + id + " is kept, but setup failed.\n" + error);
                    }
                }
                return new Shell.Result(0, "");
            }, null);
        }));
        dialog.show();
    }

    private static void autoPin(InventoryActivity activity, Apps.App app, int id, String name, long serial) {
        if (Settings.autoPin(activity)) Shortcuts.pin(activity, new CloneApps.Target(id, name, app, serial));
    }
}
