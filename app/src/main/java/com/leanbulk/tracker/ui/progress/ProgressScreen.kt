package com.leanbulk.tracker.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.data.model.WeightEntry
import com.leanbulk.tracker.ui.components.AdaptiveCoachStatusCard
import com.leanbulk.tracker.ui.components.BodyDimensionForecastCard
import com.leanbulk.tracker.ui.components.DataBackupCard
import com.leanbulk.tracker.ui.components.SmartCoachingCard
import com.leanbulk.tracker.ui.components.WeightTrajectoryChart
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showLogDialog) {
        LogWeightDialog(
            initialWeight = state.currentWeightKg,
            initialWaist = state.currentWaistInches,
            onDismiss = { viewModel.closeLogDialog() },
            onSave = { date, w, waist, notes ->
                viewModel.logWeight(date, w, waist, notes)
            }
        )
    }

    if (state.showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = state.userProfile,
            onDismiss = { viewModel.closeEditProfileDialog() },
            onSave = { updatedProfile ->
                viewModel.updateProfile(updatedProfile)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weight & Body Progress",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Target Gain: +%.2f kg / week".format(state.userProfile.targetWeeklyGainKg),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Profile button
                    OutlinedButton(
                        onClick = { viewModel.openEditProfileDialog() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profile", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    // Log Today button
                    Button(
                        onClick = { viewModel.openLogDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }

        item {
            // Autonomous Closed-Loop Coaching Status & History Card
            AdaptiveCoachStatusCard(
                status = state.adaptiveStatus,
                adaptationLogs = state.adaptationLogs,
                onToggleAutoAdapt = { viewModel.toggleAutoAdapt(it) },
                onRunAdaptationCheck = { viewModel.runAdaptationCheckNow() },
                isEvaluating = state.isEvaluatingAdaptation
            )
        }

        item {
            // User Baseline Stats Card (Clickable to edit)
            UserProfileBaselineCard(
                profile = state.userProfile,
                onEdit = { viewModel.openEditProfileDialog() }
            )
        }

        item {
            // Module 2: Body Dimensions Forecast & Smart Waist Ceiling Table
            BodyDimensionForecastCard(forecast = state.forecast)
        }

        item {
            // Smart Coaching Banner (14-day analysis)
            SmartCoachingCard(insight = state.coachingInsight)
        }

        item {
            // Line Chart: Dual Trajectory from startWeight toward goalWeight with Waist
            WeightTrajectoryChart(
                entries = state.entries,
                startWeight = state.userProfile.startWeightKg,
                targetWeight = state.userProfile.goalWeightKg,
                baselineWaist = state.userProfile.currentWaistInches
            )
        }

        item {
            // Data Management: Full Export and Import
            DataBackupCard()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHECK-IN HISTORY (${state.entries.size} LOGS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        if (state.entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No check-in entries yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap \"+ Log\" at the top to record your weight & waist",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            items(state.entries.sortedByDescending { it.date }, key = { it.id }) { entry ->
                WeightHistoryItem(
                    entry = entry,
                    startWeight = state.userProfile.startWeightKg,
                    onDelete = { viewModel.deleteEntry(entry.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UserProfileBaselineCard(
    profile: UserProfile,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROFILE BASELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        ),
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Wrist: ${profile.wristInches}\"",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Ankle: ${profile.ankleInches}\"",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Bio Stats", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${profile.age}y | ${profile.sex} | ${profile.heightCm.toInt()}cm", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                }

                Column {
                    Text("Weight Trajectory", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("%.1f kg → %.1f kg".format(profile.startWeightKg, profile.goalWeightKg), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NeonLime))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Pace", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("+%.2f kg/wk".format(profile.targetWeeklyGainKg), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AmberGold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arm: ${profile.currentArmInches}\" | Chest: ${profile.currentChestInches}\" | Waist: ${profile.currentWaistInches}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                )

                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = ElectricCyan, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun WeightHistoryItem(
    entry: WeightEntry,
    startWeight: Float,
    onDelete: () -> Unit
) {
    val diffFromStart = entry.weightKg - startWeight
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.date.format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = "Weight",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f kg".format(entry.weightKg),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(%+.1f kg)".format(diffFromStart),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (diffFromStart >= 0) NeonLime else AmberGold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = "Waist",
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f\"".format(entry.waistInches),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
