package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AuthMode {
    TOKEN,
    COOKIE_SESSION
}

@Serializable
sealed interface CanvasAuthCredential {
    @Serializable
    data class BearerToken(val token: String) : CanvasAuthCredential

    @Serializable
    data class SessionCookies(val cookies: String, val csrfToken: String? = null) : CanvasAuthCredential
}
