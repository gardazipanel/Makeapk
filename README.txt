APK MAKER - Android project

GitHub se (aasan):
1. github.com par nayi repo banayein, is ZIP ko unzip kar ke saari files upload karein (.github folder samet)
2. Actions tab -> "Build APK Maker" -> Run workflow
3. 5-8 minute baad Artifacts se "APK-Maker" download karein, zip kholein, app-debug.apk phone mein install karein

Android Studio se:
1. app/src/main/assets/ mein sign.p12 banayein:
   keytool -genkeypair -alias key -keyalg RSA -keysize 2048 -validity 36500 -storetype PKCS12 -keystore app/src/main/assets/sign.p12 -storepass apkmaker -keypass apkmaker -dname "CN=APK Maker" -J-Dkeystore.pkcs12.legacy
2. gradle :shell:assembleRelease
3. shell/build/outputs/apk/release/shell-release.apk ko app/src/main/assets/template.apk naam se copy karein
4. gradle :app:assembleDebug
