package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncDiffDetectorTest {

    private fun createAssignment(id: Long, name: String, isQuiz: Boolean = false): Assignment {
        return Assignment(
            id = id,
            courseId = 100L,
            name = name,
            description = null,
            dueAt = "2026-11-05T23:59:59Z",
            lockAt = null,
            pointsPossible = 20.0,
            htmlUrl = "https://canvas.test/assignments/$id",
            submissionTypes = if (isQuiz) listOf("online_quiz") else listOf("online_upload"),
            allowedExtensions = null,
            isCompleted = false,
            isLocked = false,
            status = "upcoming"
        )
    }

    private fun createForum(id: Long, title: String): CanvasDiscussionTopic {
        return CanvasDiscussionTopic(
            id = id,
            courseId = 100L,
            title = title,
            message = "Debate",
            postedAt = "2026-11-01T10:00:00Z"
        )
    }

    @Test
    fun testInitialSyncReturnsEmptyDiff() {
        val assignments = listOf(createAssignment(1, "Lab 1"), createAssignment(2, "Lab 2"))
        val forums = listOf(createForum(10, "Foro 1"))

        val diff = SyncDiffDetector.detectChanges(
            currentAssignments = assignments,
            currentForums = forums,
            knownAssignmentIds = emptySet(),
            knownForumIds = emptySet(),
            hasCompletedInitialSync = false,
            filterNewAssignments = true,
            filterNewQuizzes = true,
            filterNewForos = true
        )

        assertTrue(diff.newAssignments.isEmpty())
        assertTrue(diff.newQuizzes.isEmpty())
        assertTrue(diff.newForos.isEmpty())
        assertEquals(setOf(1L, 2L), diff.allAssignmentIds)
        assertEquals(setOf(10L), diff.allForumIds)
    }

    @Test
    fun testEmptyKnownIdsPreventsRetroactiveSpamEvenIfInitialSyncFlagIsTrue() {
        val assignments = listOf(createAssignment(1, "Lab 1"), createAssignment(2, "Lab 2"))
        val forums = listOf(createForum(10, "Foro 1"))

        val diff = SyncDiffDetector.detectChanges(
            currentAssignments = assignments,
            currentForums = forums,
            knownAssignmentIds = emptySet(),
            knownForumIds = emptySet(),
            hasCompletedInitialSync = true,
            filterNewAssignments = true,
            filterNewQuizzes = true,
            filterNewForos = true
        )

        assertTrue(diff.newAssignments.isEmpty())
        assertTrue(diff.newQuizzes.isEmpty())
        assertTrue(diff.newForos.isEmpty())
        assertEquals(setOf(1L, 2L), diff.allAssignmentIds)
        assertEquals(setOf(10L), diff.allForumIds)
    }

    @Test
    fun testDetectsOnlyTrulyNewAssignmentsAndQuizzes() {
        val assignments = listOf(
            createAssignment(1, "Lab 1"),
            createAssignment(2, "Lab 2"),
            createAssignment(3, "Examen Parcial", isQuiz = true)
        )
        val forums = listOf(
            createForum(10, "Foro 1"),
            createForum(11, "Foro 2")
        )

        val diff = SyncDiffDetector.detectChanges(
            currentAssignments = assignments,
            currentForums = forums,
            knownAssignmentIds = setOf(1L),
            knownForumIds = setOf(10L),
            hasCompletedInitialSync = true,
            filterNewAssignments = true,
            filterNewQuizzes = true,
            filterNewForos = true
        )

        assertEquals(1, diff.newAssignments.size)
        assertEquals(2L, diff.newAssignments.first().id)
        assertEquals("Lab 2", diff.newAssignments.first().title)

        assertEquals(1, diff.newQuizzes.size)
        assertEquals(3L, diff.newQuizzes.first().id)
        assertEquals("Examen Parcial", diff.newQuizzes.first().title)

        assertEquals(1, diff.newForos.size)
        assertEquals(11L, diff.newForos.first().id)
        assertEquals("Foro 2", diff.newForos.first().title)
    }
}
