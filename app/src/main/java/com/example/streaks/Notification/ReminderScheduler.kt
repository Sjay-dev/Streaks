package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.StreakModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

const val EXTRA_STREAK_ID = "streak_id"
const val EXTRA_REMINDER_ID = "reminder_id"
const val EXTRA_OPEN_URGE_PLAN = "open_urge_plan"
const val EXTRA_SCHEDULED_FOR = "scheduled_for"

@Singleton
class ReminderScheduler @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Compatibility path for v2 embedded reminders until all installs migrate. */
    fun schedule(streak: StreakModel, nowMillis: Long = System.currentTimeMillis()) {
        val time = streak.reminderTime ?: return cancel(streak.streakId)
        if (streak.archivedAtMillis != null) return cancel(streak.streakId)
        val trigger = nextReminderAtMillis(time, nowMillis, ZoneId.systemDefault())
        scheduleAlarm(streak.streakId, trigger, legacyPending(streak.streakId, trigger))
    }

    fun schedule(reminder: ReminderSchedule, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (!reminder.enabled || reminder.repeatDays == 0) {
            cancelReminder(reminder.reminderId)
            return null
        }
        val trigger = nextReminderAtMillis(reminder.time, reminder.repeatDays, nowMillis, ZoneId.systemDefault())
        scheduleAlarm(reminder.reminderId, trigger, reminderPending(reminder.reminderId, trigger))
        return trigger
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(id: Int, triggerAt: Long, pending: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(streakId: Int) {
        legacyPending(streakId).also { alarmManager.cancel(it); it.cancel() }
    }

    fun cancelReminder(reminderId: Int) {
        reminderPending(reminderId, 0L).also { alarmManager.cancel(it); it.cancel() }
    }

    fun cancelAll(streaks: Iterable<StreakModel>) = streaks.forEach { cancel(it.streakId) }

    private fun legacyPending(streakId: Int, scheduledFor: Long = 0L) = PendingIntent.getBroadcast(
        context, -streakId,
        Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_STREAK_ID, streakId)
            .putExtra(EXTRA_SCHEDULED_FOR, scheduledFor),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun reminderPending(reminderId: Int, scheduledFor: Long) = PendingIntent.getBroadcast(
        context, reminderId,
        Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_REMINDER_ID, reminderId)
            .putExtra(EXTRA_SCHEDULED_FOR, scheduledFor),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun nextReminderAtMillis(time: LocalTime, nowMillis: Long, zone: ZoneId): Long =
    nextReminderAtMillis(time, 0b1111111, nowMillis, zone)

fun nextReminderAtMillis(time: LocalTime, repeatDays: Int, nowMillis: Long, zone: ZoneId): Long {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDateTime()
    for (offset in 0..7) {
        val date = now.toLocalDate().plusDays(offset.toLong())
        val bit = date.dayOfWeek.value % 7 // Sunday = zero
        val candidate = LocalDateTime.of(date, time)
        if ((repeatDays and (1 shl bit)) != 0 && candidate.isAfter(now)) {
            return candidate.atZone(zone).toInstant().toEpochMilli()
        }
    }
    return LocalDateTime.of(now.toLocalDate().plusDays(1), time).atZone(zone).toInstant().toEpochMilli()
}
