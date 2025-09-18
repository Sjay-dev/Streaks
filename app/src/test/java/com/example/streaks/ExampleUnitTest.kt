package com.example.streaks

import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StreakTest {

    @Test
    fun noEndDateStreak() {
        val streak = StreakModel(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = null, // ✅ use just null
            frequency = Frequency.DAILY
        )
        val now = LocalDate.of(2025, 1, 5)
        assertEquals(4, calculateStreakCount(streak, now))
        println(calculateStreakCount(streak, now))
    }

    @Test
    fun streakWithEndDatePast() {
        val streak = StreakModel(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 1, 3),
            frequency = Frequency.DAILY
        )
        val now = LocalDate.of(2025, 1, 5)
        assertEquals(2, calculateStreakCount(streak, now)) // capped at endDate
        println(calculateStreakCount(streak, now))
    }

    @Test
    fun weeklyStreak() {
        val streak = StreakModel(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = null, // ✅ fixed
            frequency = Frequency.WEEKLY
        )
        val now = LocalDate.of(2025, 1, 15) // 2 weeks later
        assertEquals(2, calculateStreakCount(streak, now)) // inclusive
        println(calculateStreakCount(streak, now))
    }

    @Test
    fun nextCountDaily() {
        val streak = StreakModel(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = null, // ✅ fixed
            frequency = Frequency.DAILY
        )
        val now = LocalDateTime.of(2025, 1, 2, 20, 0) // 8 PM
        val result = nextCount(streak, now)
        println("Result: $result") // 👈 will print in the Run window
    }
}

fun nextCount(streak: StreakModel, now: LocalDateTime = LocalDateTime.now()): String {
    val start = streak.startDate.atStartOfDay()
    val end = streak.endDate?.atTime(23, 59, 59)

    if (end != null && now.isAfter(end)) return "Streak ended"

    fun pluralize(value: Long, unit: String) =
        "$value $unit" + if (value != 1L) "s" else ""

    return when (streak.frequency) {
        Frequency.DAILY -> {
            val endOfToday = now.toLocalDate().atTime(23, 59, 59)
            val totalMinutes = ChronoUnit.MINUTES.between(now, endOfToday)
            val hoursLeft = totalMinutes / 60
            val minutesLeft = totalMinutes % 60
            "${pluralize(hoursLeft, "hour")} ${pluralize(minutesLeft, "min")} left in today’s streak"
        }
        Frequency.WEEKLY -> {
            val daysSinceStart = ChronoUnit.DAYS.between(start, now)
            val daysIntoWeek = daysSinceStart % 7
            val daysLeft = 7 - daysIntoWeek
            "${pluralize(daysLeft, "day")} left in this weekly streak"
        }
        Frequency.MONTHLY -> {
            val monthsSinceStart = ChronoUnit.MONTHS.between(start, now)
            val currentMonthStart = streak.startDate.plusMonths(monthsSinceStart).atStartOfDay()
            val nextMonthStart = currentMonthStart.plusMonths(1)
            val daysLeft = ChronoUnit.DAYS.between(now, nextMonthStart)
            val weeks = daysLeft / 7
            val extraDays = daysLeft % 7
            "${pluralize(weeks, "week")} ${pluralize(extraDays, "day")} left in this monthly streak"
        }
    }
}

fun calculateStreakCount(streak: StreakModel, now: LocalDate = LocalDate.now()): Int {
    if (now.isBefore(streak.startDate)) return 0

    val end = streak.endDate
    return if (end != null && now.isAfter(end)) {
        when (streak.frequency) {
            Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, end).toInt()
            Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, end).toInt()
            Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, end).toInt()
        }
    } else {
        when (streak.frequency) {
            Frequency.DAILY -> ChronoUnit.DAYS.between(streak.startDate, now).toInt()
            Frequency.WEEKLY -> ChronoUnit.WEEKS.between(streak.startDate, now).toInt()
            Frequency.MONTHLY -> ChronoUnit.MONTHS.between(streak.startDate, now).toInt()
        }
    }
}
