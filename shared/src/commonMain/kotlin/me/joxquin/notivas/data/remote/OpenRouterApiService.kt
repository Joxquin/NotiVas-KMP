package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterChatResponse
import me.joxquin.notivas.data.model.OpenRouterCreditsResponse
import me.joxquin.notivas.data.model.OpenRouterKeyResponse

class OpenRouterApiService(
    private val client: HttpClient,
    private val baseUrl: String = "https://openrouter.ai/api/v1"
) {
    suspend fun chatCompletion(
        apiKey: String,
        request: OpenRouterChatRequest
    ): OpenRouterChatResponse {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val response = client.post("$baseUrl/chat/completions") {
            header("Authorization", authHeader)
            header("HTTP-Referer", "https://github.com/joxquin/NotiVas")
            header("X-Title", "NotiVas Academic Copilot")
            setBody(request)
        }
        
        val status = response.status.value
        val responseText = response.body<String>()
        println("Copilot [OpenRouter] HTTP $status response: $responseText")
        
        if (status !in 200..299) {
            val errorMsg = try {
                val errorObj = KtorHttpClientProvider.jsonConfig.decodeFromString<OpenRouterChatResponse>(responseText)
                errorObj.error?.message ?: "Error HTTP $status de OpenRouter"
            } catch (e: Exception) {
                "Error HTTP $status: $responseText"
            }
            throw IllegalStateException(errorMsg)
        }
        
        return KtorHttpClientProvider.jsonConfig.decodeFromString<OpenRouterChatResponse>(responseText)
    }

    suspend fun getCredits(
        apiKey: String
    ): OpenRouterCreditsResponse {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        return client.get("$baseUrl/credits") {
            header("Authorization", authHeader)
        }.body()
    }

    suspend fun getKeyInfo(
        apiKey: String
    ): OpenRouterKeyResponse {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        return client.get("$baseUrl/auth/key") {
            header("Authorization", authHeader)
        }.body()
    }
}
