package com.example.streaks.View.NotificationScreens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import com.example.streaks.ViewModel.NotificationViewModel
import com.example.streaks.ViewModel.StreakViewModel
import java.time.Instant
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
                .background(Color.White)
        ) {
            items(notifications) { notification ->
                StreakNotificationItem(notification = notification, viewModel = viewModel,)
            }
        }
    }
}




@Composable
fun StreakNotificationItem(
    notification: NotificationModel , viewModel: NotificationViewModel) {

    val streakColor = Color(notification.streakColor.toULong())
    val streakViewModel : StreakViewModel = hiltViewModel()

    // Gradient border
    val gradientBrush = Brush.linearGradient(

        colors = listOf(
            streakColor.copy(alpha = 0.9f),
            streakColor.copy(alpha = 0.4f),
            streakColor.copy(alpha = 0.9f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        tonalElevation = 6.dp,
        color = Color.White,
        border = BorderStroke(2.dp, gradientBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: streak info
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.streakName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = streakColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status badge
                    val statusColor = when (notification.status) {
                        Status.OnGoing -> Color(0xFF4CAF50)
                        Status.Cancelled -> Color.Red
                    }
                    Text(
                        text = notification.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(
                                statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Frequency + Time
                val formattedTime = remember(notification.timestamp) {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    Instant.ofEpochMilli(notification.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(formatter)
                }

                val frequencyText = notification.frequency.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }

                Text(
                    buildAnnotatedString {
                        append("Freq: ")

                        withStyle(style = SpanStyle(color = streakColor)) {
                            append(frequencyText)
                        }

                        append(" • $formattedTime")
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Right side: actions
            Row {
                IconButton(onClick ={
                    viewModel.updateStatus(notification.id, Status.OnGoing)
                    viewModel.deleteNotification(notification.id)
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Done",
                        tint = Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = {
                    viewModel.updateStatus(notification.id, Status.Cancelled)
                    streakViewModel.endStreak(notification.streakId)
                }) {
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









