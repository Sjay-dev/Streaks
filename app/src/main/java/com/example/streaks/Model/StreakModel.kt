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
    val streakId : Int,

    val streakName: String,

    val color: Long,

    val frequency : Frequency,

    val startDate: LocalDate ,

    val endDate: LocalDate ,

    val count: Int
)
enum class Frequency{
    DAILY , WEEKLY , MONTHLY
}



class Converters {

    @TypeConverter
    fun fromColor(color: Color): Long = color.value.toLong()

    @TypeConverter
    fun toColor(value: Long): Color = Color(value)

    @TypeConverter
    fun fromFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter
    fun frequencyToString(frequency: Frequency): String = frequency.name

    @TypeConverter
    fun fromLocalDate(date: String): LocalDate = LocalDate.parse(date)

    @TypeConverter
    fun localDateToString(date: LocalDate): String = date.toString()
}



