# Release signing

Generate a private key once on a trusted device:

```sh
keytool -genkeypair -keystore release.jks -alias iing -keyalg RSA -keysize 3072 -validity 10000
```

Back up the keystore and passwords securely. Never commit them. In repository Settings > Secrets and variables > Actions, add ANDROID_KEYSTORE_BASE64 (base64 of release.jks), ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS (iing), ANDROID_KEY_PASSWORD. Run Signed APK Release manually. The workflow publishes v<versionCode> and IING-STD-Signal-Lock.apk. Increment versionCode for each release. Install first signed release, then test upgrade to next signed release. Debug builds use a different signing key and cannot be upgraded to release builds in place.
