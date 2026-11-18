package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.serialization.json.JsonObject
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.plugins.tecsup.copilot.TecsupToolsProvider

class TecsupToolsHandler(
    private val toolsProvider: TecsupToolsProvider
) : CopilotToolHandler {

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            type = "function",
            function = OpenRouterFunction(
                name = "get_tecsup_schedule",
                description = "Obtiene el horario de clases y laboratorios del estudiante en Tecsup.",
                parameters = OpenRouterParameters(
                    type = "object",
                    properties = mapOf(
                        "day" to OpenRouterProperty(
                            type = "string",
                            description = "Dia de la semana opcional para filtrar (ej. 'Lunes', 'Martes', 'Hoy')."
                        )
                    )
                )
            )
        ),
        OpenRouterTool(
            type = "function",
            function = OpenRouterFunction(
                name = "get_tecsup_grades",
                description = "Obtiene las calificaciones ponderadas y el estado de aprobación de los cursos de Tecsup.",
                parameters = OpenRouterParameters(
                    type = "object",
                    properties = mapOf(
                        "courseName" to OpenRouterProperty(
                            type = "string",
                            description = "Nombre o fragmento del curso de Tecsup a consultar."
                        )
                    )
                )
            )
        ),
        OpenRouterTool(
            type = "function",
            function = OpenRouterFunction(
                name = "get_tecsup_attendance",
                description = "Obtiene el reporte de asistencias, inasistencias y límite máximo de faltas en Tecsup.",
                parameters = OpenRouterParameters(
                    type = "object",
                    properties = mapOf(
                        "onlyAlerts" to OpenRouterProperty(
                            type = "boolean",
                            description = "Si es true, solo retorna cursos donde las inasistencias estan cerca del limite maximo."
                        )
                    )
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean {
        return toolsProvider.canHandle(toolName)
    }

    override suspend fun execute(
        toolName: String,
        args: JsonObject,
        selectedCourseId: Long?
    ): ToolExecutionResult {
        val resultJson = toolsProvider.executeTool(toolName, args)
        return ToolExecutionResult(
            resultJson = resultJson,
            source = CopilotSource(
                title = "Tecsup u-planner",
                detail = "Datos institucionales sincronizados",
                url = "https://tecsup-api.u-planner.com"
            )
        )
    }
}
