package com.leanbulk.tracker.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.leanbulk.tracker.MainActivity
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.DynamicNutritionEngine
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.data.model.WorkoutDataSource
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

class LeanBulkGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val db = AppDatabase.getInstance(context)

        val profile = db.userProfileDao().getUserProfileSync()?.toModel() ?: UserProfile()
        val meals = db.mealDao().getMealsForDateSync(todayStr)
        val workoutLogs = db.workoutDao().getWorkoutLogsForDateSync(todayStr)

        val prefs = context.getSharedPreferences("health_sync_prefs", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("steps_date", "")
        val steps = if (savedDate == todayStr) prefs.getInt("daily_steps", 0) else 0

        val loggedCalories = meals.filter { it.isChecked }.sumOf { it.calories }
        val loggedProtein = meals.filter { it.isChecked }.sumOf { it.protein }

        val dayPlan = WorkoutDataSource.getWeeklyPlans()[today.dayOfWeek]
        val totalSegments = dayPlan?.segments?.size ?: 5
        val completedSegments = workoutLogs.count { it.isCompleted }

        val nutritionTarget = DynamicNutritionEngine.calculate(
            profile = profile,
            dailySteps = steps,
            activeCaloriesBurned = (steps * profile.currentWeightKg * 0.0005f).toInt(),
            completedWorkoutSegmentsCount = completedSegments,
            totalSegmentsForDay = totalSegments,
            dayOfWeek = today.dayOfWeek,
            currentTime = LocalTime.now(),
            includePlannedWorkoutInTarget = true
        )

        val targetCalories = nutritionTarget.dynamicCalorieTarget
        val targetProtein = nutritionTarget.proteinTargetGrams

        val dayShort = today.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.getDefault())
        val splitTitle = dayPlan?.title ?: "Hypertrophy"
        val splitProgress = "$dayShort: $splitTitle • $completedSegments/$totalSegments"

        provideContent {
            val bgDark = Color(0xFF0F172A)
            val cardDark = Color(0xFF1E293B)
            val cyan = Color(0xFF00E5FF)
            val lime = Color(0xFF76FF03)
            val gold = Color(0xFFFFB300)
            val textMuted = Color(0xFF94A3B8)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgDark)
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(10.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // Top Row: App branding + Today's Workout Split
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "LEAN BULK",
                            style = TextStyle(
                                color = ColorProvider(cyan),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = splitProgress,
                            style = TextStyle(
                                color = ColorProvider(gold),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Metrics Row: 3 Boxes (Calories, Protein, Steps)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        // Calories Box
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(cardDark)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                            ) {
                                Text(
                                    text = "CALORIES",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "$loggedCalories / $targetCalories",
                                    style = TextStyle(color = ColorProvider(cyan), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    text = "kcal",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Protein Box
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(cardDark)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                            ) {
                                Text(
                                    text = "PROTEIN",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "$loggedProtein / ${targetProtein}g",
                                    style = TextStyle(color = ColorProvider(lime), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    text = "grams",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Steps Box
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(cardDark)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                            ) {
                                Text(
                                    text = "STEPS",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "%,d".format(steps),
                                    style = TextStyle(color = ColorProvider(gold), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    text = "live steps",
                                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 8.5.sp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Bottom info strip
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "Tap to open app • Keep daily surplus locked in",
                            style = TextStyle(color = ColorProvider(textMuted), fontSize = 9.sp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
