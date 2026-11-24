package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.ArchiveAction
import com.example.streaks.Model.SetbackResult
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.StreakRepository
import com.example.streaks.Model.archiveActionFor
import com.example.streaks.Model.archiveTargetsFor
import com.example.streaks.Notification.ReminderScheduler
import com.example.streaks.View.SettingsScreens.SettingsDataStore
import com.example.streaks.View.SettingsScreens.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val repository: StreakRepository,
    private val settings: SettingsDataStore,
    private val reminders: ReminderScheduler
) : ViewModel() {
    private val streakLoadGeneration = MutableStateFlow(0)
    val streakLoadState: StateFlow<StreakLoadState> = streakLoadGeneration
        .flatMapLatest {
            repository.getAllStreaks()
                .map<List<StreakModel>, StreakLoadState>(StreakLoadState::Loaded)
                .onStart { emit(StreakLoadState.Loading) }
                .catch { emit(StreakLoadState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StreakLoadState.Loading)
    val streaks: StateFlow<List<StreakModel>> = streakLoadState
        .map { (it as? StreakLoadState.Loaded)?.streaks.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val appAccent: StateFlow<Long> = settings.appAccent
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_APP_ACCENT)
    val displayName: StateFlow<String> = settings.displayName
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_DISPLAY_NAME)
    val reminderSchedules: StateFlow<List<ReminderSchedule>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeStreak(id: Int): Flow<StreakModel?> = repository.observeStreak(id)
    fun observeAttempts(id: Int): Flow<List<AttemptModel>> = repository.observeAttempts(id)
    fun getStreakById(id: Int, callback: (StreakModel?) -> Unit) = viewModelScope.launch {
        callback(repository.getStreakById(id))
    }
    fun getReminder(
        id: Int,
        callback: (ReminderSchedule?) -> Unit,
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            callback(repository.getReminder(id))
        } catch (_: Exception) {
            onError()
        }
    }

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

    fun retryStreakLoad() = streakLoadGeneration.update { it + 1 }

    fun archiveStreak(streakId: Int, onArchived: (Int) -> Unit = {}) = viewModelScope.launch {
        reminders.cancel(streakId)
        repository.remindersForGoal(streakId).forEach { reminders.cancelReminder(it.reminderId) }
        onArchived(repository.archiveStreak(streakId))
    }

    fun archiveStreaks(streakIds: Set<Int>, onArchived: (Int) -> Unit = {}) = viewModelScope.launch {
        val reminderIds = streakIds.associateWith { repository.remindersForGoal(it).map(ReminderSchedule::reminderId) }
        val changed = repository.archiveStreaks(streakIds)
        reminderIds.forEach { (streakId, ids) ->
            reminders.cancel(streakId)
            ids.forEach(reminders::cancelReminder)
        }
        onArchived(changed)
    }

    fun unarchiveStreaks(streakIds: Set<Int>, onUnarchived: (Int) -> Unit = {}) = viewModelScope.launch {
        onUnarchived(repository.unarchiveStreaks(streakIds))
    }

    fun applyArchiveAction(
        selectedStreaks: Collection<StreakModel>,
        onComplete: (ArchiveAction, Int) -> Unit = { _, _ -> }
    ) {
        val action = archiveActionFor(selectedStreaks) ?: return
        val ids = archiveTargetsFor(selectedStreaks, action)
        when (action) {
            ArchiveAction.ARCHIVE -> archiveStreaks(ids) { onComplete(action, it) }
            ArchiveAction.UNARCHIVE -> unarchiveStreaks(ids) { onComplete(action, it) }
        }
    }

    fun deleteStreaks(streakIds: Set<Int>, onDeleted: () -> Unit = {}) = viewModelScope.launch {
        val reminderIds = streakIds.associateWith { repository.remindersForGoal(it).map(ReminderSchedule::reminderId) }
        repository.deleteStreaks(streakIds)
        reminderIds.forEach { (streakId, ids) ->
            reminders.cancel(streakId)
            ids.forEach(reminders::cancelReminder)
        }
        onDeleted()
    }

    fun recordSetback(streakId: Int, atMillis: Long, note: String, onComplete: (SetbackResult?) -> Unit) =
        viewModelScope.launch { onComplete(repository.recordSetback(streakId, atMillis, note)) }

    fun undoSetback(result: SetbackResult, onComplete: (Boolean) -> Unit = {}) = viewModelScope.launch {
        onComplete(repository.undoSetback(result))
    }

    fun recordUrge(streakId: Int, trigger: String, whatHelped: String, onSaved: () -> Unit = {}) = viewModelScope.launch {
        if (trigger.isNotBlank() || whatHelped.isNotBlank()) repository.recordUrge(streakId, trigger, whatHelped)
        onSaved()
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setAppAccent(color: Long) = viewModelScope.launch { settings.setAppAccent(color) }
    fun setDisplayName(name: String) = viewModelScope.launch { settings.setDisplayName(name) }

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

sealed interface StreakLoadState {
    data object Loading : StreakLoadState
    data class Loaded(val streaks: List<StreakModel>) : StreakLoadState
    data object Error : StreakLoadState
}
