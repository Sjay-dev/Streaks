package com.example.streaks.View.HomeScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class EditStreakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get streakId (passed from Home/Details)
        val streakId = intent.getIntExtra("streakId", -1)

        setContent {
            EditStreakScreen(streakId = streakId, onBack = { finish() })
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStreakScreen(
    streakId: Int,
    onBack: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(
        Color.Transparent,
        darkIcons = true
    )

    val viewModel: StreakViewModel = hiltViewModel()
    var streak by remember { mutableStateOf<StreakModel?>(null) }

    LaunchedEffect(streakId) {
        viewModel.getStreakById(streakId) {
            streak = it
        }
    }

    streak?.let { streakModel ->

        var streakName by remember { mutableStateOf(streakModel.streakName) }
        var streakColor by remember { mutableStateOf(Color(streakModel.streakColor.toULong())) }
        var frequency by remember { mutableStateOf(streakModel.frequency) }

        //=== Start Date values ===
        var startDate by remember { mutableStateOf(streakModel.startDate) }
        var showStartDatePicker by remember { mutableStateOf(false) }
        var isToday by remember { mutableStateOf(false) }
        var isTodayText by remember { mutableStateOf("Today?") }
        var tempSelectedStartDate by remember { mutableStateOf<Long?>(null) }
        var confirmedStartDate by remember {
            mutableStateOf<Long?>(
                startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
        val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = confirmedStartDate)

        //=== End Date values ===
        var endDate by remember { mutableStateOf(streakModel.endDate) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var isEternity by remember { mutableStateOf(false) }
        var isEternityText by remember { mutableStateOf("Externity?") }
        var confirmedEndDate by remember {
            mutableStateOf<Long?>(
                endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
        }
        var tempSelectedEndDate by remember { mutableStateOf<Long?>(null) }
        val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = confirmedEndDate)

        //=== Reminder values ===
        var isReminder by remember { mutableStateOf(false) }
        var reminderText by remember { mutableStateOf("No Reminder?") }
        var showTimePicker by remember { mutableStateOf(false) }
        var selectedTime by remember { mutableStateOf(streakModel.reminderTime) }
        var tempTime by remember { mutableStateOf<LocalTime?>(null) }
        var showBox by remember { mutableStateOf(true) }

        var reminderType by remember { mutableStateOf(streakModel.notificationType) }
        if (selectedTime == null){
            isReminder = true
            showTimePicker= true
            showBox = false
            reminderText = "No Reminder?"
        }
        else{
            reminderText = if (DateFormat.is24HourFormat(activity)) {
                "Remind at ${
                    selectedTime!!.format(
                        DateTimeFormatter.ofPattern("HH:mm"))}"
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


        val focusManager = LocalFocusManager.current

        val presetColors = listOf(Color.Red, Color.Blue, Color.Black, Color.Green, Color.Magenta)
        val frequencyOptions = Frequency.values()

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
                        Text("Edit Streak", fontWeight = Bold, fontSize = 23.sp)

                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
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

                    // === NAME ===
                    OutlinedTextField(
                        value = streakName,
                        onValueChange = { streakName = it },
                        label = { Text("Streak Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = streakColor,
                            focusedBorderColor = streakColor,
                            unfocusedBorderColor = streakColor,
                            unfocusedLabelColor = streakColor
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusManager.clearFocus() }
                    )

                    Spacer(modifier = Modifier.size(15.dp))

                    // === COLOR ===
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

                    // === FREQUENCY ===
                    Text("Frequency", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.size(5.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        frequencyOptions.forEach { option ->
                            Button(
                                onClick = { frequency = option },
                                modifier = Modifier.padding(end = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (frequency == option) streakColor else Color.LightGray,
                                    contentColor = Color.White
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
                        Text(
                            "Start $isTodayText",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
                        Text(
                            "Till $isEternityText",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
                                                    DateTimeFormatter.ofPattern("HH:mm"))}"
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
                                initialHour = selectedTime?.hour?: 3,
                                initialMinute = selectedTime?.minute ?: 24,
                                onCancel = {
                                    if (selectedTime != null) showTimePicker = false else isReminder = true
                                },
                                streakColor = streakColor,
                                onSave = { hour, minute ->
                                    tempTime = LocalTime.of(hour, minute)
                                    selectedTime = tempTime
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
                    LocalContext.current

                    // === EDIT BUTTON ===
                    Button(
                        onClick = {
                            viewModel.updateStreak(
                                streakModel.copy(
                                    streakName = streakName,
                                    streakColor = streakColor.value.toLong(),
                                    frequency = frequency,
                                    startDate = startDate,
                                    endDate = endDate,
                                    reminderTime = selectedTime,
                                    notificationType = reminderType
                                )
                            )
                                activity.finish()
                                  },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = streakColor

                        )
                    ) {
                        Text("Save", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }


                }
            }
        }
    }
}



