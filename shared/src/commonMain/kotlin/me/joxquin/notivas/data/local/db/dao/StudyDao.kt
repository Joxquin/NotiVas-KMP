package me.joxquin.notivas.data.local.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.FlashcardEntity
import me.joxquin.notivas.data.local.db.entities.QuizHistoryEntity
import me.joxquin.notivas.data.local.db.entities.StudyDeckEntity

@Dao
interface StudyDao {

    // --- MAZOS (DECKS) ---
    @Query("SELECT * FROM study_decks ORDER BY created_at_timestamp DESC")
    fun observeAllDecks(): Flow<List<StudyDeckEntity>>

    @Query("SELECT * FROM study_decks WHERE course_id = :courseId ORDER BY created_at_timestamp DESC")
    fun observeDecksByCourse(courseId: Long): Flow<List<StudyDeckEntity>>

    @Query("SELECT * FROM study_decks WHERE id = :id LIMIT 1")
    suspend fun getDeckById(id: Long): StudyDeckEntity?

    @Upsert
    suspend fun upsertDeck(deck: StudyDeckEntity): Long

    @Query("DELETE FROM study_decks WHERE id = :id")
    suspend fun deleteDeck(id: Long)

    @Query("UPDATE study_decks SET last_reviewed_timestamp = :timestamp WHERE id = :deckId")
    suspend fun updateDeckLastReviewed(deckId: Long, timestamp: Long)

    // --- TARJETAS (FLASHCARDS) ---
    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId ORDER BY id ASC")
    fun observeCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId ORDER BY id ASC")
    suspend fun getCardsByDeck(deckId: Long): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE due_epoch_ms <= :nowEpochMs ORDER BY due_epoch_ms ASC")
    fun observeDueCards(nowEpochMs: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE due_epoch_ms <= :nowEpochMs")
    fun observeDueCardsCount(nowEpochMs: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE due_epoch_ms <= :nowEpochMs")
    suspend fun getDueCardsCount(nowEpochMs: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE deck_id = :deckId AND due_epoch_ms <= :nowEpochMs")
    suspend fun getDueCardsCountByDeck(deckId: Long, nowEpochMs: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE deck_id = :deckId")
    suspend fun getTotalCardsCountByDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getTotalCardsCount(): Int

    @Upsert
    suspend fun upsertCard(card: FlashcardEntity): Long

    @Upsert
    suspend fun upsertCards(cards: List<FlashcardEntity>)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Query("DELETE FROM flashcards WHERE deck_id = :deckId")
    suspend fun deleteCardsByDeck(deckId: Long)

    // --- HISTORIAL DE SIMULACROS ---
    @Query("SELECT * FROM quiz_history WHERE deck_id = :deckId ORDER BY timestamp DESC")
    fun observeQuizHistoryByDeck(deckId: Long): Flow<List<QuizHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(history: QuizHistoryEntity)
}
