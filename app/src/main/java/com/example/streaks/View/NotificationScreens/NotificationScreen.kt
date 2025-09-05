package com.example.streaks.View.NotificationScreens

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streaks.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import com.example.streaks.View.HomeScreens.HomeScreenActivity
import com.example.streaks.ViewModel.NotificationViewModel
import com.example.streaks.ViewModel.StreakViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@Composable
fun NotificationScreen(
    paddingValues: PaddingValues,
    viewModel: NotificationViewModel = hiltViewModel()
) {

    val history by viewModel.notificationHistory.collectAsState()

    if (history.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No Notifications yet!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Blue
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(history) { record ->
                StreakNotificationItem(
                    streakName = record.streakName,
                    time = record.time,
                    status = record.status
                )
            }
        }
    }
}

@Composable
fun StreakNotificationItem(
    streakName: String,
    time: LocalDateTime,
    status: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = streakName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when (status) {
                    "Done" -> Color(0xFF4CAF50)
                    "Cancelled" -> Color.Red
                    else -> Color.Gray
                }
            )
        }
    }
}





