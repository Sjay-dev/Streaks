package com.example.streaks.View.HomeScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.streaks.Model.StreakModel
import com.example.streaks.ViewModel.StreakViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

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
                    StreakDetailScreen(streakId)
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

    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(
        Color.Transparent ,
        darkIcons = true
    )

  val  activity: Activity = LocalContext.current as  Activity

    var streak by remember { mutableStateOf<StreakModel?>(null) }

    // Load the streak
    LaunchedEffect(streakId) {
        viewModel.getStreakById(streakId) {
            streak = it
        }
    }

    val context = LocalContext.current

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
                        streak?.let {
                            viewModel.deleteStreak(it)
                            activity.finish()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
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
                            .background(Color(s.colorValue.toULong()))
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
                    fontWeight = FontWeight.Bold                )

                Spacer(modifier = Modifier.height(8.dp))

                // End date
                Text(
                    "End Date: ${s.endDate}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current count
                Text(
                    "Current Count: ${s.count}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold                )

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
                    fontWeight = FontWeight.Bold                )

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // Edit Button
                Button(
                    onClick = {
                        val intent = Intent(context, EditStreakActivity::class.java)
                        intent.putExtra("streakId", s.streakId)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(s.colorValue.toULong()))
                ) {
                    Text("Edit Streak", color = Color.White)
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

