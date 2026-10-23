package com.leanbulk.tracker.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class FoodCategory(val displayName: String) {
    GRAINS("Grains & Staples"),
    DALS_CURRIES("Dals & Curries"),
    MEAT_EGGS("Chicken, Eggs & Fish"),
    DAIRY("Milk & Dairy"),
    PROTEIN_POWDER("Protein Powder"),
    FATS_EXTRAS("Fats, Nuts & Extras"),
    VEGGIES("Sides & Vegetables"),
    CUSTOM("Custom")
}

data class FoodItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val unit: String,
    val baseServingSize: Float,
    val caloriesPerBase: Float,
    val proteinPerBase: Float,
    val carbsPerBase: Float,
    val fatPerBase: Float,
    val stepSize: Float,
    val isVeg: Boolean = true
) {
    fun calculateMacros(quantity: Float): MealIngredient {
        val factor = if (baseServingSize > 0f) quantity / baseServingSize else 0f
        return MealIngredient(
            foodItemId = id,
            name = name,
            quantity = quantity,
            unit = unit,
            calories = (caloriesPerBase * factor).toInt(),
            protein = (proteinPerBase * factor * 10).toInt() / 10f,
            carbs = (carbsPerBase * factor * 10).toInt() / 10f,
            fat = (fatPerBase * factor * 10).toInt() / 10f,
            category = category,
            isVeg = isVeg
        )
    }
}

data class MealIngredient(
    val foodItemId: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val category: FoodCategory = FoodCategory.CUSTOM,
    val isVeg: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("foodItemId", foodItemId)
            put("name", name)
            put("quantity", quantity.toDouble())
            put("unit", unit)
            put("calories", calories)
            put("protein", protein.toDouble())
            put("carbs", carbs.toDouble())
            put("fat", fat.toDouble())
            put("category", category.name)
            put("isVeg", isVeg)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): MealIngredient {
            val catName = obj.optString("category", FoodCategory.CUSTOM.name)
            val cat = try {
                FoodCategory.valueOf(catName)
            } catch (e: Exception) {
                FoodCategory.CUSTOM
            }
            return MealIngredient(
                foodItemId = obj.optString("foodItemId", ""),
                name = obj.optString("name", "Ingredient"),
                quantity = obj.optDouble("quantity", 100.0).toFloat(),
                unit = obj.optString("unit", "g"),
                calories = obj.optInt("calories", 0),
                protein = obj.optDouble("protein", 0.0).toFloat(),
                carbs = obj.optDouble("carbs", 0.0).toFloat(),
                fat = obj.optDouble("fat", 0.0).toFloat(),
                category = cat,
                isVeg = obj.optBoolean("isVeg", true)
            )
        }
    }
}

object IngredientJsonHelper {
    fun toJsonString(ingredients: List<MealIngredient>): String {
        val array = JSONArray()
        ingredients.forEach { array.put(it.toJson()) }
        return array.toString()
    }

