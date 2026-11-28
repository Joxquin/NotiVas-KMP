package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.domain.sync.effectiveDeadline
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class StudyPlanCandidate(val assignment: Assignment, val deadline: Instant)

data class StudyPlan(val scheduledIds: List<Long>, val scheduledMinutes: Int)

class BuildStudyPlanUseCase {
    fun candidates(assignments: List<Assignment>, now: Instant): List<StudyPlanCandidate> =
        assignments.asSequence()
            .filterNot { it.isLocked || it.hasBeenSubmitted() }
            .mapNotNull { assignment ->
                val deadline = assignment.effectiveDeadline() ?: return@mapNotNull null
                StudyPlanCandidate(assignment, deadline)
            }
            .filter { it.deadline > now }
            .sortedWith(compareBy({ it.deadline }, { it.assignment.id }))
            .take(3)
            .toList()

    operator fun invoke(
        candidates: List<StudyPlanCandidate>,
        estimatedMinutes: Map<Long, Int>,
        availableMinutes: Int,
        now: Instant
    ): StudyPlan {
        val scheduledIds = mutableListOf<Long>()
        var scheduledMinutes = 0

        for (candidate in candidates) {
            val assignmentId = candidate.assignment.id
            val duration = estimatedMinutes[assignmentId]?.takeIf { it > 0 } ?: DEFAULT_ESTIMATED_MINUTES
            if (duration > availableMinutes - scheduledMinutes) continue

            val finishAfterMinutes = scheduledMinutes + duration
            if (now + finishAfterMinutes.minutes > candidate.deadline) continue

            scheduledIds.add(assignmentId)
            scheduledMinutes = finishAfterMinutes
        }

        return StudyPlan(scheduledIds, scheduledMinutes)
    }

    private fun Assignment.hasBeenSubmitted(): Boolean =
        isCompleted || status == "completed" || !submittedAt.isNullOrBlank() ||
            !submission?.submittedAt.isNullOrBlank() ||
            submission?.workflowState in setOf("submitted", "graded")

    companion object {
        const val DEFAULT_ESTIMATED_MINUTES = 30
    }
}
