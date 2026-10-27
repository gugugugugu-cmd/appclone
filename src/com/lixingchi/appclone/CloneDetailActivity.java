package com.lixingchi.appclone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shows the apps available to one clone and installs, opens, or removes them. */
public final class CloneDetailActivity extends Activity {
    static final String EXTRA_ID = "id";
    static final String EXTRA_NAME = "name";

    private int userId;
    private String userName;
    private final List<Apps.App> apps = new ArrayList<>();
    private final Set<String> installed = new HashSet<>();
    private final Set<String> busy = new HashSet<>();
    private AppAdapter adapter;
    private TextView status;
    private boolean loading;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        userId = getIntent().getIntExtra(EXTRA_ID, -1);
        userName = getIntent().getStringExtra(EXTRA_NAME);
        if (userName == null) userName = "分身";

        LinearLayout root = Ui.column(this);
        Ui.applyInsets(root, 20, 22, 20, 16);

        LinearLayout head = Ui.row(this);
        head.addView(Ui.avatar(this, String.valueOf(userId)));
        LinearLayout titles = Ui.column(this);
        titles.addView(Ui.text(this, userName, 22, R.color.ink, true));
        status = Ui.text(this, "用户 " + userId, 13, R.color.muted, false);
        titles.addView(status);
        head.addView(titles, Ui.weightWithStart(this, 1, 14));
        root.addView(head);
        root.addView(Ui.label(this, "应用（勾选的项目才会出现在该分身的桌面）"),
                Ui.margins(this, 4, 22, 0, 8));

