package me.joxquin.notivas.data.repository.copilot.tools

import kotlinx.serialization.json.JsonObject
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult

interface CopilotToolHandler {
    val supportedTools: List<OpenRouterTool>
    fun canHandle(toolName: String): Boolean
    suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult
}
