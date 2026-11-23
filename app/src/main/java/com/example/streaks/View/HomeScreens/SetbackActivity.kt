package com.example.streaks.View.HomeScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.effectiveAttemptStartMillis
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.R
import com.example.streaks.View.Components.InlineDateTimePicker
import com.example.streaks.View.Components.InlineEditor
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import com.example.streaks.ui.theme.onAccent
import com.example.streaks.ui.theme.readableAccent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@AndroidEntryPoint
class SetbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_STREAK_ID, -1)
        setContent { val vm: StreakViewModel = hiltViewModel(); StreaksAppTheme(vm) { SetbackScreen(id, vm, ::finish) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetbackScreen(id: Int, vm: StreakViewModel, onBack: () -> Unit) {
    val goal by vm.observeStreak(id).collectAsState(initial = null)
    val appAccent by vm.appAccent.collectAsState()
    val streak = goal ?: return CircularProgressIndicator()
    val zone = ZoneId.systemDefault()
    val initial = remember { Instant.now().atZone(zone).withSecond(0).withNano(0) }
    var date by remember { mutableStateOf(initial.toLocalDate()) }
    var time by remember { mutableStateOf<LocalTime?>(initial.toLocalTime()) }
    var expanded by remember { mutableStateOf(InlineEditor.NONE) }
    var note by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val rawAccent = Color((if (streak.usesAppAccent) appAccent else streak.streakColor).toULong())
    val accent = readableAccent(rawAccent, MaterialTheme.colorScheme.background.luminance() < .5f)
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = accent, onPrimary = onAccent(accent))) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.setback_title)) }, navigationIcon = { IconButton(enabled = !saving, onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Button(
                enabled = !saving,
                onClick = {
                    val chosenTime = time ?: return@Button
                    val millis = date.atTime(chosenTime).atZone(zone).toInstant().toEpochMilli()
                    error = when { millis > System.currentTimeMillis() -> "Choose a time that has already passed."; millis < streak.effectiveAttemptStartMillis() -> "Choose a time after this attempt began."; else -> null }
                    if (error == null) {
                        saving = true
                        vm.recordSetback(id, millis, note) { result ->
                            saving = false
                            if (result == null) error = "This setback could not be saved. Check the chosen time."
                            else scope.launch {
                                val response = snackbar.showSnackbar("Your previous attempt is saved. You can begin again.", "Undo")
                                if (response == SnackbarResult.ActionPerformed) vm.undoSetback(result) else onBack()
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)
            ) { Text(if (saving) stringResource(R.string.saving) else stringResource(R.string.save_and_begin_again)) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Your previous attempt will stay in your history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            InlineDateTimePicker(date, time, expanded, { expanded = it }, { date = it }, { time = it }, maxDate = LocalDate.now())
            OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.note_optional)) }, maxLines = 3)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
    }
}
