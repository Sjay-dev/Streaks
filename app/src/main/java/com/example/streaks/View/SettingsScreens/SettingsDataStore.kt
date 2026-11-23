package com.example.streaks.View.SettingsScreens

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.ui.graphics.Color
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_ACCENT = longPreferencesKey("app_accent")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            when (prefs[Keys.THEME_MODE]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    val appAccent: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.APP_ACCENT] ?: DEFAULT_APP_ACCENT
    }

    suspend fun setAppAccent(color: Long) {
        context.dataStore.edit { it[Keys.APP_ACCENT] = color }
    }

    companion object {
        val DEFAULT_APP_ACCENT: Long = Color(0xFF17689A).value.toLong()
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

