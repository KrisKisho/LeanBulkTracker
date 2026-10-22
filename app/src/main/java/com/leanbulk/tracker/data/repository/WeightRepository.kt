package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.WeightDao
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity
import com.leanbulk.tracker.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WeightRepository(
    private val weightDao: WeightDao
) {
    fun getAllEntries(): Flow<List<WeightEntry>> {
        return weightDao.getAllWeightLogs().map { entities ->
            entities.map { entity ->
                WeightEntry(
                    id = entity.id,
                    date = LocalDate.parse(entity.date),
                    weightKg = entity.weightKg,
                    waistInches = entity.waistInches,
                    notes = entity.notes
                )
            }
        }
    }

    suspend fun logWeight(date: LocalDate, weightKg: Float, waistInches: Float, notes: String = "") {
        val dateStr = date.toString()
        val entity = WeightProgressEntity(
            date = dateStr,
            id = dateStr,
            weightKg = weightKg,
            waistInches = waistInches,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        weightDao.upsertWeightLog(entity)
    }

    suspend fun deleteEntry(id: String) {
        weightDao.deleteWeightLog(id)
    }

    suspend fun removeSampleData() {
        weightDao.deleteSampleWeightLogs()
    }
}
