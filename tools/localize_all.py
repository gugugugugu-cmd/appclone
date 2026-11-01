#!/usr/bin/env python3
"""Third localisation pass: every remaining user-visible string."""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src/com/lixingchi/appclone"

PAIRS = {
    "MainActivity.java": [
        ('"Source code \\u00b7 github.com/gugugugugu-cmd/appclone"',
         '"源码 · github.com/gugugugugu-cmd/appclone"'),
        ('"No clone apps to pin"', '"分身里没有可固定的应用"'),
        ('"Pin a clone app"', '"固定分身应用"'),
        ('"Removes all " + clones.size() + " profiles and their "',
         '"将删除全部 " + clones.size() + " 个分身及其"'),
        ('"Delete all profiles?"', '"删除全部分身？"'),
        ('"Delete all", null', '"全部删除", null'),
        ('"Could not open clone app"', '"无法打开分身应用"'),
        ('"Close", null', '"关闭", null'),
    ],
    "LaunchActivity.java": [
        ('"Opening clone app\\u2026"', '"正在打开分身应用…"'),
        ('"Could not open clone app"', '"无法打开分身应用"'),
    ],
    "CloneApps.java": [
        ('"Clone " + user.hashCode()', '"分身 " + user.hashCode()'),
        ('"Clone " + serial', '"分身 " + serial'),
    ],
    "Apps.java": [
        ('"Invalid app shortcut."', '"快捷方式无效。"'),
    ],
    "Clones.java": [
        ('"Not a clone profile: "', '"不是分身："'),
        ('"Clone profile no longer exists: "', '"分身已不存在："'),
        ('"Only clone profiles belonging to user 0 can be started or stopped."',
         '"只能启动或停止属于主用户（用户 0）的分身。"'),
        ('"Cannot control an ephemeral clone: stopping it can delete its data."',
         '"无法控制临时分身：停止它可能删除其数据。"'),
    ],
    "BootReceiver.java": [
        ('"Android could not schedule the job."', '"系统无法调度该任务。"'),
    ],
    "Shortcuts.java": [
        ('"This launcher cannot pin shortcuts"', '"当前桌面不支持固定快捷方式"'),
        ('"That app is no longer in the clone"', '"该应用已不在分身中"'),
    ],
    "SettingsActivity.java": [
        ('"Source code \\u00b7 github.com/gugugugugu-cmd/appclone"',
         '"源码 · github.com/gugugugugu-cmd/appclone"'),
    ],
}

for name, pairs in PAIRS.items():
    path = ROOT / name
    text = path.read_text()
    for old, new in pairs:
        text = text.replace(old, new)
    path.write_text(text)

print("third pass done")
