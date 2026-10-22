package com.leanbulk.tracker.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.model.WeightEntry
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

enum class TrajectoryViewMode {
    BOTH,
    WEIGHT_ONLY,
    WAIST_ONLY
}

@Composable
fun WeightTrajectoryChart(
    entries: List<WeightEntry>,
    startWeight: Float = 60.0f,
    targetWeight: Float = 70.0f,
    baselineWaist: Float = 29.5f,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(TrajectoryViewMode.BOTH) }

    val sortedEntries = entries.sortedBy { it.date }
    val currentWeight = sortedEntries.lastOrNull()?.weightKg ?: startWeight
    val currentWaist = sortedEntries.lastOrNull()?.waistInches ?: baselineWaist
    val totalWeightGained = currentWeight - startWeight
    val remainingWeight = targetWeight - currentWeight
    val waistChange = currentWaist - baselineWaist

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Trajectory Summary Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Weight & Waist Trajectory",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Goal: %.1f → %.1f kg  •  Baseline Waist: %.1f\"".format(startWeight, targetWeight, baselineWaist),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Cards (Equal Weight)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Weight Stat Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "CURRENT WEIGHT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "%.1f kg".format(currentWeight),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "%+.2f kg".format(totalWeightGained),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (totalWeightGained >= 0) NeonLime else AmberGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Waist Stat Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "CURRENT WAIST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "%.1f\"".format(currentWaist),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "%+.1f\"".format(waistChange),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (waistChange <= 0.5f) NeonLime else AmberGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Mode Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrajectoryFilterPill(
                    label = "Dual Trajectory",
                    isSelected = viewMode == TrajectoryViewMode.BOTH,
                    onClick = { viewMode = TrajectoryViewMode.BOTH }
                )
                TrajectoryFilterPill(
                    label = "Weight (kg)",
                    isSelected = viewMode == TrajectoryViewMode.WEIGHT_ONLY,
                    onClick = { viewMode = TrajectoryViewMode.WEIGHT_ONLY }
                )
                TrajectoryFilterPill(
                    label = "Waist (in)",
                    isSelected = viewMode == TrajectoryViewMode.WAIST_ONLY,
                    onClick = { viewMode = TrajectoryViewMode.WAIST_ONLY }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Reserve padding for Y and X axis labels
                val leftPad = 92f
                val rightPad = if (viewMode == TrajectoryViewMode.WEIGHT_ONLY) 30f else 92f
                val topPad = 36f
                val bottomPad = 48f

                val chartWidth = canvasWidth - leftPad - rightPad
                val chartHeight = canvasHeight - topPad - bottomPad

                // Dynamic Y-axis bounds for Weight (kg) adapting to user's start, goal, and logged entries
                val allWeights = sortedEntries.map { it.weightKg } + listOf(startWeight, targetWeight, currentWeight)
                val minWeightY = (allWeights.minOrNull() ?: 58.0f) - 1.5f
                val maxWeightY = (allWeights.maxOrNull() ?: 72.0f) + 1.5f
                val weightRange = (maxWeightY - minWeightY).coerceAtLeast(1.0f)

                fun getWeightY(weight: Float): Float {
                    val norm = (weight - minWeightY) / weightRange
                    return topPad + chartHeight * (1f - norm)
                }

                // Dynamic Y-axis bounds for Waist (inches)
                val allWaists = sortedEntries.map { it.waistInches } + listOf(baselineWaist, currentWaist)
                val minWaistY = (allWaists.minOrNull() ?: 28.0f) - 1.0f
                val maxWaistY = (allWaists.maxOrNull() ?: 33.0f) + 1.0f
                val waistRange = (maxWaistY - minWaistY).coerceAtLeast(1.0f)

                fun getWaistY(waist: Float): Float {
                    val norm = (waist - minWaistY) / waistRange
                    return topPad + chartHeight * (1f - norm)
                }

                val paintAxis = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(160, 158, 168, 190)
                    textSize = 24f
                    isAntiAlias = true
                }

                val weightAxisPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(230, 0, 229, 255)
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                val waistAxisPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(230, 255, 179, 0)
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }

                val targetGoalPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(230, 118, 255, 3)
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

                // Reference Grid Lines
                // 1. Dynamic Target Weight Line
                if (viewMode != TrajectoryViewMode.WAIST_ONLY) {
                    val yTarget = getWeightY(targetWeight)
                    drawLine(
                        color = NeonLime.copy(alpha = 0.45f),
                        start = Offset(leftPad, yTarget),
                        end = Offset(canvasWidth - rightPad, yTarget),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashedEffect
                    )
                    val goalLabel = if (targetWeight % 1f == 0f) "Goal %.0fkg".format(targetWeight) else "Goal %.1fkg".format(targetWeight)
                    drawContext.canvas.nativeCanvas.drawText(goalLabel, 6f, yTarget - 6f, targetGoalPaint)
                }

                // 2. Dynamic Start Weight Line
                if (viewMode != TrajectoryViewMode.WAIST_ONLY) {
                    val yStart = getWeightY(startWeight)
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.45f),
                        start = Offset(leftPad, yStart),
                        end = Offset(canvasWidth - rightPad, yStart),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashedEffect
                    )
                    val startLabel = if (startWeight % 1f == 0f) "Start %.0fkg".format(startWeight) else "Start %.1fkg".format(startWeight)
                    drawContext.canvas.nativeCanvas.drawText(startLabel, 6f, yStart - 6f, weightAxisPaint)
                }

                // 3. Dynamic Baseline Waist Line
                if (viewMode != TrajectoryViewMode.WEIGHT_ONLY) {
                    val yWaistBase = getWaistY(baselineWaist)
                    drawLine(
                        color = AmberGold.copy(alpha = 0.45f),
                        start = Offset(leftPad, yWaistBase),
                        end = Offset(canvasWidth - rightPad, yWaistBase),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashedEffect
                    )
                    val waistLabel = "Waist %.1f\"".format(baselineWaist)
                    drawContext.canvas.nativeCanvas.drawText(
                        waistLabel,
                        canvasWidth - 6f,
                        yWaistBase - 6f,
                        waistAxisPaint
                    )
                }

                if (sortedEntries.isNotEmpty()) {
                    val count = sortedEntries.size
                    val stepX = if (count > 1) chartWidth / (count - 1) else 0f

                    // WEIGHT TRAJECTORY CURVE
                    if (viewMode != TrajectoryViewMode.WAIST_ONLY) {
                        val weightPoints = sortedEntries.mapIndexed { idx, entry ->
                            val x = if (count == 1) leftPad + chartWidth / 2 else leftPad + idx * stepX
                            Offset(x, getWeightY(entry.weightKg))
                        }

                        // Gradient fill under weight curve
                        val weightFillPath = Path().apply {
                            moveTo(weightPoints.first().x, topPad + chartHeight)
                            weightPoints.forEach { lineTo(it.x, it.y) }
                            lineTo(weightPoints.last().x, topPad + chartHeight)
                            close()
                        }

                        drawPath(
                            path = weightFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ElectricCyan.copy(alpha = 0.25f),
                                    ElectricCyan.copy(alpha = 0.0f)
                                ),
                                startY = topPad,
                                endY = topPad + chartHeight
                            )
                        )

                        // Connecting stroke
                        val weightStrokePath = Path().apply {
                            moveTo(weightPoints.first().x, weightPoints.first().y)
                            for (i in 1 until weightPoints.size) {
                                lineTo(weightPoints[i].x, weightPoints[i].y)
                            }
                        }

                        drawPath(
                            path = weightStrokePath,
                            color = ElectricCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 7-DAY MOVING AVERAGE SECONDARY TREND LINE
                        if (sortedEntries.size >= 2) {
                            val maPoints = sortedEntries.mapIndexed { idx, entry ->
                                val x = if (count == 1) leftPad + chartWidth / 2 else leftPad + idx * stepX
                                val window = sortedEntries.filter { it.date in entry.date.minusDays(6)..entry.date }
                                val maWeight = if (window.isNotEmpty()) window.map { it.weightKg }.average().toFloat() else entry.weightKg
                                Offset(x, getWeightY(maWeight))
                            }

                            val maStrokePath = Path().apply {
                                moveTo(maPoints.first().x, maPoints.first().y)
                                for (i in 1 until maPoints.size) {
                                    lineTo(maPoints[i].x, maPoints[i].y)
                                }
                            }

                            drawPath(
                                path = maStrokePath,
                                color = Color(0xFFCE93D8),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )
                            )
                        }

                        // Dots & Labels
                        val weightLabelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24f
                            isFakeBoldText = true
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        weightPoints.forEachIndexed { idx, pt ->
                            drawCircle(color = ElectricCyan.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = pt)
                            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pt)

                            // Label above point
                            drawContext.canvas.nativeCanvas.drawText(
                                "%.1f".format(sortedEntries[idx].weightKg),
                                pt.x,
                                pt.y - 14f,
                                weightLabelPaint
                            )
                        }
                    }

                    // WAIST TRAJECTORY CURVE
                    if (viewMode != TrajectoryViewMode.WEIGHT_ONLY) {
                        val waistPoints = sortedEntries.mapIndexed { idx, entry ->
                            val x = if (count == 1) leftPad + chartWidth / 2 else leftPad + idx * stepX
                            Offset(x, getWaistY(entry.waistInches))
                        }

                        // Connecting stroke
                        val waistStrokePath = Path().apply {
                            moveTo(waistPoints.first().x, waistPoints.first().y)
                            for (i in 1 until waistPoints.size) {
                                lineTo(waistPoints[i].x, waistPoints[i].y)
                            }
                        }

                        drawPath(
                            path = waistStrokePath,
                            color = AmberGold,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Dots & Labels
                        val waistLabelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(240, 255, 179, 0)
                            textSize = 22f
                            isFakeBoldText = true
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        waistPoints.forEachIndexed { idx, pt ->
                            drawCircle(color = AmberGold.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = pt)
                            drawCircle(color = AmberGold, radius = 3.dp.toPx(), center = pt)

                            // Label below point
                            val yOffset = if (viewMode == TrajectoryViewMode.BOTH) 26f else -14f
                            drawContext.canvas.nativeCanvas.drawText(
                                "%.1f\"".format(sortedEntries[idx].waistInches),
                                pt.x,
                                pt.y + yOffset,
                                waistLabelPaint
                            )
                        }
                    }

                    // Date labels along X-axis
                    val datePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 158, 168, 190)
                        textSize = 22f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

                    sortedEntries.forEachIndexed { idx, entry ->
                        val x = if (count == 1) leftPad + chartWidth / 2 else leftPad + idx * stepX
                        val dateText = entry.date.format(dateFormatter)
                        drawContext.canvas.nativeCanvas.drawText(
                            dateText,
                            x,
                            topPad + chartHeight + 34f,
                            datePaint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricCyan))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Weight", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.5.sp)

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.width(12.dp).height(2.5.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFFCE93D8)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("7D Avg", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.5.sp)

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberGold))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Waist", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.5.sp)

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonLime))
                Spacer(modifier = Modifier.width(4.dp))
                val goalLegendText = if (targetWeight % 1f == 0f) "Goal (%.0f)".format(targetWeight) else "Goal (%.1f)".format(targetWeight)
                Text(goalLegendText, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.5.sp)
            }
        }
    }
}

@Composable
private fun TrajectoryFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) ElectricCyan else DarkBackground
    val textColor = if (isSelected) DarkBackground else TextSecondary
    val borderColor = if (isSelected) ElectricCyan else DarkSurfaceBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}
