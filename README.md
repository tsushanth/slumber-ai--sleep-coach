# Slumber AI: Sleep Coach

A native Android sleep-tracking app built with Jetpack Compose. Slumber AI records a night's sleep using the phone's microphone, estimates sleep stages over the session, detects and logs snoring events, and summarizes each night with a quality score and history you can browse over time.

## Features

- **One-tap sleep tracking** — start/stop a session from the home screen; tracking continues in the background via a foreground service with a persistent notification.
- **Live session view** — elapsed time, current sleep stage, a live audio waveform, and a running snoring event count while tracking.
- **Sleep stage analysis** *(Premium)* — a real-time and post-session timeline across Awake, Light, Deep, and REM stages.
- **Snoring detection** *(Premium)* — configurable sensitivity, per-event timeline with timestamps and peak amplitude, and total snoring duration per session.
- **Sleep history** — every session is saved locally with duration, quality score, and stage/snoring breakdown; free accounts see the most recent nights, Premium unlocks unlimited history.
- **Quality scoring** — each session gets a 0–100 score based on total duration and snoring frequency.
- **Premium paywall** — weekly, yearly, and lifetime tiers via Google Play Billing, with purchase restore support.
- **Material 3 theming** — dynamic color (Android 12+) with a bundled dark/light fallback palette, following the system's day/night setting throughout the OS chrome and in-app UI.
- **Accessibility** — content descriptions on all interactive elements, TalkBack-friendly merged semantics on cards and lists, and haptic feedback on key interactions.

## Requirements

- Android Studio Koala (2024.1.1) or newer
- JDK 17
- Android SDK Platform 34
- Minimum supported device OS: **Android 7.0 (API 24)**
- Target/compile SDK: **Android 14 (API 34)**
- A physical device or emulator with a microphone for sleep/snoring tracking (required at runtime; the `RECORD_AUDIO` permission is requested on first use)

## Build Instructions

Clone or open the project directory in Android Studio and let it sync, or build from the command line:

```bash
# Debug build
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires a connected device/emulator)
./gradlew connectedAndroidTest
```

> **Note:** if your checkout's folder path contains a colon (`:`), some Kotlin/Gradle toolchain versions fail to resolve build outputs due to path-canonicalization issues unrelated to this project's source. If you hit an "Internal compiler error" referencing a missing `R.jar`, rename the parent folder to remove the colon.

The debug APK does not require any additional configuration — Google Play Billing calls are safe to exercise in a debug/test environment using [license testing](https://developer.android.com/google/play/billing/test) accounts.

## Project Structure

```
app/src/main/java/com/factory/slumberaisleepcoach/
├── MainActivity.kt              # Single-activity entry point, sets up the Compose tree
├── billing/                     # Google Play Billing integration
│   ├── BillingManager.kt        #   connection lifecycle, purchase flow, purchase events
│   ├── BillingProducts.kt       #   PremiumProduct tiers (weekly/yearly/lifetime/etc.)
│   ├── PremiumManager.kt        #   persisted premium entitlement state
│   └── PricingFormat.kt         #   localized price formatting helpers
├── data/
│   ├── database/                # Room database, DAOs (sleep sessions, snoring records)
│   ├── entities/                # Room entities (SleepSession, SnoringRecord)
│   └── repository/              # SleepRepository (DB access) and SettingsRepository (DataStore)
├── model/
│   └── SleepStage.kt            # Sleep stage enum + heuristic stage-cycle estimation
├── service/
│   └── SleepTrackingService.kt  # Foreground service keeping tracking alive with a notification
├── ui/
│   ├── SlumberApp.kt            # Navigation graph, permission handling, top-level Scaffold
│   ├── components/              # Reusable Composables (cards, charts, list items, badges)
│   ├── navigation/               # Route constants
│   ├── screens/                 # One Composable per app screen
│   └── theme/                   # Material 3 color scheme, typography, dynamic theming
├── util/
│   └── TimeFormat.kt            # Duration/date/time formatting helpers
└── viewmodel/
    ├── SleepViewModel.kt        # Tracking session state machine, audio analysis, DB writes
    └── SleepViewModelFactory.kt
```

### Screens

| Screen | Route | Purpose |
|---|---|---|
| Onboarding | `onboarding` | First-run welcome and feature highlights |
| Home | `home` | Start tracking, quick stats, recent sessions |
| Tracking | `tracking` | Live session view while recording |
| History | `history` | Full session history list |
| Session Detail | `session_detail/{sessionId}` | Per-night stage/snoring breakdown |
| Settings | `settings` | Snoring sensitivity, premium status |
| Paywall | `paywall` | Premium tier selection and purchase |
