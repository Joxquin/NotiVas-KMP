package me.joxquin.notivas.ui.dashboard.draft

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.SubmissionDetails
import me.joxquin.notivas.ui.dashboard.draft.components.formatCanvasLockDate
import me.joxquin.notivas.util.DateTimeUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssignmentSubmissionEligibilityTest {

    private val referenceNowMillis = 1793862000000L // Approx early Nov 2026

    @Test
    fun testCanSubmit_lockedForUser() {
        val assignment = Assignment(
            id = 1,
            name = "Actividad en clase",
            courseId = 100,
            isLocked = true,
            submissionTypes = listOf("online_upload")
        )
        assertFalse(assignment.canSubmit(referenceNowMillis, isSubmitted = false))
        assertTrue(assignment.isHardLocked(referenceNowMillis))
    }

    @Test
    fun testCanSubmit_lockAtInPast() {
        // Locked on Oct 29, 2026
        val assignment = Assignment(
            id = 2,
            name = "MTEO S11 - Actividad en clase",
            courseId = 100,
            dueAt = "2026-10-29T18:50:00Z",
            lockAt = "2026-10-29T18:50:00Z",
            submissionTypes = listOf("online_upload")
        )
        val lockComp = DateTimeUtil.parseIsoToComponents("2026-10-29T18:50:00Z")!!
        val afterLockMillis = lockComp.epochMillis + 1000L

        assertFalse(assignment.canSubmit(afterLockMillis, isSubmitted = false))
        assertTrue(assignment.isHardLocked(afterLockMillis))
    }

    @Test
    fun testCanSubmit_pastDue_butLockAtInFuture_canStillSubmit() {
        // Due date passed yesterday, but lock date is tomorrow
        val dueComp = DateTimeUtil.parseIsoToComponents("2026-11-01T18:00:00Z")!!
        val lockComp = DateTimeUtil.parseIsoToComponents("2026-11-10T18:00:00Z")!!
        val currentMillis = dueComp.epochMillis + 24 * 60 * 60 * 1000L // 1 day after due date

        val assignment = Assignment(
            id = 3,
            name = "Tarea con entrega tardía",
            courseId = 100,
            dueAt = "2026-11-01T18:00:00Z",
            lockAt = "2026-11-10T18:00:00Z",
            submissionTypes = listOf("online_upload")
        )

        assertTrue(assignment.isPastDue(currentMillis))
        assertFalse(assignment.isHardLocked(currentMillis))
        assertTrue(assignment.canSubmit(currentMillis, isSubmitted = false))
    }

    @Test
    fun testCanSubmit_pastDue_noLockDate_canStillSubmit() {
        val dueComp = DateTimeUtil.parseIsoToComponents("2026-11-01T18:00:00Z")!!
        val currentMillis = dueComp.epochMillis + 48 * 60 * 60 * 1000L

        val assignment = Assignment(
            id = 4,
            name = "Tarea sin fecha de bloqueo",
            courseId = 100,
            dueAt = "2026-11-01T18:00:00Z",
            lockAt = null,
            submissionTypes = listOf("online_upload")
        )

        assertTrue(assignment.isPastDue(currentMillis))
        assertFalse(assignment.isHardLocked(currentMillis))
        assertTrue(assignment.canSubmit(currentMillis, isSubmitted = false))
    }

    @Test
    fun testCanSubmit_attemptsExhausted() {
        val assignment = Assignment(
            id = 5,
            name = "Tarea con 1 intento",
            courseId = 100,
            allowedAttempts = 1,
            submissionTypes = listOf("online_upload"),
            submission = SubmissionDetails(attempt = 1)
        )

        // If not submitted yet, attempt 0 < 1 -> can submit
        assertTrue(assignment.hasRemainingAttempts(isSubmitted = false))
        assertTrue(assignment.canSubmit(referenceNowMillis, isSubmitted = false))

        // If already submitted, attempt 1 >= 1 -> cannot submit
        assertFalse(assignment.hasRemainingAttempts(isSubmitted = true))
        assertFalse(assignment.canSubmit(referenceNowMillis, isSubmitted = true))
    }

    @Test
    fun testCanSubmit_multipleAttemptsRemaining() {
        val assignment = Assignment(
            id = 6,
            name = "Tarea con 3 intentos",
            courseId = 100,
            allowedAttempts = 3,
            submissionTypes = listOf("online_upload"),
            submission = SubmissionDetails(attempt = 1)
        )

        // 1 attempt used out of 3 -> can submit again
        assertTrue(assignment.hasRemainingAttempts(isSubmitted = true))
        assertTrue(assignment.canSubmit(referenceNowMillis, isSubmitted = true))
    }

    @Test
    fun testCanSubmit_unlimitedAttempts() {
        val assignment = Assignment(
            id = 7,
            name = "Tarea intentos ilimitados",
            courseId = 100,
            allowedAttempts = -1,
            submissionTypes = listOf("online_upload"),
            submission = SubmissionDetails(attempt = 5)
        )

        assertTrue(assignment.hasRemainingAttempts(isSubmitted = true))
        assertTrue(assignment.canSubmit(referenceNowMillis, isSubmitted = true))
    }

    @Test
    fun testCanSubmit_nonOnlineSubmissionType() {
        val onPaperAssignment = Assignment(
            id = 8,
            name = "Examen en papel",
            courseId = 100,
            submissionTypes = listOf("on_paper")
        )
        assertFalse(onPaperAssignment.supportsDirectSubmission)
        assertFalse(onPaperAssignment.canSubmit(referenceNowMillis, isSubmitted = false))

        val noneAssignment = Assignment(
            id = 9,
            name = "Tarea informativa sin entrega",
            courseId = 100,
            submissionTypes = listOf("none")
        )
        assertFalse(noneAssignment.supportsDirectSubmission)
        assertFalse(noneAssignment.canSubmit(referenceNowMillis, isSubmitted = false))
    }

    @Test
    fun testFormatCanvasLockDate_spanishFormat() {
        val formatted = formatCanvasLockDate("2026-10-29T18:50:00Z")
        assertEquals("29 de octubre de 2026 a las 18:50", formatted)
    }

    @Test
    fun testFormatCanvasLockDate_nullOrBlank() {
        assertEquals(null, formatCanvasLockDate(null))
        assertEquals(null, formatCanvasLockDate(""))
    }
}
