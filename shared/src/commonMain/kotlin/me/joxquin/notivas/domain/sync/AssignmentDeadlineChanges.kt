package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.model.Assignment
import kotlin.time.Instant

internal fun parseDeadline(value: String?): Instant? = value?.let {
    runCatching { Instant.parse(it) }.getOrNull()
}

internal fun Assignment.effectiveDeadline(): Instant? =
    listOfNotNull(parseDeadline(dueAt), parseDeadline(lockAt)).minOrNull()

data class AssignmentDeadlineChange(
    val assignment: Assignment,
    val previous: Instant,
    val current: Instant
)

data class DeadlineRadarEntry(
    val id: Long,
    val assignmentId: Long,
    val title: String,
    val courseName: String?,
    val previous: Instant,
    val current: Instant
) {
    companion object {
        const val ACTIVITY_TYPE = "ASSIGNMENT_DEADLINE_CHANGE"
    }
}

fun detectAssignmentDeadlineChanges(
    previousAssignments: List<Assignment>,
    currentAssignments: List<Assignment>
): List<AssignmentDeadlineChange> {
    val previousById = previousAssignments.associateBy { it.id }
    return currentAssignments.distinctBy { it.id }.mapNotNull { assignment ->
        val previous = previousById[assignment.id]?.takeIf { it.courseId == assignment.courseId }
            ?.effectiveDeadline() ?: return@mapNotNull null
        val current = assignment.effectiveDeadline() ?: return@mapNotNull null
        if (previous == current) return@mapNotNull null
        AssignmentDeadlineChange(assignment, previous, current)
    }
}
