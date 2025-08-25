package com.example.streaks.View.SettingsScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(paddingValues: PaddingValues){
    Box(modifier = Modifier.fillMaxSize()
        .padding(paddingValues)
        , contentAlignment = Alignment.Center) {
        Text("Settings Screen")
    }
}