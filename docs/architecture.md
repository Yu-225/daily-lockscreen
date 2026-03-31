# Architecture

## Layering

```text
UI (Compose)
  -> ViewModel
  -> Use Cases
  -> Data / Platform services
```

## Main Components

### UI

- `MainActivity`
- `MainScreen`
- `MainViewModel`

### Domain

- `GenerateWallpaperUseCase`
- `ApplyWallpaperUseCase`
- `ScheduleWallpaperUpdateUseCase`
- `GetRandomQuoteUseCase`

### Data / Platform

- `SettingsRepository`
- `QuoteRepository`
- `WallpaperGenerator`
- `WallpaperApplier`
- `WallpaperScheduler`
- `WallpaperUpdateWorker`

## Data Flow

1. The UI updates settings through the `MainViewModel`.
2. Settings are persisted in `DataStore`.
3. The ViewModel refreshes the preview bitmap based on the latest settings.
4. `Apply now` generates a wallpaper and applies it through `WallpaperManager`.
5. Background updates use `WorkManager` and the same use-case path as manual apply.

## Why Single Module

The MVP stays in one `app` module to keep setup, build times, and onboarding simple. If the app grows, the current package structure is already ready to split into feature or data/domain modules later.
