package com.example.streaks.ViewModel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.DataBase.NotificationRepository
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.Notification.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notificationHistory: StateFlow<List<NotificationModel>> =
        repository.getAllHistory().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun insert(notification: NotificationModel) {
        viewModelScope.launch {
            repository.insert(notification)
        }
    }

    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
        }
    }

  fun delete(notification: NotificationModel) {
        viewModelScope.launch {
            repository.delete(notification)
        }
    }

    fun deleteById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    @SuppressLint("ServiceCast")
    fun scheduleAlarm(
        context: Context,
        streak: StreakModel,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("STREAK_ID", streak.streakId)
            putExtra("STREAK_NAME", streak.streakName)
            putExtra("NOTIFICATION_TYPE", streak.notificationType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            streak.streakId, // unique per streak
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    Toast.makeText(
                        context,
                        "Please allow exact alarms in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to schedule alarm: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
