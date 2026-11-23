package com.example.streaks.View.HomeScreens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.attemptDurationValue
import com.example.streaks.Model.currentDurationValue
import com.example.streaks.Model.elapsedParts
import com.example.streaks.Model.personalBestValue
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import com.example.streaks.ui.theme.onAccent
import com.example.streaks.ui.theme.readableAccent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class StreakDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_STREAK_ID, intent.getIntExtra("streakId", -1))
        setContent {
            val vm: StreakViewModel = hiltViewModel()
            StreaksAppTheme(vm) { DetailsRoute(id, vm, ::finish) { target -> startActivity(Intent(this, target).putExtra(EXTRA_STREAK_ID, id)) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsRoute(id: Int, vm: StreakViewModel, onBack: () -> Unit, open: (Class<*>) -> Unit) {
    val goal by vm.observeStreak(id).collectAsState(initial = null)
    val attempts by vm.observeAttempts(id).collectAsState(initial = emptyList())
    val appAccent by vm.appAccent.collectAsState()
    var menu by remember { mutableStateOf(false) }
    var archiveConfirm by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    val current = goal
    val rawAccent = Color((if (current?.usesAppAccent != false) appAccent else current.streakColor).toULong())
    val accent = readableAccent(rawAccent, MaterialTheme.colorScheme.background.luminance() < .5f)
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = accent, onPrimary = onAccent(accent))) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(current?.streakName ?: stringResource(R.string.details_title)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            actions = {
                if (current != null) {
                    TextButton(onClick = { open(EditStreakActivity::class.java) }) { Text(stringResource(R.string.edit)) }
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "More options") }
                    DropdownMenu(menu, { menu = false }) {
                        if (current.archivedAtMillis == null) DropdownMenuItem({ Text(stringResource(R.string.archive_action)) }, { menu = false; archiveConfirm = true })
                        DropdownMenuItem({ Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }, { menu = false; deleteConfirm = true })
                    }
                }
            }
        )
    }) { padding ->
        if (current == null) CircularProgressIndicator(Modifier.padding(padding))
        else DetailsContent(current, attempts, padding, { open(UrgePlanActivity::class.java) }, { open(SetbackActivity::class.java) })
    }
    if (current != null && archiveConfirm) ConfirmDialog(stringResource(R.string.archive_title), stringResource(R.string.archive_explanation), stringResource(R.string.archive_action), { archiveConfirm = false }) { vm.archiveStreak(id); archiveConfirm = false }
    if (current != null && deleteConfirm) ConfirmDialog(
        stringResource(R.string.delete_goal_title),
        "${current.streakName}, its attempts, plan, and reminders will be permanently removed.",
        stringResource(R.string.delete), { deleteConfirm = false }, true
    ) { vm.deleteStreak(current) { onBack() } }
    }
}

@Composable
private fun DetailsContent(goal: StreakModel, attempts: List<AttemptModel>, padding: PaddingValues, onUrge: () -> Unit, onSetback: () -> Unit) {
    val now by produceState(System.currentTimeMillis(), goal.streakId, goal.archivedAtMillis) { while (true) { value = System.currentTimeMillis(); delay(60_000) } }
    val current = elapsedParts(0, currentDurationValue(goal, now).millis)
    val best = elapsedParts(0, personalBestValue(goal, attempts, now).millis)
    val archived = goal.archivedAtMillis != null
    val endDate = goal.archivedAtMillis?.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() } ?: goal.endDate
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (archived) stringResource(R.string.archived) else stringResource(R.string.current_streak), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(pluralStringResource(R.plurals.streak_days, current.days.toInt(), current.days), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    if (archived && endDate != null) Text("Ended ${endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.personal_best), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(pluralStringResource(R.plurals.streak_days, best.days.toInt(), best.days), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        if (goal.whyQuitting.isNotBlank()) item { InfoCard(stringResource(R.string.my_reason), goal.whyQuitting) }
        item { InfoCard(stringResource(R.string.my_urge_plan), goal.urgeAction.ifBlank { stringResource(R.string.default_urge_action) }) }
        if (!archived) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onUrge, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.urge_plan_action)) }
                OutlinedButton(onClick = onSetback, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.setback_button)) }
            }
        }
        item { Text(stringResource(R.string.attempt_history), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (attempts.isEmpty()) item { Text(if (archived) "No previous attempts." else stringResource(R.string.no_previous_attempts), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        itemsIndexed(attempts, key = { _, it -> it.attemptId }) { index, attempt -> AttemptRow(index + 1, attempt) }
    }
}

@Composable private fun InfoCard(title: String, body: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(body) } } }

@Composable
private fun AttemptRow(number: Int, attempt: AttemptModel) {
    val duration = elapsedParts(0, attemptDurationValue(attempt).millis)
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val ended = Instant.ofEpochMilli(attempt.endedAtMillis).atZone(ZoneId.systemDefault()).format(formatter)
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(stringResource(R.string.previous_attempt_number, number), fontWeight = FontWeight.SemiBold)
        Text(pluralStringResource(R.plurals.streak_days, duration.days.toInt(), duration.days))
        Text("Ended $ended", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (attempt.note.isNotBlank()) Text(attempt.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}

@Composable
private fun ConfirmDialog(title: String, text: String, confirm: String, onDismiss: () -> Unit, destructive: Boolean = false, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
