package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StudyPlanGradeScenarioTest {
    private val calculator = StudyPlanGradeScenario(AcademicCalculatorUseCase())
    private val course = Course(1, "Redes")
    private val practice = SimulationGroupWithItems(
        SimulationGroup(1, course.id, "Práctica", 20f),
        listOf(SimulationItem(1, 1, canvasAssignmentId = 9, name = "Informe"))
    )
    private val exam = SimulationGroupWithItems(
        SimulationGroup(2, course.id, "Examen", 80f),
        listOf(SimulationItem(2, 2, name = "Examen final", manualScore = 14f))
    )

    @Test
    fun projectsAOnePointDifferenceAtTwentyPercentWithoutChangingSavedGrades() {
        assertEquals(1f, calculator.improvement(course, listOf(practice, exam), 9))
        assertEquals(0f, practice.items.single().simulatedScore)
        assertEquals(false, practice.items.single().isSimulated)
    }

    @Test
    fun skipsUnlinkedAndIncompleteFormulas() {
        assertNull(calculator.improvement(course, listOf(practice, exam), 50))
        assertNull(calculator.improvement(course, listOf(practice), 9))
        assertNull(calculator.improvement(course, listOf(practice, exam.copy(group = exam.group.copy(targetAssessments = 2))), 9))
    }

    @Test
    fun skipsAmbiguousLinksAndAlreadyGradedAssignments() {
        val ambiguousExam = exam.copy(items = listOf(exam.items.single().copy(canvasAssignmentId = 9)))
        val gradedPractice = practice.copy(items = listOf(practice.items.single().copy(manualScore = 12f)))

        assertNull(calculator.improvement(course, listOf(practice, ambiguousExam), 9))
        assertNull(calculator.improvement(course, listOf(gradedPractice, exam), 9))
    }
}
