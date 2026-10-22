package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.MealDao
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.model.MealDataSource
import com.leanbulk.tracker.data.model.MealItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class MealRepository(
    private val mealDao: MealDao
) {
    fun getMealsForDate(date: LocalDate): Flow<List<MealItem>> {
        val dateStr = date.toString()
        val defaultMap = MealDataSource.getDefaultMeals().associateBy { it.id }

        return mealDao.getMealsForDate(dateStr).map { entities ->
            entities.map { entity ->
                val defaultRef = defaultMap[entity.mealId]
                MealItem(
                    id = entity.mealId,
                    timeLabel = defaultRef?.timeLabel ?: "Custom",
                    mealName = entity.mealName,
                    defaultTitle = defaultRef?.defaultTitle ?: entity.title,
                    defaultCalories = defaultRef?.defaultCalories ?: entity.calories,
                    defaultProtein = defaultRef?.defaultProtein ?: entity.protein,
                    defaultCarbs = defaultRef?.defaultCarbs ?: entity.carbs,
                    defaultFat = defaultRef?.defaultFat ?: entity.fat,
                    hasVegOption = defaultRef?.hasVegOption ?: false,
                    vegTitle = defaultRef?.vegTitle ?: "",
                    vegCalories = defaultRef?.vegCalories ?: entity.calories,
                    vegProtein = defaultRef?.vegProtein ?: entity.protein,
                    vegCarbs = defaultRef?.vegCarbs ?: entity.carbs,
                    vegFat = defaultRef?.vegFat ?: entity.fat,
                    isVeg = entity.isVeg,
                    isChecked = entity.isChecked,
                    isCustom = entity.isCustom
                )
            }
        }
    }

    suspend fun ensureDefaultMealsSeeded(date: LocalDate) {
        val dateStr = date.toString()
        val existing = mealDao.getMealsForDateSync(dateStr)
        if (existing.isEmpty()) {
            val defaults = MealDataSource.getDefaultMeals().map { item ->
                DailyMealLogEntity(
                    date = dateStr,
                    mealId = item.id,
                    id = "${dateStr}_${item.id}",
                    mealName = item.mealName,
                    title = item.defaultTitle,
                    calories = item.defaultCalories,
                    protein = item.defaultProtein,
                    carbs = item.defaultCarbs,
                    fat = item.defaultFat,
                    isVeg = false,
                    isChecked = false,
                    isCustom = false
                )
            }
            mealDao.upsertMeals(defaults)
        }
    }

    suspend fun toggleMealChecked(date: LocalDate, meal: MealItem, isChecked: Boolean) {
        val dateStr = date.toString()
        val rows = mealDao.updateCheckState(dateStr, meal.id, isChecked)
        if (rows == 0) {
            val entity = DailyMealLogEntity(
                date = dateStr,
                mealId = meal.id,
                id = "${dateStr}_${meal.id}",
                mealName = meal.mealName,
                title = meal.activeTitle,
                calories = meal.activeCalories,
                protein = meal.activeProtein,
                carbs = meal.activeCarbs,
                fat = meal.activeFat,
                isVeg = meal.isVeg,
                isChecked = isChecked,
                isCustom = meal.isCustom
            )
            mealDao.upsertMeal(entity)
        }
    }

    suspend fun toggleVegOption(date: LocalDate, meal: MealItem, isVeg: Boolean) {
        val dateStr = date.toString()
        val updatedMeal = meal.copy(isVeg = isVeg)
        val rows = mealDao.updateVegState(
            date = dateStr,
            mealId = meal.id,
            isVeg = isVeg,
            title = updatedMeal.activeTitle,
            calories = updatedMeal.activeCalories,
            protein = updatedMeal.activeProtein,
            carbs = updatedMeal.activeCarbs,
            fat = updatedMeal.activeFat
        )
        if (rows == 0) {
            val entity = DailyMealLogEntity(
                date = dateStr,
                mealId = meal.id,
                id = "${dateStr}_${meal.id}",
                mealName = meal.mealName,
                title = updatedMeal.activeTitle,
                calories = updatedMeal.activeCalories,
                protein = updatedMeal.activeProtein,
                carbs = updatedMeal.activeCarbs,
                fat = updatedMeal.activeFat,
                isVeg = isVeg,
                isChecked = meal.isChecked,
                isCustom = meal.isCustom
            )
            mealDao.upsertMeal(entity)
        }
    }

    suspend fun addCustomMeal(
        date: LocalDate,
        name: String,
        calories: Int,
        protein: Int,
        carbs: Int = 0,
        fat: Int = 0
    ) {
        val dateStr = date.toString()
        val customId = "custom_${UUID.randomUUID()}"
        val entity = DailyMealLogEntity(
            date = dateStr,
            mealId = customId,
            id = "${dateStr}_${customId}",
            mealName = name,
            title = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            isVeg = true,
            isChecked = true,
            isCustom = true
        )
        mealDao.upsertMeal(entity)
    }

    suspend fun deleteCustomMeal(date: LocalDate, mealId: String) {
        val existing = mealDao.getMealsForDateSync(date.toString())
        val item = existing.find { it.mealId == mealId && it.isCustom }
        if (item != null) {
            mealDao.deleteMealById(item.id)
        }
    }
}
