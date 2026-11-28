package me.joxquin.notivas.ui.dashboard

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.domain.sync.DeadlineRadarEntry
import me.joxquin.notivas.domain.usecase.BuildStudyPlanUseCase
import kotlin.time.Instant

data class StudyPlanTaskUiModel(
    val assignment: Assignment,
    val courseName: String,
    val deadline: Instant,
    val estimatedMinutes: Int,
    val isScheduled: Boolean,
    val gradeImprovement: Float? = null
)

data class StudyPlanUiState(
    val availableMinutes: Int = 60,
    val scheduledMinutes: Int = 0,
    val tasks: List<StudyPlanTaskUiModel> = emptyList()
)

data class DeadlineRadarUiItem(val entry: DeadlineRadarEntry, val task: AssignmentUiModel?)

internal fun BuildStudyPlanUseCase.uiState(
    assignments: List<AssignmentUiModel>,
    estimates: Map<Long, Int>,
    availableMinutes: Int,
    now: Instant,
    improvement: (Assignment) -> Float? = { null }
): StudyPlanUiState {
    val candidates = candidates(assignments.map { it.assignment }, now)
    val plan = invoke(candidates, estimates, availableMinutes, now)
    val tasks = candidates.map { candidate ->
        val assignment = candidate.assignment
        StudyPlanTaskUiModel(
            assignment = assignment,
            courseName = assignments.first { it.assignment.id == assignment.id }.courseName,
            deadline = candidate.deadline,
            estimatedMinutes = estimates[assignment.id] ?: BuildStudyPlanUseCase.DEFAULT_ESTIMATED_MINUTES,
            isScheduled = assignment.id in plan.scheduledIds,
            gradeImprovement = improvement(assignment)
        )
    }.sortedByDescending { it.isScheduled }
    return StudyPlanUiState(availableMinutes, plan.scheduledMinutes, tasks)
}
