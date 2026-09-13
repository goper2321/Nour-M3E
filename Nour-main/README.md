# Nour (نور) — Islamic Companion for Android

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203%20Expressive-000000.svg?style=flat)](https://m3.material.io/)
[![Room](https://img.shields.io/badge/Database-Room%20(KSP)-F58220.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Offline First](https://img.shields.io/badge/Privacy-100%25%20Offline%20First-09090B.svg?style=flat)](#privacy-first-architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Nour** (*Light* / *نور*) is a privacy-first, fully offline Islamic companion application built natively with modern **Android, Jetpack Compose**, and **Material 3 Expressive Monochrome design**. 

Engineered for precision, beauty, and complete user autonomy, Nour calculates astronomical prayer times locally without external servers, provides reliable exact-alarm Adhans, offers a hardware-fused Qibla compass with solar verification, and includes a full 604-page Holy Quran Mushaf reader.

---

## Key Features

### 1. 100% Offline Astronomical Prayer Times
* **Mathematical Precision**: Pure local astronomical calculations using solar declination and equation of time equations. Zero dependency on cloud APIs or remote tracking.
* **Global Calculation Authorities**:
  * Muslim World League (MWL)
  * Islamic Society of North America (ISNA)
  * Umm al-Qura University, Makkah
  * Egyptian General Authority of Survey
  * University of Islamic Sciences, Karachi
  * Dubai (Islamic Affairs & Charitable Activities Department)
  * Ministry of Awqaf & Islamic Affairs, Kuwait
  * Ministry of Awqaf & Islamic Affairs, Qatar
  * Majlis Ugama Islam Singapura (MUIS)
  * Diyanet İşleri Başkanlığı, Turkey
  * Institute of Geophysics, University of Tehran
* **Madhab Asr Variations**: Shafi'i / Maliki / Hanbali (shadow factor 1) and Hanafi (shadow factor 2).
* **High-Latitude Handling**: Robust twilight adjustments (Angle-based, Mid-Night, One-Seventh rule) for extreme northern and southern latitudes.
* **Monthly Timetable & Hijri Calendar**: Integrated Umm al-Qura lunar conversion with monthly schedule explorer and daily prayer time adjustments.

### 2. Celestial Sky & Solar Wave Visualizer
* **Dynamic 24-Hour Orbital Arc**: Real-time canvas simulation projecting the celestial sun and moon trajectory across day and night stages.
* **Atmospheric Scattering**: Smooth monochrome ambient horizon aura reflecting solar transit (Fajr dawn, Shuruq, Solar Noon zenith, Asr descent, Maghrib dusk, and Isha nightfall).
* **Live Countdown HUD**: Segmented countdown timer (Hours, Minutes, Seconds) with interactive progress tracking toward the upcoming Salah.

### 3. Exact Adhan Alarms & Background Service
* **Doze-Proof Exact Scheduling**: Uses Android's `AlarmManager.setExactAndAllowWhileIdle()` to ensure prayer calls fire on time even when the device is asleep.
* **Device Boot Recovery**: `RECEIVE_BOOT_COMPLETED` broadcast receiver automatically recalibrates and reschedules all alarms upon system restart.
* **Rich Adhan Notifications**: Foreground notifications with audio alert playback, prayer time information, and interactive one-tap actions (*Mark as Prayed*, *Dismiss*).

### 4. Hardware-Fused Qibla Compass
* **Great-Circle Vector**: Precise spherical trigonometry bearing calculated from current device coordinates to the Holy Kaaba in Makkah (`21.4225° N, 39.8262° E`).
* **Real-time Sensor Fusion**: Live compass dial driven by `Sensor.TYPE_ROTATION_VECTOR` and `Sensor.TYPE_ORIENTATION` with dynamic low-pass filtering.
* **Solar Azimuth Cross-Check**: Secondary solar vector rendered on the dial to verify compass alignment against the physical sun.
* **Sensor Quality Telemetry**: Live sensor accuracy status indicator (`Accurate`, `Medium`, `Calibrate`) with figure-8 calibration guidance.

### 5. 604-Page Quran Mushaf Reader
* **Standard Madinah Mushaf**: Complete 114 Surahs arranged across the classic 604-page Ottoman pagination.
* **Surah & Ayah Metadata**: Comprehensive Surah directory featuring revelation type (Meccan / Medinan), total Ayahs, Juz boundaries, and page numbers.
* **Interactive Navigation**: Seamless dual-mode reading (Page-by-page Mushaf slider and verse text view) with scalable typography and Room-backed bookmarks.

### 6. Daily Salah Obligation Tracker
* **One-Tap Completion**: Quick interactive status pills for Fajr, Dhuhr, Asr, Maghrib, and Isha.
* **Persistent Daily History**: Structured local Room database storing historical prayer logs and completion rates over time.

---

## Material 3 Expressive Monochrome Design

Nour incorporates a **Material 3 Expressive Monochrome design language** that emphasizes focus, tranquility, and distraction-free devotion:

* **High-Contrast Tonal Palette**: Refined grayscale spectrum ranging from pitch black (`#000000`) and obsidian (`#09090B`) through silver and platinum to pure white (`#FFFFFF`).
* **Expressive Geometry**: Soft squircle containers, pill buttons, and rounded card elevations defined via M3 `ExpressiveShapes`.
* **Adaptive Dark & Light Modes**: Seamless transition between high-contrast daylight cards and battery-saving OLED dark canvas.
* **Edge-to-Edge Architecture**: Strict window inset management handling system navigation bars, gesture bars, and display cutouts.

---

## Architecture & Tech Stack

The application follows modern Android architecture patterns (MVVM / Clean Architecture):

```
app/src/main/java/com/example/
├── MainActivity.kt                 # Single Activity entry point & navigation host
├── NourApp.kt                      # Application class, Room DB & notification channel setup
├── database/                       # Local Room Persistence
│   ├── Entities.kt                 # PrayerLog, QuranBookmark, UserSettings entities
│   ├── Daos.kt                     # Type-safe Room DAOs
│   └── NourDatabase.kt             # SQLite database instance with KSP
├── features/
│   ├── adhan/                      # Adhan Scheduling & Audio Engine
│   │   ├── player/                 # Adhan audio playback
│   │   └── scheduler/              # AlarmManager receivers & notification dispatchers
│   ├── prayer/                     # Prayer Times & Obligation Tracker
│   │   ├── calculation/            # Offline solar equation calculation engine
│   │   ├── models/                 # CalculationMethod, Madhab, PrayerTimes models
│   │   ├── providers/              # Geolocation & city coordinate providers
│   │   └── ui/                     # Prayer cards, tracker bar & timetable composables
│   ├── qibla/                      # Qibla Compass Engine
│   │   ├── QiblaCalculator.kt      # Great-circle bearing & solar azimuth math
│   │   └── ui/                     # Animated compass rose & sensor UI
│   ├── quran/                      # Holy Quran Mushaf
│   │   ├── data/                   # 114 Surahs repository & page indexes
│   │   ├── models/                 # Surah, Ayah & QuranPage data classes
│   │   └── ui/                     # Mushaf page visualizer & Surah drawer
│   ├── settings/                   # Customization
│   │   └── ui/                     # Calculation method, madhab, and notification controls
│   └── widgets/                    # Core Visualizers
│       └── SkyVisualizer.kt        # 24-hour celestial orbit & solar wave canvas
└── ui/theme/                       # Design System
    ├── Color.kt                    # M3 Monochrome tonal token system
    ├── Shapes.kt                   # Expressive squircle & pill shape scales
    ├── Theme.kt                    # Light / Dark ColorScheme orchestration
    └── Type.kt                     # Expressive typography scale
```

### Core Libraries & Tools
* **Jetpack Compose (BOM 2024.09.00)**: Declarative UI toolkit.
* **Material 3 (`androidx.compose.material3`)**: Modern M3 Expressive components.
* **Room Database (`2.6.1`)**: Local SQLite persistence with Kotlin Symbol Processing (KSP).
* **Coroutines & StateFlow**: Reactive state management and asynchronous processing.
* **AndroidX Core KTX & Lifecycle**: `lifecycle-runtime-compose` with `collectAsStateWithLifecycle`.

---

## Privacy-First Architecture

Unlike many contemporary Islamic apps, **Nour does not collect personal data**:
* **No Analytics or Trackers**: No third-party SDKs, telemetry, or user profiling.
* **No External Cloud Calls**: Prayer calculations and Qibla bearings occur 100% on-device.
* **Zero Advertisements**: Free, quiet, and completely ad-free forever.
* **Transparent Permissions**:
  * `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Required exclusively on-device to compute solar angles and Qibla bearing. Coordinates can also be set manually without granting location permissions.
  * `SCHEDULE_EXACT_ALARM`: Ensures punctual Adhan notifications at the exact computed minute.
  * `POST_NOTIFICATIONS`: Delivers daily Adhan reminders.
  * `RECEIVE_BOOT_COMPLETED`: Re-registers scheduled prayer alarms after phone reboot.

---

## Getting Started

### Prerequisites
* **Android Studio Ladybug (2024.2.1+)** or newer.
* **JDK 17** or **JDK 21**.
* **Android SDK**: `minSdk 24` (Android 7.0+), `targetSdk 36` (Android 15+).

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/nour-islamic-app.git
   cd nour-islamic-app
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync dependencies.
4. Run on a connected physical device or emulator:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Unit Tests
Execute Robolectric unit tests locally without an emulator:
```bash
./gradlew :app:testDebugUnitTest
```

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
