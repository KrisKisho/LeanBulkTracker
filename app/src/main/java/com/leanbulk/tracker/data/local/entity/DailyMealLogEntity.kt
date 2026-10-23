package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class DailyMealLogEntity(
    val date: String,
    val mealId: String,
    @PrimaryKey
    val id: String = "${date}_${mealId}",
    val mealName: String,
    val title: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val isVeg: Boolean,
    val isChecked: Boolean,
    val isCustom: Boolean,
    val ingredientsJson: String = ""
)
