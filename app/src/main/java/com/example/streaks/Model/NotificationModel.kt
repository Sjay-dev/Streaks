package com.example.streaks.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.time.LocalTime

@Entity(tableName = "Notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val streakId: Int,
    val streakName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: Status,
    val frequency: Frequency
)

enum class Status{
      Cancelled,OnGoing
}

