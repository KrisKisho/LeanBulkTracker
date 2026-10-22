package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leanbulk.tracker.data.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
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
    val equipment: String = "Single 4 kg Dumbbell + Bodyweight"
) {
    fun toModel(): UserProfile = UserProfile(
        id = id,
        age = age,
        sex = sex,
        heightCm = heightCm,
        startWeightKg = startWeightKg,
        currentWeightKg = currentWeightKg,
        goalWeightKg = goalWeightKg,
        targetWeeklyGainKg = targetWeeklyGainKg,
        wristInches = wristInches,
        ankleInches = ankleInches,
        currentWaistInches = currentWaistInches,
        currentChestInches = currentChestInches,
        currentArmInches = currentArmInches,
        currentForearmInches = currentForearmInches,
        currentNeckInches = currentNeckInches,
        currentThighInches = currentThighInches,
        currentCalfInches = currentCalfInches,
        equipment = equipment
    )

    companion object {
        fun fromModel(model: UserProfile): UserProfileEntity = UserProfileEntity(
            id = model.id,
            age = model.age,
            sex = model.sex,
            heightCm = model.heightCm,
            startWeightKg = model.startWeightKg,
            currentWeightKg = model.currentWeightKg,
            goalWeightKg = model.goalWeightKg,
            targetWeeklyGainKg = model.targetWeeklyGainKg,
            wristInches = model.wristInches,
            ankleInches = model.ankleInches,
            currentWaistInches = model.currentWaistInches,
            currentChestInches = model.currentChestInches,
            currentArmInches = model.currentArmInches,
            currentForearmInches = model.currentForearmInches,
            currentNeckInches = model.currentNeckInches,
            currentThighInches = model.currentThighInches,
            currentCalfInches = model.currentCalfInches,
            equipment = model.equipment
        )
    }
}
