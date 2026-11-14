package me.joxquin.notivas.domain.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.joxquin.notivas.util.SelectedDocument

@Serializable
data class CriterionEvaluation(
    @SerialName("title") val title: String,
    @SerialName("points_earned") val pointsEarned: Double = 0.0,
    @SerialName("max_points") val maxPoints: Double = 0.0,
    @SerialName("level") val level: String = "",
    @SerialName("feedback") val feedback: String = ""
)

@Serializable
data class AssignmentPreReviewReport(
    @SerialName("overall_score") val overallScore: Double? = null,
    @SerialName("max_score") val maxScore: Double = 20.0,
    @SerialName("percentage") val percentage: Int = 0,
    @SerialName("summary") val summary: String = "",
    @SerialName("criteria_evaluations") val criteriaEvaluations: List<CriterionEvaluation> = emptyList(),
    @SerialName("strengths") val strengths: List<String> = emptyList(),
    @SerialName("improvements") val improvements: List<String> = emptyList(),
    @SerialName("module_alignment") val moduleAlignment: List<String> = emptyList(),
    val rawMarkdown: String = ""
)

enum class ReviewMode {
    QUICK,
    EXHAUSTIVE,
    CUSTOM
}

data class ReviewOptions(
    val mode: ReviewMode = ReviewMode.QUICK,
    val checkInstructions: Boolean = true,
    val checkRubric: Boolean = true,
    val checkModules: Boolean = true,
    val deepModuleScan: Boolean = false,
    val inspectImages: Boolean = false,
    val referenceDocument: SelectedDocument? = null
)

enum class ReviewProgressStep {
    IDLE,
    READING_RUBRIC,
    FETCHING_MODULES,
    DOWNLOADING_MODULE_RESOURCES,
    EXTRACTING_DOCUMENT,
    EVALUATING,
    COMPLETED,
    ERROR
}

