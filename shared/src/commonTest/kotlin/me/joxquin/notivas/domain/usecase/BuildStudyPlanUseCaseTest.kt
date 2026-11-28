package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.SubmissionDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BuildStudyPlanUseCaseTest {
    private val buildStudyPlan = BuildStudyPlanUseCase()
    private val now = Instant.parse("2026-11-27T18:00:00Z")

    @Test
    fun candidatesExcludeClosedSubmittedAndUndatedTasks() {
        val candidates = buildStudyPlan.candidates(
            listOf(
                assignment(1).copy(isLocked = true),
                assignment(2).copy(lockAt = "2026-11-27T18:00:00Z"),
                assignment(3).copy(isCompleted = true),
                assignment(4).copy(status = "completed"),
                assignment(5).copy(submittedAt = "2026-11-27T17:00:00Z"),
                assignment(6).copy(submission = SubmissionDetails(workflowState = "graded")),
                assignment(7).copy(submission = SubmissionDetails(submittedAt = "2026-11-27T17:00:00Z")),
                assignment(8, dueAt = null),
                assignment(9, dueAt = "invalid"),
                assignment(10, dueAt = "2026-11-27T17:59:59Z"),
                assignment(11)
            ),
            now
        )

        assertEquals(listOf(11L), candidates.map { it.assignment.id })
    }

    @Test
    fun candidatesUseEarlierOfDueAndLockTimeAndBreakTiesById() {
        val candidates = buildStudyPlan.candidates(
            listOf(
                assignment(4, "2026-11-27T19:00:00Z"),
                assignment(3, null).copy(lockAt = "2026-11-27T18:50:00Z"),
                assignment(2, "2026-11-27T13:40:00-05:00"),
                assignment(1).copy(lockAt = "2026-11-27T18:40:00Z")
            ),
            now
        )

        assertEquals(listOf(1L, 2L, 3L), candidates.map { it.assignment.id })
        assertEquals(
            listOf("2026-11-27T18:40:00Z", "2026-11-27T18:40:00Z", "2026-11-27T18:50:00Z")
                .map(Instant::parse),
            candidates.map { it.deadline }
        )
    }

    @Test
    fun planCanUseExactAvailableTimeAndFinishAtDeadline() {
        val candidates = buildStudyPlan.candidates(
            listOf(assignment(1, "2026-11-27T18:45:00Z"), assignment(2, "2026-11-27T19:00:00Z")),
            now
        )

        val plan = buildStudyPlan(candidates, mapOf(1L to 45, 2L to 15), availableMinutes = 60, now = now)

        assertEquals(StudyPlan(listOf(1L, 2L), 60), plan)
    }

    @Test
    fun planSkipsTasksThatWouldMissDeadlineAfterEarlierWork() {
        val candidates = buildStudyPlan.candidates(
            listOf(
                assignment(1, "2026-11-27T18:45:00Z"),
                assignment(2, "2026-11-27T18:50:00Z"),
                assignment(3, "2026-11-27T19:00:00Z")
            ),
            now
        )

        val plan = buildStudyPlan(candidates, mapOf(1L to 40, 2L to 15, 3L to 20), availableMinutes = 60, now = now)

        assertEquals(StudyPlan(listOf(1L, 3L), 60), plan)
    }

    @Test
    fun planSkipsTasksOverBudgetAndUsesDefaultForInvalidEstimate() {
        val candidates = buildStudyPlan.candidates(
            listOf(assignment(1, "2026-11-27T19:00:00Z"), assignment(2)),
            now
        )

        val plan = buildStudyPlan(candidates, mapOf(1L to 60, 2L to 0), availableMinutes = 45, now = now)

        assertEquals(StudyPlan(listOf(2L), BuildStudyPlanUseCase.DEFAULT_ESTIMATED_MINUTES), plan)
    }

    private fun assignment(id: Long, dueAt: String? = "2026-11-27T20:00:00Z"): Assignment =
        Assignment(id = id, name = "Tarea $id", dueAt = dueAt, courseId = 1)
}
