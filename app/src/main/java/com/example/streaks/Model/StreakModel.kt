package com.example.streaks.Model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

import java.time.LocalDate

@Entity(tableName = "Streaks")
@TypeConverters(Converters::class)
data class StreakModel(

    @PrimaryKey(autoGenerate = true)
    val streakId: Int = 0,

    val streakName: String = "",

    // Store as Long in DB
    val colorValue: Long = Color.Blue.value.toLong(),

    val frequency: Frequency = Frequency.DAILY,

    val startDate: LocalDate = LocalDate.now(),

    val endDate: LocalDate = LocalDate.now().plusDays(30),

    val count: Int = 0
) {
    // Convert to Color when needed in UI
    val color: Color get() = Color(colorValue)

    companion object {
        fun fromUi(
            streakId: Int = 0,
            streakName: String = "",
            color: Color = Color.Blue,
            frequency: Frequency = Frequency.DAILY,
            startDate: LocalDate = LocalDate.now(),
            endDate: LocalDate = LocalDate.now().plusDays(30),
            count: Int = 0
        ) = StreakModel(
            streakId = streakId,
            streakName = streakName,
            colorValue = color.value.toLong(),
            frequency = frequency,
            startDate = startDate,
            endDate = endDate,
            count = count
        )
    }
}

enum class Frequency {
    DAILY, WEEKLY, MONTHLY
}

class Converters {

    // Frequency converters
    @TypeConverter
    fun fromFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter
    fun frequencyToString(frequency: Frequency): String = frequency.name

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: String): LocalDate = LocalDate.parse(date)

    @TypeConverter
    fun localDateToString(date: LocalDate): String = date.toString()
}



