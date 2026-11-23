package com.example.streaks.View.HomeScreens

import java.time.Instant
import java.time.ZonedDateTime

fun homeGreeting(hour: Int, displayName: String): String {
    val period = when (hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    return "$period, ${displayName.trim().ifEmpty { "User" }}"
}

fun nextGreetingBoundaryMillis(now: ZonedDateTime): Long {
    val next = when (now.hour) {
        in 0..11 -> now.toLocalDate().atTime(12, 0).atZone(now.zone)
        in 12..17 -> now.toLocalDate().atTime(18, 0).atZone(now.zone)
        else -> now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    }
    return next.toInstant().toEpochMilli()
}

fun greetingAt(epochMillis: Long, zone: java.time.ZoneId, displayName: String): String =
    homeGreeting(Instant.ofEpochMilli(epochMillis).atZone(zone).hour, displayName)
