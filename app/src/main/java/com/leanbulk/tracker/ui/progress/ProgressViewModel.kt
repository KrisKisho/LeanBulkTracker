package com.leanbulk.tracker.ui.progress

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.BodyDimensionCalculator
import com.leanbulk.tracker.data.model.BodyForecastResult
import com.leanbulk.tracker.data.model.CoachingInsight
import com.leanbulk.tracker.data.model.UserProfile
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
    val showEditProfileDialog: Boolean = false
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

    init {
        viewModelScope.launch {
            try {
                profileRepository.ensureDefaultProfileSeeded()
                weightRepository.seedInitialBaselineIfEmpty()
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error seeding baseline data", e)
            }
        }
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        profileRepository.getUserProfile(),
        weightRepository.getAllEntries(),
        _showLogDialog,
        _showEditProfileDialog
    ) { profile, entries, showLog, showEditProfile ->
        val latest = entries.maxByOrNull { it.date }
        val effectiveProfile = profile.copy(
            currentWeightKg = latest?.weightKg ?: profile.currentWeightKg,
            currentWaistInches = latest?.waistInches ?: profile.currentWaistInches
        )
        val forecast = BodyDimensionCalculator.calculate(effectiveProfile)
        val insight = CoachingInsight.evaluate(entries)

        ProgressUiState(
            userProfile = effectiveProfile,
            forecast = forecast,
            entries = entries,
            coachingInsight = insight,
            showLogDialog = showLog,
            showEditProfileDialog = showEditProfile
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
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error updating profile", e)
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
}
