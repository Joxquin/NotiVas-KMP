package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.domain.grades.AverageSource
import me.joxquin.notivas.domain.grades.CourseGradeSnapshot
import me.joxquin.notivas.domain.grades.GradeInsights
import me.joxquin.notivas.domain.grades.NeededGrade
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase

/**
 * Notas calculadas por la propia app (las mismas cifras que Rendimiento), sin consultar Canvas:
 * una sola llamada corta en vez de pedir las tareas curso por curso.
 */
class GradesToolsHandler(
    private val localStore: InMemoryLocalStore,
    private val calculator: AcademicCalculatorUseCase
) : CopilotToolHandler {

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            function = OpenRouterFunction(
                name = SUMMARY,
                description = "Promedios actuales del estudiante calculados por NotiVas (escala 0-20): por curso y general, " +
                    "con tareas calificadas, pendientes y vencidas, nota aprobatoria y estado. Sin 'course' devuelve todos los cursos; " +
                    "con 'course' devuelve el detalle de ese curso (últimas notas y grupos del simulador). Úsala para cualquier pregunta sobre notas o promedios.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course" to OpenRouterProperty(
                            type = "string",
                            description = "Opcional. ID, código o parte del nombre del curso (ej. 'CPS', 'Construcción y Pruebas')."
                        )
                    )
                )
            )
        ),
        OpenRouterTool(
            function = OpenRouterFunction(
                name = NEEDED,
                description = "Calcula el promedio mínimo que el estudiante necesita en lo que falta evaluar para terminar con la nota meta " +
                    "(por defecto, la nota aprobatoria). Úsala para '¿cuánto necesito para aprobar?' o '¿cuánto necesito en el examen final?'. " +
                    "Sin 'course' calcula todos los cursos en una sola llamada; con 'course', solo ese curso.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course" to OpenRouterProperty(
                            type = "string",
                            description = "Opcional. ID, código o parte del nombre del curso. Omítelo si pregunta por todos sus cursos."
                        ),
                        "target_grade" to OpenRouterProperty(
                            type = "number",
                            description = "Opcional. Nota final deseada (0-20). Si se omite se usa la nota aprobatoria de cada curso."
                        )
                    )
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean = toolName == SUMMARY || toolName == NEEDED

    override suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult {
        val courses = localStore.getAllCourses().first()
        if (courses.isEmpty()) {
            return ToolExecutionResult("""{"error": "Todavía no hay cursos sincronizados en NotiVas."}""")
        }
        val assignmentsByCourse = localStore.getAssignmentList().groupBy { it.courseId }
        val groupsByCourse = localStore.getAllGroupsWithItemsList().groupBy { it.group.courseId }
        fun snapshotOf(course: Course) = GradeInsights.snapshot(
            course,
            assignmentsByCourse[course.id].orEmpty(),
            groupsByCourse[course.id].orEmpty(),
            calculator
        )

        val query = args.text("course")
        val course = if (query != null) findCourse(courses, query) else null
        if (query != null && course == null) {
            return ToolExecutionResult(notFound(query, courses))
        }

        return when (toolName) {
            SUMMARY -> {
                val json = if (course != null) {
                    detail(snapshotOf(course))
                } else {
                    val snapshots = courses.map(::snapshotOf)
                    buildJsonObject {
                        put("escala", "0-20")
                        GradeInsights.overallAverage(snapshots)?.let { put("promedio_general", GradeInsights.round1(it)) }
                        put("cursos", buildJsonArray { snapshots.forEach { add(compact(it)) } })
                        put("nota", "promedio_general = media de los cursos con notas, igual que la pantalla Inicio de NotiVas.")
                    }
                }
                ToolExecutionResult(json.toString(), source = SOURCE)
            }
            NEEDED -> {
                val goal = args.number("target_grade")
                fun neededFor(s: CourseGradeSnapshot) =
                    GradeInsights.neededAverage(s.currentAverage, s.evaluatedPercent, goal ?: s.course.passingGrade.toDouble())
                val json = if (course != null) {
                    val snapshot = snapshotOf(course)
                    needed(snapshot, neededFor(snapshot))
                } else {
                    buildJsonObject {
                        put("escala", "0-20")
                        put("cursos", buildJsonArray {
                            courses.map(::snapshotOf).forEach { s -> add(neededCompact(s, neededFor(s))) }
                        })
                        put(
                            "metodo",
                            "ponderado=true usa los grupos del simulador de NotiVas; ponderado=false es aproximado (cada tarea pesa igual)."
                        )
                    }
                }
                ToolExecutionResult(json.toString(), source = SOURCE)
            }
            else -> ToolExecutionResult("""{"error": "Herramienta no soportada por GradesToolsHandler"}""")
        }
    }

    private fun compact(s: CourseGradeSnapshot) = buildJsonObject {
        put("id", s.course.id)
        put("curso", shortName(s.course))
        s.course.courseCode?.let { put("codigo", it) }
        s.currentAverage?.let { put("promedio", GradeInsights.round1(it)) }
        put("estado", status(s))
        put("calificadas", s.gradedCount)
        put("pendientes", s.pendingCount)
        put("vencidas", s.missingCount)
        put("nota_aprobatoria", s.course.passingGrade.toDouble())
    }

    private fun detail(s: CourseGradeSnapshot) = buildJsonObject {
        put("escala", "0-20")
        put("id", s.course.id)
        put("curso", shortName(s.course))
        s.course.courseCode?.let { put("codigo", it) }
        s.currentAverage?.let { put("promedio", GradeInsights.round1(it)) }
        put("fuente_promedio", source(s.averageSource))
        put("estado", status(s))
        put("nota_aprobatoria", s.course.passingGrade.toDouble())
        put("calificadas", s.gradedCount)
        put("pendientes", s.pendingCount)
        put("vencidas", s.missingCount)
        put("avance_evaluado_pct", GradeInsights.round1(s.evaluatedPercent))
        put("ponderaciones_configuradas", s.weightsConfigured)
        s.projectedFinal?.let { put("proyeccion_final_simulador", GradeInsights.round1(it)) }
        put("ultimas_notas", buildJsonArray {
            s.recentGrades.forEach { g ->
                add(buildJsonObject {
                    put("evaluacion", g.name)
                    put("nota", g.score20)
                })
            }
        })
    }

    private fun needed(s: CourseGradeSnapshot, n: NeededGrade) = buildJsonObject {
        put("curso", shortName(s.course))
        n.currentAverage?.let { put("promedio_actual", GradeInsights.round1(it)) }
        put("meta", n.target)
        put("avance_evaluado_pct", n.evaluatedPercent)
        put("restante_pct", n.remainingPercent)
        n.neededAverage?.let { put("promedio_necesario_en_lo_restante", it) }
        put("resultado", outcome(n.outcome))
        put(
            "metodo",
            if (s.weightsConfigured) "ponderado con los grupos del simulador de NotiVas"
            else "aproximado: sin ponderaciones configuradas, cada tarea pesa igual. Sugiere configurar los grupos del sílabo en el simulador para un cálculo exacto."
        )
    }

    /** Una fila por curso para "cuánto necesito" en todos los cursos a la vez. */
    private fun neededCompact(s: CourseGradeSnapshot, n: NeededGrade) = buildJsonObject {
        put("id", s.course.id)
        put("curso", shortName(s.course))
        n.currentAverage?.let { put("promedio_actual", GradeInsights.round1(it)) }
        put("meta", n.target)
        put("restante_pct", n.remainingPercent)
        n.neededAverage?.let { put("promedio_necesario_en_lo_restante", it) }
        put("resultado", outcome(n.outcome))
        put("ponderado", s.weightsConfigured)
    }

    private fun outcome(outcome: NeededGrade.Outcome) = when (outcome) {
        NeededGrade.Outcome.ALREADY_SECURED -> "ya_asegurado"
        NeededGrade.Outcome.REACHABLE -> "alcanzable"
        NeededGrade.Outcome.IMPOSSIBLE -> "imposible_con_lo_que_queda"
        NeededGrade.Outcome.CLOSED_PASSED -> "curso_cerrado_aprobado"
        NeededGrade.Outcome.CLOSED_FAILED -> "curso_cerrado_desaprobado"
    }

    private fun status(s: CourseGradeSnapshot): String {
        val avg = s.currentAverage ?: return "sin_notas"
        return when {
            avg >= 16.0 -> "excelente"
            avg >= s.course.passingGrade -> "aprobando"
            else -> "en_riesgo"
        }
    }

    private fun source(source: AverageSource) = when (source) {
        AverageSource.SIMULATOR -> "simulador_ponderado"
        AverageSource.CANVAS -> "canvas_promedio_simple"
        AverageSource.NONE -> "sin_notas"
    }

    /** "Construcción y Pruebas de Software - C24 4to C-A - ..." -> "Construcción y Pruebas de Software". */
    private fun shortName(course: Course) = course.name.substringBefore(" - ").trim().ifEmpty { course.name }

    private fun notFound(query: String, courses: List<Course>) = buildJsonObject {
        put("error", "No encontré el curso '$query'.")
        put("cursos_disponibles", buildJsonArray { courses.forEach { add(JsonPrimitive("${it.id}: ${shortName(it)}")) } })
    }.toString()

    private fun JsonObject.text(key: String): String? =
        (get(key) as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }

    private fun JsonObject.number(key: String): Double? =
        (get(key) as? JsonPrimitive)?.let { it.doubleOrNull ?: it.contentOrNull?.toDoubleOrNull() }

    companion object {
        const val SUMMARY = "get_grades_summary"
        const val NEEDED = "calculate_needed_grade"
        private val SOURCE = CopilotSource(title = "Notas de NotiVas", detail = "Promedios calculados en la app con tus notas de Canvas")

        /** Busca por ID, por código o por nombre (sin tildes ni mayúsculas). */
        fun findCourse(courses: List<Course>, query: String): Course? {
            query.trim().toLongOrNull()?.let { id -> courses.firstOrNull { it.id == id }?.let { return it } }
            val q = normalize(query)
            return courses.firstOrNull { normalize(it.courseCode.orEmpty()) == q }
                ?: courses.firstOrNull { normalize(it.name).contains(q) }
                ?: courses.firstOrNull { c -> q.split(" ").filter { it.length > 2 }.let { words -> words.isNotEmpty() && words.all { normalize(c.name).contains(it) } } }
        }

        private fun normalize(text: String): String = text.lowercase()
            .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')
            .replace(Regex("\\s+"), " ").trim()
    }
}
