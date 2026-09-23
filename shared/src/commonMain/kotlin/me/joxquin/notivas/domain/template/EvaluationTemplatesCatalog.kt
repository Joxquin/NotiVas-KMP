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
                        ItemTemplate(
                            name = "Lab %02d".format(i),
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
        ),
        CourseEvaluationTemplate(
            id = "utp_pregrado_18w",
            name = "UTP - Prácticas Calificadas y Tareas",
            institution = "UTP",
            description = "Ciclo regular de 18 semanas con 4 Prácticas Calificadas (descarte de peor PC), 4 Tareas Académicas y Examen Final.",
            totalWeeks = 18,
            passingGrade = 13.0f,
            groups = listOf(
                GroupTemplate(
                    name = "Prácticas Calificadas (PC)",
                    weightPercentage = 40.0f,
                    targetAssessments = 4,
                    dropLowest = true,
                    minToDrop = 3,
                    calculationMode = "DROP_LOWEST",
                    items = (1..4).map { i ->
                        ItemTemplate(
                            name = "PC $i",
                            weekNumber = i * 4,
                            matchingPatterns = listOf("(?i).*(pc|pr[aá]ctica\\s*calificada).*0?$i.*")
                        )
                    }
                ),
                GroupTemplate(
                    name = "Tareas y Actividades Continuas",
                    weightPercentage = 30.0f,
                    targetAssessments = 4,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = (1..4).map { i ->
                        ItemTemplate(
                            name = "Tarea Académica $i",
                            weekNumber = i * 3,
                            matchingPatterns = listOf("(?i).*(tarea|ta|foro|actividad|avance).*0?$i.*")
                        )
                    }
                ),
                GroupTemplate(
                    name = "Examen Final",
                    weightPercentage = 30.0f,
                    targetAssessments = 1,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = listOf(
                        ItemTemplate(
                            name = "Examen Final Individual",
                            weekNumber = 18,
                            matchingPatterns = listOf("(?i).*(examen\\s*final|ef|evaluaci[oó]n\\s*final).*")
                        )
                    )
                )
            )
        ),
        CourseEvaluationTemplate(
            id = "generica_universitaria_16w",
            name = "Evaluación Continua y Exámenes (Genérica)",
            institution = "General",
            description = "Esquema universal de 16 semanas: 60% Evaluación Continua (6 notas), 20% Parcial y 20% Final.",
            totalWeeks = 16,
            passingGrade = 12.0f,
            groups = listOf(
                GroupTemplate(
                    name = "Evaluación Continua",
                    weightPercentage = 60.0f,
                    targetAssessments = 6,
                    dropLowest = true,
                    minToDrop = 3,
                    calculationMode = "DROP_LOWEST",
                    items = (1..6).map { i ->
                        ItemTemplate(
                            name = "Evaluación Continua $i",
                            weekNumber = i * 2,
                            matchingPatterns = listOf("(?i).*(evaluaci[oó]n|continua|ec|lab|taller|pc).*0?$i.*")
                        )
                    }
                ),
                GroupTemplate(
                    name = "Examen Parcial",
                    weightPercentage = 20.0f,
                    targetAssessments = 1,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = listOf(
                        ItemTemplate(
                            name = "Examen Parcial",
                            weekNumber = 8,
                            matchingPatterns = listOf("(?i).*(parcial|ep|medio\\s*curso).*")
                        )
                    )
                ),
                GroupTemplate(
                    name = "Examen Final",
                    weightPercentage = 20.0f,
                    targetAssessments = 1,
                    dropLowest = false,
                    calculationMode = "SIMPLE",
                    items = listOf(
                        ItemTemplate(
                            name = "Examen Final",
                            weekNumber = 16,
                            matchingPatterns = listOf("(?i).*(final|ef).*")
                        )
                    )
                )
            )
        )
    )

    fun findById(id: String): CourseEvaluationTemplate? = templates.find { it.id == id }
}
