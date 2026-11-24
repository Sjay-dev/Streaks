package com.example.streaks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.streaks.Model.DataBase.StreakDataBase
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.ReminderOutcome
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.Status
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.StreakRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databases = mutableListOf<StreakDataBase>()
    private val names = mutableListOf<String>()

    @After fun tearDown() {
        databases.forEach { it.close() }
        names.forEach(context::deleteDatabase)
    }

    @Test fun populatedV1Database_survivesMigration() = runBlocking {
        val name = "migration-${System.nanoTime()}.db".also(names::add)
        createV1Database(name)
        val db = Room.databaseBuilder(context, StreakDataBase::class.java, name)
            .addMigrations(StreakDataBase.MIGRATION_1_2, StreakDataBase.MIGRATION_2_3, StreakDataBase.MIGRATION_3_4).build().also(databases::add)
        val goals = db.streakDao().getAllStreaks().first()

        assertEquals(3, goals.size)
        val active = goals.single { it.streakName == "Active" }
        val ended = goals.single { it.streakName == "Ended" }
        val noReminder = goals.single { it.streakName == "No reminder" }
        assertNull(active.reminderTime)
        assertNull(active.archivedAtMillis)
        assertEquals(0L, ended.archivedAtMillis)
        assertEquals(LocalDate.parse("2025-01-05"), ended.endDate)
        assertNull(noReminder.reminderTime)
        assertEquals("", active.whyQuitting)
        assertFalse(active.usesAppAccent)
        val migratedReminder = db.streakDao().observeReminders().first().single()
        assertEquals(active.streakId, migratedReminder.streakId)
        assertEquals("20:30", migratedReminder.time.toString())
        assertEquals(127, migratedReminder.repeatDays)
        assertTrue(migratedReminder.enabled)

        val repository = StreakRepository(db.streakDao(), db.notificationDao(), db)
        val setbackAt = Instant.parse("2026-02-01T12:00:00Z").toEpochMilli()
        assertTrue(repository.recordSetback(active.streakId, setbackAt, "") != null)
        val migratedAttempt = repository.observeAttempts(active.streakId).first().single()
        assertEquals(0L, migratedAttempt.startedAtMillis)
        assertEquals(LocalDate.parse("2026-01-01"), migratedAttempt.startedOnDate)
    }

    @Test fun setback_isSingle_preserved_andUndoable() = runBlocking {
        val name = "setback-${System.nanoTime()}.db".also(names::add)
        val db = Room.inMemoryDatabaseBuilder(context, StreakDataBase::class.java).build().also(databases::add)
        val repository = StreakRepository(db.streakDao(), db.notificationDao(), db)
        val now = System.currentTimeMillis()
        val start = now - Duration.ofDays(12).toMillis()
        val id = repository.insertStreak(StreakModel(streakName = "Fictional goal", currentAttemptStartedAtMillis = start))

        val first = async { repository.recordSetback(id, now, "A note") }
        val rapidDuplicate = async { repository.recordSetback(id, now, "A note") }
        val results = listOf(first.await(), rapidDuplicate.await())
        assertEquals(1, results.count { it != null })
        assertEquals(1, repository.observeAttempts(id).first().size)
        assertEquals(now, repository.getStreakById(id)?.currentAttemptStartedAtMillis)

        assertTrue(repository.undoSetback(results.firstNotNullOf { it }))
        assertTrue(repository.observeAttempts(id).first().isEmpty())
        assertEquals(start, repository.getStreakById(id)?.currentAttemptStartedAtMillis)
    }

    @Test fun bulkArchive_andDelete_preserveAlreadyArchivedDates_andRemoveRelations() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, StreakDataBase::class.java).build().also(databases::add)
        val repository = StreakRepository(db.streakDao(), db.notificationDao(), db)
        val activeId = repository.insertStreak(StreakModel(streakName = "Active"))
        val originalEnd = 1234L
        val archivedId = repository.insertStreak(StreakModel(streakName = "Archived", archivedAtMillis = originalEnd))
        repository.saveReminder(ReminderSchedule(streakId = activeId))

        repository.archiveStreaks(setOf(activeId, archivedId), now = 9999L)
        assertEquals(9999L, repository.getStreakById(activeId)?.archivedAtMillis)
        assertEquals(originalEnd, repository.getStreakById(archivedId)?.archivedAtMillis)
        assertFalse(repository.remindersForGoal(activeId).single().enabled)

        repository.deleteStreaks(setOf(activeId, archivedId))
        assertNull(repository.getStreakById(activeId))
        assertNull(repository.getStreakById(archivedId))
    }

    @Test fun archiveAndRestore_resumePausedDurationAndPreserveIdentityHistoryAndReminder() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, StreakDataBase::class.java).build().also(databases::add)
        val repository = StreakRepository(db.streakDao(), db.notificationDao(), db)
        val startedAt = 10_000L
        val archivedAt = 20_000L
        val restoredAt = 30_000L
        val original = StreakModel(
            streakName = "Keep everything",
            streakColor = 42L,
            currentAttemptStartedAtMillis = startedAt,
            whyQuitting = "Reason",
            urgeAction = "Plan"
        )
        val id = repository.insertStreak(original)
        repository.saveReminder(ReminderSchedule(streakId = id, enabled = true))

        assertEquals(1, repository.archiveStreak(id, archivedAt))
        assertEquals(0, repository.archiveStreak(id, archivedAt + 1))
        val archived = repository.getStreakById(id)!!
        assertEquals(archivedAt, archived.archivedAtMillis)
        assertEquals(archivedAt - startedAt, com.example.streaks.Model.currentDurationValue(archived, archivedAt).millis)
        assertTrue(repository.observeAttempts(id).first().isEmpty())
        assertFalse(repository.remindersForGoal(id).single().enabled)

        assertEquals(1, repository.unarchiveStreak(id, restoredAt))
        assertEquals(0, repository.unarchiveStreak(id, restoredAt + 1))
        val restored = repository.getStreakById(id)!!
        assertEquals(id, restored.streakId)
        assertEquals(original.streakName, restored.streakName)
        assertEquals(original.streakColor, restored.streakColor)
        assertEquals(original.whyQuitting, restored.whyQuitting)
        assertEquals(original.urgeAction, restored.urgeAction)
        assertNull(restored.archivedAtMillis)
        assertEquals(restoredAt - (archivedAt - startedAt), restored.currentAttemptStartedAtMillis)
        assertEquals(archivedAt - startedAt, com.example.streaks.Model.currentDurationValue(restored, restoredAt).millis)
        assertTrue(repository.observeAttempts(id).first().isEmpty())
        assertFalse(repository.remindersForGoal(id).single().enabled)
    }

    @Test fun dateOnlyStreak_resumesAtItsArchivedDuration() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, StreakDataBase::class.java).build().also(databases::add)
        val repository = StreakRepository(db.streakDao(), db.notificationDao(), db)
        val zone = ZoneId.systemDefault()
        val startDate = LocalDate.of(2026, 1, 1)
        val archivedAt = LocalDate.of(2026, 1, 11).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val restoredAt = LocalDate.of(2026, 2, 1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val id = repository.insertStreak(
            StreakModel(streakName = "Date only", startDate = startDate, currentAttemptStartedAtMillis = 0L)
        )

        assertEquals(1, repository.archiveStreak(id, archivedAt))
        val frozenDuration = com.example.streaks.Model.currentDurationValue(repository.getStreakById(id)!!, archivedAt).millis
        assertEquals(Duration.ofDays(10).toMillis(), frozenDuration)

        assertEquals(1, repository.unarchiveStreak(id, restoredAt))
        val restored = repository.getStreakById(id)!!
        assertEquals(frozenDuration, com.example.streaks.Model.currentDurationValue(restored, restoredAt).millis)
        assertTrue(repository.observeAttempts(id).first().isEmpty())
    }

    @Test fun reminderHistory_hasOneRowPerScheduledOccurrence() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, StreakDataBase::class.java).build().also(databases::add)
        val repository = NotificationRepository(db.notificationDao())
        val occurrence = NotificationModel(
            streakId = 1,
            streakName = "",
            message = "",
            status = Status.OnGoing,
            frequency = Frequency.DAILY,
            reminderId = 42,
            scheduledForMillis = 9999L,
            outcome = ReminderOutcome.POSTED
        )
        assertTrue(repository.addNotification(occurrence) > 0)
        assertEquals(-1L, repository.addNotification(occurrence))
        assertEquals(1, repository.getAllNotifications().first().size)
    }

    @Test fun blackAndWhite_accentsRemainReadableInBothThemes() {
        assertEquals(androidx.compose.ui.graphics.Color.Black, com.example.streaks.ui.theme.readableAccent(androidx.compose.ui.graphics.Color.White, false))
        assertEquals(androidx.compose.ui.graphics.Color.White, com.example.streaks.ui.theme.readableAccent(androidx.compose.ui.graphics.Color.Black, true))
        assertEquals(androidx.compose.ui.graphics.Color.White, com.example.streaks.ui.theme.readableAccent(androidx.compose.ui.graphics.Color.White, true))
        assertEquals(androidx.compose.ui.graphics.Color.Black, com.example.streaks.ui.theme.readableAccent(androidx.compose.ui.graphics.Color.Black, false))
    }

    private fun createV1Database(name: String) {
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(path, null)
        db.execSQL("CREATE TABLE Streaks (streakId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, streakName TEXT NOT NULL, streakColor INTEGER NOT NULL, frequency TEXT NOT NULL, startDate TEXT NOT NULL, endDate TEXT, count INTEGER NOT NULL, reminderTime TEXT, notificationType TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Notifications (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, streakId INTEGER NOT NULL, streakName TEXT NOT NULL, streakColor INTEGER NOT NULL, message TEXT NOT NULL, timestamp INTEGER NOT NULL, status TEXT NOT NULL, frequency TEXT NOT NULL)")
        db.execSQL("INSERT INTO Streaks (streakName, streakColor, frequency, startDate, endDate, count, reminderTime, notificationType) VALUES ('Active', 1, 'DAILY', '2026-01-01', NULL, 0, '20:30', 'DEFAULT')")
        db.execSQL("INSERT INTO Streaks (streakName, streakColor, frequency, startDate, endDate, count, reminderTime, notificationType) VALUES ('Ended', 2, 'DAILY', '2025-01-01', '2025-01-05', 0, NULL, 'DEFAULT')")
        db.execSQL("INSERT INTO Streaks (streakName, streakColor, frequency, startDate, endDate, count, reminderTime, notificationType) VALUES ('No reminder', 3, 'DAILY', '2026-02-01', NULL, 0, NULL, 'SILENT')")
        db.version = 1
        db.close()
    }
}
