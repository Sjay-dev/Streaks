package com.example.streaks.ViewModel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class StreakViewModel @Inject constructor
    (private val repository: StreakRepository) : ViewModel() {

    val streaks: StateFlow<List<StreakModel>> =
        repository.getAllStreaks().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
        )

    fun addStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.insertStreak(streak)
        }
    }

    fun updateStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.updateStreak(streak)
        }
    }

    fun deleteStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.deleteStreak(streak)
        }
    }

    fun getStreakById(id: Int, callback: (StreakModel?) -> Unit) {
        viewModelScope.launch {
            callback(repository.getStreakById(id))
        }
    }



    // === Calculations ===

    private fun pluralize(value: Long, unit: String): String {
        return "$value $unit" + if (value == 1L) "" else "s"
    }

    fun nextCount(streak: StreakModel): String {
        val now = LocalDateTime.now()
        val start = streak.startDate.atStartOfDay()
        val end = streak.endDate.atTime(23, 59, 59)

        if (now.isAfter(end)) return "Streak ended"

        fun pluralize(value: Long, unit: String) =
            "$value $unit" + if (value != 1L) "s" else ""

        return when (streak.frequency) {
            Frequency.DAILY -> {
                val endOfToday = now.toLocalDate().atTime(23, 59, 59)
                val totalMinutes = ChronoUnit.MINUTES.between(now, endOfToday)
                val hoursLeft = totalMinutes / 60
                val minutesLeft = totalMinutes % 60
                "${pluralize(hoursLeft, "hour")} ${pluralize(minutesLeft, "min")} left in today’s streak"
            }
            Frequency.WEEKLY -> {
                val daysSinceStart = ChronoUnit.DAYS.between(start, now)
                val daysIntoWeek = daysSinceStart % 7
                val daysLeft = 7 - daysIntoWeek
                "${pluralize(daysLeft, "day")} left in this weekly streak"
            }
            Frequency.MONTHLY -> {
                val monthsSinceStart = ChronoUnit.MONTHS.between(start, now)
                val currentMonthStart = streak.startDate.plusMonths(monthsSinceStart).atStartOfDay()
                val nextMonthStart = currentMonthStart.plusMonths(1)
                val daysLeft = ChronoUnit.DAYS.between(now, nextMonthStart)
                val weeks = daysLeft / 7
                val extraDays = daysLeft % 7
                "${pluralize(weeks, "week")} ${pluralize(extraDays, "day")} left in this monthly streak"
            }
        }
    }


    fun calculateStreakCount(streak: StreakModel): Int {
        val now = LocalDate.now()

        if (now.isBefore(streak.startDate)) return 0
        if (now.isAfter(streak.endDate)) {
            return when (streak.frequency) {
                Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, streak.endDate).toInt() + 1
                Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, streak.endDate).toInt() + 1
                Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, streak.endDate).toInt() + 1
            }
        }

        return when (streak.frequency) {
            Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, now).toInt() + 1
            Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, now).toInt() + 1
            Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, now).toInt() + 1
        }
    }


                                // === CLICKS ===
    private val _selectedStreaks = MutableStateFlow<Set<Int>>(emptySet())
    val selectedStreaks: StateFlow<Set<Int>> = _selectedStreaks

    fun toggleSelection(streakId: Int) {
        _selectedStreaks.value =
            if (_selectedStreaks.value.contains(streakId)) {
                _selectedStreaks.value - streakId
            } else {
                _selectedStreaks.value + streakId
            }
    }

    fun clearSelection() {
        _selectedStreaks.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedStreaks.value.forEach { id ->
                getStreakById(id) { streak ->
                    streak?.let { deleteStreak(it) }
                }
            }
            clearSelection()
        }
    }

    // FOR REMINDERS
    fun scheduleNotification(context: Context, streakId: Int, streakName: String, reminderTime: LocalTime) {
        val now = LocalDateTime.now()
        var targetDateTime = LocalDateTime.of(LocalDate.now(), reminderTime)

        // If selected time already passed, schedule for tomorrow
        if (targetDateTime.isBefore(now)) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        Duration.between(now, targetDateTime).toMillis()

        workDataOf(
            "streakId" to streakId,
            "streakName" to streakName
        )

    }



}