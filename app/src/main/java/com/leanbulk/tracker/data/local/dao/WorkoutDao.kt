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

    @Query("SELECT * FROM workout_logs WHERE date = :date")
    suspend fun getWorkoutLogsForDateSync(date: String): List<DailyWorkoutLogEntity>

    @Query("SELECT * FROM workout_logs")
    suspend fun getAllWorkoutLogsSync(): List<DailyWorkoutLogEntity>

    @Upsert
    suspend fun upsertWorkoutLog(entity: DailyWorkoutLogEntity)

    @Upsert
    suspend fun upsertAll(entities: List<DailyWorkoutLogEntity>)

    @Query("UPDATE workout_logs SET isCompleted = :isCompleted WHERE date = :date AND segmentId = :segmentId")
    suspend fun updateSegmentCompletion(date: String, segmentId: String, isCompleted: Boolean): Int

    @Query("UPDATE workout_logs SET weightUsedKg = :weight, repsPerSet = :reps, effortRating = :effort WHERE date = :date AND segmentId = :segmentId")
    suspend fun updateOverload(date: String, segmentId: String, weight: Float, reps: Int, effort: String): Int

    @Query("SELECT * FROM workout_logs WHERE segmentId = :segmentId AND isCompleted = 1 ORDER BY date DESC, completedAtMillis DESC LIMIT 1")
    suspend fun getLastCompletedLogForSegment(segmentId: String): DailyWorkoutLogEntity?

    @Query("SELECT * FROM workout_logs WHERE segmentId = :segmentId AND isCompleted = 1 ORDER BY date DESC, completedAtMillis DESC LIMIT 3")
    suspend fun getLastThreeLogsForSegment(segmentId: String): List<DailyWorkoutLogEntity>

    @Query("SELECT DISTINCT date FROM workout_logs WHERE isCompleted = 1")
    fun getCompletedDates(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT date) FROM workout_logs WHERE isCompleted = 1")
    fun getTotalCompletedDaysCount(): Flow<Int>
}
