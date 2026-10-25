package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leanbulk.tracker.data.local.entity.SavedMealComboEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealComboDao {

    @Query("SELECT * FROM saved_meal_combos ORDER BY createdAtMillis DESC")
    fun getAllSavedCombos(): Flow<List<SavedMealComboEntity>>

    @Query("SELECT * FROM saved_meal_combos ORDER BY createdAtMillis DESC")
    suspend fun getAllSavedCombosSync(): List<SavedMealComboEntity>

    @Query("SELECT * FROM saved_meal_combos WHERE targetMealSlotId = :slotId OR targetMealSlotId = '' ORDER BY createdAtMillis DESC")
    fun getCombosForSlot(slotId: String): Flow<List<SavedMealComboEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombo(combo: SavedMealComboEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCombos(combos: List<SavedMealComboEntity>)

    @Query("DELETE FROM saved_meal_combos WHERE id = :id")
    suspend fun deleteCombo(id: String)

    @Query("DELETE FROM saved_meal_combos")
    suspend fun clearAll()
}
