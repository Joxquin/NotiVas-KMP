package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CopilotSession(
    val id: String,
    val title: String,
    val courseId: Long? = null,
    val totalTokens: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class CopilotMessage(
    val id: String,
    val sessionId: String,
    val role: String, // "USER" or "ASSISTANT"
    val text: String,
    val sourcesJson: String? = null,
    val actionFeedback: String? = null,
    val tokens: Int = 0,
    val timestamp: Long
)
