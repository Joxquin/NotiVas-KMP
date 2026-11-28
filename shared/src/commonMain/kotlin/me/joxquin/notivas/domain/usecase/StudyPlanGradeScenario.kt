package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import kotlin.math.abs
import kotlin.math.roundToInt

internal class StudyPlanGradeScenario(private val calculator: AcademicCalculatorUseCase) {
    fun improvement(
        course: Course,
        groups: List<SimulationGroupWithItems>,
        assignmentId: Long
    ): Float? {
        if (course.passingGrade !in 0f..20f) return null
        if (abs(groups.sumOf { it.group.weightPercentage.toDouble() } - 100.0) > 0.01) return null
        if (groups.any { !it.hasSupportedWeights() }) return null

        val matchingGroup = groups.singleOrNull { group ->
            group.items.any { it.canvasAssignmentId == assignmentId }
        } ?: return null
        val matchingItem = matchingGroup.items.singleOrNull { it.canvasAssignmentId == assignmentId } ?: return null
        if (matchingItem.isPlaceholder || matchingItem.manualScore != null || matchingGroup.group.weightPercentage == 0f) {
            return null
        }

        fun projection(score: Float): Float {
            val scenarioGroups = groups.map { group ->
                if (group != matchingGroup) group
                else group.copy(items = group.items.map { item ->
                    if (item == matchingItem) item.copy(isSimulated = true, simulatedScore = score) else item
                })
            }
            return calculator.calculateCourseSummary(course, scenarioGroups).projectedFinalGrade
        }

        return ((projection(16f) - projection(11f)) * 10f).roundToInt().div(10f).takeIf { it > 0f }
    }

    private fun SimulationGroupWithItems.hasSupportedWeights(): Boolean =
        group.weightPercentage in 0f..100f &&
            group.calculationMode == "SIMPLE" && !group.dropLowest &&
            group.targetAssessments > 0 && items.size == group.targetAssessments &&
            items.all {
                it.maxScore == 20f && it.effectiveScore in 0f..20f &&
                    it.internalWeight == items.first().internalWeight && it.internalWeight > 0f
            }
}
