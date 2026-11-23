package com.example.streaks.Model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class ElapsedParts(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)
data class DurationValue(val millis: Long, val dateOnly: Boolean)

fun elapsedParts(startMillis: Long, endMillis: Long): ElapsedParts {
    var seconds = max(0, Duration.ofMillis(endMillis - startMillis).seconds)
    val days = seconds / 86_400
    seconds %= 86_400
    val hours = seconds / 3_600
    seconds %= 3_600
    val minutes = seconds / 60
    return ElapsedParts(days, hours, minutes, seconds % 60)
}

fun calendarDaysBetween(start: LocalDate, end: LocalDate): Long =
    ChronoUnit.DAYS.between(start, end).coerceAtLeast(0)

fun currentDurationValue(
    streak: StreakModel,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): DurationValue {
    if (streak.currentAttemptStartedAtMillis <= 0) {
        val end = streak.endDate
            ?: streak.archivedAtMillis?.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            ?: Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        return DurationValue(Duration.ofDays(calendarDaysBetween(streak.startDate, end)).toMillis(), dateOnly = true)
    }
    val end = streak.archivedAtMillis ?: nowMillis
    return DurationValue((end - streak.currentAttemptStartedAtMillis).coerceAtLeast(0), dateOnly = false)
}

fun attemptDurationValue(attempt: AttemptModel, zone: ZoneId = ZoneId.systemDefault()): DurationValue {
    val dateStart = attempt.startedOnDate
    if (dateStart != null) {
        val end = Instant.ofEpochMilli(attempt.endedAtMillis).atZone(zone).toLocalDate()
        return DurationValue(Duration.ofDays(calendarDaysBetween(dateStart, end)).toMillis(), dateOnly = true)
    }
    return DurationValue((attempt.endedAtMillis - attempt.startedAtMillis).coerceAtLeast(0), dateOnly = false)
}

fun personalBestValue(
    streak: StreakModel,
    attempts: List<AttemptModel>,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): DurationValue = (attempts.map { attemptDurationValue(it, zone) } + currentDurationValue(streak, nowMillis, zone))
    .maxWithOrNull(compareBy<DurationValue> { it.millis }.thenBy { it.dateOnly })
    ?: DurationValue(0, false)

fun personalBestMillis(streak: StreakModel, attempts: List<AttemptModel>, nowMillis: Long): Long =
    personalBestValue(streak, attempts, nowMillis).millis
