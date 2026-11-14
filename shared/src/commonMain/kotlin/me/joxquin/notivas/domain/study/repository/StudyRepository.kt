package me.joxquin.notivas.domain.study.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.joxquin.notivas.data.local.db.dao.StudyDao
import me.joxquin.notivas.data.local.db.entities.FlashcardEntity
import me.joxquin.notivas.data.local.db.entities.QuizHistoryEntity
import me.joxquin.notivas.data.local.db.entities.StudyDeckEntity
import me.joxquin.notivas.domain.study.fsrs.FsrsAlgorithm
import me.joxquin.notivas.domain.study.model.Flashcard
import me.joxquin.notivas.domain.study.model.FsrsRating
import me.joxquin.notivas.domain.study.model.QuizResult
import me.joxquin.notivas.domain.study.model.StudyDeck
import me.joxquin.notivas.domain.study.model.StudyOverviewStats
import me.joxquin.notivas.util.DateTimeUtil

class StudyRepository(
    private val studyDao: StudyDao,
    private val fsrsAlgorithm: FsrsAlgorithm = FsrsAlgorithm()
) {

    fun observeAllDecks(): Flow<List<StudyDeck>> {
        return studyDao.observeAllDecks().map { entities ->
            val now = DateTimeUtil.nowEpochMillis()
            entities.map { entity ->
                val total = studyDao.getTotalCardsCountByDeck(entity.id)
                val due = studyDao.getDueCardsCountByDeck(entity.id, now)
                entity.toDomain(totalCards = total, dueCardsCount = due)
            }
        }
    }

    suspend fun getDeckById(deckId: Long): StudyDeck? {
        val entity = studyDao.getDeckById(deckId) ?: return null
        val now = DateTimeUtil.nowEpochMillis()
        val total = studyDao.getTotalCardsCountByDeck(deckId)
        val due = studyDao.getDueCardsCountByDeck(deckId, now)
        return entity.toDomain(totalCards = total, dueCardsCount = due)
    }

    fun observeCardsByDeck(deckId: Long): Flow<List<Flashcard>> {
        return studyDao.observeCardsByDeck(deckId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getCardsByDeck(deckId: Long): List<Flashcard> {
        return studyDao.getCardsByDeck(deckId).map { it.toDomain() }
    }

    fun observeDueCards(nowMs: Long = DateTimeUtil.nowEpochMillis()): Flow<List<Flashcard>> {
        return studyDao.observeDueCards(nowMs).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeDueCardsCount(nowMs: Long = DateTimeUtil.nowEpochMillis()): Flow<Int> {
        return studyDao.observeDueCardsCount(nowMs)
    }

    suspend fun createDeck(
        title: String,
        description: String = "",
        courseId: Long? = null,
        courseName: String? = null
    ): Long {
        val now = DateTimeUtil.nowEpochMillis()
        val entity = StudyDeckEntity(
            courseId = courseId,
            courseName = courseName,
            title = title,
            description = description,
            createdAtTimestamp = now
        )
        return studyDao.upsertDeck(entity)
    }

    suspend fun deleteDeck(deckId: Long) {
        studyDao.deleteDeck(deckId)
    }

    suspend fun addCard(
        deckId: Long,
        front: String,
        back: String,
        hint: String? = null,
        topic: String? = null
    ): Long {
        val entity = FlashcardEntity(
            deckId = deckId,
            front = front,
            back = back,
            hint = hint,
            topic = topic,
            dueEpochMs = DateTimeUtil.nowEpochMillis()
        )
        return studyDao.upsertCard(entity)
    }

    suspend fun addCards(
        deckId: Long,
        items: List<FlashcardInput>
    ) {
        val now = DateTimeUtil.nowEpochMillis()
        val entities = items.map { item ->
            FlashcardEntity(
                deckId = deckId,
                front = item.front,
                back = item.back,
                hint = item.hint,
                topic = item.topic,
                dueEpochMs = now
            )
        }
        studyDao.upsertCards(entities)
    }

    suspend fun deleteCard(cardId: Long) {
        studyDao.deleteCard(cardId)
    }

    suspend fun reviewCard(
        card: Flashcard,
        rating: FsrsRating,
        nowMs: Long = DateTimeUtil.nowEpochMillis()
    ): Flashcard {
        val nextState = fsrsAlgorithm.next(card.state, rating, nowMs)
        val updatedCard = card.copy(state = nextState)
        studyDao.upsertCard(FlashcardEntity.fromDomain(updatedCard))
        studyDao.updateDeckLastReviewed(card.deckId, nowMs)
        return updatedCard
    }

    suspend fun recordQuizResult(result: QuizResult) {
        studyDao.insertQuizHistory(QuizHistoryEntity.fromDomain(result))
    }

    fun observeQuizHistory(deckId: Long): Flow<List<QuizResult>> {
        return studyDao.observeQuizHistoryByDeck(deckId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeOverviewStats(): Flow<StudyOverviewStats> {
        val now = DateTimeUtil.nowEpochMillis()
        return combine(
            studyDao.observeAllDecks(),
            studyDao.observeDueCards(now)
        ) { decks, dueCards ->
            val totalDecks = decks.size
            val dueCount = dueCards.size
            var totalCardsCount = 0
            for (d in decks) {
                totalCardsCount += studyDao.getTotalCardsCountByDeck(d.id)
            }
            StudyOverviewStats(
                totalDecks = totalDecks,
                totalCards = totalCardsCount,
                dueTodayCount = dueCount,
                reviewedTodayCount = 0,
                currentStreakDays = if (dueCount == 0 && totalCardsCount > 0) 1 else 0
            )
        }
    }
}

data class FlashcardInput(
    val front: String,
    val back: String,
    val hint: String? = null,
    val topic: String? = null
)
