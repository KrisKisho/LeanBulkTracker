package com.leanbulk.tracker.data.model

import java.time.LocalDate
import kotlin.math.pow

data class DimensionComparison(
    val partName: String,
    val currentInches: Float,
    val startInches: Float = currentInches,
    val projectedInches: Float,
    val idealInches: Float,
    val statusText: String,
    val isWaist: Boolean = false,
    val isWarning: Boolean = false
) {
    val deltaInches: Float get() = currentInches - startInches
}

data class BodyForecastResult(
    val startWeightKg: Float,
    val currentWeightKg: Float,
    val goalWeightKg: Float,
    val weightProgressFraction: Float,
    val remainingKg: Float,
    val weeksRemaining: Float,
    val estimatedCompletionDate: LocalDate,
    val maxHealthyWaistInches: Float,
    val hardLimitWaistInches: Float,
    val projectedWaistInches: Float,
    val currentWhtr: Float,
    val isWhtrWarning: Boolean,
    val whtrWarningMessage: String,
    val dimensions: List<DimensionComparison>
)

object BodyDimensionCalculator {

    fun calculate(profile: UserProfile): BodyForecastResult {
        val startW = profile.startWeightKg.coerceAtLeast(30f)
        val currentW = profile.currentWeightKg.coerceAtLeast(startW)
        val goalW = profile.goalWeightKg.coerceAtLeast(startW)
        val weeklyGain = profile.targetWeeklyGainKg.coerceAtLeast(0.15f)

        val totalToGain = (goalW - startW).coerceAtLeast(0.1f)
        val gainedSoFar = (currentW - startW).coerceAtLeast(0f)
        val progressFraction = (gainedSoFar / totalToGain).coerceIn(0f, 1f)

        val remainingKg = (goalW - currentW).coerceAtLeast(0f)
        val weeksRemaining = if (weeklyGain > 0f) remainingKg / weeklyGain else 0f
        val completionDate = LocalDate.now().plusDays((weeksRemaining * 7).toLong())

        val weightRatio = (goalW / startW).coerceAtLeast(1.0f)
        val muscleScale = weightRatio.pow(0.38f)
        val extremityScale = weightRatio.pow(0.30f)

        // Column A: Projected at Goal Weight
        val projChest = profile.currentChestInches * muscleScale
        val projArm = profile.currentArmInches * muscleScale
        val projForearm = profile.currentForearmInches * extremityScale
        val projNeck = profile.currentNeckInches * extremityScale
        val projThigh = profile.currentThighInches * muscleScale
        val projCalf = profile.currentCalfInches * extremityScale

        // Column B: Frame Symmetry Benchmark (McCallum Wrist-based Proportions)
        val wrist = profile.wristInches.coerceAtLeast(4.0f)
        val idealChest = wrist * 6.5f
        val idealArm = idealChest * 0.36f
        val idealForearm = idealChest * 0.29f
        val idealNeck = idealChest * 0.37f
        val idealThigh = idealChest * 0.53f
        val idealCalf = idealChest * 0.34f

        // Waist Limits
        val heightCm = profile.heightCm.coerceAtLeast(120f)
        val maxHealthyWaist = (heightCm * 0.46f) / 2.54f
        val hardLimitWaist = (heightCm * 0.48f) / 2.54f
        val expectedWaistGain = (goalW - startW) * 0.12f
        val projectedWaist = (profile.currentWaistInches + expectedWaistGain).coerceAtMost(maxHealthyWaist)

        // WHtR Check
        val currentWhtr = (profile.currentWaistInches * 2.54f) / heightCm
        val isWhtrWarning = currentWhtr > 0.48f
        val whtrWarningMessage = if (isWhtrWarning) {
            "Waist is growing faster than muscle frame! WHtR: %.2f (safe < 0.48). Reduce daily surplus by 150 kcal (0.5 cup rice) and prioritize progressive overload.".format(currentWhtr)
        } else ""

        val list = listOf(
            DimensionComparison(
                partName = "Chest",
                currentInches = profile.currentChestInches,
                startInches = profile.startChestInches,
                projectedInches = projChest,
                idealInches = idealChest,
                statusText = if (projChest >= idealChest * 0.96f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Flexed Arm",
                currentInches = profile.currentArmInches,
                startInches = profile.startArmInches,
                projectedInches = projArm,
                idealInches = idealArm,
                statusText = if (projArm >= idealArm * 0.95f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Forearm",
                currentInches = profile.currentForearmInches,
                startInches = profile.startForearmInches,
                projectedInches = projForearm,
                idealInches = idealForearm,
                statusText = if (projForearm >= idealForearm * 0.95f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Neck",
                currentInches = profile.currentNeckInches,
                startInches = profile.startNeckInches,
                projectedInches = projNeck,
                idealInches = idealNeck,
                statusText = if (projNeck >= idealNeck * 0.95f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Thigh",
                currentInches = profile.currentThighInches,
                startInches = profile.startThighInches,
                projectedInches = projThigh,
                idealInches = idealThigh,
                statusText = if (projThigh >= idealThigh * 0.95f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Calf",
                currentInches = profile.currentCalfInches,
                startInches = profile.startCalfInches,
                projectedInches = projCalf,
                idealInches = idealCalf,
                statusText = if (projCalf >= idealCalf * 0.95f) "On Track" else "Room to Grow"
            ),
            DimensionComparison(
                partName = "Navel Waist",
                currentInches = profile.currentWaistInches,
                startInches = profile.startWaistInches,
                projectedInches = projectedWaist,
                idealInches = maxHealthyWaist,
                statusText = if (profile.currentWaistInches > maxHealthyWaist) "Waist Warning" else "Safe Ceiling",
                isWaist = true,
                isWarning = profile.currentWaistInches > maxHealthyWaist
            )
        )

        return BodyForecastResult(
            startWeightKg = startW,
            currentWeightKg = currentW,
            goalWeightKg = goalW,
            weightProgressFraction = progressFraction,
            remainingKg = remainingKg,
            weeksRemaining = weeksRemaining,
            estimatedCompletionDate = completionDate,
            maxHealthyWaistInches = maxHealthyWaist,
            hardLimitWaistInches = hardLimitWaist,
            projectedWaistInches = projectedWaist,
            currentWhtr = currentWhtr,
            isWhtrWarning = isWhtrWarning,
            whtrWarningMessage = whtrWarningMessage,
            dimensions = list
        )
    }
}
