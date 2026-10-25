package com.leanbulk.tracker.data.model

object SouthIndianFoodCatalog {

    val items: List<FoodItem> = listOf(
        // ==========================================
        // Category 1: TIFFINS_BREADS (15 items)
        // ==========================================
        FoodItem(
            id = "idli_plain",
            name = "Steamed Idli",
            teluguOrRegionalHint = "ఇడ్లీ",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium idli",
            calories = 58,
            proteinGrams = 2.0f,
            carbsGrams = 12.0f,
            fatGrams = 0.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "ragi_idli",
            name = "Ragi Idli",
            teluguOrRegionalHint = "రాగి ఇడ్లీ",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium idli",
            calories = 62,
            proteinGrams = 2.3f,
            carbsGrams = 12.5f,
            fatGrams = 0.4f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "dosa_plain",
            name = "Plain / Kal Dosa",
            teluguOrRegionalHint = "దోశ",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium dosa",
            calories = 120,
            proteinGrams = 3.2f,
            carbsGrams = 20.0f,
            fatGrams = 3.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "dosa_egg",
            name = "Double Egg Dosa",
            teluguOrRegionalHint = "ఎగ్ దోశ",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 dosa with 2 eggs",
            calories = 265,
            proteinGrams = 15.2f,
            carbsGrams = 21.0f,
            fatGrams = 13.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "pesarattu",
            name = "Pesarattu (Green Moong Dosa)",
            teluguOrRegionalHint = "పెసరట్టు",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium pesarattu",
            calories = 135,
            proteinGrams = 6.8f,
            carbsGrams = 19.0f,
            fatGrams = 3.5f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "pesarattu_upma",
            name = "MLA Pesarattu (with Upma)",
            teluguOrRegionalHint = "ఎమ్మెల్యే పెసరట్టు",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 stuffed pesarattu",
            calories = 230,
            proteinGrams = 8.5f,
            carbsGrams = 34.0f,
            fatGrams = 6.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "uttapam_onion",
            name = "Onion Uttapam",
            teluguOrRegionalHint = "ఉల్లి ఉత్తప్పం",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 thick uttapam",
            calories = 165,
            proteinGrams = 4.5f,
            carbsGrams = 26.0f,
            fatGrams = 4.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "upma_rava",
            name = "Rava Upma",
            teluguOrRegionalHint = "ఉప్మా",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 katori (150g)",
            calories = 195,
            proteinGrams = 4.5f,
            carbsGrams = 32.0f,
            fatGrams = 5.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "pongal_ven",
            name = "Ven / Khara Pongal",
            teluguOrRegionalHint = "పొంగల్",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 katori (150g)",
            calories = 225,
            proteinGrams = 6.2f,
            carbsGrams = 33.0f,
            fatGrams = 7.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "uggani",
            name = "Rayalaseema Uggani (Puffed Rice)",
            teluguOrRegionalHint = "ఉగ్గాని",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 bowl (150g)",
            calories = 180,
            proteinGrams = 4.8f,
            carbsGrams = 31.0f,
            fatGrams = 4.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "bajji_mirchi",
            name = "Mirchi / Maida Bajji",
            teluguOrRegionalHint = "మిర్చి బజ్జి",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 piece",
            calories = 95,
            proteinGrams = 2.0f,
            carbsGrams = 10.0f,
            fatGrams = 5.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "puri_wheat",
            name = "Whole Wheat Puri",
            teluguOrRegionalHint = "పూరీ",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium puri",
            calories = 105,
            proteinGrams = 2.2f,
            carbsGrams = 13.0f,
            fatGrams = 5.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "chapati_pulka",
            name = "Whole Wheat Chapati / Pulka",
            teluguOrRegionalHint = "చపాతీ / పుల్కా",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 medium chapati",
            calories = 100,
            proteinGrams = 3.5f,
            carbsGrams = 18.0f,
            fatGrams = 1.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "medu_vada",
            name = "Medu Vada / Garelu",
            teluguOrRegionalHint = "గారెలు",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 piece",
            calories = 135,
            proteinGrams = 4.5f,
            carbsGrams = 14.0f,
            fatGrams = 7.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "ragi_malt",
            name = "Ragi Malt / Ambali (with Milk/Buttermilk)",
            teluguOrRegionalHint = "రాగి అంబలి",
            category = FoodCategory.TIFFINS_BREADS,
            servingUnit = "1 glass (250ml)",
            calories = 145,
            proteinGrams = 4.5f,
            carbsGrams = 26.0f,
            fatGrams = 2.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),

        // ==========================================
        // Category 2: RICE_GRAINS (9 items)
        // ==========================================
        FoodItem(
            id = "sona_masoori_rice",
            name = "Steamed Sona Masoori Rice",
            teluguOrRegionalHint = "అన్నం",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 cup / katori (150g)",
            calories = 175,
            proteinGrams = 3.6f,
            carbsGrams = 38.0f,
            fatGrams = 0.4f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "brown_rice",
            name = "Steamed Brown / Hand-Pounded Rice",
            teluguOrRegionalHint = "బ్రౌన్ రైస్",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 cup (150g)",
            calories = 165,
            proteinGrams = 4.2f,
            carbsGrams = 34.0f,
            fatGrams = 1.4f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "ragi_sangati",
            name = "Rayalaseema Ragi Sangati",
            teluguOrRegionalHint = "రాగి సంగటి",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 medium ball (200g)",
            calories = 220,
            proteinGrams = 5.2f,
            carbsGrams = 46.0f,
            fatGrams = 1.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "curd_rice",
            name = "Tempered Curd Rice (Daddojanam)",
            teluguOrRegionalHint = "దద్దోజనం",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 bowl (200g)",
            calories = 235,
            proteinGrams = 7.2f,
            carbsGrams = 34.0f,
            fatGrams = 7.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "pulihora",
            name = "Tamarind / Lemon Pulihora",
            teluguOrRegionalHint = "పులిహోర",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 bowl (180g)",
            calories = 265,
            proteinGrams = 5.5f,
            carbsGrams = 40.0f,
            fatGrams = 9.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "chicken_biryani",
            name = "Andhra / Hyderabadi Chicken Biryani",
            teluguOrRegionalHint = "చికెన్ బిర్యానీ",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 plate (300g)",
            calories = 540,
            proteinGrams = 28.0f,
            carbsGrams = 58.0f,
            fatGrams = 21.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "bisi_bele_bath",
            name = "Bisi Bele Bath",
            teluguOrRegionalHint = "బిసిబేలె బాత్",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 bowl (200g)",
            calories = 280,
            proteinGrams = 7.5f,
            carbsGrams = 48.0f,
            fatGrams = 6.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "jeera_rice",
            name = "Ghee Jeera Rice",
            teluguOrRegionalHint = "జీరా రైస్",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 bowl (180g)",
            calories = 230,
            proteinGrams = 4.0f,
            carbsGrams = 38.0f,
            fatGrams = 7.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "millet_rice",
            name = "Foxtail Millet (Korra) Rice",
            teluguOrRegionalHint = "కొర్రల అన్నం",
            category = FoodCategory.RICE_GRAINS,
            servingUnit = "1 cup (150g)",
            calories = 170,
            proteinGrams = 4.8f,
            carbsGrams = 35.0f,
            fatGrams = 1.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),

        // ==========================================
        // Category 3: DALS_SAMBAR_RASAM (9 items)
        // ==========================================
        FoodItem(
            id = "mudda_pappu",
            name = "Thick Toor Dal (Mudda Pappu)",
            teluguOrRegionalHint = "ముద్ద పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 thick katori (150g)",
            calories = 185,
            proteinGrams = 10.5f,
            carbsGrams = 28.0f,
            fatGrams = 3.2f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "tomato_pappu",
            name = "Andhra Tomato Pappu",
            teluguOrRegionalHint = "టమాటా పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150g)",
            calories = 165,
            proteinGrams = 8.5f,
            carbsGrams = 25.0f,
            fatGrams = 3.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "palakura_pappu",
            name = "Palakura (Spinach) Pappu",
            teluguOrRegionalHint = "పాలకూర పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150g)",
            calories = 170,
            proteinGrams = 9.2f,
            carbsGrams = 24.0f,
            fatGrams = 4.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "dosakaya_pappu",
            name = "Dosakaya (Yellow Cucumber) Pappu",
            teluguOrRegionalHint = "దోసకాయ పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150g)",
            calories = 160,
            proteinGrams = 8.0f,
            carbsGrams = 24.0f,
            fatGrams = 3.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "sambar_veg",
            name = "Thick Vegetable Sambar",
            teluguOrRegionalHint = "సాంబార్",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 bowl (150ml)",
            calories = 115,
            proteinGrams = 5.2f,
            carbsGrams = 16.0f,
            fatGrams = 3.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "rasam_pepper",
            name = "Tomato / Pepper Rasam",
            teluguOrRegionalHint = "రసం",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 cup (150ml)",
            calories = 45,
            proteinGrams = 1.5f,
            carbsGrams = 7.0f,
            fatGrams = 1.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "majjiga_pulusu",
            name = "Majjiga Pulusu (Buttermilk Stew)",
            teluguOrRegionalHint = "మజ్జిగ పులుసు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150ml)",
            calories = 85,
            proteinGrams = 3.8f,
            carbsGrams = 6.5f,
            fatGrams = 4.8f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "pesara_pappu",
            name = "Moong Dal Tadka (Pesara Pappu)",
            teluguOrRegionalHint = "పెసర పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150g)",
            calories = 150,
            proteinGrams = 9.0f,
            carbsGrams = 22.0f,
            fatGrams = 3.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "sorakaya_pappu",
            name = "Sorakaya (Bottle Gourd) Pappu",
            teluguOrRegionalHint = "సొరకాయ పప్పు",
            category = FoodCategory.DALS_SAMBAR_RASAM,
            servingUnit = "1 katori (150g)",
            calories = 145,
            proteinGrams = 7.8f,
            carbsGrams = 22.0f,
            fatGrams = 3.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),

        // ==========================================
        // Category 4: NON_VEG_EGGS (13 items)
        // ==========================================
        FoodItem(
            id = "boiled_egg_whole",
            name = "Whole Boiled Egg",
            teluguOrRegionalHint = "ఉడకబెట్టిన గుడ్డు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 large egg",
            calories = 72,
            proteinGrams = 6.3f,
            carbsGrams = 0.6f,
            fatGrams = 4.8f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "boiled_egg_white",
            name = "Boiled Egg White Only",
            teluguOrRegionalHint = "గుడ్డు తెల్లసొన",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 egg white",
            calories = 17,
            proteinGrams = 3.6f,
            carbsGrams = 0.2f,
            fatGrams = 0.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "egg_podimas",
            name = "2-Egg Podimas / Bhurji",
            teluguOrRegionalHint = "ఎగ్ బుర్జీ",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "2 eggs scrambled",
            calories = 185,
            proteinGrams = 13.0f,
            carbsGrams = 3.5f,
            fatGrams = 13.2f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "egg_omelette",
            name = "2-Egg Onion Chili Omelette",
            teluguOrRegionalHint = "ఆమ్లెట్",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 double omelette",
            calories = 190,
            proteinGrams = 13.0f,
            carbsGrams = 2.5f,
            fatGrams = 14.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "egg_pulusu",
            name = "Andhra Egg Pulusu / Curry",
            teluguOrRegionalHint = "కోడిగుడ్డు పులుసు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "2 eggs + gravy",
            calories = 245,
            proteinGrams = 14.0f,
            carbsGrams = 9.5f,
            fatGrams = 16.5f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "kodi_kura",
            name = "Andhra Chicken Curry (Kodi Kura)",
            teluguOrRegionalHint = "కోడి కూర",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 katori (150g)",
            calories = 265,
            proteinGrams = 31.0f,
            carbsGrams = 6.5f,
            fatGrams = 12.5f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "kodi_vepudu",
            name = "Chicken Fry (Kodi Vepudu)",
            teluguOrRegionalHint = "కోడి వేపుడు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 katori (150g)",
            calories = 290,
            proteinGrams = 33.0f,
            carbsGrams = 5.0f,
            fatGrams = 15.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "chepala_pulusu",
            name = "Andhra Fish Curry (Chepala Pulusu)",
            teluguOrRegionalHint = "చేపల పులుసు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "2 pieces + gravy (150g)",
            calories = 235,
            proteinGrams = 26.0f,
            carbsGrams = 6.0f,
            fatGrams = 11.5f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "fish_fry",
            name = "Tawa Fish Fry",
            teluguOrRegionalHint = "చేపల వేపుడు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "2 medium pieces (120g)",
            calories = 220,
            proteinGrams = 25.0f,
            carbsGrams = 4.0f,
            fatGrams = 11.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "mutton_curry",
            name = "Mutton Curry (veta mamsam)",
            teluguOrRegionalHint = "వేట మాంసం కూర",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 katori (150g)",
            calories = 310,
            proteinGrams = 27.0f,
            carbsGrams = 6.0f,
            fatGrams = 20.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "prawn_iguru",
            name = "Prawn Curry (Royyala Iguru)",
            teluguOrRegionalHint = "రొయ్యల ఇగురు",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 katori (120g)",
            calories = 195,
            proteinGrams = 24.0f,
            carbsGrams = 5.0f,
            fatGrams = 8.5f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "mutton_sukka",
            name = "Mutton Sukka / Vepudu",
            teluguOrRegionalHint = "మటన్ సుక్కా",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "1 katori (120g)",
            calories = 340,
            proteinGrams = 28.0f,
            carbsGrams = 4.0f,
            fatGrams = 23.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "chicken_breast_grilled",
            name = "Tawa Pepper Chicken Breast",
            teluguOrRegionalHint = "పెప్పర్ చికెన్",
            category = FoodCategory.NON_VEG_EGGS,
            servingUnit = "150g cooked",
            calories = 240,
            proteinGrams = 42.0f,
            carbsGrams = 2.0f,
            fatGrams = 7.0f,
            isVegetarian = false,
            isHighProteinPatch = true
        ),

        // ==========================================
        // Category 5: VEG_PROTEIN_BOOSTERS (9 items)
        // ==========================================
        FoodItem(
            id = "soya_chunks_curry",
            name = "Soya Chunks (Meal Maker) Masala/Fry",
            teluguOrRegionalHint = "మీల్ మేకర్ కర్రీ",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "50g dry weight cooked",
            calories = 195,
            proteinGrams = 26.0f,
            carbsGrams = 16.0f,
            fatGrams = 3.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "paneer_podi_fry",
            name = "Podi Tossed Paneer / Paneer Bhurji",
            teluguOrRegionalHint = "పన్నీర్ భుర్జీ",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "100g paneer",
            calories = 295,
            proteinGrams = 18.5f,
            carbsGrams = 5.5f,
            fatGrams = 22.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "paneer_curry",
            name = "Homestyle Paneer Butter/Tomato Curry",
            teluguOrRegionalHint = "పన్నీర్ కూర",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 katori (150g)",
            calories = 280,
            proteinGrams = 15.5f,
            carbsGrams = 9.0f,
            fatGrams = 20.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "kala_chana_sundal",
            name = "Tempered Kala Chana Sundal",
            teluguOrRegionalHint = "శనగల సుండల్",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 cup (150g)",
            calories = 240,
            proteinGrams = 11.5f,
            carbsGrams = 36.0f,
            fatGrams = 5.5f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "palli_sundal",
            name = "Boiled Peanut Sundal",
            teluguOrRegionalHint = "వేరుశనగ గుళ్ళు",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 small bowl (100g)",
            calories = 285,
            proteinGrams = 13.5f,
            carbsGrams = 15.0f,
            fatGrams = 20.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "pesalu_sprouts",
            name = "Steamed Green Moong Sprouts Salad",
            teluguOrRegionalHint = "మొలకలు",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 cup (120g)",
            calories = 125,
            proteinGrams = 8.5f,
            carbsGrams = 21.0f,
            fatGrams = 0.8f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "putnalu_chana",
            name = "Roasted Bengal Gram (Putnalu / Fried Gram)",
            teluguOrRegionalHint = "పుట్నాల పప్పు",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 handful (35g)",
            calories = 128,
            proteinGrams = 7.5f,
            carbsGrams = 20.0f,
            fatGrams = 1.8f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "tofu_stir_fry",
            name = "Tawa Spiced Tofu",
            teluguOrRegionalHint = "టోఫు ఫ్రై",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "100g tofu",
            calories = 140,
            proteinGrams = 15.0f,
            carbsGrams = 4.0f,
            fatGrams = 7.5f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "rajma_curry",
            name = "South Indian Style Rajma Masala",
            teluguOrRegionalHint = "రాజ్మా మసాలా",
            category = FoodCategory.VEG_PROTEIN_BOOSTERS,
            servingUnit = "1 katori (150g)",
            calories = 210,
            proteinGrams = 12.0f,
            carbsGrams = 32.0f,
            fatGrams = 4.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),

        // ==========================================
        // Category 6: VEPUDU_PORIYAL (7 items)
        // ==========================================
        FoodItem(
            id = "bendakaya_vepudu",
            name = "Bhindi / Bendakaya Fry (with Peanuts)",
            teluguOrRegionalHint = "బెండకాయ వేపుడు",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 135,
            proteinGrams = 3.5f,
            carbsGrams = 11.0f,
            fatGrams = 8.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "gutti_vankaya",
            name = "Gutti Vankaya (Stuffed Brinjal Curry)",
            teluguOrRegionalHint = "గుత్తి వంకాయ",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (120g)",
            calories = 165,
            proteinGrams = 4.2f,
            carbsGrams = 13.0f,
            fatGrams = 11.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "cabbage_poriyal",
            name = "Cabbage / Beans Coconut Poriyal",
            teluguOrRegionalHint = "క్యాబేజీ పోరియల్",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 85,
            proteinGrams = 2.2f,
            carbsGrams = 9.0f,
            fatGrams = 4.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "aloo_vepudu",
            name = "Potato / Bangaladumpa Fry",
            teluguOrRegionalHint = "బంగాళదుంప వేపుడు",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 155,
            proteinGrams = 2.2f,
            carbsGrams = 22.0f,
            fatGrams = 6.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "aratikaya_fry",
            name = "Raw Banana (Aratikaya) Stir-Fry",
            teluguOrRegionalHint = "అరటికాయ వేపుడు",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 140,
            proteinGrams = 2.0f,
            carbsGrams = 23.0f,
            fatGrams = 4.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "dondakaya_vepudu",
            name = "Dondakaya (Tindora/Ivy Gourd) Fry",
            teluguOrRegionalHint = "దొండకాయ వేపుడు",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 120,
            proteinGrams = 2.5f,
            carbsGrams = 10.0f,
            fatGrams = 7.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "beetroot_poriyal",
            name = "Beetroot Poriyal with Coconut",
            teluguOrRegionalHint = "బీట్‌రూట్ పోరియల్",
            category = FoodCategory.VEPUDU_PORIYAL,
            servingUnit = "1 katori (100g)",
            calories = 90,
            proteinGrams = 2.0f,
            carbsGrams = 14.0f,
            fatGrams = 3.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),

        // ==========================================
        // Category 7: CHUTNEYS_PODIS_FATS (7 items)
        // ==========================================
        FoodItem(
            id = "palli_chutney",
            name = "Peanut (Palli) Chutney",
            teluguOrRegionalHint = "పల్లీ చట్నీ",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "2 tbsp (40g)",
            calories = 110,
            proteinGrams = 4.2f,
            carbsGrams = 5.0f,
            fatGrams = 8.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "coconut_chutney",
            name = "Fresh Coconut Chutney",
            teluguOrRegionalHint = "కొబ్బరి చట్నీ",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "2 tbsp (40g)",
            calories = 95,
            proteinGrams = 1.5f,
            carbsGrams = 4.5f,
            fatGrams = 8.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "allam_chutney",
            name = "Ginger Tamarind (Allam) Chutney",
            teluguOrRegionalHint = "అల్లం చట్నీ",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "1 tbsp (20g)",
            calories = 45,
            proteinGrams = 0.5f,
            carbsGrams = 8.5f,
            fatGrams = 1.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "kandi_podi",
            name = "Kandi Podi / Paruppu Podi / Karam Podi",
            teluguOrRegionalHint = "కంది పొడి / కారం పొడి",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "1 tbsp (15g)",
            calories = 55,
            proteinGrams = 3.2f,
            carbsGrams = 7.5f,
            fatGrams = 1.2f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "cow_ghee",
            name = "Pure Cow Ghee",
            teluguOrRegionalHint = "నెయ్యి",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "1 tsp (5ml)",
            calories = 45,
            proteinGrams = 0.0f,
            carbsGrams = 0.0f,
            fatGrams = 5.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "idli_karam_podi",
            name = "Idli Karam Podi (Gunpowder)",
            teluguOrRegionalHint = "ఇడ్లీ కారం పొడి",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "1 tbsp (15g)",
            calories = 60,
            proteinGrams = 2.8f,
            carbsGrams = 6.5f,
            fatGrams = 2.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "avakai_pickle",
            name = "Andhra Avakai (Mango Pickle)",
            teluguOrRegionalHint = "ఆవకాయ పచ్చడి",
            category = FoodCategory.CHUTNEYS_PODIS_FATS,
            servingUnit = "1 tbsp (20g)",
            calories = 65,
            proteinGrams = 0.8f,
            carbsGrams = 3.0f,
            fatGrams = 5.5f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),

        // ==========================================
        // Category 8: DAIRY_SHAKES_SUPPLEMENTS (8 items)
        // ==========================================
        FoodItem(
            id = "curd_perugu",
            name = "Thick Whole-Milk Curd (Perugu)",
            teluguOrRegionalHint = "పెరుగు",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 cup (150g)",
            calories = 105,
            proteinGrams = 6.0f,
            carbsGrams = 7.0f,
            fatGrams = 6.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "whole_milk",
            name = "Whole / Full-Cream Milk",
            teluguOrRegionalHint = "పాలు",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 glass (250ml)",
            calories = 160,
            proteinGrams = 8.0f,
            carbsGrams = 12.0f,
            fatGrams = 8.5f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "osoaa_whey_scoop",
            name = "Osoaa Impact Whey + 3g Creatine",
            teluguOrRegionalHint = "ప్రోటీన్ పౌడర్",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 scoop (35g)",
            calories = 130,
            proteinGrams = 24.0f,
            carbsGrams = 3.5f,
            fatGrams = 2.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "peanut_butter",
            name = "Natural Peanut Butter",
            teluguOrRegionalHint = "పీనట్ బటర్",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 tbsp (16g)",
            calories = 95,
            proteinGrams = 4.0f,
            carbsGrams = 3.0f,
            fatGrams = 8.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "rolled_oats",
            name = "Powdered Rolled Oats",
            teluguOrRegionalHint = "ఓట్స్",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "30g scoop",
            calories = 115,
            proteinGrams = 4.0f,
            carbsGrams = 20.0f,
            fatGrams = 2.0f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "banana_medium",
            name = "Medium Ripe Banana",
            teluguOrRegionalHint = "అరటిపండు",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 medium banana",
            calories = 105,
            proteinGrams = 1.3f,
            carbsGrams = 27.0f,
            fatGrams = 0.3f,
            isVegetarian = true,
            isHighProteinPatch = false
        ),
        FoodItem(
            id = "buttermilk_chaas",
            name = "Spiced Majjiga / Buttermilk",
            teluguOrRegionalHint = "మజ్జిగ",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "1 glass (250ml)",
            calories = 45,
            proteinGrams = 2.5f,
            carbsGrams = 3.5f,
            fatGrams = 2.0f,
            isVegetarian = true,
            isHighProteinPatch = true
        ),
        FoodItem(
            id = "almonds_badam",
            name = "Raw / Soaked Almonds",
            teluguOrRegionalHint = "బాదం పప్పులు",
            category = FoodCategory.DAIRY_SHAKES_SUPPLEMENTS,
            servingUnit = "10 pieces (15g)",
            calories = 85,
            proteinGrams = 3.2f,
            carbsGrams = 3.0f,
            fatGrams = 7.5f,
            isVegetarian = true,
            isHighProteinPatch = true
        )
    )

