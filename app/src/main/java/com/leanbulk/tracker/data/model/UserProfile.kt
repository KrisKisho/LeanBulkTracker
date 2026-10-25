package com.leanbulk.tracker.data.model

data class UserProfile(
    val id: Int = 1,
    val age: Int = 41,
    val sex: String = "Male",
    val heightCm: Float = 166f,
    val startWeightKg: Float = 60.0f,
    val currentWeightKg: Float = 60.0f,
    val goalWeightKg: Float = 70.0f,
    val targetWeeklyGainKg: Float = 0.30f,
    val wristInches: Float = 6.1f,
    val ankleInches: Float = 7.81f,
    val currentWaistInches: Float = 30.0f,
    val currentChestInches: Float = 35.5f,
    val currentArmInches: Float = 12.0f,
    val currentForearmInches: Float = 10.2f,
    val currentNeckInches: Float = 13.8f,
    val currentThighInches: Float = 19.5f,
    val currentCalfInches: Float = 13.0f,
    val startWaistInches: Float = 30.0f,
    val startChestInches: Float = 35.5f,
    val startArmInches: Float = 12.0f,
    val startForearmInches: Float = 10.2f,
    val startNeckInches: Float = 13.8f,
    val startThighInches: Float = 19.5f,
    val startCalfInches: Float = 13.0f,
    val equipment: String = "Single 4 kg Dumbbell + Bodyweight",
    val dailyCaloriesTarget: Int = 2420,
    val dailyProteinTargetG: Int = 135,
    val dailyCarbsTargetG: Int = 345,
    val dailyFatTargetG: Int = 62,
    val minWeeklyGainKg: Float = 0.25f,
    val maxWeeklyGainKg: Float = 0.35f,
    // Autonomous Adaptive Coaching State
    val autoAdaptEnabled: Boolean = true,
    val adaptiveCalorieOffset: Int = 0,
    val splitShakeModeActive: Boolean = false,
    val dailyStepGoal: Int = 6000,
    val workoutStartTimeHour: Int = 19,
    val workoutStartTimeMinute: Int = 30,
    val currentRollingSplitIndex: Int = 0,
    val lastAdaptationEvalDate: String = "",
    // Supplement Stack & Creatine Tracker
    val wheyCreatineMode: String = "WHEY_WITH_CREATINE",
    val creatineSaturationPercent: Float = 0f,
    val d3Supplement1Iu: Int = 600,
    val d3Supplement2Iu: Int = 400,
    // Home Gym Equipment Inventory
    val eqHas4kgDumbbell: Boolean = true,
    val eqHasWeightedBackpack: Boolean = true,
    val eqHasSturdyTableOrBed: Boolean = true,
    val eqHasAdjustableDumbbell: Boolean = false,
    val eqHasBarbell: Boolean = false,
    val eqHasPullUpBar: Boolean = false,
    val eqHasResistanceBand: Boolean = false
) {
    val startingWeightKg: Float get() = startWeightKg
    val targetWeightKg: Float get() = goalWeightKg

    /**
     * Waist to Height Ratio (WHtR).
     * Values > 0.48 indicate increased abdominal visceral adiposity requiring guardrail trim.
     */
    val whtr: Float
        get() = (currentWaistInches * 2.54f) / heightCm.coerceAtLeast(100f)

    val effectiveBaseCalories: Int
        get() = dailyCaloriesTarget + adaptiveCalorieOffset
}
