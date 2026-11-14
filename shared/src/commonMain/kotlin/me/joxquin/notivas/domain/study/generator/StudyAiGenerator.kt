package me.joxquin.notivas.domain.study.generator

import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.CopilotProvider
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.domain.study.model.QuizQuestion
import me.joxquin.notivas.domain.study.repository.FlashcardInput

private data class ResolvedAiConfig(
    val apiKey: String,
    val model: String,
    val customBaseUrl: String?
)

@Serializable
private data class GeneratedFlashcardRaw(
    val front: String,
    val back: String,
    val hint: String? = null,
    val topic: String? = null
)

@Serializable
private data class GeneratedQuizRaw(
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
    val topic: String? = null
)

class StudyAiGenerator(
    private val openRouterApiService: OpenRouterApiService,
    private val preferencesManager: PreferencesManager
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private suspend fun resolveAiConfig(): Result<ResolvedAiConfig> {
        val provider = preferencesManager.copilotProvider.first()
        val googleKey = preferencesManager.googleAiStudioApiKey.first()?.trim()
        val openRouterKey = preferencesManager.openRouterApiKey.first()?.trim()

        return when {
            provider == CopilotProvider.GOOGLE_AI_STUDIO && !googleKey.isNullOrBlank() -> {
                val model = preferencesManager.googleAiStudioModel.first().ifBlank { "gemini-2.5-flash" }
                Result.success(ResolvedAiConfig(googleKey, model, "https://generativelanguage.googleapis.com/v1beta/openai"))
            }
            provider == CopilotProvider.OPENROUTER && !openRouterKey.isNullOrBlank() -> {
                val model = preferencesManager.openRouterModel.first().ifBlank { "google/gemini-2.5-flash" }
                Result.success(ResolvedAiConfig(openRouterKey, model, null))
            }
            !googleKey.isNullOrBlank() -> {
                val model = preferencesManager.googleAiStudioModel.first().ifBlank { "gemini-2.5-flash" }
                Result.success(ResolvedAiConfig(googleKey, model, "https://generativelanguage.googleapis.com/v1beta/openai"))
            }
            !openRouterKey.isNullOrBlank() -> {
                val model = preferencesManager.openRouterModel.first().ifBlank { "google/gemini-2.5-flash" }
                Result.success(ResolvedAiConfig(openRouterKey, model, null))
            }
            else -> {
                Result.failure(IllegalStateException("No hay clave de IA configurada. Ve a Ajustes > Copilot para agregar tu API Key de Google AI Studio u OpenRouter."))
            }
        }
    }

    suspend fun generateFlashcards(
        topicOrContext: String,
        count: Int = 6
    ): Result<List<FlashcardInput>> {
        val config = resolveAiConfig().getOrElse { return Result.failure(it) }

        val prompt = """
            Eres un profesor universitario experto. Tu objetivo es generar exactamente $count tarjetas de estudio (flashcards)
            didácticas, precisas y concisas basadas en el siguiente material o tema:
            
            \"\"\"
            $topicOrContext
            \"\"\"
            
            Responde ÚNICAMENTE con un array JSON válido sin texto introductorio ni formato markdown con bloques de código, con este esquema exacto:
            [
              {
                "front": "Pregunta clara o concepto clave",
                "back": "Respuesta concisa y directa",
                "hint": "Pista breve opcional",
                "topic": "Nombre del subtema"
              }
            ]
        """.trimIndent()

        val request = OpenRouterChatRequest(
            model = config.model,
            messages = listOf(
                OpenRouterMessage(role = "system", content = "Eres un generador pedagógico de tarjetas de estudio. Responde estrictamente en JSON."),
                OpenRouterMessage(role = "user", content = prompt)
            ),
            temperature = 0.3
        )

        return try {
            val response = openRouterApiService.chatCompletion(config.apiKey, request, config.customBaseUrl)
            val content = response.choices?.firstOrNull()?.message?.content.orEmpty()
            val sanitized = sanitizeJson(content)
            val parsed = json.decodeFromString<List<GeneratedFlashcardRaw>>(sanitized)
            Result.success(parsed.map {
                FlashcardInput(
                    front = it.front,
                    back = it.back,
                    hint = it.hint,
                    topic = it.topic
                )
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(
        topicOrContext: String,
        questionCount: Int = 5
    ): Result<List<QuizQuestion>> {
        val config = resolveAiConfig().getOrElse { return Result.failure(it) }

        val prompt = """
            Eres un evaluador académico riguroso. Crea un simulacro de examen con $questionCount preguntas de opción múltiple
            sobre el siguiente contenido:
            
            \"\"\"
            $topicOrContext
            \"\"\"
            
            Requisitos:
            - Exactamente 4 opciones por pregunta.
            - Una sola opción correcta indicada por índice (0, 1, 2 o 3).
            - Explicación breve de por qué la respuesta es la correcta.
            
            Responde ÚNICAMENTE con un array JSON sin texto adicional ni bloques de markdown:
            [
              {
                "question": "Texto de la pregunta",
                "options": ["Opción A", "Opción B", "Opción C", "Opción D"],
                "correctOptionIndex": 0,
                "explanation": "Justificación de la respuesta",
                "topic": "Subtema"
              }
            ]
        """.trimIndent()

        val request = OpenRouterChatRequest(
            model = config.model,
            messages = listOf(
                OpenRouterMessage(role = "system", content = "Eres un creador de simulacros de examen académicos. Responde estrictamente en JSON."),
                OpenRouterMessage(role = "user", content = prompt)
            ),
            temperature = 0.3
        )

        return try {
            val response = openRouterApiService.chatCompletion(config.apiKey, request, config.customBaseUrl)
            val content = response.choices?.firstOrNull()?.message?.content.orEmpty()
            val sanitized = sanitizeJson(content)
            val parsed = json.decodeFromString<List<GeneratedQuizRaw>>(sanitized)
            Result.success(parsed.mapIndexed { idx, it ->
                QuizQuestion(
                    id = (idx + 1).toString(),
                    question = it.question,
                    options = it.options,
                    correctOptionIndex = it.correctOptionIndex.coerceIn(0, (it.options.size - 1).coerceAtLeast(0)),
                    explanation = it.explanation,
                    topic = it.topic
                )
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        val startIdx = text.indexOf('[')
        val endIdx = text.lastIndexOf(']')
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx + 1)
        }
        return text.trim()
    }
}
