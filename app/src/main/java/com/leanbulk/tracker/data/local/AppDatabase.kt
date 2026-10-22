package com.leanbulk.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.leanbulk.tracker.data.local.dao.MealDao
import com.leanbulk.tracker.data.local.dao.ReminderDao
import com.leanbulk.tracker.data.local.dao.UserProfileDao
import com.leanbulk.tracker.data.local.dao.WeightDao
import com.leanbulk.tracker.data.local.dao.WorkoutDao
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.local.entity.ReminderEntity
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity

@Database(
    entities = [
        DailyMealLogEntity::class,
        DailyWorkoutLogEntity::class,
        WeightProgressEntity::class,
        ReminderEntity::class,
        UserProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lean_bulk_tracker.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
