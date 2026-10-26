package com.leanbulk.tracker.ui.workout

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.AdaptiveCoachEngine
import com.leanbulk.tracker.data.model.BellyFatCircuitCatalog
import com.leanbulk.tracker.data.model.DayWorkoutPlan
import com.leanbulk.tracker.data.model.MesocyclePhase
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.model.WorkoutSegment
import com.leanbulk.tracker.data.model.HomeGymInventory
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
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
    val activeCycleWeek: Int = 1,
    val isShifted: Boolean = false,
    val shiftedFromDay: DayOfWeek? = null,
    val isDeloadRecommended: Boolean = false,
    val inventory: HomeGymInventory = HomeGymInventory(),
    val showInventoryDialog: Boolean = false,
    val bellyFatRecompMode: Boolean = true,
    val bellyWorkoutVariant: String = "AUTO",
    val finisherSegments: List<WorkoutSegment> = emptyList(),
    val goalMode: String = "LEAN_BULK"
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

private data class AdaptiveWorkoutState(
    val isShifted: Boolean,
    val shiftedFromDay: DayOfWeek?,
    val isDeloadRecommended: Boolean
)

private data class WorkoutMeta(
    val day: DayOfWeek,
    val activeSegId: String?,
    val completedDates: List<String>,
    val streak: Int,
    val cycleWeek: Int,
    val isShifted: Boolean,
    val shiftedFromDay: DayOfWeek?,
    val isDeloadRecommended: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = WorkoutRepository(db.workoutDao())

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek)
    private val _activeSegmentId = MutableStateFlow<String?>(null)
    private val _activeCycleWeek = MutableStateFlow(1)
    private val _isShifted = MutableStateFlow(false)
    private val _shiftedFromDay = MutableStateFlow<DayOfWeek?>(null)
    private val _isDeloadRecommended = MutableStateFlow(false)
    private val _showInventoryDialog = MutableStateFlow(false)
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

        // Loop C: Check Missed Workout Auto-Shift and Deload Condition
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val adaptiveDay = AdaptiveCoachEngine.getAdaptivePlanDay(db, today)
                if (adaptiveDay != today.dayOfWeek) {
                    _selectedDay.value = adaptiveDay
                    _shiftedFromDay.value = adaptiveDay
                    _isShifted.value = true
                }

                // Check 2 consecutive near-failure failed rep deload
                val recentLogs = db.workoutDao().getAllWorkoutLogsSync()
                    .filter { it.isCompleted && it.completedAtMillis > 0 }
                    .sortedByDescending { it.completedAtMillis }
                val failedCount = recentLogs.take(2).count {
                    it.effortRating == "Near Failure" && it.set3RepsAchieved < it.repsPerSet
                }
                if (failedCount >= 2) {
                    _isDeloadRecommended.value = true
                }

                AdaptiveCoachEngine.evaluateAndAdapt(db, application, force = false)
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error in workout adaptive evaluation", e)
            }
        }
    }

    private val adaptiveFlow = combine(_isShifted, _shiftedFromDay, _isDeloadRecommended) { isShifted, shifted, isDeload ->
        AdaptiveWorkoutState(isShifted, shifted, isDeload)
    }

    val uiState: StateFlow<WorkoutUiState> = combine(
        _selectedDay,
        _activeSegmentId,
        repository.getCompletedDates(),
        repository.getStreakFlow(),
        combine(_activeCycleWeek, adaptiveFlow) { week, adaptive -> week to adaptive }
    ) { day, activeSegId, completedDates, streak, (cycleWeek, adaptive) ->
        WorkoutMeta(
            day = day,
            activeSegId = activeSegId,
            completedDates = completedDates,
            streak = streak,
            cycleWeek = cycleWeek,
            isShifted = adaptive.isShifted,
            shiftedFromDay = adaptive.shiftedFromDay,
            isDeloadRecommended = adaptive.isDeloadRecommended
        )
    }.flatMapLatest { meta ->
        combine(
            repository.getPlanWithStatus(LocalDate.now(), meta.day, meta.cycleWeek),
            db.workoutDao().getWorkoutLogsForDate(LocalDate.now().toString()),
            db.userProfileDao().getUserProfileFlow(),
            _showInventoryDialog
        ) { planWithStatus, workoutLogs, profileEntity, showDialog ->
            val inventory = if (profileEntity != null) {
                HomeGymInventory(
                    has4kgDumbbell = profileEntity.eqHas4kgDumbbell,
                    hasWeightedBackpack = profileEntity.eqHasWeightedBackpack,
                    hasSturdyTableOrBed = profileEntity.eqHasSturdyTableOrBed,
                    hasAdjustableDumbbell = profileEntity.eqHasAdjustableDumbbell,
                    hasBarbell = profileEntity.eqHasBarbell,
                    hasPullUpBar = profileEntity.eqHasPullUpBar,
                    hasResistanceBand = profileEntity.eqHasResistanceBand
                )
            } else {
                HomeGymInventory()
            }

            val isCutting = profileEntity?.resolvedGoalMode == "WEIGHT_LOSS"
            val recompMode = isCutting || (profileEntity?.bellyFatRecompMode ?: true)
            val variant = profileEntity?.bellyWorkoutVariant ?: "AUTO"
            val logMap = workoutLogs.associateBy { it.segmentId }
            val rawFinisher = BellyFatCircuitCatalog.getSegments(variant, meta.day)
            val finisherWithStatus = rawFinisher.map { seg ->
                seg.copy(isCompleted = logMap[seg.id]?.isCompleted ?: false)
            }

            val allAvailableSegments = planWithStatus.segments + finisherWithStatus
            val activeSeg = allAvailableSegments.find { it.id == meta.activeSegId }
                ?: planWithStatus.segments.firstOrNull { !it.isCompleted }
                ?: planWithStatus.segments.firstOrNull()

            WorkoutUiState(
                selectedDay = meta.day,
                selectedDate = LocalDate.now(),
                plan = planWithStatus,
                activeSegment = activeSeg,
                completedDates = meta.completedDates,
                streak = meta.streak,
                activeCycleWeek = meta.cycleWeek,
                isShifted = meta.isShifted,
                shiftedFromDay = meta.shiftedFromDay,
                isDeloadRecommended = meta.isDeloadRecommended,
                inventory = inventory,
                showInventoryDialog = showDialog,
                bellyFatRecompMode = recompMode,
                bellyWorkoutVariant = variant,
                finisherSegments = finisherWithStatus,
                goalMode = profileEntity?.resolvedGoalMode ?: "LEAN_BULK"
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

    fun resetToNaturalDay() {
        val today = LocalDate.now().dayOfWeek
        _selectedDay.value = today
        _isShifted.value = false
        val plan = WorkoutDataSource.getPlanForDay(today)
        _activeSegmentId.value = plan.segments.firstOrNull()?.id
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
                // Auto-evaluate closed-loop coaching (chronotype & adherence updates)
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
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
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)

                // Advance to next uncompleted segment if available
                val currentPlan = uiState.value.plan
                val finisherSegments = uiState.value.finisherSegments
                if (segment.id.startsWith("bf_seg_")) {
                    val currentIndex = finisherSegments.indexOfFirst { it.id == segment.id }
                    if (currentIndex >= 0 && currentIndex < finisherSegments.size - 1) {
                        val nextFinisher = finisherSegments[currentIndex + 1]
                        _activeSegmentId.value = nextFinisher.id
                    }
                } else {
                    val currentIndex = currentPlan.segments.indexOfFirst { it.id == segment.id }
                    if (currentIndex >= 0 && currentIndex < currentPlan.segments.size - 1) {
                        val nextSegment = currentPlan.segments[currentIndex + 1]
                        _activeSegmentId.value = nextSegment.id
                    }
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error finishing segment", e)
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
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
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
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error applying weight upgrade", e)
            }
        }
    }

    fun openInventoryDialog() {
        _showInventoryDialog.value = true
    }

    fun closeInventoryDialog() {
        _showInventoryDialog.value = false
    }

    fun updateInventory(inventory: HomeGymInventory) {
        viewModelScope.launch {
            try {
                val entity = db.userProfileDao().getUserProfileSync() ?: UserProfileEntity()
                db.userProfileDao().upsertUserProfile(
                    entity.copy(
                        eqHas4kgDumbbell = inventory.has4kgDumbbell,
                        eqHasWeightedBackpack = inventory.hasWeightedBackpack,
                        eqHasSturdyTableOrBed = inventory.hasSturdyTableOrBed,
                        eqHasAdjustableDumbbell = inventory.hasAdjustableDumbbell,
                        eqHasBarbell = inventory.hasBarbell,
                        eqHasPullUpBar = inventory.hasPullUpBar,
                        eqHasResistanceBand = inventory.hasResistanceBand
                    )
                )
                closeInventoryDialog()
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error updating inventory", e)
            }
        }
    }

    fun onMetronomeSetFinished(segment: WorkoutSegment, completedReps: Int) {
        Log.d("WorkoutViewModel", "Metronome set finished for ${segment.id}: achieved $completedReps reps")
        updateOverload(
            segment = segment,
            weightKg = segment.weightUsedKg,
            reps = segment.repsPerSet,
            set3Reps = completedReps,
            effortRating = segment.effortRating
        )
    }

    fun swapToBarbell(segment: WorkoutSegment, barbellWeightKg: Float) {
        Log.d("WorkoutViewModel", "Swapping ${segment.id} to Barbell weight: $barbellWeightKg kg")
        updateOverload(
            segment = segment,
            weightKg = barbellWeightKg,
            reps = segment.repsPerSet,
            set3Reps = segment.set3RepsAchieved,
            effortRating = segment.effortRating
        )
    }

    fun toggleBellyFatRecompMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val entity = db.userProfileDao().getUserProfileSync() ?: UserProfileEntity()
                db.userProfileDao().upsertUserProfile(entity.copy(bellyFatRecompMode = enabled))
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error toggling recomp mode", e)
            }
        }
    }

    fun setBellyWorkoutVariant(variant: String) {
        viewModelScope.launch {
            try {
                val entity = db.userProfileDao().getUserProfileSync() ?: UserProfileEntity()
                db.userProfileDao().upsertUserProfile(entity.copy(bellyWorkoutVariant = variant))
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error setting belly workout variant", e)
            }
        }
    }

}
