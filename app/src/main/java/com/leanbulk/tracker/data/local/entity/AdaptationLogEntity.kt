package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adaptation_logs")
data class AdaptationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val category: String, // NUTRITION, WORKOUT_SCHEDULE, OVERLOAD, CHRONOTYPE, WAIST_GUARDRAIL
    val title: String,
    val reason: String,
    val parameterChanged: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
