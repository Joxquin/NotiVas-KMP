package me.joxquin.notivas.domain.template

import kotlinx.serialization.Serializable

@Serializable
data class ItemTemplate(
    val name: String,
    val weekNumber: Int? = null,
    val maxScore: Float = 20f,
    val internalWeight: Float = 1.0f,
    val matchingPatterns: List<String> = emptyList() // Regex or prefixes
)

@Serializable
data class GroupTemplate(
    val name: String,
    val weightPercentage: Float,
    val targetAssessments: Int,
    val dropLowest: Boolean = false,
    val minToDrop: Int = 3,
    val calculationMode: String = "SIMPLE",
    val items: List<ItemTemplate>
)

@Serializable
data class CourseEvaluationTemplate(
    val id: String,
    val name: String,
    val institution: String,
    val description: String,
    val totalWeeks: Int = 16,
    val passingGrade: Float = 13.0f,
    val groups: List<GroupTemplate>
)

object EvaluationTemplatesCatalog {

    val templates: List<CourseEvaluationTemplate> = listOf(
        CourseEvaluationTemplate(
            id = "tecsup_standard_16w",
            name = "Tecsup - Laboratorio y Proyecto",
            institution = "Tecsup",
            description = "Estructura estándar de 16 semanas con 8 laboratorios (descarte de peor nota), 4 pruebas de avance y proyecto final.",
            totalWeeks = 16,
            passingGrade = 13.0f,
            groups = listOf(
                GroupTemplate(
                    name = "Laboratorios y Talleres",
                    weightPercentage = 40.0f,
                    targetAssessments = 8,
                    dropLowest = true,
                    minToDrop = 3,
                    calculationMode = "DROP_LOWEST",
                    items = (1..8).map { i ->
                        val padded = if (i < 10) "0$i" else "$i"
                        ItemTemplate(
                            name = "Lab $padded",
                            weekNumber = i * 2 - 1,
                            matchingPatterns = listOf("(?i).*(lab|laboratorio|plab|taller|gu[ií]a).*0?$i.*")
                        )
                    }
                ),
                GroupTemplate(
                    name = "Pruebas de Avance / PCs",
                    weightPercentage = 30.0f,
                    targetAssessments = 4,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = (1..4).map { i ->
                        ItemTemplate(
                            name = "Prueba de Avance $i",
                            weekNumber = i * 4 - 1,
                            matchingPatterns = listOf("(?i).*(prueba|avance|pc|evaluaci[oó]n|test).*0?$i.*")
                        )
                    }
                ),
                GroupTemplate(
                    name = "Proyecto Integrador Final",
                    weightPercentage = 30.0f,
                    targetAssessments = 1,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = listOf(
                        ItemTemplate(
                            name = "Entrega y Sustentación Final",
                            weekNumber = 16,
                            matchingPatterns = listOf("(?i).*(proyecto|sustentaci[oó]n|final|integrador).*")
                        )
                    )
                )
            )
        )
    )

    fun findById(id: String): CourseEvaluationTemplate? = templates.find { it.id == id }
}
