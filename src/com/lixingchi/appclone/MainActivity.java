package com.lixingchi.appclone;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** App-first inventory, with the complete profile view one tab away. */
public final class MainActivity extends InventoryActivity {
    private boolean profiles;
    private final EditText[] searches = new EditText[2];
    private final InventoryList[] pages = new InventoryList[2];
    private Button add;
    private MaterialToolbar toolbar;
    private String pendingReport;
    private static final int REQUEST_EXPORT = 1;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        profiles = state != null && state.getBoolean("profiles");
        LinearLayout root = Ui.column(this);
        toolbar = Ui.toolbar(this, root, "App Clone", false);
        pendingReport = (String) getLastCustomNonConfigurationInstance();
        toolbar.getMenu().add(0, 1, 0, "设置");
        toolbar.getMenu().add(0, 2, 1, "显示系统应用").setCheckable(true).setChecked(Settings.systemApps(this));
        toolbar.getMenu().add(0, 3, 2, "刷新");
        toolbar.getMenu().add(0, 4, 3, "导出诊断信息");
        toolbar.getMenu().add(0, 5, 4, "删除全部分身");
        toolbar.getMenu().add(0, 6, 5, "源码 · GitHub");
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: startActivity(new Intent(this, SettingsActivity.class)); break;
                case 2:
                    Settings.systemApps(this, !Settings.systemApps(this)); render(); break;
                case 3: refresh(); break;
                case 4: exportDiagnostics(); break;
                case 5: deleteAll(); break;
                case 6:
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/gugugugugu-cmd/appclone")));
                    break;
                default: return false;
            }
            return true;
        });
        TabLayout tabs = new TabLayout(this);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        root.addView(tabs);
        for (int i = 0; i < pages.length; i++) {
            final int page = i;
            pages[i] = new InventoryList(this);
            if (state != null) pages[i].restorePosition(state.getParcelable("position" + i));
            LinearLayout searchForm = Ui.column(this);
            searches[i] = Ui.input(this, searchForm, i == 0 ? "搜索应用或分身" : "搜索分身或用户 ID");
            com.google.android.material.textfield.TextInputLayout field =
                    (com.google.android.material.textfield.TextInputLayout) searchForm.getChildAt(0);
            field.setStartIconDrawable(R.drawable.ic_search);
            field.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_CLEAR_TEXT);
            searches[i].setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
            if (state != null) searches[i].setText(state.getString("query" + i, ""));
            pages[i].header.addView(searchForm, 0);
            searches[i].addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (page == (profiles ? 1 : 0)) { render(); pages[page].top(); }
                }
                public void afterTextChanged(Editable s) {}
            });
        }
        useList(pages[profiles ? 1 : 0]);
        ViewPager pager = new ViewPager(this);
        pager.setOffscreenPageLimit(1);
        root.addView(pager, new LinearLayout.LayoutParams(-1, 0, 1));
        add = Ui.addAction(this, root, profiles ? "新建分身" : "添加分身应用");
        Ui.applyInsets(root, 0, 0, 0, 0, add);
        add.setOnClickListener(v -> {
            if (inventory == null) { refresh(); return; }
            if (profiles) AddClone.create(this, null); else AddClone.chooseApp(this, null);
        });
        pager.setAdapter(new PagerAdapter() {
            public int getCount() { return pages.length; }
            public boolean isViewFromObject(View view, Object object) { return view == object; }
            public CharSequence getPageTitle(int position) { return position == 0 ? "应用" : "分身"; }
            public Object instantiateItem(ViewGroup parent, int position) { parent.addView(pages[position]); return pages[position]; }
            public void destroyItem(ViewGroup parent, int position, Object object) { parent.removeView((View) object); }
        });
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                profiles = position == 1;
                useList(pages[position]);
                render();
            }
        });
        tabs.setupWithViewPager(pager);
        pager.setCurrentItem(profiles ? 1 : 0, false);
        setContentView(root);
    }

    @Override void renderInventory() {
        toolbar.getMenu().findItem(2).setChecked(Settings.systemApps(this));
        add.setText(profiles ? "新建分身" : "添加分身应用");
        if (inventory == null) return;
        List<InventoryList.Item> rows = new ArrayList<>();
        String query = searches[profiles ? 1 : 0].getText().toString().toLowerCase(Locale.ROOT);
        int shown = 0;
        if (profiles) {
            int running = 0;
            for (Inventory.Profile profile : inventory.profiles) {
                if (profile.clone.running) running++;
                if (!Inventory.location(profile).toLowerCase(Locale.ROOT).contains(query)) continue;
                shown++;
                int count = 0;
                for (Inventory.Entry entry : inventory.apps) if ((!entry.system || Settings.systemApps(this))
                        && profile.packages.contains(entry.app.pkg)) count++;
                rows.add(InventoryList.Item.profile(profile, count));
            }
            status.setText(inventory.profiles.size() + " profiles · " + running + " running"
                    + (Settings.systemApps(this) ? "" : " · 已隐藏系统应用"));
        } else {
            for (Inventory.Entry entry : inventory.apps) {
                if (entry.system && !Settings.systemApps(this)) continue;
                StringBuilder locations = new StringBuilder();
                for (Inventory.Profile profile : entry.profiles) {
                    if (locations.length() > 0) locations.append("\n");
                    locations.append(Inventory.location(profile));
                }
                if (!(entry.app.label + " " + entry.app.pkg + " " + locations).toLowerCase(Locale.ROOT).contains(query)) continue;
                shown++;
                rows.add(InventoryList.Item.heading(entry));
                for (Inventory.Profile profile : entry.profiles) rows.add(InventoryList.Item.instance(entry, profile, false));
            }
            status.setText(shown + " 个分身应用 · " + (Settings.systemApps(this) ? "包含系统应用" : "已隐藏系统应用"));
        }
        if (shown == 0) {
            rows.add(InventoryList.Item.empty(query.isEmpty() ? (profiles ? "还没有分身" : "还没有分身应用") : "没有匹配结果",
                    !query.isEmpty() ? "换个名称或用户 ID，或清空搜索。"
                            : profiles ? "新建一个分身，用它保留另一套应用数据。"
                            : "先添加一个分身应用，系统应用可以从菜单里选。"));
        }
        body.show(rows);
    }

    private void deleteAll() {
        if (inventory == null || inventory.profiles.isEmpty()) return;
        List<Inventory.Profile> targets = new ArrayList<>(inventory.profiles);
        LinearLayout form = Ui.column(this);
        form.setPadding(Ui.dp(this, 24), 0, Ui.dp(this, 24), 0);
        form.addView(Ui.text(this, "将删除全部 " + targets.size() + " clone profiles and ALL their app data, including hidden system apps. Main user and work/private profiles are untouched.",
                16, R.color.muted, false));
        EditText confirm = Ui.input(this, form, "输入 DELETE 以确认");
        ScrollView scroll = new ScrollView(this);
        scroll.addView(form);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this).setTitle("删除全部分身？")
                .setView(scroll).setNegativeButton("取消", null).setPositiveButton("全部删除", null).create();
        dialog.setOnShowListener(unused -> dialog.getButton(-1).setOnClickListener(v -> {
            if (!"DELETE".equals(confirm.getText().toString())) { confirm.setError("请精确输入 DELETE"); return; }
            dialog.dismiss();
            work("正在删除分身…", () -> {
                for (Inventory.Profile profile : targets) {
                    Shell.Result result = Clones.remove(profile.clone.id, profile.serial);
                    if (!result.ok()) return new Shell.Result(-1, "停止于 " + Inventory.location(profile) + ". Earlier removals are not undone.\n" + result.out);
                }
                return new Shell.Result(0, "");
            }, null);
        }));
        dialog.show();
    }

    private void exportDiagnostics() {
        work("正在收集诊断信息…", () -> { pendingReport = Diagnostics.collect(this); return new Shell.Result(0, ""); }, () -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain").putExtra(Intent.EXTRA_TITLE, Diagnostics.fileName());
            startActivityForResult(intent, REQUEST_EXPORT);
        }, false);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT) return;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (pendingReport == null) {
                new MaterialAlertDialogBuilder(this).setTitle("报告已不可用")
                        .setMessage("应用已重启，请重新导出诊断信息以获取最新报告。")
                        .setPositiveButton("关闭", null).show();
                return;
            }
            try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if (out == null) throw new IllegalStateException("无法创建输出流");
                out.write(pendingReport.getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this, "诊断信息已保存", Toast.LENGTH_LONG).show();
            } catch (Exception error) {
                new MaterialAlertDialogBuilder(this).setTitle("诊断信息保存失败")
                        .setMessage(error.toString()).setPositiveButton("关闭", null).show();
            }
        }
        pendingReport = null;
    }

    // Keep potentially large reports out of the saved-state Binder transaction.
    @Override public Object onRetainCustomNonConfigurationInstance() { return pendingReport; }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("profiles", profiles);
        for (int i = 0; i < pages.length; i++) {
            state.putString("query" + i, searches[i].getText().toString());
            state.putParcelable("position" + i, pages[i].position());
        }
    }
}
