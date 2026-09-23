package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import me.joxquin.notivas.data.model.CopilotMessage
import me.joxquin.notivas.data.model.CopilotSession

@Entity(
    tableName = "copilot_sessions",
    indices = [
        Index("updated_at")
    ]
)
data class CopilotSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "course_id")
    val courseId: Long? = null,
    @ColumnInfo(name = "total_tokens")
    val totalTokens: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {
    fun toDomain(): CopilotSession = CopilotSession(
        id = id,
        title = title,
        courseId = courseId,
        totalTokens = totalTokens,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(session: CopilotSession): CopilotSessionEntity = CopilotSessionEntity(
            id = session.id,
            title = session.title,
            courseId = session.courseId,
            totalTokens = session.totalTokens,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt
        )
    }
}

@Entity(
    tableName = "copilot_messages",
    foreignKeys = [
        ForeignKey(
            entity = CopilotSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("session_id"),
        Index("timestamp")
    ]
)
data class CopilotMessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "role")
    val role: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "sources_json")
    val sourcesJson: String? = null,
    @ColumnInfo(name = "action_feedback")
    val actionFeedback: String? = null,
    @ColumnInfo(name = "tokens")
    val tokens: Int = 0,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {
    fun toDomain(): CopilotMessage = CopilotMessage(
        id = id,
        sessionId = sessionId,
        role = role,
        text = text,
        sourcesJson = sourcesJson,
        actionFeedback = actionFeedback,
        tokens = tokens,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(message: CopilotMessage): CopilotMessageEntity = CopilotMessageEntity(
            id = message.id,
            sessionId = message.sessionId,
            role = message.role,
            text = message.text,
            sourcesJson = message.sourcesJson,
            actionFeedback = message.actionFeedback,
            tokens = message.tokens,
            timestamp = message.timestamp
        )
    }
}
