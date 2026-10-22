package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.HabitDao
import com.leanbulk.tracker.data.local.entity.DailyHabitLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val habitDao: HabitDao) {

    fun getTodayHabit(date: LocalDate = LocalDate.now()): Flow<DailyHabitLogEntity?> {
        return habitDao.getHabitLog(date.toString())
    }

    suspend fun incrementWater(date: LocalDate = LocalDate.now()) {
        val dateStr = date.toString()
        val current = habitDao.getHabitLogSync(dateStr)
        val updatedGlasses = ((current?.waterGlasses ?: 0) + 1).coerceAtMost(20)
        val updated = DailyHabitLogEntity(
            date = dateStr,
            waterGlasses = updatedGlasses,
            sleepHours = current?.sleepHours ?: 0.0f
        )
        habitDao.upsertHabitLog(updated)
    }

    suspend fun decrementWater(date: LocalDate = LocalDate.now()) {
        val dateStr = date.toString()
        val current = habitDao.getHabitLogSync(dateStr)
        val updatedGlasses = ((current?.waterGlasses ?: 0) - 1).coerceAtLeast(0)
        val updated = DailyHabitLogEntity(
            date = dateStr,
            waterGlasses = updatedGlasses,
            sleepHours = current?.sleepHours ?: 0.0f
        )
        habitDao.upsertHabitLog(updated)
    }

    suspend fun updateSleep(sleepHours: Float, date: LocalDate = LocalDate.now()) {
        val dateStr = date.toString()
        val current = habitDao.getHabitLogSync(dateStr)
        val updated = DailyHabitLogEntity(
            date = dateStr,
            waterGlasses = current?.waterGlasses ?: 0,
            sleepHours = sleepHours
        )
        habitDao.upsertHabitLog(updated)
    }
}
