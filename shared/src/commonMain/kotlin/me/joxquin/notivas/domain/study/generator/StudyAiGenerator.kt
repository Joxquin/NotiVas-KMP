package me.joxquin.notivas.domain.study.generator

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.domain.study.model.QuizQuestion
import me.joxquin.notivas.domain.study.repository.FlashcardInput

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

    suspend fun generateFlashcards(
        topicOrContext: String,
        count: Int = 6
    ): Result<List<FlashcardInput>> {
        val apiKey = preferencesManager.getOpenRouterApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("No hay clave de IA configurada"))
        }

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
            model = preferencesManager.getOpenRouterModel().ifBlank { "google/gemini-2.5-flash" },
            messages = listOf(
                OpenRouterMessage(role = "system", content = "Eres un generador pedagógico de tarjetas de estudio. Responde estrictamente en JSON."),
                OpenRouterMessage(role = "user", content = prompt)
            ),
            temperature = 0.3
        )

        return try {
            val response = openRouterApiService.chatCompletion(apiKey, request)
            val content = response.choices.firstOrNull()?.message?.content.orEmpty()
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
        val apiKey = preferencesManager.getOpenRouterApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("No hay clave de IA configurada"))
        }

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
            model = preferencesManager.getOpenRouterModel().ifBlank { "google/gemini-2.5-flash" },
            messages = listOf(
                OpenRouterMessage(role = "system", content = "Eres un creador de simulacros de examen académicos. Responde estrictamente en JSON."),
                OpenRouterMessage(role = "user", content = prompt)
            ),
            temperature = 0.3
        )

        return try {
            val response = openRouterApiService.chatCompletion(apiKey, request)
            val content = response.choices.firstOrNull()?.message?.content.orEmpty()
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
