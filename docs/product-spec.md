# Product Spec

## Product

- Name: `Daily Lockscreen`
- Package: `com.yulapps.dailylockscreen`
- Target: Android 8.0+ (`minSdk 26`)

## Goal

Refresh the lock screen with a clean black wallpaper or a black wallpaper with a short motivational quote, without requiring manual wallpaper editing each time.

## MVP Features

- Wallpaper generation
  - black background
  - centered white text
  - automatic line wrapping
- Wallpaper application
  - lock screen only
  - manual `Apply now`
- Auto update
  - `WorkManager`
  - 15 minutes, 1 hour, 6 hours, 12 hours, 24 hours
- Settings
  - auto update on/off
  - interval selection
  - wallpaper mode selection
- Quotes
  - local in-app list
  - random selection
  - avoid repeating the last applied quote when possible
- Preview
  - render the current candidate wallpaper inside the app

## Out Of Scope

- server-side quote loading
- user accounts
- sync across devices
- AI image generation
- custom wallpaper editor
- home-screen wallpaper management
- exact, second-level background timing

## Technical Constraints

- `PeriodicWorkRequest` minimum interval is 15 minutes.
- Work execution timing is best-effort and may drift due to battery and doze constraints.
- OEM firmware can change or limit background behavior.

## Success Criteria

- App launches without crashes.
- Preview renders on common phone aspect ratios.
- `Apply now` updates the lock screen on a real device.
- Auto update enqueues a single periodic worker and can be canceled cleanly.
