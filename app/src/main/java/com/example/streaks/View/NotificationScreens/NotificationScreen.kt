package com.example.streaks.View.NotificationScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import com.example.streaks.Model.StreakModel
import com.example.streaks.ViewModel.NotificationViewModel
import com.example.streaks.ViewModel.StreakViewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun NotificationScreen(
    paddingValues: PaddingValues,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    if (notifications.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No Notification yet!",
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
            items(notifications) { notification ->
                StreakNotificationItem(
                    notification = notification,
                    onDone = { viewModel.updateStatus(notification.id, Status.OnGoing) },
                    onCancel = { viewModel.updateStatus(notification.id, Status.Cancelled) }
                )
            }
        }
    }
}




@Composable
fun StreakNotificationItem(
    notification: NotificationModel,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: streak info
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.streakName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Slim status badge
                    val statusColor = when (notification.status) {
                        Status.OnGoing -> Color(0xFF4CAF50)
                        Status.Cancelled -> Color.Red
                    }
                    Text(
                        text = notification.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(
                                statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Frequency + Time in one line
                val formattedTime = remember(notification.timestamp) {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    Instant.ofEpochMilli(notification.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(formatter)
                }
                Text(
                    text = "Freq: ${notification.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} • $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Right side: actions
            Row {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Done",
                        tint = Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Reminder",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}








