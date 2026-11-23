package com.example.streaks

import com.example.streaks.Model.AttemptModel
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.currentDurationValue
import com.example.streaks.Model.elapsedParts
import com.example.streaks.Model.personalBestMillis
import com.example.streaks.Notification.nextReminderAtMillis
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.LocalDate
import java.time.ZoneId

class StreakTimeTest {
    @Test fun elapsed_uses_real_duration_across_days() {
        val start = Instant.parse("2026-01-01T10:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-01-13T12:03:04Z").toEpochMilli()
        assertEquals(com.example.streaks.Model.ElapsedParts(12, 2, 3, 4), elapsedParts(start, end))
    }

    @Test fun personal_best_compares_production_attempts_with_current_time() {
        val goal = StreakModel(currentAttemptStartedAtMillis = 1_000)
        val attempts = listOf(AttemptModel(streakId = 1, startedAtMillis = 10_000, endedAtMillis = 50_000))
        assertEquals(40_000, personalBestMillis(goal, attempts, nowMillis = 20_000))
    }

    @Test fun v1_date_only_start_remains_calendar_precision() {
        val zone = ZoneId.of("Africa/Lagos")
        val now = Instant.parse("2026-01-13T12:03:04Z").toEpochMilli()
        val value = currentDurationValue(
            StreakModel(startDate = LocalDate.parse("2026-01-01"), currentAttemptStartedAtMillis = 0),
            now,
            zone
        )
        assertEquals(true, value.dateOnly)
        assertEquals(12, elapsedParts(0, value.millis).days)
    }

    @Test fun reminder_stays_at_local_wall_clock_after_timezone_change() {
        val now = Instant.parse("2026-06-10T12:00:00Z").toEpochMilli()
        val time = LocalTime.of(20, 30)
        val lagos = Instant.ofEpochMilli(nextReminderAtMillis(time, now, ZoneId.of("Africa/Lagos"))).atZone(ZoneId.of("Africa/Lagos"))
        val newYork = Instant.ofEpochMilli(nextReminderAtMillis(time, now, ZoneId.of("America/New_York"))).atZone(ZoneId.of("America/New_York"))
        assertEquals(time, lagos.toLocalTime())
        assertEquals(time, newYork.toLocalTime())
    }

    @Test fun reminder_skips_days_not_in_repeat_mask() {
        val zone = ZoneId.of("Africa/Lagos")
        // Wednesday 10 June 2026; only Sunday through Thursday, and today's time has passed.
        val now = java.time.ZonedDateTime.of(2026, 6, 10, 21, 0, 0, 0, zone).toInstant().toEpochMilli()
        val result = Instant.ofEpochMilli(nextReminderAtMillis(LocalTime.of(20, 30), 0b00011111, now, zone)).atZone(zone)
        assertEquals(LocalDate.of(2026, 6, 11), result.toLocalDate())
        assertEquals(LocalTime.of(20, 30), result.toLocalTime())
    }

    @Test fun explicit_midnight_is_not_treated_as_unknown_time() {
        val midnight = Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()
        val value = currentDurationValue(StreakModel(startDate = LocalDate.of(2026, 1, 1), currentAttemptStartedAtMillis = midnight), midnight + 60_000)
        assertEquals(false, value.dateOnly)
    }
}
