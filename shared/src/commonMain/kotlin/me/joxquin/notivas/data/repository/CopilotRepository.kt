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
        val apiKey = preferencesManager.openRouterApiKey.first()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(IllegalStateException("OpenRouter API Key no configurada. Ve a Ajustes para agregarla."))
        }

        val model = preferencesManager.openRouterModel.first()
        val courses = localStore.courses.first()
        val systemPrompt = promptBuilder.buildSystemPrompt(courses, selectedCourseId)

        val messages = promptBuilder.buildConversationMessages(systemPrompt, history, userPrompt)

        val accumulatedSources = mutableListOf<CopilotSource>()
        val accumulatedActionFeedback = mutableListOf<String>()
        var totalPromptTokens = 0
        var totalCompletionTokens = 0
        var totalTokens = 0

        var currentAttempt = 0
        val maxToolIterations = 4
        var finalReply: String? = null

        while (currentAttempt < maxToolIterations) {
            currentAttempt++

            // Intentar primero con tools
            var response = try {
                openRouterApiService.chatCompletion(
                    apiKey = apiKey,
                    request = OpenRouterChatRequest(
                        model = model,
                        messages = messages,
                        tools = toolExecutor.tools,
                        temperature = 0.3,
                        maxTokens = 4000
                    )
                )
            } catch (e: Exception) {
                // Si el proveedor del modelo rechaza el parámetro 'tools' o arroja 400 Provider error
                val isProviderToolError = e.message?.contains("Provider returned error", ignoreCase = true) == true ||
                                          e.message?.contains("invalid_request_error", ignoreCase = true) == true ||
                                          e.message?.contains("tools", ignoreCase = true) == true

                if (isProviderToolError) {
                    println("Copilot [Aviso] El modelo '$model' no soporta function calling o el proveedor falló. Reintentando sin tools...")
                    try {
                        openRouterApiService.chatCompletion(
                            apiKey = apiKey,
                            request = OpenRouterChatRequest(
                                model = model,
                                messages = messages,
                                tools = null,
                                temperature = 0.3,
                                maxTokens = 4000
                            )
                        )
                    } catch (retryException: Exception) {
                        println("Copilot [Error] Falló reintento sin tools: ${retryException.message}")
                        return Result.failure(retryException)
                    }
                } else {
                    println("Copilot [Error] Falló la petición a OpenRouter: ${e.message}")
                    return Result.failure(e)
                }
            }

            if (response.error != null) {
                val err = response.error.message ?: "Error en la respuesta de OpenRouter (Código: ${response.error.code})"
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

