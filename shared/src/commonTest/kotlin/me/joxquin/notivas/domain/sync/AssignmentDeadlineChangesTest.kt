package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.model.Assignment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class AssignmentDeadlineChangesTest {
    @Test
    fun detectsMoveOnceAcrossSnapshots() {
        val original = assignment(1, "2026-11-30T22:00:00Z")
        val moved = original.copy(dueAt = "2026-11-29T22:00:00Z")

        val change = detectAssignmentDeadlineChanges(listOf(original), listOf(moved, moved)).single()

        assertEquals(Instant.parse("2026-11-30T22:00:00Z"), change.previous)
        assertEquals(Instant.parse("2026-11-29T22:00:00Z"), change.current)
        assertTrue(detectAssignmentDeadlineChanges(listOf(moved), listOf(moved)).isEmpty())
    }

    @Test
    fun ignoresSameInstantInAnotherTimeZoneAndNewOrUnavailableAssignments() {
        val known = assignment(1, "2026-11-30T22:00:00Z")
        val priorWithoutDeadline = assignment(2, null)
        val equivalent = known.copy(dueAt = "2026-11-30T17:00:00-05:00")
        val new = assignment(3, "2026-11-30T22:00:00Z")

        val actual = detectAssignmentDeadlineChanges(
            listOf(known, priorWithoutDeadline, assignment(4, "2026-11-30T22:00:00Z")),
            listOf(equivalent, priorWithoutDeadline.copy(dueAt = "2026-11-30T22:00:00Z"), new, assignment(4, "bad-date"))
        )

        assertTrue(actual.isEmpty())
    }

    @Test
    fun comparesTheEffectiveCutoffWhenCanvasLocksAnAssignmentEarlier() {
        val original = assignment(1, "2026-11-30T22:00:00Z")
        val updated = original.copy(lockAt = "2026-11-29T22:00:00Z")

        assertEquals(
            Instant.parse("2026-11-29T22:00:00Z"),
            detectAssignmentDeadlineChanges(listOf(original), listOf(updated)).single().current
        )
    }

    private fun assignment(id: Long, dueAt: String?): Assignment =
        Assignment(id = id, name = "Informe $id", courseId = 1, dueAt = dueAt)
}
