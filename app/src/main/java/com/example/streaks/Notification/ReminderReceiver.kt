package com.example.streaks.Notification

import android.Manifest
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.Model.ReminderDestination
import com.example.streaks.Model.ReminderOutcome
import com.example.streaks.Model.Status
import com.example.streaks.Model.StreakRepository
import com.example.streaks.R
import com.example.streaks.View.HomeScreens.HomeScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

const val CHANNEL = "private_reminders"

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var streakRepository: StreakRepository
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        val reminderId = intent?.getIntExtra(EXTRA_REMINDER_ID, -1) ?: -1
        val legacyStreakId = intent?.getIntExtra(EXTRA_STREAK_ID, -1) ?: -1
        val scheduledFor = intent?.getLongExtra(EXTRA_SCHEDULED_FOR, 0L)?.takeIf { it > 0 }
            ?: System.currentTimeMillis()
        if (reminderId < 0 && legacyStreakId < 0) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminder = reminderId.takeIf { it >= 0 }?.let { streakRepository.getReminder(it) }
                val streakId = reminder?.streakId ?: legacyStreakId
                val streak = streakRepository.getStreakById(streakId)
                val eligible = streak != null && streak.archivedAtMillis == null &&
                    (reminder?.enabled ?: (streak.reminderTime != null))
                if (!eligible) {
                    recordOutcome(
                        reminderId = reminderId.takeIf { it >= 0 } ?: -streakId,
                        streakId = streakId,
                        scheduledFor = scheduledFor,
                        outcome = ReminderOutcome.SKIPPED
                    )
                    if (reminderId >= 0) scheduler.cancelReminder(reminderId) else scheduler.cancel(streakId)
                    return@launch
                }
                NotificationChannels.ensure(context)
                val channelId = reminder?.let { NotificationChannels.idFor(it.sound, it.vibration) } ?: CHANNEL
                val manager = NotificationManagerCompat.from(context)
                val hasRuntimePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                val appNotificationsEnabled = manager.areNotificationsEnabled()
                val channelEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                    context.getSystemService(NotificationManager::class.java).getNotificationChannel(channelId)?.importance != NotificationManager.IMPORTANCE_NONE
                val occurrenceReminderId = reminderId.takeIf { it >= 0 } ?: -streakId
                when {
                    !hasRuntimePermission || !appNotificationsEnabled -> recordOutcome(
                        occurrenceReminderId, streakId, scheduledFor, ReminderOutcome.BLOCKED_PERMISSION
                    )
                    !channelEnabled -> recordOutcome(
                        occurrenceReminderId, streakId, scheduledFor, ReminderOutcome.BLOCKED_CHANNEL
                    )
                    else -> {
                    val open = Intent(context, HomeScreenActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_STREAK_ID, streakId)
                        putExtra(EXTRA_OPEN_URGE_PLAN, reminder?.destination != ReminderDestination.GOAL_DETAILS)
                    }
                    val requestCode = if (reminderId >= 0) reminderId else streakId
                    val contentIntent = PendingIntent.getActivity(context, requestCode, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(reminder?.message?.ifBlank { null } ?: context.getString(R.string.reminder_title))
                        .setContentText(context.getString(R.string.reminder_text))
                        .setContentIntent(contentIntent).setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .build()
                    try {
                        manager.notify(requestCode, notification)
                        recordOutcome(occurrenceReminderId, streakId, scheduledFor, ReminderOutcome.POSTED)
                    } catch (_: SecurityException) {
                        recordOutcome(occurrenceReminderId, streakId, scheduledFor, ReminderOutcome.BLOCKED_PERMISSION)
                    }
                    }
                }
                if (reminder != null) {
                    streakRepository.getReminder(reminder.reminderId)?.takeIf { it.enabled }?.let(scheduler::schedule)
                } else {
                    streakRepository.getStreakById(streakId)?.let(scheduler::schedule)
                }
            } finally { pendingResult.finish() }
        }
    }

    private suspend fun recordOutcome(
        reminderId: Int,
        streakId: Int,
        scheduledFor: Long,
        outcome: ReminderOutcome
    ) {
        notificationRepository.addNotification(
            NotificationModel(
                streakId = streakId,
                streakName = "",
                message = "",
                status = Status.OnGoing,
                frequency = Frequency.DAILY,
                reminderId = reminderId,
                scheduledForMillis = scheduledFor,
                outcome = outcome
            )
        )
    }
}
