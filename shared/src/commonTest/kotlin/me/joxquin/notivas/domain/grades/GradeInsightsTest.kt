package me.joxquin.notivas.domain.grades

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradeInsightsTest {

    private val calculator = AcademicCalculatorUseCase()
    private val course = Course(id = 1, name = "Construcción y Pruebas de Software - C24", courseCode = "CPS", passingGrade = 12f)

    private fun assignment(id: Long, score: Double?, points: Double? = 20.0, status: String = "completed") =
        Assignment(id = id, name = "Tarea $id", courseId = 1, pointsPossible = points, score = score, status = status)

    @Test
    fun canvasAverageConvertsEveryGradeToTheTwentyScale() {
        val avg = GradeInsights.canvasAverage(listOf(assignment(1, 19.0), assignment(2, 8.0, points = 10.0), assignment(3, null)))
        assertEquals(17.5, avg)
    }

    @Test
    fun simulatorGroupsWithoutRealGradesFallBackToCanvas() {
        // Caso real: grupos de prueba con notas solo simuladas (15.0) y sin vínculo con Canvas.
        val placeholders = SimulationGroupWithItems(
            group = SimulationGroup(id = 1, courseId = 1, name = "laboratorio", weightPercentage = 40f, targetAssessments = 4),
            items = (1..4).map { SimulationItem(id = it.toLong(), groupId = 1, name = "laboratorio $it", isPlaceholder = true, simulatedScore = 15f) }
        )
        val snapshot = GradeInsights.snapshot(
            course,
            listOf(assignment(1, 19.0), assignment(2, 17.0), assignment(3, null, status = "upcoming")),
            listOf(placeholders),
            calculator
        )
        assertEquals(AverageSource.CANVAS, snapshot.averageSource)
        assertEquals(18.0, snapshot.currentAverage)
        assertEquals(false, snapshot.weightsConfigured)
        assertEquals(1, snapshot.pendingCount)
    }

    @Test
    fun courseWithoutGradesHasNoAverage() {
        val snapshot = GradeInsights.snapshot(course, listOf(assignment(1, null, status = "upcoming")), emptyList(), calculator)
        assertNull(snapshot.currentAverage)
        assertEquals(AverageSource.NONE, snapshot.averageSource)
    }

    @Test
    fun neededAverageUsesTheRemainingWeight() {
        // 15 de promedio con el 40% evaluado, meta 12: (1200 - 600) / 60 = 10.0 en el 60% restante.
        val needed = GradeInsights.neededAverage(currentAverage = 15.0, evaluatedPercent = 40.0, target = 12.0)
        assertEquals(10.0, needed.neededAverage)
        assertEquals(60.0, needed.remainingPercent)
        assertEquals(NeededGrade.Outcome.REACHABLE, needed.outcome)
    }

    @Test
    fun neededAverageDetectsSecuredAndImpossibleGoals() {
        assertEquals(NeededGrade.Outcome.ALREADY_SECURED, GradeInsights.neededAverage(19.0, 80.0, 12.0).outcome)
        assertEquals(NeededGrade.Outcome.IMPOSSIBLE, GradeInsights.neededAverage(5.0, 80.0, 12.0).outcome)
    }

    @Test
    fun withoutGradesTheWholeTargetIsNeeded() {
        val needed = GradeInsights.neededAverage(currentAverage = null, evaluatedPercent = 30.0, target = 12.0)
        assertEquals(12.0, needed.neededAverage)
        assertEquals(100.0, needed.remainingPercent)
    }

    @Test
    fun closedCourseReportsTheFinalResult() {
        val closed = GradeInsights.neededAverage(currentAverage = 13.0, evaluatedPercent = 100.0, target = 12.0)
        assertNull(closed.neededAverage)
        assertEquals(NeededGrade.Outcome.CLOSED_PASSED, closed.outcome)
    }

    @Test
    fun overallAverageIgnoresCoursesWithoutGrades() {
        val a = GradeInsights.snapshot(course, listOf(assignment(1, 18.0)), emptyList(), calculator)
        val b = GradeInsights.snapshot(course.copy(id = 2), listOf(assignment(2, null, status = "upcoming")), emptyList(), calculator)
        assertEquals(18.0, GradeInsights.overallAverage(listOf(a, b)))
        assertTrue(GradeInsights.overallAverage(listOf(b)) == null)
    }
}
