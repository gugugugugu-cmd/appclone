#!/bin/sh
# Compile the App Clone sources against minimal Android/Xposed stubs.
# Catches type and syntax errors without the SDK. Not a substitute for a real build.
set -e
BUILD=/tmp/typecheck
rm -rf "$BUILD"
mkdir -p "$BUILD/stub/android/app" "$BUILD/stub/android/content/pm" "$BUILD/stub/android/os" \
         "$BUILD/stub/de/robv/android/xposed/callbacks" "$BUILD/out"
cat > "$BUILD/stub/Stubs.java" <<'JAVA'
package android.app;
public class Activity extends android.content.Context {}
JAVA
cat > "$BUILD/stub/A.xml" <<'XML'
XML
echo "stub shell ready"