    private val itemsMap: Map<String, FoodItem> by lazy {
        items.associateBy { it.id }
    }

    fun findById(id: String): FoodItem? = itemsMap[id]

    fun getByCategory(category: FoodCategory): List<FoodItem> =
        items.filter { it.category == category }

    fun search(
        query: String,
        category: FoodCategory? = null,
        vegOnly: Boolean = false,
        customFoods: List<FoodItem> = emptyList()
    ): List<FoodItem> {
        val allAvailable = customFoods + items
        return allAvailable.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.teluguOrRegionalHint.contains(query, ignoreCase = true) ||
                item.category.displayName.contains(query, ignoreCase = true)

            val matchesCategory = category == null || item.category == category
            val matchesVeg = !vegOnly || item.isVegetarian

            matchesQuery && matchesCategory && matchesVeg
        }
    }

    fun getProteinPatches(vegOnly: Boolean = false): List<FoodItem> {
        return items.filter { it.isHighProteinPatch && (!vegOnly || it.isVegetarian) }
    }

    fun getCaloriePatches(): List<FoodItem> {
        return listOfNotNull(
            findById("cow_ghee"),
            findById("sona_masoori_rice"),
            findById("banana_medium"),
            findById("palli_chutney"),
            findById("peanut_butter"),
            findById("whole_milk")
        )
    }
}
