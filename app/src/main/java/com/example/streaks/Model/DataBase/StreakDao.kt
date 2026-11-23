package com.example.streaks.Model.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.UrgeEventModel
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStreak(streak: StreakModel): Long

    @Update suspend fun updateStreak(streak: StreakModel)
    @Delete suspend fun deleteStreak(streak: StreakModel)

    @Query("SELECT * FROM Streaks ORDER BY archivedAtMillis IS NOT NULL, streakId DESC")
    fun getAllStreaks(): Flow<List<StreakModel>>

    @Query("SELECT * FROM Streaks WHERE streakId = :id")
    suspend fun getStreakById(id: Int): StreakModel?

    @Query("SELECT * FROM Streaks WHERE streakId = :id")
    fun observeStreak(id: Int): Flow<StreakModel?>

    @Query("SELECT * FROM Streaks WHERE reminderTime IS NOT NULL AND archivedAtMillis IS NULL")
    suspend fun getAllWithReminders(): List<StreakModel>

    @Query("UPDATE Streaks SET reminderTime = NULL WHERE reminderTime IS NOT NULL")
    suspend fun clearAllReminders()

    @Query("UPDATE Streaks SET currentAttemptStartedAtMillis = :nextStart WHERE streakId = :id AND currentAttemptStartedAtMillis = :expectedStart AND archivedAtMillis IS NULL")
    suspend fun moveAttemptStart(id: Int, expectedStart: Long, nextStart: Long): Int

    @Query("UPDATE Streaks SET currentAttemptStartedAtMillis = :previousStart WHERE streakId = :id AND currentAttemptStartedAtMillis = :expectedStart")
    suspend fun restoreAttemptStart(id: Int, expectedStart: Long, previousStart: Long): Int

    @Insert suspend fun insertAttempt(attempt: AttemptModel): Long
    @Query("SELECT COUNT(*) FROM Attempts WHERE streakId = :streakId AND endedAtMillis = :endedAtMillis")
    suspend fun hasAttemptAt(streakId: Int, endedAtMillis: Long): Int
    @Query("DELETE FROM Attempts WHERE attemptId = :attemptId") suspend fun deleteAttempt(attemptId: Long)
    @Query("DELETE FROM Attempts WHERE streakId = :streakId") suspend fun deleteAttemptsForStreak(streakId: Int)
    @Query("SELECT * FROM Attempts WHERE streakId = :streakId ORDER BY endedAtMillis DESC")
    fun observeAttempts(streakId: Int): Flow<List<AttemptModel>>

    @Insert suspend fun insertUrgeEvent(event: UrgeEventModel): Long
    @Query("DELETE FROM UrgeEvents WHERE streakId = :streakId") suspend fun deleteUrgesForStreak(streakId: Int)

    @Query("DELETE FROM Attempts") suspend fun clearAttempts()
    @Query("DELETE FROM UrgeEvents") suspend fun clearUrgeEvents()
    @Query("DELETE FROM Streaks") suspend fun clearStreaks()

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertReminder(reminder: ReminderSchedule): Long
    @Update suspend fun updateReminder(reminder: ReminderSchedule)
    @Delete suspend fun deleteReminder(reminder: ReminderSchedule)
    @Query("SELECT * FROM ReminderSchedules ORDER BY time") fun observeReminders(): Flow<List<ReminderSchedule>>
    @Query("SELECT * FROM ReminderSchedules WHERE reminderId = :id") suspend fun getReminder(id: Int): ReminderSchedule?
    @Query("SELECT * FROM ReminderSchedules WHERE streakId = :streakId") suspend fun remindersForGoal(streakId: Int): List<ReminderSchedule>
    @Query("SELECT * FROM ReminderSchedules WHERE enabled = 1") suspend fun enabledReminders(): List<ReminderSchedule>
    @Query("DELETE FROM ReminderSchedules WHERE streakId = :streakId") suspend fun deleteRemindersForGoal(streakId: Int)
    @Query("DELETE FROM ReminderSchedules") suspend fun clearReminders()
}
