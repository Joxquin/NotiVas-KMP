package me.joxquin.notivas.data.repository.copilot

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasAcademicToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasDiscussionsToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.CanvasModulesToolsHandler
import me.joxquin.notivas.data.repository.copilot.tools.handlers.SimulatorToolsHandler

data class ToolExecutionResult(
    val resultJson: String,
    val source: CopilotSource? = null,
    val actionFeedback: String? = null
)

class CopilotToolExecutor(
    canvasAcademicToolsHandler: CanvasAcademicToolsHandler,
    canvasModulesToolsHandler: CanvasModulesToolsHandler,
    canvasDiscussionsToolsHandler: CanvasDiscussionsToolsHandler,
    simulatorToolsHandler: SimulatorToolsHandler
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val handlers: List<CopilotToolHandler> = listOf(
        canvasAcademicToolsHandler,
        canvasModulesToolsHandler,
        canvasDiscussionsToolsHandler,
        simulatorToolsHandler
    )

    val tools: List<OpenRouterTool> = handlers.flatMap { it.supportedTools }

    suspend fun executeTool(
        functionName: String,
        rawArgs: String,
        selectedCourseId: Long?
    ): ToolExecutionResult {
        val args = try {
            json.decodeFromString(JsonObject.serializer(), rawArgs)
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }

        val handler = handlers.firstOrNull { it.canHandle(functionName) }
        return handler?.execute(functionName, args, selectedCourseId)
            ?: ToolExecutionResult("""{"error": "Herramienta no reconocida: $functionName"}""")
    }
}
