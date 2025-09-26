package com.example.streaks.View.SettingsScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.ViewModel.StreakViewModel

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
    val viewModel: StreakViewModel = hiltViewModel()
    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Theme", style = MaterialTheme.typography.titleLarge)

        ThemeOption("System default", ThemeMode.SYSTEM, themeMode == ThemeMode.SYSTEM) {
            viewModel.setThemeMode(ThemeMode.SYSTEM)
        }
        ThemeOption("Light mode", ThemeMode.LIGHT, themeMode == ThemeMode.LIGHT) {
            viewModel.setThemeMode(ThemeMode.LIGHT)
        }
        ThemeOption("Dark mode", ThemeMode.DARK, themeMode == ThemeMode.DARK) {
            viewModel.setThemeMode(ThemeMode.DARK)
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    mode: ThemeMode,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}



