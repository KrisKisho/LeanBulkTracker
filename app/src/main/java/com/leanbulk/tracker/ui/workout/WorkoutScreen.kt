package com.leanbulk.tracker.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.ui.components.YouTubePlayerContainer
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile & Streak Banner
            WorkoutHeader(
                streak = state.streak,
                selectedDay = state.selectedDay
            )
        }

        item {
            // Day Selector Row
            DaySelectorRow(
                selectedDay = state.selectedDay,
                onDaySelected = { viewModel.selectDay(it) }
            )
        }

        item {
            // Workout Plan Overview Header
            WorkoutOverviewCard(
                planTitle = state.plan.title,
                planSubtitle = state.plan.subtitle,
                focus = state.plan.focusDescription,
                completedCount = state.completedSegmentsCount,
                totalCount = state.totalSegments,
                progress = state.completionPercentage
            )
        }

        item {
            // Embedded YouTube Video Player for Active Segment
            if (state.activeSegment != null) {
                ActiveSegmentPlayerCard(
                    segment = state.activeSegment!!,
                    onSegmentEnded = { viewModel.onSegmentFinished(it) }
                )
            }
        }

        item {
            Text(
                text = "WORKOUT PLAYLIST (${state.completedSegmentsCount}/${state.totalSegments} Completed)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    letterSpacing = 0.8.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.plan.segments, key = { it.id }) { segment ->
            val isActive = segment.id == state.activeSegment?.id
            SegmentListItem(
                segment = segment,
                isActive = isActive,
                onSelect = { viewModel.selectSegment(segment) },
                onToggleComplete = { isChecked ->
                    viewModel.toggleSegmentCompletion(segment, isChecked)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WorkoutHeader(
    streak: Int,
    selectedDay: DayOfWeek
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Lean Bulk Workout",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Single 4 kg DB + Bodyweight Hypertrophy",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // Streak Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AmberGold.copy(alpha = 0.15f))
                .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streak Day${if (streak == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                )
            }
        }
    }
}

@Composable
fun DaySelectorRow(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit
) {
    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            val isSelected = day == selectedDay
            val isToday = day == java.time.LocalDate.now().dayOfWeek

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ElectricCyan else DarkSurfaceElevated,
                label = "day_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) DarkBackground else TextPrimary,
                label = "day_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(
                        1.dp,
                        if (isToday && !isSelected) ElectricCyan.copy(alpha = 0.6f) else DarkSurfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onDaySelected(day) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DarkBackground else NeonLime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutOverviewCard(
    planTitle: String,
    planSubtitle: String,
    focus: String,
    completedCount: Int,
    totalCount: Int,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = planSubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(color = ElectricCyan)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (progress >= 1f) NeonLime.copy(alpha = 0.2f) else DarkBackground)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$completedCount / $totalCount Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (progress >= 1f) NeonLime else TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = focus,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 1f) NeonLime else ElectricCyan,
                trackColor = DarkSurfaceBorder
            )
        }
    }
}

@Composable
fun ActiveSegmentPlayerCard(
    segment: WorkoutSegment,
    onSegmentEnded: (WorkoutSegment) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
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
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = segment.formatTimeRange(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${segment.orderIndex}. ${segment.title}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Video Player
            YouTubePlayerContainer(
                segment = segment,
                onSegmentEnded = onSegmentEnded
            )

            if (segment.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tip: ${segment.note}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AmberGold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SegmentListItem(
    segment: WorkoutSegment,
    isActive: Boolean,
    onSelect: () -> Unit,
    onToggleComplete: (Boolean) -> Unit
) {
    val borderColor = if (isActive) ElectricCyan else DarkSurfaceBorder
    val bgColor = if (isActive) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = segment.isCompleted,
                onCheckedChange = onToggleComplete,
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonLime,
                    checkmarkColor = DarkBackground,
                    uncheckedColor = TextMuted
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${segment.orderIndex}. ${segment.title}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (segment.isCompleted) TextMuted else TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = segment.formatTimeRange(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = segment.formatDuration(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = ElectricCyan
                            )
                        )
                    }
                }

                if (segment.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = segment.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    )
                }
            }

            IconButton(
                onClick = onSelect,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play segment",
                    tint = if (isActive) ElectricCyan else TextSecondary
                )
            }
        }
    }
}
