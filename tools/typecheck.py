#!/usr/bin/env python3
"""Type-checks src/ against a real android.jar plus the Xposed API jar.

R needs aapt2, so a stand-in R class is generated from the res/ tree.
Usage: ANDROID_JAR=<android.jar> XPOSED_JAR=<api-82.jar> python3 tools/typecheck.py
"""
import os
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parent.parent
ANDROID = os.environ.get("ANDROID_JAR")
XPOSED = os.environ.get("XPOSED_JAR")
if not ANDROID or not XPOSED:
    raise SystemExit("set ANDROID_JAR and XPOSED_JAR")

java = shutil.which("javac") or "javac"
out = pathlib.Path(tempfile.mkdtemp(prefix="typecheck-"))
pkg = out / "com/lixingchi/appclone"
pkg.mkdir(parents=True)

# Collect every @color/... and @dimen/... name used by the sources.
fields = {"color": set(), "dimen": set(), "style": set(), "drawable": set(), "array": set()}
for source in (ROOT / "src").rglob("*.java"):
    for kind, name in re.findall(r"\bR\.(\w+)\.(\w+)", source.read_text()):
        if kind in fields:
            fields[kind].add(name)

lines = ["package com.lixingchi.appclone;", "public final class R {"]
for kind, names in fields.items():
    lines.append(f"  public static final class {kind} {{")
    for name in sorted(names):
        kind_type = "int"
        lines.append(f"    public static final {kind_type} {name} = 0;")
    lines.append("  }")
lines.append("}")
(pkg / "R.java").write_text("\n".join(lines) + "\n")

sources = sorted(str(p) for p in ROOT.joinpath("src").rglob("*.java"))
sources.append(str(pkg / "R.java"))
command = [java, "--release", "8", "-nowarn", "-Xlint:none",
           "-cp", os.pathsep.join([ANDROID, XPOSED]), "-d", str(out / "classes"), *sources]
result = subprocess.run(command, capture_output=True, text=True)
print(result.stdout or "", end="")
print(result.stderr or "", end="", file=sys.stderr)
sys.exit(result.returncode)
