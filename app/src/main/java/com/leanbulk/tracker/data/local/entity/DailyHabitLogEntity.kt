package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habit_logs")
data class DailyHabitLogEntity(
    @PrimaryKey
    val date: String, // "YYYY-MM-DD"
    val waterGlasses: Int = 0, // Target: 10 glasses (~3.0L)
    val sleepHours: Float = 0.0f // Target: 7.5+ hrs
)
