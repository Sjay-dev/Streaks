package com.example.streaks.View.HomeScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateStreakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            StreaksAppTheme(viewModel) {
                GoalEditorScreen(existing = null, onBack = ::finish) { goal ->
                    viewModel.addStreak(goal) { finish() }
                }
            }
        }
    }
}
