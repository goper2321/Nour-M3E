package com.example.features.prayer.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PrayerName(val displayName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء")
}

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    MUSLIM_WORLD_LEAGUE("Muslim World League (MWL)", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    EGYPTIAN("Egyptian General Authority of Survey", 19.5, 17.5),
    UMM_AL_QURA("Umm Al-Qura University, Makkah", 18.5, 0.0, ishaIntervalMinutes = 90),
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
    TEHRAN("Institute of Geophysics, University of Tehran", 17.7, 14.0),
    GULF("Gulf Region / Dubai", 19.5, 0.0, ishaIntervalMinutes = 90),
    QATAR("Qatar Awqaf", 18.0, 0.0, ishaIntervalMinutes = 90),
    SINGAPORE("Majlis Ugama Islam Singapura (MUIS)", 20.0, 18.0),
    FRANCE("Union des Organisations Islamiques de France (UOIF)", 12.0, 12.0),
    SWEDEN("Islamiska Förbundet (Sweden)", 18.0, 17.0),
    MOROCCO("Morocco Habous & Islamic Affairs", 19.0, 17.0)
}

enum class Madhab(val title: String, val shadowFactor: Double) {
    SHAFI_MALIKI_HANBALI("Standard (Shafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("Hanafi (Later Asr)", 2.0)
}

enum class HighLatitudeRule(val title: String) {
    ANGLE_BASED("Angle-Based Rule"),
    MIDDLE_OF_NIGHT("Middle of the Night"),
    ONE_SEVENTH("One-Seventh of the Night"),
    NONE("None / Strict Astronomical")
}

data class PrayerAdjustments(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
)

data class UserLocation(
    val latitude: Double = 21.4225, // Default Makkah
    val longitude: Double = 39.8262,
    val cityName: String = "Makkah al-Mukarramah",
    val countryName: String = "Saudi Arabia",
    val timezoneOffsetHours: Double = 3.0,
    val isAutoDetected: Boolean = false
)

data class PrayerTimes(
    val date: Date,
    val fajr: Date,
    val sunrise: Date,
    val dhuhr: Date,
    val asr: Date,
    val maghrib: Date,
    val isha: Date,
    val calculationSource: String = "On-Device Astronomical Engine"
) {
    fun getTimeFor(prayer: PrayerName): Date {
        return when (prayer) {
            PrayerName.FAJR -> fajr
            PrayerName.SUNRISE -> sunrise
            PrayerName.DHUHR -> dhuhr
            PrayerName.ASR -> asr
            PrayerName.MAGHRIB -> maghrib
            PrayerName.ISHA -> isha
        }
    }

    fun formattedTime(prayer: PrayerName): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(getTimeFor(prayer))
    }

    /**
     * Determines current active prayer interval, next upcoming prayer,
     * remaining milliseconds, and fractional completion of the current interval.
     */
    fun getCountdownState(now: Date = Date()): CountdownState {
        val nowMillis = now.time
        val pList = listOf(
            PrayerName.FAJR to fajr.time,
            PrayerName.SUNRISE to sunrise.time,
            PrayerName.DHUHR to dhuhr.time,
            PrayerName.ASR to asr.time,
            PrayerName.MAGHRIB to maghrib.time,
            PrayerName.ISHA to isha.time
        )

        // If before Fajr today, next is Fajr
        if (nowMillis < fajr.time) {
            val prevIsha = isha.time - (24 * 3600 * 1000L)
            val totalSpan = (fajr.time - prevIsha).coerceAtLeast(1L)
            val elapsed = (nowMillis - prevIsha).coerceAtLeast(0L)
            val progress = (elapsed.toDouble() / totalSpan).toFloat().coerceIn(0f, 1f)
            return CountdownState(
                currentPrayer = PrayerName.ISHA,
                nextPrayer = PrayerName.FAJR,
                remainingMillis = fajr.time - nowMillis,
                progress = progress
            )
        }

        // Check intervals within the day
        for (i in 0 until pList.size - 1) {
            val (currentP, currentT) = pList[i]
            val (nextP, nextT) = pList[i + 1]
            if (nowMillis in currentT until nextT) {
                val totalSpan = (nextT - currentT).coerceAtLeast(1L)
                val elapsed = (nowMillis - currentT).coerceAtLeast(0L)
                val progress = (elapsed.toDouble() / totalSpan).toFloat().coerceIn(0f, 1f)
                return CountdownState(
                    currentPrayer = currentP,
                    nextPrayer = nextP,
                    remainingMillis = nextT - nowMillis,
                    progress = progress
                )
            }
        }

        // After Isha: next is tomorrow's Fajr
        val tomorrowFajr = fajr.time + (24 * 3600 * 1000L)
        val totalSpan = (tomorrowFajr - isha.time).coerceAtLeast(1L)
        val elapsed = (nowMillis - isha.time).coerceAtLeast(0L)
        val progress = (elapsed.toDouble() / totalSpan).toFloat().coerceIn(0f, 1f)
        return CountdownState(
            currentPrayer = PrayerName.ISHA,
            nextPrayer = PrayerName.FAJR,
            remainingMillis = tomorrowFajr - nowMillis,
            progress = progress
        )
    }
}

data class CountdownState(
    val currentPrayer: PrayerName,
    val nextPrayer: PrayerName,
    val remainingMillis: Long,
    val progress: Float
) {
    val formattedRemaining: String
        get() {
            val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
}