    fun fromJsonString(jsonStr: String): List<MealIngredient> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MealIngredient>()
            for (i in 0 until array.length()) {
                list.add(MealIngredient.fromJson(array.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}

object FoodCatalog {
    val items: List<FoodItem> = listOf(
        // Grains & Staples (per 100g cooked unless noted)
        FoodItem(
            id = "grain_steamed_rice",
            name = "Steamed Rice (Sonamasuri/Ponni)",
            category = FoodCategory.GRAINS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 130f,
            proteinPerBase = 2.7f,
            carbsPerBase = 28.2f,
            fatPerBase = 0.3f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_brown_rice",
            name = "Brown Rice (Cooked)",
            category = FoodCategory.GRAINS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 112f,
            proteinPerBase = 2.6f,
            carbsPerBase = 23.5f,
            fatPerBase = 0.9f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_idli",
            name = "Steamed Idli",
            category = FoodCategory.GRAINS,
            unit = "idli",
            baseServingSize = 1f,
            caloriesPerBase = 60f,
            proteinPerBase = 2.0f,
            carbsPerBase = 12.0f,
            fatPerBase = 0.3f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_dosa",
            name = "Plain Dosa",
            category = FoodCategory.GRAINS,
            unit = "dosa",
            baseServingSize = 1f,
            caloriesPerBase = 140f,
            proteinPerBase = 3.0f,
            carbsPerBase = 20.0f,
            fatPerBase = 5.0f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_pesarattu",
            name = "Pesarattu (Moong Dosa)",
            category = FoodCategory.GRAINS,
            unit = "dosa",
            baseServingSize = 1f,
            caloriesPerBase = 130f,
            proteinPerBase = 7.0f,
            carbsPerBase = 18.0f,
            fatPerBase = 2.5f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_roti",
            name = "Whole Wheat Roti / Phulka",
            category = FoodCategory.GRAINS,
            unit = "roti",
            baseServingSize = 1f,
            caloriesPerBase = 80f,
            proteinPerBase = 3.0f,
            carbsPerBase = 15.0f,
            fatPerBase = 1.0f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_oats",
            name = "Rolled / Powdered Oats",
            category = FoodCategory.GRAINS,
            unit = "g",
            baseServingSize = 10f,
            caloriesPerBase = 39f,
            proteinPerBase = 1.3f,
            carbsPerBase = 6.7f,
            fatPerBase = 0.7f,
            stepSize = 10f,
            isVeg = true
        ),
        FoodItem(
            id = "grain_upma",
            name = "Rava Upma",
            category = FoodCategory.GRAINS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 145f,
            proteinPerBase = 3.2f,
            carbsPerBase = 24.0f,
            fatPerBase = 4.0f,
            stepSize = 25f,
            isVeg = true
        ),

        // Dals & Curries (per 100g cooked)
        FoodItem(
            id = "dal_toor_pappu",
            name = "Thick Toor Dal Pappu",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 90f,
            proteinPerBase = 5.5f,
            carbsPerBase = 14.0f,
            fatPerBase = 1.5f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "dal_moong",
            name = "Moong Dal (Pasi Paruppu)",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 85f,
            proteinPerBase = 6.0f,
            carbsPerBase = 13.0f,
            fatPerBase = 1.0f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "dal_chana",
            name = "Chana Dal Curry",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 115f,
            proteinPerBase = 6.5f,
            carbsPerBase = 18.0f,
            fatPerBase = 2.0f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "dal_sambar",
            name = "Thick South Indian Sambar",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 65f,
            proteinPerBase = 3.0f,
            carbsPerBase = 10.0f,
            fatPerBase = 1.5f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "curry_soya_chunks",
            name = "Soya Chunks Curry (Cooked)",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 135f,
            proteinPerBase = 17.5f,
            carbsPerBase = 11.0f,
            fatPerBase = 0.5f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "curry_paneer",
            name = "Paneer Curry / Podi Paneer",
            category = FoodCategory.DALS_CURRIES,
            unit = "g",
            baseServingSize = 50f,
            caloriesPerBase = 133f,
            proteinPerBase = 9.0f,
            carbsPerBase = 3.0f,
            fatPerBase = 9.8f,
            stepSize = 25f,
            isVeg = true
        ),

        // Meat, Eggs & Fish
        FoodItem(
            id = "meat_chicken_curry",
            name = "Andhra Chicken Curry",
            category = FoodCategory.MEAT_EGGS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 160f,
            proteinPerBase = 20.5f,
            carbsPerBase = 2.0f,
            fatPerBase = 7.5f,
            stepSize = 25f,
            isVeg = false
        ),
        FoodItem(
            id = "meat_chicken_breast",
            name = "Chicken Breast (Cooked)",
            category = FoodCategory.MEAT_EGGS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 165f,
            proteinPerBase = 31.0f,
            carbsPerBase = 0.0f,
            fatPerBase = 3.6f,
            stepSize = 25f,
            isVeg = false
        ),
        FoodItem(
            id = "egg_boiled_whole",
            name = "Whole Boiled Egg",
            category = FoodCategory.MEAT_EGGS,
            unit = "egg",
            baseServingSize = 1f,
            caloriesPerBase = 75f,
            proteinPerBase = 6.3f,
            carbsPerBase = 0.6f,
            fatPerBase = 5.0f,
            stepSize = 1f,
            isVeg = false
        ),
        FoodItem(
            id = "egg_white",
            name = "Boiled Egg White",
            category = FoodCategory.MEAT_EGGS,
            unit = "white",
            baseServingSize = 1f,
            caloriesPerBase = 17f,
            proteinPerBase = 3.6f,
            carbsPerBase = 0.2f,
            fatPerBase = 0.1f,
            stepSize = 1f,
            isVeg = false
        ),
        FoodItem(
            id = "egg_curry_serving",
            name = "Homestyle Egg Curry",
            category = FoodCategory.MEAT_EGGS,
            unit = "serving (1 egg)",
            baseServingSize = 1f,
            caloriesPerBase = 95f,
            proteinPerBase = 6.5f,
            carbsPerBase = 2.0f,
            fatPerBase = 6.5f,
            stepSize = 1f,
            isVeg = false
        ),
        FoodItem(
            id = "egg_podimas",
            name = "Egg Podimas (Scramble)",
            category = FoodCategory.MEAT_EGGS,
            unit = "serving (1 egg)",
            baseServingSize = 1f,
            caloriesPerBase = 90f,
            proteinPerBase = 6.5f,
            carbsPerBase = 1.0f,
            fatPerBase = 6.5f,
            stepSize = 1f,
            isVeg = false
        ),
        FoodItem(
            id = "meat_fish_curry",
            name = "South Indian Fish Curry",
            category = FoodCategory.MEAT_EGGS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 125f,
            proteinPerBase = 18.5f,
            carbsPerBase = 2.0f,
            fatPerBase = 4.5f,
            stepSize = 25f,
            isVeg = false
        ),

        // Dairy & Shakes
        FoodItem(
            id = "dairy_whole_milk",
            name = "Whole Buffalo/Cow Milk",
            category = FoodCategory.DAIRY,
            unit = "ml",
            baseServingSize = 100f,
            caloriesPerBase = 65f,
            proteinPerBase = 3.2f,
            carbsPerBase = 5.0f,
            fatPerBase = 3.5f,
            stepSize = 50f,
            isVeg = true
        ),
        FoodItem(
            id = "dairy_toned_milk",
            name = "Toned Milk (3% Fat)",
            category = FoodCategory.DAIRY,
            unit = "ml",
            baseServingSize = 100f,
            caloriesPerBase = 45f,
            proteinPerBase = 3.1f,
            carbsPerBase = 4.8f,
            fatPerBase = 1.5f,
            stepSize = 50f,
            isVeg = true
        ),
        FoodItem(
            id = "dairy_curd",
            name = "Plain Curd / Perugu (Dahi)",
            category = FoodCategory.DAIRY,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 60f,
            proteinPerBase = 3.5f,
            carbsPerBase = 4.5f,
            fatPerBase = 3.0f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "dairy_filter_coffee",
            name = "Filter Coffee (with milk)",
            category = FoodCategory.DAIRY,
            unit = "cup",
            baseServingSize = 1f,
            caloriesPerBase = 65f,
            proteinPerBase = 2.5f,
            carbsPerBase = 7.0f,
            fatPerBase = 2.5f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "dairy_black_coffee",
            name = "Black Coffee / Green Tea",
            category = FoodCategory.DAIRY,
            unit = "cup",
            baseServingSize = 1f,
            caloriesPerBase = 2f,
            proteinPerBase = 0.0f,
            carbsPerBase = 0.0f,
            fatPerBase = 0.0f,
            stepSize = 1f,
            isVeg = true
        ),

        // Protein Powders
        FoodItem(
            id = "protein_whey_isolate",
            name = "Whey Protein Isolate",
            category = FoodCategory.PROTEIN_POWDER,
            unit = "scoop (30g)",
            baseServingSize = 1f,
            caloriesPerBase = 115f,
            proteinPerBase = 27.0f,
            carbsPerBase = 1.0f,
            fatPerBase = 0.5f,
            stepSize = 0.5f,
            isVeg = true
        ),
        FoodItem(
            id = "protein_whey_concentrate",
            name = "Whey Protein Concentrate",
            category = FoodCategory.PROTEIN_POWDER,
            unit = "scoop (33g)",
            baseServingSize = 1f,
            caloriesPerBase = 130f,
            proteinPerBase = 24.0f,
            carbsPerBase = 3.0f,
            fatPerBase = 2.0f,
            stepSize = 0.5f,
            isVeg = true
        ),
        FoodItem(
            id = "protein_plant",
            name = "Plant / Pea Protein Powder",
            category = FoodCategory.PROTEIN_POWDER,
            unit = "scoop (33g)",
            baseServingSize = 1f,
            caloriesPerBase = 120f,
            proteinPerBase = 24.0f,
            carbsPerBase = 3.0f,
            fatPerBase = 1.5f,
            stepSize = 0.5f,
            isVeg = true
        ),

        // Fats, Nuts & Extras
        FoodItem(
            id = "extra_peanut_butter",
            name = "Peanut Butter",
            category = FoodCategory.FATS_EXTRAS,
            unit = "tbsp (16g)",
            baseServingSize = 1f,
            caloriesPerBase = 95f,
            proteinPerBase = 4.0f,
            carbsPerBase = 3.0f,
            fatPerBase = 8.0f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "extra_desi_ghee",
            name = "Desi Cow Ghee",
            category = FoodCategory.FATS_EXTRAS,
            unit = "tsp (5g)",
            baseServingSize = 1f,
            caloriesPerBase = 45f,
            proteinPerBase = 0.0f,
            carbsPerBase = 0.0f,
            fatPerBase = 5.0f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "extra_banana",
            name = "Fresh Banana",
            category = FoodCategory.FATS_EXTRAS,
            unit = "banana",
            baseServingSize = 1f,
            caloriesPerBase = 105f,
            proteinPerBase = 1.3f,
            carbsPerBase = 27.0f,
            fatPerBase = 0.3f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "extra_roasted_peanuts",
            name = "Roasted Peanuts / Chana",
            category = FoodCategory.FATS_EXTRAS,
            unit = "g",
            baseServingSize = 30f,
            caloriesPerBase = 160f,
            proteinPerBase = 7.5f,
            carbsPerBase = 12.0f,
            fatPerBase = 9.5f,
            stepSize = 10f,
            isVeg = true
        ),
        FoodItem(
            id = "extra_kala_chana_sundal",
            name = "Tempered Kala Chana Sundal",
            category = FoodCategory.FATS_EXTRAS,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 164f,
            proteinPerBase = 8.9f,
            carbsPerBase = 27.0f,
            fatPerBase = 2.5f,
            stepSize = 25f,
            isVeg = true
        ),

        // Sides & Vegetables
        FoodItem(
            id = "veg_poriyal",
            name = "Vegetable Poriyal (Beans/Carrot)",
            category = FoodCategory.VEGGIES,
            unit = "g",
            baseServingSize = 100f,
            caloriesPerBase = 70f,
            proteinPerBase = 2.0f,
            carbsPerBase = 8.0f,
            fatPerBase = 3.0f,
            stepSize = 25f,
            isVeg = true
        ),
        FoodItem(
            id = "veg_coconut_chutney",
            name = "Coconut Chutney",
            category = FoodCategory.VEGGIES,
            unit = "tbsp (15g)",
            baseServingSize = 1f,
            caloriesPerBase = 30f,
            proteinPerBase = 0.5f,
            carbsPerBase = 1.5f,
            fatPerBase = 2.5f,
            stepSize = 1f,
            isVeg = true
        ),
        FoodItem(
            id = "veg_tomato_chutney",
            name = "Tomato / Onion Chutney",
            category = FoodCategory.VEGGIES,
            unit = "tbsp (15g)",
            baseServingSize = 1f,
            caloriesPerBase = 18f,
            proteinPerBase = 0.5f,
            carbsPerBase = 2.5f,
            fatPerBase = 0.8f,
            stepSize = 1f,
            isVeg = true
        )
    )

    fun findById(id: String): FoodItem? = items.find { it.id == id }

    fun getDefaultIngredientsForMeal(mealId: String, isVeg: Boolean): List<MealIngredient> {
        return when (mealId) {
            "meal_breakfast" -> {
                if (isVeg) {
                    listOf(
                        findById("grain_idli")?.calculateMacros(4f)
                            ?: MealIngredient("grain_idli", "Steamed Idli", 4f, "idli", 240, 8f, 48f, 1.2f),
                        findById("dal_sambar")?.calculateMacros(150f)
                            ?: MealIngredient("dal_sambar", "Thick South Indian Sambar", 150f, "g", 98, 4.5f, 15f, 2.3f),
                        findById("curry_paneer")?.calculateMacros(75f)
                            ?: MealIngredient("curry_paneer", "Podi Paneer", 75f, "g", 199, 13.5f, 4.5f, 14.6f),
                        findById("veg_coconut_chutney")?.calculateMacros(1f)
                            ?: MealIngredient("veg_coconut_chutney", "Coconut Chutney", 1f, "tbsp", 30, 0.5f, 1.5f, 2.5f)
                    )
                } else {
                    listOf(
                        findById("grain_idli")?.calculateMacros(4f)
                            ?: MealIngredient("grain_idli", "Steamed Idli", 4f, "idli", 240, 8f, 48f, 1.2f),
                        findById("dal_sambar")?.calculateMacros(150f)
                            ?: MealIngredient("dal_sambar", "Thick South Indian Sambar", 150f, "g", 98, 4.5f, 15f, 2.3f),
                        findById("egg_boiled_whole")?.calculateMacros(2f)
                            ?: MealIngredient("egg_boiled_whole", "Whole Boiled Egg", 2f, "egg", 150, 12.6f, 1.2f, 10f),
                        findById("veg_coconut_chutney")?.calculateMacros(1f)
                            ?: MealIngredient("veg_coconut_chutney", "Coconut Chutney", 1f, "tbsp", 30, 0.5f, 1.5f, 2.5f)
                    )
                }
            }
            "meal_shake" -> {
                listOf(
                    findById("dairy_whole_milk")?.calculateMacros(250f)
                        ?: MealIngredient("dairy_whole_milk", "Whole Buffalo/Cow Milk", 250f, "ml", 163, 8f, 12.5f, 8.8f),
                    findById("extra_banana")?.calculateMacros(1f)
                        ?: MealIngredient("extra_banana", "Fresh Banana", 1f, "banana", 105, 1.3f, 27f, 0.3f),
                    findById("extra_peanut_butter")?.calculateMacros(1f)
                        ?: MealIngredient("extra_peanut_butter", "Peanut Butter", 1f, "tbsp", 95, 4f, 3f, 8f),
                    findById("grain_oats")?.calculateMacros(30f)
                        ?: MealIngredient("grain_oats", "Rolled / Powdered Oats", 30f, "g", 117, 4f, 20f, 2.1f),
                    findById("protein_whey_isolate")?.calculateMacros(1f)
                        ?: MealIngredient("protein_whey_isolate", "Whey Protein Isolate", 1f, "scoop", 115, 27f, 1f, 0.5f)
                )
            }
            "meal_lunch" -> {
                if (isVeg) {
                    listOf(
                        findById("grain_steamed_rice")?.calculateMacros(250f)
                            ?: MealIngredient("grain_steamed_rice", "Steamed Rice", 250f, "g", 325, 6.8f, 70.5f, 0.8f),
                        findById("curry_soya_chunks")?.calculateMacros(150f)
                            ?: MealIngredient("curry_soya_chunks", "Soya Chunks Curry", 150f, "g", 202, 26.3f, 16.5f, 0.8f),
                        findById("veg_poriyal")?.calculateMacros(100f)
                            ?: MealIngredient("veg_poriyal", "Vegetable Poriyal", 100f, "g", 70, 2f, 8f, 3f),
                        findById("extra_desi_ghee")?.calculateMacros(1f)
                            ?: MealIngredient("extra_desi_ghee", "Desi Cow Ghee", 1f, "tsp", 45, 0f, 0f, 5f)
                    )
                } else {
                    listOf(
                        findById("grain_steamed_rice")?.calculateMacros(250f)
                            ?: MealIngredient("grain_steamed_rice", "Steamed Rice", 250f, "g", 325, 6.8f, 70.5f, 0.8f),
                        findById("meat_chicken_curry")?.calculateMacros(150f)
                            ?: MealIngredient("meat_chicken_curry", "Andhra Chicken Curry", 150f, "g", 240, 30.8f, 3f, 11.3f),
                        findById("veg_poriyal")?.calculateMacros(100f)
                            ?: MealIngredient("veg_poriyal", "Vegetable Poriyal", 100f, "g", 70, 2f, 8f, 3f)
                    )
                }
            }
            "meal_snack" -> {
                listOf(
                    findById("extra_kala_chana_sundal")?.calculateMacros(150f)
                        ?: MealIngredient("extra_kala_chana_sundal", "Tempered Kala Chana Sundal", 150f, "g", 246, 13.4f, 40.5f, 3.8f),
                    findById("dairy_filter_coffee")?.calculateMacros(1f)
                        ?: MealIngredient("dairy_filter_coffee", "Filter Coffee (with milk)", 1f, "cup", 65, 2.5f, 7f, 2.5f)
                )
            }
            "meal_dinner" -> {
                if (isVeg) {
                    listOf(
                        findById("grain_steamed_rice")?.calculateMacros(200f)
                            ?: MealIngredient("grain_steamed_rice", "Steamed Rice", 200f, "g", 260, 5.4f, 56.4f, 0.6f),
                        findById("dal_toor_pappu")?.calculateMacros(200f)
                            ?: MealIngredient("dal_toor_pappu", "Thick Toor Dal Pappu", 200f, "g", 180, 11f, 28f, 3f),
                        findById("dairy_curd")?.calculateMacros(150f)
                            ?: MealIngredient("dairy_curd", "Plain Curd / Perugu", 150f, "g", 90, 5.2f, 6.8f, 4.5f),
                        findById("extra_desi_ghee")?.calculateMacros(1f)
                            ?: MealIngredient("extra_desi_ghee", "Desi Cow Ghee", 1f, "tsp", 45, 0f, 0f, 5f)
                    )
                } else {
                    listOf(
                        findById("grain_steamed_rice")?.calculateMacros(200f)
                            ?: MealIngredient("grain_steamed_rice", "Steamed Rice", 200f, "g", 260, 5.4f, 56.4f, 0.6f),
                        findById("egg_curry_serving")?.calculateMacros(2f)
                            ?: MealIngredient("egg_curry_serving", "Homestyle Egg Curry", 2f, "serving", 190, 13f, 4f, 13f),
                        findById("dairy_curd")?.calculateMacros(150f)
                            ?: MealIngredient("dairy_curd", "Plain Curd / Perugu", 150f, "g", 90, 5.2f, 6.8f, 4.5f)
                    )
                }
            }
            else -> emptyList()
        }
    }

    fun buildSummaryTitle(ingredients: List<MealIngredient>): String {
        if (ingredients.isEmpty()) return "Custom Meal"
        return ingredients.joinToString(" + ") { ing ->
            val qtyStr = if (ing.quantity % 1f == 0f) ing.quantity.toInt().toString() else "%.1f".format(ing.quantity)
            "$qtyStr ${ing.unit} ${ing.name}"
        }
    }
}
