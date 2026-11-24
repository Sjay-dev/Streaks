package com.example.streaks.View.HomeScreens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.ArchiveAction
import com.example.streaks.Model.archiveActionFor
import com.example.streaks.Model.archiveTargetsFor
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            StreaksAppTheme(viewModel) { ArchiveScreen(viewModel, ::finish) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveScreen(viewModel: StreakViewModel, onBack: () -> Unit) {
    val allGoals by viewModel.streaks.collectAsState()
    val appAccent by viewModel.appAccent.collectAsState()
    val archivedGoals = allGoals.filter { it.archivedAtMillis != null }
    var selectedIds by remember { mutableStateOf(emptySet<Int>()) }
    var processing by remember { mutableStateOf(false) }
    val selectedGoals = allGoals.filter { it.streakId in selectedIds }
    val action = archiveActionFor(selectedGoals)
    val context = LocalContext.current

    fun closeSelection() { selectedIds = emptySet() }

    LaunchedEffect(archivedGoals.map { it.streakId }) {
        selectedIds = selectedIds.intersect(archivedGoals.mapTo(mutableSetOf()) { it.streakId })
    }
    BackHandler(selectedIds.isNotEmpty()) { closeSelection() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedIds.isEmpty()) "Archive" else "${selectedIds.size} selected",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (selectedIds.isNotEmpty()) closeSelection() else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, if (selectedIds.isEmpty()) "Back" else "Close selection")
                    }
                },
                actions = {
                    if (action != null) {
                        val label = if (action == ArchiveAction.ARCHIVE) "Archive" else "Unarchive"
                        TextButton(
                            enabled = !processing && archiveTargetsFor(selectedGoals, action).isNotEmpty(),
                            onClick = {
                                processing = true
                                viewModel.applyArchiveAction(selectedGoals) { completedAction, changed ->
                                    processing = false
                                    closeSelection()
                                    val verb = if (completedAction == ArchiveAction.ARCHIVE) "archived" else "restored"
                                    Toast.makeText(context, "$changed streak${if (changed == 1) "" else "s"} $verb.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                painterResource(if (action == ArchiveAction.ARCHIVE) R.drawable.ic_archive_24 else R.drawable.ic_unarchive_24),
                                contentDescription = "$label selected streaks"
                            )
                            Text(label)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (archivedGoals.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No archived streaks", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Streaks you archive will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(archivedGoals, key = { it.streakId }) { goal ->
                    val selected = goal.streakId in selectedIds
                    GoalCard(
                        goal = goal,
                        accent = if (goal.usesAppAccent) Color(appAccent.toULong()) else Color(goal.streakColor.toULong()),
                        selected = selected,
                        selectionActive = selectedIds.isNotEmpty(),
                        onOpen = {
                            if (selectedIds.isNotEmpty()) {
                                selectedIds = if (selected) selectedIds - goal.streakId else selectedIds + goal.streakId
                            } else {
                                context.startActivity(Intent(context, StreakDetailsActivity::class.java).putExtra(EXTRA_STREAK_ID, goal.streakId))
                            }
                        },
                        onLongSelect = { selectedIds = selectedIds + goal.streakId },
                        onUrge = {}
                    )
                }
            }
        }
    }
}
