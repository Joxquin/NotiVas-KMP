package me.joxquin.notivas.domain.review

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.joxquin.notivas.data.local.CopilotProvider
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterContentPart
import me.joxquin.notivas.data.model.OpenRouterImageContent
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.GoogleDriveApiService
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.security.SecureTokenVault
import me.joxquin.notivas.util.ExtractedDocumentImage
import me.joxquin.notivas.util.PlatformDocumentImageExtractor
import me.joxquin.notivas.util.PlatformDocumentTextExtractor
import me.joxquin.notivas.util.SelectedDocument
import me.joxquin.notivas.util.resolveApiUrl

class AssignmentReviewUseCase(
    private val canvasApiService: CanvasApiService,
    private val openRouterApiService: OpenRouterApiService,
    private val preferencesManager: PreferencesManager,
    private val googleDriveApiService: GoogleDriveApiService = GoogleDriveApiService(
        httpClient = openRouterApiService.client,
        preferencesManager = preferencesManager,
        tokenRefresher = { SecureTokenVault(preferencesManager).refreshGoogleDriveAccessToken() }
    )
) {

    suspend fun reviewAssignment(
        assignment: Assignment,
        selectedDocument: SelectedDocument? = null,
        draftFileId: String? = null,
        textBody: String? = null,
        options: ReviewOptions = ReviewOptions(),
        onProgress: (ReviewProgressStep, String) -> Unit = { _, _ -> }
    ): Result<AssignmentPreReviewReport> = withContext(Dispatchers.Default) {
        try {
            // 1. Lectura de consigna y rúbrica
            onProgress(ReviewProgressStep.READING_RUBRIC, "Leyendo consigna y rúbrica oficial...")
            val rawInstructions = if (options.checkInstructions) cleanHtml(assignment.description.orEmpty()) else ""
            val maxPoints = assignment.pointsPossible ?: 20.0

            // 2. Consulta de lecturas y documentos del módulo de Canvas
            val moduleContext = if (options.checkModules) {
                onProgress(ReviewProgressStep.FETCHING_MODULES, "Consultando lecturas y recursos del módulo...")
                fetchModuleContext(
                    assignment = assignment,
                    deepScan = options.deepModuleScan || options.mode == ReviewMode.EXHAUSTIVE,
                    onDeepProgress = { onProgress(ReviewProgressStep.DOWNLOADING_MODULE_RESOURCES, it) }
                )
            } else {
                "Módulos no solicitados en esta evaluación."
            }

            // 3. Extracción de contenido del entregable y documento de referencia
            onProgress(ReviewProgressStep.EXTRACTING_DOCUMENT, "Extrayendo contenido de la entrega...")
            val documentContent = extractSubmissionContent(selectedDocument, draftFileId, textBody)
            if (documentContent.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No se pudo extraer contenido del documento o borrador para revisar."))
            }
            val referenceContent = options.referenceDocument?.let {
                extractTextFromDocument(it).take(3000)
            }.orEmpty()

            // 3.1. Extracción de evidencias visuales para visión multimodal en revisión exhaustiva
            val checkImages = options.inspectImages || options.mode == ReviewMode.EXHAUSTIVE
            val visualImages = if (checkImages && selectedDocument != null) {
                onProgress(ReviewProgressStep.EXTRACTING_DOCUMENT, "Extrayendo evidencias visuales para visión multimodal...")
                PlatformDocumentImageExtractor.extractImages(selectedDocument.bytes, selectedDocument.name, maxImages = 4)
            } else {
                emptyList()
            }

            // 4. Preparación de IA y Evaluación
            onProgress(ReviewProgressStep.EVALUATING, "Evaluando criterios y alineación académica...")
            val provider = preferencesManager.copilotProvider.first()
            val apiKey: String?
            val model: String
            val customBaseUrl: String?

            if (provider == CopilotProvider.GOOGLE_AI_STUDIO) {
                apiKey = preferencesManager.googleAiStudioApiKey.first()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException("Google AI Studio API Key no configurada. Configúrala en Ajustes Copilot."))
                }
                model = preferencesManager.googleAiStudioModel.first()
                customBaseUrl = "https://generativelanguage.googleapis.com/v1beta/openai"
            } else {
                apiKey = preferencesManager.openRouterApiKey.first()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException("OpenRouter API Key no configurada. Configúrala en Ajustes Copilot."))
                }
                model = preferencesManager.openRouterModel.first()
                customBaseUrl = null
            }

            val systemPrompt = buildSystemPrompt(maxPoints, options, hasVisualImages = visualImages.isNotEmpty())
            val userPrompt = buildUserPrompt(assignment, rawInstructions, maxPoints, moduleContext, documentContent, referenceContent, options, visualImages.size)

            val userMessage = if (visualImages.isNotEmpty()) {
                val parts = mutableListOf<OpenRouterContentPart>()
                parts.add(OpenRouterContentPart(type = "text", text = userPrompt))
                for (img in visualImages) {
                    parts.add(
                        OpenRouterContentPart(
                            type = "image_url",
                            imageUrl = OpenRouterImageContent(url = "data:${img.mimeType};base64,${img.base64Data}")
                        )
                    )
                }
                OpenRouterMessage(role = "user", contentParts = parts)
            } else {
                OpenRouterMessage(role = "user", content = userPrompt)
            }

            val chatRequest = OpenRouterChatRequest(
                model = model,
                messages = listOf(
                    OpenRouterMessage(role = "system", content = systemPrompt),
                    userMessage
                ),
                temperature = 0.2
            )

            val response = openRouterApiService.chatCompletion(apiKey, chatRequest, customBaseUrl)
            val replyText = response.choices?.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(IllegalStateException("El modelo de IA devolvió una respuesta vacía."))

            val report = parseReviewReport(replyText, maxPoints)
            onProgress(ReviewProgressStep.COMPLETED, "Revisión completada")
            Result.success(report)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            onProgress(ReviewProgressStep.ERROR, e.message ?: "Error al revisar entrega")
            Result.failure(e)
        }
    }

    private suspend fun fetchModuleContext(
        assignment: Assignment,
        deepScan: Boolean = false,
        onDeepProgress: (String) -> Unit = {}
    ): String {
        return try {
            val authCred = preferencesManager.getAuthCredential()
            val baseUrl = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"
            if (authCred == null) return "Sin conexión a Canvas LMS para obtener recursos de módulo."

            val token = when (authCred) {
                is me.joxquin.notivas.data.model.CanvasAuthCredential.BearerToken -> "Bearer ${authCred.token}"
                is me.joxquin.notivas.data.model.CanvasAuthCredential.SessionCookies -> "Cookie: ${authCred.cookies}"
            }

            val modules = canvasApiService.getModulesWithItems(baseUrl, token, assignment.courseId)
            if (modules.isEmpty()) return "No se encontraron módulos registrados en este curso."

            // Buscar el módulo al que pertenece esta tarea
            val targetModule = modules.find { mod ->
                mod.items?.any { it.contentId == assignment.id || it.title.equals(assignment.name, ignoreCase = true) } == true
            } ?: modules.firstOrNull()

            if (targetModule == null) return "Módulo no identificado."

            val sb = StringBuilder()
            sb.appendLine("Módulo de Canvas: \"${targetModule.name}\"")
            sb.appendLine("Recursos y lecturas asociadas:")

            val moduleItems = targetModule.items.orEmpty()
            var downloadedCount = 0
            for (item in moduleItems.take(8)) {
                sb.appendLine("- [${item.type}] ${item.title}")
                if (item.type.equals("Page", ignoreCase = true) && !item.pageUrl.isNullOrBlank()) {
                    try {
                        val page = canvasApiService.getPageDetails(baseUrl, token, assignment.courseId, item.pageUrl)
                        val pageSnippet = cleanHtml(page.body.orEmpty()).take(400)
                        if (pageSnippet.isNotBlank()) {
                            sb.appendLine("  Contenido clave de la página: $pageSnippet...")
                        }
                    } catch (_: Exception) { }
                } else if (deepScan && item.type.equals("File", ignoreCase = true) && item.contentId != null && downloadedCount < 2) {
                    try {
                        onDeepProgress("Descargando lectura: ${item.title}...")
                        val fileDetail = canvasApiService.getFileDetails(baseUrl, token, assignment.courseId, item.contentId)
                        val downloadUrl = fileDetail.url ?: fileDetail.publicUrl
                        if (!downloadUrl.isNullOrBlank()) {
                            val bytes = canvasApiService.downloadFileBytes(downloadUrl, token)
                            if (bytes != null && bytes.isNotEmpty()) {
                                downloadedCount++
                                val fileText = extractTextFromDocument(
                                    SelectedDocument(
                                        name = fileDetail.filename ?: item.title,
                                        sizeBytes = bytes.size.toLong(),
                                        mimeType = fileDetail.contentType.orEmpty(),
                                        bytes = bytes
                                    )
                                ).take(1200)
                                if (fileText.isNotBlank()) {
                                    sb.appendLine("  Contenido extraído del documento adjunto: $fileText...")
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }
            }
            sb.toString()
        } catch (e: Exception) {
            "No se pudieron cargar lecturas del módulo: ${e.message}"
        }
    }

    private suspend fun extractSubmissionContent(
        selectedDocument: SelectedDocument?,
        draftFileId: String?,
        textBody: String?
    ): String {
        if (!textBody.isNullOrBlank()) {
            return textBody.trim()
        }

        if (selectedDocument != null) {
            return extractTextFromDocument(selectedDocument)
        }

        if (!draftFileId.isNullOrBlank()) {
            val exported = googleDriveApiService.exportFileContentAsText(draftFileId)
            if (!exported.isNullOrBlank()) {
                return exported.trim()
            }
        }

        return ""
    }

    private fun extractTextFromDocument(doc: SelectedDocument): String {
        return PlatformDocumentTextExtractor.extractText(doc.bytes, doc.name)
    }

    private fun buildSystemPrompt(maxPoints: Double, options: ReviewOptions, hasVisualImages: Boolean): String {
        val checkImages = options.inspectImages || options.mode == ReviewMode.EXHAUSTIVE
        return """
            Eres un Evaluador Docente Universitario Oficial y Riguroso.
            Tu función es pre-revisar la entrega de un estudiante antes de que sea enviada a Canvas LMS.
            Debes contrastar el trabajo presentado estrictamente contra:
            ${if (options.checkInstructions) "1. La consigna oficial del entregable." else "1. Cumplimiento general de objetivos."}
            ${if (options.checkRubric) "2. La rúbrica de evaluación oficial (escala máxima: $maxPoints puntos)." else "2. Estándares de calidad académica."}
            ${if (options.checkModules) "3. Las lecturas, temas y recursos del módulo de Canvas para validar que el contenido aplique la teoría enseñada." else "3. Coherencia temática universitaria."}
            ${if (checkImages && hasVisualImages) "4. Inspección visual y multimodal de capturas, páginas y diagramas adjuntos en el mensaje para corroborar evidencia gráfica real." else if (checkImages) "4. Calidad, presencia y pertinencia de figuras, diagramas o imágenes incluidos para respaldar los argumentos." else ""}
            ${if (options.referenceDocument != null) "5. Documento de referencia adicional adjuntado como estándar complementario." else ""}

            IMPORTANTE: Tu respuesta DEBE incluir obligatoriamente dos secciones:
            1. Un bloque estructurado para que el sistema procese el reporte.
            2. Una devolución en Markdown limpio y profesional explicando detalladamente cada aspecto.

            Formato estricto para el bloque estructurado:
            ===ESTRUCTURA_REPORTE===
            PUNTAJE_ESTIMADO: [número decimal sobre $maxPoints, ej. 17.5]
            PUNTOS_MAXIMOS: $maxPoints
            PORCENTAJE: [entero entre 0 y 100]
            RESUMEN: [Resumen conciso del desempeño general en 2 o 3 oraciones]
            CRITERIO: [Nombre del criterio] | [Puntos obtenidos] | [Puntos máximos] | [Nivel: Excelente/Bueno/Aceptable/Insuficiente] | [Retroalimentación breve de lo que falta para el puntaje perfecto]
            (Repite la línea CRITERIO para cada criterio evaluado)
            FORTALEZA: [Punto fuerte identificado]
            (Repite la línea FORTALEZA 2 a 4 veces)
            MEJORA: [Sugerencia específica de qué agregar o corregir antes de enviar]
            (Repite la línea MEJORA 2 a 4 veces)
            ALINEACION_MODULO: [Observación sobre cómo se relaciona con las lecturas o temas del módulo]
            (Repite la línea ALINEACION_MODULO 1 a 3 veces)
            ===FIN_ESTRUCTURA===

            Luego de ese bloque, redacta la retroalimentación en Markdown detallada, formatéala con encabezados, listas y viñetas sin emojis.
        """.trimIndent()
    }

    private fun buildUserPrompt(
        assignment: Assignment,
        instructions: String,
        maxPoints: Double,
        moduleContext: String,
        documentContent: String,
        referenceContent: String,
        options: ReviewOptions,
        visualImagesCount: Int
    ): String {
        val checkImages = options.inspectImages || options.mode == ReviewMode.EXHAUSTIVE
        return buildString {
            appendLine("DATOS DE LA TAREA:")
            appendLine("- Título: ${assignment.name}")
            appendLine("- Puntaje máximo: $maxPoints")
            appendLine("- Fecha límite: ${assignment.dueAt ?: "No especificada"}")
            appendLine()
            if (options.checkInstructions && instructions.isNotBlank()) {
                appendLine("CONSIGNA OFICIAL DEL ENTREGABLE:")
                appendLine(instructions)
                appendLine()
            }
            if (options.checkModules && moduleContext.isNotBlank()) {
                appendLine("CONTEXTO DEL MÓDULO DEL CURSO:")
                appendLine(moduleContext)
                appendLine()
            }
            if (referenceContent.isNotBlank()) {
                appendLine("DOCUMENTO DE REFERENCIA EXTRA (Aportado por el estudiante):")
                appendLine(referenceContent)
                appendLine()
            }
            if (checkImages) {
                appendLine("ENFOQUE VISUAL Y DE DIAGRAMAS:")
                if (visualImagesCount > 0) {
                    appendLine("Se adjuntan $visualImagesCount imagen(es)/página(s) representativas del documento entregado. Examínalas visualmente para validar capturas de pantalla, esquemas, código fuente y ejecución de pruebas.")
                } else {
                    appendLine("Analiza rigurosamente si el trabajo incluye diagramas, figuras o gráficos que apoyen la explicación y califica su pertinencia.")
                }
                appendLine()
            }
            appendLine("CONTENIDO DE TEXTO PRESENTADO POR EL ESTUDIANTE:")
            appendLine(documentContent)
            appendLine()
            appendLine("Evalúa la entrega siguiendo estrictamente las instrucciones del sistema y genera el reporte.")
        }
    }

    private fun parseReviewReport(reply: String, maxPoints: Double): AssignmentPreReviewReport {
        var overallScore: Double? = null
        var maxScore = maxPoints
        var percentage = 0
        var summary = ""
        val criteria = mutableListOf<CriterionEvaluation>()
        val strengths = mutableListOf<String>()
        val improvements = mutableListOf<String>()
        val moduleAlignment = mutableListOf<String>()

        val structureRegex = Regex("""===ESTRUCTURA_REPORTE===([\s\S]*?)===FIN_ESTRUCTURA===""", RegexOption.IGNORE_CASE)
        val structureMatch = structureRegex.find(reply)
        val structureBlock = structureMatch?.groupValues?.get(1) ?: reply

        for (line in structureBlock.lines()) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("PUNTAJE_ESTIMADO:", ignoreCase = true) -> {
                    overallScore = trimmed.substringAfter(":").trim().toDoubleOrNull()
                }
                trimmed.startsWith("PUNTOS_MAXIMOS:", ignoreCase = true) -> {
                    trimmed.substringAfter(":").trim().toDoubleOrNull()?.let { maxScore = it }
                }
                trimmed.startsWith("PORCENTAJE:", ignoreCase = true) -> {
                    percentage = trimmed.substringAfter(":").trim().toIntOrNull() ?: 0
                }
                trimmed.startsWith("RESUMEN:", ignoreCase = true) -> {
                    summary = trimmed.substringAfter(":").trim()
                }
                trimmed.startsWith("CRITERIO:", ignoreCase = true) -> {
                    val parts = trimmed.substringAfter(":").split("|").map { it.trim() }
                    if (parts.size >= 5) {
                        criteria.add(
                            CriterionEvaluation(
                                title = parts[0],
                                pointsEarned = parts[1].toDoubleOrNull() ?: 0.0,
                                maxPoints = parts[2].toDoubleOrNull() ?: (maxScore / 4.0),
                                level = parts[3],
                                feedback = parts[4]
                            )
                        )
                    } else if (parts.size >= 3) {
                        criteria.add(
                            CriterionEvaluation(
                                title = parts[0],
                                pointsEarned = parts[1].toDoubleOrNull() ?: 0.0,
                                maxPoints = maxScore / 4.0,
                                level = parts.getOrNull(2) ?: "Evaluado",
                                feedback = parts.getOrNull(3) ?: ""
                            )
                        )
                    }
                }
                trimmed.startsWith("FORTALEZA:", ignoreCase = true) -> {
                    val str = trimmed.substringAfter(":").trim()
                    if (str.isNotBlank()) strengths.add(str)
                }
                trimmed.startsWith("MEJORA:", ignoreCase = true) -> {
                    val imp = trimmed.substringAfter(":").trim()
                    if (imp.isNotBlank()) improvements.add(imp)
                }
                trimmed.startsWith("ALINEACION_MODULO:", ignoreCase = true) -> {
                    val mod = trimmed.substringAfter(":").trim()
                    if (mod.isNotBlank()) moduleAlignment.add(mod)
                }
            }
        }

        if (percentage == 0 && overallScore != null && maxScore > 0) {
            percentage = ((overallScore / maxScore) * 100).toInt().coerceIn(0, 100)
        }

        val cleanMarkdown = reply.replace(structureRegex, "").trim()

        return AssignmentPreReviewReport(
            overallScore = overallScore,
            maxScore = maxScore,
            percentage = percentage,
            summary = summary.ifBlank { "Revisión completada por IA." },
            criteriaEvaluations = criteria,
            strengths = strengths,
            improvements = improvements,
            moduleAlignment = moduleAlignment,
            rawMarkdown = cleanMarkdown
        )
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
