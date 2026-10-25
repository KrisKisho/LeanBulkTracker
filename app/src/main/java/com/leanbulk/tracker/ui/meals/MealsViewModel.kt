package com.leanbulk.tracker.ui.meals

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leanbulk.tracker.data.health.HealthSyncData
import com.leanbulk.tracker.data.health.HealthSyncManager
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.local.entity.CustomFoodEntity
import com.leanbulk.tracker.data.local.entity.SavedMealComboEntity
import com.leanbulk.tracker.data.model.AdaptiveCoachEngine
import com.leanbulk.tracker.data.model.AdaptiveMealResizer
import com.leanbulk.tracker.data.model.AdaptiveStatusInfo
import com.leanbulk.tracker.data.model.DynamicNutritionEngine
import com.leanbulk.tracker.data.model.DynamicNutritionTarget
import com.leanbulk.tracker.data.model.MealDataSource
import com.leanbulk.tracker.data.model.MealIngredient
import com.leanbulk.tracker.data.model.MealItem
import com.leanbulk.tracker.data.model.SouthIndianTopUp
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.data.model.WorkoutDataSource
import com.leanbulk.tracker.data.repository.HabitRepository
import com.leanbulk.tracker.data.repository.MealRepository
import com.leanbulk.tracker.data.repository.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val showAddDialog: Boolean = false,
    val waterGlasses: Int = 0,
    val sleepHours: Float = 0.0f,
    val adaptiveStatus: AdaptiveStatusInfo = AdaptiveCoachEngine.extractStatusInfo(UserProfile(), emptyList()),
    val adaptationLogs: List<AdaptationLogEntity> = emptyList(),
    val isEvaluatingAdaptation: Boolean = false
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
    private val habitRepository = HabitRepository(db.habitDao())
    private val healthSyncManager = HealthSyncManager(application)
    private val workoutDao = db.workoutDao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _showAddDialog = MutableStateFlow(false)
    private val _isEvaluatingAdaptation = MutableStateFlow(false)
    private val _customizingMeal = MutableStateFlow<MealItem?>(null)
    val customizingMeal: StateFlow<MealItem?> = _customizingMeal.asStateFlow()

    val customFoods: StateFlow<List<CustomFoodEntity>> = db.customFoodDao().getAllCustomFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCombos: StateFlow<List<SavedMealComboEntity>> = db.savedMealComboDao().getAllSavedCombos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                profileRepository.ensureDefaultProfileSeeded()
                mealRepository.ensureDefaultMealsSeeded(_selectedDate.value)
                AdaptiveCoachEngine.evaluateAndAdapt(db, application, force = false)
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
            workoutDao.getWorkoutLogsForDate(meta.date.toString()),
            habitRepository.getTodayHabit(meta.date),
            db.adaptationLogDao().getAllLogs(),
            _isEvaluatingAdaptation
        ) { rawMeals, workoutLogs, habitLog, logs, isEvaluating ->
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

            val weightLogs = db.weightDao().getAllWeightLogsSync()
            val adaptedMeals = AdaptiveMealResizer.adaptMeals(rawMeals, meta.profile)
            val statusInfo = AdaptiveCoachEngine.extractStatusInfo(meta.profile, weightLogs)

            MealsUiState(
                date = meta.date,
                meals = adaptedMeals,
                userProfile = meta.profile,
                syncData = meta.syncData,
                nutritionTarget = nutritionTarget,
                showAddDialog = meta.showDialog,
                waterGlasses = habitLog?.waterGlasses ?: 0,
                sleepHours = habitLog?.sleepHours ?: 0.0f,
                adaptiveStatus = statusInfo,
                adaptationLogs = logs,
                isEvaluatingAdaptation = isEvaluating
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
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
                // Auto-evaluate closed-loop coaching after meal check-off
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = false)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error toggling meal checked", e)
            }
        }
    }

    fun toggleAutoAdapt(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updated = uiState.value.userProfile.copy(autoAdaptEnabled = enabled)
                profileRepository.updateProfile(updated)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error toggling auto adapt", e)
            }
        }
    }

    fun runAdaptationCheckNow() {
        viewModelScope.launch {
            _isEvaluatingAdaptation.value = true
            try {
                AdaptiveCoachEngine.evaluateAndAdapt(db, getApplication(), force = true)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error evaluating adaptation", e)
            } finally {
                _isEvaluatingAdaptation.value = false
            }
        }
    }

    fun toggleVegOption(meal: MealItem, isVeg: Boolean) {
        viewModelScope.launch {
            try {
                mealRepository.toggleVegOption(_selectedDate.value, meal, isVeg)
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
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

    fun openCustomizeDialog(meal: MealItem) {
        Log.d("MealsViewModel", "openCustomizeDialog for ${meal.id} - ${meal.mealName}")
        _customizingMeal.value = meal
    }

    fun closeCustomizeDialog() {
        _customizingMeal.value = null
    }

    fun customizeMeal(mealId: String, ingredients: List<MealIngredient>, saveAsTemplate: Boolean) {
        viewModelScope.launch {
            try {
                mealRepository.customizeMeal(_selectedDate.value, mealId, ingredients, saveAsTemplate)
                _customizingMeal.value = null
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error customizing meal", e)
            }
        }
    }

    fun resetMealToDefault(mealId: String) {
        viewModelScope.launch {
            try {
                mealRepository.resetMealToDefault(_selectedDate.value, mealId)
                _customizingMeal.value = null
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error resetting meal to default", e)
            }
        }
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
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
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
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error adding custom meal", e)
            }
        }
    }

    fun saveCustomFood(food: CustomFoodEntity) {
        viewModelScope.launch {
            try {
                db.customFoodDao().insertCustomFood(food)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error saving custom food", e)
            }
        }
    }

    fun deleteCustomFood(id: String) {
        viewModelScope.launch {
            try {
                db.customFoodDao().deleteCustomFood(id)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error deleting custom food", e)
            }
        }
    }

    fun saveMealCombo(combo: SavedMealComboEntity) {
        viewModelScope.launch {
            try {
                db.savedMealComboDao().insertCombo(combo)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error saving meal combo", e)
            }
        }
    }

    fun deleteMealCombo(id: String) {
        viewModelScope.launch {
            try {
                db.savedMealComboDao().deleteCombo(id)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error deleting meal combo", e)
            }
        }
    }

    fun deleteCustomMeal(mealId: String) {
        viewModelScope.launch {
            try {
                mealRepository.deleteCustomMeal(_selectedDate.value, mealId)
                com.leanbulk.tracker.widget.LeanBulkWidgetUpdater.update(getApplication())
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error deleting custom meal", e)
            }
        }
    }

    fun incrementWater() {
        viewModelScope.launch {
            try {
                habitRepository.incrementWater(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error incrementing water", e)
            }
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            try {
                habitRepository.decrementWater(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error decrementing water", e)
            }
        }
    }

    fun updateSleep(hours: Float) {
        viewModelScope.launch {
            try {
                habitRepository.updateSleep(hours, _selectedDate.value)
            } catch (e: Exception) {
                Log.e("MealsViewModel", "Error updating sleep", e)
            }
        }
    }
}
