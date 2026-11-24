# IING STD Signal Lock

Target: Redmi Note 13 Pro+ 5G, MediaTek Dimensity 7200-Ultra, non-root.
User verified EngineerMode and BandMode SIM2 are accessible via dialer. Applying band changes and third-party access remain unverified.

## Requirements
- Real signal metrics and dual SIM selection; no fabricated readings.
- Network mode and BandMode shortcuts with capability checks; no false lock success.
- Dark neon green UI.
- In-app GitHub Releases update check, APK download and Android installation confirmation. Stable private signing key across releases; never commit key. Verify package and signature.
- Document recovery to automatic network settings.

## Status
Initial Android app implemented; build #5 passed assembleDebug and lintDebug (run 45296327466). Subsequent commit b589d07 adds direct BandMode activity attempt with fallback, recovery guidance, signal indicator and system bar insets; CI and physical device tests pending. Update check currently opens GitHub release page only. Next: stable signing configuration, verified APK download and installer, release publishing, device validation of SIM selection and modem access. Do not distribute debug APK as final update-compatible release.


## Updater milestone
Build 45296617526 passed. Updater.java and InstallReceiver.java now implement version check (numeric v<versionCode> tags), bounded APK download with progress, package/version/signature comparison, and PackageInstaller confirmation. Manifest and dashboard connected. CI and device verification pending. Next: configure private persistent release signing key via GitHub secrets, publish signed APK named IING-STD-Signal-Lock.apk, test update from one signed release to next. Debug APKs are not suitable for permanent updates.
