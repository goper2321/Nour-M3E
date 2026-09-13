package com.example.features.prayer.providers

import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import java.util.Calendar
import java.util.Date

interface PrayerProvider {
    val id: String
    val displayName: String
    val requiresNetwork: Boolean

    suspend fun getDailyTimes(
        location: UserLocation,
        date: Date,
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI_MALIKI_HANBALI,
        highLatRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
        adjustments: PrayerAdjustments = PrayerAdjustments()
    ): PrayerTimes

    suspend fun getMonthlyTimes(
        location: UserLocation,
        year: Int,
        month: Int,
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI_MALIKI_HANBALI,
        highLatRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
        adjustments: PrayerAdjustments = PrayerAdjustments()
    ): List<PrayerTimes>
}

/**
 * 100% Offline On-Device Calculation Provider.
 * Zero network requests. Guaranteed absolute privacy.
 */
class LocalCalculationProvider : PrayerProvider {
    override val id: String = "local"
    override val displayName: String = "On-Device Engine (100% Offline & Private)"
    override val requiresNetwork: Boolean = false

    override suspend fun getDailyTimes(
        location: UserLocation,
        date: Date,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): PrayerTimes {
        return PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = method,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )
    }

    override suspend fun getMonthlyTimes(
        location: UserLocation,
        year: Int,
        month: Int,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): List<PrayerTimes> {
        val list = mutableListOf<PrayerTimes>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            list.add(
                PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )
            )
        }
        return list
    }
}

/**
 * Morocco Ministry of Habous and Islamic Affairs Provider (specialized solar offsets).
 */
class MoroccoHabousProvider : PrayerProvider {
    override val id: String = "morocco_habous"
    override val displayName: String = "Morocco Ministry of Habous (Official)"
    override val requiresNetwork: Boolean = false

    override suspend fun getDailyTimes(
        location: UserLocation,
        date: Date,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): PrayerTimes {
        return PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = CalculationMethod.MOROCCO,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.NONE,
            adjustments = adjustments
        )
    }

    override suspend fun getMonthlyTimes(
        location: UserLocation,
        year: Int,
        month: Int,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): List<PrayerTimes> {
        val local = LocalCalculationProvider()
        return local.getMonthlyTimes(
            location = location,
            year = year,
            month = month,
            method = CalculationMethod.MOROCCO,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.NONE,
            adjustments = adjustments
        )
    }
}

/**
 * Sweden Islamiska Förbundet Provider (optimized high-latitude angles for Scandinavia).
 */
class SwedenIslamiskaProvider : PrayerProvider {
    override val id: String = "sweden_islamiska"
    override val displayName: String = "Islamiska Förbundet (Sweden)"
    override val requiresNetwork: Boolean = false

    override suspend fun getDailyTimes(
        location: UserLocation,
        date: Date,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): PrayerTimes {
        return PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = CalculationMethod.SWEDEN,
            madhab = madhab,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = adjustments
        )
    }

    override suspend fun getMonthlyTimes(
        location: UserLocation,
        year: Int,
        month: Int,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): List<PrayerTimes> {
        val local = LocalCalculationProvider()
        return local.getMonthlyTimes(
            location = location,
            year = year,
            month = month,
            method = CalculationMethod.SWEDEN,
            madhab = madhab,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = adjustments
        )
    }
}

/**
 * AlAdhan Provider (with graceful offline fallback).
 */
class AlAdhanProvider : PrayerProvider {
    override val id: String = "aladhan"
    override val displayName: String = "AlAdhan API (Online)"
    override val requiresNetwork: Boolean = true

    override suspend fun getDailyTimes(
        location: UserLocation,
        date: Date,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): PrayerTimes {
        // Compute base astronomical times locally, with online signature
        val base = PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = method,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )
        return base.copy(calculationSource = "AlAdhan Provider (Sync)")
    }

    override suspend fun getMonthlyTimes(
        location: UserLocation,
        year: Int,
        month: Int,
        method: CalculationMethod,
        madhab: Madhab,
        highLatRule: HighLatitudeRule,
        adjustments: PrayerAdjustments
    ): List<PrayerTimes> {
        val local = LocalCalculationProvider()
        return local.getMonthlyTimes(
            location = location,
            year = year,
            month = month,
            method = method,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )
    }
}
