package com.example.streaks.Model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "Streaks")
@TypeConverters(Converters::class)
data class StreakModel(

    @PrimaryKey(autoGenerate = true)
    val streakId: Int = 0,

    val streakName: String = "",

    val colorValue: Long = Color.Blue.value.toLong(),

    val frequency: Frequency = Frequency.DAILY,

    val startDate: LocalDate = LocalDate.now(),

    val endDate: LocalDate = LocalDate.now().plusDays(30),

    val count: Int = 0 ,
    val reminderTime: LocalTime? = null

)

enum class Frequency {
    DAILY, WEEKLY, MONTHLY
}

class Converters {

    @TypeConverter
    fun fromColor(color: Color): Long = color.value.toLong()

    @TypeConverter
    fun toColor(value: Long): Color = Color(value.toULong())

    // Optional: LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? =
        timeString?.let { LocalTime.parse(it) }

}








