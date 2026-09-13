package com.example.features.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaData(
    val qiblaBearing: Float = 0f, // Direction of Kaaba relative to True North (0-360)
    val distanceKm: Double = 0.0,
    val deviceHeading: Float = 0f, // Device azimuth relative to True North (0-360)
    val relativeAngle: Float = 0f, // Angle to rotate arrow towards Kaaba
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isFacingQibla: Boolean = false, // Within +/- 3 degrees of Kaaba
    val sunAzimuth: Float = 0f, // Sun bearing for celestial cross-check
    val hasMagnetometer: Boolean = true
)

object QiblaCalculator {
    const val KAABA_LAT = 21.422487
    const val KAABA_LNG = 39.826206

    /**
     * Calculates the great-circle forward azimuth bearing to the Kaaba from user coordinates.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val deltaLngRad = Math.toRadians(KAABA_LNG - userLng)

        val y = sin(deltaLngRad) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLngRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360f) % 360f
        return bearing
    }

    /**
     * Calculates the great-circle distance to the Kaaba in kilometers.
     */
    fun calculateDistanceKm(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(KAABA_LAT - userLat)
        val dLng = Math.toRadians(KAABA_LNG - userLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LAT)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Approximates current Sun azimuth for visual sun cross-check.
     */
    fun calculateSunAzimuth(userLat: Double, userLng: Double, date: Date = Date()): Float {
        val cal = Calendar.getInstance().apply { time = date }
        val hour = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)
        // Approximate solar hour angle: 12 is solar south (in northern hemisphere)
        val solarHourAngle = (hour - 12.0) * 15.0
        val approxSunAzimuth = (180.0 + solarHourAngle + (39.8 - userLng) * 0.25).toFloat()
        return (approxSunAzimuth + 360f) % 360f
    }
}

class QiblaSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _qiblaState = MutableStateFlow(QiblaData(hasMagnetometer = magnetometer != null || rotationVectorSensor != null))
    val qiblaState: StateFlow<QiblaData> = _qiblaState.asStateFlow()

    private var userLat: Double = QiblaCalculator.KAABA_LAT
    private var userLng: Double = QiblaCalculator.KAABA_LNG

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val gravityValues = FloatArray(3)
    private val geoMagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeoMagnetic = false

    fun setLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        updateCalculations(_qiblaState.value.deviceHeading, _qiblaState.value.accuracy)
    }

    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        var azimuthDegrees = 0f
        var accuracy = event.accuracy

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, geoMagneticValues, 0, 3)
                hasGeoMagnetic = true
            }

            if (hasGravity && hasGeoMagnetic) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geoMagneticValues)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                }
            }
        }

        // Normalize device heading to 0-360
        val heading = (azimuthDegrees + 360f) % 360f
        updateCalculations(heading, accuracy)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _qiblaState.value = _qiblaState.value.copy(accuracy = accuracy)
    }

    fun setManualHeading(heading: Float) {
        updateCalculations(heading, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    }

    private fun updateCalculations(deviceHeading: Float, accuracy: Int) {
        val qiblaBearing = QiblaCalculator.calculateQiblaBearing(userLat, userLng)
        val distanceKm = QiblaCalculator.calculateDistanceKm(userLat, userLng)
        val relativeAngle = (qiblaBearing - deviceHeading + 360f) % 360f
        val isFacingQibla = relativeAngle in 357f..360f || relativeAngle in 0f..3f
        val sunAzimuth = QiblaCalculator.calculateSunAzimuth(userLat, userLng)

        _qiblaState.value = QiblaData(
            qiblaBearing = qiblaBearing,
            distanceKm = distanceKm,
            deviceHeading = deviceHeading,
            relativeAngle = relativeAngle,
            accuracy = accuracy,
            isFacingQibla = isFacingQibla,
            sunAzimuth = sunAzimuth,
            hasMagnetometer = magnetometer != null || rotationVectorSensor != null
        )
    }
}
