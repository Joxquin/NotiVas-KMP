package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorHttpClientProvider {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
        coerceInputValues = true
    }

    fun createClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
