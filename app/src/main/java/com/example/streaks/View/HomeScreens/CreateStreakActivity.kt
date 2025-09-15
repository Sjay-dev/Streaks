package com.example.streaks.View.HomeScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationType
import com.example.streaks.Model.StreakModel
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ========================= ACTIVITY =========================
@AndroidEntryPoint
class CreateStreakActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateStreakScreen()
        }
    }
}


// ========================= SCREEN =========================
@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateStreakScreen() {
    val activity = LocalContext.current as Activity
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = true)

    // === ViewModels ===
    val viewModel: StreakViewModel = hiltViewModel()
    val notiViewModel : NotificationViewModel = hiltViewModel()

    // === States ===
    var streakName by remember { mutableStateOf("") }
    var streakColor by remember { mutableStateOf(Color.Blue) }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }


    //=== Start Date values ===
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(true) }
    var isToday by remember { mutableStateOf(true) }
    var isTodayText by remember { mutableStateOf("Today?") }
    var tempSelectedStartDate by remember { mutableStateOf<Long?>(null) }
    var confirmedStartDate by remember { mutableStateOf<Long?>(null) }
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = tempSelectedStartDate)

    //=== End Date values ===
    var showEndDatePicker by remember { mutableStateOf(true) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var isEternity by remember { mutableStateOf(true) }
    var isEternityText by remember { mutableStateOf("Externity?") }
    var confirmedEndDate by remember { mutableStateOf<Long?>(null) }
    var tempSelectedEndDate by remember { mutableStateOf<Long?>(null) }
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = tempSelectedEndDate)

    //=== Reminder values ===
    var isReminder by remember { mutableStateOf(true) }
    var reminderText by remember { mutableStateOf("No Reminder?") }
    var showTimePicker by remember { mutableStateOf(true) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var tempTime by remember { mutableStateOf<LocalTime?>(null) }
    var showBox by remember { mutableStateOf(false) }


    var reminderType by remember { mutableStateOf(NotificationType.DEFAULT) }


    //===KEYBOARD focusManager===
    val focusManager = LocalFocusManager.current

    //===PRESET COLORS===
    val presetColors = listOf(Color.Red, Color.Blue, Color.Black, Color.Green, Color.Magenta)

    val frequencyOptions = Frequency.values()


    //===UI===//
    Scaffold(
        topBar = {
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Create A New Streak", fontWeight = Bold, fontSize = 23.sp)
                    IconButton(onClick = { activity.finish() }, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24px),
                            contentDescription = "Back"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(20.dp)) {


                // === STREAK NAME INPUT ===
                OutlinedTextField(
                    value = streakName,
                    onValueChange = { streakName = it },
                    label = { Text("Streak Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = streakColor,
                        focusedBorderColor = streakColor
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.size(15.dp))

                // === COLOR PICKER ===
                Text("Color Theme", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(5.dp))
                Row {
                    presetColors.forEach { presetColor ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(top = 10.dp, end = 10.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .border(
                                    width = if (streakColor == presetColor) 3.dp else 0.dp,
                                    color = if (streakColor == presetColor) Color.White else streakColor,
                                    shape = CircleShape
                                )
                                .clickable { streakColor = presetColor }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(15.dp))

                // === FREQUENCY PICKER ===
                Text("Frequency", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(5.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    frequencyOptions.forEach { option ->
                        Button(
                            onClick = { frequency = option },
                            modifier = Modifier.padding(end = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (frequency == option) streakColor else Color.LightGray,
                                contentColor = if (frequency == option) if (streakColor == Color.Yellow) Color.Black else Color.White else Color.Black
                            )
                        ) {
                            Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                Spacer(modifier = Modifier.size(15.dp))

                // === TOGGLE START DATE ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start $isTodayText", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isToday,
                        onCheckedChange = {
                            isToday = it
                            isTodayText = "Today?"
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = streakColor)
                    )
                }

                // === TOGGLE END DATE ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Till $isEternityText", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isEternity,
                        onCheckedChange = {
                            isEternity = it
                            isEternityText = "Eternity?"
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = streakColor)
                    )
                }

                // === TOGGLE REMINDER ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(reminderText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

                    Switch(
                        checked = isReminder,
                        onCheckedChange = {
                            isReminder = it
                            if (isReminder) {
                                reminderText = "No Reminder?"
                            } else {
                                if (selectedTime == null) {
                                    reminderText = "No Reminder?"
                                } else {
                                    reminderText = if (DateFormat.is24HourFormat(activity)) {
                                        "Remind at ${
                                            selectedTime!!.format(
                                                DateTimeFormatter.ofPattern(
                                                    "HH:mm"
                                                )
                                            )
                                        }"
                                    } else {
                                        "Remind at ${
                                            selectedTime!!.format(
                                                DateTimeFormatter.ofPattern(
                                                    "hh:mm a"
                                                )
                                            )
                                        }"
                                    }
                                }
                            }


                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = streakColor)
                    )
                }


                // === START DATE PICKER ===
                if (!isToday) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { showStartDatePicker = true }
                            .padding(16.dp)
                    ) {
                        val displayText = confirmedStartDate?.let {
                            "Tap to Pick another start date"
                        } ?: "Tap to select a start date"

                        Text(displayText)
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    if (showStartDatePicker) {
                        DatePicker(
                            state = startDatePickerState,
                            showModeToggle = true,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.White,
                                currentYearContentColor = streakColor,
                                selectedDayContainerColor = streakColor,
                                todayDateBorderColor = streakColor,
                                todayContentColor = streakColor,
                                selectedYearContainerColor = streakColor,
                                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedLabelColor = streakColor,
                                    focusedBorderColor = streakColor
                                )
                            )
                        )



                        Spacer(modifier = Modifier.size(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                showStartDatePicker = false
                                tempSelectedStartDate = null
                                confirmedStartDate = null
                                isToday = true
                            }) {
                                Text("Cancel", color = streakColor)
                            }

                            Spacer(modifier = Modifier.size(10.dp))

                            TextButton(onClick = {
                                confirmedStartDate = startDatePickerState.selectedDateMillis
                                tempSelectedStartDate = confirmedStartDate
                                confirmedStartDate?.let {
                                    startDate = Instant.ofEpochMilli(it)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showStartDatePicker = false
                            }) {
                                Text("OK", color = streakColor)
                            }
                        }
                    }

                    // Display selected date
                    confirmedStartDate?.let {
                        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                        val localDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        isTodayText = localDate.format(formatter)
                    }
                }

                // === END DATE PICKER ===
                if (!isEternity) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { showEndDatePicker = true }
                            .padding(16.dp)
                    ) {
                        val displayText = confirmedEndDate?.let {
                            "Tap to Pick another end date"
                        } ?: "Tap to select a end date"

                        Text(displayText)
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    if (showEndDatePicker) {
                        DatePicker(
                            state = endDatePickerState,
                            showModeToggle = true,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.White,
                                currentYearContentColor = streakColor,
                                selectedDayContainerColor = streakColor,
                                todayDateBorderColor = streakColor,
                                todayContentColor = streakColor,
                                selectedYearContainerColor = streakColor,
                                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedLabelColor = streakColor,
                                    focusedBorderColor = streakColor
                                )
                            )
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                showEndDatePicker = false
                                tempSelectedEndDate = null
                                confirmedEndDate = null
                                isEternity = true
                            }) {
                                Text("Cancel", color = streakColor)
                            }

                            Spacer(modifier = Modifier.size(10.dp))

                            TextButton(onClick = {
                                confirmedEndDate = endDatePickerState.selectedDateMillis
                                tempSelectedEndDate = confirmedEndDate
                                confirmedEndDate?.let {
                                    endDate = Instant.ofEpochMilli(it)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showEndDatePicker = false
                            }) {
                                Text("OK", color = streakColor)
                            }
                        }
                    }

                    confirmedEndDate?.let {
                        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                        val localDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        isEternityText = localDate.format(formatter)
                    }
                }

                // === CUSTOM TIME PICKER ===
                if (!isReminder) {
                    if (showBox) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F0F0))
                                .clickable {
                                    showTimePicker = true
                                }
                                .padding(16.dp)
                        ) {
                            val displayText = selectedTime?.let {
                                "Tap to Pick another reminder time"
                            } ?: "Tap to select a reminder time"

                            Text(displayText)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (showTimePicker) {
                        viewModel.CustomTimePicker(
                            initialHour = selectedTime?.hour ?: 3,
                            initialMinute = selectedTime?.minute ?: 24,
                            onCancel = {
                                if (selectedTime != null) showTimePicker = false else isReminder =
                                    true
                            },
                            streakColor = streakColor,
                            onSave = { hour, minute ->
                                tempTime = LocalTime.of(hour, minute)
                                selectedTime = tempTime
                                reminderText = if (DateFormat.is24HourFormat(activity)) {
                                    "Remind at ${selectedTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                                } else {
                                    "Remind at ${selectedTime!!.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                                }
                                showTimePicker = false
                                showBox = true
                            }
                        )

                        // === Reminder Type ===
                        Text("Reminder Type", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reminderType = NotificationType.DEFAULT }
                        ) {
                            RadioButton(
                                selected = reminderType == NotificationType.DEFAULT,
                                onClick = { reminderType = NotificationType.DEFAULT },
                                colors = RadioButtonDefaults.colors(selectedColor = streakColor)
                            )
                            if (reminderType == NotificationType.DEFAULT) Text(
                                "System Notification",
                                fontSize = 15.sp,
                                fontWeight = Bold
                            )
                            else Text("System Notification", fontSize = 15.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reminderType = NotificationType.ALARM }
                        ) {
                            RadioButton(
                                selected = reminderType == NotificationType.ALARM,
                                onClick = { reminderType = NotificationType.ALARM },
                                colors = RadioButtonDefaults.colors(selectedColor = streakColor)

                            )
                            if (reminderType == NotificationType.ALARM) Text(
                                "Alarm Notification",
                                fontSize = 15.sp,
                                fontWeight = Bold
                            )
                            else Text("Alarm Notification", fontSize = 15.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .clickable { reminderType = NotificationType.SILENT }
                        ) {
                            RadioButton(
                                selected = reminderType == NotificationType.SILENT,
                                onClick = { reminderType = NotificationType.SILENT },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = streakColor
                                )

                            )
                            if (reminderType == NotificationType.SILENT) Text(
                                "Silent Notification",
                                fontSize = 15.sp,
                                fontWeight = Bold
                            )
                            else Text("Silent Notification", fontSize = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }



                Spacer(modifier = Modifier.weight(1f))

                // === CREATE BUTTON ===
                Button(
                    onClick = {
                        viewModel.addStreak(
                            StreakModel(
                                streakName = streakName,
                                colorValue = streakColor.value.toLong(),
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate,
                                reminderTime = selectedTime,
                                notificationType = reminderType
                            )
                        )
                        val triggerAtMillis = LocalDateTime.of(LocalDate.now(), selectedTime)
                            .let { if (it.isBefore(LocalDateTime.now())) it.plusDays(1) else it }
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        notiViewModel.scheduleAlarm(activity, StreakModel(), triggerAtMillis)

                        activity.finish()
                    },
                    enabled = streakName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = streakColor

                    )
                ) {
                    Text("Create Streak", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }

            }
        }
    }
}









