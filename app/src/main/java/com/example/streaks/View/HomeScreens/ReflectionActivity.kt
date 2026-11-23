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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReflectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_STREAK_ID, -1)
        setContent {
            val vm: StreakViewModel = hiltViewModel()
            StreaksAppTheme(vm) { ReflectionScreen(id, vm, ::finish) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectionScreen(streakId: Int, vm: StreakViewModel, onClose: () -> Unit) {
    var trigger by rememberSaveable { mutableStateOf("") }
    var helped by rememberSaveable { mutableStateOf("") }
    var saving by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_reflection)) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel)) } },
                actions = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("This optional note is private and does not change your streak.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = trigger,
                onValueChange = { trigger = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.trigger_optional)) },
                minLines = 3,
                maxLines = 6
            )
            OutlinedTextField(
                value = helped,
                onValueChange = { helped = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.what_helped_optional)) },
                minLines = 3,
                maxLines = 6
            )
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = !saving && streakId > 0,
                onClick = {
                    saving = true
                    vm.recordUrge(streakId, trigger, helped, onClose)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (saving) stringResource(R.string.saving) else "Save reflection") }
        }
    }
}
