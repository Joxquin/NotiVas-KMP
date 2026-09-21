package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.data.repository.copilot.tools.HtmlUtils

class CanvasDiscussionsToolsHandler(
    private val canvasApiService: CanvasApiService,
    private val localStore: InMemoryLocalStore,
    private val preferencesManager: PreferencesManager
) : CopilotToolHandler {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "get_course_discussions",
                description = "Obtiene la lista de foros y debates (discussions) publicados en Canvas LMS para un curso por su course_id.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID de Canvas del curso"
                        )
                    ),
                    required = listOf("course_id")
                )
            )
        ),
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "fetch_discussion_details",
                description = "Obtiene las instrucciones completas, consigna, preguntas del profesor y detalles de un foro específico de Canvas LMS. Puedes buscar por topic_id o por nombre del foro.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID de Canvas del curso"
                        ),
                        "topic_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID del foro/debate en Canvas (opcional si proporcionas topic_title)"
                        ),
                        "topic_title" to OpenRouterProperty(
                            type = "string",
                            description = "Título o parte del nombre del foro (ej: 'Foro IA', 'Sustentación')"
                        )
                    ),
                    required = listOf("course_id")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean {
        return toolName in setOf("get_course_discussions", "fetch_discussion_details")
    }

    override suspend fun execute(
        toolName: String,
        args: JsonObject,
        selectedCourseId: Long?
    ): ToolExecutionResult {
        val courses = localStore.getCourseList()

        return when (toolName) {
            "get_course_discussions" -> {
                val cid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId ?: 0L
                val canvasToken = preferencesManager.accessToken.first()
                val domain = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"
                val courseName = courses.find { it.id == cid }?.name ?: "Curso $cid"

                if (!canvasToken.isNullOrBlank() && cid != 0L) {
                    try {
                        val token = "Bearer $canvasToken"
                        val discussions = canvasApiService.getDiscussionTopics(domain, token, cid)
                        val source = CopilotSource(
                            title = "Foros y debates de $courseName",
                            detail = "${discussions.size} foros registrados en Canvas"
                        )
                        val simplified = discussions.map {
                            val cleanMsg = it.message?.let { m -> HtmlUtils.cleanHtml(m) } ?: ""
                            mapOf(
                                "id" to it.id.toString(),
                                "title" to it.title,
                                "author" to (it.userName ?: "Docente"),
                                "posted_at" to (it.postedAt ?: ""),
                                "subentry_count" to (it.discussionSubentryCount?.toString() ?: "0"),
                                "message_preview" to cleanMsg.take(200)
                            )
                        }
                        ToolExecutionResult(json.encodeToString(simplified), source)
                    } catch (e: Exception) {
                        ToolExecutionResult("""{"error": "Error al obtener foros: ${e.message}"}""")
                    }
                } else {
                    ToolExecutionResult("""{"error": "No hay token de Canvas o curso no seleccionado."}""")
                }
            }

            "fetch_discussion_details" -> {
                val cid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId ?: 0L
                var topicId = args["topic_id"]?.jsonPrimitive?.longOrNull ?: 0L
                val topicTitleQuery = args["topic_title"]?.jsonPrimitive?.content?.trim() ?: ""
                val canvasToken = preferencesManager.accessToken.first()
                val domain = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"
                val courseName = courses.find { it.id == cid }?.name ?: "Curso $cid"

                if (!canvasToken.isNullOrBlank() && cid != 0L) {
                    try {
                        val token = "Bearer $canvasToken"
                        if (topicId == 0L && topicTitleQuery.isNotBlank()) {
                            val allTopics = canvasApiService.getDiscussionTopics(domain, token, cid)
                            val match = allTopics.find { it.title.contains(topicTitleQuery, ignoreCase = true) }
                                ?: allTopics.find { topicTitleQuery.contains(it.title, ignoreCase = true) }
                                ?: allTopics.find { t ->
                                    val words = topicTitleQuery.lowercase().split(" ").filter { it.length > 2 }
                                    words.isNotEmpty() && words.all { t.title.lowercase().contains(it) }
                                }
                            if (match != null) {
                                topicId = match.id
                            }
                        }

                        if (topicId != 0L) {
                            val topic = canvasApiService.getDiscussionTopic(domain, token, cid, topicId)
                            val cleanMsg = topic.message?.let { cleanHtml -> HtmlUtils.cleanHtml(cleanHtml) } ?: "Sin consigna o mensaje específico"
                            val source = CopilotSource(
                                title = "Foro: ${topic.title} ($courseName)",
                                detail = "Instrucciones y consigna en vivo de Canvas LMS"
                            )
                            val map = mapOf(
                                "id" to topic.id.toString(),
                                "title" to topic.title,
                                "author" to (topic.userName ?: "Docente"),
                                "posted_at" to (topic.postedAt ?: ""),
                                "consigna_message" to cleanMsg.take(4000),
                                "url" to (topic.htmlUrl ?: "")
                            )
                            ToolExecutionResult(json.encodeToString(map), source)
                        } else {
                            ToolExecutionResult("""{"error": "No se encontró el foro '$topicTitleQuery' en $courseName"}""")
                        }
                    } catch (e: Exception) {
                        ToolExecutionResult("""{"error": "Error al consultar foro: ${e.message}"}""")
                    }
                } else {
                    ToolExecutionResult("""{"error": "No hay token de Canvas LMS o curso no seleccionado."}""")
                }
            }

            else -> ToolExecutionResult("""{"error": "Herramienta no soportada por CanvasDiscussionsToolsHandler"}""")
        }
    }
}
