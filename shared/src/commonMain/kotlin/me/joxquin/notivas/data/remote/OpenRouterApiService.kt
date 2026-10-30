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
import me.joxquin.notivas.util.resolveApiUrl

import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

class OpenRouterApiService(
    private val client: HttpClient,
    private val baseUrl: String = "https://openrouter.ai/api/v1"
) {
    companion object {
        val requestJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true
        }

        private fun stripNulls(element: JsonElement): JsonElement {
            return when (element) {
                is JsonObject -> {
                    val filteredMap = element.mapNotNull { (key, value) ->
                        if (value is JsonNull) null
                        else key to stripNulls(value)
                    }.toMap()
                    JsonObject(filteredMap)
                }
                is JsonArray -> {
                    val filteredList = element.map { stripNulls(it) }
                    JsonArray(filteredList)
                }
                else -> element
            }
        }
    }

    suspend fun chatCompletion(
        apiKey: String,
        request: OpenRouterChatRequest,
        customBaseUrl: String? = null
    ): OpenRouterChatResponse {
        val targetBaseUrl = (customBaseUrl ?: baseUrl).trimEnd('/')
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val rawJsonTree = requestJson.encodeToJsonElement(OpenRouterChatRequest.serializer(), request)
        val cleanedJsonTree = stripNulls(rawJsonTree)
        val requestBodyJson = cleanedJsonTree.toString()

        val fullUrl = resolveApiUrl("$targetBaseUrl/chat/completions")
        val response = client.post(fullUrl) {
            header("Authorization", authHeader)
            header("HTTP-Referer", "https://github.com/joxquin/NotiVas")
            header("X-Title", "NotiVas Academic Copilot")
            contentType(ContentType.Application.Json)
            setBody(requestBodyJson)
        }
        
        val status = response.status.value
        val responseText = response.body<String>()
        println("Copilot [Api] HTTP $status response from $targetBaseUrl: $responseText")
        
        if (status !in 200..299) {
            val errorMsg = try {
                val errorObj = requestJson.decodeFromString<OpenRouterChatResponse>(responseText)
                errorObj.error?.message ?: "Error HTTP $status del proveedor de IA"
            } catch (_: Throwable) {
                "Error HTTP $status: $responseText"
            }
            throw IllegalStateException(errorMsg)
        }
        
        return requestJson.decodeFromString<OpenRouterChatResponse>(responseText)
    }

    suspend fun getCredits(
        apiKey: String
    ): OpenRouterCreditsResponse {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val fullUrl = resolveApiUrl("$baseUrl/credits")
        return client.get(fullUrl) {
            header("Authorization", authHeader)
        }.body()
    }

    suspend fun getKeyInfo(
        apiKey: String
    ): OpenRouterKeyResponse {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val fullUrl = resolveApiUrl("$baseUrl/auth/key")
        return client.get(fullUrl) {
            header("Authorization", authHeader)
        }.body()
    }

    suspend fun getModelEndpoints(
        modelId: String,
        apiKey: String? = null
    ): me.joxquin.notivas.data.model.OpenRouterModelEndpointsResponse {
        val cleanModel = modelId.trim().removePrefix("/")
        val fullUrl = resolveApiUrl("$baseUrl/models/$cleanModel/endpoints")
        val response = client.get(fullUrl) {
            if (!apiKey.isNullOrBlank()) {
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                header("Authorization", authHeader)
            }
        }
        val status = response.status.value
        val responseText = response.body<String>()
        if (status !in 200..299) {
            val errorMsg = try {
                val errorObj = KtorHttpClientProvider.jsonConfig.decodeFromString<me.joxquin.notivas.data.model.OpenRouterModelEndpointsResponse>(responseText)
                errorObj.error?.message ?: "Error HTTP $status al consultar modelo"
            } catch (_: Exception) {
                "Error HTTP $status al consultar modelo"
            }
            return me.joxquin.notivas.data.model.OpenRouterModelEndpointsResponse(
                data = null,
                error = me.joxquin.notivas.data.model.OpenRouterError(message = errorMsg, code = status)
            )
        }
        return KtorHttpClientProvider.jsonConfig.decodeFromString(responseText)
    }
}
