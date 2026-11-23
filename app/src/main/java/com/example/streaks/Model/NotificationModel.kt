package com.example.streaks.Model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Notifications",
    indices = [Index(value = ["reminderId", "scheduledForMillis"], unique = true)]
)
data class NotificationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val streakId: Int,
    val streakName: String,
    val streakColor: Long = Color.Blue.value.toLong(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: Status,
    val frequency: Frequency,
    val reminderId: Int = 0,
    val scheduledForMillis: Long = timestamp,
    val outcome: ReminderOutcome = ReminderOutcome.POSTED
)

enum class Status{
      Cancelled,OnGoing
}

enum class ReminderOutcome {
    POSTED,
    BLOCKED_PERMISSION,
    BLOCKED_CHANNEL,
    SKIPPED
}

