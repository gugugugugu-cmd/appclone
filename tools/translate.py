#!/usr/bin/env python3
"""Rewrites user-visible Java string literals to Chinese using tools/strings_zh.py.

Only literals listed in the table are touched, so log tags, class names, shell
commands and regexes are left alone. Run from the repo root:
    python3 tools/translate.py [--check]
"""
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from strings_zh import T  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src/com/lixingchi/appclone"


def main() -> int:
    check_only = "--check" in sys.argv[1:]
    total = 0
    for path in sorted(ROOT.glob("*.java")):
        text = original = path.read_text()
        hits = 0
        for source, target in T.items():
            if source == target:
                continue
            if source in text:
                hits += text.count(source)
                text = text.replace(source, target)
        if text != original:
            total += hits
            print(f"{path.name}: {hits}")
            if not check_only:
                path.write_text(text)
    print(f"{'would replace' if check_only else 'replaced'} {total} literals")
    return 0


if __name__ == "__main__":
    sys.exit(main())
