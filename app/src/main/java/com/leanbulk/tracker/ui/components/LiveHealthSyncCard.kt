package com.leanbulk.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.health.HealthSyncData
import com.leanbulk.tracker.data.model.DynamicNutritionTarget
import com.leanbulk.tracker.data.model.SouthIndianTopUp
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.CoralRed
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun LiveHealthSyncCard(
    syncData: HealthSyncData,
    nutritionTarget: DynamicNutritionTarget,
    onSyncNow: () -> Unit,
    onAddStepBonus: (Int) -> Unit,
    onAddTopUpMeal: (SouthIndianTopUp) -> Unit,
    onRequestStepPermission: () -> Unit = {},
    onRequestHealthConnectPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE HEALTH & STEP ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = syncData.dataSource,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = if (syncData.isHealthConnectAuthorized) NeonLime else AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(
                        onClick = onSyncNow,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats Row: Steps | Active Burn | Dynamic Target
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Steps", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "%,d".format(syncData.dailySteps),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "+${syncData.activeCaloriesBurned} kcal active",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = AmberGold)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Workout Burn", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "+${nutritionTarget.workoutBurn} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                    )
                    Text(
                        text = if (nutritionTarget.isWorkoutAnticipated) "Anticipated" else "Completed (${nutritionTarget.completedWorkoutBurn}k)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = if (nutritionTarget.isWorkoutAnticipated) ElectricCyan else NeonLime
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Adjusted Target", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${nutritionTarget.dynamicCalorieTarget} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonLime)
                    )
                    val diff = nutritionTarget.dynamicCalorieTarget - 2420
                    Text(
                        text = if (diff >= 0) "+$diff kcal bulk" else "$diff kcal",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = if (diff >= 150) NeonLime else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Permission Action Banner (if hardware step sensor is present but permission is not yet granted)
            if (!syncData.hasHardwarePermission && syncData.hasHardwareSensor) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CoralRed.copy(alpha = 0.15f))
                        .border(1.dp, CoralRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Step Sensor Permission Needed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralRed
                                )
                            )
                            Text(
                                text = "Enable Physical Activity permission to read steps directly from phone hardware.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestStepPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralRed,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (syncData.isHealthConnectAvailable && !syncData.isHealthConnectAuthorized) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Health Connect Available",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            )
                            Text(
                                text = "Tap to sync steps & active calories from your fitness apps.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestHealthConnectPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Step Test/Simulation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Step Calibration:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickStepPill("+1,000 Steps") { onAddStepBonus(1000) }
                    QuickStepPill("+2,500 Steps") { onAddStepBonus(2500) }
                }
            }

            // Top-Up Recommendation Banner (if dynamicCalorieTarget > 2,420 by >= 150 kcal)
            nutritionTarget.topUpRecommendation?.let { topUp ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberGold.copy(alpha = 0.12f))
                        .border(1.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "High Burn Top-Up Recommendation",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                )
                            }

                            Text(
                                text = "+${nutritionTarget.excessBurnKcal} kcal today",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonLime,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = topUp.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onAddTopUpMeal(topUp) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to Today's Meal Log (+${topUp.calories} kcal)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStepPill(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkBackground)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        )
    }
}
