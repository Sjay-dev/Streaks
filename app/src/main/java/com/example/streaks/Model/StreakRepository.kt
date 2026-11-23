package com.example.streaks.Model

import androidx.room.withTransaction
import com.example.streaks.Model.DataBase.NotificationDao
import com.example.streaks.Model.DataBase.StreakDao
import com.example.streaks.Model.DataBase.StreakDataBase
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import javax.inject.Inject

data class SetbackResult(
    val attemptId: Long,
    val streakId: Int,
    val previousRawStart: Long,
    val previousEffectiveStart: Long,
    val nextStart: Long
)

class StreakRepository @Inject constructor(
    private val dao: StreakDao,
    private val notificationDao: NotificationDao,
    private val database: StreakDataBase
) {
    suspend fun insertStreak(streak: StreakModel): Int = dao.addStreak(streak).toInt()
    suspend fun updateStreak(streak: StreakModel) = dao.updateStreak(streak)
    fun getAllStreaks(): Flow<List<StreakModel>> = dao.getAllStreaks()
    suspend fun getStreakById(id: Int): StreakModel? = dao.getStreakById(id)
    fun observeStreak(id: Int): Flow<StreakModel?> = dao.observeStreak(id)
    fun observeAttempts(id: Int): Flow<List<AttemptModel>> = dao.observeAttempts(id)
    fun observeReminders(): Flow<List<ReminderSchedule>> = dao.observeReminders()
    suspend fun getReminder(id: Int): ReminderSchedule? = dao.getReminder(id)
    suspend fun enabledReminders(): List<ReminderSchedule> = dao.enabledReminders()
    suspend fun remindersForGoal(id: Int): List<ReminderSchedule> = dao.remindersForGoal(id)
    suspend fun saveReminder(reminder: ReminderSchedule): Int = dao.upsertReminder(reminder).toInt()
    suspend fun updateReminder(reminder: ReminderSchedule) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderSchedule) = dao.deleteReminder(reminder)
    suspend fun getAllWithReminders(): List<StreakModel> = dao.getAllWithReminders()

    suspend fun deleteStreak(streak: StreakModel) = database.withTransaction {
        dao.deleteAttemptsForStreak(streak.streakId)
        dao.deleteUrgesForStreak(streak.streakId)
        dao.deleteRemindersForGoal(streak.streakId)
        dao.deleteStreak(streak)
        notificationDao.deleteForStreak(streak.streakId)
    }

    suspend fun archiveStreak(streakId: Int, now: Long = System.currentTimeMillis()) {
        database.withTransaction {
            dao.getStreakById(streakId)?.let { dao.updateStreak(it.copy(archivedAtMillis = now, reminderTime = null)) }
            dao.deleteRemindersForGoal(streakId)
        }
    }

    suspend fun archiveStreaks(streakIds: Set<Int>, now: Long = System.currentTimeMillis()) {
        if (streakIds.isEmpty()) return
        database.withTransaction {
            streakIds.forEach { streakId ->
                dao.getStreakById(streakId)?.takeIf { it.archivedAtMillis == null }?.let {
                    dao.updateStreak(it.copy(archivedAtMillis = now, reminderTime = null))
                }
                dao.deleteRemindersForGoal(streakId)
            }
        }
    }

    suspend fun deleteStreaks(streakIds: Set<Int>) {
        if (streakIds.isEmpty()) return
        database.withTransaction {
            streakIds.forEach { streakId ->
                dao.getStreakById(streakId)?.let { streak ->
                    dao.deleteAttemptsForStreak(streakId)
                    dao.deleteUrgesForStreak(streakId)
                    dao.deleteRemindersForGoal(streakId)
                    dao.deleteStreak(streak)
                    notificationDao.deleteForStreak(streakId)
                }
            }
        }
    }

    suspend fun recordSetback(streakId: Int, atMillis: Long, note: String): SetbackResult? =
        database.withTransaction {
            val streak = dao.getStreakById(streakId) ?: return@withTransaction null
            val rawStart = streak.currentAttemptStartedAtMillis
            val effectiveStart = streak.effectiveAttemptStartMillis()
            if (streak.archivedAtMillis != null || atMillis < effectiveStart) return@withTransaction null
            if (dao.hasAttemptAt(streakId, atMillis) > 0) return@withTransaction null
            // Conditional update makes a second rapid confirmation a no-op.
            if (dao.moveAttemptStart(streakId, rawStart, atMillis) != 1) return@withTransaction null
            val attemptId = dao.insertAttempt(
                AttemptModel(
                    streakId = streakId,
                    startedAtMillis = effectiveStart.takeIf { rawStart > 0 } ?: 0,
                    startedOnDate = streak.startDate.takeIf { rawStart == 0L },
                    endedAtMillis = atMillis,
                    note = note.trim()
                )
            )
            SetbackResult(attemptId, streakId, rawStart, effectiveStart, atMillis)
        }

    suspend fun undoSetback(result: SetbackResult): Boolean = database.withTransaction {
        if (dao.restoreAttemptStart(result.streakId, result.nextStart, result.previousRawStart) != 1) {
            return@withTransaction false
        }
        dao.deleteAttempt(result.attemptId)
        true
    }

    suspend fun recordUrge(streakId: Int, trigger: String, whatHelped: String) {
        dao.insertUrgeEvent(UrgeEventModel(streakId = streakId, trigger = trigger.trim(), whatHelped = whatHelped.trim()))
    }

    suspend fun clearAll() = database.withTransaction {
        notificationDao.clearAll()
        dao.clearAttempts()
        dao.clearUrgeEvents()
        dao.clearReminders()
        dao.clearStreaks()
    }
}

fun StreakModel.effectiveAttemptStartMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    currentAttemptStartedAtMillis.takeIf { it > 0 }
        ?: startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
