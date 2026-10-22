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
    val maxWeeklyGainKg: Float = 0.35f
) {
    val startingWeightKg: Float get() = startWeightKg
    val targetWeightKg: Float get() = goalWeightKg
}
