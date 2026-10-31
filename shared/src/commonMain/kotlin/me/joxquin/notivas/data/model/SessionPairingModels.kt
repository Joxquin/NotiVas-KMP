package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionPairingPayload(
    val universityUrl: String,
    val authMode: AuthMode,
    val accessToken: String? = null,
    val sessionCookies: String? = null,
    val csrfToken: String? = null
)

@Serializable
data class PairCreateResponse(
    val sessionId: String,
    val pin: String,
    val expiresIn: Int = 120
)

@Serializable
data class PairResolvePinResponse(
    val sessionId: String,
    val publicKey: String,
    val status: String
)

@Serializable
data class PairPollResponse(
    val status: String,
    val encryptedPayload: String? = null,
    val iv: String? = null
)

