package com.example.streaks.View.HomeScreens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.ensure(this)
        intent.getIntExtra(EXTRA_STREAK_ID, -1).takeIf { it > 0 }?.let { id ->
            val destination = if (intent.getBooleanExtra(EXTRA_OPEN_URGE_PLAN, false)) UrgePlanActivity::class.java else StreakDetailsActivity::class.java
            startActivity(Intent(this, destination).putExtra(EXTRA_STREAK_ID, id))
            intent.removeExtra(EXTRA_STREAK_ID)
        }
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            StreaksAppTheme(viewModel) { HomeScaffold(viewModel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(viewModel: StreakViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val titles = listOf(R.string.home_title, R.string.reminders_title, R.string.settings_title)
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(titles[tab]), fontWeight = FontWeight.SemiBold) }) },
        floatingActionButton = {
            if (tab == 0) ExtendedFloatingActionButton(
                onClick = { context.startActivity(Intent(context, CreateStreakActivity::class.java)) },
                icon = { Icon(Icons.Default.Add, null) }, text = { Text(stringResource(R.string.new_goal)) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.Home, stringResource(R.string.home_title)) }, label = { BottomLabel(stringResource(R.string.home_title)) })
                NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.Notifications, stringResource(R.string.reminders_title)) }, label = { BottomLabel(stringResource(R.string.reminders_title)) })
                NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.Settings, stringResource(R.string.settings_title)) }, label = { BottomLabel(stringResource(R.string.settings_title)) })
            }
        }
    ) { padding ->
        when (tab) {
            0 -> GoalsScreen(padding, viewModel)
            1 -> NotificationScreen(padding, viewModel)
            else -> SettingsScreen(padding, viewModel)
        }
    }
}

@Composable
private fun BottomLabel(label: String) = Text(label, maxLines = 1, fontSize = 10.sp)

@Composable
private fun GoalsScreen(padding: PaddingValues, viewModel: StreakViewModel) {
    val goals by viewModel.streaks.collectAsState()
    val appAccent by viewModel.appAccent.collectAsState()
    val context = LocalContext.current
    if (goals.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(padding).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals, key = { it.streakId }) { goal ->
            GoalCard(
                goal, if (goal.usesAppAccent) Color(appAccent.toULong()) else Color(goal.streakColor.toULong()),
                onOpen = { context.startActivity(Intent(context, StreakDetailsActivity::class.java).putExtra(EXTRA_STREAK_ID, goal.streakId)) },
                onUrge = { context.startActivity(Intent(context, UrgePlanActivity::class.java).putExtra(EXTRA_STREAK_ID, goal.streakId)) }
            )
        }
    }
}

@Composable
private fun GoalCard(goal: StreakModel, accent: Color, onOpen: () -> Unit, onUrge: () -> Unit) {
    val actionAccent = readableAccent(accent, MaterialTheme.colorScheme.background.luminance() < .5f)
    val archived = goal.archivedAtMillis != null
    val days = currentDurationValue(goal, System.currentTimeMillis()).millis / 86_400_000
    val zone = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val since = goal.currentAttemptStartedAtMillis.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: goal.startDate
    val end = goal.archivedAtMillis?.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: goal.endDate
    Card(
        modifier = Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = "${goal.streakName}, $days days" }.clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(10.dp).background(accent, CircleShape))
                Text(goal.streakName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (archived) Text(stringResource(R.string.archived), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(pluralStringResource(R.plurals.streak_days, days.toInt(), days), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(
                if (archived && end != null) "Ended ${end.format(formatter)}" else "Since ${since.format(formatter)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!archived) TextButton(onClick = onUrge, modifier = Modifier.align(Alignment.End).semantics { contentDescription = "Open ${goal.streakName} urge plan" }) {
                Text(stringResource(R.string.urge_plan_action), color = actionAccent)
            }
        }
    }
}
