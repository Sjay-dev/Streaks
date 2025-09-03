package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {
    private fun pendingIntentFor(context: Context, streakId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("streakId", streakId)
        }
        return PendingIntent.getBroadcast(
            context, streakId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("ServiceCast")
    fun scheduleNext(context: Context, streak: StreakModel) {
        val time = streak.reminderTime ?: return
        val trigger = computeNextTriggerMillis(streak.frequency, time)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, trigger, pendingIntentFor(context, streak.streakId)
        )
    }

    fun cancel(context: Context, streakId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntentFor(context, streakId))
    }
}

fun computeNextTriggerMillis(frequency: Frequency, reminderTime: LocalTime): Long {
    val now = LocalDateTime.now()
    var candidate = now.withHour(reminderTime.hour).withMinute(reminderTime.minute).withSecond(0).withNano(0)

    if (candidate.isBefore(now)) {
        candidate = when (frequency) {
            Frequency.DAILY -> candidate.plusDays(1)
            Frequency.WEEKLY -> candidate.plusWeeks(1)
            Frequency.MONTHLY -> candidate.plusMonths(1)
        }
    }
    return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

