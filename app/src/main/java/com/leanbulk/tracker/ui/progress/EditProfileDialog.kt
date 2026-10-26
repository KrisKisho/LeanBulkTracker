package com.leanbulk.tracker.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import com.leanbulk.tracker.ui.theme.CoralRed
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leanbulk.tracker.data.model.UserProfile
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var isCmUnit by remember { mutableStateOf(false) }

    fun toDisplay(inches: Float): String = if (isCmUnit) "%.1f".format(inches * 2.54f) else "%.1f".format(inches)
    fun fromDisplay(text: String, fallbackInches: Float): Float {
        val v = text.toFloatOrNull() ?: return fallbackInches
        return if (isCmUnit) v / 2.54f else v
    }

    var ageText by remember { mutableStateOf(currentProfile.age.toString()) }
    var sex by remember { mutableStateOf(currentProfile.sex) }
    var heightCmText by remember { mutableStateOf(currentProfile.heightCm.toInt().toString()) }

    var startWeightText by remember { mutableStateOf("%.1f".format(currentProfile.startWeightKg)) }
    var currentWeightText by remember { mutableStateOf("%.1f".format(currentProfile.currentWeightKg)) }
    var goalWeightText by remember { mutableStateOf("%.1f".format(currentProfile.goalWeightKg)) }
    var selectedGoalMode by remember { mutableStateOf(currentProfile.resolvedGoalMode) }
    var weeklyGain by remember {
        val initialPace = if (currentProfile.resolvedGoalMode == "WEIGHT_LOSS" && currentProfile.targetWeeklyGainKg < 0.25f) 0.50f else currentProfile.targetWeeklyGainKg
        mutableFloatStateOf(initialPace)
    }

    var wristText by remember { mutableStateOf(toDisplay(currentProfile.wristInches)) }
    var ankleText by remember { mutableStateOf(toDisplay(currentProfile.ankleInches)) }

    var waistText by remember { mutableStateOf(toDisplay(currentProfile.currentWaistInches)) }
    var chestText by remember { mutableStateOf(toDisplay(currentProfile.currentChestInches)) }
    var armText by remember { mutableStateOf(toDisplay(currentProfile.currentArmInches)) }
    var forearmText by remember { mutableStateOf(toDisplay(currentProfile.currentForearmInches)) }
    var neckText by remember { mutableStateOf(toDisplay(currentProfile.currentNeckInches)) }
    var thighText by remember { mutableStateOf(toDisplay(currentProfile.currentThighInches)) }
    var calfText by remember { mutableStateOf(toDisplay(currentProfile.currentCalfInches)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
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
                    Column {
                        Text(
                            text = "Body & Goals Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Biometrics, Trajectory & Frame Dimensions",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TRANSFORMATION GOAL MODE SELECTOR
                Text(
                    text = "TRANSFORMATION GOAL MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GoalModeTab(
                        label = "📈 Gain (Bulk)",
                        isSelected = selectedGoalMode == "LEAN_BULK",
                        activeColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedGoalMode = "LEAN_BULK"
                        if (weeklyGain < 0.15f || weeklyGain > 0.50f) weeklyGain = 0.30f
                    }

                    GoalModeTab(
                        label = "🔥 Lose (Cut)",
                        isSelected = selectedGoalMode == "WEIGHT_LOSS",
                        activeColor = CoralRed,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedGoalMode = "WEIGHT_LOSS"
                        if (weeklyGain < 0.25f || weeklyGain > 0.75f) weeklyGain = 0.50f
                    }

                    GoalModeTab(
                        label = "⚖️ Recomp",
                        isSelected = selectedGoalMode == "BODY_RECOMP",
                        activeColor = AmberGold,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedGoalMode = "BODY_RECOMP"
                        weeklyGain = 0.00f
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unit Toggle: Inches vs CM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIRCUMFERENCE UNITS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isCmUnit) ElectricCyan else Color.Transparent)
                                .clickable {
                                    if (isCmUnit) {
                                        isCmUnit = false
                                        wristText = toDisplay(fromDisplay(wristText, 6.1f))
                                        ankleText = toDisplay(fromDisplay(ankleText, 7.8f))
                                        waistText = toDisplay(fromDisplay(waistText, 30.0f))
                                        chestText = toDisplay(fromDisplay(chestText, 35.5f))
                                        armText = toDisplay(fromDisplay(armText, 12.0f))
                                        forearmText = toDisplay(fromDisplay(forearmText, 10.2f))
                                        neckText = toDisplay(fromDisplay(neckText, 13.8f))
                                        thighText = toDisplay(fromDisplay(thighText, 19.5f))
                                        calfText = toDisplay(fromDisplay(calfText, 13.0f))
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Inches",
                                color = if (!isCmUnit) DarkBackground else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCmUnit) ElectricCyan else Color.Transparent)
                                .clickable {
                                    if (!isCmUnit) {
                                        isCmUnit = true
                                        wristText = toDisplay(fromDisplay(wristText, 6.1f))
                                        ankleText = toDisplay(fromDisplay(ankleText, 7.8f))
                                        waistText = toDisplay(fromDisplay(waistText, 30.0f))
                                        chestText = toDisplay(fromDisplay(chestText, 35.5f))
                                        armText = toDisplay(fromDisplay(armText, 12.0f))
                                        forearmText = toDisplay(fromDisplay(forearmText, 10.2f))
                                        neckText = toDisplay(fromDisplay(neckText, 13.8f))
                                        thighText = toDisplay(fromDisplay(thighText, 19.5f))
                                        calfText = toDisplay(fromDisplay(calfText, 13.0f))
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Centimeters",
                                color = if (isCmUnit) DarkBackground else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Core Biometrics
                SectionTitle("1. CORE BIOMETRICS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Age (yrs)",
                        value = ageText,
                        onValueChange = { ageText = it },
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sex", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (sex == "Male") ElectricCyan.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { sex = "Male" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Male", color = if (sex == "Male") ElectricCyan else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (sex == "Female") AmberGold.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { sex = "Female" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Female", color = if (sex == "Female") AmberGold else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    ProfileInputField(
                        label = "Height (cm)",
                        value = heightCmText,
                        onValueChange = { heightCmText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Weight Trajectory Inputs
                SectionTitle("2. WEIGHT TRAJECTORY (KG)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Start (kg)",
                        value = startWeightText,
                        onValueChange = { startWeightText = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Current (kg)",
                        value = currentWeightText,
                        onValueChange = {
                            currentWeightText = it
                            val cw = it.toFloatOrNull()
                            val gw = goalWeightText.toFloatOrNull() ?: currentProfile.goalWeightKg
                            if (cw != null) {
                                if (gw < cw - 0.5f) {
                                    selectedGoalMode = "WEIGHT_LOSS"
                                    if (weeklyGain < 0.25f || weeklyGain > 0.75f) weeklyGain = 0.50f
                                } else if (gw > cw + 0.5f) {
                                    selectedGoalMode = "LEAN_BULK"
                                    if (weeklyGain < 0.15f || weeklyGain > 0.50f) weeklyGain = 0.30f
                                } else {
                                    selectedGoalMode = "BODY_RECOMP"
                                    weeklyGain = 0.00f
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Goal (kg)",
                        value = goalWeightText,
                        onValueChange = {
                            goalWeightText = it
                            val gw = it.toFloatOrNull()
                            val cw = currentWeightText.toFloatOrNull() ?: currentProfile.currentWeightKg
                            if (gw != null) {
                                if (gw < cw - 0.5f) {
                                    selectedGoalMode = "WEIGHT_LOSS"
                                    if (weeklyGain < 0.25f || weeklyGain > 0.75f) weeklyGain = 0.50f
                                } else if (gw > cw + 0.5f) {
                                    selectedGoalMode = "LEAN_BULK"
                                    if (weeklyGain < 0.15f || weeklyGain > 0.50f) weeklyGain = 0.30f
                                } else {
                                    selectedGoalMode = "BODY_RECOMP"
                                    weeklyGain = 0.00f
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Healthy BMI Safety Guardrail (Calculated live from Height & Goal Weight)
                val heightCmVal = heightCmText.toFloatOrNull() ?: currentProfile.heightCm
                val goalWeightKgVal = goalWeightText.toFloatOrNull() ?: currentProfile.goalWeightKg
                val heightMVal = heightCmVal / 100f
                val goalBmi = if (heightMVal > 0f) goalWeightKgVal / (heightMVal * heightMVal) else 22f
                val minHealthyWeightKg = 19.0f * (heightMVal * heightMVal)

                if (goalBmi < 19.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberGold.copy(alpha = 0.15f))
                            .border(1.dp, AmberGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Healthy BMI Safety Guardrail",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Goal BMI of %.1f is below the clinical minimum healthy threshold (19.0). Recommended minimum healthy goal weight for %d cm is %.1f kg.".format(goalBmi, heightCmVal.toInt(), minHealthyWeightKg),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.5.sp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    goalWeightText = "%.1f".format(minHealthyWeightKg)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberGold,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reset to Min Healthy Weight (%.1f kg)".format(minHealthyWeightKg), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Target Weekly Pace Slider (Dynamic based on selectedGoalMode)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val paceTitle = when (selectedGoalMode) {
                        "WEIGHT_LOSS" -> "Target Weekly Fat Loss Pace"
                        "BODY_RECOMP" -> "Target Weekly Recomp Pace"
                        else -> "Target Weekly Gain Pace"
                    }
                    val paceFormatted = when (selectedGoalMode) {
                        "WEIGHT_LOSS" -> "-%.2f kg / week".format(weeklyGain)
                        "BODY_RECOMP" -> "±%.2f kg / week".format(weeklyGain)
                        else -> "+%.2f kg / week".format(weeklyGain)
                    }
                    val paceColor = when (selectedGoalMode) {
                        "WEIGHT_LOSS" -> CoralRed
                        "BODY_RECOMP" -> AmberGold
                        else -> NeonLime
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = paceTitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        )
                        Text(
                            text = paceFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = paceColor)
                        )
                    }

                    if (selectedGoalMode == "WEIGHT_LOSS") {
                        Slider(
                            value = weeklyGain.coerceIn(0.25f, 0.75f),
                            onValueChange = { weeklyGain = (it * 20).toInt() / 20f },
                            valueRange = 0.25f..0.75f,
                            colors = SliderDefaults.colors(
                                thumbColor = CoralRed,
                                activeTrackColor = CoralRed,
                                inactiveTrackColor = DarkSurfaceBorder
                            )
                        )
                        Text(
                            text = "Recommended Fat Loss Zone: -0.25 to -0.75 kg/week (-275 to -750 kcal deficit)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    } else if (selectedGoalMode == "BODY_RECOMP") {
                        Slider(
                            value = weeklyGain.coerceIn(0.00f, 0.20f),
                            onValueChange = { weeklyGain = (it * 20).toInt() / 20f },
                            valueRange = 0.00f..0.20f,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberGold,
                                activeTrackColor = AmberGold,
                                inactiveTrackColor = DarkSurfaceBorder
                            )
                        )
                        Text(
                            text = "Body Recomp Zone: Maintain scale weight while trimming navel waist & building muscle frame",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    } else {
                        Slider(
                            value = weeklyGain.coerceIn(0.15f, 0.50f),
                            onValueChange = { weeklyGain = (it * 20).toInt() / 20f },
                            valueRange = 0.15f..0.50f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonLime,
                                activeTrackColor = NeonLime,
                                inactiveTrackColor = DarkSurfaceBorder
                            )
                        )
                        Text(
                            text = "Recommended Lean Bulk Zone: +0.25 to +0.35 kg/week (+275 to +385 kcal surplus)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: Bone Structure Inputs
                SectionTitle("3. BONE STRUCTURE (${if (isCmUnit) "CM" else "INCHES"})")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Wrist Size",
                        value = wristText,
                        onValueChange = {
                            wristText = it
                            val w = it.toFloatOrNull()
                            if (w != null && ankleText.isBlank()) {
                                ankleText = "%.1f".format(w * 1.28f)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Ankle Size (optional)",
                        value = ankleText,
                        onValueChange = { ankleText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 4: Current Baseline Body Circumferences
                SectionTitle("4. BASELINE CIRCUMFERENCES (${if (isCmUnit) "CM" else "INCHES"})")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Navel Waist",
                        value = waistText,
                        onValueChange = { waistText = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Chest",
                        value = chestText,
                        onValueChange = { chestText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Flexed Arm",
                        value = armText,
                        onValueChange = { armText = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Forearm",
                        value = forearmText,
                        onValueChange = { forearmText = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Neck",
                        value = neckText,
                        onValueChange = { neckText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInputField(
                        label = "Thigh",
                        value = thighText,
                        onValueChange = { thighText = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileInputField(
                        label = "Calf",
                        value = calfText,
                        onValueChange = { calfText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        val parsedWrist = fromDisplay(wristText, currentProfile.wristInches)
                        val parsedAnkle = fromDisplay(ankleText, parsedWrist * 1.28f)
                        val updated = currentProfile.copy(
                            age = ageText.toIntOrNull() ?: currentProfile.age,
                            sex = sex,
                            heightCm = heightCmText.toFloatOrNull() ?: currentProfile.heightCm,
                            startWeightKg = startWeightText.toFloatOrNull() ?: currentProfile.startWeightKg,
                            currentWeightKg = currentWeightText.toFloatOrNull() ?: currentProfile.currentWeightKg,
                            goalWeightKg = goalWeightText.toFloatOrNull() ?: currentProfile.goalWeightKg,
                            targetWeeklyGainKg = weeklyGain,
                            goalMode = selectedGoalMode,
                            wristInches = parsedWrist,
                            ankleInches = parsedAnkle,
                            currentWaistInches = fromDisplay(waistText, currentProfile.currentWaistInches),
                            currentChestInches = fromDisplay(chestText, currentProfile.currentChestInches),
                            currentArmInches = fromDisplay(armText, currentProfile.currentArmInches),
                            currentForearmInches = fromDisplay(forearmText, currentProfile.currentForearmInches),
                            currentNeckInches = fromDisplay(neckText, currentProfile.currentNeckInches),
                            currentThighInches = fromDisplay(thighText, currentProfile.currentThighInches),
                            currentCalfInches = fromDisplay(calfText, currentProfile.currentCalfInches),
                            // Preserve initial baseline measurements
                            startWaistInches = if (currentProfile.startWaistInches > 0f) currentProfile.startWaistInches else currentProfile.currentWaistInches,
                            startChestInches = if (currentProfile.startChestInches > 0f) currentProfile.startChestInches else currentProfile.currentChestInches,
                            startArmInches = if (currentProfile.startArmInches > 0f) currentProfile.startArmInches else currentProfile.currentArmInches,
                            startForearmInches = if (currentProfile.startForearmInches > 0f) currentProfile.startForearmInches else currentProfile.currentForearmInches,
                            startNeckInches = if (currentProfile.startNeckInches > 0f) currentProfile.startNeckInches else currentProfile.currentNeckInches,
                            startThighInches = if (currentProfile.startThighInches > 0f) currentProfile.startThighInches else currentProfile.currentThighInches,
                            startCalfInches = if (currentProfile.startCalfInches > 0f) currentProfile.startCalfInches else currentProfile.currentCalfInches
                        )
                        onSave(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Save & Recalculate Trajectory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = AmberGold,
            letterSpacing = 0.8.sp
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GoalModeTab(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.22f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.5.sp,
            maxLines = 1
        )
    }
}
