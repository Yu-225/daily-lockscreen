# Daily Lockscreen

Daily Lockscreen is an Android app that generates lock-screen wallpapers with built-in or custom quotes, custom background images and a controllable text style system.

## V1 Foundation In This Repo

- redesigned multi-tab Compose UI: Home, Quotes, Design, Schedule, About
- custom quote library with built-in/custom/mixed source modes
- improved multiline quote rendering for preview and final wallpaper
- text presets for size, color, alignment, position, bold and font family
- custom image background with fit/crop/fill and dark overlay control
- background status card with worker state, last update, next eligible run and recent activity log
- adaptive launcher icon plus Play Store icon asset
- CI with lint, build and unit tests
- release workflow skeleton for signed AAB generation when signing secrets are configured

## Tech Stack

- Kotlin
- Jetpack Compose
- WorkManager
- DataStore Preferences
- WallpaperManager with `FLAG_LOCK`

## Main Paths

- `app/src/main/java/com/yulapps/dailylockscreen/ui/`
- `app/src/main/java/com/yulapps/dailylockscreen/data/`
- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `docs/assets/play_store_icon_512.png`

## Validation

The current project passes:

- `assembleDebug`
- `lintDebug`
- `testDebugUnitTest`

## Android Caveats

- Background refresh is best effort, not exact-to-the-minute.
- `PeriodicWorkRequest` cannot go below 15 minutes.
- Some OEM firmware may still throttle wallpaper updates.
