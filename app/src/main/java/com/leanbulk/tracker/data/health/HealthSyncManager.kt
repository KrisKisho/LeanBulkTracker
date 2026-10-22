package com.leanbulk.tracker.data.health

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

data class HealthSyncData(
    val dailySteps: Int = 0,
    val stepCaloriesBurned: Int = 0,
    val activeCaloriesBurned: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val isHealthConnectAvailable: Boolean = false,
    val isHealthConnectAuthorized: Boolean = false,
    val hasHardwarePermission: Boolean = false,
    val hasHardwareSensor: Boolean = false,
    val dataSource: String = "Hardware Sensor"
)

class HealthSyncManager(private val context: Context) : SensorEventListener {

    private val prefs: SharedPreferences = context.getSharedPreferences("health_sync_prefs", Context.MODE_PRIVATE)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _syncData = MutableStateFlow(loadInitialData())
    val syncData: StateFlow<HealthSyncData> = _syncData.asStateFlow()

    private var currentWeightKg: Float = 60.0f
    private var isSensorRegistered = false

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    init {
        registerStepSensor()
        refreshSync(currentWeightKg = 60.0f)
    }

    fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isHealthConnectSupported(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Throwable) {
            false
        }
    }

    private fun loadInitialData(): HealthSyncData {
        val todayStr = LocalDate.now().toString()
        val savedDate = prefs.getString("steps_date", "")
        val steps = if (savedDate == todayStr) {
            prefs.getInt("daily_steps", 0)
        } else {
            0
        }
        val cal = (steps * 60.0f * 0.0005f).roundToInt()
        val hasPerm = hasActivityRecognitionPermission()
        val hasSensor = (stepCounterSensor != null || stepDetectorSensor != null)
        val source = when {
            hasSensor && hasPerm -> "Hardware Sensor"
            hasSensor && !hasPerm -> "Permission Needed"
            else -> "Step Counter"
        }
        return HealthSyncData(
            dailySteps = steps,
            stepCaloriesBurned = cal,
            activeCaloriesBurned = cal,
            totalCaloriesBurned = cal,
            isHealthConnectAvailable = isHealthConnectSupported(),
            isHealthConnectAuthorized = false,
            hasHardwarePermission = hasPerm,
            hasHardwareSensor = hasSensor,
            dataSource = source
        )
    }

    fun registerStepSensor() {
        if (isSensorRegistered) return
        if (!hasActivityRecognitionPermission()) {
            Log.w("HealthSyncManager", "ACTIVITY_RECOGNITION permission not granted yet")
            _syncData.value = _syncData.value.copy(
                hasHardwarePermission = false,
                dataSource = if (stepCounterSensor != null || stepDetectorSensor != null) "Permission Needed" else "No Sensor"
            )
            return
        }

        try {
            var registeredAny = false
            stepCounterSensor?.let { sensor ->
                val ok = sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) ?: false
                if (ok) {
                    registeredAny = true
                    Log.d("HealthSyncManager", "Successfully registered hardware TYPE_STEP_COUNTER")
                }
            }
            stepDetectorSensor?.let { sensor ->
                val ok = sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) ?: false
                if (ok) {
                    registeredAny = true
                    Log.d("HealthSyncManager", "Successfully registered hardware TYPE_STEP_DETECTOR")
                }
            }
            isSensorRegistered = registeredAny
            _syncData.value = _syncData.value.copy(
                hasHardwarePermission = true,
                dataSource = if (registeredAny) "Hardware Sensor" else "Step Sensor"
            )
        } catch (e: Exception) {
            Log.w("HealthSyncManager", "Could not register hardware step sensor", e)
        }
    }

    fun unregisterStepSensor() {
        if (isSensorRegistered) {
            try {
                sensorManager?.unregisterListener(this)
                isSensorRegistered = false
            } catch (e: Exception) {
                Log.w("HealthSyncManager", "Error unregistering sensor", e)
            }
        }
    }

    fun onPermissionGranted(weightKg: Float = currentWeightKg) {
        currentWeightKg = weightKg
        registerStepSensor()
        refreshSync(weightKg)
    }

    fun refreshSync(currentWeightKg: Float) {
        this.currentWeightKg = currentWeightKg
        registerStepSensor()

        CoroutineScope(Dispatchers.IO).launch {
            var fetchedFromHc = false
            if (isHealthConnectSupported()) {
                try {
                    val client = HealthConnectClient.getOrCreate(context)
                    val granted = client.permissionController.getGrantedPermissions()
                    if (granted.containsAll(permissions)) {
                        val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                        val endTime = Instant.now()
                        val timeRange = TimeRangeFilter.between(startTime, endTime)

                        val stepsResponse = client.readRecords(
                            ReadRecordsRequest(
                                recordType = StepsRecord::class,
                                timeRangeFilter = timeRange
                            )
                        )
                        val totalHcSteps = stepsResponse.records.sumOf { it.count }

                        val calsResponse = client.readRecords(
                            ReadRecordsRequest(
                                recordType = ActiveCaloriesBurnedRecord::class,
                                timeRangeFilter = timeRange
                            )
                        )
                        val activeCals = calsResponse.records.sumOf { it.energy.inKilocalories }.roundToInt()

                        var totalCals = 0
                        try {
                            val totalBurnResponse = client.readRecords(
                                ReadRecordsRequest(
                                    recordType = TotalCaloriesBurnedRecord::class,
                                    timeRangeFilter = timeRange
                                )
                            )
                            totalCals = totalBurnResponse.records.sumOf { it.energy.inKilocalories }.roundToInt()
                        } catch (_: Exception) {}

                        // Merge Health Connect steps with local hardware steps (take the maximum)
                        val todayStr = LocalDate.now().toString()
                        val savedDate = prefs.getString("steps_date", "")
                        val savedSteps = if (savedDate == todayStr) prefs.getInt("daily_steps", 0) else 0
                        val resolvedSteps = maxOf(savedSteps, totalHcSteps.toInt())
                        val stepBurn = (resolvedSteps * currentWeightKg * 0.0005f).roundToInt()
                        val effectiveBurn = if (activeCals > 0) activeCals else stepBurn

                        _syncData.value = HealthSyncData(
                            dailySteps = resolvedSteps,
                            stepCaloriesBurned = stepBurn,
                            activeCaloriesBurned = effectiveBurn,
                            totalCaloriesBurned = totalCals,
                            isHealthConnectAvailable = true,
                            isHealthConnectAuthorized = true,
                            hasHardwarePermission = hasActivityRecognitionPermission(),
                            hasHardwareSensor = (stepCounterSensor != null || stepDetectorSensor != null),
                            dataSource = "Health Connect"
                        )
                        saveSteps(resolvedSteps)
                        fetchedFromHc = true
                    }
                } catch (e: Exception) {
                    Log.w("HealthSyncManager", "Health Connect query error", e)
                }
            }

            if (!fetchedFromHc) {
                // Read from SharedPreferences / hardware sensor
                val todayStr = LocalDate.now().toString()
                val savedDate = prefs.getString("steps_date", "")
                val currentSteps = if (savedDate == todayStr) prefs.getInt("daily_steps", 0) else 0
                val stepBurn = (currentSteps * currentWeightKg * 0.0005f).roundToInt()
                val hasPerm = hasActivityRecognitionPermission()
                val hasSensor = (stepCounterSensor != null || stepDetectorSensor != null)
                val source = when {
                    hasPerm && hasSensor -> "Hardware Sensor"
                    !hasPerm && hasSensor -> "Permission Needed"
                    else -> "Step Counter"
                }

                _syncData.value = HealthSyncData(
                    dailySteps = currentSteps,
                    stepCaloriesBurned = stepBurn,
                    activeCaloriesBurned = stepBurn,
                    totalCaloriesBurned = stepBurn,
                    isHealthConnectAvailable = isHealthConnectSupported(),
                    isHealthConnectAuthorized = false,
                    hasHardwarePermission = hasPerm,
                    hasHardwareSensor = hasSensor,
                    dataSource = source
                )
            }
        }
    }

    fun setManualSteps(steps: Int, weightKg: Float = currentWeightKg) {
        val nonNegSteps = steps.coerceAtLeast(0)
        val stepBurn = (nonNegSteps * weightKg * 0.0005f).roundToInt()
        _syncData.value = _syncData.value.copy(
            dailySteps = nonNegSteps,
            stepCaloriesBurned = stepBurn,
            activeCaloriesBurned = stepBurn,
            dataSource = "Manual / Step Sensor"
        )
        saveSteps(nonNegSteps)
    }

    fun addSteps(additionalSteps: Int, weightKg: Float = currentWeightKg) {
        val newTotal = _syncData.value.dailySteps + additionalSteps
        setManualSteps(newTotal, weightKg)
    }

    private fun saveSteps(steps: Int) {
        prefs.edit()
            .putString("steps_date", LocalDate.now().toString())
            .putInt("daily_steps", steps)
            .apply()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val today = LocalDate.now().toString()
        val savedDate = prefs.getString("steps_date", "")

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val rawCounter = event.values[0]
            var dailySteps = if (savedDate == today) prefs.getInt("daily_steps", 0) else 0
            var bootOffset = prefs.getFloat("boot_steps_offset", -1f)
            val lastRaw = prefs.getFloat("last_raw_sensor_steps", -1f)

            if (savedDate != today) {
                // Day reset
                dailySteps = 0
                bootOffset = rawCounter
                prefs.edit()
                    .putString("steps_date", today)
                    .putInt("daily_steps", 0)
                    .putFloat("boot_steps_offset", bootOffset)
                    .putFloat("last_raw_sensor_steps", rawCounter)
                    .apply()
            } else if (bootOffset < 0f) {
                bootOffset = (rawCounter - dailySteps).coerceAtLeast(0f)
                prefs.edit()
                    .putString("steps_date", today)
                    .putFloat("boot_steps_offset", bootOffset)
                    .putFloat("last_raw_sensor_steps", rawCounter)
                    .apply()
            } else if (rawCounter < lastRaw) {
                // Device rebooted
                bootOffset = rawCounter
                prefs.edit()
                    .putFloat("boot_steps_offset", bootOffset)
                    .putFloat("last_raw_sensor_steps", rawCounter)
                    .apply()
            } else {
                val delta = (rawCounter - bootOffset).coerceAtLeast(0f).toInt()
                dailySteps = maxOf(dailySteps, delta)
                prefs.edit()
                    .putFloat("last_raw_sensor_steps", rawCounter)
                    .putInt("daily_steps", dailySteps)
                    .apply()
            }

            val stepBurn = (dailySteps * currentWeightKg * 0.0005f).roundToInt()
            _syncData.value = _syncData.value.copy(
                dailySteps = dailySteps,
                stepCaloriesBurned = stepBurn,
                activeCaloriesBurned = stepBurn,
                hasHardwarePermission = true,
                dataSource = "Hardware Sensor"
            )
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                val current = if (savedDate == today) prefs.getInt("daily_steps", 0) else 0
                val updated = current + 1
                prefs.edit()
                    .putString("steps_date", today)
                    .putInt("daily_steps", updated)
                    .apply()
                val stepBurn = (updated * currentWeightKg * 0.0005f).roundToInt()
                _syncData.value = _syncData.value.copy(
                    dailySteps = updated,
                    stepCaloriesBurned = stepBurn,
                    activeCaloriesBurned = stepBurn,
                    hasHardwarePermission = true,
                    dataSource = "Hardware Sensor"
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
