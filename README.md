# Lean Bulk Tracker (60 to 70 kg)

An offline-first Android fitness, hypertrophy, and nutrition tracking application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, **Android Health Connect**, and the **Android YouTube Player** library.

Designed specifically for lean-bulk progression (60.0 kg $\rightarrow$ 70.0 kg) utilizing minimal equipment (single 4 kg dumbbell + bodyweight) paired with a high-protein South Indian nutritional framework.

---

## Key Features

### 1. 7-Day Video-Segmented Daily Workout Player
- **Daily Rotation**: Automatically loads the day's training split (Chest & Push Hypertrophy, Legs/Back/Core, Dedicated Arms, Lower Body Volume, Shoulders, Back Thickness, Active Mobility).
- **Embedded YouTube Player**: Uses `Pierfrancescosoffritti/android-youtube-player` to cue precise video segments with start/end second timestamps.
- **No Unsolicited Autoplay**: Videos cue cleanly with thumbnails; playback only starts when user taps play.
- **Segment Checklist**: Track completed sets and auto-advance when segments end.

### 2. South Indian Hypertrophy Meal Tracker (2,420 kcal)
- **Calibrated Meal Plan**: 5 daily structured meals (Breakfast Tiffin, Mid-Morning PB Banana Shake, Lunch, Pre-Workout Sundal, Dinner).
- **Veg / Non-Veg Swaps**: 1-tap toggling between whole eggs/chicken curry and paneer/soya chunks with automatic macro adjustment.
- **Dynamic Top-Ups**: Add custom meals or "+ Extra Rice" top-ups directly to daily logs.

### 3. Dynamic Nutrition & Hardware Step Counter Engine
- **Mifflin-St Jeor TDEE Model**: Real-time calorie adjustments based on current weight, age, height, step activity, and completed workout segments.
- **Morning Workout Anticipation**: Training days anticipate scheduled workout burn prior to 8:00 PM, maintaining target stability.
- **Dual Hardware Sensor Integration**: Directly reads hardware Qualcomm sensors (`TYPE_STEP_COUNTER` and `TYPE_STEP_DETECTOR`) with runtime `ACTIVITY_RECOGNITION` support.
- **Health Connect Integration**: Reads `StepsRecord`, `ActiveCaloriesBurnedRecord`, and `TotalCaloriesBurnedRecord` with automatic fallback.
- **Smart Compensation Banners**: Triggers calibrated high-burn South Indian snack recommendations (e.g. Banana & PB, Rice + Ghee + Egg, Whole Milk + Roasted Chana) when daily expenditure exceeds baseline by $\ge 150\text{ kcal}$.

### 4. Dynamic Body Dimension & Waist Predictor
- **McCallum Wrist Symmetry**: Calculates ideal aesthetic frame proportions based on wrist bone circumference.
- **3D Lean Mass Scaling**: Forecasts muscle girth expansion at target weight using cross-sectional lean mass scaling ($w^{0.38}$).
- **Visceral Fat Guardrail**: Computes maximum safe healthy waist ceiling ($30.1''$) and warns if waist-to-height ratio (WHtR) exceeds $0.48$.
- **Dynamic Dual Trajectory Chart**: Canvas-based line chart showing weight and waist progression with auto-scaling Y-axes and dynamic target lines.

### 5. Full Offline Data Export & Migration
- **Zero Cloud Dependence**: Export all app data (Profile biometrics, weigh-in history, meal check-ins, workout completions, and reminder schedules) to a single portable `.json` file.
- **Storage Access Framework & System Share**: Save directly to local storage or share via Google Drive, WhatsApp, Gmail, or Bluetooth.
- **1-Tap Import & Restore**: Restore on a new device with automatic Room `@Upsert` operations and notification re-scheduling.

### 6. Local Reminder System
- **AlarmManager & WorkManager**: Scheduled meal windows, pre-workout nutrition alerts, and weekly Thursday weigh-in reminders with boot auto-recovery (`BOOT_COMPLETED`).

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.1.0
- **UI Toolkit**: Jetpack Compose (Material 3) with Dark Neon Cyan & Gold theme
- **Architecture**: MVVM + Repository Pattern + Kotlin StateFlow / Coroutines
- **Local Persistence**: Room Database 2.7.0 (5 entities: UserProfile, WeightEntry, DailyMealLog, DailyWorkoutLog, Reminder)
- **Health & Fitness**: `androidx.health.connect:connect-client:1.1.0-alpha11` + Android Hardware SensorManager
- **Video Playback**: `com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0`
- **Background Tasks**: AndroidX WorkManager 2.10.0 + Android AlarmManager

---

## Project Structure

```
app/src/main/java/com/leanbulk/tracker/
├── data/
│   ├── backup/       # Full JSON export/import & Storage Access Framework
│   ├── health/       # Hardware step sensors & Health Connect client
│   ├── local/        # Room Database, DAOs, and Entities
│   ├── model/        # Domain models, calculations (McCallum, MSJ TDEE)
│   ├── notification/ # AlarmManager receivers & BootReceiver
│   └── repository/   # Repository layer for meals, profile, and weights
├── ui/
│   ├── components/   # Compose UI cards, charts, and YouTube player
│   ├── meals/        # Nutrition dashboard, checklist, and top-up cards
│   ├── navigation/   # Bottom navigation bar and routing
│   ├── progress/     # Forecast table, trajectory charts, and backup card
│   ├── reminders/    # Notification schedules
│   ├── theme/        # Material 3 colors, typography, and styling
│   └── workout/      # 7-day video workout player and segment playlists
```

---

## Building & Installation

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK Build-Tools 35
- JDK 17+
- Android Device running Android 8.0 (API 26) or higher

### Build from Source
```bash
git clone https://github.com/KrisKisho/LeanBulkTracker.git
cd LeanBulkTracker
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## License

This project is licensed under the [MIT License](LICENSE).
