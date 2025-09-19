package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.NotificationType
import com.example.streaks.Model.Status
import com.example.streaks.R
import com.example.streaks.View.HomeScreens.CHANNEL
import com.example.streaks.View.HomeScreens.HomeScreenActivity
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
const val STREAK_COLOR = "STREAK_COLOR"
const val NOTIFICATION_PAGE = "NOTIFICATION_PAGE"

@AndroidEntryPoint
class ReminderRecevier : BroadcastReceiver() {

    companion object {
        private var mediaPlayer: MediaPlayer? = null

        fun stopAlarm() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    @Inject lateinit var notificationRepository: NotificationRepository

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {

        val streakId = intent?.getIntExtra(STREAK_ID, 0) ?:0

        val streakName = intent?.getStringExtra(EXTRA_STREAK_NAME) ?: "Streak"

        val frequency = intent?.getStringExtra(EXTRA_FREQUENCY)?.let { Frequency.valueOf(it) } ?: Frequency.DAILY

        val streakColor = intent?.getLongExtra(STREAK_COLOR, 0L)

        val notificationType = intent?.getStringExtra(EXTRA_NOTIFICATION_TYPE)?.let { NotificationType.valueOf(it) } ?: NotificationType.DEFAULT

        when (intent?.action) {
            ACTION_DONE -> {
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

                val message = "It’s time for your ${frequency.name.lowercase()} streak!"

                val openIntent = Intent(context, HomeScreenActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(NOTIFICATION_PAGE, 1)
                }

                val openPending = PendingIntent.getActivity(
                    context,
                    streakId,
                    openIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Reminder: $streakName")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setContentIntent(openPending)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_launcher_foreground, "Done", donePending) // ✅ only Done button

                when (notificationType) {
                    NotificationType.DEFAULT -> {
                        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
                    }
                    NotificationType.SILENT -> {
                        builder.setSound(null)
                        builder.setVibrate(null)
                    }
                    NotificationType.ALARM -> {
                        val soundUri =
                            Uri.parse("android.resource://${context.packageName}/${R.raw.alarm1}")

                        builder
                            .setSound(soundUri)
                            .setCategory(NotificationCompat.CATEGORY_ALARM)
                            .setFullScreenIntent(donePending, true) // use donePending here
                            .setVibrate(longArrayOf(0, 500, 1000, 500))

                        mediaPlayer = MediaPlayer.create(context, soundUri).apply {
                            isLooping = true
                            start()

                            Handler(Looper.getMainLooper()).postDelayed({
                                stopAlarm()
                            }, 3 * 60 * 1000L)
                        }
                    }
                }

                val notification = builder.build()
                NotificationManagerCompat.from(context).notify(streakId, notification)

                // Save as ongoing when shown
                CoroutineScope(Dispatchers.IO).launch {
                    notificationRepository.addNotification(
                        NotificationModel(
                            streakId = streakId,
                            streakName = streakName,
                            streakColor = streakColor!!,
                            message = message,
                            status = Status.OnGoing,
                            frequency = frequency
                        )
                    )
                }
            }
        }
    }

}
