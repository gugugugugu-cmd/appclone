#!/usr/bin/env python3
"""Checks for the per-clone launcher allow list, without an SDK or device."""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "src/com/lixingchi/appclone"


def test_filter_only_touches_launcher_queries():
    source = (SRC / "LauncherFilter.java").read_text()
    # A resolve for start must never be filtered, or shortcuts and launches break.
    assert "resolveForStart" in source
    assert "Boolean.TRUE.equals(param.args[startArg])" in source
    # An explicit package/component query answers "is this app installed", not
    # "what is on the home screen", so it has to stay intact too.
    assert "intent.getPackage() == null && intent.getComponent() == null" in source


def test_filter_is_a_noop_without_an_allow_list():
    visibility = (SRC / "CloneVisibility.java").read_text()
    settings = (SRC / "Settings.java").read_text()
    # Null allow list means "show everything", so existing clones keep working.
    assert 'getStringSet(VISIBLE + userId, null)' in settings
    assert "chosen != null && !chosen.contains(pkg)" in visibility


def test_launcher_itself_stays_visible():
    visibility = (SRC / "CloneVisibility.java").read_text()
    # Without a launcher a clone cannot start, so it is never filtered out.
    assert "pkg.equals(REQUIRED_LAUNCHER)" in visibility


def test_filter_runs_in_system_server_not_the_ui():
    hook = (SRC / "MainHook.java").read_text()
    assert '"android".equals(pkg.packageName)' in hook
    assert "LauncherFilter.install(cl)" in hook
    # Scoping the module to its own package would put the hook inside every clone.
    assert "com.lixingchi.appclone".replace("com.lixingchi.appclone", '"android"') in hook


def test_choosing_an_app_adds_it_to_the_list():
    detail = (SRC / "CloneDetailActivity.java").read_text()
    assert "choose(app.pkg, true)" in detail
    assert "Settings.visibleApps(CloneDetailActivity.this, userId, chosen)" in detail


if __name__ == "__main__":
    for name, fn in sorted(globals().items()):
        if name.startswith("test_") and callable(fn):
            fn()
            print("ok", name)
