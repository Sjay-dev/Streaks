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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.Frequency
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

    // === ViewModel ===
    val viewModel: StreakViewModel = hiltViewModel()

    // === States ===
    var streakName by remember { mutableStateOf("") }
    var streakColor by remember { mutableStateOf(Color.Blue) }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }

    // Start date state

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
    var reminderText by remember { mutableStateOf("No Reminder") }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var tempTime by remember { mutableStateOf<LocalTime?>(null) }

    var reminderType by remember { mutableStateOf("Notification") }
    var soundAndVibration by remember { mutableStateOf(false) }


    //===KEYBOARD focusManager===
    val focusManager = LocalFocusManager.current

    //===PRESET COLORS===
    val presetColors = listOf(Color.Red, Color.Blue, Color.Yellow, Color.Black, Color.Green, Color.Magenta)
    val frequencyOptions = Frequency.values()

    //===UI===//
    Scaffold(
        topBar = {
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Create A New Streak", fontWeight = FontWeight.Bold, fontSize = 23.sp)
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
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Blue,
                        focusedBorderColor = Color.Blue
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
                                containerColor = if (frequency == option) Color.Blue else Color.LightGray,
                                contentColor = if (frequency == option) Color.White else Color.Black
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
                        colors = SwitchDefaults.colors(checkedTrackColor = Color.Blue)
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
                        colors = SwitchDefaults.colors(checkedTrackColor = Color.Blue)
                    )
                }

                // === REMINDER PICKER ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminder", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isReminder,
                        onCheckedChange = {
                            isReminder = it
                            if (!isReminder) {
                                selectedTime = null
                                reminderText = "No Reminder"
                                reminderType = "Notification"
                                soundAndVibration = false
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color.Blue)
                    )
                }

                if (!isReminder) {
                    Spacer(Modifier.height(12.dp))

                    // Reminder Time
                    Text("Reminder Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    CustomTimePicker(
                        initialHour = selectedTime?.hour ?: LocalTime.now().hour,
                        initialMinute = selectedTime?.minute ?: LocalTime.now().minute,
                        onCancel = {
                            isReminder = false
                            selectedTime = null
                            reminderText = "No Reminder"
                        },
                        onSave = { hour, minute ->
                            selectedTime = LocalTime.of(hour, minute)
                            reminderText = if (DateFormat.is24HourFormat(activity)) {
                                "Remind @ ${selectedTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                            } else {
                                "Remind @ ${selectedTime!!.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Reminder Type
                    Text("Reminder Type", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // Notification (default)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = reminderType == "Notification",
                            onClick = { reminderType = "Notification" }
                        )
                        Text("Notification")
                    }

                    // Sound + Vibration toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = reminderType == "SoundVibration",
                            onClick = { reminderType = "SoundVibration" }
                        )
                        Text("Sound + Vibration", modifier = Modifier.weight(1f))
                        Switch(
                            checked = soundAndVibration,
                            onCheckedChange = {
                                reminderType = "SoundVibration"
                                soundAndVibration = it
                            }
                        )
                    }

                    // Silent notification
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = reminderType == "Silent",
                            onClick = { reminderType = "Silent" }
                        )
                        Text("Silent Notification")
                    }
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
                                currentYearContentColor = Color.Blue,
                                selectedDayContainerColor = Color.Blue,
                                todayDateBorderColor = Color.Blue,
                                todayContentColor = Color.Blue,
                                selectedYearContainerColor = Color.Blue,
                                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedLabelColor = Color.Blue,
                                    focusedBorderColor = Color.Blue
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
                                Text("Cancel")
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
                                Text("OK")
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
                                currentYearContentColor = Color.Blue,
                                selectedDayContainerColor = Color.Blue,
                                todayDateBorderColor = Color.Blue,
                                todayContentColor = Color.Blue,
                                selectedYearContainerColor = Color.Blue,
                                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedLabelColor = Color.Blue,
                                    focusedBorderColor = Color.Blue
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
                                Text("Cancel")
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
                                Text("OK")
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

                // === CUSTOM TIME PICKER DIALOG ===
                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = {
                            showTimePicker = false
                            isReminder = false // reset switch if dismissed
                        },
                        confirmButton = {},
                        text = {
                            CustomTimePicker(
                                initialHour = tempTime?.hour ?: LocalTime.now().hour,
                                initialMinute = tempTime?.minute ?: LocalTime.now().minute,
                                onCancel = {
                                    showTimePicker = false
                                    isReminder = false // reset switch on cancel
                                },
                                onSave = { hour, minute ->
                                    selectedTime = LocalTime.of(hour, minute)
                                    reminderText = if (DateFormat.is24HourFormat(activity)) {
                                        "Remind @ ${selectedTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                                    } else {
                                        "Remind @ ${selectedTime!!.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                                    }
                                    showTimePicker = false
                                }
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                val context = LocalContext.current

                // === CREATE BUTTON ===
                Button(
                    onClick = {
                        viewModel.addStreak(
                            StreakModel(
                                streakName = streakName,
                                colorValue = streakColor.value.toLong(),
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate ,
                                reminderTime = selectedTime
                            )
                        )
                        viewModel.scheduleNotification(context , 0 , streakName , selectedTime!! )
                        activity.finish()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Create Streak", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ========================= CUSTOM TIME PICKER =========================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onCancel: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var isAm by remember { mutableStateOf(true) }

    val hours = if (is24Hour) (0..23).toList() else (1..12).toList()
    val minutes = (0..59).toList()

    val hourState = rememberLazyListState(initialHour)
    val minuteState = rememberLazyListState(initialMinute)

    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            "Select Time",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3) // Blue
        )

        Spacer(Modifier.height(20.dp))

        // Time pickers row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoopingNumberPicker(hours, hourState) { selectedHour = it }
            Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
            LoopingNumberPicker(minutes, minuteState) { selectedMinute = it }

            if (!is24Hour) {
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = { isAm = true }) {
                        Text(
                            "AM",
                            fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAm) Color(0xFF2196F3) else Color.Gray
                        )
                    }
                    TextButton(onClick = { isAm = false }) {
                        Text(
                            "PM",
                            fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isAm) Color(0xFF2196F3) else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Today - $formattedDate", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onCancel() }) {
                Text("Cancel", fontSize = 16.sp, color = Color.Gray)
            }
            Button(
                onClick = {
                    val finalHour =
                        if (is24Hour) selectedHour
                        else if (isAm) selectedHour % 12
                        else (selectedHour % 12) + 12
                    onSave(finalHour, selectedMinute)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Save", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun LoopingNumberPicker(
    items: List<Int>,
    state: LazyListState,
    onValueChange: (Int) -> Unit
) {
    val repeatedItems = List(1000) { index -> items[index % items.size] }
    val middleIndex = repeatedItems.size / 2

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    LaunchedEffect(Unit) { state.scrollToItem(middleIndex) }

    Box(
        modifier = Modifier
            .height(140.dp)
            .width(80.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = state,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(repeatedItems.size) { index ->
                val value = repeatedItems[index]
                val isSelected = state.firstVisibleItemIndex == index
                Text(
                    text = String.format("%02d", value),
                    fontSize = if (isSelected) 36.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF2196F3) else Color.Gray,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex) {
        val actualValue = repeatedItems[state.firstVisibleItemIndex]
        onValueChange(actualValue)
    }
}






