#!/usr/bin/env bash
# Builds QuietFeed.apk with the plain Android SDK tools (no Gradle needed).
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:?set ANDROID_HOME}}"
BT="$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)"
AJ="$(ls -d "$SDK"/platforms/android-* | sort -V | tail -1)/android.jar"
echo "build-tools: $BT"; echo "android.jar: $AJ"

rm -rf build && mkdir -p build/classes build/dex
"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -o build/base.apk -I "$AJ" --manifest AndroidManifest.xml build/res.zip --java build/gen
javac --release 8 -nowarn -classpath "$AJ" -d build/classes $(find src build/gen -name '*.java')
"$BT/d8" --release --min-api 26 --lib "$AJ" --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks key.jks --ks-pass pass:quietfeed --out QuietFeed.apk build/aligned.apk
"$BT/apksigner" verify QuietFeed.apk && echo "Built QuietFeed.apk"
