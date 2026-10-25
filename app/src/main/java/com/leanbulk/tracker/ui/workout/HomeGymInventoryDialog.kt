package com.leanbulk.tracker.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leanbulk.tracker.data.model.HomeGymInventory
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary

@Composable
fun HomeGymInventoryDialog(
    initialInventory: HomeGymInventory,
    onDismiss: () -> Unit,
    onSave: (HomeGymInventory) -> Unit
) {
    var has4kgDb by remember { mutableStateOf(initialInventory.has4kgDumbbell) }
    var hasBackpack by remember { mutableStateOf(initialInventory.hasWeightedBackpack) }
    var hasTable by remember { mutableStateOf(initialInventory.hasSturdyTableOrBed) }
    var hasAdjDb by remember { mutableStateOf(initialInventory.hasAdjustableDumbbell) }
    var hasBarbell by remember { mutableStateOf(initialInventory.hasBarbell) }
    var hasPullUp by remember { mutableStateOf(initialInventory.hasPullUpBar) }
    var hasBands by remember { mutableStateOf(initialInventory.hasResistanceBand) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkBackground)
                .border(1.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Home Gym Inventory",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Auto-calculates loads & exercise swaps",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELECT AVAILABLE EQUIPMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                EquipmentCheckRow(
                    title = "Single 4 kg Dumbbell",
                    subtitle = "Base training weight for strict tempo and isolation",
                    checked = has4kgDb,
                    onCheckedChange = { has4kgDb = it }
                )

                EquipmentCheckRow(
                    title = "Weighted Backpack (Water Bottles / Books)",
                    subtitle = "1L bottle = 1.0 kg • Overload squats & presses up to 12 kg",
                    checked = hasBackpack,
                    onCheckedChange = { hasBackpack = it }
                )

                EquipmentCheckRow(
                    title = "Sturdy Table / Heavy Bed",
                    subtitle = "Inverted bodyweight rows & elevated push-ups",
                    checked = hasTable,
                    onCheckedChange = { hasTable = it }
                )

                EquipmentCheckRow(
                    title = "Adjustable Dumbbells (Spin-lock)",
                    subtitle = "Auto-calculates plate math per side (1.5 kg rod)",
                    checked = hasAdjDb,
                    onCheckedChange = { hasAdjDb = it }
                )

                EquipmentCheckRow(
                    title = "Barbell with Weight Plates",
                    subtitle = "Unlocks 1-tap swap to barbell squats, presses & rows",
                    checked = hasBarbell,
                    onCheckedChange = { hasBarbell = it }
                )

                EquipmentCheckRow(
                    title = "Doorway Pull-Up Bar",
                    subtitle = "Unlocks bodyweight pull-up swaps for Tuesday/Saturday back",
                    checked = hasPullUp,
                    onCheckedChange = { hasPullUp = it }
                )

                EquipmentCheckRow(
                    title = "Resistance Bands (Loop / Tube)",
                    subtitle = "Adds continuous tension curve and face pull variations",
                    checked = hasBands,
                    onCheckedChange = { hasBands = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updated = HomeGymInventory(
                                has4kgDumbbell = has4kgDb,
                                hasWeightedBackpack = hasBackpack,
                                hasSturdyTableOrBed = hasTable,
                                hasAdjustableDumbbell = hasAdjDb,
                                hasBarbell = hasBarbell,
                                hasPullUpBar = hasPullUp,
                                hasResistanceBand = hasBands
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Inventory", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentCheckRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (checked) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (checked) NeonLime.copy(alpha = 0.4f) else DarkSurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = NeonLime,
                checkmarkColor = DarkBackground,
                uncheckedColor = TextMuted
            ),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                    color = if (checked) TextPrimary else TextSecondary,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}
