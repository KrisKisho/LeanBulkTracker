package com.leanbulk.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leanbulk.tracker.data.local.dao.AdaptationLogDao
import com.leanbulk.tracker.data.local.dao.HabitDao
import com.leanbulk.tracker.data.local.dao.MealDao
import com.leanbulk.tracker.data.local.dao.ReminderDao
import com.leanbulk.tracker.data.local.dao.UserProfileDao
import com.leanbulk.tracker.data.local.dao.WeightDao
import com.leanbulk.tracker.data.local.dao.WorkoutDao
import com.leanbulk.tracker.data.local.entity.AdaptationLogEntity
import com.leanbulk.tracker.data.local.entity.DailyHabitLogEntity
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.local.entity.MealTemplateEntity
import com.leanbulk.tracker.data.local.entity.ReminderEntity
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
        AdaptationLogEntity::class
    ],
    version = 7,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lean_bulk_tracker.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
