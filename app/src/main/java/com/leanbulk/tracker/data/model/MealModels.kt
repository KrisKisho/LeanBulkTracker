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

    fun getDefaultCuttingMeals(): List<MealItem> = listOf(
        MealItem(
            id = "meal_breakfast",
            timeLabel = "8:15 AM",
            mealName = "Breakfast (Tiffin)",
            defaultTitle = "3 Steamed Idlis + Thick Vegetable Sambar + Allam Chutney + 2 Whole Boiled Eggs + 2 Boiled Egg Whites",
            defaultCalories = 360,
            defaultProtein = 25,
            defaultCarbs = 44,
            defaultFat = 10,
            hasVegOption = true,
            vegTitle = "2 Pesarattu (Green Moong Dosa) + Sambar + 75g Low-Oil Podi Paneer",
            vegCalories = 360,
            vegProtein = 25,
            vegCarbs = 42,
            vegFat = 11,
            isVeg = false,
            isChecked = false,
            ingredients = emptyList(),
            ingredientsJson = ""
        ),
        MealItem(
            id = "meal_shake",
            timeLabel = "11:00 AM",
            mealName = "Lean Protein & Gut Refresher",
            defaultTitle = "1 Scoop Osoaa Whey in 200ml cold water + 1 glass Spiced Buttermilk (Majjiga) with curry leaves & ginger",
            defaultCalories = 175,
            defaultProtein = 27,
            defaultCarbs = 6,
            defaultFat = 4,
            hasVegOption = false,
            vegTitle = "",
            isVeg = true,
            isChecked = false,
            ingredients = emptyList(),
            ingredientsJson = ""
        ),
        MealItem(
            id = "meal_lunch",
            timeLabel = "1:30 PM",
            mealName = "Lunch (Cut Volume)",
            defaultTitle = "1.0 cup Steamed Rice + 150g Low-Oil Chicken Breast Curry + 2 katoris Poriyal + Pepper Rasam",
            defaultCalories = 450,
            defaultProtein = 39,
            defaultCarbs = 46,
            defaultFat = 12,
            hasVegOption = true,
            vegTitle = "1.0 cup Steamed Rice + 50g dry Soya Chunks Fry + 2 katoris Poriyal + Pepper Rasam",
            vegCalories = 450,
            vegProtein = 39,
            vegCarbs = 48,
            vegFat = 11,
            isVeg = false,
            isChecked = false,
            ingredients = emptyList(),
            ingredientsJson = ""
        ),
        MealItem(
            id = "meal_snack",
            timeLabel = "5:30 PM",
            mealName = "Pre-Workout Snack",
            defaultTitle = "0.75 cup Steamed Kala Chana or Green Moong Sprouts Sundal + Cucumber Slices + Black Filter Coffee",
            defaultCalories = 210,
            defaultProtein = 12,
            defaultCarbs = 28,
            defaultFat = 5,
            hasVegOption = false,
            vegTitle = "",
            isVeg = true,
            isChecked = false,
            ingredients = emptyList(),
            ingredientsJson = ""
        ),
        MealItem(
            id = "meal_dinner",
            timeLabel = "8:30 PM",
            mealName = "Light Recovery Dinner",
            defaultTitle = "2 Whole-Wheat Phulkas (no oil) + 2-Egg Curry + 1 cup Low-Fat Curd (Perugu)",
            defaultCalories = 435,
            defaultProtein = 29,
            defaultCarbs = 48,
            defaultFat = 14,
            hasVegOption = true,
            vegTitle = "2 Whole-Wheat Phulkas + 100g Paneer Bhurji (or Palakura Pappu) + 1 cup Low-Fat Curd",
            vegCalories = 435,
            vegProtein = 29,
            vegCarbs = 50,
            vegFat = 13,
            isVeg = false,
            isChecked = false,
            ingredients = emptyList(),
            ingredientsJson = ""
        )
    )

    fun getDefaultMeals(goalMode: String = "LEAN_BULK"): List<MealItem> {
        return if (goalMode == "WEIGHT_LOSS") getDefaultCuttingMeals() else getDefaultMeals()
    }

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
