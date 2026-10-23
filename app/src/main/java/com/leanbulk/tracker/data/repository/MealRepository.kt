package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.MealDao
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.MealTemplateEntity
import com.leanbulk.tracker.data.model.FoodCatalog
import com.leanbulk.tracker.data.model.IngredientJsonHelper
import com.leanbulk.tracker.data.model.MealDataSource
import com.leanbulk.tracker.data.model.MealIngredient
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

                // Parse customized ingredients if available; otherwise initialize from default catalog
                val ingredients = if (entity.ingredientsJson.isNotBlank()) {
                    IngredientJsonHelper.fromJsonString(entity.ingredientsJson)
                } else if (defaultRef != null) {
                    FoodCatalog.getDefaultIngredientsForMeal(entity.mealId, entity.isVeg)
                } else {
                    emptyList()
                }

                MealItem(
                    id = entity.mealId,
                    timeLabel = defaultRef?.timeLabel ?: "Custom",
                    mealName = entity.mealName,
                    defaultTitle = entity.title.ifBlank { defaultRef?.defaultTitle ?: "Meal" },
                    defaultCalories = if (entity.calories > 0) entity.calories else (defaultRef?.defaultCalories ?: 0),
                    defaultProtein = if (entity.protein > 0) entity.protein else (defaultRef?.defaultProtein ?: 0),
                    defaultCarbs = entity.carbs,
                    defaultFat = entity.fat,
                    hasVegOption = defaultRef?.hasVegOption ?: false,
                    vegTitle = defaultRef?.vegTitle ?: "",
                    vegCalories = defaultRef?.vegCalories ?: entity.calories,
                    vegProtein = defaultRef?.vegProtein ?: entity.protein,
                    vegCarbs = defaultRef?.vegCarbs ?: entity.carbs,
                    vegFat = defaultRef?.vegFat ?: entity.fat,
                    isVeg = entity.isVeg,
                    isChecked = entity.isChecked,
                    isCustom = entity.isCustom,
                    ingredients = ingredients,
                    ingredientsJson = if (entity.ingredientsJson.isNotBlank()) entity.ingredientsJson else IngredientJsonHelper.toJsonString(ingredients)
                )
            }
        }
    }

    suspend fun ensureDefaultMealsSeeded(date: LocalDate) {
        val dateStr = date.toString()
        val existing = mealDao.getMealsForDateSync(dateStr)
        if (existing.isEmpty()) {
            val templates = mealDao.getAllMealTemplatesSync().associateBy { it.mealId }
            val defaults = MealDataSource.getDefaultMeals().map { item ->
                val template = templates[item.id]
                if (template != null) {
                    DailyMealLogEntity(
                        date = dateStr,
                        mealId = item.id,
                        id = "${dateStr}_${item.id}",
                        mealName = item.mealName,
                        title = template.title,
                        calories = template.calories,
                        protein = template.protein,
                        carbs = template.carbs,
                        fat = template.fat,
                        isVeg = template.isVeg,
                        isChecked = false,
                        isCustom = false,
                        ingredientsJson = template.ingredientsJson
                    )
                } else {
                    val initialIngredients = FoodCatalog.getDefaultIngredientsForMeal(item.id, false)
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
                        isCustom = false,
                        ingredientsJson = IngredientJsonHelper.toJsonString(initialIngredients)
                    )
                }
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
                isCustom = meal.isCustom,
                ingredientsJson = meal.ingredientsJson
            )
            mealDao.upsertMeal(entity)
        }
    }

    suspend fun toggleVegOption(date: LocalDate, meal: MealItem, isVeg: Boolean) {
        val dateStr = date.toString()
        val newIngredients = FoodCatalog.getDefaultIngredientsForMeal(meal.id, isVeg)
        val newTitle = if (newIngredients.isNotEmpty()) FoodCatalog.buildSummaryTitle(newIngredients) else (if (isVeg) meal.vegTitle else meal.defaultTitle)
        val newCalories = if (newIngredients.isNotEmpty()) newIngredients.sumOf { it.calories } else (if (isVeg) meal.vegCalories else meal.defaultCalories)
        val newProtein = if (newIngredients.isNotEmpty()) newIngredients.sumOf { it.protein.toDouble() }.toInt() else (if (isVeg) meal.vegProtein else meal.defaultProtein)
        val newCarbs = if (newIngredients.isNotEmpty()) newIngredients.sumOf { it.carbs.toDouble() }.toInt() else (if (isVeg) meal.vegCarbs else meal.defaultCarbs)
        val newFat = if (newIngredients.isNotEmpty()) newIngredients.sumOf { it.fat.toDouble() }.toInt() else (if (isVeg) meal.vegFat else meal.defaultFat)
        val ingredientsJson = IngredientJsonHelper.toJsonString(newIngredients)

        val rows = mealDao.updateVegStateWithIngredients(
            date = dateStr,
            mealId = meal.id,
            isVeg = isVeg,
            title = newTitle,
            calories = newCalories,
            protein = newProtein,
            carbs = newCarbs,
            fat = newFat,
            ingredientsJson = ingredientsJson
        )
        if (rows == 0) {
            val entity = DailyMealLogEntity(
                date = dateStr,
                mealId = meal.id,
                id = "${dateStr}_${meal.id}",
                mealName = meal.mealName,
                title = newTitle,
                calories = newCalories,
                protein = newProtein,
                carbs = newCarbs,
                fat = newFat,
                isVeg = isVeg,
                isChecked = meal.isChecked,
                isCustom = meal.isCustom,
                ingredientsJson = ingredientsJson
            )
            mealDao.upsertMeal(entity)
        }
    }

    suspend fun customizeMeal(
        date: LocalDate,
        mealId: String,
        ingredients: List<MealIngredient>,
        saveAsTemplate: Boolean
    ) {
        val dateStr = date.toString()
        val title = FoodCatalog.buildSummaryTitle(ingredients)
        val calories = ingredients.sumOf { it.calories }
        val protein = ingredients.sumOf { it.protein.toDouble() }.toInt()
        val carbs = ingredients.sumOf { it.carbs.toDouble() }.toInt()
        val fat = ingredients.sumOf { it.fat.toDouble() }.toInt()
        val ingredientsJson = IngredientJsonHelper.toJsonString(ingredients)
        val isVeg = ingredients.all { it.isVeg }

        val rows = mealDao.updateCustomizedMeal(
            date = dateStr,
            mealId = mealId,
            title = title,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            ingredientsJson = ingredientsJson
        )

        // Find existing to get mealName if needed
        val existing = mealDao.getMealsForDateSync(dateStr).find { it.mealId == mealId }
        val mealName = existing?.mealName ?: MealDataSource.getDefaultMeals().find { it.id == mealId }?.mealName ?: "Meal"

        if (rows == 0 && existing == null) {
            val entity = DailyMealLogEntity(
                date = dateStr,
                mealId = mealId,
                id = "${dateStr}_${mealId}",
                mealName = mealName,
                title = title,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                isVeg = isVeg,
                isChecked = false,
                isCustom = false,
                ingredientsJson = ingredientsJson
            )
            mealDao.upsertMeal(entity)
        }

        if (saveAsTemplate) {
            val template = MealTemplateEntity(
                mealId = mealId,
                mealName = mealName,
                title = title,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                isVeg = isVeg,
                ingredientsJson = ingredientsJson
            )
            mealDao.upsertMealTemplate(template)
        }
    }

    suspend fun resetMealToDefault(date: LocalDate, mealId: String) {
        val dateStr = date.toString()
        val defaultRef = MealDataSource.getDefaultMeals().find { it.id == mealId }
        if (defaultRef != null) {
            val defaultIngredients = FoodCatalog.getDefaultIngredientsForMeal(mealId, false)
            val json = IngredientJsonHelper.toJsonString(defaultIngredients)
            mealDao.updateVegStateWithIngredients(
                date = dateStr,
                mealId = mealId,
                isVeg = false,
                title = defaultRef.defaultTitle,
                calories = defaultRef.defaultCalories,
                protein = defaultRef.defaultProtein,
                carbs = defaultRef.defaultCarbs,
                fat = defaultRef.defaultFat,
                ingredientsJson = json
            )
            // Also remove template if user wants to reset their permanent baseline
            mealDao.deleteMealTemplate(mealId)
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
        val singleIngredient = MealIngredient(
            foodItemId = "custom_${UUID.randomUUID()}",
            name = name,
            quantity = 1f,
            unit = "portion",
            calories = calories,
            protein = protein.toFloat(),
            carbs = carbs.toFloat(),
            fat = fat.toFloat(),
            category = com.leanbulk.tracker.data.model.FoodCategory.CUSTOM,
            isVeg = true
        )
        val ingredientsJson = IngredientJsonHelper.toJsonString(listOf(singleIngredient))

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
            isCustom = true,
            ingredientsJson = ingredientsJson
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
