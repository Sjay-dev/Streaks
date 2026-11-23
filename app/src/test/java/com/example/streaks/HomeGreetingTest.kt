package com.example.streaks

import com.example.streaks.View.HomeScreens.homeGreeting
import com.example.streaks.View.HomeScreens.nextGreetingBoundaryMillis
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class HomeGreetingTest {
    @Test fun greeting_boundaries_and_fallback_are_exact() {
        assertEquals("Good morning, User", homeGreeting(0, ""))
        assertEquals("Good morning, Jo", homeGreeting(11, " Jo "))
        assertEquals("Good afternoon, Jo", homeGreeting(12, "Jo"))
        assertEquals("Good afternoon, Jo", homeGreeting(17, "Jo"))
        assertEquals("Good evening, Jo", homeGreeting(18, "Jo"))
        assertEquals("Good evening, Jo", homeGreeting(23, "Jo"))
    }

    @Test fun next_boundary_moves_from_noon_to_evening_to_midnight() {
        val zone = ZoneId.of("Africa/Lagos")
        assertEquals(
            ZonedDateTime.of(2026, 11, 23, 12, 0, 0, 0, zone).toInstant().toEpochMilli(),
            nextGreetingBoundaryMillis(ZonedDateTime.of(2026, 11, 23, 11, 59, 0, 0, zone))
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 23, 18, 0, 0, 0, zone).toInstant().toEpochMilli(),
            nextGreetingBoundaryMillis(ZonedDateTime.of(2026, 11, 23, 12, 0, 0, 0, zone))
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 24, 0, 0, 0, 0, zone).toInstant().toEpochMilli(),
            nextGreetingBoundaryMillis(ZonedDateTime.of(2026, 11, 23, 18, 0, 0, 0, zone))
        )
    }
}
