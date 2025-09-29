package com.example.streaks.View.HomeScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.View.SettingsScreens.ThemeMode
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class StreakDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streakId = intent.getIntExtra("streakId", -1)

        if (streakId == -1) {
            finish()
            return
        }
        setContent{
            val viewModel: StreakViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val systemUiController = rememberSystemUiController()
            systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = !isDarkTheme)


            StreaksTheme(darkTheme = isDarkTheme) {
                StreakDetailScreen(streakId)

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun StreakDetailScreen(
    streakId: Int,
    viewModel: StreakViewModel = hiltViewModel(),
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }



    val activity: Activity = LocalContext.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var streak by remember { mutableStateOf<StreakModel?>(null) }

    // Load the streak
    LaunchedEffect(streakId) {
        viewModel.getStreakById(streakId) {
            streak = it
        }
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getStreakById(streakId) {
                    streak = it
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current

    // Delete confirm dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this streak?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStreak(streak!!)
                    viewModel.cancelAlarm(streakId, context)
                    activity.finish()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // End Streak confirm dialog
    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End Streak") },
            text = { Text("Are you sure you want to end this streak immediately?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.endStreak(streakId)
                    viewModel.cancelAlarm(streakId, context
                    )
                    showEndDialog = false
                    activity.finish()
                }) {
                    Text("End", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Details") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showDeleteDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
            )
        }
    ) { padding ->

        streak?.let { s ->


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            ) {
                // Title & Color
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(s.streakColor.toULong()))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        s.streakName,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Frequency
                Text(
                    "Frequency: ${s.frequency.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Start date
                Text(
                    "Start Date: ${s.startDate}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // End date
                Text(
                    "End Date: ${s.endDate}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current count
                Text(
                    "Current Count: ${viewModel.calculateStreakCount(s)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time left info
                Text(
                    viewModel.nextCount(s),
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                // Reminder
                Text(
                    "Reminder Time Selected: ${s.reminderTime}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Edit Button
                Button(
                    onClick = {
                        val intent = Intent(context, EditStreakActivity::class.java)
                        intent.putExtra("streakId", s.streakId)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(s.streakColor.toULong()))
                ) {
                    Text("Edit Streak", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // End Streak Button
                val hasEnded = s.endDate?.let { end ->
                    !end.isAfter(LocalDate.now())
                } ?: false
                Button(
                    onClick = { showEndDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !hasEnded
                ) {
                    Text(if (hasEnded) "Streak Ended" else "End Streak",
                        color = Color.White)
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


