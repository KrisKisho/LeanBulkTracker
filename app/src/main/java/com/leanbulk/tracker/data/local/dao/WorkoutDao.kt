package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_logs WHERE date = :date")
    fun getWorkoutLogsForDate(date: String): Flow<List<DailyWorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs")
    suspend fun getAllWorkoutLogsSync(): List<DailyWorkoutLogEntity>

    @Upsert
    suspend fun upsertWorkoutLog(entity: DailyWorkoutLogEntity)

    @Upsert
    suspend fun upsertAll(entities: List<DailyWorkoutLogEntity>)

    @Query("UPDATE workout_logs SET isCompleted = :isCompleted WHERE date = :date AND segmentId = :segmentId")
    suspend fun updateSegmentCompletion(date: String, segmentId: String, isCompleted: Boolean): Int

    @Query("SELECT DISTINCT date FROM workout_logs WHERE isCompleted = 1")
    fun getCompletedDates(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT date) FROM workout_logs WHERE isCompleted = 1")
    fun getTotalCompletedDaysCount(): Flow<Int>
}
