#!/usr/bin/env python3
"""Rewrites test assertions that compare against now-localised UI text.

Each entry names the exact assertion text; the replacement is what the Chinese
build produces. Log tags, package names and fixtures are deliberately absent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "tests"

PAIRS = [
    # Inventory.state() / row subtitles
    ('contains("Disabled")', 'contains("已停用")'),
    ('contains("stopped")', 'contains("已停止")'),
    ('contains("Disabled · Profile stopped")', 'contains("已停用 · 分身已停止")'),
    ('contains("Disabled · Profile running")', 'contains("已停用 · 分身运行中")'),
    ('contains("Profile stopped")', 'contains("分身已停止")'),
    # empty / search states
    ('contains("No cloned apps yet")', 'contains("还没有分身应用")'),
    ('contains("No matches")', 'contains("没有匹配结果")'),
    # inventory screen chrome
    ('contains("User 11")', 'contains("用户 11")'),
    ('contains("Personal")', 'contains("个人")'),
    ('contains("Manage")', 'contains("管理")'),
    ('contains("Clone Profile")', 'contains("分身")'),
    # refresh status
    ('contains("Refresh failed; showing previous list")', 'contains("刷新失败，显示之前的列表")'),
    ('contains("Refresh failed")', 'contains("刷新失败")'),
    # boot policy summaries (rendered text)
    ('contains("2 succeeded, 0 failed, 0 pending")', 'contains("成功 2，失败 0，待处理 0")'),
    ('contains("1 succeeded, 0 failed, 1 pending")', 'contains("成功 1，失败 0，待处理 1")'),
    ('contains("1 succeeded, 1 failed, 0 pending")', 'contains("成功 1，失败 1，待处理 0")'),
    ('contains("0 succeeded, 0 failed, 0 pending")', 'contains("成功 0，失败 0，待处理 0")'),
    ('contains("Cannot schedule")', 'contains("无法调度")'),
    ('contains("Could not read the profile list")', 'contains("无法读取分身列表")'),
    ('contains("Could not save")', 'contains("无法保存")'),
    ('contains("Saved")', 'contains("已保存")'),
    # profile start messages
    ('contains("did not become ready")', 'contains("尚未就绪")'),
    ('contains("PIN, pattern or password")', 'contains("PIN、图案或密码")'),
    # background controls explanation
    ('contains("Data is kept")', 'contains("数据保留")'),
    ('contains("Notifications and jobs may be delayed")', 'contains("通知和任务可能被延迟")'),
    ("contains(\"Android's current setting stays unchanged\")",
     'contains("Android 当前设置保持不变")'),
]


def main() -> int:
    missing = []
    for path in sorted(ROOT.glob("*.java")):
        text = original = path.read_text()
        for old, new in PAIRS:
            if old in text:
                text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            print(path.name)
    for old, _ in PAIRS:
        if not any(old in p.read_text() for p in ROOT.glob("*.java")):
            missing.append(old)
    if missing:
        print("not found anywhere (already updated):")
        for m in missing:
            print("   ", m)
    return 0


if __name__ == "__main__":
    sys.exit(main())
