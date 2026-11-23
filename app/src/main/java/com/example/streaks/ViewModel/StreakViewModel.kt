package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.SetbackResult
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.StreakRepository
import com.example.streaks.Notification.ReminderScheduler
import com.example.streaks.View.SettingsScreens.SettingsDataStore
import com.example.streaks.View.SettingsScreens.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val repository: StreakRepository,
    private val settings: SettingsDataStore,
    private val reminders: ReminderScheduler
) : ViewModel() {
    val streaks: StateFlow<List<StreakModel>> = repository.getAllStreaks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val appAccent: StateFlow<Long> = settings.appAccent
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_APP_ACCENT)
    val reminderSchedules: StateFlow<List<ReminderSchedule>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeStreak(id: Int): Flow<StreakModel?> = repository.observeStreak(id)
    fun observeAttempts(id: Int): Flow<List<AttemptModel>> = repository.observeAttempts(id)
    fun getStreakById(id: Int, callback: (StreakModel?) -> Unit) = viewModelScope.launch {
        callback(repository.getStreakById(id))
    }
    fun getReminder(id: Int, callback: (ReminderSchedule?) -> Unit) = viewModelScope.launch { callback(repository.getReminder(id)) }

    fun addStreak(streak: StreakModel, onSaved: (Int) -> Unit = {}) = viewModelScope.launch {
        val id = repository.insertStreak(streak)
        reminders.schedule(streak.copy(streakId = id))
        onSaved(id)
    }

    fun updateStreak(streak: StreakModel, onSaved: () -> Unit = {}) = viewModelScope.launch {
        val previous = repository.getStreakById(streak.streakId)
        if (previous?.reminderTime != streak.reminderTime || streak.reminderTime == null) reminders.cancel(streak.streakId)
        repository.updateStreak(streak)
        reminders.schedule(streak)
        onSaved()
    }

    fun deleteStreak(streak: StreakModel, onDeleted: () -> Unit = {}) = viewModelScope.launch {
        reminders.cancel(streak.streakId)
        repository.remindersForGoal(streak.streakId).forEach { reminders.cancelReminder(it.reminderId) }
        repository.deleteStreak(streak)
        onDeleted()
    }

    fun archiveStreak(streakId: Int, onArchived: () -> Unit = {}) = viewModelScope.launch {
        reminders.cancel(streakId)
        repository.remindersForGoal(streakId).forEach { reminders.cancelReminder(it.reminderId) }
        repository.archiveStreak(streakId)
        onArchived()
    }

    fun recordSetback(streakId: Int, atMillis: Long, note: String, onComplete: (SetbackResult?) -> Unit) =
        viewModelScope.launch { onComplete(repository.recordSetback(streakId, atMillis, note)) }

    fun undoSetback(result: SetbackResult, onComplete: (Boolean) -> Unit = {}) = viewModelScope.launch {
        onComplete(repository.undoSetback(result))
    }

    fun recordUrge(streakId: Int, trigger: String, whatHelped: String) = viewModelScope.launch {
        if (trigger.isNotBlank() || whatHelped.isNotBlank()) repository.recordUrge(streakId, trigger, whatHelped)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setAppAccent(color: Long) = viewModelScope.launch { settings.setAppAccent(color) }

    fun saveReminder(reminder: ReminderSchedule, onSaved: (Int) -> Unit = {}) = viewModelScope.launch {
        val id = if (reminder.reminderId == 0) repository.saveReminder(reminder) else {
            reminders.cancelReminder(reminder.reminderId)
            repository.updateReminder(reminder)
            reminder.reminderId
        }
        reminders.schedule(reminder.copy(reminderId = id))
        onSaved(id)
    }

    fun setReminderEnabled(reminder: ReminderSchedule, enabled: Boolean) = viewModelScope.launch {
        val updated = reminder.copy(enabled = enabled)
        repository.updateReminder(updated)
        if (enabled) reminders.schedule(updated) else reminders.cancelReminder(reminder.reminderId)
    }

    fun deleteReminder(reminder: ReminderSchedule) = viewModelScope.launch {
        reminders.cancelReminder(reminder.reminderId)
        repository.deleteReminder(reminder)
    }

    fun deleteAllPrivateData(onComplete: () -> Unit = {}) = viewModelScope.launch {
        reminders.cancelAll(streaks.value)
        reminderSchedules.value.forEach { reminders.cancelReminder(it.reminderId) }
        repository.clearAll()
        onComplete()
    }
}
