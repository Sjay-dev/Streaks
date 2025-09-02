package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.streaks.Model.NotificationType
import com.example.streaks.Model.StreakModel
import com.example.streaks.R

object NotificationHelper {
    const val CHANNEL_ID_DEFAULT = "streak_default"
    const val CHANNEL_ID_SILENT = "streak_silent"
    const val CHANNEL_ID_ALARM = "streak_alarm"

    @SuppressLint("ServiceCast")
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val defaultChannel = NotificationChannel(
            CHANNEL_ID_DEFAULT, "Default Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val silentChannel = NotificationChannel(
            CHANNEL_ID_SILENT, "Silent Reminders",
            NotificationManager.IMPORTANCE_LOW
        )
        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM, "Alarm Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannels(listOf(defaultChannel, silentChannel, alarmChannel))
    }

    private fun channelFor(type: NotificationType): String =
        when (type) {
            NotificationType.DEFAULT -> CHANNEL_ID_DEFAULT
            NotificationType.SILENT -> CHANNEL_ID_SILENT
            NotificationType.ALARM -> CHANNEL_ID_ALARM
        }

    fun buildNotification(context: Context, streak: StreakModel): Notification {
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MARK_DONE"
            putExtra("streakId", streak.streakId)
        }
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("streakId", streak.streakId)
        }

        val markDonePending = PendingIntent.getBroadcast(
            context, streak.streakId, markDoneIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val snoozePending = PendingIntent.getBroadcast(
            context, streak.streakId + 1000, snoozeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelFor(streak.notificationType))
            .setContentTitle(streak.streakName)
            .setContentText("It’s time to keep your streak alive!")
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(R.drawable.ic_check, "Mark Done", markDonePending)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePending)
            .setAutoCancel(true)
            .build()
    }
}
