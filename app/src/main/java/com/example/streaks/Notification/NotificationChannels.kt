package com.example.streaks.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import com.example.streaks.Model.ReminderSound
import com.example.streaks.R

object NotificationChannels {
    fun idFor(sound: ReminderSound, vibration: Boolean) = "private_${sound.name.lowercase()}_${if (vibration) "v" else "quiet"}"

    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Private reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Discreet, offline reminders from Streaks"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
        )
        ReminderSound.entries.forEach { sound ->
            listOf(false, true).forEach { vibration ->
                val channel = NotificationChannel(idFor(sound, vibration), "Private reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Discreet, offline reminders from Streaks"
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
                    enableVibration(vibration)
                    val uri = when (sound) {
                        ReminderSound.SOFT_CHIME -> rawUri(context, R.raw.soft_chime)
                        ReminderSound.BELL -> rawUri(context, R.raw.bell)
                        ReminderSound.GENTLE_PULSE -> rawUri(context, R.raw.gentle_pulse)
                        ReminderSound.SILENT -> null
                        ReminderSound.PHONE_DEFAULT -> android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    }
                    setSound(uri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun rawUri(context: Context, id: Int): Uri = Uri.parse("android.resource://${context.packageName}/$id")
}
