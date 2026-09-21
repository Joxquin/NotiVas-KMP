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
                        temperature = 0.2
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
                                temperature = 0.2
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

            // Si el modelo solicitó ejecutar herramientas
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

            // Respuesta textual final obtenida
            if (!choiceMessage?.content.isNullOrBlank()) {
                finalReply = choiceMessage?.content
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
}
