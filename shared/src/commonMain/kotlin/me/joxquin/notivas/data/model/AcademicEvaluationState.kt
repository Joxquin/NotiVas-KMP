package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

enum class AssessmentVisualState {
    ACTIVE,
    DISCARDED,
    PENDING
}

enum class GradePerformanceLevel {
    EXCELLENT,   // >= 16.0
    PASSING,     // 13.0 - 15.9
    AT_RISK      // < 13.0
}

@Serializable
data class EvaluatedSimulationItem(
    val item: SimulationItem,
    val visualState: AssessmentVisualState,
    val scoreToDisplay: Float?
)

@Serializable
data class EvaluatedGroupState(
    val group: SimulationGroup,
    val items: List<EvaluatedSimulationItem>,
    val average: Float,
    val assessedCount: Int,
    val effectiveWeightPercentage: Float,
    val progressRatio: Float // 0.0 to 1.0
)

@Serializable
data class CourseAcademicSummary(
    val courseId: Long,
    val evaluatedProgressPercentage: Float, // e.g. 45.0%
    val currentAverage: Float?,             // null if 0% assessed
    val projectedFinalGrade: Float,
    val passingGrade: Float,
    val isPassingProjected: Boolean,
    val pointsNeededToPass: Float,
    val performanceLevel: GradePerformanceLevel,
    val groups: List<EvaluatedGroupState>,
    val hasConfiguredGroups: Boolean
)
