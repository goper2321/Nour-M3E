package com.example.features.adhan.scheduler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.models.PrayerName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {
    const val CHANNEL_PRAYER_ID = "nour_prayer_notifications"
    const val CHANNEL_COUNTDOWN_ID = "nour_live_countdown"

    const val ACTION_PRAYER_ALARM = "com.aistudio.nour.ACTION_PRAYER_ALARM"
    const val ACTION_MARK_PRAYED = "com.aistudio.nour.ACTION_MARK_PRAYED"
    const val ACTION_SNOOZE = "com.aistudio.nour.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.aistudio.nour.ACTION_DISMISS"

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_IS_PRE_REMINDER = "extra_is_pre_reminder"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "Adhan & Prayer Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Exact notifications and Adhan alarms when prayer time arrives"
                enableVibration(true)
            }

            val countdownChannel = NotificationChannel(
                CHANNEL_COUNTDOWN_ID,
                "Live Prayer Countdown",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent status and countdown to next Salah"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(countdownChannel)
        }
    }

    /**
     * Schedules an exact alarm with the system AlarmManager.
     */
    fun scheduleExactAlarm(
        context: Context,
        prayerName: PrayerName,
        triggerAtMillis: Long,
        isPreReminder: Boolean = false
    ) {
        if (triggerAtMillis < System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_IS_PRE_REMINDER, isPreReminder)
        }

        val requestCode = prayerName.ordinal + (if (isPreReminder) 100 else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In Android 12+, SCHEDULE_EXACT_ALARM can require user setting if revoked
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Updates or creates the live countdown status notification.
     */
    fun updateLiveCountdownNotification(
        context: Context,
        nextPrayerName: String,
        remainingFormatted: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val openAppIntent = PendingIntent.getActivity(
            context,
            999,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COUNTDOWN_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Next Prayer: $nextPrayerName")
            .setContentText("Countdown: $remainingFormatted remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val isPreReminder = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_PRE_REMINDER, false)
        val prayerName = try {
            PrayerName.valueOf(prayerNameString)
        } catch (e: Exception) {
            PrayerName.FAJR
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = PendingIntent.getActivity(
            context,
            prayerName.ordinal,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark as Prayed
        val prayedIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = PrayerNotificationScheduler.ACTION_MARK_PRAYED
            putExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME, prayerName.name)
        }
        val prayedPending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 200,
            prayedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 10 minutes
        val snoozeIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = PrayerNotificationScheduler.ACTION_SNOOZE
            putExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME, prayerName.name)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 300,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPreReminder) {
            "Reminder: ${prayerName.displayName} in 15 minutes"
        } else {
            "Hayya 'alas-Salah: It's time for ${prayerName.displayName} (${prayerName.arabicName})"
        }

        val body = if (isPreReminder) {
            "Prepare for prayer. Perform wudhu and find a clean space."
        } else {
            "Allahu Akbar. The call to ${prayerName.displayName} prayer has arrived."
        }

        val builder = NotificationCompat.Builder(context, PrayerNotificationScheduler.CHANNEL_PRAYER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Prayed", prayedPending)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10m", snoozePending)

        val notificationId = prayerName.ordinal + (if (isPreReminder) 50 else 10)
        notificationManager.notify(notificationId, builder.build())

        // Play Adhan Audio if it's the actual prayer time
        if (!isPreReminder) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = NourDatabase.getInstance(context)
                val settings = db.adhanSettingsDao().getSettingsForPrayer(prayerName.name)
                if (settings == null || settings.isAdhanEnabled) {
                    AdhanPlayer.playAdhan(
                        context = context,
                        toneName = settings?.toneName ?: "Makkah Adhan",
                        customUriString = settings?.customUri,
                        volume = settings?.volume ?: 1.0f
                    )
                }
            }
        }
    }
}

class PrayerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Stop any running Adhan playback
        AdhanPlayer.stop()

        when (intent.action) {
            PrayerNotificationScheduler.ACTION_MARK_PRAYED -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                CoroutineScope(Dispatchers.IO).launch {
                    val db = NourDatabase.getInstance(context)
                    db.prayerLogDao().insertOrUpdateLog(
                        PrayerLogEntity(
                            date = todayStr,
                            prayerName = prayerNameString,
                            status = "PRAYED",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                notificationManager.cancelAll()
            }
            PrayerNotificationScheduler.ACTION_SNOOZE -> {
                val prayerName = try { PrayerName.valueOf(prayerNameString) } catch (e: Exception) { PrayerName.FAJR }
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L)
                PrayerNotificationScheduler.scheduleExactAlarm(context, prayerName, snoozeTime, isPreReminder = false)
                notificationManager.cancelAll()
            }
            PrayerNotificationScheduler.ACTION_DISMISS -> {
                notificationManager.cancelAll()
            }
        }
    }
}
