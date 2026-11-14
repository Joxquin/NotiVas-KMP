package me.joxquin.notivas.data.repository.copilot.tools.handlers

import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler
import me.joxquin.notivas.domain.study.repository.FlashcardInput
import me.joxquin.notivas.domain.study.repository.StudyRepository

class CanvasStudyToolsHandler(
    private val studyRepository: StudyRepository
) : CopilotToolHandler {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "list_study_decks",
                description = "Lista los mazos de estudio, cantidad de tarjetas didácticas y tarjetas pendientes por repasar hoy con el algoritmo FSRS.",
                parameters = OpenRouterParameters(
                    properties = emptyMap()
                )
            )
        ),
        OpenRouterTool(
            function = OpenRouterFunction(
                name = "create_flashcards_deck",
                description = "Crea un nuevo mazo de tarjetas de estudio con preguntas y respuestas didácticas para el estudiante.",
                parameters = OpenRouterParameters(
                    properties = mapOf(
                        "title" to OpenRouterProperty(
                            type = "string",
                            description = "Título descriptivo del mazo"
                        ),
                        "description" to OpenRouterProperty(
                            type = "string",
                            description = "Descripción o tema del mazo"
                        ),
                        "cards" to OpenRouterProperty(
                            type = "array",
                            description = "Lista de tarjetas con campos 'front' (pregunta/concepto) y 'back' (respuesta concisa)"
                        )
                    ),
                    required = listOf("title", "cards")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean =
        toolName == "list_study_decks" || toolName == "create_flashcards_deck"

    override suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult {
        return when (toolName) {
            "list_study_decks" -> {
                try {
                    val decks = studyRepository.observeAllDecks().first()
                    val simplified = decks.map {
                        mapOf(
                            "id" to it.id.toString(),
                            "title" to it.title,
                            "total_cards" to it.totalCards.toString(),
                            "due_today" to it.dueCardsCount.toString()
                        )
                    }
                    ToolExecutionResult(
                        resultJson = json.encodeToString(simplified),
                        source = CopilotSource(
                            title = "Mazos de Estudio FSRS",
                            url = "#utilidades/study"
                        )
                    )
                } catch (e: Exception) {
                    ToolExecutionResult("""{"error": "No se pudieron obtener los mazos de estudio: ${e.message}"}""")
                }
            }
            "create_flashcards_deck" -> {
                try {
                    val title = args["title"]?.jsonPrimitive?.content ?: "Mazo de Estudio"
                    val desc = args["description"]?.jsonPrimitive?.content ?: "Generado por Copilot"
                    val cardsArray = args["cards"]?.jsonArray

                    val deckId = studyRepository.createDeck(
                        title = title,
                        description = desc,
                        courseId = selectedCourseId
                    )

                    val cardsToInsert = mutableListOf<FlashcardInput>()
                    cardsArray?.forEach { el ->
                        val obj = el.jsonObject
                        val front = obj["front"]?.jsonPrimitive?.content
                        val back = obj["back"]?.jsonPrimitive?.content
                        val hint = obj["hint"]?.jsonPrimitive?.content
                        if (!front.isNullOrBlank() && !back.isNullOrBlank()) {
                            cardsToInsert.add(
                                FlashcardInput(
                                    front = front,
                                    back = back,
                                    hint = hint
                                )
                            )
                        }
                    }

                    if (cardsToInsert.isNotEmpty()) {
                        studyRepository.addCards(deckId, cardsToInsert)
                    }

                    ToolExecutionResult(
                        resultJson = """{"status": "ok", "deck_id": $deckId, "cards_created": ${cardsToInsert.size}}""",
                        actionFeedback = "Se ha creado el mazo '$title' con ${cardsToInsert.size} tarjetas didácticas en Utilidades."
                    )
                } catch (e: Exception) {
                    ToolExecutionResult("""{"error": "Fallo al crear mazo de estudio: ${e.message}"}""")
                }
            }
            else -> ToolExecutionResult("""{"error": "Herramienta desconocida: $toolName"}""")
        }
    }
}
