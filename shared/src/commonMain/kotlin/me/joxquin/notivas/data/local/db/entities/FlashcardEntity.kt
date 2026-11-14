package me.joxquin.notivas.data.local.db.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import me.joxquin.notivas.domain.study.model.CardPhase
import me.joxquin.notivas.domain.study.model.Flashcard
import me.joxquin.notivas.domain.study.model.FsrsReviewState

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = StudyDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("deck_id"),
        Index("due_epoch_ms")
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "deck_id")
    val deckId: Long,
    @ColumnInfo(name = "front")
    val front: String,
    @ColumnInfo(name = "back")
    val back: String,
    @ColumnInfo(name = "hint")
    val hint: String? = null,
    @ColumnInfo(name = "topic")
    val topic: String? = null,
    @ColumnInfo(name = "phase")
    val phase: String = CardPhase.NEW.name,
    @ColumnInfo(name = "due_epoch_ms")
    val dueEpochMs: Long = 0L,
    @ColumnInfo(name = "stability")
    val stability: Double = 0.0,
    @ColumnInfo(name = "difficulty")
    val difficulty: Double = 0.0,
    @ColumnInfo(name = "reps")
    val reps: Int = 0,
    @ColumnInfo(name = "lapses")
    val lapses: Int = 0,
    @ColumnInfo(name = "last_review_epoch_ms")
    val lastReviewEpochMs: Long? = null,
    @ColumnInfo(name = "step")
    val step: Int = 0
) {
    fun toDomain(): Flashcard {
        val parsedPhase = try {
            CardPhase.valueOf(phase)
        } catch (_: Exception) {
            CardPhase.NEW
        }
        return Flashcard(
            id = id,
            deckId = deckId,
            front = front,
            back = back,
            hint = hint,
            topic = topic,
            state = FsrsReviewState(
                phase = parsedPhase,
                dueEpochMs = dueEpochMs,
                stability = stability,
                difficulty = difficulty,
                reps = reps,
                lapses = lapses,
                lastReviewEpochMs = lastReviewEpochMs,
                step = step
            )
        )
    }

    companion object {
        fun fromDomain(card: Flashcard): FlashcardEntity = FlashcardEntity(
            id = card.id,
            deckId = card.deckId,
            front = card.front,
            back = card.back,
            hint = card.hint,
            topic = card.topic,
            phase = card.state.phase.name,
            dueEpochMs = card.state.dueEpochMs,
            stability = card.state.stability,
            difficulty = card.state.difficulty,
            reps = card.state.reps,
            lapses = card.state.lapses,
            lastReviewEpochMs = card.state.lastReviewEpochMs,
            step = card.state.step
        )
    }
}
