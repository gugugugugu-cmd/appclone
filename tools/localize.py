#!/usr/bin/env python3
"""Localises App Clone's UI strings. Run from the repo root; safe to re-run."""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src/com/lixingchi/appclone"

# Longest first so a short key never eats a longer one.
PAIRS = [
    ('"Run apps in separate users"', '"让应用跑在独立用户里"'),
    ('"+  New profile"', '"＋ 新建分身"'),
    ('"Reading clones\\u2026"', '"正在读取分身…"'),
    ('"No profiles yet"', '"还没有分身"'),
    ('"Create a clone profile, then choose the apps to install into it."',
     '"先建一个分身，再勾选要装进去的应用。"'),
    ('"User " + clone.id + " \\u00b7 "', '"用户 " + clone.id + " · "'),
    ('clone.running ? "Running" : "Stopped"', 'clone.running ? "运行中" : "已停止"'),
    ('clone.running ? "Stop" : "Start"', 'clone.running ? "停止" : "启动"'),
    ('(clone.running ? "Stop " : "Start ")', '(clone.running ? "停止 " : "启动 ")'),
    ('+ ", user " + clone.id', '+ "，用户 " + clone.id'),
    ('"Stop " + clone.name + "?"', '"停止 " + clone.name + "？"'),
    ('"Starting profile\\u2026" : "Stopping profile\\u2026"',
     '"正在启动分身…" : "正在停止分身…"'),
    ('"Could not start profile" : "Could not stop profile"',
     '"无法启动分身" : "无法停止分身"'),
    ('"Root command failed."', '"Root 命令执行失败。"'),
    ('setPositiveButton("Close", null)', 'setPositiveButton("关闭", null)'),
    ('menu.getMenu().add(0, 1, 0, "Settings")', 'menu.getMenu().add(0, 1, 0, "设置")'),
    ('menu.getMenu().add(0, 2, 1, "Pin a shortcut")',
     'menu.getMenu().add(0, 2, 1, "固定快捷方式")'),
    ('menu.getMenu().add(0, 3, 2, "Delete all profiles")',
     'menu.getMenu().add(0, 3, 2, "删除全部分身")'),
    ('menu.getMenu().add(0, 4, 3, "Export diagnostics")',
     'menu.getMenu().add(0, 4, 3, "导出诊断信息")'),
    ('"No clones to delete"', '"没有可删除的分身"'),
    ('form.addView(Ui.label(this, "Name"));', 'form.addView(Ui.label(this, "名称"));'),
    ('form.addView(Ui.label(this, "How many"),', 'form.addView(Ui.label(this, "数量"),'),
    ('"Append -1, -2, \\u2026 to names"', '"在名称后追加 -1、-2 …"'),
    ('.setTitle("New profile")', '.setTitle("新建分身")'),
    ('.setNegativeButton("Cancel", null)', '.setNegativeButton("取消", null)'),
    ('.setPositiveButton("Create", new DialogInterface', '.setPositiveButton("创建", new DialogInterface'),
    ('typed.isEmpty() ? "Clone" : typed', 'typed.isEmpty() ? "分身" : typed'),
    ('return "Clone Profile";', 'return "分身";'),
    ('"Creating " + (howMany == 1 ? "clone" : howMany + " clones") + "\\u2026"',
     '"正在创建分身（" + howMany + "）…"'),
    ('(madeFinal == 1 ? " profile created" : " profiles created")', '" 个分身已创建"'),
    ('madeFinal + (madeFinal == 1 ? " profile created"', 'madeFinal + (madeFinal == 1 ? " 个分身已创建"'),
    ('.setTitle("Clone creation failed")', '.setTitle("分身创建失败")'),
    ('"Export logs"', '"导出日志"'),
    ('"Collecting diagnostics\\u2026"', '"正在收集诊断信息…"'),
    ('.setNegativeButton("Stop", (dialog, which) -> setRunning(clone.id, false))',
     '.setNegativeButton("停止", (dialog, which) -> setRunning(clone.id, false))'),
]

for name in ("MainActivity.java", "SettingsActivity.java", "LaunchActivity.java",
             "CloneDetailActivity.java"):
    path = ROOT / name
    if not path.is_file():
        continue
    text = path.read_text()
    for old, new in PAIRS:
        text = text.replace(old, new)
    path.write_text(text)

print("done")
