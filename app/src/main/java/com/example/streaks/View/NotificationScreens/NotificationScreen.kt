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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.ViewModel.NotificationViewModel
import com.example.streaks.ViewModel.StreakViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun NotificationScreen(
    paddingValues: PaddingValues,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notificationHistory.collectAsState()

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
                    onDone = {  },
                    onCancel = {  }
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
            // Left side: Streak info
            Column {
                Text(
                    text = notification.streakName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                notification.time?.let { reminder ->
                    Text(
                        text = reminder.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Right side: Actions ✅ ❌
            Row {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Done",
                        tint = Color(0xFF4CAF50) // green
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





