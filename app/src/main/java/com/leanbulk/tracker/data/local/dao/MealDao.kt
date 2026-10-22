package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
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

    @Query("UPDATE meal_logs SET isVeg = :isVeg, title = :title, calories = :calories, protein = :protein, carbs = :carbs, fat = :fat WHERE date = :date AND mealId = :mealId")
    suspend fun updateVegState(
        date: String,
        mealId: String,
        isVeg: Boolean,
        title: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int
    ): Int

    @Query("SELECT DISTINCT date FROM meal_logs WHERE isChecked = 1")
    fun getDatesWithLogs(): Flow<List<String>>
}
