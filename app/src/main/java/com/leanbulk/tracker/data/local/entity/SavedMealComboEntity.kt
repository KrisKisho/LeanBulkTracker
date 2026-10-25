package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leanbulk.tracker.data.model.IngredientJsonHelper
import com.leanbulk.tracker.data.model.MealIngredient

@Entity(tableName = "saved_meal_combos")
data class SavedMealComboEntity(
    @PrimaryKey
    val id: String = "combo_${System.currentTimeMillis()}",
    val name: String,
    val targetMealSlotId: String = "",
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val isVegetarian: Boolean,
    val itemsJson: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun parseIngredients(): List<MealIngredient> {
        return if (itemsJson.isNotBlank()) {
            IngredientJsonHelper.fromJsonString(itemsJson)
        } else {
            emptyList()
        }
    }
}
