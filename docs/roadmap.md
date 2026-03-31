# Roadmap

## Sprint 1: Project Skeleton

- initialize Android project
- set up CI
- create baseline Compose settings screen
- document product and architecture

## Sprint 2: Wallpaper Generator

- build bitmap generator
- support black-only mode
- support quote mode
- add in-app preview

## Sprint 3: Apply Wallpaper

- integrate `WallpaperManager`
- apply lock-screen wallpaper with `FLAG_LOCK`
- add manual error handling and device verification

## Sprint 4: Scheduling

- store settings in `DataStore`
- add `WorkManager` periodic updates
- support cancel/update flow

## Sprint 5: Stabilization

- improve copy and polish UI
- test on multiple devices
- add app icon and store assets
- prepare internal testing release
