package com.example.streaks.Model

enum class ArchiveAction {
    ARCHIVE,
    UNARCHIVE
}

/**
 * A mixed selection always archives its active members. Only an entirely archived
 * selection becomes an unarchive operation.
 */
fun archiveActionFor(streaks: Collection<StreakModel>): ArchiveAction? = when {
    streaks.isEmpty() -> null
    streaks.any { it.archivedAtMillis == null } -> ArchiveAction.ARCHIVE
    else -> ArchiveAction.UNARCHIVE
}

fun archiveTargetsFor(streaks: Collection<StreakModel>, action: ArchiveAction): Set<Int> =
    streaks.asSequence()
        .filter {
            when (action) {
                ArchiveAction.ARCHIVE -> it.archivedAtMillis == null
                ArchiveAction.UNARCHIVE -> it.archivedAtMillis != null
            }
        }
        .map(StreakModel::streakId)
        .toSet()

/** Active goals are eligible even when they do not have any reminder schedules yet. */
fun eligibleReminderGoals(streaks: Collection<StreakModel>): List<StreakModel> =
    streaks.filter { it.archivedAtMillis == null }
