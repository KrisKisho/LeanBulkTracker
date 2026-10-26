package com.leanbulk.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.model.AdaptiveStatusInfo
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.CoralRed
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurface
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.PurpleAccent
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveCoachStatusCard(
    status: AdaptiveStatusInfo,
    adaptationLogs: List<AdaptationLogEntity>,
    onToggleAutoAdapt: (Boolean) -> Unit,
    onRunAdaptationCheck: () -> Unit,
    isEvaluating: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showLogSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkSurfaceElevated,
                        DarkSurface
                    )
                )
            )
            .border(
                1.5.dp,
                if (status.autoAdaptEnabled) ElectricCyan.copy(alpha = 0.5f) else DarkSurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Header with Coach Icon, Title and Live Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (status.autoAdaptEnabled) ElectricCyan.copy(alpha = 0.15f)
                                else DarkSurfaceBorder.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (status.autoAdaptEnabled) ElectricCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Autonomous Coach",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (status.autoAdaptEnabled) NeonLime.copy(alpha = 0.15f)
                                        else DarkSurfaceBorder
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (status.autoAdaptEnabled) "ACTIVE" else "PAUSED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (status.autoAdaptEnabled) NeonLime else TextMuted
                                )
                            }
                        }
                        Text(
                            text = if (status.autoAdaptEnabled) "Self-adapting calories, splits & schedule"
                            else "Manual control • Auto-adaptation off",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = status.autoAdaptEnabled,
                    onCheckedChange = onToggleAutoAdapt,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonLime,
                        checkedTrackColor = NeonLime.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Real-time Adaptive Metrics Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                val isCutting = status.goalMode == "WEIGHT_LOSS"
                val paceSubtext = if (isCutting) {
                    "Target: -${String.format("%.2f", status.targetWeeklyGainKg)} kg/wk"
                } else if (status.goalMode == "BODY_RECOMP") {
                    "Target: ±0.00 kg/wk (Recomp)"
                } else {
                    "Target: +${String.format("%.2f", status.targetWeeklyGainKg)} kg/wk"
                }
                val paceAccent = if (isCutting) {
                    when {
                        status.ewmaGainRateKgPerWeek < -1.0f -> CoralRed
                        status.ewmaGainRateKgPerWeek > -0.15f -> AmberGold
                        else -> NeonLime
                    }
                } else {
                    when {
                        status.ewmaGainRateKgPerWeek < (status.targetWeeklyGainKg * 0.55f) -> AmberGold
                        status.ewmaGainRateKgPerWeek > 0.50f -> CoralRed
                        else -> NeonLime
                    }
                }

                // Metric 1: True EWMA Pace
                AdaptiveMetricTile(
                    label = "EWMA Pace",
                    value = String.format("%+.2f kg/wk", status.ewmaGainRateKgPerWeek),
                    subtext = paceSubtext,
                    accentColor = paceAccent,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Metric 2: Dynamic Calorie Calibration
                AdaptiveMetricTile(
                    label = "Calorie Target",
                    value = "${status.effectiveBaseCalories} kcal",
                    subtext = if (isCutting) {
                        "Fat-Loss Calorie Target"
                    } else if (status.adaptiveCalorieOffset != 0) {
                        "Base 2,420 ${if (status.adaptiveCalorieOffset > 0) "+" else ""}${status.adaptiveCalorieOffset} kcal"
                    } else {
                        "Base 2,420 kcal (Nominal)"
                    },
                    accentColor = AmberGold,
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Metric 3: Workout Queue Status
                AdaptiveMetricTile(
                    label = "Workout Queue",
                    value = if (status.isShifted) "Auto-Shifted" else "On Schedule",
                    subtext = if (status.isShifted) status.workoutQueueStatus else "48h fatigue lock active",
                    accentColor = if (status.isShifted) PurpleAccent else ElectricCyan,
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Metric 4: Evening Strategy
                AdaptiveMetricTile(
                    label = "Evening Fuel",
                    value = if (status.isSplitShakeActive) "Split Shake" else "Standard",
                    subtext = if (status.isSplitShakeActive) "8:05 PM Whey + Light Dinner" else "11 AM Single Shake",
                    accentColor = if (status.isSplitShakeActive) NeonLime else TextSecondary,
                    icon = Icons.Default.Nightlife,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: View Adaptation Log & Run Adaptation Check Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showLogSheet = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, ElectricCyan.copy(alpha = 0.5f)))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "History (${adaptationLogs.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onRunAdaptationCheck,
                    enabled = !isEvaluating,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = DarkBackground
                    )
                ) {
                    if (isEvaluating) {
                        CircularProgressIndicator(
                            color = DarkBackground,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEvaluating) "Evaluating..." else "Run Check",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showLogSheet) {
        AdaptationLogBottomSheet(
            logs = adaptationLogs,
            onDismiss = { showLogSheet = false }
        )
    }
}

@Composable
fun AdaptiveMetricTile(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBackground.copy(alpha = 0.6f))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationLogBottomSheet(
    logs: List<AdaptationLogEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Adaptation Log",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Audit trail of autonomous coaching decisions",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All Parameters Running Smoothly",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The coach evaluates your weight EWMA, adherence, and timing every 7 days or after late workouts.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs.sortedByDescending { it.timestampMillis }) { log ->
                        AdaptationLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptationLogItem(log: AdaptationLogEntity) {
    val (categoryColor, categoryIcon) = when (log.category) {
        "NUTRITION" -> Pair(AmberGold, Icons.Default.LocalFireDepartment)
        "WORKOUT_SCHEDULE" -> Pair(ElectricCyan, Icons.Default.FitnessCenter)
        "OVERLOAD" -> Pair(PurpleAccent, Icons.Default.Speed)
        "CHRONOTYPE" -> Pair(NeonLime, Icons.Default.Nightlife)
        "WAIST_GUARDRAIL" -> Pair(CoralRed, Icons.Default.Warning)
        else -> Pair(ElectricCyan, Icons.Default.AutoAwesome)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBackground.copy(alpha = 0.7f))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.category.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                Text(
                    text = log.date,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.reason,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            if (log.parameterChanged.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Mutated: ${log.parameterChanged}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}
