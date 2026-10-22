package com.leanbulk.tracker.ui.reminders

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.ReminderItem
import com.leanbulk.tracker.data.repository.ReminderRepository
import com.leanbulk.tracker.notification.NotificationHelper
import com.leanbulk.tracker.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<ReminderItem> = emptyList(),
    val isPermissionGranted: Boolean = true
)

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReminderRepository(AppDatabase.getInstance(application).reminderDao())
    private val context = application.applicationContext

    private val _isPermissionGranted = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            try {
                repository.ensureDefaultRemindersSeeded()
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error seeding reminders", e)
            }
        }
    }

    val uiState: StateFlow<RemindersUiState> = combine(
        repository.getAllReminders(),
        _isPermissionGranted
    ) { reminders, hasPerm ->
        RemindersUiState(
            reminders = reminders,
            isPermissionGranted = hasPerm
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RemindersUiState()
    )

    fun setPermissionGranted(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    fun toggleReminder(item: ReminderItem, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateReminderEnabled(item.id, isEnabled)
                val updatedItem = item.copy(isEnabled = isEnabled)
                if (isEnabled) {
                    NotificationScheduler.scheduleReminder(context, updatedItem)
                } else {
                    NotificationScheduler.cancelReminder(context, item.id)
                }
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error toggling reminder", e)
            }
        }
    }

    fun updateReminderTime(id: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                repository.updateReminderTime(id, hour, minute)
                val active = repository.getActiveReminders().find { it.id == id }
                if (active != null && active.isEnabled) {
                    NotificationScheduler.scheduleReminder(context, active.copy(hour = hour, minute = minute))
                }
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error updating reminder time", e)
            }
        }
    }

    fun triggerTestNotification(item: ReminderItem) {
        try {
            NotificationHelper.showNotification(
                context = context,
                notificationId = item.id + 1000,
                title = "[TEST] ${item.title}",
                message = item.description
            )
        } catch (e: Exception) {
            Log.e("RemindersViewModel", "Error triggering test notification", e)
        }
    }
}
