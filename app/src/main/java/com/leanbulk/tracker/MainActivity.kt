package com.leanbulk.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.leanbulk.tracker.ui.meals.MealsScreen
import com.leanbulk.tracker.ui.navigation.Screen
import com.leanbulk.tracker.ui.progress.ProgressScreen
import com.leanbulk.tracker.ui.reminders.RemindersScreen
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurface
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.LeanBulkTrackerTheme
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.workout.WorkoutScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.e("MainActivity", "enableEdgeToEdge fallback", e)
        }

        // Request Activity Recognition and Notifications permissions if not yet granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permsToRequest = mutableListOf<String>()
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (permsToRequest.isNotEmpty()) {
                requestPermissions(permsToRequest.toTypedArray(), 1001)
            }
        }

        try {
            setContent {
                LeanBulkTrackerTheme {
                    MainAppScaffold()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in setContent", e)
        }
    }

    override fun onResume() {
        super.onResume()
        com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(this)
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Workout,
        Screen.Meals,
        Screen.Progress,
        Screen.Reminders
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Workout.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Workout.route) {
                WorkoutScreen()
            }
            composable(Screen.Meals.route) {
                MealsScreen()
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Reminders.route) {
                RemindersScreen()
            }
        }
    }
}
