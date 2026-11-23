package com.example.streaks.View.HomeScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.R
import com.example.streaks.View.Components.InlineDateTimePicker
import com.example.streaks.View.Components.InlineEditor
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.onAccent
import com.example.streaks.ui.theme.readableAccent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditorScreen(existing: StreakModel?, onBack: () -> Unit, onSave: (StreakModel) -> Unit) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val viewModel: StreakViewModel = hiltViewModel()
    val appAccentValue by viewModel.appAccent.collectAsState()
    val appAccent = Color(appAccentValue.toULong())
    val initialInstant = existing?.currentAttemptStartedAtMillis?.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone) }
    var name by remember(existing) { mutableStateOf(existing?.streakName.orEmpty()) }
    var why by remember(existing) { mutableStateOf(existing?.whyQuitting.orEmpty()) }
    var urgeAction by remember(existing) { mutableStateOf(existing?.urgeAction.orEmpty()) }
    var usesAppAccent by remember(existing) { mutableStateOf(existing?.usesAppAccent ?: true) }
    var color by remember(existing, appAccentValue) { mutableStateOf(existing?.takeUnless { it.usesAppAccent }?.let { Color(it.streakColor.toULong()) } ?: appAccent) }
    var startDate by remember(existing) { mutableStateOf(initialInstant?.toLocalDate() ?: existing?.startDate ?: LocalDate.now()) }
    var startTime by remember(existing) { mutableStateOf(initialInstant?.toLocalTime()?.withSecond(0)?.withNano(0) ?: if (existing == null) LocalTime.now().withSecond(0).withNano(0) else null) }
    var expanded by remember { mutableStateOf(InlineEditor.NONE) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val effectiveAccent = if (usesAppAccent) appAccent else color
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val scheme = MaterialTheme.colorScheme.copy(primary = readableAccent(effectiveAccent, dark), onPrimary = onAccent(readableAccent(effectiveAccent, dark)))
    val colors = listOf(Color(0xFF17689A), Color(0xFF2E7D32), Color(0xFFC62828), Color(0xFFFFE082), Color.White, Color.Black, Color(0xFF7B1FA2))
    val names = listOf("Blue", "Green", "Red", "Yellow", "White", "Black", "Purple")

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            topBar = { TopAppBar(
                title = { Text(stringResource(if (existing == null) R.string.create_goal else R.string.edit_goal)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            ) }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(stringResource(R.string.goal_basics), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.goal_name)) }, supportingText = { Text(stringResource(R.string.goal_name_hint)) })
                Text(stringResource(R.string.start_time), style = MaterialTheme.typography.labelLarge)
                InlineDateTimePicker(
                    date = startDate, time = startTime, expanded = expanded, onExpandedChange = { expanded = it },
                    onDateChange = { startDate = it }, onTimeChange = { startTime = it }, allowUnknownTime = true,
                    maxDate = LocalDate.now()
                )
                Text(stringResource(R.string.color), style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { usesAppAccent = true }, modifier = Modifier.semantics { selected = usesAppAccent }) {
                    if (usesAppAccent) Icon(Icons.Default.Check, null)
                    Text("Use app color")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    colors.forEachIndexed { index, choice ->
                        val selectedColor = !usesAppAccent && choice == color
                        Box(
                            Modifier.size(44.dp).semantics { contentDescription = names[index]; selected = selectedColor; role = Role.RadioButton }
                                .background(choice, CircleShape).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { color = choice; usesAppAccent = false }, contentAlignment = Alignment.Center
                        ) { if (selectedColor) Icon(Icons.Default.Check, null, tint = if (choice.luminance() > .5f) Color.Black else Color.White) }
                    }
                }
                Text(stringResource(R.string.personal_plan), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(why, { why = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.why_optional)) }, maxLines = 3)
                OutlinedTextField(urgeAction, { urgeAction = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.urge_action_optional)) }, supportingText = { Text(stringResource(R.string.urge_action_hint)) }, maxLines = 3)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    enabled = !saving,
                    onClick = {
                        val startMillis = startTime?.let { startDate.atTime(it).atZone(zone).toInstant().toEpochMilli() } ?: 0L
                        error = when {
                            name.isBlank() -> context.getString(R.string.name_required)
                            startDate.isAfter(LocalDate.now()) || startMillis > System.currentTimeMillis() -> context.getString(R.string.start_future_error)
                            else -> null
                        }
                        if (error == null) {
                            saving = true
                            onSave((existing ?: StreakModel()).copy(
                                streakName = name.trim(), streakColor = effectiveAccent.value.toLong(), usesAppAccent = usesAppAccent,
                                startDate = startDate, currentAttemptStartedAtMillis = startMillis,
                                whyQuitting = why.trim(), urgeAction = urgeAction.trim()
                            ))
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) { Text(if (saving) stringResource(R.string.saving) else stringResource(R.string.save_goal)) }
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
    }
}
