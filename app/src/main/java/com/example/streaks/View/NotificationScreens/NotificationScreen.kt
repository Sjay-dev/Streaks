package com.example.streaks.View.NotificationScreens

import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.streaks.Model.ReminderDestination
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Notification.EXTRA_REMINDER_ID
import com.example.streaks.Notification.nextReminderAtMillis
import com.example.streaks.ViewModel.StreakViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationScreen(paddingValues: PaddingValues, viewModel: StreakViewModel) {
    val reminders by viewModel.reminderSchedules.collectAsState()
    val history by androidx.hilt.navigation.compose.hiltViewModel<com.example.streaks.ViewModel.NotificationViewModel>().notifications.collectAsState()
    val context = LocalContext.current
    var showHistory by remember { mutableStateOf(false) }
    fun open(id: Int = 0) { context.startActivity(Intent(context, ReminderEditorActivity::class.java).putExtra(EXTRA_REMINDER_ID, id)) }
    val now = System.currentTimeMillis()
    val ordered = reminders.sortedWith(compareBy<ReminderSchedule> { !it.enabled }.thenBy {
        if (it.enabled) nextReminderAtMillis(it.time, it.repeatDays, now, ZoneId.systemDefault()) else Long.MAX_VALUE
    })
    LazyColumn(
        Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ordered.isEmpty()) item {
            Column(Modifier.fillMaxWidth().padding(vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("No reminders scheduled", style = MaterialTheme.typography.titleLarge)
                Text("Add a discreet reminder for a moment when support may help.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { open() }) { Icon(Icons.Default.Add, null); Text("Add reminder") }
            }
        } else {
            item { Button(onClick = { open() }) { Icon(Icons.Default.Add, null); Text("Add reminder") } }
            items(ordered, key = { it.reminderId }) { reminder ->
                ReminderRow(reminder, { open(reminder.reminderId) }) { viewModel.setReminderEnabled(reminder, it) }
            }
        }
        item {
            TextButton(onClick = { showHistory = !showHistory }) { Text(if (showHistory) "Hide reminder history" else "Reminder history (${history.size})") }
        }
        if (showHistory) items(history, key = { "history-${it.id}" }) { item ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f))) {
                Column(Modifier.padding(14.dp)) {
                    Text("Reminder delivered", fontWeight = FontWeight.Medium)
                    Text(Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm")), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(reminder: ReminderSchedule, onOpen: () -> Unit, onEnabled: (Boolean) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(reminder.time.format(formatter), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(repeatSummary(reminder.repeatDays), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (reminder.destination == ReminderDestination.URGE_PLAN) "Opens urge plan" else "Opens goal details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!reminder.enabled) Text("Paused", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            Switch(reminder.enabled, onEnabled, modifier = Modifier)
        }
    }
}

fun repeatSummary(mask: Int): String {
    if (mask == 0b1111111) return "Every day"
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return labels.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.joinToString(", ").ifBlank { "No repeat days" }
}
