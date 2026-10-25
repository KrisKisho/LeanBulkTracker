package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplement_logs")
data class SupplementLogEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val wheyLogged: Boolean = false,
    val creatineGramsLogged: Float = 0f, // 3.0f when WHEY_WITH_CREATINE; 0.0f when WHEY_ONLY_NO_CREATINE
    val vitD3B12K2Logged: Boolean = false,
    val fishOilLogged: Boolean = false,
    val magnesiumGlycinateLogged: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)
