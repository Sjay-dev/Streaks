package com.example.streaks.View.HomeScreens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.currentDurationValue
import com.example.streaks.Notification.EXTRA_OPEN_URGE_PLAN
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.Notification.NotificationChannels
import com.example.streaks.R
import com.example.streaks.View.NotificationScreens.NotificationScreen
import com.example.streaks.View.SettingsScreens.SettingsScreen
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import com.example.streaks.ui.theme.readableAccent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.ensure(this)
        openDeepLink(intent)
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            StreaksAppTheme(viewModel) { HomeScaffold(viewModel) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openDeepLink(intent)
    }

    private fun openDeepLink(source: Intent) {
        source.getIntExtra(EXTRA_STREAK_ID, -1).takeIf { it > 0 }?.let { id ->
            val destination = if (source.getBooleanExtra(EXTRA_OPEN_URGE_PLAN, false)) {
                UrgePlanActivity::class.java
            } else {
                StreakDetailsActivity::class.java
            }
            startActivity(Intent(this, destination).putExtra(EXTRA_STREAK_ID, id))
            source.removeExtra(EXTRA_STREAK_ID)
        }
    }
}

private enum class BulkConfirmation { ARCHIVE, DELETE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(viewModel: StreakViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selectionActive by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<Int>()) }
    var confirmation by remember { mutableStateOf<BulkConfirmation?>(null) }
    var processing by remember { mutableStateOf(false) }
    val goals by viewModel.streaks.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val context = LocalContext.current
    val greeting = rememberHomeGreeting(displayName)
    val selectedGoals = goals.filter { it.streakId in selectedIds }
    val activeSelected = selectedGoals.count { it.archivedAtMillis == null }
    val archivedSelected = selectedGoals.size - activeSelected

    fun closeSelection() {
        selectionActive = false
        selectedIds = emptySet()
        confirmation = null
    }

    BackHandler(selectionActive) { closeSelection() }

    confirmation?.let { type ->
        val deleting = type == BulkConfirmation.DELETE
        AlertDialog(
            onDismissRequest = { if (!processing) confirmation = null },
            title = { Text(if (deleting) "Delete ${selectedIds.size} goals?" else "Archive ${selectedIds.size} goals?") },
            text = {
                Text(
                    if (deleting) {
                        "This permanently deletes the selected goals, their attempts, reflections, reminder history, and scheduled reminders."
                    } else {
                        "$activeSelected active goal${if (activeSelected == 1) "" else "s"} will end now. " +
                            "$archivedSelected already archived goal${if (archivedSelected == 1) "" else "s"} will keep their existing end dates. Scheduled reminders are cancelled."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !processing && (deleting || activeSelected > 0),
                    onClick = {
                        processing = true
                        val done = { processing = false; closeSelection() }
                        if (deleting) viewModel.deleteStreaks(selectedIds, done)
                        else viewModel.archiveStreaks(selectedIds, done)
                    }
                ) {
                    Text(if (processing) "Working…" else if (deleting) "Delete permanently" else "Archive", color = if (deleting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = { TextButton(enabled = !processing, onClick = { confirmation = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            tab == 0 && selectionActive -> "${selectedIds.size} selected"
                            tab == 0 -> greeting
                            tab == 1 -> stringResource(R.string.reminders_title)
                            else -> stringResource(R.string.settings_title)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    if (tab == 0 && selectionActive) {
                        TextButton(enabled = selectedIds.isNotEmpty() && activeSelected > 0 && !processing, onClick = { confirmation = BulkConfirmation.ARCHIVE }) { Text("Archive") }
                        TextButton(enabled = selectedIds.isNotEmpty() && !processing, onClick = { confirmation = BulkConfirmation.DELETE }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                        TextButton(enabled = !processing, onClick = { closeSelection() }) { Text("Close") }
                    } else if (tab == 0 && goals.isNotEmpty()) {
                        TextButton(onClick = { selectionActive = true }) { Text("Select") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (tab == 0 && !selectionActive) {
                ExtendedFloatingActionButton(
                    onClick = { context.startActivity(Intent(context, CreateStreakActivity::class.java)) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.new_goal)) }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = .14f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationBarItem(tab == 0, { tab = 0; closeSelection() }, { Icon(Icons.Default.Home, stringResource(R.string.home_title)) }, label = { BottomLabel(stringResource(R.string.home_title)) }, colors = itemColors)
                NavigationBarItem(tab == 1, { tab = 1; closeSelection() }, { Icon(Icons.Default.Notifications, stringResource(R.string.reminders_title)) }, label = { BottomLabel(stringResource(R.string.reminders_title)) }, colors = itemColors)
                NavigationBarItem(tab == 2, { tab = 2; closeSelection() }, { Icon(Icons.Default.Settings, stringResource(R.string.settings_title)) }, label = { BottomLabel(stringResource(R.string.settings_title)) }, colors = itemColors)
            }
        }
    ) { padding ->
        when (tab) {
            0 -> GoalsScreen(
                padding = padding,
                viewModel = viewModel,
                selectionActive = selectionActive,
                selectedIds = selectedIds,
                onSelectionChange = { id, selected ->
                    selectedIds = if (selected) selectedIds + id else selectedIds - id
                    selectionActive = selectedIds.isNotEmpty() || (selectionActive && selected)
                    if (selectedIds.isEmpty() && !selected) selectionActive = false
                },
                onLongSelect = { id -> selectionActive = true; selectedIds = selectedIds + id }
            )
            1 -> NotificationScreen(padding, viewModel)
            else -> SettingsScreen(padding, viewModel)
        }
    }
}

@Composable
private fun rememberHomeGreeting(displayName: String): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) now = System.currentTimeMillis()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { now = System.currentTimeMillis() }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.unregisterReceiver(receiver)
        }
    }
    LaunchedEffect(now, zone) {
        val boundary = nextGreetingBoundaryMillis(ZonedDateTime.now(zone))
        delay((boundary - System.currentTimeMillis()).coerceAtLeast(1L) + 50L)
        now = System.currentTimeMillis()
    }
    return greetingAt(now, zone, displayName)
}

@Composable
private fun BottomLabel(label: String) = Text(label, maxLines = 1, fontSize = 11.sp)

@Composable
private fun GoalsScreen(
    padding: PaddingValues,
    viewModel: StreakViewModel,
    selectionActive: Boolean,
    selectedIds: Set<Int>,
    onSelectionChange: (Int, Boolean) -> Unit,
    onLongSelect: (Int) -> Unit
) {
    val goals by viewModel.streaks.collectAsState()
    val appAccent by viewModel.appAccent.collectAsState()
    val context = LocalContext.current
    if (goals.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(padding).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals, key = { it.streakId }) { goal ->
            val selected = goal.streakId in selectedIds
            GoalCard(
                goal = goal,
                accent = if (goal.usesAppAccent) Color(appAccent.toULong()) else Color(goal.streakColor.toULong()),
                selected = selected,
                selectionActive = selectionActive,
                onOpen = {
                    if (selectionActive) onSelectionChange(goal.streakId, !selected)
                    else context.startActivity(Intent(context, StreakDetailsActivity::class.java).putExtra(EXTRA_STREAK_ID, goal.streakId))
                },
                onLongSelect = { onLongSelect(goal.streakId) },
                onUrge = {
                    if (selectionActive) onSelectionChange(goal.streakId, !selected)
                    else context.startActivity(Intent(context, UrgePlanActivity::class.java).putExtra(EXTRA_STREAK_ID, goal.streakId))
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoalCard(
    goal: StreakModel,
    accent: Color,
    selected: Boolean,
    selectionActive: Boolean,
    onOpen: () -> Unit,
    onLongSelect: () -> Unit,
    onUrge: () -> Unit
) {
    val actionAccent = readableAccent(accent, MaterialTheme.colorScheme.background.luminance() < .5f)
    val archived = goal.archivedAtMillis != null
    val days = currentDurationValue(goal, System.currentTimeMillis()).millis / 86_400_000
    val zone = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val since = goal.currentAttemptStartedAtMillis.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: goal.startDate
    val end = goal.archivedAtMillis?.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: goal.endDate
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = "${goal.streakName}, $days days${if (archived) ", archived" else ""}"
                customActions = listOf(CustomAccessibilityAction(if (selected) "Deselect goal" else "Select goal") {
                    if (selected) onOpen() else onLongSelect()
                    true
                })
            }
            .combinedClickable(onClick = onOpen, onLongClick = onLongSelect),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (selected) 4.dp else 2.dp, actionAccent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(goal.streakName, color = actionAccent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (archived && end != null) "Ended ${end.format(formatter)}" else "Since ${since.format(formatter)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!archived) {
                    TextButton(
                        onClick = onUrge,
                        modifier = Modifier.heightIn(min = 36.dp).semantics { contentDescription = "Open ${goal.streakName} urge plan" },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) { Text(stringResource(R.string.urge_plan_action), color = actionAccent) }
                } else {
                    Text(stringResource(R.string.archived), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
            }
            Box(
                Modifier.size(76.dp).border(3.dp, actionAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, "Selected", tint = actionAccent, modifier = Modifier.size(36.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(days.toString(), color = actionAccent, fontWeight = FontWeight.Bold, fontSize = if (days > 999) 17.sp else 25.sp, maxLines = 1)
                        Text("days", color = actionAccent, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
