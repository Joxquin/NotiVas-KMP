package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.data.repository.copilot.tools.HtmlUtils

class CanvasAcademicToolsHandler(
    private val canvasApiService: CanvasApiService,
    private val localStore: InMemoryLocalStore,
    private val preferencesManager: PreferencesManager
) : CopilotToolHandler {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "get_academic_overview",
                description = "Obtiene un resumen global de todos los cursos inscritos y tareas pendientes de entrega.",
                parameters = OpenRouterParameters(
                    properties = emptyMap(),
                    required = emptyList()
                )
            )
        ),
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "get_course_assignments",
                description = "Obtiene la lista detallada de tareas, evaluaciones y estados de entrega de un curso específico de Canvas LMS.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID del curso en Canvas (opcional si se deduce del contexto)"
                        )
                    )
                )
            )
        ),
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "fetch_canvas_assignment_details",
                description = "Obtiene los detalles en vivo de una tarea o evaluación en Canvas LMS, incluyendo consigna completa, rúbrica de calificación y entrega del estudiante.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "assignment_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID de la tarea en Canvas LMS"
                        ),
                        "course_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID del curso en Canvas LMS (opcional)"
                        )
                    ),
                    required = listOf("assignment_id")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean {
        return toolName in setOf("get_academic_overview", "get_course_assignments", "fetch_canvas_assignment_details")
    }

    override suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult {
        val courses: List<Course> = localStore.courses.first()
        val allAssignments: List<Assignment> = localStore.assignments.first()

        return when (toolName) {
            "get_academic_overview" -> {
                if (courses.isEmpty()) {
                    ToolExecutionResult("""{"message": "No hay cursos registrados en el almacenamiento local."}""")
                } else {
                    val summaryList = courses.map { course ->
                        val courseAssignments = allAssignments.filter { it.courseId == course.id }
                        val pendingAssignments = courseAssignments.filter { !it.isCompleted }
                        mapOf(
                            "course_id" to course.id.toString(),
                            "name" to course.name,
                            "course_code" to (course.courseCode ?: "N/A"),
                            "pending_assignments_count" to pendingAssignments.size.toString(),
                            "next_deadlines" to pendingAssignments.take(3).map { a ->
                                mapOf(
                                    "id" to a.id.toString(),
                                    "name" to a.name,
                                    "due_at" to (a.dueAt ?: "Sin fecha")
                                )
                            }
                        )
                    }
                    val source = CopilotSource(
                        title = "Resumen Académico (${courses.size} cursos)",
                        detail = "Estado de cursos y entregas pendientes"
                    )
                    ToolExecutionResult(json.encodeToString(summaryList), source)
                }
            }

            "get_course_assignments" -> {
                val cid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId
                if (cid == null) {
                    val source = CopilotSource(
                        title = "Todas las Tareas (${allAssignments.size} items)",
                        detail = "Lista global de tareas"
                    )
                    val simplified = allAssignments.map { a ->
                        mapOf(
                            "id" to a.id.toString(),
                            "course_id" to a.courseId.toString(),
                            "name" to a.name,
                            "due_at" to (a.dueAt ?: "Sin fecha"),
                            "points_possible" to (a.pointsPossible?.toString() ?: "N/A"),
                            "submitted" to a.isCompleted.toString(),
                            "score" to (a.submission?.score?.toString() ?: "N/A")
                        )
                    }
                    ToolExecutionResult(json.encodeToString(simplified), source)
                } else {
                    val course = courses.find { it.id == cid }
                    val courseName = course?.name ?: "Curso $cid"
                    val assignments = allAssignments.filter { it.courseId == cid }
                    val source = CopilotSource(
                        title = "$courseName (${assignments.size} tareas)",
                        detail = "Tareas y estado de entregas"
                    )
                    val simplified = assignments.map { a ->
                        mapOf(
                            "id" to a.id.toString(),
                            "name" to a.name,
                            "due_at" to (a.dueAt ?: "Sin fecha"),
                            "points_possible" to (a.pointsPossible?.toString() ?: "N/A"),
                            "submitted" to a.isCompleted.toString(),
                            "score" to (a.submission?.score?.toString() ?: "N/A"),
                            "grade" to (a.submission?.grade ?: "N/A")
                        )
                    }
                    ToolExecutionResult(json.encodeToString(simplified), source)
                }
            }

            "fetch_canvas_assignment_details" -> {
                val aid = args["assignment_id"]?.jsonPrimitive?.longOrNull
                if (aid == null) {
                    ToolExecutionResult("""{"error": "Falta el parámetro 'assignment_id'."}""")
                } else {
                    val rawToken = preferencesManager.accessToken.first()
                    val domain = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"

                    var resolvedCid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId
                    if (resolvedCid == null) {
                        val match = allAssignments.find { it.id == aid }
                        if (match != null) {
                            resolvedCid = match.courseId
                        }
                    }

                    if (rawToken.isNullOrBlank() || resolvedCid == null) {
                        // Fallback a almacenamiento local
                        val localMatch = allAssignments.find { it.id == aid }
                        if (localMatch != null) {
                            val cleanDesc = localMatch.description?.let { HtmlUtils.cleanHtml(it) } ?: "Sin consigna detallada"
                            val source = CopilotSource(
                                title = "${localMatch.name} (Caché local)",
                                detail = "Detalle de tarea"
                            )
                            val resultMap = mapOf(
                                "id" to localMatch.id.toString(),
                                "name" to localMatch.name,
                                "due_at" to (localMatch.dueAt ?: "Sin fecha"),
                                "points_possible" to (localMatch.pointsPossible?.toString() ?: "N/A"),
                                "description" to cleanDesc.take(3000),
                                "submitted" to localMatch.isCompleted.toString()
                            )
                            ToolExecutionResult(json.encodeToString(resultMap), source)
                        } else {
                            ToolExecutionResult("""{"error": "No se encontró la tarea $aid en el almacenamiento local."}""")
                        }
                    } else {
                        val course = courses.find { it.id == resolvedCid }
                        val courseName = course?.name ?: "Curso $resolvedCid"
                        val token = "Bearer $rawToken"

                        try {
                            val details = canvasApiService.getAssignmentDetails(domain, token, resolvedCid, aid)
                            val cleanDesc = details.description?.let { HtmlUtils.cleanHtml(it) } ?: "Sin consigna detallada"

                            val source = CopilotSource(
                                title = "${details.name} ($courseName)",
                                detail = "Consigna y rúbrica en vivo de Canvas LMS"
                            )

                            val submissionInfo = details.submission?.let { sub ->
                                mapOf(
                                    "submitted_at" to (sub.submittedAt ?: "No entregado"),
                                    "score" to (sub.score?.toString() ?: "Sin calificación"),
                                    "grade" to (sub.grade ?: "N/A"),
                                    "workflow_state" to (sub.workflowState ?: "unsubmitted"),
                                    "late" to sub.late.toString(),
                                    "missing" to sub.missing.toString()
                                )
                            }

                            val resultMap = mutableMapOf<String, String?>(
                                "id" to details.id.toString(),
                                "name" to details.name,
                                "due_at" to details.dueAt,
                                "points_possible" to details.pointsPossible?.toString(),
                                "description" to cleanDesc.take(4000),
                                "submission_types" to details.submissionTypes?.joinToString(", "),
                                "student_submission" to submissionInfo?.let { json.encodeToString(it) }
                            )

                            if (!details.rubric.isNullOrEmpty()) {
                                resultMap["rubric"] = details.rubric.joinToString("\n") { r ->
                                    "• [${r.points ?: 0.0} pts] ${r.description ?: ""}: ${r.longDescription ?: ""}"
                                }
                            }

                            ToolExecutionResult(json.encodeToString(resultMap.filterValues { it != null }), source)
                        } catch (e: Exception) {
                            // Fallback local
                            val localMatch = allAssignments.find { it.id == aid }
                            if (localMatch != null) {
                                val cleanDesc = localMatch.description?.let { HtmlUtils.cleanHtml(it) } ?: "Sin consigna"
                                val source = CopilotSource(
                                    title = "${localMatch.name} ($courseName)",
                                    detail = "Detalle local (Canvas offline)"
                                )
                                val resultMap = mapOf(
                                    "id" to localMatch.id.toString(),
                                    "name" to localMatch.name,
                                    "due_at" to (localMatch.dueAt ?: "Sin fecha"),
                                    "points_possible" to (localMatch.pointsPossible?.toString() ?: "N/A"),
                                    "description" to cleanDesc.take(3000)
                                )
                                ToolExecutionResult(json.encodeToString(resultMap), source)
                            } else {
                                ToolExecutionResult("""{"error": "Error al consultar Canvas: ${e.message}"}""")
                            }
                        }
                    }
                }
            }

            else -> ToolExecutionResult("""{"error": "Herramienta no soportada por CanvasAcademicToolsHandler: $toolName"}""")
        }
    }
}
