package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_templates")
data class MealTemplateEntity(
    @PrimaryKey
    val mealId: String,
    val mealName: String,
    val title: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val isVeg: Boolean,
    val ingredientsJson: String
)
