package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.DataBase.NotificationRepository
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.NotificationType
import com.example.streaks.Model.Status
import com.example.streaks.R
import com.example.streaks.View.HomeScreens.CHANNEL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ACTION_DONE = "ACTION_DONE"
const val ACTION_CANCEL = "ACTION_CANCEL"

const val EXTRA_STREAK_NAME = "EXTRA_STREAK_NAME"
const val EXTRA_FREQUENCY = "EXTRA_FREQUENCY"
const val EXTRA_NOTIFICATION_TYPE = "EXTRA_NOTIFICATION_TYPE"
const val STREAK_ID = "STREAK_ID"

@AndroidEntryPoint
class ReminderRecevier : BroadcastReceiver() {

    private var mediaPlayer: MediaPlayer? = null
    @Inject lateinit var notificationRepository: NotificationRepository

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {

        val streakId = intent?.getIntExtra(STREAK_ID, 0) ?:0

        val streakName = intent?.getStringExtra(EXTRA_STREAK_NAME) ?: "Streak"

        val frequency = intent?.getStringExtra(EXTRA_FREQUENCY)?.let { Frequency.valueOf(it) } ?: Frequency.DAILY

        val notificationType = intent?.getStringExtra(EXTRA_NOTIFICATION_TYPE)?.let { NotificationType.valueOf(it) } ?: NotificationType.DEFAULT

        when (intent?.action) {
            ACTION_DONE, ACTION_CANCEL -> {
                stopAlarm()
                NotificationManagerCompat.from(context).cancel(streakId)
            }
            else -> {
                stopAlarm()

                val doneIntent = Intent(context, ReminderRecevier::class.java).apply {
                    action = ACTION_DONE
                    putExtra(STREAK_ID, streakId)
                }
                val donePending = PendingIntent.getBroadcast(
                    context,
                    streakId * 10 + 1,
                    doneIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val cancelIntent = Intent(context, ReminderRecevier::class.java).apply {
                    action = ACTION_CANCEL
                    putExtra(STREAK_ID, streakId)
                }
                val cancelPending = PendingIntent.getBroadcast(
                    context,
                    streakId * 10 + 2,
                    cancelIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val message = "It’s time for your ${frequency.name.lowercase()} streak!"

                // Notification builder
                val builder = NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Reminder: $streakName")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_launcher_foreground, "Done", donePending)
                    .addAction(R.drawable.ic_launcher_foreground, "Cancel", cancelPending)
                    .setAutoCancel(false)

                when (notificationType) {
                    NotificationType.DEFAULT -> {
                        // ✅ Use system defaults (sound/vibrate/silent depending on ringer mode)
                        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
                    }

                    NotificationType.SILENT -> {
                        // ✅ No sound, no vibration
                        builder.setSound(null)
                        builder.setVibrate(null)
                    }

                    NotificationType.ALARM -> {
                        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm1}")

                        // ✅ Force alarm-like behavior
                        builder
                            .setSound(soundUri) // plays sound once
                            .setCategory(NotificationCompat.CATEGORY_ALARM) // mark as alarm
                            .setFullScreenIntent(cancelPending, true) // popup over lock screen
                            .setVibrate(longArrayOf(0, 500, 1000, 500))

                        // ✅ Loop sound manually
                        mediaPlayer = MediaPlayer.create(context, soundUri).apply {
                            isLooping = true
                            start()
                        }
                    }
                }

                val notification = builder.build()
                NotificationManagerCompat.from(context).notify(streakId, notification)
                CoroutineScope(Dispatchers.IO).launch {
                    notificationRepository.addNotification(
                        NotificationModel(
                            streakId = streakId,
                            streakName = streakName,
                            message = message,
                            status = Status.OnGoing,
                            frequency = frequency
                        )
                    )
                }

            }
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
