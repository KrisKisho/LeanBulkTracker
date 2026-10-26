package com.leanbulk.tracker.data.model

object AdaptiveMealResizer {

    /**
     * Dynamically adapts portion sizes, caloric payloads, and meal structures
     * according to the user's active [UserProfile] adaptive settings.
     */
    fun adaptMeals(meals: List<MealItem>, profile: UserProfile): List<MealItem> {
        if ((!profile.autoAdaptEnabled && !profile.bellyFatRecompMode) || meals.isEmpty()) {
            return meals
        }

        val offset = profile.adaptiveCalorieOffset
        val isSplitShake = profile.splitShakeModeActive

        val adapted = meals.map { meal ->
            when (meal.id) {
                "meal_breakfast" -> {
                    if (offset >= 150) {
                        meal.copy(
                            defaultCalories = meal.defaultCalories + 75,
                            vegCalories = meal.vegCalories + 75,
                            defaultFat = meal.defaultFat + 5,
                            defaultCarbs = meal.defaultCarbs + 8,
                            defaultTitle = "${stripAdaptationTag(meal.defaultTitle)} • [+75 kcal: +1 tsp Ghee on idlis & +1 extra idli]",
                            vegTitle = "${stripAdaptationTag(meal.vegTitle)} • [+75 kcal: +1 tsp Ghee & +1 extra idli]"
                        )
                    } else {
                        meal
                    }
                }
                "meal_shake" -> {
                    if (isSplitShake) {
                        meal.copy(
                            defaultCalories = 370,
                            defaultProtein = 12,
                            defaultCarbs = 48,
                            defaultFat = 14,
                            vegCalories = 370,
                            vegProtein = 12,
                            defaultTitle = "Light Pre-Noon Smoothie (1 Banana + 150ml Milk + 1 tbsp PB + 20g Oats • Whey shifted to 8:05 PM)"
                        )
                    } else if (offset >= 150) {
                        meal.copy(
                            defaultCalories = meal.defaultCalories + 75,
                            defaultProtein = meal.defaultProtein + 4,
                            defaultCarbs = meal.defaultCarbs + 5,
                            defaultFat = meal.defaultFat + 6,
                            defaultTitle = "${stripAdaptationTag(meal.defaultTitle)} • [+75 kcal: +1 extra tbsp Peanut Butter / 20g Oats]"
                        )
                    } else {
                        meal
                    }
                }
                "meal_lunch" -> {
                    if (offset >= 300) {
                        meal.copy(
                            defaultCalories = meal.defaultCalories + 150,
                            vegCalories = meal.vegCalories + 150,
                            defaultCarbs = meal.defaultCarbs + 28,
                            defaultFat = meal.defaultFat + 4,
                            defaultTitle = "${stripAdaptationTag(meal.defaultTitle)} • [+150 kcal: Scaled to 2.0 cups Rice + 1 tsp Ghee]",
                            vegTitle = "${stripAdaptationTag(meal.vegTitle)} • [+150 kcal: Scaled to 2.0 cups Rice + 1 tsp Ghee]"
                        )
                    } else {
                        meal
                    }
                }
                "meal_dinner" -> {
                    if (isSplitShake) {
                        meal.copy(
                            defaultCalories = 410,
                            defaultProtein = 22,
                            defaultCarbs = 58,
                            defaultFat = 10,
                            vegCalories = 410,
                            vegProtein = 22,
                            defaultTitle = "1 cup Steamed Rice + 2-Egg Homestyle Curry + 1 cup Curd (Lightened for post-workout)",
                            vegTitle = "1 cup Steamed Rice + Thick Dal Pappu + 1 cup Curd (Lightened for post-workout)"
                        )
                    } else if (offset <= -150) {
                        meal.copy(
                            defaultCalories = (meal.defaultCalories - 150).coerceAtLeast(350),
                            vegCalories = (meal.vegCalories - 150).coerceAtLeast(350),
                            defaultCarbs = (meal.defaultCarbs - 35).coerceAtLeast(30),
                            defaultTitle = "${stripAdaptationTag(meal.defaultTitle)} • [Waist Guardrail: Rice dropped to 0.75 cup]",
                            vegTitle = "${stripAdaptationTag(meal.vegTitle)} • [Waist Guardrail: Rice dropped to 0.75 cup]"
                        )
                    } else if (profile.bellyFatRecompMode) {
                        meal.copy(
                            defaultCalories = (meal.defaultCalories - 110).coerceAtLeast(350),
                            vegCalories = (meal.vegCalories - 110).coerceAtLeast(350),
                            defaultCarbs = (meal.defaultCarbs - 26).coerceAtLeast(30),
                            defaultTitle = "${stripAdaptationTag(meal.defaultTitle)} • [Waist Recomp: Rice reduced to 1.0 cup (-110 kcal)]",
                            vegTitle = "${stripAdaptationTag(meal.vegTitle)} • [Waist Recomp: Rice reduced to 1.0 cup (-110 kcal)]"
                        )
                    } else {
                        meal
                    }
                }
                else -> meal
            }
        }.toMutableList()

        if (isSplitShake) {
            val hasWhey = adapted.any { it.id == "meal_post_workout_whey" }
            if (!hasWhey) {
                val wheyMeal = MealItem(
                    id = "meal_post_workout_whey",
                    timeLabel = "8:05 PM",
                    mealName = "Post-Workout Whey Shake",
                    defaultTitle = "1 scoop Whey / Plant Protein in 200ml Water/Milk",
                    defaultCalories = 120,
                    defaultProtein = 24,
                    defaultCarbs = 2,
                    defaultFat = 1,
                    hasVegOption = false,
                    vegTitle = "",
                    isVeg = true,
                    isChecked = false
                )
                val dinnerIndex = adapted.indexOfFirst { it.id == "meal_dinner" }
                if (dinnerIndex >= 0) {
                    adapted.add(dinnerIndex, wheyMeal)
                } else {
                    adapted.add(wheyMeal)
                }
            }
        } else {
            // Remove uncompleted auto-inserted whey item if split shake mode is deactivated
            adapted.removeAll { it.id == "meal_post_workout_whey" && !it.isChecked }
        }

        return adapted
    }

    private fun stripAdaptationTag(text: String): String {
        val idx = text.indexOf(" • [")
        return if (idx >= 0) text.substring(0, idx).trim() else text
    }
}
