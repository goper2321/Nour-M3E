package com.example.features.prayer.calculation

import java.util.Calendar
import java.util.Date
import kotlin.math.floor

object HijriDateHelper {

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    data class HijriDate(
        val day: Int,
        val month: Int, // 1 to 12
        val year: Int,
        val monthNameEn: String,
        val monthNameAr: String
    ) {
        val formattedEn: String
            get() = "$day $monthNameEn $year AH"

        val formattedAr: String
            get() = "$day $monthNameAr $year هـ"
    }

    /**
     * Converts a Gregorian Date to Hijri Date using astronomical calculation.
     */
    fun fromGregorian(date: Date, adjustmentDays: Int = 0): HijriDate {
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, adjustmentDays)
        }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        // Epoch of Islamic Calendar is JD 1948439.5
        val epoch = 1948439.5
        val daysSinceEpoch = jd - epoch

        val cycle = floor(daysSinceEpoch / 10631.0)
        val cycleDays = daysSinceEpoch - (cycle * 10631.0)

        var hijriYear = (cycle * 30.0) + floor((cycleDays + 0.5) / 354.366)
        val yearStartDays = floor((hijriYear - 1) * 354.366)
        var dayOfYear = daysSinceEpoch - yearStartDays

        if (dayOfYear <= 0) {
            hijriYear -= 1
            dayOfYear += 354
        }

        var hijriMonth = 1
        var remainingDays = dayOfYear.toInt()
        val monthLengths = listOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)

        for (i in 0 until 12) {
            val len = monthLengths[i]
            if (remainingDays <= len) {
                hijriMonth = i + 1
                break
            }
            remainingDays -= len
        }

        val hijriDay = remainingDays.coerceIn(1, 30)
        val monthIndex = (hijriMonth - 1).coerceIn(0, 11)

        return HijriDate(
            day = hijriDay,
            month = hijriMonth,
            year = hijriYear.toInt(),
            monthNameEn = HIJRI_MONTHS_EN[monthIndex],
            monthNameAr = HIJRI_MONTHS_AR[monthIndex]
        )
    }
}
