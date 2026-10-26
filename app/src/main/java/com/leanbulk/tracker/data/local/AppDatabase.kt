package com.leanbulk.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leanbulk.tracker.data.local.dao.AdaptationLogDao
import com.leanbulk.tracker.data.local.dao.CustomFoodDao
import com.leanbulk.tracker.data.local.dao.HabitDao
import com.leanbulk.tracker.data.local.dao.MealDao
import com.leanbulk.tracker.data.local.dao.ReminderDao
import com.leanbulk.tracker.data.local.dao.SavedMealComboDao
import com.leanbulk.tracker.data.local.dao.SupplementDao
import com.leanbulk.tracker.data.local.dao.UserProfileDao
import com.leanbulk.tracker.data.local.dao.WeightDao
import com.leanbulk.tracker.data.local.dao.WorkoutDao
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.local.entity.CustomFoodEntity
import com.leanbulk.tracker.data.local.entity.DailyHabitLogEntity
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.local.entity.MealTemplateEntity
import com.leanbulk.tracker.data.local.entity.ReminderEntity
import com.leanbulk.tracker.data.local.entity.SavedMealComboEntity
import com.leanbulk.tracker.data.local.entity.SupplementLogEntity
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity

@Database(
    entities = [
        DailyMealLogEntity::class,
        DailyWorkoutLogEntity::class,
        WeightProgressEntity::class,
        ReminderEntity::class,
        UserProfileEntity::class,
        DailyHabitLogEntity::class,
        MealTemplateEntity::class,
        AdaptationLogEntity::class,
        CustomFoodEntity::class,
        SavedMealComboEntity::class,
        SupplementLogEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun adaptationLogDao(): AdaptationLogDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun savedMealComboDao(): SavedMealComboDao
    abstract fun supplementDao(): SupplementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Workout overload columns
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN weightUsedKg REAL NOT NULL DEFAULT 4.0")
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN repsPerSet INTEGER NOT NULL DEFAULT 12")
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN effortRating TEXT NOT NULL DEFAULT 'Good Burn'")

                // UserProfile baseline columns
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startWaistInches REAL NOT NULL DEFAULT 30.0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startChestInches REAL NOT NULL DEFAULT 35.5")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startArmInches REAL NOT NULL DEFAULT 12.0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startForearmInches REAL NOT NULL DEFAULT 10.2")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startNeckInches REAL NOT NULL DEFAULT 13.8")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startThighInches REAL NOT NULL DEFAULT 19.5")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN startCalfInches REAL NOT NULL DEFAULT 13.0")

                // Daily Habit Logs table
                db.execSQL("CREATE TABLE IF NOT EXISTS `daily_habit_logs` (`date` TEXT NOT NULL, `waterGlasses` INTEGER NOT NULL, `sleepHours` REAL NOT NULL, PRIMARY KEY(`date`))")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add ingredientsJson to meal_logs
                db.execSQL("ALTER TABLE meal_logs ADD COLUMN ingredientsJson TEXT NOT NULL DEFAULT ''")

                // Create meal_templates table for persistent customizable default recipes
                db.execSQL("CREATE TABLE IF NOT EXISTS `meal_templates` (`mealId` TEXT NOT NULL, `mealName` TEXT NOT NULL, `title` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL, `isVeg` INTEGER NOT NULL, `ingredientsJson` TEXT NOT NULL, PRIMARY KEY(`mealId`))")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add set3RepsAchieved and progressionWeek to workout_logs
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN set3RepsAchieved INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN progressionWeek INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add adaptive coaching state to user_profile
                db.execSQL("ALTER TABLE user_profile ADD COLUMN autoAdaptEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN adaptiveCalorieOffset INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN splitShakeModeActive INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN dailyStepGoal INTEGER NOT NULL DEFAULT 6000")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutStartTimeHour INTEGER NOT NULL DEFAULT 19")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutStartTimeMinute INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN currentRollingSplitIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastAdaptationEvalDate TEXT NOT NULL DEFAULT ''")

                // Create adaptation_logs table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `adaptation_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `reason` TEXT NOT NULL,
                        `parameterChanged` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_foods` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `teluguOrRegionalHint` TEXT NOT NULL DEFAULT '',
                        `category` TEXT NOT NULL,
                        `servingUnit` TEXT NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `proteinGrams` REAL NOT NULL,
                        `carbsGrams` REAL NOT NULL,
                        `fatGrams` REAL NOT NULL,
                        `isVegetarian` INTEGER NOT NULL DEFAULT 1,
                        `isHighProteinPatch` INTEGER NOT NULL DEFAULT 0,
                        `createdAtMillis` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_meal_combos` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `targetMealSlotId` TEXT NOT NULL DEFAULT '',
                        `totalCalories` INTEGER NOT NULL,
                        `totalProtein` REAL NOT NULL,
                        `totalCarbs` REAL NOT NULL,
                        `totalFat` REAL NOT NULL,
                        `isVegetarian` INTEGER NOT NULL DEFAULT 1,
                        `itemsJson` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // UserProfile columns for Supplement & Equipment
                db.execSQL("ALTER TABLE user_profile ADD COLUMN wheyCreatineMode TEXT NOT NULL DEFAULT 'WHEY_WITH_CREATINE'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN creatineSaturationPercent REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN d3Supplement1Iu INTEGER NOT NULL DEFAULT 600")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN d3Supplement2Iu INTEGER NOT NULL DEFAULT 400")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHas4kgDumbbell INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasWeightedBackpack INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasSturdyTableOrBed INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasAdjustableDumbbell INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasBarbell INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasPullUpBar INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN eqHasResistanceBand INTEGER NOT NULL DEFAULT 0")

                // supplement_logs table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `supplement_logs` (
                        `date` TEXT PRIMARY KEY NOT NULL,
                        `wheyLogged` INTEGER NOT NULL DEFAULT 0,
                        `creatineGramsLogged` REAL NOT NULL DEFAULT 0.0,
                        `vitD3B12K2Logged` INTEGER NOT NULL DEFAULT 0,
                        `fishOilLogged` INTEGER NOT NULL DEFAULT 0,
                        `magnesiumGlycinateLogged` INTEGER NOT NULL DEFAULT 0,
                        `timestampMillis` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN bellyFatRecompMode INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN bellyWorkoutVariant TEXT NOT NULL DEFAULT 'AUTO'")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN goalMode TEXT NOT NULL DEFAULT 'AUTO'")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lean_bulk_tracker.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
