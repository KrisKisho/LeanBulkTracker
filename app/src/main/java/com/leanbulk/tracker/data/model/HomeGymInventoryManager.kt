package com.leanbulk.tracker.data.model

data class HomeGymInventory(
    val has4kgDumbbell: Boolean = true,
    val hasWeightedBackpack: Boolean = true,
    val hasSturdyTableOrBed: Boolean = true,
    val hasAdjustableDumbbell: Boolean = false,
    val hasBarbell: Boolean = false,
    val hasPullUpBar: Boolean = false,
    val hasResistanceBand: Boolean = false
) {
    val summaryString: String
        get() {
            val items = mutableListOf<String>()
            if (has4kgDumbbell) items.add("4kg DB")
            if (hasWeightedBackpack) items.add("Backpack")
            if (hasSturdyTableOrBed) items.add("Table/Bed")
            if (hasAdjustableDumbbell) items.add("Adj DB")
            if (hasBarbell) items.add("Barbell")
            if (hasPullUpBar) items.add("Pull-Up Bar")
            if (hasResistanceBand) items.add("Bands")
            return if (items.isNotEmpty()) items.joinToString(" • ") else "Bodyweight Only"
        }
}
