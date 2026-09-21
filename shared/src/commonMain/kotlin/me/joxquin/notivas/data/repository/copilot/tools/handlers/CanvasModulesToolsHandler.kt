package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.CanvasModuleItem
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.data.repository.copilot.tools.HtmlUtils

class CanvasModulesToolsHandler(
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
                name = "get_course_modules",
                description = "Obtiene los módulos del curso y todos los recursos, lecturas, enlaces y diapositivas subidos por el profesor en Canvas LMS.",
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
                name = "fetch_module_item_content",
                description = "Consulta y lee el contenido detallado o texto completo de un recurso de módulo en Canvas LMS (por ejemplo una página de lectura, sistema de evaluación, temario, guía, enlace o archivo). Puedes buscar por nombre del recurso o proporcionar su id/url.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "course_id" to OpenRouterProperty(
                            type = "integer",
                            description = "ID de Canvas del curso"
                        ),
                        "resource_name" to OpenRouterProperty(
                            type = "string",
                            description = "Título o nombre del recurso a leer (ej: 'Sistema de Evaluación', 'Silabo', 'Guía de laboratorio')"
                        ),
                        "page_url" to OpenRouterProperty(
                            type = "string",
                            description = "URL o slug de la página en Canvas si se conoce (opcional)"
                        )
                    ),
                    required = listOf("course_id", "resource_name")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean {
        return toolName in setOf("get_course_modules", "fetch_module_item_content")
    }

    override suspend fun execute(
        toolName: String,
        args: JsonObject,
        selectedCourseId: Long?
    ): ToolExecutionResult {
        val courses = localStore.getCourseList()

        return when (toolName) {
            "get_course_modules" -> {
                val cid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId ?: 0L
                val rawToken = preferencesManager.accessToken.first()
                val domain = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"

                if (!rawToken.isNullOrBlank() && cid != 0L) {
                    try {
                        val modules = canvasApiService.getModulesWithItems(domain, "Bearer $rawToken", cid)
                        val courseName = courses.find { it.id == cid }?.name ?: "Curso $cid"
                        val source = CopilotSource(
                            title = "Módulos de $courseName",
                            detail = "${modules.size} módulos obtenidos de Canvas"
                        )
                        val modulesData = modules.map { mod ->
                            mapOf(
                                "module_id" to mod.id.toString(),
                                "name" to mod.name,
                                "items" to (mod.items ?: emptyList()).map { item ->
                                    mapOf(
                                        "id" to item.id.toString(),
                                        "title" to item.title,
                                        "type" to item.type,
                                        "html_url" to (item.htmlUrl ?: ""),
                                        "url" to (item.url ?: "")
                                    )
                                }
                            )
                        }
                        ToolExecutionResult(json.encodeToString(modulesData), source)
                    } catch (e: Exception) {
                        ToolExecutionResult("""{"error": "No se pudieron obtener los módulos de Canvas: ${e.message}"}""")
                    }
                } else {
                    ToolExecutionResult("""{"error": "No hay token o course_id no válido."}""")
                }
            }

            "fetch_module_item_content" -> {
                val cid = args["course_id"]?.jsonPrimitive?.longOrNull ?: selectedCourseId ?: 0L
                val resourceNameQuery = args["resource_name"]?.jsonPrimitive?.content?.trim() ?: ""
                val explicitPageUrl = args["page_url"]?.jsonPrimitive?.content?.trim()
                val rawToken = preferencesManager.accessToken.first()
                val domain = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"

                if (!rawToken.isNullOrBlank() && cid != 0L) {
                    try {
                        val courseName = courses.find { it.id == cid }?.name ?: "Curso $cid"
                        val token = "Bearer $rawToken"

                        if (!explicitPageUrl.isNullOrBlank()) {
                            val pageDetail = canvasApiService.getPageDetails(domain, token, cid, explicitPageUrl)
                            val cleanBody = pageDetail.body?.let { HtmlUtils.cleanHtml(it) } ?: "Sin contenido textual disponible"
                            val source = CopilotSource(
                                title = "${pageDetail.title ?: resourceNameQuery} ($courseName)",
                                detail = "Página de Canvas LMS leída"
                            )
                            val map = mapOf(
                                "title" to (pageDetail.title ?: resourceNameQuery),
                                "url" to (pageDetail.url ?: explicitPageUrl),
                                "content" to cleanBody.take(4000)
                            )
                            ToolExecutionResult(json.encodeToString(map), source)
                        } else {
                            val modules = canvasApiService.getModulesWithItems(domain, token, cid)
                            var foundItem: CanvasModuleItem? = null
                            var foundModule: CanvasModule? = null

                            for (mod in modules) {
                                val items = mod.items ?: continue
                                val match = items.find { it.title.contains(resourceNameQuery, ignoreCase = true) }
                                    ?: items.find { resourceNameQuery.contains(it.title, ignoreCase = true) }
                                    ?: items.find { item ->
                                        val qWords = resourceNameQuery.lowercase().split(" ").filter { it.length > 2 }
                                        qWords.isNotEmpty() && qWords.all { item.title.lowercase().contains(it) }
                                    }
                                if (match != null) {
                                    foundItem = match
                                    foundModule = mod
                                    break
                                }
                            }

                            if (foundItem != null) {
                                when (foundItem.type) {
                                    "Page" -> {
                                        val pageUrl = foundItem.pageUrl
                                            ?: foundItem.url?.substringAfterLast("/pages/")
                                            ?: foundItem.title.lowercase().replace(" ", "-")

                                        val pageDetail = canvasApiService.getPageDetails(domain, token, cid, pageUrl)
                                        val cleanBody = pageDetail.body?.let { HtmlUtils.cleanHtml(it) } ?: "Sin texto en la página"
                                        val source = CopilotSource(
                                            title = "${pageDetail.title ?: foundItem.title} (${foundModule?.name ?: courseName})",
                                            detail = "Página de Canvas LMS leída en vivo"
                                        )
                                        val map = mapOf(
                                            "title" to (pageDetail.title ?: foundItem.title),
                                            "module" to (foundModule?.name ?: ""),
                                            "content" to cleanBody.take(4000),
                                            "url" to (foundItem.htmlUrl ?: "")
                                        )
                                        ToolExecutionResult(json.encodeToString(map), source)
                                    }

                                    "File" -> {
                                        val fileId = foundItem.contentId ?: foundItem.id
                                        val fileDetail = canvasApiService.getFileDetails(domain, token, cid, fileId)
                                        val source = CopilotSource(
                                            title = "${fileDetail.displayName ?: foundItem.title} ($courseName)",
                                            detail = "Archivo de Canvas (${fileDetail.contentType ?: "Documento"})"
                                        )
                                        val map = mapOf(
                                            "title" to (fileDetail.displayName ?: foundItem.title),
                                            "filename" to (fileDetail.filename ?: ""),
                                            "content_type" to (fileDetail.contentType ?: ""),
                                            "download_url" to (fileDetail.url ?: foundItem.htmlUrl ?: ""),
                                            "note" to "Archivo descargable en Canvas LMS"
                                        )
                                        ToolExecutionResult(json.encodeToString(map), source)
                                    }

                                    else -> {
                                        val source = CopilotSource(
                                            title = "${foundItem.title} ($courseName)",
                                            detail = "Recurso de tipo ${foundItem.type}"
                                        )
                                        val map = mapOf(
                                            "title" to foundItem.title,
                                            "type" to foundItem.type,
                                            "module" to (foundModule?.name ?: ""),
                                            "url" to (foundItem.htmlUrl ?: "")
                                        )
                                        ToolExecutionResult(json.encodeToString(map), source)
                                    }
                                }
                            } else {
                                ToolExecutionResult("""{"error": "No se encontró ningún recurso que coincida con '$resourceNameQuery' en los módulos de $courseName"}""")
                            }
                        }
                    } catch (e: Exception) {
                        ToolExecutionResult("""{"error": "Error al consultar recurso de módulo: ${e.message}"}""")
                    }
                } else {
                    ToolExecutionResult("""{"error": "No hay token de Canvas LMS o curso no seleccionado."}""")
                }
            }

            else -> ToolExecutionResult("""{"error": "Herramienta no soportada por CanvasModulesToolsHandler"}""")
        }
    }
}
