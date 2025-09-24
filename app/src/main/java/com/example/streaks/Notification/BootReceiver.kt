package com.example.streaks.Notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.StreakRepository
import com.example.streaks.ViewModel.StreakViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var streakRepository: StreakRepository
    @Inject lateinit var notificationRepository: NotificationRepository


    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {

            // Re-schedule alarms in background
            CoroutineScope(Dispatchers.IO).launch {
             val streaks = streakRepository.getAllStreaks().first()
                streaks.forEach { streak ->
                    val triggerAtMillis = LocalDateTime.of(LocalDate.now(), streak.reminderTime)
                        .let { if (it.isBefore(LocalDateTime.now())) it.plusDays(1) else it }
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    scheduleAlarm(context, streak , triggerAtMillis)
                    }
                }
            }
        }
    }


fun scheduleAlarm(context: Context, streak: StreakModel, triggerAtMillis: Long) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, ReminderRecevier::class.java).apply {
        putExtra(EXTRA_STREAK_NAME, streak.streakName)
        putExtra(EXTRA_FREQUENCY, streak.frequency.name)
        putExtra(EXTRA_NOTIFICATION_TYPE, streak.notificationType.name)
        putExtra(STREAK_ID, streak.streakId)
        putExtra(STREAK_COLOR , streak.streakColor)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        streak.streakId,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                Toast.makeText(context, "Please allow exact alarms in settings.", Toast.LENGTH_LONG).show()
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to schedule alarm: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
