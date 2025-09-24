package com.example.streaks.View.SettingsScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.ViewModel.StreakViewModel

@Composable
fun SettingsScreen(paddingValues: PaddingValues){

    val viewModel : StreakViewModel = hiltViewModel()
    Surface(modifier = Modifier.fillMaxSize()
        .padding(paddingValues)
        .background(color = Color.White)
    )  {
        val isDarkMode by viewModel.isDarkMode.collectAsState()

        Switch(
            checked = isDarkMode,
            onCheckedChange = { enabled -> viewModel.toggleDarkMode(enabled) }
        )
    }
}


@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
