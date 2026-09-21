package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Assignment

class GetUrgentAssignmentsUseCase {
    operator fun invoke(assignments: List<Assignment>): List<Assignment> {
        return assignments
            .filter { !it.isCompleted && !it.isLocked && it.dueAt != null }
            .sortedBy { it.dueAt }
    }
}
