#!/usr/bin/env python3
"""Localises the settings screen and points the repo link at the fork."""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "src/com/lixingchi/appclone"
FORK = "github.com/gugugugugu-cmd/appclone"

PAIRS = [
    ('Ui.text(this, "Settings", 24', 'Ui.text(this, "设置", 24'),
    ('"Profiles and shortcuts"', '"分身与快捷方式"'),
    ('"After reboot"', '"重启后"'),
    ('"Could not save reboot setting"', '"无法保存重启设置"'),
    ('"Reboot setting could not be saved. Select it again to retry."',
     '"重启设置保存失败，请重新选择一次。"'),
    ('"Shortcut labels"', '"快捷方式名称"'),
    ('"Prefix clone app names"', '"在应用名前加分身编号"'),
    ('"Put the clone id before the app name, so cloned shortcuts are distinguishable.",',
     '"在应用名前面加上分身编号，方便区分各个分身的快捷方式。",'),
    ('"Format"', '"命名格式"'),
    ('"Tokens: {id} clone user id \\u00b7 {name} app name \\u00b7 {clone} clone name. "',
     '"可用变量：{id} 分身编号 · {name} 应用名 · {clone} 分身名称。"\n                        + "'),
    ('"Colons and other characters are allowed.",', '"冒号和其他字符都可以直接使用。",'),
    ('"Automation"', '"自动化"'),
    ('"Auto-pin new clone apps"', '"自动固定新装分身应用"'),
    ('"Request a home-screen shortcut as each app is added to a clone. "',
     '"每往分身里加一个应用就请求一个桌面快捷方式。"\n                        + "'),
    ('"The launcher still asks you to confirm.",', '"仍需在桌面上确认一次。",'),
    ('"Labels apply to shortcuts created from now on. Re-pin an existing shortcut "',
     '"名称只对之后创建的快捷方式生效，已有的需要重新固定一次。"\n                        + ""'),
    ('+ "to update its name.",', '+ ""'),
    ('"About"', '"关于"'),
    ('"Applies once, at least 10 seconds after the main user unlocks after reboot; Android may delay it. "',
     '"重启后主用户解锁至少 10 秒才执行一次，系统可能再延后。"\n                        + "'),
    ('+ "System default leaves profiles alone. Stop all preserves data but pauses notifications. "',
     '+ "默认不干预；全部停止会保留数据但暂停通知。"\n                        + "'),
    ('+ "Root must already be granted and available at boot. Start and shortcuts still work afterward.",',
     '+ "开机时 Root 必须已授权可用，之后启动和快捷方式照常能用。",'),
    ('github.com/doccstat/appclone', FORK),
    ('"Start all profiles"', '"启动全部分身"'),
    ('"Stop all profiles"', '"停止全部分身"'),
    ('"System default"', '"系统默认"'),
]

for name in ("SettingsActivity.java", "MainActivity.java", "BootPolicy.java"):
    path = SRC / name
    text = path.read_text()
    for old, new in PAIRS:
        text = text.replace(old, new)
    path.write_text(text)

# Manifest labels and the launcher title.
manifest = ROOT / "AndroidManifest.xml"
text = manifest.read_text()
text = text.replace('android:label="App Clone"', 'android:label="分身"')
text = text.replace('android:label="Clone" android:parentActivityName', 'android:label="分身" android:parentActivityName')
text = text.replace('android:label="Settings" android:parentActivityName', 'android:label="设置" android:parentActivityName')
manifest.write_text(text)

print("done")
