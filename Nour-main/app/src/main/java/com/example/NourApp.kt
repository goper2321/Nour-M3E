package com.example

import android.app.Application
import com.example.database.NourDatabase
import com.example.features.adhan.scheduler.PrayerNotificationScheduler

class NourApp : Application() {
    lateinit var database: NourDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = NourDatabase.getInstance(this)
        PrayerNotificationScheduler.initChannels(this)
    }

    companion object {
        lateinit var instance: NourApp
            private set
    }
}
