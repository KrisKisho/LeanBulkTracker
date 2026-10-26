# Lean Bulk Tracker (60 to 70 kg)

An offline-first Android hypertrophy and nutrition tracking application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database v10**, **Media3 ExoPlayer**, **Android Health Connect**, and Qualcomm **Hardware Step Sensors**.

Designed specifically for lean-bulk progression ($60.0\text{ kg} \rightarrow 70.0\text{ kg}$) utilizing progressive overload with minimal equipment (single 4 kg dumbbell, weighted backpack, adjustable dumbbells, or barbell) paired with a high-protein South Indian nutritional framework and closed-loop autonomous coaching.

> 📘 **For in-depth architecture, database schema, math models, and engine mechanics, see the [Technical Implementation Guide](IMPLEMENTATION_GUIDE.md).**

---

## Key Features

### 1. Audio/Visual Tempo Metronome (`TempoMetronomeBar.kt` & `TempoMetronomeEngine.kt`)
- **Native Tone Synthesis:** Uses `android.media.ToneGenerator` for zero-asset audio cues (Lower tick, Hold double-tick, Lift chime, and Completion fanfare).
- **Mesocycle Cadence Parsing:** Week 1 (`3-1-1`), Week 2 (`2-1-1`), Week 3 (`2-0-1`), and Week 4 (`30s controlled + final 10s burnout pulses`).
- **Animated Tension Bar:** Dynamically contracts ($100\% \rightarrow 20\%$) in Electric Cyan during eccentric lowering, pulses in Amber Gold on hold, and rapidly expands ($20\% \rightarrow 100\%$) in Neon Lime during concentric lift.
- **Auto Set 3 Logging:** Live rep counting that automatically pre-fills Set 3 reps upon completion.

### 2. Creatine Saturation & Daily Supplement Stack Tracker (`SupplementTrackerCard.kt`)
- **Whey Mode Switcher:** 1-tap toggle between `Osoaa Whey + 3g Creatine` (+24g P, +3g Creatine) and `Plain Whey Only` (+24g P, 0g Creatine).
- **Phosphocreatine Muscle Saturation Gauge ($0\% \rightarrow 100\%$):** Tracks cumulative daily creatine intake (+4.0%/day) and natural washout decay (-2.5%/day).
- **4-Supplement Label Checklist:** Osoaa Impact Whey, Osoaa Vitamin D3+B12+K2, Carbamide Forte Salmon Fish Oil, and Osoaa Chelated Magnesium Glycinate.
- **Vitamin D3 Overlap Guardrail:** Computes combined D3 IU across supplements and displays an amber warning banner if intake exceeds $4,000\text{ IU/day}$.

### 3. "My Home Gym Inventory" & Smart Load / Exercise Swapper (`EquipmentLoadTranslator.kt`)
- **Equipment Inventory Manager:** Interactive modal on Workout and Progress screens managing 4 kg dumbbell, weighted backpack, sturdy table/bed, adjustable dumbbells, barbell, pull-up bar, and resistance bands.
- **Backpack Loading Math ($1\text{L} = 1.0\text{ kg}$):** Automatically calculates extra kg and water bottle count for squats and presses when only a 4 kg dumbbell is owned.
- **Adjustable Dumbbell Math:** Calculates plate weight per side assuming a 1.5 kg spinlock rod.
- **1-Tap Barbell Swaps:** Swaps compound exercises (Goblet Squat $\rightarrow$ Barbell Squat, Single-Arm Floor Press $\rightarrow$ Barbell Floor Press, Single-Arm Row $\rightarrow$ Barbell Bent-Over Row) with $2.0\times$ bilateral load adjustments.

### 4. 4-Week Double Progression & Overload Engine (`ProgressiveOverloadEngine.kt`)
- **4-Week Mesocycle Cadence:** Neuromuscular Groove (`3-1-1`), Rep Density Expansion (`2-1-1`), Continuous Tension Overload (`2-0-1`), and Graduation Test Week.
- **Movement Category Dumbbell Ladders:** Prescribes structured weight jumps (+1.5 kg to +5.0 kg) across Small Isolation, Medium Upper, Large Upper, and Lower Body.
- **3-Signal Weight Upgrade Unlock:** Detects when a user graduates Week 4 Set 3 targets with reserve effort.

### 5. Closed-Loop Autonomous Coaching Engine (`AdaptiveCoachEngine.kt`)
- **7-Day EWMA Weight Tracking:** Filters out South Indian carb/sodium water spikes using $\alpha = 0.25$.
- **Autonomous Calorie & Portion Scaling:** Adjusts daily calorie targets in steps of $\pm 150\text{ kcal}$ based on true weekly gain rate vs. $+0.30\text{ kg/wk}$ target.
- **Missed Workout Rolling Shift:** Automatically shifts missed workouts forward to maintain weekly volume while enforcing a 48h fatigue recovery lock.
- **Chronotype Learning:** Adapts pre-workout nutrition reminder alerts to the user's learned completion times.
- **Split-Shake Mode:** Activates an 8:05 PM Whey shake + lighter dinner if post-workout meal adherence drops.
- **Waist Guardrail & NEAT Step Scaling:** Monitors Waist-to-Height Ratio ($\text{WHtR} > 0.48$) and scales daily step goals from 6,000 to 9,500 steps.

