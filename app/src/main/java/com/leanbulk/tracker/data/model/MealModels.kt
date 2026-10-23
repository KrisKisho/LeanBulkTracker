package com.leanbulk.tracker.data.model

data class MealItem(
    val id: String,
    val timeLabel: String,
    val mealName: String,
    val defaultTitle: String,
    val defaultCalories: Int,
    val defaultProtein: Int,
    val defaultCarbs: Int = 0,
    val defaultFat: Int = 0,
    val hasVegOption: Boolean = false,
    val vegTitle: String = "",
    val vegCalories: Int = defaultCalories,
    val vegProtein: Int = defaultProtein,
    val vegCarbs: Int = defaultCarbs,
    val vegFat: Int = defaultFat,
    val isVeg: Boolean = false,
    val isChecked: Boolean = false,
    val isCustom: Boolean = false,
    val ingredients: List<MealIngredient> = emptyList(),
    val ingredientsJson: String = ""
) {
    val activeTitle: String
        get() = if (ingredients.isNotEmpty()) {
            FoodCatalog.buildSummaryTitle(ingredients)
        } else if (hasVegOption && isVeg) vegTitle else defaultTitle

    val activeCalories: Int
        get() = if (ingredients.isNotEmpty()) {
            ingredients.sumOf { it.calories }
        } else if (hasVegOption && isVeg) vegCalories else defaultCalories

    val activeProtein: Int
        get() = if (ingredients.isNotEmpty()) {
            ingredients.sumOf { it.protein.toDouble() }.toInt()
        } else if (hasVegOption && isVeg) vegProtein else defaultProtein

    val activeCarbs: Int
        get() = if (ingredients.isNotEmpty()) {
            ingredients.sumOf { it.carbs.toDouble() }.toInt()
        } else if (hasVegOption && isVeg) vegCarbs else defaultCarbs

    val activeFat: Int
        get() = if (ingredients.isNotEmpty()) {
            ingredients.sumOf { it.fat.toDouble() }.toInt()
        } else if (hasVegOption && isVeg) vegFat else defaultFat
}

object MealDataSource {
    fun getDefaultMeals(): List<MealItem> = listOf(
        MealItem(
            id = "meal_breakfast",
            timeLabel = "8:15 AM",
            mealName = "Breakfast (Tiffin)",
            defaultTitle = "4 Idlis or 2 Dosas + Thick Sambar + 2 Boiled Eggs / Podimas",
            defaultCalories = 520,
            defaultProtein = 26,
            defaultCarbs = 68,
            defaultFat = 14,
            hasVegOption = true,
            vegTitle = "4 Idlis or 2 Dosas + Thick Sambar + 75g Podi Paneer or Pesarattu",
            vegCalories = 520,
            vegProtein = 26,
            vegCarbs = 66,
            vegFat = 15,
            isVeg = false,
            isChecked = false,
            ingredients = FoodCatalog.getDefaultIngredientsForMeal("meal_breakfast", false),
            ingredientsJson = IngredientJsonHelper.toJsonString(FoodCatalog.getDefaultIngredientsForMeal("meal_breakfast", false))
        ),
        MealItem(
            id = "meal_shake",
            timeLabel = "11:00 AM",
            mealName = "Mid-Morning Shake",
            defaultTitle = "250ml Whole Milk + 1 Banana + 2 tbsp Peanut Butter + 30g Powdered Oats + 1 scoop Protein Powder",
            defaultCalories = 490,
            defaultProtein = 36,
            defaultCarbs = 52,
            defaultFat = 16,
            hasVegOption = false,
            vegTitle = "",
            isVeg = true,
            isChecked = false,
            ingredients = FoodCatalog.getDefaultIngredientsForMeal("meal_shake", true),
            ingredientsJson = IngredientJsonHelper.toJsonString(FoodCatalog.getDefaultIngredientsForMeal("meal_shake", true))
        ),
        MealItem(
            id = "meal_lunch",
            timeLabel = "1:30 PM",
            mealName = "Lunch",
            defaultTitle = "1.5 cups Steamed Rice + 150g Andhra Chicken Curry + Vegetable Poriyal",
            defaultCalories = 570,
            defaultProtein = 38,
            defaultCarbs = 80,
            defaultFat = 12,
            hasVegOption = true,
            vegTitle = "1.5 cups Steamed Rice + 50g dry Soya Chunks Curry (or 100g Paneer) + Vegetable Poriyal",
            vegCalories = 570,
            vegProtein = 38,
            vegCarbs = 82,
            vegFat = 11,
            isVeg = false,
            isChecked = false,
            ingredients = FoodCatalog.getDefaultIngredientsForMeal("meal_lunch", false),
            ingredientsJson = IngredientJsonHelper.toJsonString(FoodCatalog.getDefaultIngredientsForMeal("meal_lunch", false))
        ),
        MealItem(
            id = "meal_snack",
            timeLabel = "5:00 PM",
            mealName = "Pre-Workout Snack",
            defaultTitle = "1 cup Tempered Kala Chana Sundal with grated coconut + Filter Coffee / Green Tea",
            defaultCalories = 310,
            defaultProtein = 13,
            defaultCarbs = 42,
            defaultFat = 9,
            hasVegOption = false,
            vegTitle = "",
            isVeg = true,
            isChecked = false,
            ingredients = FoodCatalog.getDefaultIngredientsForMeal("meal_snack", true),
            ingredientsJson = IngredientJsonHelper.toJsonString(FoodCatalog.getDefaultIngredientsForMeal("meal_snack", true))
        ),
        MealItem(
            id = "meal_dinner",
            timeLabel = "8:30 PM",
            mealName = "Dinner",
            defaultTitle = "1.5 cups Steamed Rice + 2-Egg Homestyle Curry + 1 cup Curd (Perugu)",
            defaultCalories = 530,
            defaultProtein = 23,
            defaultCarbs = 78,
            defaultFat = 13,
            hasVegOption = true,
            vegTitle = "1.5 cups Steamed Rice + 1 cup Thick Toor Dal Pappu with Ghee + 1 cup Curd (Perugu)",
            vegCalories = 530,
            vegProtein = 23,
            vegCarbs = 82,
            vegFat = 12,
            isVeg = false,
            isChecked = false,
            ingredients = FoodCatalog.getDefaultIngredientsForMeal("meal_dinner", false),
            ingredientsJson = IngredientJsonHelper.toJsonString(FoodCatalog.getDefaultIngredientsForMeal("meal_dinner", false))
        )
    )

    data class QuickPreset(
        val label: String,
        val calories: Int,
        val protein: Int,
        val carbs: Int = 0,
        val fat: Int = 0
    )

    val quickAddPresets = listOf(
        QuickPreset("+0.5 Cup Steamed Rice", 150, 3, 34, 0),
        QuickPreset("+1 tbsp Peanut Butter", 95, 4, 3, 8),
        QuickPreset("+1 Boiled Egg", 75, 6, 0, 5),
        QuickPreset("+1 Cup Curd (Perugu)", 100, 5, 8, 4),
        QuickPreset("+1 Banana", 105, 1, 27, 0)
    )
}
