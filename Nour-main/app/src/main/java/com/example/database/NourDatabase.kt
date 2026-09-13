package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PrayerLogEntity::class,
        BookmarkEntity::class,
        KhatmahPlanEntity::class,
        AdhanSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NourDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun khatmahDao(): KhatmahDao
    abstract fun adhanSettingsDao(): AdhanSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: NourDatabase? = null

        fun getInstance(context: Context): NourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NourDatabase::class.java,
                    "nour_islamic_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultAdhan = listOf(
                                AdhanSettingsEntity("FAJR", isAdhanEnabled = true, toneName = "Makkah Adhan"),
                                AdhanSettingsEntity("DHUHR", isAdhanEnabled = true, toneName = "Madinah Adhan"),
                                AdhanSettingsEntity("ASR", isAdhanEnabled = true, toneName = "Madinah Adhan"),
                                AdhanSettingsEntity("MAGHRIB", isAdhanEnabled = true, toneName = "Al-Aqsa Adhan"),
                                AdhanSettingsEntity("ISHA", isAdhanEnabled = true, toneName = "Makkah Adhan")
                            )
                            getInstance(context).adhanSettingsDao().insertAll(defaultAdhan)

                            val defaultKhatmah = KhatmahPlanEntity(
                                title = "Ramadan 30-Day Khatmah",
                                targetDays = 30,
                                startPage = 1,
                                currentPage = 1,
                                totalPages = 604,
                                isActive = true
                            )
                            getInstance(context).khatmahDao().insertPlan(defaultKhatmah)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
