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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var isToday by remember { mutableStateOf(true) }
    var isTodayText by remember { mutableStateOf("Today?") }
    var tempSelectedStartDate by remember { mutableStateOf<Long?>(null) }
    rememberDatePickerState(initialSelectedDateMillis = tempSelectedStartDate)

    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var isEternity by remember { mutableStateOf(true) }
    var isEternityText by remember { mutableStateOf("Eternity?") }
    var tempSelectedEndDate by remember { mutableStateOf<Long?>(null) }
    rememberDatePickerState(initialSelectedDateMillis = tempSelectedEndDate)

    var isReminder by remember { mutableStateOf(true) }
    var reminderText by remember { mutableStateOf("No Reminder") }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val focusManager = LocalFocusManager.current

    val presetColors = listOf(Color.Red, Color.Blue, Color.Yellow, Color.Black, Color.Green, Color.Magenta)
    val frequencyOptions = Frequency.values()

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
                    Text(reminderText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isReminder,
                        onCheckedChange = {
                            isReminder = it
                            if (isReminder) {
                                reminderText = "No Reminder"
                                selectedTime = null
                            } else {
                                showTimePicker = true

                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color.Blue)
                    )
                }

                // === CUSTOM TIME PICKER DIALOG ===
                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {},
                        text = {
                            CustomTimePicker(
                                onCancel = { showTimePicker = false },
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

                // === CREATE BUTTON ===
                Button(
                    onClick = {
                        viewModel.addStreak(
                            StreakModel(
                                streakName = streakName,
                                colorValue = streakColor.value.toLong(),
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate
                            )
                        )
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
    onCancel: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    val hours = if (is24Hour) (0..23).toList() else (1..12).toList()
    val minutes = (0..59).toList()

    val hourState = rememberLazyListState()
    val minuteState = rememberLazyListState()

    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoopingNumberPicker(hours, hourState) { selectedHour = it }
            Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            LoopingNumberPicker(minutes, minuteState) { selectedMinute = it }

            if (!is24Hour) {
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = { isAm = true }) {
                        Text("AM", fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(onClick = { isAm = false }) {
                        Text("PM", fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Today - $formattedDate", color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onCancel() }) {
                Text("Cancel", fontSize = 18.sp)
            }
            TextButton(onClick = {
                val finalHour =
                    if (is24Hour) selectedHour
                    else if (isAm) selectedHour % 12
                    else (selectedHour % 12) + 12
                onSave(finalHour, selectedMinute)
            }) {
                Text("Save", fontSize = 18.sp)
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

    LaunchedEffect(Unit) { state.scrollToItem(middleIndex) }

    Box(
        modifier = Modifier
            .height(120.dp)
            .width(80.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(state = state, horizontalAlignment = Alignment.CenterHorizontally) {
            items(repeatedItems.size) { index ->
                val value = repeatedItems[index]
                val isSelected = state.firstVisibleItemIndex == index
                Text(
                    text = String.format("%02d", value),
                    fontSize = if (isSelected) 40.sp else 24.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex) {
        val actualValue = repeatedItems[state.firstVisibleItemIndex]
        onValueChange(actualValue)
    }
}





