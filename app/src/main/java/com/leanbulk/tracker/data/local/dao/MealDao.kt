package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.MealTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Query("SELECT * FROM meal_logs WHERE date = :date")
    fun getMealsForDate(date: String): Flow<List<DailyMealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE date = :date")
    suspend fun getMealsForDateSync(date: String): List<DailyMealLogEntity>

    @Query("SELECT * FROM meal_logs")
    suspend fun getAllMealLogsSync(): List<DailyMealLogEntity>

    @Upsert
    suspend fun upsertMeal(entity: DailyMealLogEntity)

    @Upsert
    suspend fun upsertMeals(entities: List<DailyMealLogEntity>)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealById(id: String)

    @Query("UPDATE meal_logs SET isChecked = :isChecked WHERE date = :date AND mealId = :mealId")
    suspend fun updateCheckState(date: String, mealId: String, isChecked: Boolean): Int

    @Query("UPDATE meal_logs SET isVeg = :isVeg, title = :title, calories = :calories, protein = :protein, carbs = :carbs, fat = :fat, ingredientsJson = :ingredientsJson WHERE date = :date AND mealId = :mealId")
    suspend fun updateVegStateWithIngredients(
        date: String,
        mealId: String,
        isVeg: Boolean,
        title: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        ingredientsJson: String
    ): Int

    @Query("UPDATE meal_logs SET title = :title, calories = :calories, protein = :protein, carbs = :carbs, fat = :fat, ingredientsJson = :ingredientsJson WHERE date = :date AND mealId = :mealId")
    suspend fun updateCustomizedMeal(
        date: String,
        mealId: String,
        title: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        ingredientsJson: String
    ): Int

    @Query("SELECT DISTINCT date FROM meal_logs WHERE isChecked = 1")
    fun getDatesWithLogs(): Flow<List<String>>

    // Meal Templates (user's saved custom default recipes)
    @Query("SELECT * FROM meal_templates")
    fun getAllMealTemplates(): Flow<List<MealTemplateEntity>>

    @Query("SELECT * FROM meal_templates")
    suspend fun getAllMealTemplatesSync(): List<MealTemplateEntity>

    @Query("SELECT * FROM meal_templates WHERE mealId = :mealId")
    suspend fun getMealTemplateSync(mealId: String): MealTemplateEntity?

    @Upsert
    suspend fun upsertMealTemplate(template: MealTemplateEntity)

    @Upsert
    suspend fun upsertMealTemplates(templates: List<MealTemplateEntity>)

    @Query("DELETE FROM meal_templates WHERE mealId = :mealId")
    suspend fun deleteMealTemplate(mealId: String)
}
