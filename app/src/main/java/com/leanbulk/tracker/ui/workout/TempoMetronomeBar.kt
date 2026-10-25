package com.leanbulk.tracker.ui.workout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.model.MesocyclePhase
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.CoralRed
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun TempoMetronomeBar(
    segment: WorkoutSegment,
    activeWeek: Int,
    onSetFinished: (completedReps: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val metronomeEngine = remember(segment.id) {
        TempoMetronomeEngine(coroutineScope)
    }

    DisposableEffect(segment.id) {
        onDispose {
            metronomeEngine.release()
        }
    }

    val state by metronomeEngine.state.collectAsState()
    val isFinisher = segment.title.contains("Finisher", ignoreCase = true) ||
            segment.title.contains("Push-Up", ignoreCase = true) ||
            segment.durationSeconds <= 35
    val defaultDuration = if (isFinisher) 30 else 40

    // Animation for Tension Bar Width & Color
    val animatedWidthFraction by animateFloatAsState(
        targetValue = state.tensionFraction.coerceIn(0.15f, 1.0f),
        animationSpec = tween(
            durationMillis = when (state.phase) {
                TempoPhase.LIFT -> 350
                TempoPhase.PULSE -> 450
                else -> 900
            },
            easing = if (state.phase == TempoPhase.LIFT) FastOutSlowInEasing else LinearEasing
        ),
        label = "tensionBarWidth"
    )

    val barColor by animateColorAsState(
        targetValue = when (state.phase) {
            TempoPhase.LOWER -> ElectricCyan
            TempoPhase.HOLD -> AmberGold
            TempoPhase.LIFT -> NeonLime
            TempoPhase.PULSE -> CoralRed
            TempoPhase.FINISHED -> NeonLime
            TempoPhase.IDLE -> ElectricCyan.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "tensionBarColor"
    )

    val phaseLabel = when (state.phase) {
        TempoPhase.LOWER -> "⬇ LOWER: ${state.phaseCount}s"
        TempoPhase.HOLD -> "⏸ HOLD STRETCH: 1s"
        TempoPhase.LIFT -> "⬆ LIFT & SQUEEZE: 1s"
        TempoPhase.PULSE -> "⚡ 10s BURNOUT PULSES"
        TempoPhase.FINISHED -> "🏆 SET COMPLETE!"
        TempoPhase.IDLE -> "Ready: ${MesocyclePhase.fromWeek(activeWeek).tempoCode} Cadence"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBackground.copy(alpha = 0.95f))
            .border(1.dp, barColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Tempo Tag, Duration Toggle & Sound Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Tempo Metronome",
                        tint = barColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEMPO METRONOME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = barColor,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Duration Toggle: 30s / 40s
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        listOf(30, 40).forEach { dur ->
                            val selected = state.durationSeconds == dur
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (selected) ElectricCyan.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable(enabled = !state.isRunning) {
                                        metronomeEngine.setDuration(dur)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${dur}s",
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) ElectricCyan else TextSecondary
                                )
                            }
                        }
                    }

                    // Mute / Unmute Toggle
                    IconButton(
                        onClick = { metronomeEngine.toggleMute() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Metronome Sound",
                            tint = if (state.isMuted) TextSecondary else NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Tension Bar: Expanding / Contracting Animated Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(DarkSurfaceElevated)
                    .border(0.5.dp, DarkSurfaceBorder, RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedWidthFraction)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(barColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live State Indicators: Phase description & Live Rep Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phaseLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = barColor,
                        fontSize = 12.sp
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Set: ${state.remainingSeconds}s left",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rep: #${state.currentRep}/${state.targetReps}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (state.currentRep >= state.targetReps) NeonLime else AmberGold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: [ ▶ Start Metronome ] [ ⏸ Pause ] [ ↺ Reset ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isRunning) {
                    Button(
                        onClick = {
                            if (state.isPaused) {
                                metronomeEngine.resume(activeWeek, onFinished = onSetFinished)
                            } else {
                                val dur = if (state.durationSeconds > 0) state.durationSeconds else defaultDuration
                                metronomeEngine.startSet(activeWeek, dur, onFinished = onSetFinished)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isPaused) "Resume Set" else "Start ${state.durationSeconds}s Set",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { metronomeEngine.pause() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Pause Metronome", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Reset Button
                IconButton(
                    onClick = { metronomeEngine.reset() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
