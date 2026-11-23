package com.example.streaks.View.HomeScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Notification.EXTRA_STREAK_ID
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditStreakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_STREAK_ID, intent.getIntExtra("streakId", -1))
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            StreaksAppTheme(viewModel) {
                val goal by viewModel.observeStreak(id).collectAsState(initial = null)
                goal?.let { existing ->
                    GoalEditorScreen(existing, onBack = ::finish) { updated ->
                        viewModel.updateStreak(updated) { finish() }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }
}
