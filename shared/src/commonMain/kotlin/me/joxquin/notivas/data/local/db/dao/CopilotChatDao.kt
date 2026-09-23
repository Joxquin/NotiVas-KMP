package me.joxquin.notivas.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.CopilotMessageEntity
import me.joxquin.notivas.data.local.db.entities.CopilotSessionEntity

@Dao
interface CopilotChatDao {
    @Query("SELECT * FROM copilot_sessions ORDER BY updated_at DESC")
    fun observeSessions(): Flow<List<CopilotSessionEntity>>

    @Query("SELECT * FROM copilot_sessions ORDER BY updated_at DESC")
    suspend fun getAllSessions(): List<CopilotSessionEntity>

    @Query("SELECT * FROM copilot_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): CopilotSessionEntity?

    @Upsert
    suspend fun upsertSession(session: CopilotSessionEntity)

    @Upsert
    suspend fun upsertSessions(sessions: List<CopilotSessionEntity>)

    @Query("DELETE FROM copilot_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM copilot_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun observeMessages(sessionId: String): Flow<List<CopilotMessageEntity>>

    @Query("SELECT * FROM copilot_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessages(sessionId: String): List<CopilotMessageEntity>

    @Query("SELECT * FROM copilot_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<CopilotMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CopilotMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CopilotMessageEntity>)

    @Query("DELETE FROM copilot_messages WHERE session_id = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM copilot_messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM copilot_sessions")
    suspend fun deleteAllSessions()
}
