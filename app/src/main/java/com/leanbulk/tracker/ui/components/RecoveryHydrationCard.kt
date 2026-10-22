package com.leanbulk.tracker.ui.components

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun RecoveryHydrationCard(
    waterGlasses: Int,
    sleepHours: Float,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    onUpdateSleep: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val waterTarget = 10
    val waterLiters = waterGlasses * 0.3f
    val sleepTarget = 7.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recovery & Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Muscle Hypertrophy & Tissue Repair",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (waterGlasses >= waterTarget && sleepHours >= sleepTarget)
                                NeonLime.copy(alpha = 0.15f)
                            else ElectricCyan.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (waterGlasses >= waterTarget && sleepHours >= sleepTarget)
                                NeonLime.copy(alpha = 0.4f)
                            else ElectricCyan.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (waterGlasses >= waterTarget && sleepHours >= sleepTarget) "Fully Recovered" else "Active Recovery",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (waterGlasses >= waterTarget && sleepHours >= sleepTarget) NeonLime else ElectricCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hydration Section (1-Tap Glass Counter)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Hydration",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Hydration ($waterGlasses/$waterTarget Glasses)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "%.1f L / 3.0 L Daily Target".format(waterLiters),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (waterGlasses >= waterTarget) NeonLime else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Steppers: [-] and 1-tap [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkSurfaceBorder, CircleShape)
                                    .clickable { onDecrementWater() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan)
                                    .clickable { onIncrementWater() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+1 Glass",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 10 Glasses Visual Progress Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..waterTarget) {
                            val isFilled = i <= waterGlasses
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isFilled) ElectricCyan else DarkSurfaceElevated
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sleep Duration Section (Target: 7.5+ hrs)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Sleep",
                                    tint = AmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sleep Duration",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = if (sleepHours > 0f) "%.1f hrs (Target: 7.5+ hrs)".format(sleepHours) else "Target: 7.5+ hrs Rest",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (sleepHours >= sleepTarget) NeonLime else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Steppers [-] [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkSurfaceBorder, CircleShape)
                                    .clickable {
                                        val newH = (sleepHours - 0.5f).coerceAtLeast(0f)
                                        onUpdateSleep(newH)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Text(
                                text = "%.1f h".format(sleepHours),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (sleepHours >= sleepTarget) NeonLime else AmberGold,
                                    fontSize = 12.sp
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkSurfaceBorder, CircleShape)
                                    .clickable {
                                        val newH = (sleepHours + 0.5f).coerceAtMost(14f)
                                        onUpdateSleep(newH)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Toggle Chips: 6.0h, 7.0h, 7.5h, 8.0h, 8.5h
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(6.0f, 7.0f, 7.5f, 8.0f, 8.5f).forEach { hours ->
                            val isSelected = kotlin.math.abs(sleepHours - hours) < 0.1f
                            val isTargetReached = hours >= sleepTarget
                            val chipBg = when {
                                isSelected && isTargetReached -> NeonLime.copy(alpha = 0.25f)
                                isSelected -> AmberGold.copy(alpha = 0.25f)
                                else -> DarkSurfaceElevated
                            }
                            val chipBorder = when {
                                isSelected && isTargetReached -> NeonLime
                                isSelected -> AmberGold
                                else -> DarkSurfaceBorder
                            }
                            val chipText = when {
                                isSelected && isTargetReached -> NeonLime
                                isSelected -> AmberGold
                                else -> TextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(6.dp))
                                    .clickable { onUpdateSleep(hours) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%.1fh".format(hours),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = chipText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
