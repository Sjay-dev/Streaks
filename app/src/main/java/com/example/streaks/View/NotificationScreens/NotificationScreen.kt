package com.example.streaks.View.NotificationScreens

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.streaks.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.DataBase.StreakDao
import com.example.streaks.Model.StreakRepository
import com.example.streaks.View.HomeScreens.HomeScreenActivity
import com.example.streaks.ViewModel.StreakViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject


const val CHANNEL_ID = "streak_channel"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Streak Reminders"
        val descriptionText = "Reminders for your streaks"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("MissingPermission")
fun showNotification(context: Context, streakName: String) {
    val intent = Intent(context, HomeScreenActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent =
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Streak Reminder")
        .setContentText("Don't forget: $streakName")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(streakName.hashCode(), builder.build())
    }
}


@AndroidEntryPoint // if you are using Hilt
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StreakRepository  // Hilt will inject this

    override fun onReceive(context: Context, intent: Intent?) {
        val streakId = intent?.getIntExtra("streakId", -1) ?: return
        val streakName = intent.getStringExtra("streakName") ?: "Your streak"

        // Show notification
        showNotification(context, streakName)

        // Reschedule next day using repository
        CoroutineScope(Dispatchers.IO).launch {
            val streak = repository.getStreakById(streakId)
            streak?.reminderTime?.let { reminderTime ->
                // Re-schedule for tomorrow
                val viewModel = StreakViewModel(repository) // or inject ViewModel if using Hilt properly
                viewModel.scheduleReminder(context, streakId, streakName, reminderTime)
            }
        }
    }

    private fun showNotification(context: Context, streakName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "streak_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Streak Reminder")
            .setContentText("Don't forget your streak: $streakName 🚀")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(streakName.hashCode(), notification)
    }
}




@Composable
fun NotificationScreen(onNotifyClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onNotifyClick) {
            Text("Show Notification Now")
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}
