package me.joxquin.notivas.data.local.db.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import me.joxquin.notivas.domain.study.model.QuizResult

@Entity(
    tableName = "quiz_history",
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
        Index("timestamp")
    ]
)
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "deck_id")
    val deckId: Long,
    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,
    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int,
    @ColumnInfo(name = "score_percentage")
    val scorePercentage: Float,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {
    fun toDomain(): QuizResult = QuizResult(
        deckId = deckId,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        durationSeconds = durationSeconds,
        timestampEpochMs = timestamp
    )

    companion object {
        fun fromDomain(result: QuizResult): QuizHistoryEntity = QuizHistoryEntity(
            deckId = result.deckId,
            totalQuestions = result.totalQuestions,
            correctAnswers = result.correctAnswers,
            scorePercentage = result.scorePercentage,
            durationSeconds = result.durationSeconds,
            timestamp = result.timestampEpochMs
        )
    }
}
