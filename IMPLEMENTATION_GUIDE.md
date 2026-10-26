# Lean Bulk Tracker — Complete Technical Implementation Guide

> **Target Profile:** 60.0 kg $\rightarrow$ 70.0 kg Lean Hypertrophy  
> **Framework:** Android Jetpack Compose (Material 3), Room Database v10, Media3 ExoPlayer, Health Connect & Hardware Step Sensors  
> **Architecture:** Offline-First Clean MVVM + Repository Pattern with Kotlin Coroutines & StateFlow

---

## 1. System Overview & Architectural Principles

The **Lean Bulk Tracker** (`com.leanbulk.tracker`) is an offline-first, closed-loop, self-adapting hypertrophy and nutrition engineering system. Unlike static workout trackers or calorie counters, it operates on a **continuous physiological feedback loop**:

```
           ┌──────────────────────────────────────────────┐
           │          User Logs Real-World Data           │
           │ (Weigh-ins, Meals Checked, Workouts Done)    │
           └──────────────────────┬───────────────────────┘
                                  ▼
           ┌──────────────────────────────────────────────┐
           │            Intelligent Engines               │
           │ • AdaptiveCoachEngine (EWMA & Calorie Shift) │
           │ • ProgressiveOverloadEngine (4-Wk Mesocycle) │
           │ • DynamicNutritionEngine (MSJ TDEE + Steps)  │
           │ • EquipmentLoadTranslator (Gym Load Math)    │
           │ • MacroGapBalancer (1-Tap South Indian Patch)│
           └──────────────────────┬───────────────────────┘
                                  ▼
           ┌──────────────────────────────────────────────┐
           │          Autonomous System Mutation          │
           │ • Calorie Targets Scale (±150 kcal steps)    │
           │ • Missed Workouts Shift Automatically        │
           │ • Plateau Overload Unlocks Next Dumbbell     │
           │ • Phosphocreatine Muscle Saturation Tracks   │
           └──────────────────────────────────────────────┘
```

### Core Architecture Pillars
- **Zero-Cloud Autonomy:** All biometric history, workout sets, and food databases exist locally in encrypted/structured SQLite via Room. No login, no remote telemetry, no subscription servers.
- **Unidirectional Data Flow (UDF):** Every UI screen observes an immutable `UiState` via `StateFlow` produced by combining Room DAO flows and hardware sensors.
- **Graceful Hardware Degradation:** Automatically detects and prioritizes Qualcomm hardware step sensors (`TYPE_STEP_COUNTER` / `TYPE_STEP_DETECTOR`), seamlessly falls back to Google Health Connect, and defaults to estimated TDEE if permissions are denied.
- **Hybrid Media Delivery:** Video segments prioritize offline local MP4 files via AndroidX Media3 ExoPlayer for zero-latency, frame-accurate seeking; falls back seamlessly to the official YouTube IFrame player if offline files are not found.

---

## 2. Database Schema & Room Persistence Architecture

The app uses **Room Database Version 10** with structured sequential migrations (`MIGRATION_3_4` through `MIGRATION_9_10`) and destructive fallback safety.

```
AppDatabase (v10)
├── user_profile            # Biometrics, adaptive coach offsets, chronotype, equipment
├── weight_progress         # Date-stamped weigh-ins with waist measurements & notes
├── daily_meal_logs         # Daily meal checklist logs, portion multipliers, custom macros
├── daily_workout_logs      # Segment completion, weight used, set 3 achieved reps, RPE
├── reminders               # AlarmManager scheduled meal & workout notification times
├── daily_habit_logs        # Water intake glasses, sleep hours, tissue recovery markers
├── meal_templates          # Saved meal templates
├── adaptation_logs         # Audit trail of autonomous coaching decisions
├── custom_foods            # User-created South Indian foods & macro entries
├── saved_meal_combos       # Multi-item customized South Indian meal plates
└── supplement_logs         # Daily stack checklist (Whey, D3, Fish Oil, Magnesium)
```

### 2.1 Table Definitions & Room Entities

