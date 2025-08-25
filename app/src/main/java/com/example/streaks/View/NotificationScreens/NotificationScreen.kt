package com.example.streaks.View.NotificationScreens

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.streaks.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.streaks.View.HomeScreens.HomeScreenActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val CHANNEL_ID = "streak_channel"

// ------------------- Notification Repository (in-memory for prototype) -------------------
object NotificationRepository {
    private val _notifications = mutableStateListOf<NotificationData>()
    val notifications: List<NotificationData> get() = _notifications

    fun addNotification(notification: NotificationData) {
        _notifications.add(0, notification) // add new notifications on top
    }
}

data class NotificationData(
    val streakId: Int,
    val streakName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ------------------- Notification Channel -------------------
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

// ------------------- Show Notification -------------------
@SuppressLint("MissingPermission")
fun showNotification(context: Context, streakId: Int, streakName: String) {
    val intent = Intent(context, HomeScreenActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent =
        PendingIntent.getActivity(context, streakId, intent, PendingIntent.FLAG_IMMUTABLE)

    val message = "Don’t forget: $streakName 🚀"

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Streak Reminder")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(streakId, builder.build()) // unique per streak
    }

    //  Save in repository for in-app history
    NotificationRepository.addNotification(
        NotificationData(streakId, streakName, message)
    )
}

// ------------------- Worker -------------------
class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val streakId = inputData.getInt("streakId", (System.currentTimeMillis() % Int.MAX_VALUE).toInt())
        val streakName = inputData.getString("streakName") ?: "Your Streak"

        showNotification(context, streakId, streakName)
        return Result.success()
    }
}

@Composable
fun NotificationScreen(paddingValues: PaddingValues) {
    val notifications = NotificationRepository.notifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Streak Notifications", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Text("No notifications yet.")
        } else {
            LazyColumn {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(notif.streakName, style = MaterialTheme.typography.titleMedium)
                            Text(notif.message, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                    .format(Date(notif.timestamp)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
