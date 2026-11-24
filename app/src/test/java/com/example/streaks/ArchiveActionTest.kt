package com.example.streaks

import com.example.streaks.Model.ArchiveAction
import com.example.streaks.Model.ReminderSchedule
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.archiveActionFor
import com.example.streaks.Model.archiveTargetsFor
import com.example.streaks.Model.eligibleReminderGoals
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveActionTest {
    private val active = StreakModel(streakId = 1, streakName = "Active")
    private val archived = StreakModel(streakId = 2, streakName = "Archived", archivedAtMillis = 123L)

    @Test fun mixedSelection_archivesOnlyActive_regardlessOfSelectionOrder() {
        listOf(listOf(active, archived), listOf(archived, active)).forEach { selection ->
            val action = archiveActionFor(selection)
            assertEquals(ArchiveAction.ARCHIVE, action)
            assertEquals(setOf(active.streakId), archiveTargetsFor(selection, action!!))
        }
    }

    @Test fun entirelyArchivedSelection_unarchivesEverySelectedStreak() {
        val second = archived.copy(streakId = 3)
        val selection = listOf(archived, second)
        val action = archiveActionFor(selection)
        assertEquals(ArchiveAction.UNARCHIVE, action)
        assertEquals(setOf(2, 3), archiveTargetsFor(selection, action!!))
    }

    @Test fun activeGoalWithoutExistingReminder_isEligibleForFirstReminder() {
        val schedules = emptyList<ReminderSchedule>()
        assertEquals(0, schedules.size)
        assertEquals(listOf(active), eligibleReminderGoals(listOf(archived, active)))
    }
}
