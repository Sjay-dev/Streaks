package com.example.streaks.View.NotificationScreens

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.ReminderDestination
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.ReminderSound
import com.example.streaks.Notification.EXTRA_REMINDER_ID
import com.example.streaks.Notification.NotificationChannels
import com.example.streaks.R
import com.example.streaks.View.Components.InlineTimeWheel
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import com.example.streaks.ui.theme.onAccent
import com.example.streaks.ui.theme.readableAccent
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ReminderEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)
        setContent { val vm: StreakViewModel = hiltViewModel(); StreaksAppTheme(vm) { ReminderEditor(reminderId, vm, ::finish) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderEditor(reminderId: Int, vm: StreakViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val goals by vm.streaks.collectAsState()
    val appAccent by vm.appAccent.collectAsState()
    var loaded by remember { mutableStateOf(reminderId == 0) }
    var draft by remember { mutableStateOf<ReminderSchedule?>(null) }
    LaunchedEffect(reminderId, goals) {
        if (reminderId > 0 && draft == null) vm.getReminder(reminderId) { draft = it; loaded = true }
        else if (reminderId == 0 && draft == null) goals.firstOrNull { it.archivedAtMillis == null }?.let { draft = ReminderSchedule(streakId = it.streakId) }
    }
    if (!loaded || (draft == null && goals.isNotEmpty())) return CircularProgressIndicator()
    val reminder = draft
    if (reminder == null) {
        Scaffold(topBar = { TopAppBar(title = { Text("Add reminder") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
            Text("Create an active goal before adding a reminder.", Modifier.padding(padding).padding(24.dp))
        }
        return
    }
    val selectedGoal = goals.firstOrNull { it.streakId == reminder.streakId }
    val rawAccent = Color((if (selectedGoal?.usesAppAccent != false) appAccent else selectedGoal.streakColor).toULong())
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val accent = readableAccent(rawAccent, dark)
    var goalMenu by remember { mutableStateOf(false) }
    var soundMenu by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(true) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    DisposableEffect(Unit) { onDispose { player?.stop(); player?.release() } }

    fun preview(sound: ReminderSound) {
        player?.stop(); player?.release(); player = null
        val created = when (sound) {
            ReminderSound.SOFT_CHIME -> MediaPlayer.create(context, R.raw.soft_chime)
            ReminderSound.BELL -> MediaPlayer.create(context, R.raw.bell)
            ReminderSound.GENTLE_PULSE -> MediaPlayer.create(context, R.raw.gentle_pulse)
            ReminderSound.PHONE_DEFAULT -> MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI)
            ReminderSound.SILENT -> null
        }
        player = created?.apply { setOnCompletionListener { it.release(); if (player === it) player = null }; start() }
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = accent, onPrimary = onAccent(accent))) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(if (reminderId == 0) "Add reminder" else "Edit reminder") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) },
            bottomBar = { Button(onClick = {
                error = if (reminder.repeatDays == 0) "Choose at least one repeat day." else null
                if (error == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    vm.saveReminder(reminder) { onBack() }
                }
            }, Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) { Text("Save reminder") } }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Goal", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = { goalMenu = true }, Modifier.fillMaxWidth()) { Text(selectedGoal?.streakName ?: "Choose goal", Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null) }
                DropdownMenu(goalMenu, { goalMenu = false }) {
                    goals.filter { it.archivedAtMillis == null }.forEach { goal -> DropdownMenuItem({ Text(goal.streakName) }, { draft = reminder.copy(streakId = goal.streakId); goalMenu = false }) }
                }
                Text("Time", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { timeExpanded = !timeExpanded }) { Text(reminder.time.format(DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a")), style = MaterialTheme.typography.headlineSmall) }
                if (timeExpanded) InlineTimeWheel(reminder.time, DateFormat.is24HourFormat(context)) { draft = reminder.copy(time = it) }
                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, label ->
                        val bit = 1 shl index
                        FilterChip(reminder.repeatDays and bit != 0, { draft = reminder.copy(repeatDays = reminder.repeatDays xor bit) }, { Text(label) })
                    }
                }
                Text("On tap", style = MaterialTheme.typography.labelLarge)
                DestinationOption("Open this goal's urge plan", reminder.destination == ReminderDestination.URGE_PLAN) { draft = reminder.copy(destination = ReminderDestination.URGE_PLAN) }
                DestinationOption("Open goal details", reminder.destination == ReminderDestination.GOAL_DETAILS) { draft = reminder.copy(destination = ReminderDestination.GOAL_DETAILS) }
                OutlinedTextField(reminder.message, { draft = reminder.copy(message = it.take(80)) }, Modifier.fillMaxWidth(), label = { Text("Lock-screen message") }, supportingText = { Text("Keep this neutral. Goal names and private notes are never added.") })
                Text("Sound", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = { soundMenu = true }, Modifier.fillMaxWidth()) { Text(soundLabel(reminder.sound), Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null) }
                DropdownMenu(soundMenu, { soundMenu = false }) { ReminderSound.entries.forEach { choice -> DropdownMenuItem({ Text(soundLabel(choice)) }, { player?.stop(); draft = reminder.copy(sound = choice); soundMenu = false }) } }
                OutlinedButton(onClick = { preview(reminder.sound) }, enabled = reminder.sound != ReminderSound.SILENT) { Icon(Icons.Default.PlayArrow, null); Text("Preview sound") }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Vibration", Modifier.weight(1f)); Switch(reminder.vibration, { draft = reminder.copy(vibration = it) }) }
                TextButton(onClick = {
                    NotificationChannels.ensure(context)
                    context.startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.idFor(reminder.sound, reminder.vibration)))
                }) { Text("Open Android notification settings") }
                Text("Android settings and your phone's sound mode control whether this reminder can make sound.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (reminderId > 0) TextButton(onClick = { vm.deleteReminder(reminder); onBack() }) { Text("Delete reminder", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun DestinationOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onSelect); Text(label, Modifier.padding(start = 8.dp)) }
}

private fun soundLabel(sound: ReminderSound) = when (sound) {
    ReminderSound.PHONE_DEFAULT -> "Phone default"
    ReminderSound.SOFT_CHIME -> "Soft chime"
    ReminderSound.BELL -> "Bell"
    ReminderSound.GENTLE_PULSE -> "Gentle pulse"
    ReminderSound.SILENT -> "Silent"
}
