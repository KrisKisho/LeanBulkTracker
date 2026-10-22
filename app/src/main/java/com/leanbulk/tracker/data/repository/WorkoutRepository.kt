package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.WorkoutDao
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.model.DayWorkoutPlan
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.model.WorkoutSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate

class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {
    fun getPlanWithStatus(date: LocalDate, dayOfWeek: DayOfWeek): Flow<DayWorkoutPlan> {
        val basePlan = WorkoutDataSource.getPlanForDay(dayOfWeek)
        val dateStr = date.toString()

        return workoutDao.getWorkoutLogsForDate(dateStr).map { logs ->
            val logMap = logs.associateBy { it.segmentId }
            val updatedSegments = basePlan.segments.map { segment ->
                val log = logMap[segment.id]
                segment.copy(isCompleted = log?.isCompleted ?: false)
            }
            basePlan.copy(segments = updatedSegments)
        }
    }

    suspend fun toggleSegmentCompletion(date: LocalDate, segment: WorkoutSegment, isCompleted: Boolean) {
        val dateStr = date.toString()
        val rows = workoutDao.updateSegmentCompletion(dateStr, segment.id, isCompleted)
        if (rows == 0) {
            val entity = DailyWorkoutLogEntity(
                date = dateStr,
                segmentId = segment.id,
                id = "${dateStr}_${segment.id}",
                dayOfWeek = segment.dayOfWeek.name,
                isCompleted = isCompleted,
                completedAtMillis = System.currentTimeMillis()
            )
            workoutDao.upsertWorkoutLog(entity)
        }
    }

    fun getCompletedDates(): Flow<List<String>> = workoutDao.getCompletedDates()

    fun getStreakFlow(): Flow<Int> {
        return workoutDao.getCompletedDates().map { completedDateStrings ->
            if (completedDateStrings.isEmpty()) return@map 0
            val sortedDates = completedDateStrings.mapNotNull {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }.distinct().sortedDescending()

            if (sortedDates.isEmpty()) return@map 0

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val latest = sortedDates.first()
            if (latest != today && latest != yesterday) {
                return@map 0
            }

            var streak = 1
            var prev = latest
            for (i in 1 until sortedDates.size) {
                val current = sortedDates[i]
                if (current == prev.minusDays(1)) {
                    streak++
                    prev = current
                } else if (current == prev) {
                    continue
                } else {
                    break
                }
            }
            streak
        }
    }
}
