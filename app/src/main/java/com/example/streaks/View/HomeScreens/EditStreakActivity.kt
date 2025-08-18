package com.example.streaks.View.HomeScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
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
        var streakColor by remember { mutableStateOf(Color(streakModel.colorValue.toULong())) }
        var frequency by remember { mutableStateOf(streakModel.frequency) }

        // Start & End Dates
        var startDate by remember { mutableStateOf(streakModel.startDate) }
        var endDate by remember { mutableStateOf(streakModel.endDate) }

        var confirmedStartDate by remember { mutableStateOf<Long?>(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
        var confirmedEndDate by remember { mutableStateOf<Long?>(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }

        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }

        val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = confirmedStartDate)
        val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = confirmedEndDate)

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
                        Text("Edit Streak", fontWeight = FontWeight.Bold, fontSize = 23.sp)

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
                                    containerColor = if (frequency == option) Color.Blue else Color.LightGray,
                                    contentColor = if (frequency == option) Color.White else Color.Black
                                )
                            ) {
                                Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(15.dp))

                    // === START DATE ===
                    Text("Start Date", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { showStartDatePicker = true }
                            .padding(16.dp)
                    ) {
                        Text(startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                    }

                    if (showStartDatePicker) {
                        DatePicker(state = startDatePickerState)
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showStartDatePicker = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            TextButton(onClick = {
                                confirmedStartDate = startDatePickerState.selectedDateMillis
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

                    Spacer(modifier = Modifier.size(15.dp))

                    // === END DATE ===
                    Text("End Date", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { showEndDatePicker = true }
                            .padding(16.dp)
                    ) {
                        Text(endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                    }

                    if (showEndDatePicker) {
                        DatePicker(state = endDatePickerState)
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showEndDatePicker = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            TextButton(onClick = {
                                confirmedEndDate = endDatePickerState.selectedDateMillis
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

                    Spacer(modifier = Modifier.weight(1f))

                    // === SAVE BUTTON ===
                    Button(
                        onClick = {
                            viewModel.updateStreak(
                                streakModel.copy(
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
                        Text("Save Changes", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
