package com.leanbulk.tracker.data.model

data class LoadSetupAdvice(
    val title: String,
    val details: String,
    val equipmentTag: String,
    val canSwapBarbell: Boolean = false,
    val barbellExerciseName: String = "",
    val barbellWeightKg: Float = 0f,
    val canSwapBodyweight: Boolean = false,
    val bodyweightAlternative: String = ""
)

object EquipmentLoadTranslator {

    fun getAdviceForSegment(
        segmentTitle: String,
        targetWeightKg: Float,
        category: MovementCategory,
        inventory: HomeGymInventory
    ): LoadSetupAdvice {
        val titleLower = segmentTitle.lowercase()

        // 1. Check for Barbell Swap eligibility if user has barbell
        if (inventory.hasBarbell) {
            when {
                titleLower.contains("squat") || titleLower.contains("deadlift") || titleLower.contains("rdl") || titleLower.contains("lunge") -> {
                    val bilateralLoad = (targetWeightKg * 2.0f).coerceAtLeast(15.0f)
                    return LoadSetupAdvice(
                        title = "Barbell Swap Available",
                        details = "Swap to Barbell Squat / Barbell RDL (Prescription: %.1f kg bilateral load)".format(bilateralLoad),
                        equipmentTag = "BARBELL",
                        canSwapBarbell = true,
                        barbellExerciseName = if (titleLower.contains("squat")) "Barbell Back/Front Squat" else "Barbell Romanian Deadlift",
                        barbellWeightKg = bilateralLoad
                    )
                }
                titleLower.contains("floor press") || titleLower.contains("bench press") || titleLower.contains("crush press") -> {
                    val bilateralLoad = (targetWeightKg * 2.0f).coerceAtLeast(15.0f)
                    return LoadSetupAdvice(
                        title = "Barbell Press Swap Available",
                        details = "Swap to Barbell Floor/Bench Press (Prescription: %.1f kg • cuts set time in half bilateral)".format(bilateralLoad),
                        equipmentTag = "BARBELL",
                        canSwapBarbell = true,
                        barbellExerciseName = "Barbell Floor / Bench Press",
                        barbellWeightKg = bilateralLoad
                    )
                }
                titleLower.contains("row") -> {
                    val bilateralLoad = (targetWeightKg * 2.0f).coerceAtLeast(15.0f)
                    return LoadSetupAdvice(
                        title = "Barbell Row Swap Available",
                        details = "Swap to Barbell Bent-Over Row (Prescription: %.1f kg bilateral load)".format(bilateralLoad),
                        equipmentTag = "BARBELL",
                        canSwapBarbell = true,
                        barbellExerciseName = "Barbell Bent-Over Row",
                        barbellWeightKg = bilateralLoad
                    )
                }
                titleLower.contains("curl") -> {
                    val bilateralLoad = (targetWeightKg * 2.0f).coerceAtLeast(10.0f)
                    return LoadSetupAdvice(
                        title = "Barbell Curl Swap Available",
                        details = "Swap to Strict Barbell Bicep Curl (Prescription: %.1f kg bilateral load)".format(bilateralLoad),
                        equipmentTag = "BARBELL",
                        canSwapBarbell = true,
                        barbellExerciseName = "Strict Barbell Bicep Curl",
                        barbellWeightKg = bilateralLoad
                    )
                }
            }
        }

        // 2. Check for Adjustable Dumbbell plate math
        if (inventory.hasAdjustableDumbbell && targetWeightKg > 4.0f) {
            val handleWeight = 1.5f
            val plateWeightPerSide = ((targetWeightKg - handleWeight) / 2.0f).coerceAtLeast(0.5f)
            return LoadSetupAdvice(
                title = "Adjustable Dumbbell Setup",
                details = "Handle (1.5 kg) + %.1f kg plates/side = %.1f kg total".format(plateWeightPerSide, targetWeightKg),
                equipmentTag = "ADJ DUMBBELL"
            )
        }

        // 3. Check for Pull-Up / Table Back Swaps
        if ((inventory.hasPullUpBar || inventory.hasSturdyTableOrBed) && (titleLower.contains("row") || titleLower.contains("back") || titleLower.contains("pull"))) {
            val altText = if (inventory.hasPullUpBar) "Doorway Pull-Ups" else "Under-Table Inverted Bodyweight Rows"
            if (targetWeightKg <= 4.0f) {
                return LoadSetupAdvice(
                    title = "Bodyweight Back Swap",
                    details = "Add/Swap with $altText for lat width & scapular retraction",
                    equipmentTag = "BODYWEIGHT",
                    canSwapBodyweight = true,
                    bodyweightAlternative = altText
                )
            }
        }

        // 4. Case 1: Target Weight > 4.0 kg with only 4kg DB + Weighted Backpack
        if (targetWeightKg > 4.0f && inventory.hasWeightedBackpack) {
            val extraKg = targetWeightKg - 4.0f
            val bottlesCount = extraKg.toInt().coerceAtLeast(1)

            when (category) {
                MovementCategory.LOWER_BODY -> {
                    return LoadSetupAdvice(
                        title = "Backpack Loading: %.1f kg".format(targetWeightKg),
                        details = "Hold 4 kg Dumbbell + Wear Backpack with %d × 1L Water Bottles (+%.1f kg)".format(bottlesCount, extraKg),
                        equipmentTag = "4KG DB + BACKPACK"
                    )
                }
                MovementCategory.LARGE_UPPER -> {
                    return LoadSetupAdvice(
                        title = "Heavy Upper Load: %.1f kg".format(targetWeightKg),
                        details = "Hold 4 kg DB + Grip Backpack Handle (%dL bottles) OR use 4-2-1 Ultra-Slow Tempo".format(bottlesCount),
                        equipmentTag = "4KG DB + BACKPACK"
                    )
                }
                MovementCategory.MEDIUM_UPPER, MovementCategory.SMALL_ISOLATION -> {
                    return LoadSetupAdvice(
                        title = "Isolation Overload: %.1f kg".format(targetWeightKg),
                        details = "Curl/Press Backpack (%.0fL bottles) by top handle OR 4 kg DB w/ 3s Peak Squeeze".format(targetWeightKg),
                        equipmentTag = "BACKPACK / TEMPO"
                    )
                }
                MovementCategory.BODYWEIGHT_CORE -> {
                    return LoadSetupAdvice(
                        title = "Core / Bodyweight Overload",
                        details = "Elevate feet on bed/chair or wear loaded backpack for progressive resistance",
                        equipmentTag = "ELEVATION"
                    )
                }
            }
        }

        // Default: 4kg DB + Strict Cadence
        return LoadSetupAdvice(
            title = "Single 4 kg Dumbbell Setup",
            details = if (targetWeightKg <= 4.0f) "Single 4.0 kg Dumbbell • Focus on 3-1-1 strict eccentric cadence"
            else "4 kg Dumbbell • Increase TUT with 4s eccentric lower to match %.1f kg stimulus".format(targetWeightKg),
            equipmentTag = "4KG DB"
        )
    }
}
