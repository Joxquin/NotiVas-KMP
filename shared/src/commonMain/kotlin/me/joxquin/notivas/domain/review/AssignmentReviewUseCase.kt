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
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.GoogleDriveApiService
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.util.SelectedDocument
import me.joxquin.notivas.util.resolveApiUrl

class AssignmentReviewUseCase(
    private val canvasApiService: CanvasApiService,
    private val openRouterApiService: OpenRouterApiService,
    private val preferencesManager: PreferencesManager,
    private val googleDriveApiService: GoogleDriveApiService = GoogleDriveApiService(
        httpClient = openRouterApiService.client,
        preferencesManager = preferencesManager
    )
) {

    suspend fun reviewAssignment(
        assignment: Assignment,
        selectedDocument: SelectedDocument? = null,
        draftFileId: String? = null,
        textBody: String? = null,
        onProgress: (ReviewProgressStep, String) -> Unit = { _, _ -> }
    ): Result<AssignmentPreReviewReport> = withContext(Dispatchers.Default) {
        try {
            // 1. Lectura de consigna y rúbrica
            onProgress(ReviewProgressStep.READING_RUBRIC, "Leyendo consigna y rúbrica oficial...")
            val rawInstructions = cleanHtml(assignment.description.orEmpty())
            val maxPoints = assignment.pointsPossible ?: 20.0

            // 2. Consulta de lecturas y documentos del módulo de Canvas
            onProgress(ReviewProgressStep.FETCHING_MODULES, "Consultando lecturas y recursos del módulo...")
            val moduleContext = fetchModuleContext(assignment)

            // 3. Extracción de contenido del entregable
            onProgress(ReviewProgressStep.EXTRACTING_DOCUMENT, "Extrayendo contenido de la entrega...")
            val documentContent = extractSubmissionContent(selectedDocument, draftFileId, textBody)
            if (documentContent.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No se pudo extraer contenido del documento o borrador para revisar."))
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

            val systemPrompt = buildSystemPrompt(maxPoints)
            val userPrompt = buildUserPrompt(assignment, rawInstructions, maxPoints, moduleContext, documentContent)

            val chatRequest = OpenRouterChatRequest(
                model = model,
                messages = listOf(
                    OpenRouterMessage(role = "system", content = systemPrompt),
                    OpenRouterMessage(role = "user", content = userPrompt)
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

    private suspend fun fetchModuleContext(assignment: Assignment): String {
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
        val nameLower = doc.name.lowercase()
        return when {
            nameLower.endsWith(".txt") || nameLower.endsWith(".md") ||
            nameLower.endsWith(".json") || nameLower.endsWith(".csv") ||
            nameLower.endsWith(".html") || nameLower.endsWith(".xml") ||
            nameLower.endsWith(".kt") || nameLower.endsWith(".java") ||
            nameLower.endsWith(".py") || nameLower.endsWith(".js") -> {
                try {
                    doc.bytes.decodeToString()
                } catch (_: Exception) {
                    "Archivo de texto: ${doc.name} (${doc.sizeBytes} bytes)"
                }
            }
            nameLower.endsWith(".docx") -> {
                extractTextFromDocxZip(doc.bytes)
            }
            nameLower.endsWith(".pdf") -> {
                extractReadableAsciiFromPdf(doc.bytes)
            }
            else -> {
                "Archivo adjunto: ${doc.name} (${doc.mimeType}, ${doc.sizeBytes} bytes)"
            }
        }
    }

    private fun extractTextFromDocxZip(bytes: ByteArray): String {
        return try {
            // Un archivo DOCX es un ZIP que contiene 'word/document.xml' con tags <w:t>...</w:t>
            val rawString = bytes.decodeToString()
            val textRegex = Regex("""<w:t[^>]*>([^<]+)</w:t>""")
            val matches = textRegex.findAll(rawString).map { it.groupValues[1] }.toList()
            if (matches.isNotEmpty()) {
                matches.joinToString(" ")
            } else {
                "Documento DOCX detectado (${bytes.size} bytes). Extracción preliminar de texto no disponible."
            }
        } catch (_: Exception) {
            "Documento DOCX (${bytes.size} bytes)."
        }
    }

    private fun extractReadableAsciiFromPdf(bytes: ByteArray): String {
        return try {
            val text = bytes.decodeToString()
            val streamRegex = Regex("""\(([^)]{2,})\)""")
            val matches = streamRegex.findAll(text).map { it.groupValues[1] }.take(250).toList()
            if (matches.isNotEmpty()) {
                matches.joinToString(" ")
            } else {
                "Documento PDF (${bytes.size} bytes)."
            }
        } catch (_: Exception) {
            "Documento PDF (${bytes.size} bytes)."
        }
    }

    private fun buildSystemPrompt(maxPoints: Double): String {
        return """
            Eres un Evaluador Docente Universitario Oficial y Riguroso.
            Tu función es pre-revisar la entrega de un estudiante antes de que sea enviada a Canvas LMS.
            Debes contrastar el trabajo presentado estrictamente contra:
            1. La consigna oficial del entregable.
            2. La rúbrica de evaluación oficial (escala máxima: $maxPoints puntos).
            3. Las lecturas, temas y recursos del módulo de Canvas para validar que el contenido aplique la teoría enseñada.

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
        documentContent: String
    ): String {
        return """
            DATOS DE LA TAREA:
            - Título: ${assignment.name}
            - Puntaje máximo: $maxPoints
            - Fecha límite: ${assignment.dueAt ?: "No especificada"}

            CONSIGNA OFICIAL DEL ENTREGABLE:
            ${instructions.ifBlank { "Sin descripción detallada en Canvas LMS." }}

            CONTEXTO DEL MÓDULO DEL CURSO:
            $moduleContext

            CONTENIDO PRESENTADO POR EL ESTUDIANTE:
            $documentContent

            Evalúa la entrega siguiendo estrictamente las instrucciones del sistema y genera el reporte.
        """.trimIndent()
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
