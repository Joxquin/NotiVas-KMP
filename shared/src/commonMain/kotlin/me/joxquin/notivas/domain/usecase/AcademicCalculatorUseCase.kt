package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.AssessmentVisualState
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.CourseAcademicSummary
import me.joxquin.notivas.data.model.EvaluatedGroupState
import me.joxquin.notivas.data.model.EvaluatedSimulationItem
import me.joxquin.notivas.data.model.GradePerformanceLevel
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import kotlin.math.max
import kotlin.math.roundToInt

class AcademicCalculatorUseCase {

    /**
     * Calcula el estado académico completo de un curso a partir de sus grupos y evaluaciones.
     */
    fun calculateCourseSummary(
        course: Course,
        groupsWithItems: List<SimulationGroupWithItems>
    ): CourseAcademicSummary {
        if (groupsWithItems.isEmpty()) {
            return CourseAcademicSummary(
                courseId = course.id,
                evaluatedProgressPercentage = 0f,
                currentAverage = null,
                projectedFinalGrade = 0f,
                passingGrade = course.passingGrade,
                isPassingProjected = false,
                pointsNeededToPass = course.passingGrade,
                performanceLevel = GradePerformanceLevel.AT_RISK,
                groups = emptyList(),
                hasConfiguredGroups = false
            )
        }

        var totalEvaluatedProgress = 0f
        var totalWeightedPoints = 0f
        var totalProjectedPoints = 0f
        var totalGroupWeightSum = 0f

        val evaluatedGroups = groupsWithItems.map { groupWithItems ->
            val group = groupWithItems.group
            val items = groupWithItems.items.sortedBy { it.orderIndex }
            totalGroupWeightSum += group.weightPercentage

            // 1. Identificar evaluaciones rendidas con nota real
            val renderedItems = items.filter { it.manualScore != null }
            val renderedCount = renderedItems.size

            // 2. Regla de descarte de la nota más baja (descartando solo una en caso de empate)
            var discardedItemId: Long? = null
            if (group.dropLowest && renderedCount >= group.minToDrop && renderedCount > 1) {
                val minItem = renderedItems.minByOrNull { it.manualScore ?: Float.MAX_VALUE }
                discardedItemId = minItem?.id
            }

            // 3. Asignar estado visual a cada item
            val evaluatedItems = items.map { item ->
                val state = when {
                    item.id == discardedItemId -> AssessmentVisualState.DISCARDED
                    item.manualScore != null -> AssessmentVisualState.ACTIVE
                    else -> AssessmentVisualState.PENDING
                }
                val displayScore = item.effectiveScore
                EvaluatedSimulationItem(
                    item = item,
                    visualState = state,
                    scoreToDisplay = if (item.manualScore != null || item.isSimulated) displayScore else null
                )
            }

            // 4. Promedio real del grupo (excluyendo descartadas)
            val validRendered = evaluatedItems.filter {
                it.visualState == AssessmentVisualState.ACTIVE && it.item.manualScore != null
            }
            val groupAverage = if (validRendered.isNotEmpty()) {
                validRendered.map { it.item.manualScore!! }.average().toFloat()
            } else {
                0f
            }

            // 5. Avance evaluado del grupo
            val target = max(1, group.targetAssessments)
            val fraction = (renderedCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            val effectiveGroupWeight = group.weightPercentage * fraction

            totalEvaluatedProgress += effectiveGroupWeight
            totalWeightedPoints += (groupAverage * effectiveGroupWeight)

            // 6. Proyección del grupo (incluyendo notas simuladas activas)
            val activeForSimulation = evaluatedItems.filter { it.visualState != AssessmentVisualState.DISCARDED }
            val groupProjectedAvg = if (activeForSimulation.isNotEmpty()) {
                activeForSimulation.map { it.item.effectiveScore }.average().toFloat()
            } else {
                0f
            }
            totalProjectedPoints += (groupProjectedAvg * (group.weightPercentage / 100f))

            EvaluatedGroupState(
                group = group,
                items = evaluatedItems,
                average = roundOneDecimal(groupAverage),
                assessedCount = renderedCount,
                effectiveWeightPercentage = effectiveGroupWeight,
                progressRatio = fraction
            )
        }

        // 7. Promedio Actual sobre lo evaluado
        val currentAvg = if (totalEvaluatedProgress > 0f) {
            roundOneDecimal(totalWeightedPoints / totalEvaluatedProgress)
        } else {
            null
        }

        val projectedGrade = roundOneDecimal(totalProjectedPoints)
        val passingGrade = course.passingGrade
        val isPassing = (currentAvg ?: projectedGrade) >= passingGrade
        val needed = max(0f, passingGrade - (currentAvg ?: 0f))

        val level = when {
            (currentAvg ?: 0f) >= 16.0f -> GradePerformanceLevel.EXCELLENT
            (currentAvg ?: 0f) >= passingGrade -> GradePerformanceLevel.PASSING
            else -> GradePerformanceLevel.AT_RISK
        }

        return CourseAcademicSummary(
            courseId = course.id,
            evaluatedProgressPercentage = roundOneDecimal(totalEvaluatedProgress.coerceIn(0f, 100f)),
            currentAverage = currentAvg,
            projectedFinalGrade = projectedGrade,
            passingGrade = passingGrade,
            isPassingProjected = isPassing,
            pointsNeededToPass = roundOneDecimal(needed),
            performanceLevel = level,
            groups = evaluatedGroups,
            hasConfiguredGroups = true
        )
    }

    private fun roundOneDecimal(value: Float): Float {
        return (value * 10f).roundToInt() / 10f
    }
}
