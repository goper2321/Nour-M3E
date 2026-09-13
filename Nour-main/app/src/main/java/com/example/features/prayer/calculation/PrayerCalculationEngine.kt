package com.example.features.prayer.calculation

import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalculationEngine {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    /**
     * Calculates prayer times for a given date, coordinates, method, and adjustments.
     */
    fun calculate(
        date: Date,
        location: UserLocation,
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI_MALIKI_HANBALI,
        highLatRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
        adjustments: PrayerAdjustments = PrayerAdjustments()
    ): PrayerTimes {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            time = date
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian date at 0h UT
        val jd = julianDay(year, month, day) - (location.longitude / (15.0 * 24.0))

        // Solar parameters
        val (declination, eqTime) = sunCoordinates(jd)

        // Dhuhr is transit time (when sun crosses the meridian)
        val timezoneOffset = location.timezoneOffsetHours
        val solarNoonHours = 12.0 + timezoneOffset - (location.longitude / 15.0) - (eqTime / 60.0)

        // Sunrise & Sunset angle: -0.833°
        val sunAltitude = -0.8333
        val sunriseHourAngle = hourAngle(location.latitude, declination, sunAltitude)
        val sunriseHours = solarNoonHours - (sunriseHourAngle / 15.0)
        val sunsetHours = solarNoonHours + (sunriseHourAngle / 15.0)

        // Fajr
        val fajrAngle = -method.fajrAngle
        var fajrHourAngle = hourAngle(location.latitude, declination, fajrAngle)
        if (fajrHourAngle.isNaN()) {
            fajrHourAngle = adjustHighLatitudeHourAngle(sunriseHourAngle, method.fajrAngle, highLatRule)
        }
        val fajrHours = solarNoonHours - (fajrHourAngle / 15.0)

        // Asr: shadow factor (1 for standard, 2 for Hanafi)
        val latRad = location.latitude * DEG_TO_RAD
        val decRad = declination * DEG_TO_RAD
        val shadow = madhab.shadowFactor + tan(abs(latRad - decRad))
        val asrAltitude = kotlin.math.atan(1.0 / shadow) * RAD_TO_DEG
        val asrHourAngle = hourAngle(location.latitude, declination, asrAltitude)
        val asrHours = solarNoonHours + (asrHourAngle / 15.0)

        // Maghrib: sunset (for some Shia methods, slight twilight depression like 4.5°)
        val maghribHours = if (method == CalculationMethod.TEHRAN) {
            val maghribAngle = -4.5
            val maghribHourAngle = hourAngle(location.latitude, declination, maghribAngle)
            solarNoonHours + (maghribHourAngle / 15.0)
        } else {
            sunsetHours
        }

        // Isha
        val ishaHours = if (method.ishaIntervalMinutes != null) {
            maghribHours + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaAngle = -method.ishaAngle
            var ishaHourAngle = hourAngle(location.latitude, declination, ishaAngle)
            if (ishaHourAngle.isNaN()) {
                ishaHourAngle = adjustHighLatitudeHourAngle(sunriseHourAngle, method.ishaAngle, highLatRule)
            }
            solarNoonHours + (ishaHourAngle / 15.0)
        }

        // Convert calculated fractional hours to Dates with adjustments
        val fajrDate = makeDate(year, month, day, fajrHours, adjustments.fajr)
        val sunriseDate = makeDate(year, month, day, sunriseHours, adjustments.sunrise)
        val dhuhrDate = makeDate(year, month, day, solarNoonHours, adjustments.dhuhr)
        val asrDate = makeDate(year, month, day, asrHours, adjustments.asr)
        val maghribDate = makeDate(year, month, day, maghribHours, adjustments.maghrib)
        val ishaDate = makeDate(year, month, day, ishaHours, adjustments.isha)

        return PrayerTimes(
            date = date,
            fajr = fajrDate,
            sunrise = sunriseDate,
            dhuhr = dhuhrDate,
            asr = asrDate,
            maghrib = maghribDate,
            isha = ishaDate,
            calculationSource = method.title
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716.0)) + floor(30.6001 * (m + 1.0)) + day + b - 1524.5
    }

    private fun sunCoordinates(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(g * DEG_TO_RAD) + 0.020 * sin(2.0 * g * DEG_TO_RAD))

        val e = 23.439 - 0.00000036 * d
        val dec = asin(sin(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD)) * RAD_TO_DEG
        var ra = atan2(cos(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD), cos(l * DEG_TO_RAD)) * RAD_TO_DEG
        ra = fixAngle(ra) / 15.0

        val eqTime = (q / 15.0) - ra
        return Pair(dec, eqTime * 60.0) // eqTime in minutes
    }

    private fun hourAngle(lat: Double, dec: Double, alt: Double): Double {
        val latR = lat * DEG_TO_RAD
        val decR = dec * DEG_TO_RAD
        val altR = alt * DEG_TO_RAD
        val cosH = (sin(altR) - sin(latR) * sin(decR)) / (cos(latR) * cos(decR))
        if (cosH < -1.0 || cosH > 1.0) return Double.NaN
        return acos(cosH) * RAD_TO_DEG
    }

    private fun adjustHighLatitudeHourAngle(
        sunriseHourAngle: Double,
        angle: Double,
        rule: HighLatitudeRule
    ): Double {
        val baseHourAngle = if (sunriseHourAngle.isNaN()) 90.0 else sunriseHourAngle
        val nightFraction = when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5
            HighLatitudeRule.ONE_SEVENTH -> 1.0 / 7.0
            HighLatitudeRule.ANGLE_BASED, HighLatitudeRule.NONE -> angle / 60.0
        }
        val nightHours = (180.0 - baseHourAngle) * 2.0
        return baseHourAngle + (nightHours * nightFraction / 2.0)
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0.0) a += 360.0
        return a
    }

    private fun makeDate(year: Int, month: Int, day: Int, fractionalHours: Double, adjustmentMinutes: Int): Date {
        var hours = fractionalHours
        var dayOffset = 0
        if (hours < 0) {
            hours += 24.0
            dayOffset = -1
        } else if (hours >= 24.0) {
            hours -= 24.0
            dayOffset = 1
        }
        val totalSeconds = (hours * 3600.0).roundToLong() + (adjustmentMinutes * 60L)
        val hourInt = (totalSeconds / 3600).toInt()
        val minuteInt = ((totalSeconds % 3600) / 60).toInt()
        val secondInt = (totalSeconds % 60).toInt()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day + dayOffset)
            set(Calendar.HOUR_OF_DAY, hourInt.coerceIn(0, 23))
            set(Calendar.MINUTE, minuteInt.coerceIn(0, 59))
            set(Calendar.SECOND, secondInt.coerceIn(0, 59))
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }
}
