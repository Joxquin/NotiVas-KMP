package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
        return HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000L
                connectTimeoutMillis = 20_000L
                socketTimeoutMillis = 60_000L
            }
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

@Serializable
data class GoogleOAuthTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("token_type") val tokenType: String? = null
)

@Serializable
data class GoogleUserInfoResponse(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val picture: String? = null
)

object GoogleDriveConstants {
    // Desktop / Web OAuth Client ID
    const val CLIENT_ID_DESKTOP = "1085214217081-r1j4g4ndnmpm9n4coltds7fkruj800uo.apps.googleusercontent.com"
    // Android Native Client ID
    const val CLIENT_ID_ANDROID = "1085214217081-nkmpa3ghrt8i5qidqed9h42qnbobp1hm.apps.googleusercontent.com"
    // Default fallback
    const val CLIENT_ID = CLIENT_ID_DESKTOP
    const val PROJECT_ID = "notivas-510404"
    const val PACKAGE_NAME = "me.joxquin.notivas"
    const val SHA1_FINGERPRINT = "5F:76:A5:27:CC:A7:23:D8:B4:03:23:A9:5C:94:EA:DC:8E:37:16:DA"
    const val AUTH_URI = "https://accounts.google.com/o/oauth2/auth"
    const val TOKEN_URI = "https://oauth2.googleapis.com/token"
    const val USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo"
    const val SCOPES = "https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/userinfo.email"
}

class GoogleDriveAuthService(
    private val httpClient: HttpClient = KtorHttpClientProvider.createClient()
) {
    fun getAuthorizationUrl(redirectUri: String, state: String = "notivas_auth"): String {
        val scopeEncoded = GoogleDriveConstants.SCOPES
        return "${GoogleDriveConstants.AUTH_URI}?" +
                "client_id=${GoogleDriveConstants.CLIENT_ID_DESKTOP}&" +
                "redirect_uri=${redirectUri}&" +
                "response_type=code&" +
                "scope=${scopeEncoded.replace(" ", "%20")}&" +
                "access_type=offline&" +
                "prompt=consent&" +
                "state=${state}"
    }

    suspend fun exchangeCodeForTokens(
        code: String,
        redirectUri: String,
        clientId: String = GoogleDriveConstants.CLIENT_ID_DESKTOP
    ): GoogleOAuthTokenResponse? {
        return try {
            httpClient.submitForm(
                url = GoogleDriveConstants.TOKEN_URI,
                formParameters = parameters {
                    append("code", code)
                    append("client_id", clientId)
                    append("redirect_uri", redirectUri)
                    append("grant_type", "authorization_code")
                }
            ).body<GoogleOAuthTokenResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun refreshAccessToken(
        refreshToken: String,
        clientId: String = GoogleDriveConstants.CLIENT_ID_DESKTOP
    ): GoogleOAuthTokenResponse? {
        return try {
            httpClient.submitForm(
                url = GoogleDriveConstants.TOKEN_URI,
                formParameters = parameters {
                    append("refresh_token", refreshToken)
                    append("client_id", clientId)
                    append("grant_type", "refresh_token")
                }
            ).body<GoogleOAuthTokenResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchUserInfo(accessToken: String): GoogleUserInfoResponse? {
        return try {
            httpClient.get(GoogleDriveConstants.USER_INFO_URI) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.body<GoogleUserInfoResponse>()
        } catch (_: Exception) {
            null
        }
    }
}

