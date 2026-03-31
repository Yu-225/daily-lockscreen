# Release Process

## Current Goal

Ship a stable MVP to Google Play internal testing after device validation.

## Pre-Release Checklist

- verify preview rendering on at least one real device
- verify `Apply now`
- verify auto update scheduling
- verify behavior after reboot
- verify behavior after force stop and relaunch

## GitHub / CI Setup

Recommended repository configuration:

- protect `main`
- require pull requests
- require CI success before merge
- disable force pushes to `main`

Suggested labels:

- `bug`
- `feature`
- `ci`
- `release`
- `refactor`

## Future Release Secrets

Store these in GitHub repository or environment secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `PLAY_SERVICE_ACCOUNT_JSON`

## Play Store Path

1. Generate signed AAB.
2. Upload to internal testing.
3. Validate install/update behavior.
4. Prepare store listing assets.
5. Expand to broader testing only after wallpaper behavior is validated on real devices.
