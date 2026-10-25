package com.leanbulk.tracker.ui.meals

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leanbulk.tracker.data.model.MealDataSource
import com.leanbulk.tracker.data.model.MealItem
import com.leanbulk.tracker.ui.components.LiveHealthSyncCard
import com.leanbulk.tracker.ui.components.NutritionSummaryCard
import com.leanbulk.tracker.ui.components.AdaptiveCoachStatusCard
import com.leanbulk.tracker.ui.components.RecoveryHydrationCard
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.CoralRed
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun MealsScreen(
    viewModel: MealsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val customizingMeal by viewModel.customizingMeal.collectAsState()
    val customFoods by viewModel.customFoods.collectAsState()
    val savedCombos by viewModel.savedCombos.collectAsState()

    val stepPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onStepPermissionGranted()
        }
    }

    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onHealthConnectPermissionsResult(grantedPermissions)
    }

    if (state.showAddDialog) {
        AddCustomMealDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onAddPreset = { preset -> viewModel.addPresetMeal(preset) },
            onAddCustom = { name, cal, p, c, f -> viewModel.addCustomMeal(name, cal, p, c, f) }
        )
    }

    // Interactive South Indian Meal Plate Builder Sheet
    customizingMeal?.let { mealToCustomize ->
        val slotTargetCalories = remember(mealToCustomize.id, state.targetCalories) {
            val ratio = when (mealToCustomize.id) {
                "meal_breakfast" -> 520f / 2420f
                "meal_shake" -> 490f / 2420f
                "meal_lunch" -> 570f / 2420f
                "meal_snack" -> 310f / 2420f
                "meal_dinner" -> 530f / 2420f
                else -> if (mealToCustomize.defaultCalories > 0) mealToCustomize.defaultCalories.toFloat() / 2420f else 0.2f
            }
            (state.targetCalories * ratio).toInt().coerceAtLeast(100)
        }
        val slotTargetProtein = remember(mealToCustomize.id, state.targetProtein) {
            val ratio = when (mealToCustomize.id) {
                "meal_breakfast" -> 26f / 136f
                "meal_shake" -> 36f / 136f
                "meal_lunch" -> 38f / 136f
                "meal_snack" -> 13f / 136f
                "meal_dinner" -> 23f / 136f
                else -> if (mealToCustomize.defaultProtein > 0) mealToCustomize.defaultProtein.toFloat() / 136f else 0.2f
            }
            (state.targetProtein * ratio).coerceAtLeast(10f)
        }

        MealPlateBuilderSheet(
            meal = mealToCustomize,
            slotTargetCalories = slotTargetCalories,
            slotTargetProtein = slotTargetProtein,
            customFoods = customFoods,
            savedCombos = savedCombos,
            onDismiss = { viewModel.closeCustomizeDialog() },
            onApplyPlate = { ingredients, saveAsTemplate ->
                viewModel.customizeMeal(mealToCustomize.id, ingredients, saveAsTemplate)
                Toast.makeText(context, "Applied plate to ${mealToCustomize.mealName}!", Toast.LENGTH_SHORT).show()
            },
            onResetToDefault = {
                viewModel.resetMealToDefault(mealToCustomize.id)
                Toast.makeText(context, "Reset ${mealToCustomize.mealName} to default", Toast.LENGTH_SHORT).show()
            },
            onSaveCustomFood = { customFood ->
                viewModel.saveCustomFood(customFood)
                Toast.makeText(context, "Saved ${customFood.name} to Database", Toast.LENGTH_SHORT).show()
            },
            onSaveMealCombo = { combo ->
                viewModel.saveMealCombo(combo)
                Toast.makeText(context, "Saved combo '${combo.name}'", Toast.LENGTH_SHORT).show()
            },
            onDeleteMealCombo = { comboId ->
                viewModel.deleteMealCombo(comboId)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "South Indian Bulk Fuel",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Customizable Portions & Exact Calorie Engine",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1-Tap Quick Extra Rice Button
                    Button(
                        onClick = {
                            viewModel.addPresetMeal(
                                MealDataSource.QuickPreset(
                                    label = "+0.5 Cup Steamed Rice",
                                    calories = 150,
                                    protein = 3,
                                    carbs = 34,
                                    fat = 0
                                )
                            )
                            Toast.makeText(context, "Added +0.5 Cup Steamed Rice (+150 kcal)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Extra Rice", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, maxLines = 1)
                    }

                    // Open Presets & Custom Meal Dialog
                    Button(
                        onClick = { viewModel.openAddDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = ElectricCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("+ More", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, maxLines = 1)
                    }
                }
            }
        }

        item {
            // Live Health Connect & Step Engine Card
            LiveHealthSyncCard(
                syncData = state.syncData,
                nutritionTarget = state.nutritionTarget,
                onSyncNow = { viewModel.syncHealthNow() },
                onAddStepBonus = { bonus -> viewModel.addStepBonus(bonus) },
                onAddTopUpMeal = { topUp -> viewModel.addTopUpMeal(topUp) },
                onRequestStepPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        stepPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        viewModel.onStepPermissionGranted()
                    }
                },
                onRequestHealthConnectPermission = {
                    healthConnectLauncher.launch(viewModel.healthConnectPermissions)
                }
            )
        }

        item {
            // High contrast nutrition rings card
            NutritionSummaryCard(
                currentCalories = state.consumedCalories,
                targetCalories = state.targetCalories,
                currentProtein = state.consumedProtein,
                targetProtein = state.targetProtein,
                currentCarbs = state.consumedCarbs,
                targetCarbs = state.targetCarbs,
                currentFat = state.consumedFat,
                targetFat = state.targetFat
            )
        }

        item {
            // Autonomous Closed-Loop Coaching Status & History Card
            AdaptiveCoachStatusCard(
                status = state.adaptiveStatus,
                adaptationLogs = state.adaptationLogs,
                onToggleAutoAdapt = { viewModel.toggleAutoAdapt(it) },
                onRunAdaptationCheck = { viewModel.runAdaptationCheckNow() },
                isEvaluating = state.isEvaluatingAdaptation
            )
        }

        item {
            // Compact Recovery & Hydration Card (Water & Sleep Trackers)
            RecoveryHydrationCard(
                waterGlasses = state.waterGlasses,
                sleepHours = state.sleepHours,
                onIncrementWater = { viewModel.incrementWater() },
                onDecrementWater = { viewModel.decrementWater() },
                onUpdateSleep = { hours -> viewModel.updateSleep(hours) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY MEAL CHECKLIST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.8.sp
                    )
                )

                Text(
                    text = "${state.meals.count { it.isChecked }} / ${state.meals.size} Logged",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        items(state.meals, key = { it.id }) { meal ->
            MealChecklistItem(
                meal = meal,
                onCheckedChange = { isChecked ->
                    viewModel.toggleMealChecked(meal, isChecked)
                },
                onVegToggle = { isVeg ->
                    viewModel.toggleVegOption(meal, isVeg)
                },
                onCustomize = {
                    viewModel.openCustomizeDialog(meal)
                },
                onDelete = {
                    viewModel.deleteCustomMeal(meal.id)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MealChecklistItem(
    meal: MealItem,
    onCheckedChange: (Boolean) -> Unit,
    onVegToggle: (Boolean) -> Unit,
    onCustomize: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (meal.isChecked) NeonLime.copy(alpha = 0.5f) else DarkSurfaceBorder
    val bgColor = if (meal.isChecked) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = meal.isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonLime,
                        checkmarkColor = DarkBackground,
                        uncheckedColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = meal.timeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            )
                        }

                        VegNonVegBadge(
                            isVeg = meal.isVeg,
                            size = 14.dp,
                            modifier = if (meal.hasVegOption) {
                                Modifier.clickable { onVegToggle(!meal.isVeg) }
                            } else Modifier
                        )

                        Text(
                            text = meal.mealName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (meal.isChecked) TextPrimary else TextPrimary.copy(alpha = 0.9f)
                            )
                        )
                    }
                }

                // Delete button for custom meals
                if (meal.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom item",
                            tint = CoralRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Meal content description
            Text(
                text = meal.activeTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (meal.isChecked) TextSecondary else TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .padding(start = 44.dp)
                    .clickable { onCustomize() }
            )

            // Customized Ingredient breakdown pills (if meal has ingredients)
            if (meal.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    meal.ingredients.forEach { ing ->
                        val qtyStr = if (ing.quantity % 1f == 0f) ing.quantity.toInt().toString() else "%.1f".format(ing.quantity)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                                .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$qtyStr${ing.unit} ${ing.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Calories, Protein, Customize button & Veg Swap toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberGold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${meal.activeCalories} kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonLime.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${meal.activeProtein}g Protein",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Customize / Build Plate Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onCustomize() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Customize / Build Plate",
                                tint = ElectricCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Build Plate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Veg / Non-Veg Swap Button
                    if (meal.hasVegOption) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background((if (meal.isVeg) NeonLime else CoralRed).copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    (if (meal.isVeg) NeonLime else CoralRed).copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onVegToggle(!meal.isVeg) }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                VegNonVegBadge(isVeg = meal.isVeg, size = 14.dp)
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = if (meal.isVeg) "Switch to Non-Veg" else "Switch to Veg",
                                    tint = if (meal.isVeg) NeonLime else CoralRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Authentic Indian Food Dietary Mark:
 * Green square with centered green circle for Vegetarian.
 * Red/Coral square with centered red/coral circle for Non-Vegetarian.
 */
@Composable
fun VegNonVegBadge(
    isVeg: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 14.dp
) {
    val badgeColor = if (isVeg) NeonLime else CoralRed
    Box(
        modifier = modifier
            .size(size)
            .border(1.5.dp, badgeColor, RoundedCornerShape(3.dp))
            .padding(size * 0.22f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(badgeColor)
        )
    }
}
