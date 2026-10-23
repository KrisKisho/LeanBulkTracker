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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.leanbulk.tracker.data.model.FoodCatalog
import com.leanbulk.tracker.data.model.FoodCategory
import com.leanbulk.tracker.data.model.FoodItem
import com.leanbulk.tracker.data.model.MealIngredient
import com.leanbulk.tracker.data.model.MealItem
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
import java.util.UUID

@Composable
fun CustomizeMealDialog(
    meal: MealItem,
    onDismiss: () -> Unit,
    onSave: (ingredients: List<MealIngredient>, saveAsTemplate: Boolean) -> Unit,
    onResetToDefault: () -> Unit
) {
    // Current editable ingredients list
    val ingredients = remember(meal) {
        val initial = if (meal.ingredients.isNotEmpty()) {
            meal.ingredients
        } else {
            FoodCatalog.getDefaultIngredientsForMeal(meal.id, meal.isVeg)
        }
        mutableStateListOf<MealIngredient>().apply { addAll(initial) }
    }

    var saveAsDefault by remember { mutableStateOf(true) }
    var showAddSection by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Direct Grams/Quantity editing modal state
    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var directQuantityInput by remember { mutableStateOf("") }

    // Calculated totals
    val totalCalories = ingredients.sumOf { it.calories }
    val totalProtein = ingredients.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = ingredients.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = ingredients.sumOf { it.fat.toDouble() }.toFloat()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize ${meal.mealName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Calibrated South Indian Portions & Macros",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Macro Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkBackground)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALCULATED TOTAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$totalCalories",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "kcal",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = AmberGold,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Macro breakdown pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MacroPill(label = "Protein", value = "%.1fg".format(totalProtein), color = NeonLime)
                            MacroPill(label = "Carbs", value = "%.1fg".format(totalCarbs), color = ElectricCyan)
                            MacroPill(label = "Fat", value = "%.1fg".format(totalFat), color = CoralRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ingredients List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INGREDIENTS & PORTIONS (${ingredients.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        )
                    )

                    TextButton(
                        onClick = { showAddSection = !showAddSection },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (showAddSection) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddSection) "Done Adding" else "+ Add Food",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Inline Food Item Catalog Picker (When expanded)
                AnimatedVisibility(visible = showAddSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkBackground)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Add from South Indian Food Catalog",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CategoryFilterChip(
                                title = "All",
                                isSelected = selectedCategory == null,
                                onClick = { selectedCategory = null }
                            )
                            FoodCategory.values().filter { it != FoodCategory.CUSTOM }.forEach { cat ->
                                CategoryFilterChip(
                                    title = cat.displayName,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search rice, dal, milk, chicken...", fontSize = 12.sp, color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filtered catalog list
                        val filteredItems = FoodCatalog.items.filter { item ->
                            val matchesCategory = selectedCategory == null || item.category == selectedCategory
                            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                            matchesCategory && matchesSearch
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredItems.take(8).forEach { foodItem ->
                                CatalogItemRow(
                                    foodItem = foodItem,
                                    onAdd = {
                                        // Default starting portion is baseServingSize or reasonable starter
                                        val starterQty = when (foodItem.category) {
                                            FoodCategory.GRAINS -> if (foodItem.unit == "g") 200f else 2f
                                            FoodCategory.DALS_CURRIES -> if (foodItem.unit == "g") 150f else 1f
                                            FoodCategory.DAIRY -> if (foodItem.unit == "ml") 250f else 1f
                                            FoodCategory.PROTEIN_POWDER -> 1f
                                            FoodCategory.MEAT_EGGS -> if (foodItem.unit == "g") 150f else 2f
                                            FoodCategory.FATS_EXTRAS -> if (foodItem.unit == "g") 30f else 1f
                                            else -> foodItem.baseServingSize
                                        }
                                        ingredients.add(foodItem.calculateMacros(starterQty))
                                    }
                                )
                            }
                        }
                    }
                }

                if (showAddSection) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Render each ingredient card
                if (ingredients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ingredients added yet. Tap '+ Add Food' above.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    ingredients.forEachIndexed { index, ingredient ->
                        IngredientCard(
                            ingredient = ingredient,
                            onQuantityChange = { newQty ->
                                if (newQty <= 0f) {
                                    ingredients.removeAt(index)
                                } else {
                                    val foodItem = FoodCatalog.findById(ingredient.foodItemId)
                                    val updated = if (foodItem != null) {
                                        foodItem.calculateMacros(newQty)
                                    } else {
                                        val factor = if (ingredient.quantity > 0f) newQty / ingredient.quantity else 1f
                                        ingredient.copy(
                                            quantity = newQty,
                                            calories = (ingredient.calories * factor).toInt(),
                                            protein = ((ingredient.protein * factor) * 10).toInt() / 10f,
                                            carbs = ((ingredient.carbs * factor) * 10).toInt() / 10f,
                                            fat = ((ingredient.fat * factor) * 10).toInt() / 10f
                                        )
                                    }
                                    ingredients[index] = updated
                                }
                            },
                            onDirectEdit = {
                                editingIngredientIndex = index
                                directQuantityInput = if (ingredient.quantity % 1f == 0f) {
                                    ingredient.quantity.toInt().toString()
                                } else {
                                    "%.1f".format(ingredient.quantity)
                                }
                            },
                            onSwap = {
                                // Swap dal/curry or milk/protein to alternatives
                                val alternatives = when (ingredient.category) {
                                    FoodCategory.DALS_CURRIES -> listOf("dal_toor_pappu", "dal_moong", "dal_chana", "dal_sambar", "curry_soya_chunks", "curry_paneer")
                                    FoodCategory.MEAT_EGGS -> listOf("meat_chicken_curry", "meat_chicken_breast", "egg_curry_serving", "egg_boiled_whole", "curry_soya_chunks", "curry_paneer", "meat_fish_curry")
                                    FoodCategory.DAIRY -> listOf("dairy_whole_milk", "dairy_toned_milk", "dairy_curd")
                                    FoodCategory.PROTEIN_POWDER -> listOf("protein_whey_isolate", "protein_whey_concentrate", "protein_plant")
                                    FoodCategory.GRAINS -> listOf("grain_steamed_rice", "grain_brown_rice", "grain_idli", "grain_dosa", "grain_pesarattu", "grain_roti", "grain_oats")
                                    else -> emptyList()
                                }
                                if (alternatives.isNotEmpty()) {
                                    val currentIndex = alternatives.indexOf(ingredient.foodItemId)
                                    val nextId = alternatives[(currentIndex + 1).coerceAtLeast(0) % alternatives.size]
                                    val nextFood = FoodCatalog.findById(nextId)
                                    if (nextFood != null) {
                                        val targetQty = if (nextFood.unit == ingredient.unit) ingredient.quantity else nextFood.baseServingSize
                                        ingredients[index] = nextFood.calculateMacros(targetQty)
                                    }
                                }
                            },
                            onDelete = {
                                ingredients.removeAt(index)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Default Template Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .clickable { saveAsDefault = !saveAsDefault }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveAsDefault,
                        onCheckedChange = { saveAsDefault = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonLime,
                            checkmarkColor = DarkBackground,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Save as daily default recipe for ${meal.mealName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "New days will automatically populate with these custom portions",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = onResetToDefault,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            onSave(ingredients.toList(), saveAsDefault)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Customized Meal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Direct Quantity Input Dialog
    editingIngredientIndex?.let { idx ->
        if (idx in ingredients.indices) {
            val targetIng = ingredients[idx]
            Dialog(onDismissRequest = { editingIngredientIndex = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Set Exact Quantity: ${targetIng.name}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = directQuantityInput,
                            onValueChange = { directQuantityInput = it },
                            label = { Text("Quantity (${targetIng.unit})", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingIngredientIndex = null }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val parsed = directQuantityInput.toFloatOrNull() ?: targetIng.quantity
                                    val foodItem = FoodCatalog.findById(targetIng.foodItemId)
                                    val updated = if (foodItem != null) {
                                        foodItem.calculateMacros(parsed)
                                    } else {
                                        val factor = if (targetIng.quantity > 0f) parsed / targetIng.quantity else 1f
                                        targetIng.copy(
                                            quantity = parsed,
                                            calories = (targetIng.calories * factor).toInt(),
                                            protein = ((targetIng.protein * factor) * 10).toInt() / 10f,
                                            carbs = ((targetIng.carbs * factor) * 10).toInt() / 10f,
                                            fat = ((targetIng.fat * factor) * 10).toInt() / 10f
                                        )
                                    }
                                    ingredients[idx] = updated
                                    editingIngredientIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Text("Update", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: MealIngredient,
    onQuantityChange: (Float) -> Unit,
    onDirectEdit: () -> Unit,
    onSwap: () -> Unit,
    onDelete: () -> Unit
) {
    val foodItem = FoodCatalog.findById(ingredient.foodItemId)
    val step = foodItem?.stepSize ?: when (ingredient.unit) {
        "g" -> 25f
        "ml" -> 50f
        "scoop" -> 0.5f
        else -> 1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBackground)
            .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Ingredient title and delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${ingredient.calories} kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        )
                        Text(
                            text = "•",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "%.1fg P".format(ingredient.protein),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                        )
                        Text(
                            text = "•",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "%.1fg C".format(ingredient.carbs),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricCyan
                            )
                        )
                        Text(
                            text = "•",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "%.1fg F".format(ingredient.fat),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CoralRed
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick swap button for swappable categories (dals, curries, shakes, grains)
                    if (ingredient.category in listOf(FoodCategory.DALS_CURRIES, FoodCategory.MEAT_EGGS, FoodCategory.DAIRY, FoodCategory.PROTEIN_POWDER, FoodCategory.GRAINS)) {
                        IconButton(
                            onClick = onSwap,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap type",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stepper controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Buttons [-] [Quantity] [+]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Decrement Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkSurfaceBorder, CircleShape)
                            .clickable {
                                val next = (ingredient.quantity - step).coerceAtLeast(0f)
                                onQuantityChange(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Tappable Quantity text (Click to type exact grams)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onDirectEdit() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val qtyStr = if (ingredient.quantity % 1f == 0f) {
                            ingredient.quantity.toInt().toString()
                        } else {
                            "%.1f".format(ingredient.quantity)
                        }
                        Text(
                            text = "$qtyStr ${ingredient.unit}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    // Increment Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkSurfaceBorder, CircleShape)
                            .clickable {
                                onQuantityChange(ingredient.quantity + step)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Step label hint (e.g. "±25g", "±50ml", "±0.5 scoop")
                val stepLabel = if (step % 1f == 0f) "±${step.toInt()}${ingredient.unit}" else "±$step ${ingredient.unit}"
                Text(
                    text = "Tap to type • $stepLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CatalogItemRow(
    foodItem: FoodItem,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onAdd() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val baseQtyStr = if (foodItem.baseServingSize % 1f == 0f) foodItem.baseServingSize.toInt().toString() else "%.1f".format(foodItem.baseServingSize)
            Text(
                text = "${foodItem.caloriesPerBase.toInt()} kcal • ${foodItem.proteinPerBase}g P / $baseQtyStr ${foodItem.unit}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ElectricCyan)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ElectricCyan else DarkSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else DarkSurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) DarkBackground else TextPrimary,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun MacroPill(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 11.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
        }
    }
}