#### `user_profile` (`UserProfileEntity.kt`)
| Column | Type | Default | Purpose |
|---|---|---|---|
| `id` | `INTEGER` (PK) | `1` | Singleton user profile record |
| `currentWeightKg` | `REAL` | `60.0f` | Current scale weight |
| `startWeightKg` | `REAL` | `60.0f` | Starting benchmark weight |
| `goalWeightKg` | `REAL` | `70.0f` | Target lean bulk goal |
| `targetWeeklyGainKg`| `REAL` | `0.30f` | Target rate of gain (+0.25 to +0.35 kg/wk) |
| `wristInches` | `REAL` | `6.1f` | McCallum wrist girth benchmark |
| `currentWaistInches`| `REAL` | `30.0f` | Abdominal visceral fat monitoring |
| `autoAdaptEnabled` | `INTEGER` | `1` | Master toggle for autonomous coaching |
| `adaptiveCalorieOffset` | `INTEGER` | `0` | Auto-adjusted offset (steps of ±150 kcal; -300 to +600) |
| `splitShakeModeActive` | `INTEGER` | `0` | 1 if evening dinner adherence requires split shake |
| `dailyStepGoal` | `INTEGER` | `6000` | Dynamic NEAT step target (6,000 to 9,500) |
| `workoutStartTimeHour` | `INTEGER` | `19` | Learned preferred workout start hour |
| `workoutStartTimeMinute` | `INTEGER` | `30` | Learned preferred workout start minute |
| `currentRollingSplitIndex` | `INTEGER` | `0` | Rolling workout rotation index |
| `lastAdaptationEvalDate` | `TEXT` | `""` | 7-day rate-limiting debounce timestamp |
| `wheyCreatineMode` | `TEXT` | `'WHEY_WITH_CREATINE'` | Mode switcher (`WHEY_WITH_CREATINE` vs `WHEY_ONLY_NO_CREATINE`) |
| `creatineSaturationPercent` | `REAL` | `0.0f` | Phosphocreatine muscle saturation gauge (0–100%) |
| `d3Supplement1Iu` | `INTEGER` | `600` | Vitamin D3 label dose in D3+B12+K2 |
| `d3Supplement2Iu` | `INTEGER` | `400` | Vitamin D3 label dose in Fish Oil |
| `eqHas4kgDumbbell` | `INTEGER` | `1` | Home gym inventory flag: 4 kg dumbbell |
| `eqHasWeightedBackpack` | `INTEGER` | `1` | Home gym inventory flag: water bottle backpack |
| `eqHasSturdyTableOrBed` | `INTEGER` | `1` | Home gym inventory flag: inverted rows / pushups |
| `eqHasAdjustableDumbbell` | `INTEGER` | `0` | Home gym inventory flag: spinlock dumbbells |
| `eqHasBarbell` | `INTEGER` | `0` | Home gym inventory flag: barbell with weight plates |
| `eqHasPullUpBar` | `INTEGER` | `0` | Home gym inventory flag: doorway pull-up bar |
| `eqHasResistanceBand` | `INTEGER` | `0` | Home gym inventory flag: resistance loop/tubes |

#### `supplement_logs` (`SupplementLogEntity.kt`)
| Column | Type | Purpose |
|---|---|---|
| `date` | `TEXT` (PK) | YYYY-MM-DD log date |
| `wheyLogged` | `INTEGER` | Whether Osoaa Impact Whey was consumed |
| `creatineGramsLogged` | `REAL` | 3.0g if creatine mode active; 0.0g if plain whey |
| `vitD3B12K2Logged` | `INTEGER` | Whether D3+B12+K2 capsule was logged |
| `fishOilLogged` | `INTEGER` | Whether Carbamide Forte Fish Oil was logged |
| `magnesiumGlycinateLogged` | `INTEGER` | Whether Magnesium Glycinate was logged |
| `timestampMillis` | `INTEGER` | Unix timestamp of completion |

#### `custom_foods` (`CustomFoodEntity.kt`)
Stores custom ingredients created by the user with Telugu hints, serving units, and macro breakdowns.

#### `saved_meal_combos` (`SavedMealComboEntity.kt`)
Stores user-designed multi-item South Indian meal plates (e.g., 4 Idlis + 150g Sambar + 2 Boiled Eggs) serialized as `itemsJson`.

