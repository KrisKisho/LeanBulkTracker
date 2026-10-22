package com.leanbulk.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.model.BodyForecastResult
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
import java.time.format.DateTimeFormatter

@Composable
fun BodyDimensionForecastCard(
    forecast: BodyForecastResult,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Body Dimensions & Frame Forecast",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lean Bulk Growth vs. McCallum Symmetry",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Goal: %.1f kg".format(forecast.goalWeightKg),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Current vs Target Weight Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weight Trajectory",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%.1f kg → %.1f kg (%.0f%%)".format(
                            forecast.currentWeightKg,
                            forecast.goalWeightKg,
                            forecast.weightProgressFraction * 100f
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { forecast.weightProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonLime,
                    trackColor = DarkSurfaceBorder
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remaining: %.1f kg (%.1f wks)".format(
                            forecast.remainingKg,
                            forecast.weeksRemaining
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                        maxLines = 1
                    )
                    Text(
                        text = "Target: ${forecast.estimatedCompletionDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AmberGold,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. WHtR Visceral Fat Alert (if applicable)
            if (forecast.isWhtrWarning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberGold.copy(alpha = 0.15f))
                        .border(1.dp, AmberGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Visceral Fat Guardrail Alert",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = forecast.whtrWarningMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Smart Waist Ceiling & Guardrail Summary Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text("Safe Waist Ceiling", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                    Text("%.1f\" (Hard: %.1f\")".format(forecast.maxHealthyWaistInches, forecast.hardLimitWaistInches), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AmberGold), maxLines = 1)
                }
                Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Projected at 70kg", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                    Text("%.1f\"".format(forecast.projectedWaistInches), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = ElectricCyan), maxLines = 1)
                }
                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                    Text("WHtR Index", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                    Text("%.2f".format(forecast.currentWhtr), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (forecast.isWhtrWarning) AmberGold else NeonLime), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Dimensions Comparison Table
            Text(
                text = "BODY DIMENSIONS FORECAST TABLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Part", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.4f))
                Text("Now", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold), modifier = Modifier.weight(0.85f))
                Text("At 70kg", style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold), modifier = Modifier.weight(0.95f))
                Text("Ideal", style = MaterialTheme.typography.labelSmall.copy(color = AmberGold, fontWeight = FontWeight.Bold), modifier = Modifier.weight(0.85f))
                Text("Status", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.35f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Table Rows
            forecast.dimensions.forEach { dim ->
                val rowBg = if (dim.isWaist) DarkBackground else DarkSurfaceElevated.copy(alpha = 0.5f)
                val statusColor = when {
                    dim.isWarning -> CoralRed
                    dim.statusText == "On Track" || dim.statusText == "Safe Ceiling" -> NeonLime
                    else -> ElectricCyan
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(
                            1.dp,
                            if (dim.isWaist) AmberGold.copy(alpha = 0.3f) else DarkSurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dim.partName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (dim.isWaist) FontWeight.Bold else FontWeight.Medium,
                            color = if (dim.isWaist) AmberGold else TextPrimary,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.weight(1.4f),
                        maxLines = 1
                    )

                    Text(
                        text = "%.1f\"".format(dim.currentInches),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.5.sp),
                        modifier = Modifier.weight(0.85f)
                    )

                    Text(
                        text = "%.1f\"".format(dim.projectedInches),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.weight(0.95f)
                    )

                    Text(
                        text = "%.1f\"".format(dim.idealInches),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AmberGold,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.weight(0.85f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dim.statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
