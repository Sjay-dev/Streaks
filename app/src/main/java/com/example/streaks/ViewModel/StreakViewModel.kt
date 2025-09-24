package com.example.streaks.ViewModel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.StreakRepository
import com.example.streaks.Notification.EXTRA_FREQUENCY
import com.example.streaks.Notification.EXTRA_NOTIFICATION_TYPE
import com.example.streaks.Notification.EXTRA_STREAK_NAME
import com.example.streaks.Notification.ReminderRecevier
import com.example.streaks.Notification.STREAK_COLOR
import com.example.streaks.Notification.STREAK_ID
import com.example.streaks.View.SettingsScreens.SettingsDataStore
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class StreakViewModel @Inject constructor
    (private val repository: StreakRepository,
     private val settingsDataStore: SettingsDataStore,
     application: Application) : AndroidViewModel(application) {

    val streaks: StateFlow<List<StreakModel>> = repository.getAllStreaks().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
        )

    fun addStreak(streak: StreakModel) {
        viewModelScope.launch {

            val id = repository.insertStreak(streak)

            val savedStreak = streak.copy(streakId = id)

            if (savedStreak.reminderTime == null){

            }

            else{
                savedStreak.reminderTime?.let { reminderTime ->
                    val triggerAtMillis = LocalDateTime.of(LocalDate.now(), reminderTime)
                        .let { if (it.isBefore(LocalDateTime.now())) it.plusDays(1) else it }
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()


                    scheduleAlarm(getApplication(), savedStreak, triggerAtMillis)
                }

            }



        }
    }

    fun updateStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.updateStreak(streak)

            streak.reminderTime?.let { reminderTime ->
                val triggerAtMillis = LocalDateTime.of(LocalDate.now(), reminderTime)
                    .let { if (it.isBefore(LocalDateTime.now())) it.plusDays(1) else it }
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()


                scheduleAlarm(getApplication(), streak, triggerAtMillis)
            }

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

    fun endStreak(streakId: Int) {
        viewModelScope.launch {
            repository.endStreak(streakId)
        }
    }

    val isDarkMode: StateFlow<Boolean> = settingsDataStore.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(enabled)
        }
    }

    // === Calculations ===

    fun nextCount(streak: StreakModel, now: LocalDateTime = LocalDateTime.now()): String {
        val start = streak.startDate.atStartOfDay()
        val end = streak.endDate?.atTime(23, 59, 59)

        if (end != null && now.isAfter(end)) return "Streak ended"

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

    fun calculateStreakCount(
        streak: StreakModel,
        now: LocalDate = LocalDate.now()
    ): Int {
        if (now.isBefore(streak.startDate)) return 0

        val end = streak.endDate
        return when {
            //  If endDate exists and streak has already ended → cap at endDate
            end != null && now.isAfter(end) -> {
                when (streak.frequency) {
                    Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, end).toInt()
                    Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, end).toInt()
                    Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, end).toInt()
                }
            }

            // ✅ If endDate is null → keep counting up to "now"
            else -> {
                when (streak.frequency) {
                    Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, now).toInt()
                    Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, now).toInt()
                    Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, now).toInt()
                }
            }
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
                    streak?.let { deleteStreak(it)
                    cancelAlarm(it.streakId , getApplication())
                    }
                }
            }
            clearSelection()
        }
    }





    // ========================= CUSTOM TIME PICKER =========================
    @Composable
    fun CustomTimePicker(
        initialHour: Int,
        initialMinute: Int,
        streakColor : Color,
        onCancel: () -> Unit,
        onSave: (Int, Int) -> Unit
    ) {
        val context = LocalContext.current
        val is24Hour = DateFormat.is24HourFormat(context)

        var selectedHour by remember { mutableStateOf(initialHour) }
        var selectedMinute by remember { mutableStateOf(initialMinute) }
        var isAm by remember { mutableStateOf(initialHour < 12) }
        val hours = if (is24Hour) (0..23).toList() else (1..12).toList()
        val minutes = (0..59).toList()
        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM"))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Select Time",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = streakColor
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoopingNumberPicker(streakColor, hours, initialHour) { selectedHour = it }

                Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = streakColor)

                LoopingNumberPicker(streakColor , minutes, initialMinute) { selectedMinute = it }

                if (!is24Hour) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = { isAm = true }) {
                            Text(
                                "AM",
                                fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAm) streakColor else Color.Gray
                            )
                        }
                        TextButton(onClick = { isAm = false }) {
                            Text(
                                "PM",
                                fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isAm) streakColor else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Today - $formattedDate", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onCancel() }) {
                    Text("Cancel", fontSize = 16.sp, color = Color.Black)
                }
                Button(
                    onClick = {
                        val finalHour =
                            if (is24Hour) selectedHour
                            else if (isAm) selectedHour % 12
                            else (selectedHour % 12) + 12
                        onSave(finalHour, selectedMinute)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = streakColor)
                ) {
                    Text("Save", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }

    @Composable
    fun LoopingNumberPicker(
        streakColor: Color,
        items: List<Int>,
        initialValue: Int,
        onValueChange: (Int) -> Unit
    ) {
        val repeatedItems = List(1000) { index -> items[index % items.size] }
        val middleIndex = repeatedItems.size / 2

        val initialIndex = remember(initialValue) {
            val safeValue = if (initialValue in items) initialValue else items.first()
            val offset = items.indexOf(safeValue)
            if (offset >= 0) middleIndex - (middleIndex % items.size) + offset else middleIndex
        }

        val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)

        Box(
            modifier = Modifier
                .height(140.dp)
                .width(80.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = state,
                flingBehavior = flingBehavior,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(repeatedItems.size) { index ->
                    val value = repeatedItems[index]
                    val isSelected =
                        state.firstVisibleItemIndex + (state.layoutInfo.visibleItemsInfo.size / 2) == index

                    Text(
                        text = String.format("%02d", value),
                        fontSize = if (isSelected) 36.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)  streakColor else Color.Gray,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        LaunchedEffect(state.isScrollInProgress) {
            if (!state.isScrollInProgress) {
                val centerIndex =
                    state.firstVisibleItemIndex + (state.layoutInfo.visibleItemsInfo.size / 2)
                val actualValue = repeatedItems.getOrNull(centerIndex)
                if (actualValue != null) {
                    onValueChange(actualValue)
                }
            }
        }
    }

    // ========================= REMINDER SCHEDULE LOGIC =========================

    fun scheduleAlarm(context: Context, streak: StreakModel, triggerAtMillis: Long) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderRecevier::class.java).apply {
            putExtra(EXTRA_STREAK_NAME, streak.streakName)
            putExtra(EXTRA_FREQUENCY, streak.frequency.name)
            putExtra(EXTRA_NOTIFICATION_TYPE, streak.notificationType.name)
            putExtra(STREAK_ID, streak.streakId)
            putExtra(STREAK_COLOR , streak.streakColor)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            streak.streakId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    Toast.makeText(context, "Please allow exact alarms in settings.", Toast.LENGTH_LONG).show()
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to schedule alarm: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun cancelAlarm(streakId: Int , context: Context) {
        val intent = Intent(context, ReminderRecevier::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            streakId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // cleanup
    }

}