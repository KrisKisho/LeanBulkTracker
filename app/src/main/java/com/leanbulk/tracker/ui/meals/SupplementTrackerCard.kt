package com.leanbulk.tracker.ui.meals

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.leanbulk.tracker.data.local.entity.SupplementLogEntity
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
fun SupplementTrackerCard(
    supplementLog: SupplementLogEntity,
    wheyCreatineMode: String, // "WHEY_WITH_CREATINE" or "WHEY_ONLY_NO_CREATINE"
    creatineSaturationPercent: Float,
    d3Supplement1Iu: Int,
    d3Supplement2Iu: Int,
    onToggleWheyMode: () -> Unit,
    onToggleSupplement: (String, Boolean) -> Unit, // "WHEY", "D3", "FISH_OIL", "MAGNESIUM"
    modifier: Modifier = Modifier
) {
    val isCreatineMode = wheyCreatineMode == "WHEY_WITH_CREATINE"
    val combinedDailyD3Iu = (if (supplementLog.vitD3B12K2Logged) d3Supplement1Iu else 0) +
            (if (supplementLog.fishOilLogged) d3Supplement2Iu else 0)
    val isD3Exceeded = combinedDailyD3Iu > 4000

    var hideWashoutBar by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Title, Subtitle, and Whey Mode Switcher Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "Supplements",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SUPPLEMENT STACK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "Daily Stack & Creatine Saturation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                // Whey Mode Switcher 1-Tap Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isCreatineMode) AmberGold.copy(alpha = 0.18f) else DarkBackground)
                        .border(
                            1.dp,
                            if (isCreatineMode) AmberGold else DarkSurfaceBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onToggleWheyMode() }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCreatineMode) "Whey + 3g Creatine" else "Plain Whey Only",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCreatineMode) AmberGold else TextSecondary
                    )
                }
            }

            // Creatine Saturation Gauge (when active or when washing out)
            if (isCreatineMode || (!hideWashoutBar && creatineSaturationPercent > 0f)) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground.copy(alpha = 0.8f))
                        .border(1.dp, if (isCreatineMode) AmberGold.copy(alpha = 0.35f) else DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Phosphocreatine Saturation",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCreatineMode) AmberGold else TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            if (!isCreatineMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Paused)",
                                    fontSize = 9.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "%.0f%%".format(creatineSaturationPercent),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (creatineSaturationPercent >= 85f) NeonLime else AmberGold,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (creatineSaturationPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (creatineSaturationPercent >= 85f) NeonLime else AmberGold,
                        trackColor = DarkSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCreatineMode) {
                                if (creatineSaturationPercent >= 90f) "Full intramuscular creatine saturation maintained"
                                else "Needs ~25 consecutive days @ 3g/day for peak saturation"
                            } else {
                                "Plain Whey Mode Active • Gradual 2.5%/day natural washout"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.5.sp
                            )
                        )

                        if (!isCreatineMode && creatineSaturationPercent <= 0f) {
                            Text(
                                text = "Hide Bar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.clickable { hideWashoutBar = true }
                            )
                        }
                    }
                }
            } else if (!isCreatineMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Plain Whey Mode Active (Creatine Paused) • Zero creatine logged",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Daily Supplement Rows
            SupplementCheckRow(
                title = "Osoaa Impact Whey",
                description = if (isCreatineMode) "24g Protein + 3g Creatine Monohydrate" else "24g Protein • Creatine-Free Formula",
                badgeText = if (isCreatineMode) "+3g Creatine" else "Clean Whey",
                badgeColor = if (isCreatineMode) AmberGold else ElectricCyan,
                checked = supplementLog.wheyLogged,
                onCheckedChange = { onToggleSupplement("WHEY", it) }
            )

            SupplementCheckRow(
                title = "Osoaa Vitamin D3 + B12 + K2 (MK-7)",
                description = "Fat-soluble bioavailable micro-nutrient support (${d3Supplement1Iu} IU)",
                badgeText = "Fat Meal Pair",
                badgeColor = ElectricCyan,
                checked = supplementLog.vitD3B12K2Logged,
                onCheckedChange = { onToggleSupplement("D3", it) }
            )

            SupplementCheckRow(
                title = "Carbamide Forte Salmon Fish Oil 1000mg+",
                description = "Rich in EPA / DHA Omega-3 fatty acids (${d3Supplement2Iu} IU D3 label)",
                badgeText = "EPA/DHA",
                badgeColor = ElectricCyan,
                checked = supplementLog.fishOilLogged,
                onCheckedChange = { onToggleSupplement("FISH_OIL", it) }
            )

            SupplementCheckRow(
                title = "Osoaa Chelated Magnesium Glycinate",
                description = "High-absorption muscular relaxation & deep restorative sleep",
                badgeText = "Night Rest",
                badgeColor = NeonLime,
                checked = supplementLog.magnesiumGlycinateLogged,
                onCheckedChange = { onToggleSupplement("MAGNESIUM", it) }
            )

            // Vitamin D3 Overlap Guardrail Banner
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isD3Exceeded) AmberGold else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Combined Label D3: $combinedDailyD3Iu IU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isD3Exceeded) AmberGold else TextSecondary,
                            fontSize = 10.5.sp
                        )
                    )
                }

                Text(
                    text = "Upper Limit: 4,000 IU/day",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.5.sp
                    )
                )
            }

            if (isD3Exceeded) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberGold.copy(alpha = 0.15f))
                        .border(1.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = AmberGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vitamin D3 Label Alert: Both D3+B12+K2 and Fish Oil contain Vitamin D3. Combined total ($combinedDailyD3Iu IU) exceeds 4,000 IU/day.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AmberGold,
                                fontSize = 10.5.sp,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplementCheckRow(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) DarkBackground.copy(alpha = 0.9f) else DarkBackground.copy(alpha = 0.4f))
            .border(
                0.5.dp,
                if (checked) NeonLime.copy(alpha = 0.35f) else DarkSurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 7.dp),
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
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                        color = if (checked) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}
