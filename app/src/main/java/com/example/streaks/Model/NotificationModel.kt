package com.example.streaks.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_notifications")
data class NotificationModel(

    @PrimaryKey(autoGenerate = true)
    val streakId: Int = 0,
    val streakName: String,
    val message: String,
    val timestamp: Long ,
    val dueTime: Long,
    val clicked: Boolean = false,
    val status: StreakStatus = StreakStatus.ONGOING
)

enum class StreakStatus {
    ONGOING,
    ENDED,
    COMPLETED // optional if you want a third state (user acted on time)
}

