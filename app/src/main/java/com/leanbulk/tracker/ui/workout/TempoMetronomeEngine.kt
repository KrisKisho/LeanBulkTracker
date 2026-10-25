package com.leanbulk.tracker.ui.workout

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TempoPhase {
    IDLE,
    LOWER,   // Eccentric
    HOLD,    // Peak/Stretch Isometric
    LIFT,    // Concentric
    PULSE,   // Halfway Burnout Pulse (Week 4 final 10s)
    FINISHED
}

data class MetronomeState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isMuted: Boolean = false,
    val durationSeconds: Int = 40,
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int = 40,
    val currentRep: Int = 0,
    val targetReps: Int = 10,
    val phase: TempoPhase = TempoPhase.IDLE,
    val phaseCount: Int = 0, // e.g., 3, 2, 1 for lower
    val tensionFraction: Float = 1.0f // 1.0 = fully contracted/starting position, 0.2 = bottom stretch
)

class TempoMetronomeEngine(
    private val scope: CoroutineScope
) {
    private var toneGenerator: ToneGenerator? = null
    private var metronomeJob: Job? = null

    private val _state = MutableStateFlow(MetronomeState())
    val state: StateFlow<MetronomeState> = _state.asStateFlow()

    init {
        initToneGenerator()
    }

    private fun initToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (e: Exception) {
            Log.w("TempoMetronome", "Failed to init ToneGenerator: ${e.message}")
        }
    }

    fun toggleMute() {
        _state.value = _state.value.copy(isMuted = !_state.value.isMuted)
    }

    fun setDuration(seconds: Int) {
        if (!_state.value.isRunning) {
            _state.value = _state.value.copy(
                durationSeconds = seconds,
                remainingSeconds = seconds
            )
        }
    }

    fun startSet(progressionWeek: Int, durationSeconds: Int = 40, onFinished: ((Int) -> Unit)? = null) {
        metronomeJob?.cancel()

        val targetReps = when (progressionWeek) {
            1 -> (durationSeconds / 5).coerceAtLeast(1)  // 3-1-1 = 5s/rep -> 8 reps for 40s
            2 -> (durationSeconds / 4).coerceAtLeast(1)  // 2-1-1 = 4s/rep -> 10 reps for 40s
            3 -> (durationSeconds / 3).coerceAtLeast(1)  // 2-0-1 = 3s/rep -> 13 reps for 40s
            4 -> 10 + 10 // 30s controlled (10 reps) + 10s pulse (10 pulses) = 20 reps
            else -> 10
        }

        _state.value = _state.value.copy(
            isRunning = true,
            isPaused = false,
            durationSeconds = durationSeconds,
            elapsedSeconds = 0,
            remainingSeconds = durationSeconds,
            currentRep = 1,
            targetReps = targetReps,
            phase = TempoPhase.LOWER,
            phaseCount = if (progressionWeek == 1) 3 else 2,
            tensionFraction = 1.0f
        )

        metronomeJob = scope.launch(Dispatchers.Default) {
            runMetronomeLoop(progressionWeek, durationSeconds, onFinished)
        }
    }

    fun pause() {
        metronomeJob?.cancel()
        _state.value = _state.value.copy(isRunning = false, isPaused = true)
    }

    fun resume(progressionWeek: Int, onFinished: ((Int) -> Unit)? = null) {
        if (_state.value.isPaused) {
            val remaining = _state.value.remainingSeconds
            val duration = _state.value.durationSeconds
            _state.value = _state.value.copy(isRunning = true, isPaused = false)
            metronomeJob = scope.launch(Dispatchers.Default) {
                runMetronomeLoop(progressionWeek, duration, onFinished, startFromElapsed = duration - remaining)
            }
        }
    }

    fun reset() {
        metronomeJob?.cancel()
        val duration = _state.value.durationSeconds
        _state.value = MetronomeState(
            isRunning = false,
            isPaused = false,
            isMuted = _state.value.isMuted,
            durationSeconds = duration,
            elapsedSeconds = 0,
            remainingSeconds = duration,
            currentRep = 0,
            targetReps = _state.value.targetReps,
            phase = TempoPhase.IDLE,
            tensionFraction = 1.0f
        )
    }

    private suspend fun runMetronomeLoop(
        progressionWeek: Int,
        totalDuration: Int,
        onFinished: ((Int) -> Unit)?,
        startFromElapsed: Int = 0
    ) {
        var elapsed = startFromElapsed
        var currentRep = (elapsed / when (progressionWeek) {
            1 -> 5
            2 -> 4
            3 -> 3
            else -> 3
        }).coerceAtLeast(1)

        val repCycleDuration = when (progressionWeek) {
            1 -> 5
            2 -> 4
            3 -> 3
            else -> 3
        }

        while (elapsed < totalDuration) {
            val isFinalPulses = progressionWeek == 4 && elapsed >= (totalDuration - 10)

            if (isFinalPulses) {
                // Week 4 Halfway Burnout Pulses (0.5s down / 0.5s up = 1s per pulse)
                val pulseNum = 10 + (elapsed - (totalDuration - 10)) + 1
                currentRep = pulseNum

                // Down half
                _state.value = _state.value.copy(
                    elapsedSeconds = elapsed,
                    remainingSeconds = totalDuration - elapsed,
                    currentRep = currentRep,
                    phase = TempoPhase.PULSE,
                    phaseCount = 1,
                    tensionFraction = 0.40f
                )
                playTone(ToneType.TICK)
                delay(500)

                // Up half
                _state.value = _state.value.copy(
                    tensionFraction = 0.60f
                )
                playTone(ToneType.LIFT)
                delay(500)

                elapsed++
            } else {
                // Normal Rep Cadence
                val secondInRep = elapsed % repCycleDuration
                currentRep = (elapsed / repCycleDuration) + 1

                when (progressionWeek) {
                    1 -> {
                        // Week 1: 3-1-1 (5s total)
                        // sec 0: lower 3, sec 1: lower 2, sec 2: lower 1, sec 3: hold 1, sec 4: lift 1
                        when (secondInRep) {
                            0 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 3, tensionFraction = 0.8f)
                                playTone(ToneType.TICK)
                            }
                            1 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 2, tensionFraction = 0.5f)
                                playTone(ToneType.TICK)
                            }
                            2 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 1, tensionFraction = 0.2f)
                                playTone(ToneType.TICK)
                            }
                            3 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.HOLD, phaseCount = 1, tensionFraction = 0.2f)
                                playTone(ToneType.HOLD)
                            }
                            4 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LIFT, phaseCount = 1, tensionFraction = 1.0f)
                                playTone(ToneType.LIFT)
                            }
                        }
                    }
                    2 -> {
                        // Week 2: 2-1-1 (4s total)
                        // sec 0: lower 2, sec 1: lower 1, sec 2: hold 1, sec 3: lift 1
                        when (secondInRep) {
                            0 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 2, tensionFraction = 0.6f)
                                playTone(ToneType.TICK)
                            }
                            1 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 1, tensionFraction = 0.2f)
                                playTone(ToneType.TICK)
                            }
                            2 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.HOLD, phaseCount = 1, tensionFraction = 0.2f)
                                playTone(ToneType.HOLD)
                            }
                            3 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LIFT, phaseCount = 1, tensionFraction = 1.0f)
                                playTone(ToneType.LIFT)
                            }
                        }
                    }
                    else -> {
                        // Week 3 & Week 4 (first 30s): 2-0-1 (3s total)
                        // sec 0: lower 2, sec 1: lower 1, sec 2: lift 1
                        when (secondInRep) {
                            0 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 2, tensionFraction = 0.5f)
                                playTone(ToneType.TICK)
                            }
                            1 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LOWER, phaseCount = 1, tensionFraction = 0.2f)
                                playTone(ToneType.TICK)
                            }
                            2 -> {
                                _state.value = _state.value.copy(phase = TempoPhase.LIFT, phaseCount = 1, tensionFraction = 1.0f)
                                playTone(ToneType.LIFT)
                            }
                        }
                    }
                }

                _state.value = _state.value.copy(
                    elapsedSeconds = elapsed,
                    remainingSeconds = totalDuration - elapsed,
                    currentRep = currentRep
                )

                delay(1000)
                elapsed++
            }
        }

        // Set finished
        playTone(ToneType.FANFARE)
        val finalReps = currentRep
        _state.value = _state.value.copy(
            isRunning = false,
            isPaused = false,
            elapsedSeconds = totalDuration,
            remainingSeconds = 0,
            phase = TempoPhase.FINISHED,
            tensionFraction = 1.0f
        )
        onFinished?.invoke(finalReps)
    }

    private enum class ToneType { TICK, HOLD, LIFT, FANFARE }

    private fun playTone(type: ToneType) {
        if (_state.value.isMuted) return
        try {
            when (type) {
                ToneType.TICK -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
                ToneType.HOLD -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                ToneType.LIFT -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                ToneType.FANFARE -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 350)
            }
        } catch (e: Exception) {
            Log.w("TempoMetronome", "Failed to play tone: ${e.message}")
        }
    }

    fun release() {
        metronomeJob?.cancel()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
