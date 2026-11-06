#!/usr/bin/env bash
# Construit l'appli Verre (APK signé) sans Gradle : seulement le SDK Android.
#   ANDROID_HOME=/chemin/vers/sdk ./build.sh
# Il faut : platforms;android-34 et build-tools;35.0.0, plus un JDK 17 ou plus.
set -euo pipefail

cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-$HOME/android-sdk}"
BT="$SDK/build-tools/35.0.0"
JAR="$SDK/platforms/android-34/android.jar"
OUT=build
VERSION_CODE="${VERSION_CODE:-1}"
VERSION_NAME="${VERSION_NAME:-1.0}"

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

"$BT/aapt2" compile --dir app/res -o "$OUT/res.zip"
"$BT/aapt2" link -I "$JAR" --manifest app/AndroidManifest.xml \
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
  --java "$OUT/gen" -o "$OUT/base.apk" "$OUT/res.zip"

javac -nowarn -encoding UTF-8 --release 11 -classpath "$JAR" -d "$OUT/classes" \
  $(find app/src "$OUT/gen" -name '*.java')

"$BT/d8" --release --min-api 24 --lib "$JAR" --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')

cp "$OUT/base.apk" "$OUT/unsigned.apk"
(cd "$OUT/dex" && zip -q -j ../unsigned.apk classes.dex)
"$BT/zipalign" -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
# Clé de signature locale (jamais publiée). Créée au premier build si elle n'existe pas.
KS="${VERRE_KEYSTORE:-keystore/verre.jks}"
KS_PASS_FILE="$(dirname "$KS")/password.txt"
if [ ! -f "$KS" ]; then
  mkdir -p "$(dirname "$KS")"
  head -c 18 /dev/urandom | base64 | tr -d '/+=' > "$KS_PASS_FILE"; echo >> "$KS_PASS_FILE"
  keytool -genkeypair -keystore "$KS" -storetype JKS -alias verre -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Verre Launcher" -storepass:file "$KS_PASS_FILE" -keypass:file "$KS_PASS_FILE"
fi
"$BT/apksigner" sign --ks "$KS" --ks-pass "file:$KS_PASS_FILE" \
  --out "$OUT/Verre.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify "$OUT/Verre.apk"
echo "OK -> $OUT/Verre.apk ($(du -h "$OUT/Verre.apk" | cut -f1))"
