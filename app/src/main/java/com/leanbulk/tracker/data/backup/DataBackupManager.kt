package com.leanbulk.tracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.data.local.entity.DailyHabitLogEntity
import com.leanbulk.tracker.data.local.entity.DailyMealLogEntity
import com.leanbulk.tracker.data.local.entity.DailyWorkoutLogEntity
import com.leanbulk.tracker.data.local.entity.MealTemplateEntity
import com.leanbulk.tracker.data.local.entity.ReminderEntity
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.local.entity.WeightProgressEntity
import com.leanbulk.tracker.data.model.ReminderItem
import com.leanbulk.tracker.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class BackupSummary(
    val success: Boolean,
    val message: String,
    val userProfileImported: Boolean = false,
    val weightLogsCount: Int = 0,
    val mealLogsCount: Int = 0,
    val workoutLogsCount: Int = 0,
    val remindersCount: Int = 0,
    val habitsCount: Int = 0
)

class DataBackupManager(private val database: AppDatabase) {

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "LeanBulkTracker")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("exportedDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

        // 1. User Profile
        val profile = database.userProfileDao().getUserProfileSync()
        if (profile != null) {
            val pObj = JSONObject().apply {
                put("id", profile.id)
                put("age", profile.age)
                put("sex", profile.sex)
                put("heightCm", profile.heightCm.toDouble())
                put("startWeightKg", profile.startWeightKg.toDouble())
                put("currentWeightKg", profile.currentWeightKg.toDouble())
                put("goalWeightKg", profile.goalWeightKg.toDouble())
                put("targetWeeklyGainKg", profile.targetWeeklyGainKg.toDouble())
                put("wristInches", profile.wristInches.toDouble())
                put("ankleInches", profile.ankleInches.toDouble())
                put("currentWaistInches", profile.currentWaistInches.toDouble())
                put("currentChestInches", profile.currentChestInches.toDouble())
                put("currentArmInches", profile.currentArmInches.toDouble())
                put("currentForearmInches", profile.currentForearmInches.toDouble())
                put("currentNeckInches", profile.currentNeckInches.toDouble())
                put("currentThighInches", profile.currentThighInches.toDouble())
                put("currentCalfInches", profile.currentCalfInches.toDouble())
                put("startWaistInches", profile.startWaistInches.toDouble())
                put("startChestInches", profile.startChestInches.toDouble())
                put("startArmInches", profile.startArmInches.toDouble())
                put("startForearmInches", profile.startForearmInches.toDouble())
                put("startNeckInches", profile.startNeckInches.toDouble())
                put("startThighInches", profile.startThighInches.toDouble())
                put("startCalfInches", profile.startCalfInches.toDouble())
            }
            root.put("userProfile", pObj)
        }

        // 2. Weight History Logs
        val weightLogs = database.weightDao().getAllWeightLogsSync()
        val weightArr = JSONArray()
        weightLogs.forEach { w ->
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("date", w.date)
                put("weightKg", w.weightKg.toDouble())
                put("waistInches", w.waistInches.toDouble())
                put("notes", w.notes)
            }
            weightArr.put(wObj)
        }
        root.put("weightLogs", weightArr)

        // 3. Daily Meal Logs
        val mealLogs = database.mealDao().getAllMealLogsSync()
        val mealArr = JSONArray()
        mealLogs.forEach { m ->
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("date", m.date)
                put("mealId", m.mealId)
                put("mealName", m.mealName)
                put("title", m.title)
                put("calories", m.calories)
                put("protein", m.protein)
                put("carbs", m.carbs)
                put("fat", m.fat)
                put("isChecked", m.isChecked)
                put("isVeg", m.isVeg)
                put("isCustom", m.isCustom)
                put("ingredientsJson", m.ingredientsJson)
            }
            mealArr.put(mObj)
        }
        root.put("mealLogs", mealArr)

        // 3b. Custom Meal Templates
        val templates = database.mealDao().getAllMealTemplatesSync()
        val templateArr = JSONArray()
        templates.forEach { t ->
            val tObj = JSONObject().apply {
                put("mealId", t.mealId)
                put("mealName", t.mealName)
                put("title", t.title)
                put("calories", t.calories)
                put("protein", t.protein)
                put("carbs", t.carbs)
                put("fat", t.fat)
                put("isVeg", t.isVeg)
                put("ingredientsJson", t.ingredientsJson)
            }
            templateArr.put(tObj)
        }
        root.put("mealTemplates", templateArr)

        // 4. Daily Workout Logs
        val workoutLogs = database.workoutDao().getAllWorkoutLogsSync()
        val workoutArr = JSONArray()
        workoutLogs.forEach { wo ->
            val woObj = JSONObject().apply {
                put("id", wo.id)
                put("date", wo.date)
                put("segmentId", wo.segmentId)
                put("dayOfWeek", wo.dayOfWeek)
                put("isCompleted", wo.isCompleted)
                put("completedAtMillis", wo.completedAtMillis)
                put("weightUsedKg", wo.weightUsedKg.toDouble())
                put("repsPerSet", wo.repsPerSet)
                put("effortRating", wo.effortRating)
            }
            workoutArr.put(woObj)
        }
        root.put("workoutLogs", workoutArr)

        // 5. Reminders
        val reminders = database.reminderDao().getAllRemindersSync()
        val reminderArr = JSONArray()
        reminders.forEach { r ->
            val rObj = JSONObject().apply {
                put("id", r.id)
                put("hour", r.hour)
                put("minute", r.minute)
                put("title", r.title)
                put("description", r.description)
                put("isEnabled", r.isEnabled)
                put("isWeeklyOnly", r.isWeeklyOnly)
                put("weeklyDayOfWeek", r.weeklyDayOfWeek)
            }
            reminderArr.put(rObj)
        }
        root.put("reminders", reminderArr)

        // 6. Daily Habit Logs (Hydration & Sleep)
        val habitLogs = database.habitDao().getAllHabitLogsSync()
        val habitArr = JSONArray()
        habitLogs.forEach { h ->
            val hObj = JSONObject().apply {
                put("date", h.date)
                put("waterGlasses", h.waterGlasses)
                put("sleepHours", h.sleepHours.toDouble())
            }
            habitArr.put(hObj)
        }
        root.put("habitLogs", habitArr)

        root.toString(2)
    }

    suspend fun writeBackupToOutputStream(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = generateBackupJson()
            outputStream.bufferedWriter().use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
    }

    suspend fun createShareableBackupFile(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val json = generateBackupJson()
            val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
            val timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupFile = File(backupDir, "LeanBulk_Backup_$timeStamp.json")
            backupFile.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
        }
    }

    suspend fun importBackupFromInputStream(context: Context, inputStream: InputStream): BackupSummary = withContext(Dispatchers.IO) {
        try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)

            if (!root.has("app") || root.optString("app") != "LeanBulkTracker") {
                return@withContext BackupSummary(
                    success = false,
                    message = "Invalid backup file: Not a recognized Lean Bulk Tracker backup."
                )
            }

            // 1. Restore User Profile
            var profileImported = false
            if (root.has("userProfile")) {
                val pObj = root.getJSONObject("userProfile")
                val entity = UserProfileEntity(
                    id = 1,
                    age = pObj.optInt("age", 41),
                    sex = pObj.optString("sex", "Male"),
                    heightCm = pObj.optDouble("heightCm", 166.0).toFloat(),
                    startWeightKg = pObj.optDouble("startWeightKg", 60.0).toFloat(),
                    currentWeightKg = pObj.optDouble("currentWeightKg", 60.0).toFloat(),
                    goalWeightKg = pObj.optDouble("goalWeightKg", 70.0).toFloat(),
                    targetWeeklyGainKg = pObj.optDouble("targetWeeklyGainKg", 0.30).toFloat(),
                    wristInches = pObj.optDouble("wristInches", 6.1).toFloat(),
                    ankleInches = pObj.optDouble("ankleInches", 7.81).toFloat(),
                    currentWaistInches = pObj.optDouble("currentWaistInches", 30.0).toFloat(),
                    currentChestInches = pObj.optDouble("currentChestInches", 35.5).toFloat(),
                    currentArmInches = pObj.optDouble("currentArmInches", 12.0).toFloat(),
                    currentForearmInches = pObj.optDouble("currentForearmInches", 10.2).toFloat(),
                    currentNeckInches = pObj.optDouble("currentNeckInches", 13.8).toFloat(),
                    currentThighInches = pObj.optDouble("currentThighInches", 19.5).toFloat(),
                    currentCalfInches = pObj.optDouble("currentCalfInches", 13.0).toFloat(),
                    startWaistInches = pObj.optDouble("startWaistInches", pObj.optDouble("currentWaistInches", 30.0)).toFloat(),
                    startChestInches = pObj.optDouble("startChestInches", pObj.optDouble("currentChestInches", 35.5)).toFloat(),
                    startArmInches = pObj.optDouble("startArmInches", pObj.optDouble("currentArmInches", 12.0)).toFloat(),
                    startForearmInches = pObj.optDouble("startForearmInches", pObj.optDouble("currentForearmInches", 10.2)).toFloat(),
                    startNeckInches = pObj.optDouble("startNeckInches", pObj.optDouble("currentNeckInches", 13.8)).toFloat(),
                    startThighInches = pObj.optDouble("startThighInches", pObj.optDouble("currentThighInches", 19.5)).toFloat(),
                    startCalfInches = pObj.optDouble("startCalfInches", pObj.optDouble("currentCalfInches", 13.0)).toFloat()
                )
                database.userProfileDao().upsertUserProfile(entity)
                profileImported = true
            }

            // 2. Restore Weight Logs
            var weightCount = 0
            if (root.has("weightLogs")) {
                val arr = root.getJSONArray("weightLogs")
                val list = mutableListOf<WeightProgressEntity>()
                for (i in 0 until arr.length()) {
                    val w = arr.getJSONObject(i)
                    list.add(
                        WeightProgressEntity(
                            id = w.optString("id", java.util.UUID.randomUUID().toString()),
                            date = w.getString("date"),
                            weightKg = w.getDouble("weightKg").toFloat(),
                            waistInches = w.optDouble("waistInches", 30.0).toFloat(),
                            notes = w.optString("notes", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.weightDao().upsertAll(list)
                    weightCount = list.size
                }
            }

            // 3. Restore Meal Logs
            var mealCount = 0
            if (root.has("mealLogs")) {
                val arr = root.getJSONArray("mealLogs")
                val list = mutableListOf<DailyMealLogEntity>()
                for (i in 0 until arr.length()) {
                    val m = arr.getJSONObject(i)
                    list.add(
                        DailyMealLogEntity(
                            date = m.getString("date"),
                            mealId = m.getString("mealId"),
                            id = m.optString("id", "${m.getString("date")}_${m.getString("mealId")}"),
                            mealName = m.optString("mealName", m.optString("title", "Meal")),
                            title = m.getString("title"),
                            calories = m.getInt("calories"),
                            protein = m.getInt("protein"),
                            carbs = m.getInt("carbs"),
                            fat = m.getInt("fat"),
                            isVeg = m.optBoolean("isVeg", false),
                            isChecked = m.optBoolean("isChecked", false),
                            isCustom = m.optBoolean("isCustom", false),
                            ingredientsJson = m.optString("ingredientsJson", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.mealDao().upsertMeals(list)
                    mealCount = list.size
                }
            }

            // 3b. Restore Custom Meal Templates
            if (root.has("mealTemplates")) {
                val tArr = root.getJSONArray("mealTemplates")
                val tList = mutableListOf<MealTemplateEntity>()
                for (i in 0 until tArr.length()) {
                    val t = tArr.getJSONObject(i)
                    tList.add(
                        MealTemplateEntity(
                            mealId = t.getString("mealId"),
                            mealName = t.getString("mealName"),
                            title = t.getString("title"),
                            calories = t.getInt("calories"),
                            protein = t.getInt("protein"),
                            carbs = t.getInt("carbs"),
                            fat = t.getInt("fat"),
                            isVeg = t.optBoolean("isVeg", false),
                            ingredientsJson = t.optString("ingredientsJson", "")
                        )
                    )
                }
                if (tList.isNotEmpty()) {
                    database.mealDao().upsertMealTemplates(tList)
                }
            }

            // 4. Restore Workout Logs
            var workoutCount = 0
            if (root.has("workoutLogs")) {
                val arr = root.getJSONArray("workoutLogs")
                val list = mutableListOf<DailyWorkoutLogEntity>()
                for (i in 0 until arr.length()) {
                    val wo = arr.getJSONObject(i)
                    list.add(
                        DailyWorkoutLogEntity(
                            date = wo.getString("date"),
                            segmentId = wo.getString("segmentId"),
                            id = wo.optString("id", "${wo.getString("date")}_${wo.getString("segmentId")}"),
                            dayOfWeek = wo.optString("dayOfWeek", ""),
                            isCompleted = wo.optBoolean("isCompleted", false),
                            completedAtMillis = wo.optLong("completedAtMillis", System.currentTimeMillis()),
                            weightUsedKg = wo.optDouble("weightUsedKg", 4.0).toFloat(),
                            repsPerSet = wo.optInt("repsPerSet", 12),
                            effortRating = wo.optString("effortRating", "Good Burn")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.workoutDao().upsertAll(list)
                    workoutCount = list.size
                }
            }

            // 5. Restore Reminders
            var reminderCount = 0
            if (root.has("reminders")) {
                val arr = root.getJSONArray("reminders")
                val list = mutableListOf<ReminderEntity>()
                for (i in 0 until arr.length()) {
                    val r = arr.getJSONObject(i)
                    list.add(
                        ReminderEntity(
                            id = r.getInt("id"),
                            title = r.getString("title"),
                            description = r.optString("description", r.optString("message", "")),
                            hour = r.getInt("hour"),
                            minute = r.getInt("minute"),
                            isEnabled = r.optBoolean("isEnabled", true),
                            isWeeklyOnly = r.optBoolean("isWeeklyOnly", false),
                            weeklyDayOfWeek = r.optInt("weeklyDayOfWeek", 1)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.reminderDao().upsertAll(list)
                    reminderCount = list.size
                    // Reschedule active reminders
                    list.filter { it.isEnabled }.forEach { rem ->
                        try {
                            val reminderItem = ReminderItem(
                                id = rem.id,
                                title = rem.title,
                                description = rem.description,
                                hour = rem.hour,
                                minute = rem.minute,
                                isEnabled = rem.isEnabled,
                                isWeeklyOnly = rem.isWeeklyOnly,
                                weeklyDayOfWeek = rem.weeklyDayOfWeek
                            )
                            NotificationScheduler.scheduleReminder(context, reminderItem)
                        } catch (_: Exception) {}
                    }
                }
            }

            // 6. Restore Daily Habit Logs
            var habitCount = 0
            if (root.has("habitLogs")) {
                val arr = root.getJSONArray("habitLogs")
                for (i in 0 until arr.length()) {
                    val h = arr.getJSONObject(i)
                    database.habitDao().upsertHabitLog(
                        DailyHabitLogEntity(
                            date = h.getString("date"),
                            waterGlasses = h.optInt("waterGlasses", 0),
                            sleepHours = h.optDouble("sleepHours", 0.0).toFloat()
                        )
                    )
                }
                habitCount = arr.length()
            }

            BackupSummary(
                success = true,
                message = "Backup restored successfully!",
                userProfileImported = profileImported,
                weightLogsCount = weightCount,
                mealLogsCount = mealCount,
                workoutLogsCount = workoutCount,
                remindersCount = reminderCount,
                habitsCount = habitCount
            )
        } catch (e: Exception) {
            BackupSummary(
                success = false,
                message = "Error importing backup: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
