package com.leanbulk.tracker.data.model

import android.content.Context
import android.util.Log
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity
import com.leanbulk.tracker.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class AdaptiveStatusInfo(
    val autoAdaptEnabled: Boolean,
    val ewmaGainRateKgPerWeek: Float,
    val targetWeeklyGainKg: Float,
    val adaptiveCalorieOffset: Int,
    val effectiveBaseCalories: Int,
    val workoutQueueStatus: String,
    val isShifted: Boolean,
    val eveningStrategy: String,
    val isSplitShakeActive: Boolean,
    val whtr: Float,
    val lastEvalDate: String
)

data class AdaptationResult(
    val evaluated: Boolean,
    val changesApplied: List<String> = emptyList(),
    val ewmaGainRate: Float = 0f,
    val adherenceRatio: Float = 1.0f,
    val shiftedWorkoutDay: DayOfWeek? = null,
    val deloadTriggered: Boolean = false,
    val splitShakeToggled: Boolean = false
)

object AdaptiveCoachEngine {

    private const val TAG = "AdaptiveCoachEngine"
    const val EWMA_ALPHA = 0.25f

    /**
     * Calculates the Exponentially Weighted Moving Average (EWMA) series
     * for weight logs sorted by date.
     */
    fun calculateEwmaSeries(
        logs: List<WeightProgressEntity>,
        alpha: Float = EWMA_ALPHA
    ): List<Pair<LocalDate, Float>> {
        if (logs.isEmpty()) return emptyList()
        val sorted = logs.sortedBy { it.date }
        val result = mutableListOf<Pair<LocalDate, Float>>()
        var currentEwma = sorted.first().weightKg
        sorted.forEach { entry ->
            val parsedDate = runCatching { LocalDate.parse(entry.date) }.getOrDefault(LocalDate.now())
            currentEwma = (alpha * entry.weightKg) + ((1f - alpha) * currentEwma)
            result.add(parsedDate to currentEwma)
        }
        return result
    }

    /**
     * Computes the current 7-day EWMA weekly gain rate.
     */
    fun computeTrueWeeklyGainRate(logs: List<WeightProgressEntity>): Float {
        val ewmaSeries = calculateEwmaSeries(logs)
        if (ewmaSeries.size < 2) return 0f
        val latest = ewmaSeries.last()
        val sevenDaysAgoDate = latest.first.minusDays(7)
        val past = ewmaSeries.filter { !it.first.isAfter(sevenDaysAgoDate) }.maxByOrNull { it.first }
            ?: ewmaSeries.first()
        val daysBetween = ChronoUnit.DAYS.between(past.first, latest.first).coerceAtLeast(1)
        return ((latest.second - past.second) / daysBetween) * 7f
    }