### 6. South Indian Food Database & Custom Meal Plate Builder (`MealPlateBuilderSheet.kt`)
- **77 Pre-Populated Staples:** Authentic dishes across 8 categories with regional Telugu hints and exact macros.
- **Interactive Plate Builder Sheet:** Compose custom plates with 0.5 portion steppers and live Target vs. Plate macro comparison bars.
- **Smart Macro-Gap Balancer:** 1-tap South Indian protein patches ($\ge 10\text{g}$ gap) and calorie patches ($\ge 100\text{ kcal}$ gap).

### 7. Hybrid Offline Video Player & Fullscreen Mode (`YouTubePlayerContainer.kt`)
- **Local Media3 ExoPlayer:** Automatically detects and plays offline MP4s from `/sdcard/Movies/LeanBulk/` or internal app storage.
- **YouTube Player Fallback:** Non-autoplay embedded player when local video files are absent.
- **In-App Video Picker Dialog:** One-tap transfer of workout video files into local app storage.
- **Fullscreen Overlay Controls:** Top bar with segment title and continuous play toggle; bottom bar with $-5\text{s}$ rewind, Play/Pause, and Restart buttons.


### 8. Waist Recomposition & Core Tightener Finisher (`BellyFatCircuitCatalog.kt`)
- **8-Move Metabolic & Core Circuit (`digpucxFbMo`):** High-intensity circuit (~9.5 mins active, ~95 kcal burn) targeting core endurance and visceral adiposity.
- **Smart Leg-Day Variant Switcher:** Automatically runs Pure Abs Mode (~6 mins, 6 moves) on Tuesday & Thursday to skip high-impact jumps on lower-body days.
- **Promo Skip & Stomach Vacuum Cues:** Seamlessly bypasses the 37s mid-video sponsor ad (565s–601s) and highlights transverse abdominis vacuum cues on Plank (`bf_seg_3`) and Leg Tuck-Ins (`bf_seg_7`).
- **Autonomous Activation:** Activates whenever 14-day navel waist expands $\ge 0.25''$ or $	ext{WHtR} > 0.46$. Tightens surplus to $+220	ext{ kcal}$ and elevates protein to $2.15	ext{ g/kg}$.

### 9. Full Offline Data Export & Migration (`DataBackupManager.kt`)
- **Zero Cloud Dependence:** Export all 11 Room tables into a single portable `.json` file.
- **System Share Sheet & SAF:** Direct local file save or share via Google Drive, WhatsApp, Gmail, or Bluetooth.
- **Atomic Restore:** 1-tap transactional import with Room `@Upsert` and AlarmManager notification re-scheduling.

---

## Tech Stack & Architecture

- **Language:** Kotlin 2.1.0
- **UI Toolkit:** Jetpack Compose (Material 3) with Dark Neon Cyan & Gold theme
- **Architecture:** Clean MVVM + Repository Pattern + Kotlin StateFlow / Coroutines
- **Local Persistence:** Room Database v10 (11 entities with sequential migrations)
- **Audio Synthesis:** `android.media.ToneGenerator`
- **Video Playback:** AndroidX Media3 ExoPlayer 1.5.0 + `android-youtube-player:core:13.0.0`
- **Health & Fitness:** `androidx.health.connect:connect-client:1.1.0-alpha11` + Qualcomm Hardware Step Sensors
- **Background Tasks:** AndroidX WorkManager 2.10.0 + Android AlarmManager

---

## Project Structure

```
app/src/main/java/com/leanbulk/tracker/
├── data/
│   ├── backup/       # Full JSON export/import & Storage Access Framework
│   ├── health/       # Hardware step sensors & Health Connect client
│   ├── local/        # Room Database v10, DAOs, and 11 Entities
│   ├── model/        # AdaptiveCoachEngine, OverloadEngine, EquipmentLoadTranslator, FoodCatalog
│   ├── notification/ # AlarmManager receivers & BootReceiver
│   ├── repository/   # Repositories for meals, workouts, profile, habits, and weights
│   └── video/        # Local offline video manager & path resolver
├── ui/
│   ├── components/   # Compose UI cards, YouTube/ExoPlayer container, charts
│   ├── meals/        # Meals checklist, Plate builder sheet, Supplement tracker card
│   ├── navigation/   # Bottom navigation bar and routing
│   ├── progress/     # Forecast table, trajectory charts, equipment button, backup card
│   ├── reminders/    # Notification schedules
│   ├── theme/        # Material 3 colors, typography, and styling
│   └── workout/      # 7-day video workout player, Tempo metronome, Equipment dialog
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
Output path: `app/build/outputs/apk/debug/app-debug.apk`

### Install via ADB / Wi-Fi Debugging
```powershell
# Connect over Wi-Fi
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" connect 192.168.1.108:42339

# Stream install APK
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.108:42339 install -r app\build\outputs\apk\debug\app-debug.apk

# Launch app
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.108:42339 shell am start -n com.leanbulk.tracker/.MainActivity
```

---

## License

This project is licensed under the [MIT License](LICENSE).
