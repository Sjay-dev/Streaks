package com.example.streaks.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val streakId: Int,                // reference to streak
    val streakName: String,           // name of the streak
    val message: String,              // notification message
    val timestamp: Long = System.currentTimeMillis(), // when sent

    val dueTime: Long,                // ⏰ deadline by which user should act
    val clicked: Boolean = false,     // ✅ did the user tap/click the notification?
    val status: StreakStatus = StreakStatus.ONGOING // 🚦 ongoing/ended
)

enum class StreakStatus {
    ONGOING,
    ENDED,
    COMPLETED // optional if you want a third state (user acted on time)
}

