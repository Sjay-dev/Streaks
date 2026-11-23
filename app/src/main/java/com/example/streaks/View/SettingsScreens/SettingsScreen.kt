package com.example.streaks.View.SettingsScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.R
import com.example.streaks.View.Components.ColorPalette
import com.example.streaks.ViewModel.StreakViewModel

@Composable
fun SettingsScreen(paddingValues: PaddingValues, viewModel: StreakViewModel = hiltViewModel()) {
    val theme by viewModel.themeMode.collectAsState()
    val accentValue by viewModel.appAccent.collectAsState()
    val savedName by viewModel.displayName.collectAsState()
    var nameDraft by rememberSaveable(savedName) { mutableStateOf(savedName) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_all_title)) },
            text = { Text(stringResource(R.string.delete_all_explanation)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAllPrivateData(); confirmDelete = false }) {
                    Text(stringResource(R.string.delete_everything), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = nameDraft,
            onValueChange = { nameDraft = it.take(40) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            supportingText = { Text("Used only in your private Home greeting. Blank uses User.") },
            singleLine = true
        )
        Button(
            onClick = { viewModel.setDisplayName(nameDraft) },
            enabled = nameDraft.trim().ifEmpty { SettingsDataStore.DEFAULT_DISPLAY_NAME } != savedName,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Save name") }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        ThemeOption(stringResource(R.string.system_theme), theme == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
        ThemeOption(stringResource(R.string.light_theme), theme == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
        ThemeOption(stringResource(R.string.dark_theme), theme == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }

        Spacer(Modifier.height(16.dp))
        Text("App color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Used for navigation and goals that inherit the app color.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ColorPalette(
            selectedColor = Color(accentValue.toULong()),
            onColorSelected = { viewModel.setAppAccent(it.value.toLong()) },
            modifier = Modifier.fillMaxWidth(),
            contentDescriptionSuffix = "app color"
        )

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.privacy), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.privacy_summary), color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { confirmDelete = true }) {
            Text(stringResource(R.string.delete_private_data), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ThemeOption(text: String, selected: Boolean, onSelected: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onSelected).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelected)
        Text(text, Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
