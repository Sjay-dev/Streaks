package com.example.streaks.View

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.streaks.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CreateStreakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            setContent {
CreateStreakScreen()
            }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStreakScreen(){

    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(
        Color.Transparent ,
        darkIcons = true
    )


    var streakName by remember { mutableStateOf("") }
    var streakColor by remember { mutableStateOf(Color.Blue) }

    // Start date state
    var showStartDatePicker by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }

    var isToday by remember { mutableStateOf(true) }
    var isTodayText by remember { mutableStateOf("Today?") }

    var confirmedStartDate by remember { mutableStateOf<Long?>(null) }
    var tempSelectedStartDate by remember { mutableStateOf<Long?>(null) }
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = tempSelectedStartDate)

    // End date state
    var showEndDatePicker by remember { mutableStateOf(true) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    var isEternity by remember { mutableStateOf(true) }
    var isEternityText by remember { mutableStateOf("Externity?") }

    var confirmedEndDate by remember { mutableStateOf<Long?>(null) }
    var tempSelectedEndDate by remember { mutableStateOf<Long?>(null) }
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = tempSelectedEndDate)

    val focusManager = LocalFocusManager.current

    // UI data
    val presetColors = listOf(Color.Red, Color.Blue, Color.Yellow, Color.Black, Color.Green, Color.Magenta)


    // Scaffold for consistent layout structure
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

                    IconButton(
                        onClick = {  },
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
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions { focusManager.clearFocus() }
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

                Spacer(modifier = Modifier.size(15.dp))

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

                Spacer(modifier = Modifier.weight(1f))

                // === CREATE BUTTON ===
                Button(
                    onClick = {},
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

