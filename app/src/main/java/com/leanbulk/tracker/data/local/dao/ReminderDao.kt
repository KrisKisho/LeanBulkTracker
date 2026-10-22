package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    suspend fun getAllRemindersSync(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Upsert
    suspend fun upsertReminder(entity: ReminderEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ReminderEntity>)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateReminderEnabled(id: Int, isEnabled: Boolean)

    @Query("UPDATE reminders SET hour = :hour, minute = :minute WHERE id = :id")
    suspend fun updateReminderTime(id: Int, hour: Int, minute: Int)
}
