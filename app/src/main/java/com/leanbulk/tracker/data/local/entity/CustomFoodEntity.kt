package com.leanbulk.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leanbulk.tracker.data.model.FoodCategory
import com.leanbulk.tracker.data.model.FoodItem

@Entity(tableName = "custom_foods")
data class CustomFoodEntity(
    @PrimaryKey
    val id: String = "custom_${System.currentTimeMillis()}",
    val name: String,
    val teluguOrRegionalHint: String = "",
    val category: String = FoodCategory.CUSTOM.name,
    val servingUnit: String = "1 serving",
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val isVegetarian: Boolean = true,
    val isHighProteinPatch: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toFoodItem(): FoodItem {
        val cat = try {
            FoodCategory.valueOf(category)
        } catch (e: Exception) {
            FoodCategory.CUSTOM
        }
        return FoodItem(
            id = id,
            name = name,
            teluguOrRegionalHint = teluguOrRegionalHint,
            category = cat,
            servingUnit = servingUnit,
            calories = calories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            isVegetarian = isVegetarian,
            isHighProteinPatch = isHighProteinPatch
        )
    }

    companion object {
        fun fromFoodItem(item: FoodItem): CustomFoodEntity {
            return CustomFoodEntity(
                id = item.id,
                name = item.name,
                teluguOrRegionalHint = item.teluguOrRegionalHint,
                category = item.category.name,
                servingUnit = item.servingUnit,
                calories = item.calories,
                proteinGrams = item.proteinGrams,
                carbsGrams = item.carbsGrams,
                fatGrams = item.fatGrams,
                isVegetarian = item.isVegetarian,
                isHighProteinPatch = item.isHighProteinPatch
            )
        }
    }
}
