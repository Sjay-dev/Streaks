package com.example.streaks.Model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime

@Entity(tableName = "Notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streakId: Int,
    val streakName: String,
    val message: String,
    val time: LocalDateTime,
    val status: String
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}
