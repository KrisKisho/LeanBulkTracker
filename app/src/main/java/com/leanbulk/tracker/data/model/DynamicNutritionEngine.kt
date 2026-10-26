package com.leanbulk.tracker.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

data class DynamicNutritionTarget(
    val bmr: Int,
    val baseSedentaryBurn: Int,
    val stepCaloriesBurned: Int,
    val workoutBurn: Int,
    val completedWorkoutBurn: Int = workoutBurn,
    val isWorkoutAnticipated: Boolean = false,
    val totalTodayTDEE: Int,
    val dailySurplus: Int,
    val dynamicCalorieTarget: Int,
    val proteinTargetGrams: Int,
    val fatTargetGrams: Int,
    val carbTargetGrams: Int,
    val excessBurnKcal: Int,
    val topUpRecommendation: SouthIndianTopUp?,
    val adaptiveCalorieOffset: Int = 0,
    val isWaistGuardrailActive: Boolean = false,
    val isWaistRecompActive: Boolean = false,
    val recompBadgeText: String? = null,
    val goalMode: String = "LEAN_BULK",
    val minSafeCalories: Int = bmr
)

data class SouthIndianTopUp(
    val title: String,
    val description: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

object DynamicNutritionEngine {

    fun calculate(
        profile: UserProfile,
        dailySteps: Int,
        activeCaloriesBurned: Int,
        completedWorkoutSegmentsCount: Int = 0,
        totalSegmentsForDay: Int = 5,
        dayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
        currentTime: LocalTime = LocalTime.now(),
        includePlannedWorkoutInTarget: Boolean = true
    ): DynamicNutritionTarget {
        val weight = profile.currentWeightKg.coerceAtLeast(30f)
        val height = profile.heightCm.coerceAtLeast(100f)
        val age = profile.age.coerceAtLeast(15)

        // Step 1: BMR (Mifflin-St Jeor)
        val bmr = if (profile.sex.equals("Female", ignoreCase = true)) {
            (10f * weight) + (6.25f * height) - (5f * age) - 161f
        } else {
            (10f * weight) + (6.25f * height) - (5f * age) + 5f
        }.roundToInt()

        // Step 2: Base Sedentary TDEE (BMR * 1.2)
        val baseSedentaryBurn = (bmr * 1.2f).roundToInt()

        // Step 3: Live Activity / Step Burn (approx. 30 kcal per 1,000 steps at 60kg)
        val stepBurn = if (activeCaloriesBurned > 0) {
            activeCaloriesBurned
        } else {
            (dailySteps * weight * 0.0005f).roundToInt()
        }

        // Step 4: Workout Burn normalized by the day's segment count
        // Mon-Sat training days = 220 kcal; Sunday active recovery = 110 kcal
        val maxDayWorkoutBurn = if (dayOfWeek == DayOfWeek.SUNDAY) 110 else 220
        val completedWorkoutBurn = ((completedWorkoutSegmentsCount.toFloat() / totalSegmentsForDay.coerceAtLeast(1)) * maxDayWorkoutBurn).toInt()

        // Before 8:00 PM on training days, anticipate planned workout burn in daily target
        // (prevents calorie target from reading 2,191 kcal in morning while base meal plan is 2,420 kcal)
        val isBefore8PM = currentTime.hour < 20
        val isWorkoutAnticipated = includePlannedWorkoutInTarget && isBefore8PM && (completedWorkoutSegmentsCount < totalSegmentsForDay)

        val workoutBurn = if (isWorkoutAnticipated) {
            maxOf(completedWorkoutBurn, maxDayWorkoutBurn)
        } else {
            completedWorkoutBurn
        }

        val totalTodayTDEE = baseSedentaryBurn + stepBurn + workoutBurn
        val resolvedMode = profile.resolvedGoalMode
        val isWaistRecompActive = profile.bellyFatRecompMode || resolvedMode == "BODY_RECOMP"
        val isWaistGuardrailActive = profile.autoAdaptEnabled && (profile.adaptiveCalorieOffset < 0 || profile.whtr > 0.48f)
        val adaptiveOffset = if (profile.autoAdaptEnabled) profile.adaptiveCalorieOffset else 0
        val minSafeCalories = maxOf(bmr, 1200)

        val dailySurplusOrDeficit: Int
        val dynamicCalorieTarget: Int
        val proteinTarget: Int
        val fatTarget: Int
        val carbTarget: Int
        val recompBadge: String?
        val topUp: SouthIndianTopUp?
        var excessKcal = 0

        when (resolvedMode) {
            "WEIGHT_LOSS" -> {
                // Mode B: WEIGHT_LOSS (Fat-Loss Logic with Clinical Safety Floors)
                val requestedDeficit = ((profile.targetWeeklyGainKg.coerceAtLeast(0.25f) * 7700f) / 7f).coerceIn(300f, 750f)
                val rawTarget = (totalTodayTDEE - requestedDeficit + adaptiveOffset).roundToInt()
                dynamicCalorieTarget = maxOf(rawTarget, minSafeCalories)
                dailySurplusOrDeficit = dynamicCalorieTarget - totalTodayTDEE

                // Muscle-Sparing Cut Macros: 2.15g/kg protein, min 45g fat
                proteinTarget = (weight * 2.15f).roundToInt()
                fatTarget = (weight * 0.85f).coerceAtLeast(45f).roundToInt()
                carbTarget = ((dynamicCalorieTarget - (proteinTarget * 4) - (fatTarget * 9)) / 4).coerceAtLeast(100)

                val actualDeficit = totalTodayTDEE - dynamicCalorieTarget
                recompBadge = "🔥 Fat-Loss Active: -${actualDeficit} kcal Deficit mobilizing body fat • Muscle-Sparing Protein (${proteinTarget}g) active"

                // Over-Deficit Muscle Protection Guardrail:
                // Only if high steps push total daily deficit beyond 750 kcal or near BMR
                topUp = if (actualDeficit > 750 || dynamicCalorieTarget <= minSafeCalories + 100) {
                    SouthIndianTopUp(
                        title = "Lean Muscle-Sparing Refuel",
                        description = "+3 Boiled Egg Whites + 1 Glass Spiced Buttermilk Majjiga (+95 kcal | 14g P)",
                        calories = 95,
                        protein = 14,
                        carbs = 3,
                        fat = 1
                    )
                } else null
            }
            "BODY_RECOMP" -> {
                // Mode C: BODY_RECOMP
                dailySurplusOrDeficit = -100
                dynamicCalorieTarget = totalTodayTDEE - 100 + adaptiveOffset
                proteinTarget = (weight * 2.2f).roundToInt()
                fatTarget = (weight * 0.95f).coerceAtLeast(45f).roundToInt()
                carbTarget = ((dynamicCalorieTarget - (proteinTarget * 4) - (fatTarget * 9)) / 4).coerceAtLeast(100)
                recompBadge = "⚖️ Body Recomp Active: -100 kcal buffer • 2.2g/kg Protein for muscle build"
                topUp = null
            }
            else -> { // Mode A: LEAN_BULK
                dailySurplusOrDeficit = if (isWaistRecompActive) 220 else ((profile.targetWeeklyGainKg * 7700f) / 7f).roundToInt()
                dynamicCalorieTarget = totalTodayTDEE + dailySurplusOrDeficit + adaptiveOffset

                val proteinMultiplier = if (isWaistGuardrailActive || isWaistRecompActive) 2.15f else 2.0f
                proteinTarget = (weight * proteinMultiplier).roundToInt()
                fatTarget = (weight * 1.05f).roundToInt()
                carbTarget = ((dynamicCalorieTarget - (proteinTarget * 4) - (fatTarget * 9)) / 4).coerceAtLeast(100)

                val baseTarget = 2420 + adaptiveOffset
                val excess = dynamicCalorieTarget - baseTarget
                excessKcal = excess
                topUp = when {
                    excess in 150..249 -> SouthIndianTopUp(
                        title = "Banana & Peanut Butter Top-Up",
                        description = "1 Ripe Banana + 1 tbsp Peanut Butter (+195 kcal | 4g P)",
                        calories = 195,
                        protein = 4,
                        carbs = 27,
                        fat = 8
                    )
                    excess in 250..399 -> SouthIndianTopUp(
                        title = "Rice, Ghee & Egg Top-Up",
                        description = "0.75 cup Extra Steamed Rice + 1 tsp Ghee + 1 Boiled Egg (+285 kcal | 9g P)",
                        calories = 285,
                        protein = 9,
                        carbs = 42,
                        fat = 9
                    )
                    excess >= 400 -> SouthIndianTopUp(
                        title = "Whole Milk & Roasted Chana Top-Up",
                        description = "1 Glass Whole Milk (250ml) + 30g Roasted Chana/Peanuts (+320 kcal | 15g P)",
                        calories = 320,
                        protein = 15,
                        carbs = 22,
                        fat = 18
                    )
                    else -> null
                }

                recompBadge = if (isWaistRecompActive) {
                    "Waist Recomp Active: +220 kcal Lean Surplus • 95 kcal Core Finisher Burn (Not Eaten Back)"
                } else null
            }
        }

        return DynamicNutritionTarget(
            bmr = bmr,
            baseSedentaryBurn = baseSedentaryBurn,
            stepCaloriesBurned = stepBurn,
            workoutBurn = workoutBurn,
            completedWorkoutBurn = completedWorkoutBurn,
            isWorkoutAnticipated = isWorkoutAnticipated,
            totalTodayTDEE = totalTodayTDEE,
            dailySurplus = dailySurplusOrDeficit,
            dynamicCalorieTarget = dynamicCalorieTarget,
            proteinTargetGrams = proteinTarget,
            fatTargetGrams = fatTarget,
            carbTargetGrams = carbTarget,
            excessBurnKcal = excessKcal,
            topUpRecommendation = topUp,
            adaptiveCalorieOffset = adaptiveOffset,
            isWaistGuardrailActive = isWaistGuardrailActive,
            isWaistRecompActive = isWaistRecompActive,
            recompBadgeText = recompBadge,
            goalMode = resolvedMode,
            minSafeCalories = minSafeCalories
        )
    }
}
