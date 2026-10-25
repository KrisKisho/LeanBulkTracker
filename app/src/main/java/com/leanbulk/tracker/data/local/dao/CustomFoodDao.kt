package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leanbulk.tracker.data.local.entity.CustomFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFoodDao {

    @Query("SELECT * FROM custom_foods ORDER BY createdAtMillis DESC")
    fun getAllCustomFoods(): Flow<List<CustomFoodEntity>>

    @Query("SELECT * FROM custom_foods ORDER BY createdAtMillis DESC")
    suspend fun getAllCustomFoodsSync(): List<CustomFoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomFood(food: CustomFoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomFoods(foods: List<CustomFoodEntity>)

    @Query("DELETE FROM custom_foods WHERE id = :id")
    suspend fun deleteCustomFood(id: String)

    @Query("DELETE FROM custom_foods")
    suspend fun clearAll()
}
