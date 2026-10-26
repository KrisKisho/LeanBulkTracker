package com.leanbulk.tracker.ui.meals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leanbulk.tracker.data.local.entity.CustomFoodEntity
import com.leanbulk.tracker.data.local.entity.SavedMealComboEntity
import com.leanbulk.tracker.data.model.FoodCategory
import com.leanbulk.tracker.data.model.FoodItem
import com.leanbulk.tracker.data.model.IngredientJsonHelper
import com.leanbulk.tracker.data.model.MacroGapBalancer
import com.leanbulk.tracker.data.model.MacroPatchOption
import com.leanbulk.tracker.data.model.MealIngredient
import com.leanbulk.tracker.data.model.MealItem
import com.leanbulk.tracker.data.model.SouthIndianFoodCatalog
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.CoralRed
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.PurpleAccent
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

private enum class PlateBuilderTab {
    ALL,
    SAVED_COMBOS,
    TIFFINS,
    RICE,
    PAPPU_SAMBAR,
    NON_VEG_EGGS,
    VEG_PROTEIN,
    VEPUDU,
    CHUTNEYS_GHEE,
    DAIRY_SHAKES
}

@Composable
fun MealPlateBuilderSheet(
    meal: MealItem,
    slotTargetCalories: Int,
    slotTargetProtein: Float,
    customFoods: List<CustomFoodEntity>,
    savedCombos: List<SavedMealComboEntity>,
    onDismiss: () -> Unit,
    onApplyPlate: (ingredients: List<MealIngredient>, saveAsTemplate: Boolean) -> Unit,
    onResetToDefault: () -> Unit,
    onSaveCustomFood: (CustomFoodEntity) -> Unit,
    onSaveMealCombo: (SavedMealComboEntity) -> Unit,
    onDeleteMealCombo: (String) -> Unit
) {
    // Current editable plate ingredients list
    val currentPlate = remember(meal) {
        val initial = if (meal.ingredients.isNotEmpty()) {
            meal.ingredients
        } else {
            val defaultFood = SouthIndianFoodCatalog.findById(meal.id)
            if (defaultFood != null) {
                listOf(defaultFood.calculateMacros(1f))
            } else {
                emptyList()
            }
        }
        mutableStateListOf<MealIngredient>().apply { addAll(initial) }
    }

    var saveAsDefaultRecipe by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(PlateBuilderTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewFoodDialog by remember { mutableStateOf(false) }
    var showSaveComboDialog by remember { mutableStateOf(false) }
    var comboNameInput by remember { mutableStateOf("") }

    // Live Plate Totals
    val plateCalories = currentPlate.sumOf { it.calories }
    val plateProtein = currentPlate.sumOf { it.protein.toDouble() }.toFloat()
    val plateCarbs = currentPlate.sumOf { it.carbs.toDouble() }.toFloat()
    val plateFat = currentPlate.sumOf { it.fat.toDouble() }.toFloat()
    val isPlateVegetarian = currentPlate.isEmpty() || currentPlate.all { it.isVeg }

    // Macro-Gap Balancer Analysis
    val macroGapAnalysis = remember(plateCalories, plateProtein, isPlateVegetarian, slotTargetCalories, slotTargetProtein) {
        MacroGapBalancer.analyzeGap(
            targetCalories = slotTargetCalories,
            targetProtein = slotTargetProtein,
            currentCalories = plateCalories,
            currentProtein = plateProtein,
            isVegetarian = isPlateVegetarian
        )
    }

    // Helper: Add or increment item in plate
    fun addOrIncrementFood(food: FoodItem, incrementBy: Float = 1.0f) {
        val existingIndex = currentPlate.indexOfFirst { it.foodItemId == food.id }
        if (existingIndex >= 0) {
            val existing = currentPlate[existingIndex]
            val newQty = existing.quantity + incrementBy
            currentPlate[existingIndex] = food.calculateMacros(newQty)
        } else {
            currentPlate.add(food.calculateMacros(incrementBy))
        }
    }

    // Helper: Modify item quantity
    fun updateQuantity(index: Int, newQty: Float) {
        if (index in currentPlate.indices) {
            if (newQty <= 0f) {
                currentPlate.removeAt(index)
            } else {
                val item = currentPlate[index]
                val catalogFood = SouthIndianFoodCatalog.findById(item.foodItemId)
                    ?: customFoods.find { it.id == item.foodItemId }?.toFoodItem()
                if (catalogFood != null) {
                    currentPlate[index] = catalogFood.calculateMacros(newQty)
                } else {
                    val factor = if (item.quantity > 0f) newQty / item.quantity else 1f
                    currentPlate[index] = item.copy(
                        quantity = newQty,
                        calories = (item.calories * factor).toInt(),
                        protein = ((item.protein * factor) * 10).toInt() / 10f,
                        carbs = ((item.carbs * factor) * 10).toInt() / 10f,
                        fat = ((item.fat * factor) * 10).toInt() / 10f
                    )
                }
            }
        }
    }

    // Helper: Apply patch chip from MacroGapBalancer
    fun applyPatch(patch: MacroPatchOption) {
        val food = SouthIndianFoodCatalog.findById(patch.foodItemId)
            ?: customFoods.find { it.id == patch.foodItemId }?.toFoodItem()
        if (food != null) {
            addOrIncrementFood(food, patch.quantity)
        } else {
            currentPlate.add(
                MealIngredient(
                    foodItemId = patch.foodItemId,
                    name = patch.label,
                    quantity = patch.quantity,
                    unit = "serving",
                    calories = patch.addedCalories,
                    protein = patch.addedProtein,
                    carbs = patch.addedCarbs,
                    fat = patch.addedFat,
                    category = FoodCategory.CUSTOM,
                    isVeg = patch.isVegetarian
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ==========================================
                // 1. TOP HEADER & CLOSE BUTTON
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Build Plate: ${meal.mealName}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            VegNonVegBadge(isVeg = isPlateVegetarian, size = 13.dp)
                        }
                        Text(
                            text = "South Indian Food Database • Exact Macros",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // 2. LIVE COMPARISON BAR
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Calories row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Plate: $plateCalories / $slotTargetCalories kcal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (plateCalories >= slotTargetCalories) NeonLime else AmberGold
                                )
                            )
                            Text(
                                text = if (plateCalories >= slotTargetCalories) "+${plateCalories - slotTargetCalories} kcal" else "-${slotTargetCalories - plateCalories} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (plateCalories >= slotTargetCalories) NeonLime else CoralRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (plateCalories.toFloat() / slotTargetCalories.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (plateCalories >= slotTargetCalories) NeonLime else AmberGold,
                            trackColor = DarkSurfaceBorder
                        )

                        // Protein row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Protein: ${"%.1f".format(plateProtein)}g / ${"%.1f".format(slotTargetProtein)}g",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (plateProtein >= slotTargetProtein) ElectricCyan else CoralRed
                                )
                            )
                            Text(
                                text = "Carbs: ${"%.0f".format(plateCarbs)}g • Fat: ${"%.0f".format(plateFat)}g",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (plateProtein / slotTargetProtein.coerceAtLeast(1f)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (plateProtein >= slotTargetProtein) ElectricCyan else CoralRed,
                            trackColor = DarkSurfaceBorder
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // 3. SMART MACRO-GAP AUTO-BALANCER CHIPS
                // ==========================================
                if (macroGapAnalysis.patches.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (macroGapAnalysis.isProteinShort) CoralRed.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (macroGapAnalysis.isProteinShort) CoralRed.copy(alpha = 0.4f) else AmberGold.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (macroGapAnalysis.isProteinShort) CoralRed else AmberGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = macroGapAnalysis.statusMessage,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (macroGapAnalysis.isProteinShort) CoralRed else AmberGold,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                macroGapAnalysis.patches.forEach { patch ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkBackground)
                                            .border(
                                                1.dp,
                                                if (patch.isVegetarian) NeonLime.copy(alpha = 0.7f) else CoralRed.copy(alpha = 0.7f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { applyPatch(patch) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            VegNonVegBadge(isVeg = patch.isVegetarian, size = 10.dp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = patch.label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ==========================================
                // 4. CURRENT PLATE ITEMS (WITH STEPPERS)
                // ==========================================
                if (currentPlate.isNotEmpty()) {
                    Text(
                        text = "ITEMS ON PLATE (${currentPlate.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.6.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (currentPlate.size > 2) 110.dp else 75.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentPlate.size) { index ->
                                val item = currentPlate[index]
                                val qtyStr = if (item.quantity % 1f == 0f) item.quantity.toInt().toString() else "%.1f".format(item.quantity)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        VegNonVegBadge(isVeg = item.isVeg, size = 11.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${item.calories}k, ${item.protein}g P)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }

                                    // Stepper: [-] Qty [+]
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(DarkSurfaceElevated)
                                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                                .clickable { updateQuantity(index, item.quantity - 0.5f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        Text(
                                            text = "$qtyStr ${item.unit}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = AmberGold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(DarkSurfaceElevated)
                                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                                .clickable { updateQuantity(index, item.quantity + 0.5f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        IconButton(
                                            onClick = { currentPlate.removeAt(index) },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = CoralRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ==========================================
                // 5. SEARCH & CATEGORY FILTER CHIPS
                // ==========================================
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search idli, chicken, dal, ghee, eggs...", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(12.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips Scroll Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip("All", selectedTab == PlateBuilderTab.ALL) { selectedTab = PlateBuilderTab.ALL }
                    CategoryChip("Saved Combos", selectedTab == PlateBuilderTab.SAVED_COMBOS, badge = savedCombos.size.toString()) {
                        selectedTab = PlateBuilderTab.SAVED_COMBOS
                    }
                    CategoryChip("Tiffins", selectedTab == PlateBuilderTab.TIFFINS) { selectedTab = PlateBuilderTab.TIFFINS }
                    CategoryChip("Rice", selectedTab == PlateBuilderTab.RICE) { selectedTab = PlateBuilderTab.RICE }
                    CategoryChip("Pappu/Sambar", selectedTab == PlateBuilderTab.PAPPU_SAMBAR) { selectedTab = PlateBuilderTab.PAPPU_SAMBAR }
                    CategoryChip("Eggs/Non-Veg", selectedTab == PlateBuilderTab.NON_VEG_EGGS) { selectedTab = PlateBuilderTab.NON_VEG_EGGS }
                    CategoryChip("Veg Protein", selectedTab == PlateBuilderTab.VEG_PROTEIN) { selectedTab = PlateBuilderTab.VEG_PROTEIN }
                    CategoryChip("Vepudu", selectedTab == PlateBuilderTab.VEPUDU) { selectedTab = PlateBuilderTab.VEPUDU }
                    CategoryChip("Chutneys/Ghee", selectedTab == PlateBuilderTab.CHUTNEYS_GHEE) { selectedTab = PlateBuilderTab.CHUTNEYS_GHEE }
                    CategoryChip("Shakes/Dairy", selectedTab == PlateBuilderTab.DAIRY_SHAKES) { selectedTab = PlateBuilderTab.DAIRY_SHAKES }

                    // "+ New Food" Action Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { showNewFoodDialog = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("+ New Food", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ==========================================
                // 6. CATALOG ITEMS / SAVED COMBOS LIST
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (selectedTab == PlateBuilderTab.SAVED_COMBOS) {
                        // Render Saved Combos
                        if (savedCombos.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("No saved meal combos yet.", color = TextMuted, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (currentPlate.isNotEmpty()) {
                                    Button(
                                        onClick = { showSaveComboDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Current Plate as Combo", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item {
                                    if (currentPlate.isNotEmpty()) {
                                        Button(
                                            onClick = { showSaveComboDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkBackground),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save Current Plate as New Combo", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                items(savedCombos) { combo ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkBackground)
                                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    VegNonVegBadge(isVeg = combo.isVegetarian, size = 11.dp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(combo.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                                }
                                                Text(
                                                    text = "${combo.totalCalories} kcal • ${"%.1f".format(combo.totalProtein)}g P",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Button(
                                                    onClick = {
                                                        currentPlate.clear()
                                                        currentPlate.addAll(combo.parseIngredients())
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkBackground),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Load", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                                }

                                                IconButton(
                                                    onClick = { onDeleteMealCombo(combo.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Filter foods based on category & search
                        val categoryFilter = when (selectedTab) {
                            PlateBuilderTab.ALL -> null
                            PlateBuilderTab.TIFFINS -> FoodCategory.TIFFINS_BREADS
                            PlateBuilderTab.RICE -> FoodCategory.RICE_GRAINS
                            PlateBuilderTab.PAPPU_SAMBAR -> FoodCategory.DALS_SAMBAR_RASAM
                            PlateBuilderTab.NON_VEG_EGGS -> FoodCategory.NON_VEG_EGGS
                            PlateBuilderTab.VEG_PROTEIN -> FoodCategory.VEG_PROTEIN_BOOSTERS
                            PlateBuilderTab.VEPUDU -> FoodCategory.VEPUDU_PORIYAL
                            PlateBuilderTab.CHUTNEYS_GHEE -> FoodCategory.CHUTNEYS_PODIS_FATS
                            PlateBuilderTab.DAIRY_SHAKES -> FoodCategory.DAIRY_SHAKES_SUPPLEMENTS
                            else -> null
                        }

                        val customFoodItems = customFoods.map { it.toFoodItem() }
                        val matchingFoods = remember(searchQuery, categoryFilter, customFoods) {
                            SouthIndianFoodCatalog.search(
                                query = searchQuery,
                                category = categoryFilter,
                                vegOnly = false,
                                customFoods = customFoodItems
                            )
                        }

                        if (matchingFoods.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No foods match your search.", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(matchingFoods, key = { it.id }) { food ->
                                    val countInPlate = currentPlate.firstOrNull { it.foodItemId == food.id }?.quantity ?: 0f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkBackground)
                                            .border(1.dp, if (countInPlate > 0f) NeonLime.copy(alpha = 0.5f) else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                VegNonVegBadge(isVeg = food.isVegetarian, size = 12.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = food.name,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextPrimary,
                                                                fontSize = 11.5.sp
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (food.teluguOrRegionalHint.isNotBlank()) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "(${food.teluguOrRegionalHint})",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = TextSecondary,
                                                                    fontSize = 10.sp
                                                                )
                                                            )
                                                        }
                                                        if (food.isHighProteinPatch) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(PurpleAccent.copy(alpha = 0.2f))
                                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                                            ) {
                                                                Text("High Protein", color = PurpleAccent, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "${food.calories} kcal • ${"%.1f".format(food.proteinGrams)}g P / ${food.servingUnit}",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                                    )
                                                }
                                            }

                                            // Quick Add Button or Current Count
                                            if (countInPlate > 0f) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val qtyStr = if (countInPlate % 1f == 0f) countInPlate.toInt().toString() else "%.1f".format(countInPlate)
                                                    Text(
                                                        text = "x$qtyStr",
                                                        color = NeonLime,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonLime)
                                                            .clickable { addOrIncrementFood(food, 0.5f) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("+", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                }
                                            } else {
                                                Button(
                                                    onClick = { addOrIncrementFood(food, 1.0f) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = DarkSurfaceElevated,
                                                        contentColor = ElectricCyan
                                                    ),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("+ Add", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // 7. FOOTER ACTION BUTTONS
                // ==========================================
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { saveAsDefaultRecipe = !saveAsDefaultRecipe }
                        ) {
                            Checkbox(
                                checked = saveAsDefaultRecipe,
                                onCheckedChange = { saveAsDefaultRecipe = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonLime)
                            )
                            Text(
                                text = "Save as Default for ${meal.mealName}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                            )
                        }

                        TextButton(onClick = onResetToDefault) {
                            Text("Reset Slot", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onApplyPlate(currentPlate.toList(), saveAsDefaultRecipe)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (plateProtein >= slotTargetProtein) NeonLime else AmberGold,
                                contentColor = DarkBackground
                            ),
                            modifier = Modifier.weight(2f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Apply to Today's ${meal.mealName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG: ADD NEW CUSTOM FOOD (PER 100G)
    // ==========================================
    if (showNewFoodDialog) {
        AddFoodItemDialog(
            onDismiss = { showNewFoodDialog = false },
            onSaveFood = { customFood ->
                onSaveCustomFood(customFood)
                addOrIncrementFood(customFood.toFoodItem(), 1.0f)
                showNewFoodDialog = false
            }
        )
    }

    // ==========================================
    // DIALOG: SAVE CURRENT PLATE AS COMBO
    // ==========================================
    if (showSaveComboDialog) {
        Dialog(onDismissRequest = { showSaveComboDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Save Plate as Reusable Combo", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Text("e.g. 'My Pesarattu + 2 Eggs Breakfast' or 'Sunday Kodi Kura Plate'", color = TextSecondary, fontSize = 11.sp)

                    OutlinedTextField(
                        value = comboNameInput,
                        onValueChange = { comboNameInput = it },
                        placeholder = { Text("Enter combo name...", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSaveComboDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (comboNameInput.isNotBlank()) {
                                    val combo = SavedMealComboEntity(
                                        name = comboNameInput,
                                        targetMealSlotId = meal.id,
                                        totalCalories = plateCalories,
                                        totalProtein = plateProtein,
                                        totalCarbs = plateCarbs,
                                        totalFat = plateFat,
                                        isVegetarian = isPlateVegetarian,
                                        itemsJson = IngredientJsonHelper.toJsonString(currentPlate.toList())
                                    )
                                    onSaveMealCombo(combo)
                                    showSaveComboDialog = false
                                    comboNameInput = ""
                                    selectedTab = PlateBuilderTab.SAVED_COMBOS
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkBackground)
                        ) {
                            Text("Save Combo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ElectricCyan else DarkBackground)
            .border(1.dp, if (isSelected) ElectricCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) DarkBackground else TextSecondary,
                    fontSize = 11.sp
                )
            )
            if (badge != null && badge != "0") {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DarkBackground else AmberGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = if (isSelected) ElectricCyan else DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
