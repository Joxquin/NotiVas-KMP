package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler

class SimulatorToolsHandler(
    private val localStore: InMemoryLocalStore
) : CopilotToolHandler {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "create_simulation_group",
                description = "Crea un grupo de evaluaciones simuladas con sus ponderaciones y notas en el Simulador de Calificaciones para proyectar el promedio final.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "group_name" to OpenRouterProperty(
                            type = "string",
                            description = "Nombre descriptivo del grupo de evaluación (ej. 'Evaluaciones Continuas')"
                        ),
                        "weight" to OpenRouterProperty(
                            type = "number",
                            description = "Peso porcentual del grupo (0 a 100)"
                        ),
                        "assignments" to OpenRouterProperty(
                            type = "array",
                            description = "Lista de evaluaciones con 'name', 'score', 'max_score'"
                        )
                    ),
                    required = listOf("group_name", "assignments")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean = toolName == "create_simulation_group"

    override suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult {
        return when (toolName) {
            "create_simulation_group" -> {
                val groupName = args["group_name"]?.jsonPrimitive?.content ?: "Simulación Copilot"
                val weight = args["weight"]?.jsonPrimitive?.doubleOrNull ?: 100.0
                val assignmentsArray = args["assignments"]?.jsonArray
                val courseId = selectedCourseId

                if (courseId == null || courseId <= 0L) {
                    return ToolExecutionResult("""{"error": "Debes tener un curso seleccionado para poder agregar grupos de simulación en tus notas."}""")
                }

                if (assignmentsArray == null || assignmentsArray.isEmpty()) {
                    ToolExecutionResult("""{"error": "'assignments' debe ser una lista no vacía de evaluaciones."}""")
                } else {
                    val group = SimulationGroup(
                        courseId = courseId,
                        name = groupName,
                        weightPercentage = weight.toFloat()
                    )
                    val groupId = localStore.insertGroup(group)

                    var createdCount = 0
                    assignmentsArray.forEach { element ->
                        val obj = element.jsonObject
                        val name = obj["name"]?.jsonPrimitive?.content ?: return@forEach
                        val score = obj["score"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                        val maxScore = obj["max_score"]?.jsonPrimitive?.doubleOrNull ?: 20.0

                        val item = SimulationItem(
                            groupId = groupId,
                            name = name,
                            simulatedScore = score.toFloat(),
                            maxScore = maxScore.toFloat()
                        )
                        localStore.insertItem(item)
                        createdCount++
                    }

                    val source = CopilotSource(
                        title = "Simulador de Calificaciones",
                        detail = "Grupo '$groupName' ($createdCount notas, peso $weight%) creado en el Simulador"
                    )

                    val feedback = "Se creó el grupo '$groupName' con $createdCount evaluaciones en tu Simulador de Calificaciones."
                    ToolExecutionResult(
                        resultJson = json.encodeToString(mapOf("status" to "success", "group_id" to groupId.toString(), "created_evaluations" to createdCount.toString())),
                        source = source,
                        actionFeedback = feedback
                    )
                }
            }
            else -> ToolExecutionResult("""{"error": "Herramienta no soportada por SimulatorToolsHandler"}""")
        }
    }
}
