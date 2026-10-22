package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val isWeeklyOnly: Boolean,
    val weeklyDayOfWeek: Int
)