        ListView list = new ListView(this);
        list.setDivider(new android.graphics.drawable.InsetDrawable(
                new android.graphics.drawable.ColorDrawable(getColor(R.color.line)),
                Ui.dp(this, 56), 0, 0, 0));
        list.setDividerHeight(Math.max(1, Ui.dp(this, 1)));
        list.setSelector(Ui.selectable(this));
        adapter = new AppAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button delete = Ui.quietDanger(this, "删除分身");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { confirmDelete(); }
        });
        root.addView(delete, Ui.margins(this, 0, 6, 0, 0));
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        if (loading || userId < 0) return;
        loading = true;
        status.setText("用户 " + userId + " · 正在读取应用…");
        new Thread(new Runnable() {
            @Override public void run() {
                final List<Apps.App> found = Apps.launcherApps(CloneDetailActivity.this, getPackageName());
                final Set<String> have = Apps.installed(userId);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        loading = false;
                        apps.clear();
                        apps.addAll(found);
                        installed.clear();
                        installed.addAll(have);
                        adapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });
            }
        }, "clone-apps").start();
    }

    private void updateStatus() {
        int count = 0;
        for (Apps.App app : apps) if (installed.contains(app.pkg)) count++;
        status.setText("用户 " + userId + " · 已安装 " + count + "/" + apps.size() + " 个应用"
                + (Settings.hasVisibleApps(CloneDetailActivity.this, userId)
                ? " · 桌面仅显示勾选项" : ""));
    }

    private void toggle(final Apps.App app, final boolean want) {
        if (busy.contains(app.pkg)) return;
        busy.add(app.pkg);
        adapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override public void run() {
                final Shell.Result result = want ? Apps.install(userId, app.pkg)
                        : Apps.uninstall(userId, app.pkg);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        busy.remove(app.pkg);
                        if (result.ok()) {
                            if (want) {
                                installed.add(app.pkg);
                                // The app the user just picked belongs on the clone's
                                // home screen; anything that merely came along with it
                                // stays out of the drawer.
                                choose(app.pkg, true);
                                if (Settings.autoPin(CloneDetailActivity.this)) {
                                    Shortcuts.pin(CloneDetailActivity.this,
                                            new CloneApps.Target(userId, userName, app));
                                }
                            } else {
                                installed.remove(app.pkg);
                                choose(app.pkg, false);
                            }
                        } else if (want && result.out.contains("already installed")) {
                            // Installed before this option existed: the package is
                            // there, but it is not yet on the clone's allow list.
                            choose(app.pkg, true);
                        } else {
                            Toast.makeText(CloneDetailActivity.this,
                                    app.label + ": " + firstLine(result.out), Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });
            }
        }, "clone-app").start();
    }

    /** Adds or removes one package from this clone's launcher allow list. */
    private void choose(String pkg, boolean visible) {
        Set<String> chosen = new HashSet<>();
        Set<String> current = Settings.visibleApps(CloneDetailActivity.this, userId);
        if (current != null) chosen.addAll(current);
        if (visible) chosen.add(pkg);
        else chosen.remove(pkg);
        Settings.visibleApps(CloneDetailActivity.this, userId, chosen);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("删除 " + userName + "？")
                .setMessage("将删除这个分身及其全部应用数据。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { delete(); }
                })
                .show();
    }

    private void delete() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("正在删除…");
        progress.setCancelable(false);
        progress.show();
        new Thread(new Runnable() {
            @Override public void run() {
                Clones.remove(userId);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        progress.dismiss();
                        finish();
                    }
                });
            }
        }, "clone-remove").start();
    }

    private static String firstLine(String value) {
        int newline = value.indexOf('\n');
        return newline < 0 ? value : value.substring(0, newline);
    }

    private final class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return apps.size(); }
        @Override public Apps.App getItem(int position) { return apps.get(position); }
        @Override public long getItemId(int position) { return apps.get(position).pkg.hashCode(); }
        @Override public boolean hasStableIds() { return true; }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            AppRow row;
            if (convertView == null) {
                row = new AppRow();
                convertView = row.root;
                convertView.setTag(row);
            } else {
                row = (AppRow) convertView.getTag();
            }
            row.bind(apps.get(position));
            return convertView;
        }
    }

    private final class AppRow {
        final LinearLayout root;
        final CheckBox check;
        final TextView label;
        final TextView meta;
        final Button open;
        Apps.App app;

        AppRow() {
            Activity activity = CloneDetailActivity.this;
            check = new CheckBox(activity);
            check.setClickable(false);
            check.setFocusable(false);
            label = Ui.text(activity, "", 16, R.color.ink, false);
            meta = Ui.text(activity, "", 12, R.color.muted, false);
            open = Ui.quiet(activity, "打开");
            open.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            open.setMinHeight(Ui.dp(activity, 40));
            open.setPadding(Ui.dp(activity, 14), 0, Ui.dp(activity, 14), 0);

            LinearLayout texts = Ui.column(activity);
            texts.addView(label);
            texts.addView(meta);

            root = Ui.row(activity);
            root.setPadding(0, Ui.dp(activity, 10), 0, Ui.dp(activity, 10));
            root.addView(check);
            root.addView(texts, Ui.weight(1));
            root.addView(open);

            root.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (app != null) toggle(app, !installed.contains(app.pkg));
                }
            });
            open.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (app == null) return;
                    startActivity(new Intent(CloneDetailActivity.this, LaunchActivity.class)
                            .putExtra(LaunchActivity.EXTRA_USER, userId)
                            .putExtra(LaunchActivity.EXTRA_PKG, app.pkg)
                            .putExtra(LaunchActivity.EXTRA_ACTIVITY, app.activity)
                            .putExtra(LaunchActivity.EXTRA_LABEL, app.label));
                }
            });
        }

        void bind(Apps.App value) {
            app = value;
            label.setText(value.label);
            meta.setText(value.pkg);
            boolean isInstalled = installed.contains(value.pkg);
            check.setChecked(isInstalled);
            open.setVisibility(isInstalled ? View.VISIBLE : View.GONE);
            boolean locked = busy.contains(value.pkg);
            root.setEnabled(!locked);
            root.setAlpha(locked ? 0.5f : 1f);
        }
    }
}
