package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Query("SELECT * FROM weight_progress ORDER BY date ASC")
    fun getAllWeightLogs(): Flow<List<WeightProgressEntity>>

    @Query("SELECT * FROM weight_progress ORDER BY date ASC")
    suspend fun getAllWeightLogsSync(): List<WeightProgressEntity>

    @Query("SELECT * FROM weight_progress WHERE date = :date LIMIT 1")
    suspend fun getWeightLogByDate(date: String): WeightProgressEntity?

    @Upsert
    suspend fun upsertWeightLog(entity: WeightProgressEntity)

    @Upsert
    suspend fun upsertAll(entities: List<WeightProgressEntity>)

    @Query("DELETE FROM weight_progress WHERE id = :id")
    suspend fun deleteWeightLog(id: String)
}
