package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdaptationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AdaptationLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AdaptationLogEntity>)

    @Query("SELECT * FROM adaptation_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<AdaptationLogEntity>>

    @Query("SELECT * FROM adaptation_logs ORDER BY id DESC")
    suspend fun getAllLogsSync(): List<AdaptationLogEntity>

    @Query("SELECT * FROM adaptation_logs ORDER BY id DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<AdaptationLogEntity>>

    @Query("DELETE FROM adaptation_logs")
    suspend fun clearAll()
}
