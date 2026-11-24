package com.example.streaks.View.NotificationScreens

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.streaks.Model.ReminderDestination
import com.example.streaks.Model.ReminderOutcome
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Notification.EXTRA_REMINDER_ID
import com.example.streaks.Notification.NotificationChannels
import com.example.streaks.Notification.nextReminderAtMillis
import com.example.streaks.ViewModel.StreakViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class DeliveryState { READY, INEXACT, BLOCKED_NOTIFICATIONS, BLOCKED_CHANNEL }

@Composable
fun NotificationScreen(paddingValues: PaddingValues, viewModel: StreakViewModel) {
    val reminders by viewModel.reminderSchedules.collectAsState()
    val history by androidx.hilt.navigation.compose.hiltViewModel<com.example.streaks.ViewModel.NotificationViewModel>().notifications.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showHistory by remember { mutableStateOf(false) }
    var statusRefresh by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastEditorLaunchAt by remember { mutableLongStateOf(0L) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) statusRefresh = System.currentTimeMillis()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    fun open(id: Int = 0) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastEditorLaunchAt < 750L) return
        lastEditorLaunchAt = now
        context.startActivity(Intent(context, ReminderEditorActivity::class.java).putExtra(EXTRA_REMINDER_ID, id))
    }
    val now = statusRefresh
    val ordered = reminders.sortedWith(compareBy<ReminderSchedule> { !it.enabled }.thenBy {
        if (it.enabled) nextReminderAtMillis(it.time, it.repeatDays, now, ZoneId.systemDefault()) else Long.MAX_VALUE
    })
    LazyColumn(
        Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ordered.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("No reminders scheduled", style = MaterialTheme.typography.titleLarge)
                    Text("Add a discreet reminder for a moment when support may help.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { open() }) { Icon(Icons.Default.Add, null); Text("Add reminder") }
                }
            }
        } else {
            item { Button(onClick = { open() }) { Icon(Icons.Default.Add, null); Text("Add reminder") } }
            items(ordered, key = { it.reminderId }) { reminder ->
                ReminderRow(
                    reminder = reminder,
                    now = now,
                    state = deliveryState(context, reminder),
                    onOpen = { open(reminder.reminderId) },
                    onEnabled = { viewModel.setReminderEnabled(reminder, it) },
                    onFix = { openDeliverySettings(context, reminder, deliveryState(context, reminder)) }
                )
            }
        }
        item {
            TextButton(onClick = { showHistory = !showHistory }) {
                Text(if (showHistory) "Hide reminder history" else "Reminder history (${history.size})")
            }
        }
        if (showHistory) {
            items(history, key = { "history-${it.id}" }) { item ->
                val label = when (item.outcome) {
                    ReminderOutcome.POSTED -> "Reminder posted"
                    ReminderOutcome.BLOCKED_PERMISSION -> "Reminder blocked — notifications are off"
                    ReminderOutcome.BLOCKED_CHANNEL -> "Reminder blocked — reminder channel is off"
                    ReminderOutcome.SKIPPED -> "Reminder skipped — schedule changed"
                }
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, fontWeight = FontWeight.Medium, color = if (item.outcome == ReminderOutcome.POSTED) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                        Text(
                            Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm")),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.scheduledForMillis > 0 && kotlin.math.abs(item.timestamp - item.scheduledForMillis) > 60_000) {
                            Text(
                                "Scheduled for ${Instant.ofEpochMilli(item.scheduledForMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM · HH:mm"))}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: ReminderSchedule,
    now: Long,
    state: DeliveryState,
    onOpen: () -> Unit,
    onEnabled: (Boolean) -> Unit,
    onFix: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val nextFormatter = DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a")
    val next = if (reminder.enabled && reminder.repeatDays != 0) {
        Instant.ofEpochMilli(nextReminderAtMillis(reminder.time, reminder.repeatDays, now, ZoneId.systemDefault()))
            .atZone(ZoneId.systemDefault()).format(nextFormatter)
    } else null
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reminder.time.format(timeFormatter), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(repeatSummary(reminder.repeatDays), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (reminder.destination == ReminderDestination.URGE_PLAN) "Opens urge plan" else "Opens goal details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                when {
                    !reminder.enabled -> Text("Paused", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    next != null -> Text("Next: $next", fontWeight = FontWeight.Medium)
                }
                if (reminder.enabled) {
                    when (state) {
                        DeliveryState.READY -> Text("Ready — exact alarm scheduled", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        DeliveryState.INEXACT -> Text("Android may delay this reminder", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        DeliveryState.BLOCKED_NOTIFICATIONS -> Text("Blocked — notifications are off", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        DeliveryState.BLOCKED_CHANNEL -> Text("Blocked — this reminder channel is off", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (state != DeliveryState.READY) {
                        TextButton(onClick = onFix, contentPadding = PaddingValues(0.dp)) {
                            Text(if (state == DeliveryState.INEXACT) "Allow exact timing" else "Fix notification settings")
                        }
                    }
                }
            }
            Switch(checked = reminder.enabled, onCheckedChange = onEnabled)
        }
    }
}

private fun deliveryState(context: Context, reminder: ReminderSchedule): DeliveryState {
    NotificationChannels.ensure(context)
    val runtimeGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    if (!runtimeGranted || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return DeliveryState.BLOCKED_NOTIFICATIONS
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = NotificationChannels.idFor(reminder.sound, reminder.vibration)
        val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(channelId)
        if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return DeliveryState.BLOCKED_CHANNEL
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
        return DeliveryState.INEXACT
    }
    return DeliveryState.READY
}

private fun openDeliverySettings(context: Context, reminder: ReminderSchedule, state: DeliveryState) {
    val intent = when (state) {
        DeliveryState.INEXACT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }
        DeliveryState.BLOCKED_CHANNEL -> Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.idFor(reminder.sound, reminder.vibration))
        }
        else -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }
    context.startActivity(intent)
}

fun repeatSummary(mask: Int): String {
    if (mask == 0b1111111) return "Every day"
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return labels.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.joinToString(", ").ifBlank { "No repeat days" }
}
