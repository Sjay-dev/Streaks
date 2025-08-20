package com.example.streaks.View.NotificationScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun NotificationScreen(onNotifyClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { onNotifyClick() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Show Notification")
        }
    }
}
