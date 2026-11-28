package me.joxquin.notivas.domain.team

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TeamChecklistTest {
    @Test
    fun stableAssignmentLinkDropsQueryAndFragment() {
        val link = teamChecklistCanvasLink(
            "https://campus.example/courses/4/assignments/8?token=secret#notes",
            courseId = 4,
            assignmentId = 8
        )

        assertEquals("https://campus.example/courses/4/assignments/8", link)
        assertNull(teamChecklistCanvasLink("https://person:password@campus.example", 4, 8))
        assertNull(teamChecklistCanvasLink("https://campus.example", 4, 0))
    }

    @Test
    fun sharesEditedStepsAndSkipsBlankRows() {
        val preview = formatTeamChecklist(
            heading = "Informe",
            items = listOf(
                TeamChecklistItem(1, " Preparar PDF ", "Ana", "jueves"),
                TeamChecklistItem(2, " Revisión ", done = true),
                TeamChecklistItem(3, "  ", owner = "Fila vacía")
            ),
            ownerLabel = "Responsable",
            deadlineLabel = "Fecha acordada",
            sourceUrl = "https://campus.example/courses/4/assignments/8"
        )

        assertTrue(preview.contains("[ ] Preparar PDF · Responsable Ana · Fecha acordada jueves"))
        assertTrue(preview.contains("[x] Revisión"))
        assertTrue(preview.endsWith("Canvas https://campus.example/courses/4/assignments/8"))
        assertFalse(preview.contains("Fila vacía"))
    }
}
