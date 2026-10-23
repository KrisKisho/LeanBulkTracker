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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leanbulk.tracker.data.model.MesocyclePhase
import com.leanbulk.tracker.data.model.ProgressiveOverloadEngine
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.ui.components.OfflineVideosDialog
import com.leanbulk.tracker.ui.components.YouTubePlayerContainer
import kotlinx.coroutines.launch
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var autoPlaySegmentId by remember { mutableStateOf<String?>(null) }
    var isContinuousPlay by remember { mutableStateOf(false) }
    var showOfflineVideosDialog by remember { mutableStateOf(false) }

    if (showOfflineVideosDialog) {
        OfflineVideosDialog(onDismiss = { showOfflineVideosDialog = false })
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile & Streak Banner
            WorkoutHeader(
                streak = state.streak,
                selectedDay = state.selectedDay,
                onOpenOfflineVideos = { showOfflineVideosDialog = true }
            )
        }

        item {
            // 4-Week Double Progression Cycle Header Card
            MesocycleHeaderCard(
                activeWeek = state.activeCycleWeek,
                onWeekSelected = { viewModel.selectCycleWeek(it) }
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
            // Embedded YouTube / Local Video Player for Active Segment
            if (state.activeSegment != null) {
                ActiveSegmentPlayerCard(
                    segment = state.activeSegment!!,
                    onSegmentEnded = { viewModel.onSegmentFinished(it) },
                    autoPlay = autoPlaySegmentId == state.activeSegment?.id,
                    continuousPlay = isContinuousPlay,
                    onToggleContinuousPlay = { isContinuousPlay = it }
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
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(state.plan.segments, key = { it.id }) { segment ->
            val isActive = segment.id == state.activeSegment?.id
            SegmentListItem(
                segment = segment,
                isActive = isActive,
                activeCycleWeek = state.activeCycleWeek,
                onSelect = { viewModel.selectSegment(segment) },
                onPlay = {
                    viewModel.selectSegment(segment)
                    autoPlaySegmentId = segment.id
                    coroutineScope.launch {
                        listState.animateScrollToItem(4)
                    }
                },
                onToggleComplete = { isChecked ->
                    viewModel.toggleSegmentCompletion(segment, isChecked)
                },
                onOverloadChange = { weight, reps, set3Reps, effort ->
                    viewModel.updateOverload(segment, weight, reps, set3Reps, effort)
                },
                onApplyUpgrade = { nextWeight ->
                    viewModel.applyWeightUpgrade(segment, nextWeight)
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
    selectedDay: DayOfWeek,
    onOpenOfflineVideos: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Offline Videos Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NeonLime.copy(alpha = 0.15f))
                    .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onOpenOfflineVideos() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Offline Videos",
                        tint = NeonLime,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Videos",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    )
                }
            }

            // Streak Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AmberGold.copy(alpha = 0.15f))
                    .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
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
}

@Composable
fun MesocycleHeaderCard(
    activeWeek: Int,
    onWeekSelected: (Int) -> Unit
) {
    val phase = MesocyclePhase.fromWeek(activeWeek)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Mesocycle",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "4-WEEK DOUBLE PROGRESSION",
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
                        text = "Tempo: ${phase.tempoCode}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Week Selector Row: [Week 1: 3-1-1] [Week 2: 2-1-1] [Week 3: 2-0-1] [Week 4: Test]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    1 to "Week 1: 3-1-1",
                    2 to "Week 2: 2-1-1",
                    3 to "Week 3: 2-0-1",
                    4 to "Week 4: Test"
                ).forEach { (wk, label) ->
                    val isSelected = wk == activeWeek
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) ElectricCyan else DarkBackground,
                        label = "chip_bg"
                    )
                    val chipText by animateColorAsState(
                        targetValue = if (isSelected) DarkBackground else TextSecondary,
                        label = "chip_text"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isSelected) ElectricCyan else DarkSurfaceBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onWeekSelected(wk) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = chipText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Week Tempo Rule Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (activeWeek == 4) AmberGold.copy(alpha = 0.4f) else DarkSurfaceBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (activeWeek == 4) AmberGold else NeonLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Week $activeWeek: ${phase.phaseName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (activeWeek == 4) AmberGold else TextPrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${phase.cadenceSummary} • ${phase.focusDescription}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
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
    onSegmentEnded: (WorkoutSegment) -> Unit,
    autoPlay: Boolean = false,
    continuousPlay: Boolean = false,
    onToggleContinuousPlay: (Boolean) -> Unit = {}
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
                onSegmentEnded = onSegmentEnded,
                autoPlay = autoPlay,
                continuousPlay = continuousPlay,
                onToggleContinuousPlay = onToggleContinuousPlay
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
    activeCycleWeek: Int,
    onSelect: () -> Unit,
    onPlay: () -> Unit = onSelect,
    onToggleComplete: (Boolean) -> Unit,
    onOverloadChange: (Float, Int, Int, String) -> Unit = { _, _, _, _ -> },
    onApplyUpgrade: (Float) -> Unit = {}
) {
    val borderColor = if (isActive) ElectricCyan else DarkSurfaceBorder
    val bgColor = if (isActive) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.6f)
    val profile = ProgressiveOverloadEngine.getProfile(segment.id)
    val phase = MesocyclePhase.fromWeek(activeCycleWeek)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generous 44dp hit target for completing/uncompleting segment
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleComplete(!segment.isCompleted) },
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = segment.isCompleted,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonLime,
                            checkmarkColor = DarkBackground,
                            uncheckedColor = TextMuted
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect() }
                ) {
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
                    onClick = onPlay,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play segment",
                        tint = if (isActive) ElectricCyan else TextSecondary
                    )
                }
            }

            // Target Prescription Badge for Active Week
            if (profile != null && profile.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground.copy(alpha = 0.9f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    profile.exercises.forEach { ex ->
                        val targetRep = ex.getTargetForWeek(activeCycleWeek)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (profile.exercises.size > 1) "• ${ex.exerciseName}" else ex.exerciseName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Target: $targetRep @ %.1f kg • %s".format(segment.weightUsedKg, phase.tempoCode),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonLime,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // 3-Signal "WEIGHT UPGRADE UNLOCKED!" Alert Banner
            if (segment.isUpgradeUnlocked && segment.upgradeDetails != null) {
                val upgrade = segment.upgradeDetails
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberGold.copy(alpha = 0.15f))
                        .border(1.5.dp, AmberGold, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Upgrade Unlocked",
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WEIGHT UPGRADE UNLOCKED!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You hit ${segment.set3RepsAchieved} clean reps on Set 3! Move up from %.1f kg -> %.1f kg next session and reset to Week 1 Tempo (%s slow 3-1-1 reps).".format(
                                upgrade.currentWeightKg,
                                upgrade.nextWeightKg,
                                upgrade.w1RepRange
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberGold)
                                .clickable {
                                    onApplyUpgrade(upgrade.nextWeightKg)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Upgrade to %.1f kg & Reset to Week 1".format(upgrade.nextWeightKg),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBackground,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            } else if (segment.isOverloadReady) {
                // Inline Overload Coach Badge (Triggered when last 3 logs are rated "Easy")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberGold.copy(alpha = 0.12f))
                        .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Overload Ready",
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Overload Ready: Increase reps by +2, use a 4s eccentric tempo, or step up dumbbell weight.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AmberGold,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }

            // Progressive Overload Adjustment Controls (When Completed)
            if (segment.isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground.copy(alpha = 0.7f))
                        .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Weight Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Weight:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.5.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .clickable {
                                    val newW = (segment.weightUsedKg - 0.5f).coerceAtLeast(0f)
                                    onOverloadChange(newW, segment.repsPerSet, segment.set3RepsAchieved, segment.effortRating)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.1f kg".format(segment.weightUsedKg),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .clickable {
                                    val newW = segment.weightUsedKg + 0.5f
                                    onOverloadChange(newW, segment.repsPerSet, segment.set3RepsAchieved, segment.effortRating)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // Set 3 Reps Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Set 3 Reps:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.5.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .clickable {
                                    val newR = (segment.set3RepsAchieved - 1).coerceAtLeast(1)
                                    onOverloadChange(segment.weightUsedKg, newR, newR, segment.effortRating)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${segment.set3RepsAchieved}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonLime,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .clickable {
                                    val newR = segment.set3RepsAchieved + 1
                                    onOverloadChange(segment.weightUsedKg, newR, newR, segment.effortRating)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Effort Rating Chips Row with RIR labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Effort:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.5.sp)
                    listOf(
                        "Easy" to "Easy (3+ RIR)",
                        "Good Burn" to "Good Burn (1-2 RIR)",
                        "Near Failure" to "Near Failure (0 RIR)"
                    ).forEach { (ratingKey, label) ->
                        val isSelected = segment.effortRating.equals(ratingKey, ignoreCase = true)
                        val chipBg = when {
                            isSelected && ratingKey == "Easy" -> AmberGold.copy(alpha = 0.25f)
                            isSelected && ratingKey == "Good Burn" -> NeonLime.copy(alpha = 0.25f)
                            isSelected && ratingKey == "Near Failure" -> Color(0xFFFF5252).copy(alpha = 0.25f)
                            else -> DarkBackground
                        }
                        val chipBorder = when {
                            isSelected && ratingKey == "Easy" -> AmberGold
                            isSelected && ratingKey == "Good Burn" -> NeonLime
                            isSelected && ratingKey == "Near Failure" -> Color(0xFFFF5252)
                            else -> DarkSurfaceBorder
                        }
                        val chipText = when {
                            isSelected && ratingKey == "Easy" -> AmberGold
                            isSelected && ratingKey == "Good Burn" -> NeonLime
                            isSelected && ratingKey == "Near Failure" -> Color(0xFFFF5252)
                            else -> TextSecondary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    onOverloadChange(segment.weightUsedKg, segment.repsPerSet, segment.set3RepsAchieved, ratingKey)
                                }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
