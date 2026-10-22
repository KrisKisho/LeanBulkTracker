package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_progress")
data class WeightProgressEntity(
    val date: String,
    @PrimaryKey
    val id: String = date,
    val weightKg: Float,
    val waistInches: Float,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