#### `adaptation_logs` (`AdaptationLogEntity.kt`)
Maintains an immutable audit log of autonomous coaching decisions categorized by `NUTRITION`, `WORKOUT_SCHEDULE`, `OVERLOAD`, `CHRONOTYPE`, and `WAIST_GUARDRAIL`.

---

## 3. Intelligent Engine Architectures

### 3.1 Closed-Loop Coaching Engine (`AdaptiveCoachEngine.kt`)
Operates 5 closed loops evaluated on app launch, after weight logs, after meal checkoffs, and after workout completions:

1. **Loop A — 7-Day Exponentially Weighted Moving Average (EWMA):**
   $$\text{EWMA}_t = (\alpha \times \text{Weight}_t) + ((1 - \alpha) \times \text{EWMA}_{t-1}) \quad (\alpha = 0.25)$$
   - Smooths out short-term South Indian carb/sodium water retention spikes.
   - Computes $\text{TrueWeeklyGainRate} = \text{EWMA}_{\text{today}} - \text{EWMA}_{\text{7 days ago}}$.
   - **Hard Plateau** ($< +0.10\text{ kg/wk}$ despite $\ge 80\%$ meal adherence): Automatically increases `adaptiveCalorieOffset` by $+150\text{ kcal}$ and expands Lunch rice from 1.5 to 2.0 cups.
   - **Excess Fat Gain** ($> +0.45\text{ kg/wk}$): Automatically reduces `adaptiveCalorieOffset` by $-150\text{ kcal}$.

2. **Loop B — Chronotype Completion Timing:**
   - Tracks the moving average hour/minute of workout completions.
   - Re-schedules pre-workout snack notifications to exactly 45 minutes prior to learned training time.

3. **Loop C — Missed Workout Rolling Shift & 48h Fatigue Lock:**
   - Detects incomplete workouts from yesterday and shifts them forward to today.
   - Enforces a 48-hour recovery lock between identical muscle groups to prevent overtraining.

4. **Loop D — Split-Shake Mode:**
   - Automatically activates if evening dinner adherence drops below 75% for workouts completed after 7:30 PM.
   - Reconfigures the post-workout nutrition window into an 8:05 PM fast-absorbing Whey shake paired with a lighter dinner.

5. **Loop E — Waist Guardrail & NEAT Step Scaling:**
   - Evaluates Waist-to-Height Ratio ($\text{WHtR} = \text{Waist (cm)} / \text{Height (cm)}$).
   - If $\text{WHtR} > 0.48$ or waist expands by $> 0.25''$ over 2 consecutive weeks without target weight progress, scales daily NEAT step goals by $+1,000\text{ steps}$ up to $9,500\text{ steps}$.

---

### 3.2 4-Week Double Progression & Overload Engine (`ProgressiveOverloadEngine.kt`)
Implements strict time-under-tension (TUT) hypertrophy progression inside fixed 40-second work intervals:

| Mesocycle Phase | Tempo Code | Cadence Breakdown | Rep Target per 40s Set | Focus |
|---|---|---|---|---|
| **Week 1** | `3-1-1` | 3s lower, 1s pause, 1s lift (~5s/rep) | 8–10 reps | Neuromuscular Groove |
| **Week 2** | `2-1-1` | 2s lower, 1s peak squeeze, 1s lift (~4s/rep) | 10–12 reps | Rep Density Expansion |
| **Week 3** | `2-0-1` | 2s lower, 0s rest, 1s lift (~3s/rep) | 12–15 reps | Continuous Tension Overload |
| **Week 4** | `Test + Pulses` | 30s controlled 2-0-1 reps + final 10s halfway pulses | 16+ reps | Graduation Test Week |

