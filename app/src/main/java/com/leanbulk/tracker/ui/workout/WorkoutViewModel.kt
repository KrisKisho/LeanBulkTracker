package com.leanbulk.tracker.ui.workout

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.DayWorkoutPlan
import com.leanbulk.tracker.data.model.MesocyclePhase
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WorkoutUiState(
    val selectedDay: DayOfWeek = LocalDate.now().dayOfWeek,
    val selectedDate: LocalDate = LocalDate.now(),
    val plan: DayWorkoutPlan = WorkoutDataSource.getPlanForDay(LocalDate.now().dayOfWeek),
    val activeSegment: WorkoutSegment? = null,
    val completedDates: List<String> = emptyList(),
    val streak: Int = 0,
    val activeCycleWeek: Int = 1
) {
    val activeMesocyclePhase: MesocyclePhase
        get() = MesocyclePhase.fromWeek(activeCycleWeek)

    val totalSegments: Int
        get() = plan.segments.size

    val completedSegmentsCount: Int
        get() = plan.segments.count { it.isCompleted }

    val completionPercentage: Float
        get() = if (totalSegments > 0) completedSegmentsCount.toFloat() / totalSegments else 0f
}

private data class WorkoutMeta(
    val day: DayOfWeek,
    val activeSegId: String?,
    val completedDates: List<String>,
    val streak: Int,
    val cycleWeek: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(AppDatabase.getInstance(application).workoutDao())

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek)
    private val _activeSegmentId = MutableStateFlow<String?>(null)
    private val _activeCycleWeek = MutableStateFlow(1)
    private var hasUserOverriddenWeek = false

    init {
        // Auto-select cycle week based on first completed workout date
        viewModelScope.launch {
            repository.getCompletedDates().collect { dates ->
                if (!hasUserOverriddenWeek && dates.isNotEmpty()) {
                    val sorted = dates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.sorted()
                    if (sorted.isNotEmpty()) {
                        val firstDate = sorted.first()
                        val daysSinceStart = ChronoUnit.DAYS.between(firstDate, LocalDate.now()).coerceAtLeast(0)
                        val calculatedWeek = ((daysSinceStart / 7) % 4).toInt() + 1
                        _activeCycleWeek.value = calculatedWeek
                    }
                }
            }
        }
    }

    val uiState: StateFlow<WorkoutUiState> = combine(
        _selectedDay,
        _activeSegmentId,
        repository.getCompletedDates(),
        repository.getStreakFlow(),
        _activeCycleWeek
    ) { day, activeSegId, completedDates, streak, cycleWeek ->
        WorkoutMeta(day, activeSegId, completedDates, streak, cycleWeek)
    }.flatMapLatest { meta ->
        repository.getPlanWithStatus(LocalDate.now(), meta.day, meta.cycleWeek).map { planWithStatus ->
            val activeSeg = planWithStatus.segments.find { it.id == meta.activeSegId }
                ?: planWithStatus.segments.firstOrNull { !it.isCompleted }
                ?: planWithStatus.segments.firstOrNull()

            WorkoutUiState(
                selectedDay = meta.day,
                selectedDate = LocalDate.now(),
                plan = planWithStatus,
                activeSegment = activeSeg,
                completedDates = meta.completedDates,
                streak = meta.streak,
                activeCycleWeek = meta.cycleWeek
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WorkoutUiState()
    )

    fun selectDay(day: DayOfWeek) {
        _selectedDay.value = day
        val plan = WorkoutDataSource.getPlanForDay(day)
        _activeSegmentId.value = plan.segments.firstOrNull()?.id
    }

    fun selectSegment(segment: WorkoutSegment) {
        _activeSegmentId.value = segment.id
    }

    fun selectCycleWeek(week: Int) {
        hasUserOverriddenWeek = true
        _activeCycleWeek.value = week.coerceIn(1, 4)
    }

    fun toggleSegmentCompletion(segment: WorkoutSegment, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleSegmentCompletion(
                    date = LocalDate.now(),
                    segment = segment,
                    isCompleted = isCompleted,
                    activeCycleWeek = _activeCycleWeek.value
                )
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error toggling segment completion", e)
            }
        }
    }

    fun onSegmentFinished(segment: WorkoutSegment) {
        viewModelScope.launch {
            try {
                repository.toggleSegmentCompletion(
                    date = LocalDate.now(),
                    segment = segment,
                    isCompleted = true,
                    activeCycleWeek = _activeCycleWeek.value
                )
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
                val currentPlan = uiState.value.plan
                val currentIndex = currentPlan.segments.indexOfFirst { it.id == segment.id }
                if (currentIndex in 0 until currentPlan.segments.size - 1) {
                    val nextSeg = currentPlan.segments[currentIndex + 1]
                    _activeSegmentId.value = nextSeg.id
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error handling segment finished", e)
            }
        }
    }

    fun updateOverload(segment: WorkoutSegment, weightKg: Float, reps: Int, set3Reps: Int, effortRating: String) {
        Log.d("WorkoutOverload", "updateOverload called for ${segment.id}: weight=$weightKg, reps=$reps, set3Reps=$set3Reps, effort=$effortRating, week=${_activeCycleWeek.value}")
        viewModelScope.launch {
            try {
                repository.updateOverloadData(
                    date = uiState.value.selectedDate,
                    segmentId = segment.id,
                    weightKg = weightKg,
                    reps = reps,
                    set3Reps = set3Reps,
                    effortRating = effortRating,
                    progressionWeek = _activeCycleWeek.value
                )
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error updating overload", e)
            }
        }
    }

    fun applyWeightUpgrade(segment: WorkoutSegment, nextWeightKg: Float) {
        Log.d("WorkoutOverload", "applyWeightUpgrade for ${segment.id} -> $nextWeightKg kg, resetting to Week 1")
        viewModelScope.launch {
            try {
                repository.applyWeightUpgrade(
                    date = uiState.value.selectedDate,
                    segmentId = segment.id,
                    nextWeightKg = nextWeightKg
                )
                _activeCycleWeek.value = 1
                hasUserOverriddenWeek = true
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error applying weight upgrade", e)
            }
        }
    }
}
