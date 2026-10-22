package com.leanbulk.tracker.data.model

import java.time.LocalDate

data class WeightEntry(
    val id: String = "",
    val date: LocalDate,
    val weightKg: Float,
    val waistInches: Float,
    val notes: String = ""
)

enum class CoachingStatus {
    OPTIMAL,
    PLATEAU,
    FAST_GAIN,
    MODERATE,
    NEED_DATA
}

data class CoachingInsight(
    val status: CoachingStatus,
    val title: String,
    val message: String,
    val weeklyGainKg: Float?,
    val waistChangeInches: Float?,
    val actionableTip: String
) {
    companion object {
        fun evaluate(entries: List<WeightEntry>): CoachingInsight {
            if (entries.size < 2) {
                return CoachingInsight(
                    status = CoachingStatus.NEED_DATA,
                    title = "Baseline Calibration",
                    message = "Log your morning fasting weight and waist size across at least 2 days to enable personalized 14-day Lean Bulk trajectory coaching.",
                    weeklyGainKg = null,
                    waistChangeInches = null,
                    actionableTip = "Target: +0.25 to +0.35 kg/week with 2,420 kcal South Indian surplus."
                )
            }

            val sorted = entries.sortedBy { it.date }
            val now = LocalDate.now()
            val cutoff = now.minusDays(14)
            val windowEntries = sorted.filter { !it.date.isBefore(cutoff) }.ifEmpty { sorted.takeLast(5) }

            if (windowEntries.size < 2) {
                return CoachingInsight(
                    status = CoachingStatus.NEED_DATA,
                    title = "Recent Logs Required",
                    message = "We need at least two logs within the last 14 days to compute an accurate weekly rate.",
                    weeklyGainKg = null,
                    waistChangeInches = null,
                    actionableTip = "Log your fasting weight every morning after bathroom visit."
                )
            }

            val first = windowEntries.first()
            val last = windowEntries.last()
            val days = java.time.temporal.ChronoUnit.DAYS.between(first.date, last.date).coerceAtLeast(1)
            val totalWeightDiff = last.weightKg - first.weightKg
            val weeklyGain = (totalWeightDiff / days) * 7.0f
            val waistDiff = last.waistInches - first.waistInches

            return when {
                // If weekly gain > 0.50 kg or waist increases > 0.5 inch in 2 weeks
                weeklyGain > 0.50f || waistDiff > 0.50f -> CoachingInsight(
                    status = CoachingStatus.FAST_GAIN,
                    title = "Fast Gain Alert",
                    message = "Fast Gain Alert: Reduce rice by 0.5 cup and increase workout time-under-tension.",
                    weeklyGainKg = weeklyGain,
                    waistChangeInches = waistDiff,
                    actionableTip = "Your current gain rate is +%.2f kg/wk (Waist: %+.1f\"). Trim ~150 kcal (half cup rice) to minimize adipose accumulation.".format(weeklyGain, waistDiff)
                )

                // If weekly gain < 0.20 kg
                weeklyGain < 0.20f -> CoachingInsight(
                    status = CoachingStatus.PLATEAU,
                    title = "Plateau Alert",
                    message = "Plateau Alert: Add 1 extra tbsp peanut butter or +0.5 cup rice (+150 kcal/day).",
                    weeklyGainKg = weeklyGain,
                    waistChangeInches = waistDiff,
                    actionableTip = "Weekly pace is +%.2f kg/wk. Increase caloric surplus slightly to ensure progressive muscle synthesis.".format(weeklyGain)
                )

                // If weekly gain is 0.25 - 0.35 kg
                weeklyGain in 0.25f..0.35f -> CoachingInsight(
                    status = CoachingStatus.OPTIMAL,
                    title = "Optimal Lean Bulk Zone!",
                    message = "Optimal Lean Bulk Zone! Keep current calories and push sets near failure.",
                    weeklyGainKg = weeklyGain,
                    waistChangeInches = waistDiff,
                    actionableTip = "Pace: +%.2f kg/wk! Hypertrophy stimulus with single 4kg DB + bodyweight is locked in perfectly.".format(weeklyGain)
                )

                // Between 0.20 and 0.25 kg
                weeklyGain < 0.25f -> CoachingInsight(
                    status = CoachingStatus.OPTIMAL,
                    title = "Steady Lean Bulk",
                    message = "Pacing well (+%.2f kg/wk). Approaching optimal target zone (+0.25 - 0.35 kg/wk).".format(weeklyGain),
                    weeklyGainKg = weeklyGain,
                    waistChangeInches = waistDiff,
                    actionableTip = "Maintain South Indian meal structure and monitor for one more week."
                )

                // Between 0.35 and 0.50 kg
                else -> CoachingInsight(
                    status = CoachingStatus.MODERATE,
                    title = "Upper Bulk Pacing",
                    message = "Weekly gain is +%.2f kg/wk. Solid mass gain; watch waist circumference closely.".format(weeklyGain),
                    weeklyGainKg = weeklyGain,
                    waistChangeInches = waistDiff,
                    actionableTip = "Keep pushing dumbbell reps to failure to convert fuel into lean tissue."
                )
            }
        }
    }
}
