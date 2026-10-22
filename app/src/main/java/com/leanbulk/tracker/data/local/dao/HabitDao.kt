package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leanbulk.tracker.data.local.entity.DailyHabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM daily_habit_logs WHERE date = :date")
    fun getHabitLog(date: String): Flow<DailyHabitLogEntity?>

    @Query("SELECT * FROM daily_habit_logs WHERE date = :date")
    suspend fun getHabitLogSync(date: String): DailyHabitLogEntity?

    @Query("SELECT * FROM daily_habit_logs")
    suspend fun getAllHabitLogsSync(): List<DailyHabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabitLog(log: DailyHabitLogEntity)

    @Query("DELETE FROM daily_habit_logs")
    suspend fun deleteAllHabits()
}
