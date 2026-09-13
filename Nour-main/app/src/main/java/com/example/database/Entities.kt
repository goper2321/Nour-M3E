package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: yyyy-MM-dd
    val prayerName: String, // FAJR, DHUHR, ASR, MAGHRIB, ISHA
    val status: String, // PRAYED, PRAYED_LATE, MISSED, EXCUSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahText: String,
    val translationText: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "khatmah_plans")
data class KhatmahPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDays: Int,
    val startPage: Int = 1,
    val currentPage: Int = 1,
    val totalPages: Int = 604,
    val startDateMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val lastReadDate: String = ""
)

@Entity(tableName = "adhan_settings")
data class AdhanSettingsEntity(
    @PrimaryKey val prayerName: String,
    val isAdhanEnabled: Boolean = true,
    val toneName: String = "Makkah Adhan",
    val customUri: String? = null,
    val volume: Float = 1.0f,
    val preReminderMinutes: Int = 15,
    val isPreReminderEnabled: Boolean = false
)
