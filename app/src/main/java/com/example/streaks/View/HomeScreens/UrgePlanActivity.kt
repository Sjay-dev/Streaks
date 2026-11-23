package com.example.streaks.View.HomeScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import com.example.streaks.ui.theme.onAccent
import com.example.streaks.ui.theme.readableAccent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UrgePlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_STREAK_ID, -1)
        setContent {
            val vm: StreakViewModel = hiltViewModel()
            StreaksAppTheme(vm) { UrgePlanScreen(id, vm, ::finish) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrgePlanScreen(id: Int, vm: StreakViewModel, onDone: () -> Unit) {
    val goal by vm.observeStreak(id).collectAsState(initial = null)
    val appAccent by vm.appAccent.collectAsState()
    val current = goal ?: return CircularProgressIndicator()
    val stored = if (current.usesAppAccent) appAccent else current.streakColor
    val rawAccent = Color(stored.toULong())
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val accent = readableAccent(rawAccent, dark)
    var reflecting by remember { mutableStateOf(false) }
    var trigger by remember { mutableStateOf("") }
    var helped by remember { mutableStateOf("") }
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = accent, onPrimary = onAccent(accent))) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.urge_plan_action)) }, navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) },
            bottomBar = {
                Button(
                    onClick = { if (reflecting) vm.recordUrge(id, trigger, helped); onDone() },
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)
                ) { Text(stringResource(R.string.done)) }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).imePadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(stringResource(R.string.urge_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(current.urgeAction.ifBlank { stringResource(R.string.default_urge_action) }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.urge_reassurance), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (!reflecting) TextButton(onClick = { reflecting = true }) { Text(stringResource(R.string.add_reflection)) }
                else {
                    Text(stringResource(R.string.optional_reflection), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(trigger, { trigger = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.trigger_optional)) }, maxLines = 2)
                    OutlinedTextField(helped, { helped = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.what_helped_optional)) }, maxLines = 2)
                }
            }
        }
    }
}
