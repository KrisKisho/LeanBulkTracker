package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.ReminderDao
import com.leanbulk.tracker.data.local.entity.ReminderEntity
import com.leanbulk.tracker.data.model.DefaultReminders
import com.leanbulk.tracker.data.model.ReminderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    fun getAllReminders(): Flow<List<ReminderItem>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { entity ->
                ReminderItem(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    hour = entity.hour,
                    minute = entity.minute,
                    isEnabled = entity.isEnabled,
                    isWeeklyOnly = entity.isWeeklyOnly,
                    weeklyDayOfWeek = entity.weeklyDayOfWeek
                )
            }
        }
    }

    suspend fun ensureDefaultRemindersSeeded() {
        val existing = reminderDao.getActiveReminders()
        val allFlow = reminderDao.getAllReminders()
        // Check if DB is completely empty
        val defaults = DefaultReminders.getList().map { item ->
            ReminderEntity(
                id = item.id,
                title = item.title,
                description = item.description,
                hour = item.hour,
                minute = item.minute,
                isEnabled = item.isEnabled,
                isWeeklyOnly = item.isWeeklyOnly,
                weeklyDayOfWeek = item.weeklyDayOfWeek
            )
        }
        reminderDao.upsertAll(defaults)
    }

    suspend fun updateReminderEnabled(id: Int, isEnabled: Boolean) {
        reminderDao.updateReminderEnabled(id, isEnabled)
    }

    suspend fun updateReminderTime(id: Int, hour: Int, minute: Int) {
        reminderDao.updateReminderTime(id, hour, minute)
    }

    suspend fun getActiveReminders(): List<ReminderItem> {
        return reminderDao.getActiveReminders().map { entity ->
            ReminderItem(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                hour = entity.hour,
                minute = entity.minute,
                isEnabled = entity.isEnabled,
                isWeeklyOnly = entity.isWeeklyOnly,
                weeklyDayOfWeek = entity.weeklyDayOfWeek
            )
        }
    }
}
