package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leanbulk.tracker.data.local.entity.SupplementLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplement_logs WHERE date = :date LIMIT 1")
    fun getSupplementLog(date: String): Flow<SupplementLogEntity?>

    @Query("SELECT * FROM supplement_logs WHERE date = :date LIMIT 1")
    suspend fun getSupplementLogSync(date: String): SupplementLogEntity?

    @Query("SELECT * FROM supplement_logs ORDER BY date DESC")
    suspend fun getAllSupplementLogsSync(): List<SupplementLogEntity>

    @Query("SELECT * FROM supplement_logs ORDER BY date DESC")
    fun getAllSupplementLogs(): Flow<List<SupplementLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: SupplementLogEntity)

    @Query("DELETE FROM supplement_logs WHERE date = :date")
    suspend fun deleteSupplementLog(date: String)
}
