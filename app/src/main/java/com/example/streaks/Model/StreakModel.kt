package com.example.streaks.Model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.LocalTime

/**
 * The original v1 columns remain intact so existing installations migrate without
 * reinterpreting an ended goal as a setback. New elapsed-time data is stored as an instant.
 */
@Entity(tableName = "Streaks")
@TypeConverters(Converters::class)
data class StreakModel(
    @PrimaryKey(autoGenerate = true) val streakId: Int = 0,
    val streakName: String = "",
    val streakColor: Long = Color.Blue.value.toLong(),
    val frequency: Frequency = Frequency.DAILY,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val count: Int = 0,
    val reminderTime: LocalTime? = null,
    val notificationType: NotificationType = NotificationType.DEFAULT,
    val whyQuitting: String = "",
    val urgeAction: String = "",
    val currentAttemptStartedAtMillis: Long = 0L,
    val archivedAtMillis: Long? = null,
    /** Existing goals migrate as explicit colors; newly-created goals inherit the app accent. */
    val usesAppAccent: Boolean = true
)

@Entity(tableName = "ReminderSchedules", indices = [Index("streakId")])
data class ReminderSchedule(
    @PrimaryKey(autoGenerate = true) val reminderId: Int = 0,
    val streakId: Int,
    val time: LocalTime = LocalTime.of(20, 30),
    /** Bit zero is Sunday, bit six is Saturday. */
    val repeatDays: Int = 0b1111111,
    val enabled: Boolean = true,
    val destination: ReminderDestination = ReminderDestination.URGE_PLAN,
    val message: String = "A moment for yourself.",
    val sound: ReminderSound = ReminderSound.PHONE_DEFAULT,
    val vibration: Boolean = true
)

@Entity(tableName = "Attempts", indices = [Index("streakId"), Index(value = ["streakId", "endedAtMillis"], unique = true)])
data class AttemptModel(
    @PrimaryKey(autoGenerate = true) val attemptId: Long = 0,
    val streakId: Int,
    val startedAtMillis: Long,
    val startedOnDate: LocalDate? = null,
    val endedAtMillis: Long,
    val note: String = "",
    val recordedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "UrgeEvents", indices = [Index("streakId")])
data class UrgeEventModel(
    @PrimaryKey(autoGenerate = true) val urgeEventId: Long = 0,
    val streakId: Int,
    val trigger: String = "",
    val whatHelped: String = "",
    val recordedAtMillis: Long = System.currentTimeMillis()
)

enum class Frequency { DAILY, WEEKLY, MONTHLY }
enum class NotificationType { DEFAULT, SILENT, ALARM }
enum class ReminderDestination { GOAL_DETAILS, URGE_PLAN }
enum class ReminderSound { PHONE_DEFAULT, SOFT_CHIME, BELL, GENTLE_PULSE, SILENT }

class Converters {
    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun fromLocalTime(time: LocalTime?): String? = time?.toString()
    @TypeConverter fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
