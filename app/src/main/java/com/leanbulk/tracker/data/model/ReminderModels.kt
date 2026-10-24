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
            title = "Breakfast & Morning Hydration",
            description = "4 Idlis/Dosas + 2 Eggs (520 kcal | 26g P) + 500ml water",
            hour = 8,
            minute = 15,
            isEnabled = true
        ),
        ReminderItem(
            id = 102,
            title = "Pre-Noon Bulking Shake",
            description = "Blend your PB Banana Oat Shake!",
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
            id = 108,
            title = "Sedentary Check",
            description = "Sedentary Check: Logged low steps so far. Take a 15-min walk to stay on track for your 6,000+ step goal.",
            hour = 16,
            minute = 0,
            isEnabled = true
        ),
        ReminderItem(
            id = 104,
            title = "Pre-Workout Sundal Fuel",
            description = "1 cup Kala Chana Sundal (310 kcal) for sustained workout energy.",
            hour = 17,
            minute = 45,
            isEnabled = true
        ),
        ReminderItem(
            id = 109,
            title = "Pre-Workout Primer",
            description = "Workout in 15 min: Drink 300ml water, cue up dumbbells & playlist!",
            hour = 19,
            minute = 15,
            isEnabled = true
        ),
        ReminderItem(
            id = 105,
            title = "Workout Start",
            description = "Today's workout video segments are ready! Grab your 4kg dumbbell.",
            hour = 19,
            minute = 30,
            isEnabled = true
        ),
        ReminderItem(
            id = 110,
            title = "Post-Workout Protein Window",
            description = "Hit 24g protein within 45 min for optimal muscle repair.",
            hour = 20,
            minute = 5,
            isEnabled = true
        ),
        ReminderItem(
            id = 106,
            title = "Post-Workout Dinner",
            description = "Rice + Egg Curry/Thick Pappu + Curd (530 kcal)",
            hour = 20,
            minute = 45,
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
