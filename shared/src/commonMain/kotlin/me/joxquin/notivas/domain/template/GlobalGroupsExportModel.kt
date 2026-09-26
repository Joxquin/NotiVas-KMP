package me.joxquin.notivas.domain.template

import kotlinx.serialization.Serializable

@Serializable
data class GlobalGroupExportItem(
    val name: String,
    val weekNumber: Int? = null,
    val maxScore: Float = 20f,
    val internalWeight: Float = 1.0f,
    val matchingPatterns: List<String> = emptyList()
)

@Serializable
data class GlobalGroupExportGroup(
    val name: String,
    val weightPercentage: Float,
    val targetAssessments: Int,
    val dropLowest: Boolean = false,
    val minToDrop: Int = 3,
    val calculationMode: String = "SIMPLE",
    val items: List<GlobalGroupExportItem> = emptyList()
)

@Serializable
data class CourseGroupsExport(
    val courseId: Long? = null,
    val courseCode: String? = null,
    val courseName: String? = null,
    val passingGrade: Float = 13f,
    val totalWeeks: Int = 16,
    val groups: List<GlobalGroupExportGroup> = emptyList()
)

@Serializable
data class GlobalGroupsExportFile(
    val version: Int = 1,
    val exportedAt: String? = null,
    val app: String = "NotiVas",
    val courses: List<CourseGroupsExport> = emptyList(),
    /**
     * Plantilla base reutilizable para importar como estructura global genérica a cualquier curso.
     */
    val defaultTemplate: CourseEvaluationTemplate? = null
)
