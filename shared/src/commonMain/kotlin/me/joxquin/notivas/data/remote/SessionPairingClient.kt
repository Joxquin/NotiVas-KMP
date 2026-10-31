package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.joxquin.notivas.data.model.PairCreateResponse
import me.joxquin.notivas.data.model.PairPollResponse
import me.joxquin.notivas.data.model.PairResolvePinResponse

object SessionPairingClient {
    private const val BASE_URL = "https://notivas-cors-proxy.jotajoxquin.workers.dev"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun resolvePin(pin: String): Result<PairResolvePinResponse> {
        return runCatching {
            val response = client.post("$BASE_URL/pair/resolve-pin") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("pin", pin)
                })
            }
            response.body<PairResolvePinResponse>()
        }
    }

    suspend fun completePairing(
        sessionId: String,
        encryptedPayload: String,
        iv: String
    ): Result<Unit> {
        return runCatching {
            client.post("$BASE_URL/pair/complete") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("sessionId", sessionId)
                    put("encryptedPayload", encryptedPayload)
                    put("iv", iv)
                })
            }
            Unit
        }
    }
}

