package com.leanbulk.tracker.data.model

import java.time.DayOfWeek

data class WorkoutSegment(
    val id: String,
    val dayOfWeek: DayOfWeek,
    val orderIndex: Int,
    val title: String,
    val videoId: String,
    val startSeconds: Float,
    val endSeconds: Float,
    val note: String = "",
    val isCompleted: Boolean = false,
    val weightUsedKg: Float = 4.0f,
    val repsPerSet: Int = 12,
    val set3RepsAchieved: Int = 10,
    val effortRating: String = "Good Burn", // "Easy", "Good Burn", "Near Failure"
    val progressionWeek: Int = 1,
    val isOverloadReady: Boolean = false,
    val isUpgradeUnlocked: Boolean = false,
    val upgradeDetails: UpgradeEligibilityResult? = null
) {
    val durationSeconds: Int
        get() = (endSeconds - startSeconds).toInt()

    fun formatDuration(): String {
        val totalSec = durationSeconds
        val mins = totalSec / 60
        val secs = totalSec % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    fun formatTimeRange(): String {
        val startM = (startSeconds.toInt()) / 60
        val startS = (startSeconds.toInt()) % 60
        val endM = (endSeconds.toInt()) / 60
        val endS = (endSeconds.toInt()) % 60
        return String.format("%02d:%02d - %02d:%02d", startM, startS, endM, endS)
    }
}

data class DayWorkoutPlan(
    val dayOfWeek: DayOfWeek,
    val title: String,
    val subtitle: String,
    val focusDescription: String,
    val segments: List<WorkoutSegment>
)
