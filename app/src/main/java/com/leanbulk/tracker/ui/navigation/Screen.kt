package com.leanbulk.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Workout : Screen("workout", "Workout", Icons.Default.FitnessCenter)
    object Meals : Screen("meals", "Meals", Icons.Default.Restaurant)
    object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
    object Reminders : Screen("reminders", "Reminders", Icons.Default.Alarm)
}