    /**
     * Closed-loop evaluation and adaptation engine:
     * - Loop A: EWMA Weight Trend & Calorie/Portion Scaling
     * - Loop C: Workout Schedule Auto-Shift & Deload Triggers
     * - Loop D: Chronotype median workout timing & reminder realignment
     */
    suspend fun evaluateAndAdapt(
        database: AppDatabase,
        context: Context,
        force: Boolean = false
    ): AdaptationResult = withContext(Dispatchers.IO) {
        val profileEntity = database.userProfileDao().getUserProfileSync()
            ?: return@withContext AdaptationResult(evaluated = false)

        val profile = UserProfile(
            id = profileEntity.id,
            age = profileEntity.age,
            sex = profileEntity.sex,
            heightCm = profileEntity.heightCm,
            startWeightKg = profileEntity.startWeightKg,
            currentWeightKg = profileEntity.currentWeightKg,
            goalWeightKg = profileEntity.goalWeightKg,
            targetWeeklyGainKg = profileEntity.targetWeeklyGainKg,
            wristInches = profileEntity.wristInches,
            ankleInches = profileEntity.ankleInches,
            currentWaistInches = profileEntity.currentWaistInches,
            currentChestInches = profileEntity.currentChestInches,
            currentArmInches = profileEntity.currentArmInches,
            currentForearmInches = profileEntity.currentForearmInches,
            currentNeckInches = profileEntity.currentNeckInches,
            currentThighInches = profileEntity.currentThighInches,
            currentCalfInches = profileEntity.currentCalfInches,
            startWaistInches = profileEntity.startWaistInches,
            startChestInches = profileEntity.startChestInches,
            startArmInches = profileEntity.startArmInches,
            startForearmInches = profileEntity.startForearmInches,
            startNeckInches = profileEntity.startNeckInches,
            startThighInches = profileEntity.startThighInches,
            startCalfInches = profileEntity.startCalfInches,
            autoAdaptEnabled = profileEntity.autoAdaptEnabled,
            adaptiveCalorieOffset = profileEntity.adaptiveCalorieOffset,
            splitShakeModeActive = profileEntity.splitShakeModeActive,
            dailyStepGoal = profileEntity.dailyStepGoal,
            workoutStartTimeHour = profileEntity.workoutStartTimeHour,
            workoutStartTimeMinute = profileEntity.workoutStartTimeMinute,
            currentRollingSplitIndex = profileEntity.currentRollingSplitIndex,
            lastAdaptationEvalDate = profileEntity.lastAdaptationEvalDate
        )

        if (!profile.autoAdaptEnabled && !force) {
            return@withContext AdaptationResult(evaluated = false)
        }

        val changesApplied = mutableListOf<String>()
        val today = LocalDate.now()
        val todayStr = today.toString()

        // -------------------------------------------------------------------
        // 1. LOOP A: 7-DAY EWMA WEIGHT & AUTO-CALORIE SCALING
        // -------------------------------------------------------------------
        val weightLogs = database.weightDao().getAllWeightLogsSync()
        val weeklyGainRate = computeTrueWeeklyGainRate(weightLogs)

        // 7-Day Meal Adherence
        val last7Days = (0..6).map { today.minusDays(it.toLong()).toString() }
        var totalLoggedCalories = 0
        var totalTargetCalories = 0
        val targetPerDay = profile.effectiveBaseCalories

        last7Days.forEach { d ->
            val meals = database.mealDao().getMealsForDateSync(d)
            val dayLogged = meals.filter { it.isChecked }.sumOf { it.calories }
            totalLoggedCalories += dayLogged
            totalTargetCalories += targetPerDay
        }

        val adherenceRatio = if (totalTargetCalories > 0) {
            (totalLoggedCalories.toFloat() / totalTargetCalories.toFloat()).coerceIn(0f, 2f)
        } else {
            1.0f
        }

        val daysSinceLastEval = if (profile.lastAdaptationEvalDate.isNotBlank()) {
            runCatching {
                ChronoUnit.DAYS.between(LocalDate.parse(profile.lastAdaptationEvalDate), today)
            }.getOrDefault(999L)
        } else 999L

        val canMutateCalories = force || (daysSinceLastEval >= 7)
        var updatedProfile = profileEntity

        if (canMutateCalories) {
            val oldOffset = updatedProfile.adaptiveCalorieOffset

            // Check Waist 14-day delta
            val date14DaysAgo = today.minusDays(14)
            val waistLogs14d = weightLogs.filter {
                runCatching { !LocalDate.parse(it.date).isBefore(date14DaysAgo) }.getOrDefault(false)
            }
            val waistDelta14d = if (waistLogs14d.size >= 2) {
                val sorted = waistLogs14d.sortedBy { it.date }
                sorted.last().waistInches - sorted.first().waistInches
            } else {
                0f
            }

            // Case 2 check: Waist Guardrail / Rapid Fat Accumulation
            val isWaistTrigger = (profile.whtr > 0.48f) || (waistDelta14d >= 0.40f) || (weeklyGainRate > 0.50f)

            if (isWaistTrigger) {
                val newOffset = (oldOffset - 150).coerceAtLeast(-300)
                val newStepGoal = updatedProfile.dailyStepGoal.coerceAtLeast(8500)
                if (newOffset != oldOffset || newStepGoal != updatedProfile.dailyStepGoal || force) {
                    updatedProfile = updatedProfile.copy(
                        adaptiveCalorieOffset = newOffset,
                        dailyStepGoal = newStepGoal,
                        lastAdaptationEvalDate = todayStr
                    )
                    database.userProfileDao().upsertUserProfile(updatedProfile)

                    val gainFormatted = String.format("%.2f", weeklyGainRate)
                    val whtrFormatted = String.format("%.3f", profile.whtr)
                    database.adaptationLogDao().insert(
                        AdaptationLogEntity(
                            date = todayStr,
                            category = "WAIST_GUARDRAIL",
                            title = "Waist Guardrail Triggered: Trimmed Surplus & Raised Steps",
                            reason = "Waist/Height ratio is $whtrFormatted (limit 0.48) or weekly gain was +$gainFormatted kg/wk. Reducing carb surplus to prioritize lean mass.",
                            parameterChanged = "adaptiveCalorieOffset: $oldOffset -> $newOffset kcal | dailyStepGoal: ${profileEntity.dailyStepGoal} -> $newStepGoal | Protein: 2.15 g/kg"
                        )
                    )
                    changesApplied.add("Waist Guardrail: Surplus trimmed by 150 kcal, step goal raised to $newStepGoal")
                }
            } else if (adherenceRatio >= 0.80f && weeklyGainRate < (profile.targetWeeklyGainKg * 0.55f)) {
                // Case 1: Plateau Detected (and adherence was good)
                val newOffset = (oldOffset + 150).coerceAtMost(600)
                if (newOffset != oldOffset || force) {
                    updatedProfile = updatedProfile.copy(
                        adaptiveCalorieOffset = newOffset,
                        lastAdaptationEvalDate = todayStr
                    )
                    database.userProfileDao().upsertUserProfile(updatedProfile)

                    val gainFormatted = String.format("%.2f", weeklyGainRate)
                    val targetFormatted = String.format("%.2f", profile.targetWeeklyGainKg)
                    val adherencePct = (adherenceRatio * 100).roundToInt()
                    database.adaptationLogDao().insert(
                        AdaptationLogEntity(
                            date = todayStr,
                            category = "NUTRITION",
                            title = "Auto-Boosted Daily Surplus by +150 kcal",
                            reason = "7-day EWMA gain was +$gainFormatted kg/wk vs target +$targetFormatted kg/wk despite $adherencePct% adherence.",
                            parameterChanged = "adaptiveCalorieOffset: $oldOffset -> $newOffset kcal | Breakfast & Shake scaled +75 kcal each"
                        )
                    )
                    changesApplied.add("Plateau Detected: Daily target auto-boosted by +150 kcal")
                }
            }
        }

        // Case 3: Evening Fullness / Split Shake Trigger
        var splitShakeToggled = false
        val last5Days = (0..4).map { today.minusDays(it.toLong()).toString() }
        var dinnerCompletedCount = 0
        last5Days.forEach { d ->
            val meals = database.mealDao().getMealsForDateSync(d)
            if (meals.any { it.id == "meal_dinner" && it.isChecked }) {
                dinnerCompletedCount++
            }
        }

        if (!updatedProfile.splitShakeModeActive && dinnerCompletedCount <= 3 && profileEntity.workoutStartTimeHour >= 19) {
            updatedProfile = updatedProfile.copy(splitShakeModeActive = true)
            database.userProfileDao().upsertUserProfile(updatedProfile)
            splitShakeToggled = true

            database.adaptationLogDao().insert(
                AdaptationLogEntity(
                    date = todayStr,
                    category = "NUTRITION",
                    title = "Auto-Enabled Split Shake Mode",
                    reason = "Dinner adherence was only $dinnerCompletedCount/5 after late-evening workouts. Shifted 1 scoop whey to 8:05 PM post-workout to keep dinner light and comfortable.",
                    parameterChanged = "splitShakeModeActive: false -> true | 11 AM Shake: 370 kcal | 8:05 PM Whey: 120 kcal"
                )
            )
            changesApplied.add("Split Shake Activated: Lightened 11 AM shake & inserted 8:05 PM post-workout whey")
        }

        // -------------------------------------------------------------------
        // 2. LOOP C: ADAPTIVE WORKOUT QUEUE & RECOVERY
        // -------------------------------------------------------------------
        val yesterday = today.minusDays(1)
        val yesterdayDay = yesterday.dayOfWeek
        var shiftedDay: DayOfWeek? = null

        if (yesterdayDay != DayOfWeek.SUNDAY) {
            val yesterdayLogs = database.workoutDao().getWorkoutLogsForDateSync(yesterday.toString())
            val completedYesterday = yesterdayLogs.count { it.isCompleted }
            if (completedYesterday == 0) {
                // Yesterday was completely missed! Auto-shift yesterday's session to today
                shiftedDay = yesterdayDay
                changesApplied.add("Workout Shift: Yesterday's missed ${yesterdayDay.name.lowercase().replaceFirstChar { it.uppercase() }} workout shifted to Today")
            }
        }

        // Progression Deload check: 2 consecutive "Near Failure" with set3RepsAchieved < repsPerSet
        val recentLogs = database.workoutDao().getAllWorkoutLogsSync()
            .filter { it.isCompleted && it.completedAtMillis > 0 }
            .sortedByDescending { it.completedAtMillis }

        var deloadTriggered = false
        val failedSessions = recentLogs.take(2).filter {
            it.effortRating == "Near Failure" && it.set3RepsAchieved < it.repsPerSet
        }
        if (failedSessions.size >= 2) {
            deloadTriggered = true
            database.adaptationLogDao().insert(
                AdaptationLogEntity(
                    date = todayStr,
                    category = "OVERLOAD",
                    title = "Autonomous Deload Recommended",
                    reason = "User logged 2 consecutive sessions at Near Failure missing rep targets. Form fatigue detected.",
                    parameterChanged = "Recommended: Week 1 Neuromuscular Groove tempo & -20% volume"
                )
            )
            changesApplied.add("Deload Triggered: 2 consecutive missed rep sessions detected. Week 1 tempo recommended.")
        }

        // -------------------------------------------------------------------
        // 3. LOOP D: 7:30 PM CHRONOTYPE & WORKOUT MEDIAN TIMING
        // -------------------------------------------------------------------
        val workoutsWithTimestamps = recentLogs.take(5)
        if (workoutsWithTimestamps.isNotEmpty()) {
            val minutesOfDayList = workoutsWithTimestamps.mapNotNull { log ->
                val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.completedAtMillis), ZoneId.systemDefault())
                ldt.hour * 60 + ldt.minute
            }.sorted()