#### Movement Category Dumbbell Weight Upgrade Ladder
- **Small Isolation** (Chest Fly, Overhead Tricep Ext, Kickback): `4.0 kg -> 5.5 kg -> 7.5 kg -> 9.5 kg` (+1.5 to 2.0 kg)
- **Medium Upper** (Bicep Curls, Hammer Curls, Single-Arm Shoulder Press): `4.0 kg -> 6.0 kg -> 8.5 kg -> 11.5 kg` (+2.0 to 2.5 kg)
- **Large Upper** (Single-Arm Floor Press, Row, Pullover, Crush Press): `4.0 kg -> 7.5 kg -> 10.5 kg -> 14.0 kg` (+3.0 to 3.5 kg)
- **Lower Body** (Goblet Squat, Romanian Deadlift, Split Lunge): `4.0 kg -> 8.0 kg -> 12.5 kg -> 17.5 kg` (+4.0 to 5.0 kg)

#### 3-Signal Weight Upgrade Unlock Criteria
1. User is on **Week 4 (Graduation Test Week)**.
2. Achieved Set 3 reps $\ge \text{Week 4 Trigger Threshold}$.
3. Effort rating is `"Good Burn"` or `"Easy"` (not `"Near Failure"`).

---

### 3.3 Audio/Visual Tempo Metronome Engine (`TempoMetronomeEngine.kt`)
Generates synthesized audio cues using native `android.media.ToneGenerator`:
- **Lowering (Eccentric):** `ToneGenerator.TONE_PROP_BEEP` (60ms) on each second.
- **Hold (Isometric):** `ToneGenerator.TONE_PROP_ACK` (80ms).
- **Lift & Squeeze (Concentric):** `ToneGenerator.TONE_PROP_BEEP2` (100ms).
- **Set Completion:** `ToneGenerator.TONE_PROP_PROMPT` (350ms fanfare).
- **Animated Tension Bar (`TempoMetronomeBar.kt`):** Uses Compose `animateFloatAsState` to contract bar width from $100\% \rightarrow 20\%$ during lower, hold at $20\%$, and expand $20\% \rightarrow 100\%$ during lift.

---

### 3.4 Creatine Saturation & Supplement Tracker Engine
- **Phosphocreatine Muscle Saturation:**
  - Full muscle saturation requires $\approx 100\text{g}$ cumulative creatine ($\approx 25\text{ days}$ at $3\text{g/day}$).
  - Each logged day of $\ge 3\text{g}$ creatine increments saturation by $+4.0\%$.
  - Missed days or switching to Plain Whey decays saturation by $-2.5\%/\text{day}$.
- **Vitamin D3 Label Guardrail:**
  - Combines D3 IU from both `Osoaa D3+B12+K2` (default 600 IU) and `Carbamide Forte Salmon Fish Oil` (default 400 IU).
  - Triggers an amber alert banner if combined daily dose exceeds $4,000\text{ IU/day}$.

---

### 3.5 Home Gym Inventory & Load Swapper Engine (`EquipmentLoadTranslator.kt`)
Translates target dumbbell weights into actionable home equipment setups:
- **Only 4 kg Dumbbell + Weighted Backpack:**
  $$\text{ExtraKgNeeded} = \text{TargetWeightKg} - 4.0\text{ kg} \quad (1\text{L water bottle} = 1.0\text{ kg})$$
  - *Lower Body:* Hold 4 kg dumbbell + wear backpack with $N \times 1\text{L}$ water bottles.
  - *Large Upper:* Hold 4 kg dumbbell + grip backpack handle or use 4-2-1 ultra-slow tempo.
  - *Medium/Small Upper:* Curl/press loaded backpack by top handle or 4 kg dumbbell with 3s peak squeeze.
- **Adjustable Dumbbells Owned:**
  $$\text{PlatePerSide} = \frac{\text{TargetWeightKg} - 1.5\text{ kg rod}}{2}$$
- **Barbell Owned:** Unlocks 1-tap swap to barbell variations (Squat, RDL, Floor Press, Bent-Over Row, Curl) with $2.0\times$ bilateral load adjustments.

---

