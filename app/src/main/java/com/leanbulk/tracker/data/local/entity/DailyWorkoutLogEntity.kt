package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
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
    val effortRating: String = "Good Burn" // "Easy", "Good Burn", "Near Failure"
)
