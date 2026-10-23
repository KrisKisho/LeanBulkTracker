package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class DailyWorkoutLogEntity(
    val date: String,
    val segmentId: String,
    @PrimaryKey
    val id: String = "${date}_${segmentId}",
    val dayOfWeek: String,
    val isCompleted: Boolean,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val weightUsedKg: Float = 4.0f,
    val repsPerSet: Int = 12,
    val set3RepsAchieved: Int = 10,
    val effortRating: String = "Good Burn", // "Easy", "Good Burn", "Near Failure"
    val progressionWeek: Int = 1 // 1 to 4
) {
    @get:Ignore
    val completed: Boolean
        get() = isCompleted
}

typealias WorkoutLogEntity = DailyWorkoutLogEntity