### 3.6 South Indian Food Database & Smart Macro Gap Balancer
Pre-populated catalog of **77 authentic staples** in [`SouthIndianFoodCatalog.kt`](file:///c:/Users/degas/OneDrive/Documents/Workout%20App/app/src/main/java/com/leanbulk/tracker/data/model/SouthIndianFoodCatalog.kt) across 8 categories:
1. `TIFFINS_BREADS` (Idli, Ragi Idli, Kal Dosa, Double Egg Dosa, Pesarattu, MLA Pesarattu, Upma, Pongal, Uggani, Puri, Chapati, Medu Vada, Ragi Malt)
2. `RICE_GRAINS` (Sona Masoori Rice, Curd Rice, Andhra Chicken Dum Biryani, Tomato Rice, Lemon Rice, Tamarind Pulihora, Sambar Rice, Bagara Rice, Khichdi)
3. `DAL_LENTILS` (Andhra Tomato Pappu, Dosakaya Pappu, Thick South Indian Sambar, Palakura Pappu, Kandi Pappu with Ghee, Rasam, Dal Tadka, Chana Masala, Rajma)
4. `MEAT_EGGS_SEAFOOD` (Andhra Chicken Curry, Country Chicken Pulusu, Chicken Fry, Chicken Liver, Boiled Egg, Egg White, Omelette, Chepala Pulusu, Mutton Sukka, Prawns)
5. `DAIRY_VEG_PROTEIN` (Full Cream Curd, Paneer Curry, Buffalo/Cow Milk, Low-fat Paneer, Soya Chunks Curry, Whey Isolate)
6. `VEGETABLES_FRIES` (Bendakaya Vepudu, Dondakaya Fry, Aloo Vepudu, Beetroot Poriyal, Cabbage Poriyal, Palak Sabzi)
7. `CHUTNEYS_FATS` (Coconut Chutney, Peanut Chutney, Pappula Chutney, Tomato Chutney, Gongura Pachadi, Pure Desi Cow Ghee)
8. `SNACKS_HEALTHY_FATS` (Roasted Peanuts, Roasted Chana, Makhana, Boiled Peanuts, Almonds, Banana)

#### Smart Macro-Gap Auto-Balancer (`MacroGapBalancer.kt`)
- If a customized meal plate has a protein deficit $\ge 10\text{g}$, automatically renders 1-tap quick-add chips:
  - `+2 Boiled Eggs (+144 kcal | +12.6g P)`
  - `+3 Egg Whites (+51 kcal | +10.8g P)`
  - `+50g Paneer (+130 kcal | +9.0g P)`
  - `+25g Soya Chunks (+86 kcal | +13.0g P)`
- If calories are under target by $\ge 100\text{ kcal}$, renders calorie patch chips (`+0.5 cup Steamed Rice`, `+1 tbsp Desi Ghee`, `+30g Roasted Peanuts`).

---

## 4. UI Layer Architecture & Jetpack Compose Screens

```
com.leanbulk.tracker.ui/
├── components/
│   ├── YouTubePlayerContainer.kt       # Hybrid ExoPlayer / YouTube Player + Fullscreen Dialog
│   ├── LiveHealthSyncCard.kt           # Step sensor & Health Connect real-time bar
│   ├── NutritionSummaryCard.kt         # Calorie & macro progress rings
│   ├── AdaptiveCoachStatusCard.kt      # Autonomous coaching state & decision logs
│   ├── RecoveryHydrationCard.kt        # Water & sleep duration tracker
│   ├── WeightTrajectoryChart.kt        # Dual canvas line charts (weight + waist)
│   ├── BodyDimensionForecastCard.kt    # McCallum frame proportions & visceral fat ceiling
│   ├── OfflineVideosDialog.kt          # Offline video directory picker & file transfer
│   └── DataBackupCard.kt               # JSON export & restore controls
├── workout/
│   ├── WorkoutScreen.kt                # 7-Day rotation, active player, playlist checklist
│   ├── WorkoutViewModel.kt             # Workout state, overload logic, equipment flow
│   ├── TempoMetronomeBar.kt            # Visual tension bar & metronome controls
│   ├── TempoMetronomeEngine.kt         # ToneGenerator audio synthesis
│   └── HomeGymInventoryDialog.kt       # Equipment checkboxes dialog
├── meals/
│   ├── MealsScreen.kt                  # Daily meals checklist & supplement tracker
│   ├── MealsViewModel.kt               # Meals state, dynamic nutrition target, custom foods
│   ├── MealPlateBuilderSheet.kt        # Interactive South Indian plate builder modal
│   └── SupplementTrackerCard.kt        # Supplement stack & creatine saturation card
└── progress/
    ├── ProgressScreen.kt               # Biometrics, trajectory charts, forecast
    └── ProgressViewModel.kt            # Progress state, weigh-in logging
```

---

## 5. Offline Media & Video Architecture

### Hybrid Video Playback Pipeline (`LocalVideoManager.kt` & `YouTubePlayerContainer.kt`)
The app supports dual-mode zero-latency playback:

1. **Local Media3 ExoPlayer (Preferred):**
   - Scans `/sdcard/Movies/LeanBulk/` and `context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)` for matching video files (e.g., `chest_workout.mp4`, `arms_workout.mp4`, or video ID `l5yuivJYj5k.mp4`).
   - Plays offline with zero buffering, frame-accurate seeking, and no cellular data consumption.
2. **YouTube Player Fallback:**
   - If local file is missing, loads `com.pierfrancescosoffritti.androidyoutubeplayer` in non-autoplay mode.
3. **In-App Video File Transfer Dialog (`OfflineVideosDialog.kt`):**
   - Features a one-tap SAF storage file picker allowing users to copy workout MP4s directly into app storage.
4. **Fullscreen Video Mode:**
   - Fullscreen dialog overlay with semi-transparent controls: Top bar with segment title, time range, loop toggle, and exit button; bottom bar with `-5s` rewind, Play/Pause, and Restart buttons.

---

## 6. Data Backup & Migration Pipeline (`DataBackupManager.kt`)

### Portable JSON Schema (Version 1)
Exports all app data into a portable `.json` file:
```json
{
  "app": "LeanBulkTracker",
  "version": 1,
  "exportedAt": 1729837200000,
  "exportedDate": "2026-10-25T12:00:00",
  "userProfile": {
    "currentWeightKg": 60.0,
    "wheyCreatineMode": "WHEY_WITH_CREATINE",
    "creatineSaturationPercent": 4.0,
    "eqHasBarbell": true
  },
  "weightHistory": [...],
  "dailyMealLogs": [...],
  "workoutLogs": [...],
  "reminders": [...],
  "dailyHabitLogs": [...],
  "adaptationLogs": [...],
  "customFoods": [...],
  "savedMealCombos": [...],
  "supplementLogs": [...]
}
```
- **SAF & System Share Sheet:** Direct file save or share via Google Drive, WhatsApp, Gmail, or Bluetooth.
- **Transactional Restore:** Atomically upserts all records into Room and reschedules local AlarmManager notifications.

---


### 6. Waist Recomposition & Core Tightening Finisher Module (`BellyFatCircuitCatalog.kt`)

The waist recomposition system enables aggressive core tightening without stalling lean-bulk muscle hypertrophy:

1. **Dual Circuit Architecture (`digpucxFbMo`):**
   - **Option A (Full 8 Moves, ~9.5 mins active | ~95 kcal burn):** Jumping Jacks (45s), Mountain Climbers (45s), Isometric Elbow Plank (45s), Seated Flutter Kicks (45s), High Knees (45s), Russian Twists (45s), Seated Leg Tuck-Ins (45s), Bicycle Crunches (45s).
   - **Option B (Pure Abs Only, ~6 mins | Leg-Day Safe):** Skips jumping jacks and high knees to protect knee joints and lower-body recovery on Tuesday & Thursday leg sessions.
   - **Smart Auto Variant:** Automatically toggles between Full 8 Moves (Mon, Wed, Fri, Sat, Sun) and Pure Abs Only (Tue, Thu).

2. **Mid-Video Ad/Promo Seamless Skip:**
   - The circuit automatically jumps from Segment 7 (ends at 564s) directly into Segment 8 (starts at 602s), cleanly bypassing the 37-second sponsor promo between 565s and 601s without player latency.

3. **Stomach Vacuum Coaching Cues:**
   - Highlights transverse abdominis activation on Isometric Elbow Plank (`bf_seg_3`) and Seated Leg Tuck-Ins (`bf_seg_7`):
     > *"Stomach Vacuum Cue: Exhale completely and pull your navel in toward your spine during the entire 45 seconds to tighten your inner abdominal wall."*

4. **Recomposition Nutrition Rules:**
   - **Surplus Tightening:** Daily surplus shifts from standard $+330\text{ kcal}$ to $+220\text{ kcal}$ ($-110\text{ kcal}$ from dinner carbs: 1.5 cups Rice $\rightarrow$ 1.0 cup Rice).
   - **Higher Protein Floor:** Scales daily protein from $2.0\text{ g/kg}$ to $2.15\text{ g/kg}$ (~130–140g/day) to preserve lean mass during core oxidation.
   - **Uncompensated Energy Buffer:** The ~95 kcal burned during the finisher is *not* added back to the dynamic calorie target, establishing an uncompensated fat-oxidation buffer.

5. **Autonomous Closed-Loop Trigger:**
   - `AdaptiveCoachEngine.kt` auto-activates `bellyFatRecompMode = true` whenever the 14-day navel waist measurement increases by $\ge 0.25''$ or $\text{WHtR} > 0.46$.

---

## 7. Developer Build & Deployment Guide

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK Command-line Tools
- JDK 17 or higher
- Android Device running Android 8.0 (API level 26) or higher

### Building the Debug APK
```bash
# Clean and assemble debug APK
.\gradlew assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk`

### Wireless Debugging Installation via ADB
```powershell
# 1. Connect over Wi-Fi debugging (replace with device IP:Port)
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" connect 192.168.1.108:42339

# 2. Stream install APK onto device
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.108:42339 install -r app\build\outputs\apk\debug\app-debug.apk

# 3. Launch MainActivity
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s 192.168.1.108:42339 shell am start -n com.leanbulk.tracker/.MainActivity
```

---

## 8. Verification & Quality Assurance Matrix

| Feature | Component | Verification Mechanism | Status |
|---|---|---|---|
| **Tempo Metronome** | `TempoMetronomeEngine.kt` | Native `ToneGenerator` audio beeps & Compose `animateFloatAsState` tension bar | Verified on Device |
| **Creatine Saturation** | `SupplementTrackerCard.kt` | $+4\%/\text{day}$ gauge scaling, plain whey mode switch, washout decay | Verified on Device |
| **Vitamin D3 Overlap** | `SupplementTrackerCard.kt` | Combined IU math and $>4,000\text{ IU}$ warning banner | Verified on Device |
| **Home Gym Inventory** | `HomeGymInventoryDialog.kt` | Room v9 persistence, interactive dialog on Workout & Progress screens | Verified on Device |
| **Equipment Load Math** | `EquipmentLoadTranslator.kt` | Water bottle backpack math ($1\text{L} = 1.0\text{ kg}$) and 1-tap Barbell swap | Verified on Device |
| **Plate Builder Sheet** | `MealPlateBuilderSheet.kt` | 77 South Indian foods, portion steppers, macro-gap auto-balancer | Verified on Device |
| **Media3 ExoPlayer** | `YouTubePlayerContainer.kt` | Offline video playback with fallback to YouTube, Fullscreen dialog | Verified on Device |
| **Database Migration** | `AppDatabase.kt` | `MIGRATION_9_10` SQLite execution without data loss | Verified on Device |
| **JSON Backup/Restore**| `DataBackupManager.kt` | Export & restore of all 11 tables including supplements, equipment & recomp mode | Verified on Device |
| **Waist Recomp Finisher**| `BellyFatCircuitCatalog.kt` | Video ID `digpucxFbMo`, promo skip (564s->602s), stomach vacuum cue banner | Verified on Device |
| **Recomp Macros** | `DynamicNutritionEngine.kt` | Surplus shift to +220 kcal, 2.15g/kg protein, dinner rice reduction (-110 kcal) | Verified on Device |
| **Database Migration v10**| `AppDatabase.kt` | `MIGRATION_9_10` adding `bellyFatRecompMode` and `bellyWorkoutVariant` | Verified on Device |
