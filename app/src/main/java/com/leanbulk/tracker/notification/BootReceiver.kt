package com.leanbulk.tracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.leanbulk.tracker.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val activeReminders = db.reminderDao().getActiveReminders()
                    activeReminders.forEach { entity ->
                        val item = com.leanbulk.tracker.data.model.ReminderItem(
                            id = entity.id,
                            title = entity.title,
                            description = entity.description,
                            hour = entity.hour,
                            minute = entity.minute,
                            isEnabled = entity.isEnabled,
                            isWeeklyOnly = entity.isWeeklyOnly,
                            weeklyDayOfWeek = entity.weeklyDayOfWeek
                        )
                        NotificationScheduler.scheduleReminder(context, item)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