            if (minutesOfDayList.isNotEmpty()) {
                val medianMinutes = minutesOfDayList[minutesOfDayList.size / 2]
                val medianHour = medianMinutes / 60
                val medianMinute = medianMinutes % 60
                val currentHour = updatedProfile.workoutStartTimeHour
                val currentMinute = updatedProfile.workoutStartTimeMinute
                val currentTotalMinutes = currentHour * 60 + currentMinute

                // If deviation is greater than 20 minutes, auto-align reminder timings
                if (kotlin.math.abs(medianMinutes - currentTotalMinutes) >= 20) {
                    val oldTimeStr = String.format("%02d:%02d", currentHour, currentMinute)
                    val newTimeStr = String.format("%02d:%02d", medianHour, medianMinute)

                    updatedProfile = updatedProfile.copy(
                        workoutStartTimeHour = medianHour,
                        workoutStartTimeMinute = medianMinute
                    )
                    database.userProfileDao().upsertUserProfile(updatedProfile)

                    // Realign Reminders in Database
                    // 104: Pre-workout fuel (-105 min)
                    val fuelMin = (medianMinutes - 105).coerceAtLeast(0)
                    database.reminderDao().updateReminderTime(104, fuelMin / 60, fuelMin % 60)

                    // 109: Pre-workout primer (-15 min)
                    val primerMin = (medianMinutes - 15).coerceAtLeast(0)
                    database.reminderDao().updateReminderTime(109, primerMin / 60, primerMin % 60)

                    // 105: Workout start
                    database.reminderDao().updateReminderTime(105, medianHour, medianMinute)

                    // 110: Post-workout protein (+35 min)
                    val proteinMin = (medianMinutes + 35) % 1440
                    database.reminderDao().updateReminderTime(110, proteinMin / 60, proteinMin % 60)

                    // 106: Post-workout dinner (+75 min)
                    val dinnerMin = (medianMinutes + 75) % 1440
                    database.reminderDao().updateReminderTime(106, dinnerMin / 60, dinnerMin % 60)

                    // Reschedule in AlarmManager
                    val activeReminders = database.reminderDao().getActiveReminders()
                    activeReminders.forEach { r ->
                        NotificationScheduler.scheduleReminder(
                            context,
                            ReminderItem(
                                id = r.id,
                                title = r.title,
                                description = r.description,
                                hour = r.hour,
                                minute = r.minute,
                                isEnabled = r.isEnabled,
                                isWeeklyOnly = r.isWeeklyOnly,
                                weeklyDayOfWeek = r.weeklyDayOfWeek
                            )
                        )
                    }

                    database.adaptationLogDao().insert(
                        AdaptationLogEntity(
                            date = todayStr,
                            category = "CHRONOTYPE",
                            title = "Adapted Workout Reminder Schedule",
                            reason = "Median workout completion time over recent workouts was $newTimeStr. Auto-aligned Pre-Workout Fuel, Primer, and Dinner alarms.",
                            parameterChanged = "workoutStartTime: $oldTimeStr -> $newTimeStr"
                        )
                    )
                    changesApplied.add("Chronotype Realignment: Workout reminders shifted to $newTimeStr based on logged sessions")
                }
            }
        }

        AdaptationResult(
            evaluated = true,
            changesApplied = changesApplied,
            ewmaGainRate = weeklyGainRate,
            adherenceRatio = adherenceRatio,
            shiftedWorkoutDay = shiftedDay,
            deloadTriggered = deloadTriggered,
            splitShakeToggled = splitShakeToggled
        )
    }

    /**
     * Determines which workout plan should be shown for today,
     * applying the Missed Workout Auto-Shift and 48-Hour Fatigue Lock.
     */
    suspend fun getAdaptivePlanDay(
        database: AppDatabase,
        today: LocalDate = LocalDate.now()
    ): DayOfWeek = withContext(Dispatchers.IO) {
        val profile = database.userProfileDao().getUserProfileSync()
        if (profile?.autoAdaptEnabled != true) {
            return@withContext today.dayOfWeek
        }

        val yesterday = today.minusDays(1)
        if (yesterday.dayOfWeek == DayOfWeek.SUNDAY) {
            return@withContext today.dayOfWeek
        }

        val yesterdayLogs = database.workoutDao().getWorkoutLogsForDateSync(yesterday.toString())
        val completedYesterday = yesterdayLogs.count { it.isCompleted }

        // If yesterday was completely missed, auto-shift yesterday's workout to today!
        if (completedYesterday == 0) {
            return@withContext yesterday.dayOfWeek
        }

        today.dayOfWeek
    }

    /**
     * Extracts an [AdaptiveStatusInfo] summary for the status UI card.
     */
    fun extractStatusInfo(
        profile: UserProfile,
        weightLogs: List<WeightProgressEntity>,
        isShifted: Boolean = false,
        shiftedDay: DayOfWeek? = null
    ): AdaptiveStatusInfo {
        val ewmaGain = computeTrueWeeklyGainRate(weightLogs)
        val workoutQueue = if (isShifted && shiftedDay != null) {
            "Auto-Shifted (${shiftedDay.name.lowercase().replaceFirstChar { it.uppercase() }} moved to Today)"
        } else {
            "On Schedule (Normal Rotation)"
        }

        val eveningStrategy = if (profile.splitShakeModeActive) {
            "Split Shake Active (8:05 PM Post-Workout Whey)"
        } else {
            "Standard Single Shake (11:00 AM)"
        }

        return AdaptiveStatusInfo(
            autoAdaptEnabled = profile.autoAdaptEnabled,
            ewmaGainRateKgPerWeek = ewmaGain,
            targetWeeklyGainKg = profile.targetWeeklyGainKg,
            adaptiveCalorieOffset = profile.adaptiveCalorieOffset,
            effectiveBaseCalories = profile.effectiveBaseCalories,
            workoutQueueStatus = workoutQueue,
            isShifted = isShifted,
            eveningStrategy = eveningStrategy,
            isSplitShakeActive = profile.splitShakeModeActive,
            whtr = profile.whtr,
            lastEvalDate = profile.lastAdaptationEvalDate
        )
    }
}
