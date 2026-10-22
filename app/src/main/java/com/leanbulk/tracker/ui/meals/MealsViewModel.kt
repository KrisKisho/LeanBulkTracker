package com.leanbulk.tracker.ui.meals

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.health.HealthSyncData
import com.leanbulk.tracker.data.health.HealthSyncManager
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.model.DynamicNutritionEngine
import com.leanbulk.tracker.data.model.DynamicNutritionTarget
import com.leanbulk.tracker.data.model.MealDataSource
import com.leanbulk.tracker.data.model.MealItem
import com.leanbulk.tracker.data.model.SouthIndianTopUp
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.repository.MealRepository
import com.leanbulk.tracker.data.repository.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class MealsUiState(
    val date: LocalDate = LocalDate.now(),
    val meals: List<MealItem> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val syncData: HealthSyncData = HealthSyncData(),
    val nutritionTarget: DynamicNutritionTarget = DynamicNutritionEngine.calculate(UserProfile(), 4500, 135),
    val showAddDialog: Boolean = false
) {
    val targetCalories: Int
        get() = nutritionTarget.dynamicCalorieTarget

    val targetProtein: Int
        get() = nutritionTarget.proteinTargetGrams

    val targetCarbs: Int
        get() = nutritionTarget.carbTargetGrams

    val targetFat: Int
        get() = nutritionTarget.fatTargetGrams

    val consumedCalories: Int
        get() = meals.filter { it.isChecked }.sumOf { it.activeCalories }

    val consumedProtein: Int
        get() = meals.filter { it.isChecked }.sumOf { it.activeProtein }

    val consumedCarbs: Int
        get() = meals.filter { it.isChecked }.sumOf { it.activeCarbs }

    val consumedFat: Int
        get() = meals.filter { it.isChecked }.sumOf { it.activeFat }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MealsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val mealRepository = MealRepository(db.mealDao())
    private val profileRepository = UserProfileRepository(db.userProfileDao())
    private val healthSyncManager = HealthSyncManager(application)
    private val workoutDao = db.workoutDao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _showAddDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                profileRepository.ensureDefaultProfileSeeded()
                mealRepository.ensureDefaultMealsSeeded(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error seeding meals or profile", e)
            }
        }
    }

    val uiState: StateFlow<MealsUiState> = combine(
        _selectedDate,
        _showAddDialog,
        profileRepository.getUserProfile(),
        healthSyncManager.syncData
    ) { date, showDialog, profile, syncData ->
        MetaState(date, showDialog, profile, syncData)
    }.flatMapLatest { meta ->
        combine(
            mealRepository.getMealsForDate(meta.date),
            workoutDao.getWorkoutLogsForDate(meta.date.toString())
        ) { meals, workoutLogs ->
            val dayPlan = WorkoutDataSource.getWeeklyPlans()[meta.date.dayOfWeek]
            val totalSegments = dayPlan?.segments?.size ?: 5
            val completedSegments = workoutLogs.count { it.isCompleted }

            val nutritionTarget = DynamicNutritionEngine.calculate(
                profile = meta.profile,
                dailySteps = meta.syncData.dailySteps,
                activeCaloriesBurned = meta.syncData.activeCaloriesBurned,
                completedWorkoutSegmentsCount = completedSegments,
                totalSegmentsForDay = totalSegments,
                dayOfWeek = meta.date.dayOfWeek,
                currentTime = if (meta.date == LocalDate.now()) LocalTime.now() else LocalTime.of(23, 59),
                includePlannedWorkoutInTarget = true
            )

            MealsUiState(
                date = meta.date,
                meals = meals,
                userProfile = meta.profile,
                syncData = meta.syncData,
                nutritionTarget = nutritionTarget,
                showAddDialog = meta.showDialog
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MealsUiState()
    )

    private data class MetaState(
        val date: LocalDate,
        val showDialog: Boolean,
        val profile: UserProfile,
        val syncData: HealthSyncData
    )

    fun syncHealthNow() {
        val currentWeight = uiState.value.userProfile.currentWeightKg
        healthSyncManager.refreshSync(currentWeight)
    }

    val healthConnectPermissions: Set<String>
        get() = healthSyncManager.permissions

    fun onStepPermissionGranted() {
        val currentWeight = uiState.value.userProfile.currentWeightKg
        healthSyncManager.onPermissionGranted(currentWeight)
    }

    fun onHealthConnectPermissionsResult(granted: Set<String>) {
        val currentWeight = uiState.value.userProfile.currentWeightKg
        healthSyncManager.refreshSync(currentWeight)
    }

    fun addStepBonus(steps: Int) {
        val currentWeight = uiState.value.userProfile.currentWeightKg
        healthSyncManager.addSteps(steps, currentWeight)
    }

    fun addTopUpMeal(topUp: SouthIndianTopUp) {
        viewModelScope.launch {
            try {
                mealRepository.addCustomMeal(
                    date = _selectedDate.value,
                    name = topUp.title,
                    calories = topUp.calories,
                    protein = topUp.protein,
                    carbs = topUp.carbs,
                    fat = topUp.fat
                )
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error adding top up meal", e)
            }
        }
    }

    fun toggleMealChecked(meal: MealItem, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                mealRepository.toggleMealChecked(_selectedDate.value, meal, isChecked)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error toggling meal checked", e)
            }
        }
    }

    fun toggleVegOption(meal: MealItem, isVeg: Boolean) {
        viewModelScope.launch {
            try {
                mealRepository.toggleVegOption(_selectedDate.value, meal, isVeg)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error toggling veg option", e)
            }
        }
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun addPresetMeal(preset: MealDataSource.QuickPreset) {
        viewModelScope.launch {
            try {
                mealRepository.addCustomMeal(
                    date = _selectedDate.value,
                    name = preset.label,
                    calories = preset.calories,
                    protein = preset.protein,
                    carbs = preset.carbs,
                    fat = preset.fat
                )
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error adding preset meal", e)
            }
        }
    }

    fun addCustomMeal(name: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            try {
                mealRepository.addCustomMeal(
                    date = _selectedDate.value,
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error adding custom meal", e)
            }
        }
    }

    fun deleteCustomMeal(mealId: String) {
        viewModelScope.launch {
            try {
                mealRepository.deleteCustomMeal(_selectedDate.value, mealId)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error deleting custom meal", e)
            }
        }
    }
}
