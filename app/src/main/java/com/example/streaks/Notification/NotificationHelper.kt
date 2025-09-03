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
        // ✅ Mark Done intent
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_DONE
            putExtra("streakId", streak.streakId)
        }

        // ❌ End Streak intent
        val endStreakIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_END_STREAK
            putExtra("streakId", streak.streakId)
        }

        val markDonePending = PendingIntent.getBroadcast(
            context,
            streak.streakId, // unique requestCode
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endStreakPending = PendingIntent.getBroadcast(
            context,
            streak.streakId + 1000, // different requestCode so they don’t clash
            endStreakIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelFor(streak.notificationType))
            .setContentTitle(streak.streakName)
            .setContentText("It’s time to keep your streak alive!")
            .setSmallIcon(R.drawable.arrow_back_24px) // use your app’s bell/check icon
            .addAction(R.drawable.monitoring_24px, "Mark Done", markDonePending)
            .addAction(R.drawable.ic_launcher_background, "End Streak", endStreakPending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ensures it shows on time
            .build()
    }
}
