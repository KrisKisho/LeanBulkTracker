package com.leanbulk.tracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.leanbulk.tracker.data.model.ReminderItem
import java.util.Calendar

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    fun scheduleReminder(context: Context, item: ReminderItem) {
        if (!item.isEnabled) {
            cancelReminder(context, item.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_REMINDER
            putExtra(AlarmReceiver.EXTRA_ID, item.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, item.title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, item.description)
            putExtra(AlarmReceiver.EXTRA_IS_WEEKLY, item.isWeeklyOnly)
            putExtra(AlarmReceiver.EXTRA_DAY_OF_WEEK, item.weeklyDayOfWeek)
            putExtra(AlarmReceiver.EXTRA_HOUR, item.hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, item.minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = calculateTriggerMillis(item)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canExact = try { alarmManager.canScheduleExactAlarms() } catch (_: Exception) { false }
                if (canExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder ${item.id} for $triggerAtMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact reminder ${item.id}", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Fallback schedule failed for ${item.id}", fallbackError)
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling reminder $reminderId", e)
            }
        }
    }

    fun calculateTriggerMillis(item: ReminderItem): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.hour)
            set(Calendar.MINUTE, item.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()

        if (item.isWeeklyOnly) {
            val targetCalDay = when (item.weeklyDayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }
            while (calendar.get(Calendar.DAY_OF_WEEK) != targetCalDay || calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis
    }
}
