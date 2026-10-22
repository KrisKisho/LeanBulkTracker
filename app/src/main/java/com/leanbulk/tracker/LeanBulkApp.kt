package com.leanbulk.tracker

import android.app.Application
import android.util.Log
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.repository.ReminderRepository
import com.leanbulk.tracker.notification.NotificationHelper
import com.leanbulk.tracker.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeanBulkApp : Application() {

    companion object {
        private const val TAG = "LeanBulkApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Set safe uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            NotificationHelper.createNotificationChannel(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel", e)
        }

        // Safe background seeding and alarm scheduling
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(this@LeanBulkApp)
                val reminderRepo = ReminderRepository(db.reminderDao())
                reminderRepo.ensureDefaultRemindersSeeded()

                val active = reminderRepo.getActiveReminders()
                active.forEach { item ->
                    try {
                        NotificationScheduler.scheduleReminder(this@LeanBulkApp, item)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to schedule reminder ${item.id}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during LeanBulkApp background init", e)
            }
        }
    }
}
