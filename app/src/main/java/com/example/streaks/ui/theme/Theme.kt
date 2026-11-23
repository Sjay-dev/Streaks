package com.example.streaks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.streaks.View.SettingsScreens.ThemeMode
import com.example.streaks.ViewModel.StreakViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = readableAccent(accent, true),
    onPrimary = onAccent(readableAccent(accent, true)),
    primaryContainer = Color(0xFF174A70),
    onPrimaryContainer = Color(0xFFD1E9FF),
    background = Color(0xFF0E151B),
    onBackground = Color(0xFFE1EAF1),
    surface = Color(0xFF121B22),
    onSurface = Color(0xFFE1EAF1),
    surfaceVariant = Color(0xFF25323C),
    onSurfaceVariant = Color(0xFFC1CBD3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = readableAccent(accent, false),
    onPrimary = onAccent(readableAccent(accent, false)),
    primaryContainer = Color(0xFFCDE8FF),
    onPrimaryContainer = Color(0xFF001E30),
    background = Color(0xFFF7FAFC),
    onBackground = Color(0xFF172026),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172026),
    surfaceVariant = Color(0xFFE4EBF0),
    onSurfaceVariant = Color(0xFF414A51),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun StreaksTheme(darkTheme: Boolean = isSystemInDarkTheme(), accent: Color = Color(0xFF17689A), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkScheme(accent) else lightScheme(accent), typography = Typography, content = content)
}

@Composable
fun StreaksAppTheme(viewModel: StreakViewModel, content: @Composable () -> Unit) {
    val mode by viewModel.themeMode.collectAsState()
    val accentValue by viewModel.appAccent.collectAsState()
    val dark = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val systemUi = rememberSystemUiController()
    SideEffect { systemUi.setSystemBarsColor(Color.Transparent, darkIcons = !dark) }
    StreaksTheme(darkTheme = dark, accent = Color(accentValue.toULong()), content = content)
}

fun readableAccent(color: Color, dark: Boolean): Color {
    val luma = color.luminance()
    return when {
        dark && luma < .28f -> Color(
            (color.red + .45f).coerceAtMost(1f),
            (color.green + .45f).coerceAtMost(1f),
            (color.blue + .45f).coerceAtMost(1f),
            color.alpha
        )
        !dark && luma > .72f -> Color(color.red * .55f, color.green * .55f, color.blue * .55f, color.alpha)
        else -> color
    }
}

fun onAccent(color: Color): Color = if (color.luminance() > .5f) Color.Black else Color.White
