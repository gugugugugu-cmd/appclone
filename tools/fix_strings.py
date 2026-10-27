#!/usr/bin/env python3
"""Repairs the three joined string literals the localisation pass split."""
import pathlib

path = pathlib.Path(__file__).resolve().parent.parent / "src/com/lixingchi/appclone/SettingsActivity.java"
text = path.read_text()

fixes = [
    (
        '''                "重启后主用户解锁至少 10 秒才执行一次，系统可能再延后。"
                        + "
                        + "默认不干预；全部停止会保留数据但暂停通知。"
                        + "
                        + "开机时 Root 必须已授权可用，之后启动和快捷方式照常能用。",''',
        '''                "重启后主用户解锁至少 10 秒才执行一次，系统可能再延后。"
                        + "默认不干预；全部停止会保留数据但暂停通知。"
                        + "开机时 Root 必须已授权可用，之后启动和快捷方式照常能用。",''',
    ),
    (
        '''                "每往分身里加一个应用就请求一个桌面快捷方式。"
                        + "
                        + "仍需在桌面上确认一次。",''',
        '''                "每往分身里加一个应用就请求一个桌面快捷方式。"
                        + "仍需在桌面上确认一次。",''',
    ),
    (
        '''                "名称只对之后创建的快捷方式生效，已有的需要重新固定一次。"
                        + ""
                        + ""
                12, R.color.muted, false), Ui.margins(this, 0, 20, 0, 0));''',
        '''                "名称只对之后创建的快捷方式生效，已有的需要重新固定一次。",
                12, R.color.muted, false), Ui.margins(this, 0, 20, 0, 0));''',
    ),
]

for old, new in fixes:
    if old not in text:
        raise SystemExit("pattern missing:\n" + old[:60])
    text = text.replace(old, new)

path.write_text(text)
print("repaired")
