package com.leanbulk.tracker.ui.meals

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.leanbulk.tracker.data.local.entity.CustomFoodEntity
import com.leanbulk.tracker.data.model.FoodCategory
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
import kotlin.math.roundToInt

enum class PortionMode {
    STANDARD_100G,
    CUSTOM_HOUSEHOLD
}

@Composable
fun AddFoodItemDialog(
    onDismiss: () -> Unit,
    onSaveFood: (CustomFoodEntity) -> Unit
) {
    val context = LocalContext.current

    var foodName by remember { mutableStateOf("") }
    var foodTeluguHint by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FoodCategory.CUSTOM) }
    var isVegetarian by remember { mutableStateOf(true) }

    // Nutritional & calorific values per 100g
    var caloriesPer100gInput by remember { mutableStateOf("") }
    var proteinPer100gInput by remember { mutableStateOf("") }
    var carbsPer100gInput by remember { mutableStateOf("") }
    var fatPer100gInput by remember { mutableStateOf("") }

    // Portion mode: Per 100g standard vs Custom household portion
    var portionMode by remember { mutableStateOf(PortionMode.STANDARD_100G) }
    var portionNameInput by remember { mutableStateOf("1 serving") }
    var portionGramsInput by remember { mutableStateOf("100") }

    // Derived numeric values per 100g
    val calsPer100g = caloriesPer100gInput.toIntOrNull() ?: 0
    val proteinPer100g = proteinPer100gInput.toFloatOrNull() ?: 0f
    val carbsPer100g = carbsPer100gInput.toFloatOrNull() ?: 0f
    val fatPer100g = fatPer100gInput.toFloatOrNull() ?: 0f

    // Estimated calories based on 4-4-9 macro math (useful verification for nutrition label check)
    val calculatedKcalFromMacros = ((proteinPer100g * 4f) + (carbsPer100g * 4f) + (fatPer100g * 9f)).roundToInt()

    // Derived portion grams
    val portionGrams = portionGramsInput.toFloatOrNull() ?: 100f
    val portionRatio = (portionGrams / 100f).coerceAtLeast(0.01f)

    val finalCalories = if (portionMode == PortionMode.STANDARD_100G) {
        calsPer100g
    } else {
        (calsPer100g * portionRatio).roundToInt()
    }
    val finalProtein = if (portionMode == PortionMode.STANDARD_100G) {
        proteinPer100g
    } else {
        proteinPer100g * portionRatio
    }
    val finalCarbs = if (portionMode == PortionMode.STANDARD_100G) {
        carbsPer100g
    } else {
        carbsPer100g * portionRatio
    }
    val finalFat = if (portionMode == PortionMode.STANDARD_100G) {
        fatPer100g
    } else {
        fatPer100g * portionRatio
    }

    val finalServingUnit = if (portionMode == PortionMode.STANDARD_100G) {
        "100g"
    } else {
        val pName = portionNameInput.ifBlank { "1 portion" }
        "$pName (${portionGrams.toInt()}g)"
    }

    val isHighProtein = (proteinPer100g >= 10f) || (finalProtein >= 10f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Add Item (Per 100g)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Add to Available Foods Catalog",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Item Name
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Item Name (e.g. Roasted Chana, Paneer, Oats)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Telugu / Regional Hint
                OutlinedTextField(
                    value = foodTeluguHint,
                    onValueChange = { foodTeluguHint = it },
                    label = { Text("Regional / Telugu Hint (Optional, e.g. పుట్నాలు)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector Row
                Text(
                    text = "FOOD CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                val categories = listOf(
                    FoodCategory.CUSTOM,
                    FoodCategory.TIFFINS_BREADS,
                    FoodCategory.RICE_GRAINS,
                    FoodCategory.DALS_SAMBAR_RASAM,
                    FoodCategory.NON_VEG_EGGS,
                    FoodCategory.VEG_PROTEIN_BOOSTERS,
                    FoodCategory.VEPUDU_PORIYAL,
                    FoodCategory.CHUTNEYS_PODIS_FATS,
                    FoodCategory.DAIRY_SHAKES_SUPPLEMENTS
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else DarkBackground)
                                .border(1.dp, if (isSelected) ElectricCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) ElectricCyan else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Veg / Non-Veg Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .clickable { isVegetarian = !isVegetarian }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VegNonVegBadge(isVeg = isVegetarian, size = 16.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isVegetarian) "Vegetarian" else "Non-Vegetarian",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tap to toggle diet type",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = if (isVegetarian) "VEG" else "NON-VEG",
                        color = if (isVegetarian) NeonLime else CoralRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Section: Values Per 100g
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NUTRITION PER 100g (LABEL VALUES)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = "per 100g",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        // Calories & Protein Grid Row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = caloriesPer100gInput,
                                onValueChange = { caloriesPer100gInput = it },
                                label = { Text("Calories (kcal)", fontSize = 11.sp) },
                                placeholder = { Text("e.g. 360", color = TextMuted, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberGold,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = proteinPer100gInput,
                                onValueChange = { proteinPer100gInput = it },
                                label = { Text("Protein (g)", fontSize = 11.sp) },
                                placeholder = { Text("e.g. 24.0", color = TextMuted, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Carbs & Fat Grid Row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = carbsPer100gInput,
                                onValueChange = { carbsPer100gInput = it },
                                label = { Text("Carbohydrates (g)", fontSize = 11.sp) },
                                placeholder = { Text("e.g. 48.0", color = TextMuted, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = fatPer100gInput,
                                onValueChange = { fatPer100gInput = it },
                                label = { Text("Fat (g)", fontSize = 11.sp) },
                                placeholder = { Text("e.g. 8.0", color = TextMuted, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralRed,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (calsPer100g > 0 || calculatedKcalFromMacros > 0) {
                            Text(
                                text = "Macro Math Check: (P×4 + C×4 + F×9) = ~$calculatedKcalFromMacros kcal",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                        }
                    }
                }

                // Section: Serving Portion Definition
                Text(
                    text = "STANDARD SERVING PORTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Standard 100g chip
                    val isStd = portionMode == PortionMode.STANDARD_100G
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isStd) ElectricCyan.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (isStd) ElectricCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { portionMode = PortionMode.STANDARD_100G }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Standard 100g",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (isStd) ElectricCyan else TextSecondary
                            )
                            Text(
                                text = "1.0 = 100g • 0.5 = 50g",
                                fontSize = 9.5.sp,
                                color = if (isStd) TextPrimary else TextMuted
                            )
                        }
                    }

                    // Custom Household Portion chip
                    val isCustom = portionMode == PortionMode.CUSTOM_HOUSEHOLD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustom) ElectricCyan.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (isCustom) ElectricCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { portionMode = PortionMode.CUSTOM_HOUSEHOLD }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Custom Portion (g)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (isCustom) ElectricCyan else TextSecondary
                            )
                            Text(
                                text = "e.g. 1 scoop (30g), 1 bowl",
                                fontSize = 9.5.sp,
                                color = if (isCustom) TextPrimary else TextMuted
                            )
                        }
                    }
                }

                if (portionMode == PortionMode.CUSTOM_HOUSEHOLD) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = portionNameInput,
                            onValueChange = { portionNameInput = it },
                            label = { Text("Portion Name (e.g. 1 scoop, 1 bowl)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedTextField(
                            value = portionGramsInput,
                            onValueChange = { portionGramsInput = it },
                            label = { Text("Grams (g)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 30", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }

                // Live Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VegNonVegBadge(isVeg = isVegetarian, size = 12.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = foodName.ifBlank { "Preview Food Item" },
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp
                                )
                                if (foodTeluguHint.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($foodTeluguHint)",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            if (isHighProtein) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PurpleAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("High Protein", color = PurpleAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$finalCalories kcal • ${"%.1f".format(finalProtein)}g P • ${"%.1f".format(finalCarbs)}g C • ${"%.1f".format(finalFat)}g F  /  $finalServingUnit",
                            style = MaterialTheme.typography.bodySmall.copy(color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        )
                        if (portionMode == PortionMode.CUSTOM_HOUSEHOLD) {
                            Text(
                                text = "Base Label: $calsPer100g kcal • ${"%.1f".format(proteinPer100g)}g P • ${"%.1f".format(carbsPer100g)}g C • ${"%.1f".format(fatPer100g)}g F per 100g",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 9.5.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSave = foodName.isNotBlank() && (calsPer100g > 0 || proteinPer100g > 0f || carbsPer100g > 0f)

                    Button(
                        onClick = {
                            if (canSave) {
                                val customEntity = CustomFoodEntity(
                                    id = "custom_${System.currentTimeMillis()}",
                                    name = foodName.trim(),
                                    teluguOrRegionalHint = foodTeluguHint.trim(),
                                    category = selectedCategory.name,
                                    servingUnit = finalServingUnit,
                                    calories = finalCalories,
                                    proteinGrams = finalProtein,
                                    carbsGrams = finalCarbs,
                                    fatGrams = finalFat,
                                    isVegetarian = isVegetarian,
                                    isHighProteinPatch = isHighProtein
                                )
                                onSaveFood(customEntity)
                                Toast.makeText(
                                    context,
                                    "Added '${customEntity.name}' to Available Foods!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DarkBackground,
                            disabledContainerColor = DarkSurfaceBorder,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save to Available Foods", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
