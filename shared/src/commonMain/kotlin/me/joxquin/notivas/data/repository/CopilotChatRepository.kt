package me.joxquin.notivas.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.model.CopilotMessage
import me.joxquin.notivas.data.model.CopilotSession

@Serializable
enum class CopilotRole {
    USER, ASSISTANT
}

@Serializable
data class CopilotSource(
    val title: String,
    val detail: String
)

@Serializable
data class CopilotMessageItem(
    val id: String,
    val role: CopilotRole,
    val text: String,
    val sources: List<CopilotSource> = emptyList(),
    val actionFeedback: String? = null,
    val tokens: Int = 0,
    val timestamp: Long
)

class CopilotChatRepository(
    private val localStore: InMemoryLocalStore
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val allSessions: Flow<List<CopilotSession>> = localStore.getAllSessions()

    suspend fun getSessionById(sessionId: String): CopilotSession? {
        return localStore.getSessionById(sessionId)
    }

    suspend fun getMessagesForSession(sessionId: String): List<CopilotMessageItem> {
        val entities = localStore.getMessagesForSessionOnce(sessionId)
        return entities.map { entity ->
            val sources = if (!entity.sourcesJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<CopilotSource>>(entity.sourcesJson)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            CopilotMessageItem(
                id = entity.id,
                role = if (entity.role == "USER") CopilotRole.USER else CopilotRole.ASSISTANT,
                text = entity.text,
                sources = sources,
                actionFeedback = entity.actionFeedback,
                tokens = entity.tokens,
                timestamp = entity.timestamp
            )
        }
    }

    suspend fun createOrUpdateSession(
        sessionId: String,
        title: String,
        courseId: Long?,
        initialTokens: Int = 0
    ) {
        val existing = localStore.getSessionById(sessionId)
        if (existing == null) {
            localStore.insertSession(
                CopilotSession(
                    id = sessionId,
                    title = title,
                    courseId = courseId,
                    totalTokens = initialTokens,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        } else {
            localStore.updateSessionTokens(sessionId, existing.totalTokens + initialTokens)
        }
    }

    suspend fun saveMessage(
        sessionId: String,
        message: CopilotMessageItem
    ) {
        val sourcesJsonStr = if (message.sources.isNotEmpty()) {
            json.encodeToString(message.sources)
        } else null

        localStore.insertMessage(
            CopilotMessage(
                id = message.id,
                sessionId = sessionId,
                role = message.role.name,
                text = message.text,
                sourcesJson = sourcesJsonStr,
                actionFeedback = message.actionFeedback,
                tokens = message.tokens,
                timestamp = message.timestamp
            )
        )

        localStore.updateSessionTimestamp(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        localStore.updateSessionTitle(sessionId, newTitle)
    }

    suspend fun updateSessionTokens(sessionId: String, totalTokens: Int) {
        localStore.updateSessionTokens(sessionId, totalTokens)
    }

    suspend fun deleteSession(sessionId: String) {
        localStore.deleteSession(sessionId)
    }

    suspend fun clearMessages(sessionId: String) {
        localStore.deleteMessagesForSession(sessionId)
    }
}
