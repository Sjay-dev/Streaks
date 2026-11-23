package com.example.streaks.View.Components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class InlineEditor { NONE, DATE, TIME }

@Composable
fun InlineDateTimePicker(
    date: LocalDate,
    time: LocalTime?,
    expanded: InlineEditor,
    onExpandedChange: (InlineEditor) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    allowUnknownTime: Boolean = false,
    maxDate: LocalDate? = null,
    minDate: LocalDate? = null
) {
    val dateText = date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val timeText = time?.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a")) ?: "Add start time"
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { onExpandedChange(if (expanded == InlineEditor.DATE) InlineEditor.NONE else InlineEditor.DATE) },
            shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp
        ) { Text(dateText, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium) }
        if (expanded == InlineEditor.DATE) {
            InlineCalendar(date, onDateChange, maxDate, minDate)
        }
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { onExpandedChange(if (expanded == InlineEditor.TIME) InlineEditor.NONE else InlineEditor.TIME) },
            shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp
        ) { Text(timeText, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium) }
        if (expanded == InlineEditor.TIME) {
            InlineTimeWheel(time ?: LocalTime.now().withSecond(0).withNano(0), is24Hour, onTimeChange)
            if (allowUnknownTime && time != null) TextButton(onClick = { onTimeChange(null) }) { Text("Remove start time") }
        }
    }
}

@Composable
private fun InlineCalendar(
    selected: LocalDate,
    onSelected: (LocalDate) -> Unit,
    maxDate: LocalDate?,
    minDate: LocalDate?
) {
    var month by remember(selected) { mutableStateOf(YearMonth.from(selected)) }
    var yearText by remember(month) { mutableStateOf(month.year.toString()) }
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month") }
            Text(month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = yearText,
                onValueChange = { raw ->
                    yearText = raw.filter(Char::isDigit).take(4)
                    yearText.toIntOrNull()?.takeIf { it in 1900..2200 }?.let { month = month.withYear(it) }
                },
                modifier = Modifier.size(width = 92.dp, height = 56.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Year") }
            )
            IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month") }
        }
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.entries.drop(1).plus(DayOfWeek.SUNDAY).forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(day.getDisplayName(TextStyle.NARROW, Locale.getDefault())) }
            }
        }
        val leading = month.atDay(1).dayOfWeek.value - 1
        val cells = leading + month.lengthOfMonth()
        repeat((cells + 6) / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val day = week * 7 + column - leading + 1
                    if (day !in 1..month.lengthOfMonth()) Box(Modifier.weight(1f).height(44.dp))
                    else {
                        val candidate = month.atDay(day)
                        val enabled = (minDate == null || !candidate.isBefore(minDate)) && (maxDate == null || !candidate.isAfter(maxDate))
                        val selectedDay = candidate == selected
                        Box(
                            Modifier.weight(1f).height(44.dp)
                                .background(if (selectedDay) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(50))
                                .clickable(enabled = enabled, role = Role.Button) { onSelected(candidate) },
                            contentAlignment = Alignment.Center
                        ) { Text(day.toString(), color = when { selectedDay -> MaterialTheme.colorScheme.onPrimary; enabled -> MaterialTheme.colorScheme.onSurface; else -> MaterialTheme.colorScheme.onSurface.copy(alpha = .35f) }) }
                    }
                }
            }
        }
        TextButton(onClick = { val today = LocalDate.now(); month = YearMonth.from(today); if ((minDate == null || !today.isBefore(minDate)) && (maxDate == null || !today.isAfter(maxDate))) onSelected(today) }) { Text("Today") }
    }
}

@Composable
fun InlineTimeWheel(value: LocalTime, is24Hour: Boolean, onChange: (LocalTime) -> Unit) {
    val displayHour = if (is24Hour) value.hour else ((value.hour + 11) % 12) + 1
    var hour by remember(value) { mutableIntStateOf(displayHour) }
    var minute by remember(value) { mutableIntStateOf(value.minute) }
    var pm by remember(value) { mutableStateOf(value.hour >= 12) }
    fun commit(h: Int = hour, m: Int = minute, isPm: Boolean = pm) {
        hour = h; minute = m; pm = isPm
        val hour24 = if (is24Hour) h else (h % 12) + if (isPm) 12 else 0
        onChange(LocalTime.of(hour24.coerceIn(0, 23), m.coerceIn(0, 59)))
    }
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Wheel((if (is24Hour) 0..23 else 1..12).toList(), hour, "Hour") { commit(h = it) }
                Text(":", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Wheel((0..59).toList(), minute, "Minute") { commit(m = it) }
                if (!is24Hour) {
                    Column {
                        PeriodButton("AM", selected = !pm) { commit(isPm = false) }
                        PeriodButton("PM", selected = pm) { commit(isPm = true) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hour.toString().padStart(2, '0'), onValueChange = { it.toIntOrNull()?.let { n -> if (n in (if (is24Hour) 0..23 else 1..12)) commit(h = n) } },
                    modifier = Modifier.size(width = 88.dp, height = 58.dp), label = { Text("Hour") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = minute.toString().padStart(2, '0'), onValueChange = { it.toIntOrNull()?.let { n -> if (n in 0..59) commit(m = n) } },
                    modifier = Modifier.size(width = 88.dp, height = 58.dp), label = { Text("Minute") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
private fun PeriodButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun Wheel(values: List<Int>, selected: Int, label: String, onSelected: (Int) -> Unit) {
    // The leading spacer means firstVisible=N places values[N] in the center row.
    val state = rememberLazyListState(initialFirstVisibleItemIndex = values.indexOf(selected).coerceAtLeast(0))
    val fling = rememberSnapFlingBehavior(state)
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) values.getOrNull(state.firstVisibleItemIndex)?.let(onSelected)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onSelected(values[(values.indexOf(selected) + 1).coerceAtMost(values.lastIndex)]) }) { Icon(Icons.Default.Add, "Increase $label") }
        Box(Modifier.size(width = 78.dp, height = 144.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth().height(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), RoundedCornerShape(12.dp)))
            LazyColumn(state = state, flingBehavior = fling, modifier = Modifier.height(144.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                item { Box(Modifier.height(48.dp)) }
                items(values) { n ->
                    Box(Modifier.height(48.dp).fillMaxWidth().clickable { onSelected(n) }.semantics { contentDescription = "$label $n" }, contentAlignment = Alignment.Center) {
                        Text(n.toString().padStart(2, '0'), fontSize = 25.sp, fontWeight = if (n == selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                item { Box(Modifier.height(48.dp)) }
            }
        }
        IconButton(onClick = { onSelected(values[(values.indexOf(selected) - 1).coerceAtLeast(0)]) }) { Text("-", modifier = Modifier.semantics { contentDescription = "Decrease $label" }) }
    }
}
