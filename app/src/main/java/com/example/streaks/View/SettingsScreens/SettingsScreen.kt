package com.example.streaks.View.SettingsScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel

@Composable
fun SettingsScreen(paddingValues: PaddingValues, viewModel: StreakViewModel = hiltViewModel()) {
    val theme by viewModel.themeMode.collectAsState()
    val accentValue by viewModel.appAccent.collectAsState()
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
        Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        ThemeOption(stringResource(R.string.system_theme), theme == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
        ThemeOption(stringResource(R.string.light_theme), theme == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
        ThemeOption(stringResource(R.string.dark_theme), theme == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
        Spacer(Modifier.height(16.dp))
        Text("App color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Used for navigation and goals that inherit the app color.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val choices = listOf(
                "Blue" to Color(0xFF17689A), "Green" to Color(0xFF2E7D32), "Red" to Color(0xFFC62828),
                "Yellow" to Color(0xFFFFE082), "White" to Color.White, "Purple" to Color(0xFF7B1FA2), "Black" to Color.Black
            )
            choices.forEach { (name, color) ->
                val isSelected = accentValue == color.value.toLong()
                Box(
                    Modifier.size(42.dp).semantics { contentDescription = "$name app color"; selected = isSelected; role = Role.RadioButton }
                        .background(color, CircleShape).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { viewModel.setAppAccent(color.value.toLong()) }, contentAlignment = Alignment.Center
                ) { if (isSelected) Icon(Icons.Default.Check, null, tint = if (color.luminance() > .5f) Color.Black else Color.White) }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.privacy), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.privacy_summary), color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete_private_data), color = MaterialTheme.colorScheme.error) }
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
