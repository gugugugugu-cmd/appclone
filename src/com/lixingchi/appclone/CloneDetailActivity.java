package com.lixingchi.appclone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.appbar.MaterialToolbar;

/** Installed apps in one profile; adding/removing apps is always an explicit action. */
public final class CloneDetailActivity extends InventoryActivity {
    static final String EXTRA_ID = "id";
    static final String EXTRA_SERIAL = "serial";
    private int id;
    private long serial;
    private MaterialToolbar toolbar;
    private Button add;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        id = getIntent().getIntExtra(EXTRA_ID, -1);
        serial = getIntent().getLongExtra(EXTRA_SERIAL, -1);
        LinearLayout root = Ui.column(this);
        toolbar = Ui.toolbar(this, root, "分身", true);
        toolbar.getMenu().add(0, 1, 0, "启动 / 停止分身");
        toolbar.getMenu().add(0, 2, 1, "显示系统应用").setCheckable(true).setChecked(Settings.systemApps(this));
        toolbar.getMenu().add(0, 3, 2, "删除分身");
        toolbar.getMenu().add(0, 4, 3, "刷新");
        toolbar.getMenu().add(0, 5, 0, "重命名分身");
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 4) { refresh(); return true; }
            Inventory.Profile profile = current();
            if (profile == null) return true;
            if (item.getItemId() == 1) AppControls.power(this, profile);
            else if (item.getItemId() == 2) {
                Settings.systemApps(this, !Settings.systemApps(this)); render();
            } else if (item.getItemId() == 5) ProfileControls.rename(this, profile);
            else if (item.getItemId() == 3) ProfileControls.remove(this, profile, this::finish);
            return true;
        });
        useList(new InventoryList(this));
        if (state != null) body.restorePosition(state.getParcelable("listPosition"));
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        add = Ui.addAction(this, root, "把应用加到分身");
        Ui.applyInsets(root, 0, 0, 0, 0, add);
        add.setOnClickListener(v -> { Inventory.Profile profile = current(); if (profile != null) AddClone.chooseApp(this, profile); });
        setContentView(root);
    }

    private Inventory.Profile current() {
        Inventory.Profile profile = inventory == null ? null : inventory.profile(id);
        return profile != null && profile.serial == serial ? profile : null;
    }

    @Override void renderInventory() {
        toolbar.getMenu().findItem(2).setChecked(Settings.systemApps(this));
        List<InventoryList.Item> rows = new ArrayList<>();
        Inventory.Profile profile = current();
        add.setEnabled(profile != null);
        if (profile == null) {
            status.setText("分身已被删除或替换，请返回「分身」页刷新。");
            rows.add(InventoryList.Item.empty("分身不可用", "请返回「分身」页刷新后再操作。"));
            body.show(rows);
            return;
        }
        toolbar.setTitle(profile.clone.name);
        toolbar.getMenu().findItem(1).setTitle(profile.clone.running ? "停止分身" : "启动分身");
        int count = 0;
        for (Inventory.Entry entry : inventory.apps) {
            if (!profile.packages.contains(entry.app.pkg) || (entry.system && !Settings.systemApps(this))) continue;
            count++;
            rows.add(InventoryList.Item.instance(entry, profile, true));
        }
        status.setText("用户 " + id + " · " + (profile.clone.running ? "运行中" : "已停止")
                + " · " + count + " 个应用" + (Settings.systemApps(this) ? "" : " · 已隐藏系统应用"));
        if (count == 0) rows.add(InventoryList.Item.empty("没有可显示的应用", "从菜单里添加应用，或显示系统应用。"));
        body.show(rows);
    }
}
