#!/usr/bin/env python3
"""Updates test assertions that matched English UI text.

The app is localised, so the strings the tests look for had to change with it.
Only assertion arguments are rewritten; log tags, package names and fixtures are
left alone.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "tests"

# Rendered UI text -> what the translated build now produces.
TEXT = {
    "Clone Profile": "分身",
    "No matches": "没有匹配结果",
    "No cloned apps yet": "还没有分身应用",
    "Profile stopped": "分身已停止",
    "Profile running": "分身运行中",
    "Disabled": "已停用",
    "Stopped": "已停止",
    "Personal": "个人",
    "User 11": "用户 11",
    "Chat": "聊天",
    "Manage": "管理",
    "Data is kept": "数据保留",
    "Material You": "Material You",
    "PIN, pattern or password": "PIN、图案或密码",
    "Notifications and jobs may be delayed": "通知和任务可能被延迟",
    "Android's current setting stays unchanged": "Android 当前设置保持不变",
    "did not become ready": "尚未就绪",
    "Refresh failed; showing previous list": "刷新失败，显示之前的列表",
    "Refresh failed": "刷新失败",
    "Could not read the profile list": "无法读取分身列表",
    "Could not save": "无法保存",
    "Cannot schedule": "无法调度",
    "Saved": "已保存",
    "failed: Root denied": "失败：Root denied",
    "0 succeeded, 0 failed, 0 pending": "成功 0，失败 0，待处理 0",
    "1 succeeded, 0 failed, 1 pending": "成功 1，失败 0，待处理 1",
    "1 succeeded, 1 failed, 0 pending": "成功 1，失败 1，待处理 0",
    "2 succeeded, 0 failed, 0 pending": "成功 2，失败 0，待处理 0",
}

# Untouched: log tags and fixtures the code still emits verbatim.
KEEP = {"AppClone", "AndroidRuntime", "PackageManager", "UserManagerService", "Other",
        "System fixture", "appclone-test-missing-su", "error output", "Chat\ncom.chat\n1 clone"}


def main() -> int:
    total = 0
    for path in sorted(ROOT.glob("*.java")):
        text = original = path.read_text()
        for english, chinese in TEXT.items():
            if english == chinese:
                continue
            if english in KEEP:
                continue
            # Only rewrite the inside of a quoted literal used for comparison.
            text = text.replace(f'"{english}"', f'"{chinese}"')
            text = text.replace(english, chinese) if "\n" in english else text
        if text != original:
            path.write_text(text)
            total += 1
            print(path.name)
    print(f"{total} test files updated")
    return 0


if __name__ == "__main__":
    sys.exit(main())
