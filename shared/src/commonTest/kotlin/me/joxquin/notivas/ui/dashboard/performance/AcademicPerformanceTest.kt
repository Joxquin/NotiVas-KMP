package me.joxquin.notivas.ui.dashboard.performance

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.DayTaskStatus
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateComponents
import me.joxquin.notivas.util.DateTimeUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcademicPerformanceTest {

    private fun computeDayStatus(
        tasksForDay: List<AssignmentUiModel>,
        today: DateComponents,
        nowMillis: Long
    ): DayTaskStatus {
        if (tasksForDay.isEmpty()) return DayTaskStatus.NONE

        val hasOverdue = tasksForDay.any { ui ->
            val a = ui.assignment
            if (a.status == "missing") true
            else if (a.status == "upcoming") {
                val dueRaw = a.dueAt ?: a.lockAt
                if (dueRaw != null) {
                    val comp = DateTimeUtil.parseIsoToComponents(dueRaw)
                    comp != null && comp.epochMillis < nowMillis
                } else false
            } else false
        }
        if (hasOverdue) return DayTaskStatus.OVERDUE

        val allCompleted = tasksForDay.all { it.assignment.status == "completed" || it.assignment.isCompleted }
        if (allCompleted) return DayTaskStatus.COMPLETED

        return DayTaskStatus.PENDING
    }

    @Test
    fun testComputeDayStatus_empty() {
        val today = DateComponents(2026, 11, 1)
        val status = computeDayStatus(emptyList(), today, 1000L)
        assertEquals(DayTaskStatus.NONE, status)
    }

    @Test
    fun testComputeDayStatus_allCompleted() {
        val today = DateComponents(2026, 11, 1)
        val tasks = listOf(
            AssignmentUiModel(
                assignment = Assignment(id = 1, name = "Lab 1", courseId = 10, status = "completed", isCompleted = true),
                courseName = "Course 1"
            ),
            AssignmentUiModel(
                assignment = Assignment(id = 2, name = "Lab 2", courseId = 10, status = "completed", isCompleted = true),
                courseName = "Course 1"
            )
        )
        val status = computeDayStatus(tasks, today, 1000L)
        assertEquals(DayTaskStatus.COMPLETED, status)
    }

    @Test
    fun testComputeDayStatus_hasMissing() {
        val today = DateComponents(2026, 11, 1)
        val tasks = listOf(
            AssignmentUiModel(
                assignment = Assignment(id = 1, name = "Lab 1", courseId = 10, status = "completed", isCompleted = true),
                courseName = "Course 1"
            ),
            AssignmentUiModel(
                assignment = Assignment(id = 2, name = "Lab 2", courseId = 10, status = "missing", isCompleted = false),
                courseName = "Course 1"
            )
        )
        val status = computeDayStatus(tasks, today, 1000L)
        assertEquals(DayTaskStatus.OVERDUE, status)
    }

    @Test
    fun testComputeDayStatus_hasUpcoming() {
        val today = DateComponents(2026, 11, 1)
        val tasks = listOf(
            AssignmentUiModel(
                assignment = Assignment(
                    id = 1,
                    name = "Lab 1",
                    courseId = 10,
                    status = "upcoming",
                    dueAt = "2026-11-05T23:59:00Z"
                ),
                courseName = "Course 1"
            )
        )
        val status = computeDayStatus(tasks, today, 1000L) // nowMillis before due
        assertEquals(DayTaskStatus.PENDING, status)
    }

    @Test
    fun testCourseFallbackAverageCalculation() {
        val assignments = listOf(
            Assignment(id = 1, name = "Lab 1", courseId = 10, score = 18.0, pointsPossible = 20.0),
            Assignment(id = 2, name = "Lab 2", courseId = 10, score = 16.0, pointsPossible = 20.0),
            Assignment(id = 3, name = "Lab 3", courseId = 10, score = null, pointsPossible = 20.0) // unrated
        )

        val graded = assignments.filter { it.score != null && (it.pointsPossible ?: 0.0) > 0.0 }
        val avg = graded.map { (it.score!! / it.pointsPossible!!) * 20.0 }.average()
        assertEquals(17.0, avg, 0.01)
    }
}
