package com.example.streaks.Model.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.Converters
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.UrgeEventModel

@Database(
    entities = [StreakModel::class, NotificationModel::class, AttemptModel::class, UrgeEventModel::class, ReminderSchedule::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StreakDataBase : RoomDatabase() {
    abstract fun streakDao(): StreakDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Streaks ADD COLUMN whyQuitting TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE Streaks ADD COLUMN urgeAction TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE Streaks ADD COLUMN currentAttemptStartedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE Streaks ADD COLUMN archivedAtMillis INTEGER DEFAULT NULL")
                // Zero is an explicit date-only archive marker: preserve v1 precision and never invent a time.
                db.execSQL("UPDATE Streaks SET archivedAtMillis = 0 WHERE endDate IS NOT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS Attempts (attemptId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, streakId INTEGER NOT NULL, startedAtMillis INTEGER NOT NULL, startedOnDate TEXT, endedAtMillis INTEGER NOT NULL, note TEXT NOT NULL, recordedAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_Attempts_streakId ON Attempts (streakId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_Attempts_streakId_endedAtMillis ON Attempts (streakId, endedAtMillis)")
                db.execSQL("CREATE TABLE IF NOT EXISTS UrgeEvents (urgeEventId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, streakId INTEGER NOT NULL, `trigger` TEXT NOT NULL, whatHelped TEXT NOT NULL, recordedAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_UrgeEvents_streakId ON UrgeEvents (streakId)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // All pre-v3 goal colors were explicitly chosen or stored, so preserve them.
                db.execSQL("ALTER TABLE Streaks ADD COLUMN usesAppAccent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS ReminderSchedules (reminderId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, streakId INTEGER NOT NULL, time TEXT NOT NULL, repeatDays INTEGER NOT NULL, enabled INTEGER NOT NULL, destination TEXT NOT NULL, message TEXT NOT NULL, sound TEXT NOT NULL, vibration INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ReminderSchedules_streakId ON ReminderSchedules (streakId)")
                // Preserve every existing daily reminder as an enabled all-days schedule.
                db.execSQL("INSERT INTO ReminderSchedules (streakId, time, repeatDays, enabled, destination, message, sound, vibration) SELECT streakId, reminderTime, 127, 1, 'URGE_PLAN', 'A moment for yourself.', CASE notificationType WHEN 'SILENT' THEN 'SILENT' ELSE 'PHONE_DEFAULT' END, 1 FROM Streaks WHERE reminderTime IS NOT NULL AND archivedAtMillis IS NULL")
                // The schedule table is now authoritative; clearing the legacy field prevents duplicate alarms.
                db.execSQL("UPDATE Streaks SET reminderTime = NULL WHERE reminderTime IS NOT NULL")
            }
        }
    }
}
