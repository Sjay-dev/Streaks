package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.DataBase.StreakDataBase
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.NotificationType
import com.example.streaks.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

const val  CHANNEL = "Streaks"
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var mediaPlayer: MediaPlayer? = null
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        val streakId = intent?.getIntExtra("STREAK_ID", -1) ?: -1
        val streakName = intent?.getStringExtra("STREAK_NAME") ?: "Streak"
        val typeName = intent?.getStringExtra("NOTIFICATION_TYPE") ?: NotificationType.DEFAULT.name
        val notificationType = NotificationType.valueOf(typeName)

        stopAlarm()

        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Streak Reminder")
            .setContentText("Don't forget: $streakName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)

        // Notification type handling
        when (notificationType) {
            NotificationType.DEFAULT -> {
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            NotificationType.SILENT -> {
                builder.setSound(null).setVibrate(null)
            }
            NotificationType.ALARM -> {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm1}")
                builder.setSound(soundUri)
                builder.setVibrate(longArrayOf(0, 500, 1000, 500))
                mediaPlayer = MediaPlayer.create(context, soundUri).apply {
                    isLooping = true
                    start()
                }
            }
        }

        val notification = builder.build()
        NotificationManagerCompat.from(context).notify(streakId, notification)

        // Optionally: Insert into your Notifications table
//        saveNotification(context, streakId, streakName, "Reminder triggered")
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

//    private fun saveNotification(context: Context, streakId: Int, streakName: String, message: String) {
//        // This should go through your Repository/DAO
//        val notification = NotificationModel(
//            streakId = streakId,
//            streakName = streakName,
//            message = message,
//            time = LocalTime.now(),
//            status = "TRIGGERED"
//        )
//        // Insert into DB (Room) on background thread
//        // Example: CoroutineScope(Dispatchers.IO).launch { dao.insert(notification) }
//    }
}



