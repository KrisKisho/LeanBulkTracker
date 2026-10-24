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
    val isWaistGuardrailActive: Boolean = false
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

        // Step 5: Dynamic Surplus Target ((WeeklyGain * 7700) / 7) + Adaptive Offset
        val dailySurplus = ((profile.targetWeeklyGainKg * 7700f) / 7f).roundToInt()
        val adaptiveOffset = if (profile.autoAdaptEnabled) profile.adaptiveCalorieOffset else 0
        val dynamicCalorieTarget = totalTodayTDEE + dailySurplus + adaptiveOffset

        // Step 6: Dynamic Macros (2.15g/kg protein if Waist Guardrail / WHtR alert is active)
        val isWaistGuardrailActive = profile.autoAdaptEnabled && (profile.adaptiveCalorieOffset < 0 || profile.whtr > 0.48f)
        val proteinMultiplier = if (isWaistGuardrailActive) 2.15f else 2.0f
        val proteinTarget = (weight * proteinMultiplier).roundToInt()
        val fatTarget = (weight * 1.05f).roundToInt()
        val carbTarget = ((dynamicCalorieTarget - (proteinTarget * 4) - (fatTarget * 9)) / 4).coerceAtLeast(100)

        // Step 7: South Indian Top-Up recommendations
        // Triggers cleanly at ~9,500–10,000 steps on training days whenever dynamicCalorieTarget - (2420 + adaptiveOffset) >= 150
        val baseTarget = 2420 + adaptiveOffset
        val excess = dynamicCalorieTarget - baseTarget
        val topUp = when {
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

        return DynamicNutritionTarget(
            bmr = bmr,
            baseSedentaryBurn = baseSedentaryBurn,
            stepCaloriesBurned = stepBurn,
            workoutBurn = workoutBurn,
            completedWorkoutBurn = completedWorkoutBurn,
            isWorkoutAnticipated = isWorkoutAnticipated,
            totalTodayTDEE = totalTodayTDEE,
            dailySurplus = dailySurplus,
            dynamicCalorieTarget = dynamicCalorieTarget,
            proteinTargetGrams = proteinTarget,
            fatTargetGrams = fatTarget,
            carbTargetGrams = carbTarget,
            excessBurnKcal = excess,
            topUpRecommendation = topUp,
            adaptiveCalorieOffset = adaptiveOffset,
            isWaistGuardrailActive = isWaistGuardrailActive
        )
    }
}
