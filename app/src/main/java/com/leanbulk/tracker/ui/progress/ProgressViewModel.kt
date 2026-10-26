package com.leanbulk.tracker.ui.progress

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity
import com.leanbulk.tracker.data.model.AdaptiveCoachEngine
import com.leanbulk.tracker.data.model.AdaptiveStatusInfo
import com.leanbulk.tracker.data.model.BodyDimensionCalculator
import com.leanbulk.tracker.data.model.BodyForecastResult
import com.leanbulk.tracker.data.model.CoachingInsight
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.data.model.HomeGymInventory
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.model.WeightEntry
import com.leanbulk.tracker.data.repository.UserProfileRepository
import com.leanbulk.tracker.data.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProgressUiState(
    val userProfile: UserProfile = UserProfile(),
    val forecast: BodyForecastResult = BodyDimensionCalculator.calculate(UserProfile()),
    val entries: List<WeightEntry> = emptyList(),
    val coachingInsight: CoachingInsight = CoachingInsight.evaluate(emptyList()),
    val showLogDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val adaptationLogs: List<AdaptationLogEntity> = emptyList(),
    val adaptiveStatus: AdaptiveStatusInfo = AdaptiveCoachEngine.extractStatusInfo(UserProfile(), emptyList()),
    val isEvaluatingAdaptation: Boolean = false,
    val showInventoryDialog: Boolean = false,
    val inventory: HomeGymInventory = HomeGymInventory()
) {
    val latestEntry: WeightEntry?
        get() = entries.maxByOrNull { it.date }

    val currentWeightKg: Float
        get() = latestEntry?.weightKg ?: userProfile.currentWeightKg

    val currentWaistInches: Float
        get() = latestEntry?.waistInches ?: userProfile.currentWaistInches
}

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val weightRepository = WeightRepository(db.weightDao())
    private val profileRepository = UserProfileRepository(db.userProfileDao())

    private val _showLogDialog = MutableStateFlow(false)
    private val _showEditProfileDialog = MutableStateFlow(false)
    private val _isEvaluatingAdaptation = MutableStateFlow(false)
    private val _showInventoryDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                profileRepository.ensureDefaultProfileSeeded()
                weightRepository.removeSampleData()
                AdaptiveCoachEngine.evaluateAndAdapt(db, application, force = false)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error initializing progress data", e)
            }
        }
    }

    private val dialogsFlow = combine(_showLogDialog, _showEditProfileDialog, _isEvaluatingAdaptation, _showInventoryDialog) { showLog, showEdit, isEval, showInv ->
        Quadruple(showLog, showEdit, isEval, showInv)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    val uiState: StateFlow<ProgressUiState> = combine(
        profileRepository.getUserProfile(),
        weightRepository.getAllEntries(),
        db.adaptationLogDao().getAllLogs(),
        dialogsFlow
    ) { profile, entries, logs, dialogs ->
        val (showLog, showEditProfile, isEvaluating, showInventory) = dialogs
        val inventory = HomeGymInventory(
            has4kgDumbbell = profile.eqHas4kgDumbbell,
            hasWeightedBackpack = profile.eqHasWeightedBackpack,
            hasSturdyTableOrBed = profile.eqHasSturdyTableOrBed,
            hasAdjustableDumbbell = profile.eqHasAdjustableDumbbell,
            hasBarbell = profile.eqHasBarbell,
            hasPullUpBar = profile.eqHasPullUpBar,
            hasResistanceBand = profile.eqHasResistanceBand
        )
        val latest = entries.maxByOrNull { it.date }
        val effectiveProfile = profile.copy(
            currentWeightKg = latest?.weightKg ?: profile.currentWeightKg,
            currentWaistInches = latest?.waistInches ?: profile.currentWaistInches
        )
        val forecast = BodyDimensionCalculator.calculate(effectiveProfile)
        val insight = CoachingInsight.evaluate(entries)

        val rawWeightEntities = entries.map {
            WeightProgressEntity(
                id = it.id,
                date = it.date.toString(),
                weightKg = it.weightKg,
                waistInches = it.waistInches,
                notes = it.notes
            )
        }
        val statusInfo = AdaptiveCoachEngine.extractStatusInfo(effectiveProfile, rawWeightEntities)

        ProgressUiState(
            userProfile = effectiveProfile,
            forecast = forecast,
            entries = entries,
            coachingInsight = insight,
            showLogDialog = showLog,
            showEditProfileDialog = showEditProfile,
            adaptationLogs = logs,
            adaptiveStatus = statusInfo,
            isEvaluatingAdaptation = isEvaluating,
            showInventoryDialog = showInventory,
            inventory = inventory
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProgressUiState()
    )

    fun openLogDialog() {
        _showLogDialog.value = true
    }

    fun closeLogDialog() {
        _showLogDialog.value = false
    }

    fun openEditProfileDialog() {
        _showEditProfileDialog.value = true
    }

    fun closeEditProfileDialog() {
        _showEditProfileDialog.value = false
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(profile)
                closeEditProfileDialog()
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error updating profile", e)
            }
        }
    }

    fun toggleAutoAdapt(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updated = uiState.value.userProfile.copy(autoAdaptEnabled = enabled)
                profileRepository.updateProfile(updated)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error toggling auto adapt", e)
            }
        }
    }

    fun toggleBellyFatRecompMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updated = uiState.value.userProfile.copy(bellyFatRecompMode = enabled)
                profileRepository.updateProfile(updated)
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error toggling recomp mode", e)
            }
        }
    }

    fun runAdaptationCheckNow() {
        viewModelScope.launch {
            _isEvaluatingAdaptation.value = true
            try {
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = true)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error evaluating adaptation", e)
            } finally {
                _isEvaluatingAdaptation.value = false
            }
        }
    }

    fun logWeight(date: LocalDate, weightKg: Float, waistInches: Float, notes: String) {
        viewModelScope.launch {
            try {
                weightRepository.logWeight(date, weightKg, waistInches, notes)
                profileRepository.updateCurrentWeight(weightKg)
                profileRepository.updateCurrentWaist(waistInches)
                closeLogDialog()
                // Auto-evaluate immediately after any new weigh-in!
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error logging weight", e)
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try {
                weightRepository.deleteEntry(id)
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error deleting entry", e)
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
                Log.e("ProgressViewModel", "Error updating inventory", e)
            }
        }
    }

}
