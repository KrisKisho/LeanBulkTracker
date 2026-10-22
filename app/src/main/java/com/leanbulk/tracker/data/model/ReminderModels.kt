package com.leanbulk.tracker.data.model

data class ReminderItem(
    val id: Int,
    val title: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val isWeeklyOnly: Boolean = false,
    val weeklyDayOfWeek: Int = java.time.DayOfWeek.MONDAY.value // 1 for Monday
) {
    fun formattedTime(): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    val scheduleTypeLabel: String
        get() = if (isWeeklyOnly) "Every Monday" else "Daily"
}

object DefaultReminders {
    fun getList(): List<ReminderItem> = listOf(
        ReminderItem(
            id = 101,
            title = "Breakfast Time",
            description = "4 Idlis/Dosas + 2 Eggs (520 kcal | 26g P)",
            hour = 8,
            minute = 15,
            isEnabled = true
        ),
        ReminderItem(
            id = 102,
            title = "Power Shake Time",
            description = "Blend your 490 kcal PB Banana Oat Shake!",
            hour = 11,
            minute = 0,
            isEnabled = true
        ),
        ReminderItem(
            id = 103,
            title = "Lunch Window",
            description = "Rice + Chicken/Soya Curry + Poriyal (570 kcal | 38g P)",
            hour = 13,
            minute = 30,
            isEnabled = true
        ),
        ReminderItem(
            id = 104,
            title = "Pre-Workout Fuel",
            description = "1 cup Kala Chana Sundal (310 kcal)",
            hour = 17,
            minute = 0,
            isEnabled = true
        ),
        ReminderItem(
            id = 105,
            title = "Workout Time",
            description = "Today's workout video segments are ready! Grab your 4kg dumbbell.",
            hour = 18,
            minute = 15,
            isEnabled = true
        ),
        ReminderItem(
            id = 106,
            title = "Dinner & Recovery",
            description = "Rice + Egg Curry/Thick Pappu + Curd (530 kcal)",
            hour = 20,
            minute = 30,
            isEnabled = true
        ),
        ReminderItem(
            id = 107,
            title = "Weekly Weigh-In",
            description = "Log your empty-stomach weight and waist size.",
            hour = 7,
            minute = 0,
            isEnabled = true,
            isWeeklyOnly = true,
            weeklyDayOfWeek = java.time.DayOfWeek.MONDAY.value
        )
    )
}
