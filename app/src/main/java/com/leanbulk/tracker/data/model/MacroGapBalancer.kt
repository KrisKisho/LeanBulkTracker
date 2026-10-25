package com.leanbulk.tracker.data.model

data class MacroPatchOption(
    val id: String,
    val foodItemId: String,
    val label: String,
    val quantity: Float,
    val addedCalories: Int,
    val addedProtein: Float,
    val addedCarbs: Float,
    val addedFat: Float,
    val isVegetarian: Boolean
)

data class MacroGapAnalysis(
    val targetCalories: Int,
    val targetProtein: Float,
    val currentCalories: Int,
    val currentProtein: Float,
    val calorieGap: Int,
    val proteinGap: Float,
    val isProteinShort: Boolean,
    val isCalorieShort: Boolean,
    val isSurplus: Boolean,
    val statusMessage: String,
    val patches: List<MacroPatchOption>
)

object MacroGapBalancer {

    fun analyzeGap(
        targetCalories: Int,
        targetProtein: Float,
        currentCalories: Int,
        currentProtein: Float,
        isVegetarian: Boolean
    ): MacroGapAnalysis {
        val calorieGap = targetCalories - currentCalories
        val proteinGap = targetProtein - currentProtein
        val isProteinShort = proteinGap >= 10.0f
        val isCalorieShort = !isProteinShort && calorieGap >= 100
        val isSurplus = calorieGap < -100

        val patches = mutableListOf<MacroPatchOption>()

        val statusMessage = when {
            isProteinShort -> "Protein Short by %.1fg • Tap a South Indian Protein Patch:".format(proteinGap)
            isCalorieShort -> "Protein Met! Calories Short by ${calorieGap} kcal • Tap a Calorie Patch:"
            isSurplus -> "Plate In Healthy Surplus (+${-calorieGap} kcal)!"
            else -> "Plate Well-Balanced to Slot Target!"
        }

        if (isProteinShort) {
            // South Indian Protein Patch Chips (Respecting Veg/Non-Veg Preference)
            if (!isVegetarian) {
                patches.add(
                    MacroPatchOption(
                        id = "patch_eggs_2",
                        foodItemId = "boiled_egg_whole",
                        label = "+2 Boiled Eggs (+144 kcal | +12.6g P)",
                        quantity = 2.0f,
                        addedCalories = 144,
                        addedProtein = 12.6f,
                        addedCarbs = 1.2f,
                        addedFat = 9.6f,
                        isVegetarian = false
                    )
                )
                patches.add(
                    MacroPatchOption(
                        id = "patch_egg_whites_3",
                        foodItemId = "boiled_egg_white",
                        label = "+3 Egg Whites (+51 kcal | +10.8g P)",
                        quantity = 3.0f,
                        addedCalories = 51,
                        addedProtein = 10.8f,
                        addedCarbs = 0.6f,
                        addedFat = 0.0f,
                        isVegetarian = false
                    )
                )
            }

            // High-protein veg options
            patches.add(
                MacroPatchOption(
                    id = "patch_soya_50g",
                    foodItemId = "soya_chunks_curry",
                    label = "+50g Soya Chunks Fry (+195 kcal | +26g P)",
                    quantity = 1.0f,
                    addedCalories = 195,
                    addedProtein = 26.0f,
                    addedCarbs = 16.0f,
                    addedFat = 3.0f,
                    isVegetarian = true
                )
            )

            patches.add(
                MacroPatchOption(
                    id = "patch_paneer_100g",
                    foodItemId = "paneer_podi_fry",
                    label = "+100g Paneer Bhurji (+295 kcal | +18.5g P)",
                    quantity = 1.0f,
                    addedCalories = 295,
                    addedProtein = 18.5f,
                    addedCarbs = 5.5f,
                    addedFat = 22.0f,
                    isVegetarian = true
                )
            )

            patches.add(
                MacroPatchOption(
                    id = "patch_whey_1",
                    foodItemId = "osoaa_whey_scoop",
                    label = "+1 Scoop Osoaa Whey in Water (+130 kcal | +24g P)",
                    quantity = 1.0f,
                    addedCalories = 130,
                    addedProtein = 24.0f,
                    addedCarbs = 3.5f,
                    addedFat = 2.0f,
                    isVegetarian = true
                )
            )

            patches.add(
                MacroPatchOption(
                    id = "patch_sundal_1",
                    foodItemId = "kala_chana_sundal",
                    label = "+1 Cup Kala Chana Sundal (+240 kcal | +11.5g P)",
                    quantity = 1.0f,
                    addedCalories = 240,
                    addedProtein = 11.5f,
                    addedCarbs = 36.0f,
                    addedFat = 5.5f,
                    isVegetarian = true
                )
            )
        } else if (isCalorieShort) {
            // South Indian Calorie Patch Chips
            patches.add(
                MacroPatchOption(
                    id = "patch_ghee_2tsp",
                    foodItemId = "cow_ghee",
                    label = "+2 tsp Cow Ghee on Rice/Idli (+90 kcal | +10g F)",
                    quantity = 2.0f,
                    addedCalories = 90,
                    addedProtein = 0.0f,
                    addedCarbs = 0.0f,
                    addedFat = 10.0f,
                    isVegetarian = true
                )
            )
            patches.add(
                MacroPatchOption(
                    id = "patch_rice_half_cup",
                    foodItemId = "sona_masoori_rice",
                    label = "+0.5 cup Steamed Rice (+88 kcal | +19g C)",
                    quantity = 0.5f,
                    addedCalories = 88,
                    addedProtein = 1.8f,
                    addedCarbs = 19.0f,
                    addedFat = 0.2f,
                    isVegetarian = true
                )
            )
            patches.add(
                MacroPatchOption(
                    id = "patch_banana_1",
                    foodItemId = "banana_medium",
                    label = "+1 Banana (+105 kcal | +27g C)",
                    quantity = 1.0f,
                    addedCalories = 105,
                    addedProtein = 1.3f,
                    addedCarbs = 27.0f,
                    addedFat = 0.3f,
                    isVegetarian = true
                )
            )
            patches.add(
                MacroPatchOption(
                    id = "patch_palli_chutney_2tbsp",
                    foodItemId = "palli_chutney",
                    label = "+2 tbsp Palli Chutney (+110 kcal | +4.2g P)",
                    quantity = 1.0f,
                    addedCalories = 110,
                    addedProtein = 4.2f,
                    addedCarbs = 5.0f,
                    addedFat = 8.5f,
                    isVegetarian = true
                )
            )
            patches.add(
                MacroPatchOption(
                    id = "patch_peanut_butter_1tbsp",
                    foodItemId = "peanut_butter",
                    label = "+1 tbsp Peanut Butter (+95 kcal | +4g P)",
                    quantity = 1.0f,
                    addedCalories = 95,
                    addedProtein = 4.0f,
                    addedCarbs = 3.0f,
                    addedFat = 8.0f,
                    isVegetarian = true
                )
            )
        }

        return MacroGapAnalysis(
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            currentCalories = currentCalories,
            currentProtein = currentProtein,
            calorieGap = calorieGap,
            proteinGap = proteinGap,
            isProteinShort = isProteinShort,
            isCalorieShort = isCalorieShort,
            isSurplus = isSurplus,
            statusMessage = statusMessage,
            patches = patches
        )
    }
}
