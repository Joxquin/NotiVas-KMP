package me.joxquin.notivas.data.repository

import kotlinx.coroutines.flow.first
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.data.repository.copilot.CopilotPromptBuilder
import me.joxquin.notivas.data.repository.copilot.CopilotToolExecutor
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasAcademicToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasDiscussionsToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasModulesToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.SimulatorToolsHandler

data class CopilotResult(
    val reply: String,
    val sources: List<CopilotSource> = emptyList(),
    val actionFeedback: String? = null,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

data class OpenRouterAccountBalance(
    val totalCredits: Double? = null,
    val totalUsage: Double? = null,
    val remainingCredits: Double? = null,
    val isFreeTier: Boolean? = null,
    val limit: Double? = null
)

class CopilotRepository(
    private val openRouterApiService: OpenRouterApiService,
    private val canvasApiService: CanvasApiService,
    private val localStore: InMemoryLocalStore,
    private val preferencesManager: PreferencesManager
) {
    private val promptBuilder = CopilotPromptBuilder()
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val toolExecutor = CopilotToolExecutor(
        canvasAcademicToolsHandler = CanvasAcademicToolsHandler(canvasApiService, localStore, preferencesManager),
        canvasModulesToolsHandler = CanvasModulesToolsHandler(canvasApiService, localStore, preferencesManager),
        canvasDiscussionsToolsHandler = CanvasDiscussionsToolsHandler(canvasApiService, localStore, preferencesManager),
        simulatorToolsHandler = SimulatorToolsHandler(localStore)
    )

    suspend fun queryCopilot(
        history: List<OpenRouterMessage>,
        userPrompt: String,
        selectedCourseId: Long? = null
    ): Result<CopilotResult> {
        val provider = preferencesManager.copilotProvider.first()
        val apiKey: String?
        val model: String
        val customBaseUrl: String?

        if (provider == me.joxquin.notivas.data.local.CopilotProvider.GOOGLE_AI_STUDIO) {
            apiKey = preferencesManager.googleAiStudioApiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Google AI Studio API Key no configurada. Ve a Ajustes para agregarla."))
            }
            model = preferencesManager.googleAiStudioModel.first()
            customBaseUrl = "https://generativelanguage.googleapis.com/v1beta/openai"
        } else {
            apiKey = preferencesManager.openRouterApiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(IllegalStateException("OpenRouter API Key no configurada. Ve a Ajustes para agregarla."))
            }
            model = preferencesManager.openRouterModel.first()
            customBaseUrl = null
        }

        val appLanguage = preferencesManager.appLanguage.first()
        val courses = localStore.courses.first()
        val systemPrompt = promptBuilder.buildSystemPrompt(courses, selectedCourseId, appLanguage)

        val messages = promptBuilder.buildConversationMessages(systemPrompt, history, userPrompt)

        val accumulatedSources = mutableListOf<CopilotSource>()
        val accumulatedActionFeedback = mutableListOf<String>()
        var totalPromptTokens = 0
        var totalCompletionTokens = 0
        var totalTokens = 0

        var currentAttempt = 0
        val maxToolIterations = 5
        var disableTools = false
        var finalReply: String? = null

        while (currentAttempt < maxToolIterations) {
            currentAttempt++

            // Intentar primero con tools (salvo que ya sepamos que este modelo/proveedor no las soporta)
            var response = try {
                openRouterApiService.chatCompletion(
                    apiKey = apiKey,
                    request = OpenRouterChatRequest(
                        model = model,
                        messages = messages,
                        tools = if (disableTools) null else toolExecutor.tools,
                        temperature = 0.3,
                        maxTokens = 4000
                    ),
                    customBaseUrl = customBaseUrl
                )
            } catch (e: Exception) {
                // Si el proveedor del modelo rechaza el parámetro 'tools' o arroja 400 Provider error
                val isProviderToolError = !disableTools && (
                    e.message?.contains("Provider returned error", ignoreCase = true) == true ||
                    e.message?.contains("invalid_request_error", ignoreCase = true) == true ||
                    e.message?.contains("tools", ignoreCase = true) == true
                )

                if (isProviderToolError) {
                    disableTools = true
                    println("Copilot [Aviso] El modelo '$model' ($provider) no soporta function calling o el proveedor falló. Reintentando sin tools...")
                    try {
                        openRouterApiService.chatCompletion(
                            apiKey = apiKey,
                            request = OpenRouterChatRequest(
                                model = model,
                                messages = messages,
                                tools = null,
                                temperature = 0.3,
                                maxTokens = 4000
                            ),
                            customBaseUrl = customBaseUrl
                        )
                    } catch (retryException: Exception) {
                        println("Copilot [Error] Falló reintento sin tools: ${retryException.message}")
                        return Result.failure(retryException)
                    }
                } else {
                    println("Copilot [Error] Falló la petición a $provider: ${e.message}")
                    return Result.failure(e)
                }
            }

            if (response.error != null) {
                val err = response.error.message ?: "Error en la respuesta del proveedor IA (Código: ${response.error.code})"
                return Result.failure(IllegalStateException(err))
            }

            val choice = response.choices?.firstOrNull()
            val choiceMessage = choice?.message
            val toolCalls = choiceMessage?.toolCalls

            val usage = response.usage
            if (usage != null) {
                val pTok = usage.promptTokens ?: 0
                val cTok = usage.completionTokens ?: 0
                val tTok = usage.totalTokens ?: (pTok + cTok)
                totalPromptTokens += pTok
                totalCompletionTokens += cTok
                totalTokens += tTok
            }

            // 1. Si el modelo solicitó ejecutar herramientas nativas
            if (!toolCalls.isNullOrEmpty()) {
                messages.add(
                    OpenRouterMessage(
                        role = "assistant",
                        content = choiceMessage.content,
                        toolCalls = toolCalls
                    )
                )

                for (call in toolCalls) {
                    val fnName = call.function.name
                    val fnArgs = call.function.arguments
                    val execution = toolExecutor.executeTool(fnName, fnArgs, selectedCourseId)

                    if (execution.source != null) {
                        accumulatedSources.add(execution.source)
                    }
                    if (execution.actionFeedback != null) {
                        accumulatedActionFeedback.add(execution.actionFeedback)
                    }

                    messages.add(
                        OpenRouterMessage(
                            role = "tool",
                            name = fnName,
                            toolCallId = call.id,
                            content = execution.resultJson
                        )
                    )
                }
                // Continuar bucle para que el modelo sintetice la respuesta final con los datos de las herramientas
                continue
            }

            // 2. Si el modelo no tiene tools nativos y escribió pseudo-tags <tool_call> en el texto
            val rawContent = choiceMessage?.content ?: ""
            val textToolCalls = parseTextToolCalls(rawContent)
            if (textToolCalls.isNotEmpty()) {
                println("Copilot [Fallback] Detectadas ${textToolCalls.size} llamadas de herramientas en texto plano: ${textToolCalls.map { it.name }}")
                messages.add(
                    OpenRouterMessage(
                        role = "assistant",
                        content = rawContent
                    )
                )

                val toolResultsSummary = StringBuilder()
                for (call in textToolCalls) {
                    val execution = toolExecutor.executeTool(call.name, call.argsJson, selectedCourseId)
                    if (execution.source != null) {
                        accumulatedSources.add(execution.source)
                    }
                    if (execution.actionFeedback != null) {
                        accumulatedActionFeedback.add(execution.actionFeedback)
                    }
                    toolResultsSummary.append("\n[Resultado de ${call.name}]:\n${execution.resultJson}\n")
                }

                // Añadir los resultados como mensaje de usuario contextual para que el modelo sintetice la respuesta final
                messages.add(
                    OpenRouterMessage(
                        role = "user",
                        content = "Aquí tienes los datos solicitados de Canvas LMS para responder mi consulta previa:\n$toolResultsSummary\nPor favor, responde detalladamente al usuario en base a estos datos."
                    )
                )
                continue
            }

            // 3. Respuesta textual final obtenida
            if (rawContent.isNotBlank()) {
                // Limpiar posibles pseudo-tags residuales de tool_call en crudo
                val cleanedContent = rawContent.replace(Regex("(?s)<tool_call>.*?</tool_call>"), "").trim()
                if (cleanedContent.isNotBlank()) {
                    finalReply = cleanedContent
                }
            }
            break
        }

        // Si se agotaron los intentos de llamadas a herramientas y el modelo no generó su síntesis final,
        // forzamos una última llamada sin tools exigiéndole la redacción final con los datos acumulados
        if (finalReply.isNullOrBlank() && accumulatedSources.isNotEmpty()) {
            try {
                messages.add(
                    OpenRouterMessage(
                        role = "user",
                        content = "Con toda la información obtenida arriba de Canvas LMS, responde ahora de forma completa, detallada y estructurada la consulta original del estudiante en Markdown (usando tablas, divisores y viñetas según corresponda)."
                    )
                )
                val finalSynthesisResponse = openRouterApiService.chatCompletion(
                    apiKey = apiKey,
                    request = OpenRouterChatRequest(
                        model = model,
                        messages = messages,
                        tools = null,
                        temperature = 0.3,
                        maxTokens = 4000
                    ),
                    customBaseUrl = customBaseUrl
                )
                val synthesisText = finalSynthesisResponse.choices?.firstOrNull()?.message?.content
                if (!synthesisText.isNullOrBlank()) {
                    finalReply = synthesisText.replace(Regex("(?s)<tool_call>.*?</tool_call>"), "").trim()
                }
                finalSynthesisResponse.usage?.let { u ->
                    val pTok = u.promptTokens ?: 0
                    val cTok = u.completionTokens ?: 0
                    totalPromptTokens += pTok
                    totalCompletionTokens += cTok
                    totalTokens += u.totalTokens ?: (pTok + cTok)
                }
            } catch (e: Exception) {
                println("Copilot [Aviso] Falló la síntesis forzada final: ${e.message}")
            }
        }

        if (finalReply.isNullOrBlank()) {
            finalReply = if (accumulatedSources.isNotEmpty()) {
                buildString {
                    append("He consultado la siguiente información de Canvas LMS:\n\n")
                    accumulatedSources.distinctBy { it.title + it.detail }.forEach { src ->
                        append("• **${src.title}**: ${src.detail}\n")
                    }
                }
            } else {
                "El modelo no generó texto de respuesta para esta consulta."
            }
        }

        if (totalTokens > 0) {
            preferencesManager.addCopilotTokens(totalTokens.toLong())
        }

        return Result.success(
            CopilotResult(
                reply = finalReply,
                sources = accumulatedSources.distinctBy { it.title + it.detail },
                actionFeedback = accumulatedActionFeedback.joinToString("\n").ifBlank { null },
                promptTokens = totalPromptTokens,
                completionTokens = totalCompletionTokens,
                totalTokens = totalTokens
            )
        )
    }

    suspend fun getOpenRouterBalance(): OpenRouterAccountBalance? {
        val provider = preferencesManager.copilotProvider.first()
        if (provider != me.joxquin.notivas.data.local.CopilotProvider.OPENROUTER) {
            return null
        }
        val apiKey = preferencesManager.openRouterApiKey.first() ?: return null
        if (apiKey.isBlank()) return null

        return try {
            val creditsResponse = openRouterApiService.getCredits(apiKey)
            val keyResponse = openRouterApiService.getKeyInfo(apiKey)

            val creditsData = creditsResponse.data
            val keyData = keyResponse.data

            val totalCredits = creditsData?.totalCredits
            val totalUsage = creditsData?.totalUsage ?: keyData?.usage
            val remaining = if (totalCredits != null && totalUsage != null) {
                (totalCredits - totalUsage).coerceAtLeast(0.0)
            } else if (keyData?.limit != null && keyData.usage != null) {
                (keyData.limit - keyData.usage).coerceAtLeast(0.0)
            } else null

            OpenRouterAccountBalance(
                totalCredits = totalCredits,
                totalUsage = totalUsage,
                remainingCredits = remaining,
                isFreeTier = keyData?.isFreeTier,
                limit = keyData?.limit
            )
        } catch (e: Exception) {
            println("Copilot [Error] Error fetching OpenRouter balance: ${e.message}")
            null
        }
    }

    suspend fun analyzeModelCompatibility(modelId: String): me.joxquin.notivas.data.model.CustomModelAnalysisResult {
        val cleanModel = modelId.trim()
        if (cleanModel.isBlank()) {
            return me.joxquin.notivas.data.model.CustomModelAnalysisResult(
                modelId = modelId,
                name = "Modelo no especificado",
                status = me.joxquin.notivas.data.model.ModelCompatibilityStatus.INCOMPATIBLE,
                message = "Debes ingresar un ID de modelo valido."
            )
        }

        val apiKey = preferencesManager.openRouterApiKey.first()
        val response = try {
            openRouterApiService.getModelEndpoints(cleanModel, apiKey)
        } catch (e: Exception) {
            return me.joxquin.notivas.data.model.CustomModelAnalysisResult(
                modelId = cleanModel,
                name = cleanModel,
                status = me.joxquin.notivas.data.model.ModelCompatibilityStatus.ERROR,
                message = "Error de red al consultar el modelo: ${e.message ?: "Comprueba tu conexion"}"
            )
        }

        if (response.error != null || response.data == null) {
            val code = response.error?.code
            val isNotFound = code == 404 || response.error?.message?.contains("Not Found", ignoreCase = true) == true
            return me.joxquin.notivas.data.model.CustomModelAnalysisResult(
                modelId = cleanModel,
                name = cleanModel,
                status = if (isNotFound) me.joxquin.notivas.data.model.ModelCompatibilityStatus.NOT_FOUND else me.joxquin.notivas.data.model.ModelCompatibilityStatus.ERROR,
                message = if (isNotFound) "El modelo no fue encontrado en OpenRouter. Revisa el ID (ej: meta-llama/llama-3.3-70b-instruct)." else (response.error?.message ?: "Error al consultar modelo")
            )
        }

        val data = response.data
        val name = data.name ?: cleanModel
        val description = data.description?.take(220)?.trim()
        val architecture = data.architecture

        val modality = architecture?.modality.orEmpty().lowercase()
        val inputModalities = architecture?.inputModalities?.map { it.lowercase() } ?: emptyList()
        val outputModalities = architecture?.outputModalities?.map { it.lowercase() } ?: emptyList()

        val isTextOutput = outputModalities.contains("text") || modality.contains("->text")
        val isTextInput = inputModalities.contains("text") || modality.startsWith("text")
        val isMultimodal = inputModalities.any { it in listOf("image", "audio", "video") } || modality.contains("image")

        val endpoints = data.endpoints
        val contextLen = data.contextLength ?: endpoints.mapNotNull { it.contextLength }.maxOrNull()

        // Verificar si algun endpoint soporta tool calling
        val supportsToolCalling = endpoints.any { ep ->
            val toolChoice = ep.supportsToolChoice
            toolChoice?.get("function") == true || toolChoice?.get("auto") == true ||
                    ep.supportedParameters.any { it.contains("tools", ignoreCase = true) || it.contains("tool_choice", ignoreCase = true) }
        }

        val isChatCapable = isTextInput && isTextOutput

        val (status, msg) = when {
            !isChatCapable -> {
                me.joxquin.notivas.data.model.ModelCompatibilityStatus.INCOMPATIBLE to "Este modelo no es de chat/texto (modalidad: ${modality.ifBlank { "desconocida" }}). No funcionara en NotiVas."
            }
            supportsToolCalling -> {
                me.joxquin.notivas.data.model.ModelCompatibilityStatus.OPTIMAL to "100% Compatible con NotiVas: Soporta chat conversacional y herramientas automaticas de Canvas LMS."
            }
            else -> {
                me.joxquin.notivas.data.model.ModelCompatibilityStatus.COMPATIBLE to "Compatible con chat: Responde preguntas y redacta borradores, pero no soporta herramientas dinamicas de Canvas."
            }
        }

        return me.joxquin.notivas.data.model.CustomModelAnalysisResult(
            modelId = cleanModel,
            name = name,
            description = description,
            contextLength = contextLen,
            isChatCapable = isChatCapable,
            supportsFunctionCalling = supportsToolCalling,
            isMultimodal = isMultimodal,
            status = status,
            message = msg
        )
    }

    private data class ParsedTextToolCall(
        val name: String,
        val argsJson: String
    )

    private fun parseTextToolCalls(content: String): List<ParsedTextToolCall> {
        val list = mutableListOf<ParsedTextToolCall>()
        // Match <tool_call>...</tool_call> blocks
        val toolRegex = Regex("(?s)<tool_call>(.*?)</tool_call>")
        val matches = toolRegex.findAll(content)

        for (match in matches) {
            val inner = match.groupValues[1].trim()
            if (inner.isBlank()) continue

            // Strategy 1: JSON format inside <tool_call> {"name": "...", "arguments": {...}}
            if (inner.startsWith("{") && inner.endsWith("}")) {
                try {
                    val jsonObj = json.decodeFromString(kotlinx.serialization.json.JsonObject.serializer(), inner)
                    val name = jsonObj["name"]?.toString()?.replace("\"", "") ?: ""
                    val args = jsonObj["arguments"]?.toString() ?: jsonObj["parameters"]?.toString() ?: "{}"
                    if (name.isNotBlank()) {
                        list.add(ParsedTextToolCall(name, args))
                        continue
                    }
                } catch (_: Exception) { }
            }

            // Strategy 2: First line or token is function name, followed by xml tags or key-values
            // Example:
            // get_course_assignments\ncourse_id</arg_key>72693</arg_value>
            // or get_course_assignments\n<arg_key>course_id</arg_key><arg_value>72693</arg_value>
            val lines = inner.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            val firstLine = lines.first()
            val fnName = firstLine.substringBefore('<').substringBefore('(').substringBefore(' ').trim()

            if (fnName.isNotBlank() && toolExecutor.tools.any { it.function.name.equals(fnName, ignoreCase = true) }) {
                val realName = toolExecutor.tools.first { it.function.name.equals(fnName, ignoreCase = true) }.function.name
                val argsMap = mutableMapOf<String, kotlinx.serialization.json.JsonPrimitive>()

                // Match XML style: <arg_key>foo</arg_key><arg_value>bar</arg_value> or foo</arg_key>bar</arg_value>
                val argRegex = Regex("(?s)(?:<arg_key>)?([a-zA-Z0-9_-]+)</arg_key>\\s*(?:<arg_value>)?(.*?)(?:</arg_value>|$)")
                val argMatches = argRegex.findAll(inner)
                for (am in argMatches) {
                    val k = am.groupValues[1].trim()
                    val v = am.groupValues[2].replace("</arg_value>", "").trim()
                    if (k.isNotBlank()) {
                        // Check if numeric or boolean
                        v.toLongOrNull()?.let { num ->
                            argsMap[k] = kotlinx.serialization.json.JsonPrimitive(num)
                        } ?: v.toDoubleOrNull()?.let { num ->
                            argsMap[k] = kotlinx.serialization.json.JsonPrimitive(num)
                        } ?: v.toBooleanStrictOrNull()?.let { bool ->
                            argsMap[k] = kotlinx.serialization.json.JsonPrimitive(bool)
                        } ?: run {
                            argsMap[k] = kotlinx.serialization.json.JsonPrimitive(v)
                        }
                    }
                }

                val argsJson = kotlinx.serialization.json.JsonObject(argsMap).toString()
                list.add(ParsedTextToolCall(realName, argsJson))
            }
        }
        return list
    }
}

