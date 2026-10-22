package com.leanbulk.tracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.leanbulk.tracker.data.model.ReminderItem
import com.leanbulk.tracker.data.model.WorkoutDataSource
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.leanbulk.tracker.TRIGGER_REMINDER"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_IS_WEEKLY = "extra_is_weekly"
        const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        var title = intent.getStringExtra(EXTRA_TITLE) ?: "Lean Bulk Reminder"
        var message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Stay on track with your lean bulk goals!"
        val isWeekly = intent.getBooleanExtra(EXTRA_IS_WEEKLY, false)
        val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, 1)
        val hour = intent.getIntExtra(EXTRA_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        // If this is the workout reminder (id 105), customize message with today's day & workout title
        if (id == 105) {
            val today = LocalDate.now().dayOfWeek
            val plan = WorkoutDataSource.getPlanForDay(today)
            title = "Workout Time: ${today.name.lowercase().replaceFirstChar { it.uppercase() }}"
            message = "Today's ${plan.title} segments are ready! Grab your 4kg dumbbell."
        }

        NotificationHelper.showNotification(
            context = context,
            notificationId = id,
            title = title,
            message = message
        )

        // Reschedule next occurrence
        val reminderItem = ReminderItem(
            id = id,
            title = intent.getStringExtra(EXTRA_TITLE) ?: title,
            description = intent.getStringExtra(EXTRA_MESSAGE) ?: message,
            hour = hour,
            minute = minute,
            isEnabled = true,
            isWeeklyOnly = isWeekly,
            weeklyDayOfWeek = dayOfWeek
        )
        NotificationScheduler.scheduleReminder(context, reminderItem)
    }
}
