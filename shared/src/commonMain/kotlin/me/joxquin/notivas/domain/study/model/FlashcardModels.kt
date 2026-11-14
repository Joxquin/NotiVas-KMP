package me.joxquin.notivas.domain.study.model

import kotlinx.serialization.Serializable

/**
 * Calificación al repasar una tarjeta con FSRS-4.5:
 * 1 = Otra vez (Again), 2 = Difícil (Hard), 3 = Bien (Good), 4 = Fácil (Easy).
 */
@Serializable
enum class FsrsRating(val value: Int) {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    companion object {
        fun fromValue(value: Int): FsrsRating = entries.firstOrNull { it.value == value } ?: AGAIN
    }
}

/**
 * Fase en el ciclo de vida de la tarjeta en FSRS.
 */
@Serializable
enum class CardPhase {
    NEW,
    LEARNING,
    REVIEW,
    RELEARNING
}

/**
 * Estado matemático DSR y calendario de repaso de una tarjeta.
 */
@Serializable
data class FsrsReviewState(
    val phase: CardPhase = CardPhase.NEW,
    val dueEpochMs: Long = 0L,
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val lastReviewEpochMs: Long? = null,
    val step: Int = 0
)

/**
 * Mazo o grupo de tarjetas de estudio.
 */
@Serializable
data class StudyDeck(
    val id: Long = 0L,
    val courseId: Long? = null,
    val courseName: String? = null,
    val title: String,
    val description: String = "",
    val totalCards: Int = 0,
    val dueCardsCount: Int = 0,
    val createdAtEpochMs: Long = 0L,
    val lastReviewedAtEpochMs: Long? = null
)

/**
 * Tarjeta individual de estudio interactiva.
 */
@Serializable
data class Flashcard(
    val id: Long = 0L,
    val deckId: Long,
    val front: String,
    val back: String,
    val hint: String? = null,
    val topic: String? = null,
    val state: FsrsReviewState = FsrsReviewState()
)

/**
 * Pregunta de simulacro de examen (quiz).
 */
@Serializable
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
    val topic: String? = null
)

/**
 * Resultado de una sesión de simulacro de examen.
 */
@Serializable
data class QuizResult(
    val deckId: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Float,
    val durationSeconds: Long,
    val timestampEpochMs: Long,
    val topicBreakdown: Map<String, Float> = emptyMap()
)

/**
 * Resumen general de estadísticas de estudio para el Dashboard y Utilidades.
 */
@Serializable
data class StudyOverviewStats(
    val totalDecks: Int = 0,
    val totalCards: Int = 0,
    val dueTodayCount: Int = 0,
    val reviewedTodayCount: Int = 0,
    val currentStreakDays: Int = 0,
    val averageRetentionRate: Float = 0f
)
