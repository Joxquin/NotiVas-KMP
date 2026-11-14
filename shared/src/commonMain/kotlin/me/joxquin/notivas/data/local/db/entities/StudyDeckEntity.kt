package me.joxquin.notivas.data.local.db.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import me.joxquin.notivas.domain.study.model.StudyDeck

@Entity(
    tableName = "study_decks",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("course_id")
    ]
)
data class StudyDeckEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "course_id")
    val courseId: Long? = null,
    @ColumnInfo(name = "course_name")
    val courseName: String? = null,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "created_at_timestamp")
    val createdAtTimestamp: Long = 0L,
    @ColumnInfo(name = "last_reviewed_timestamp")
    val lastReviewedTimestamp: Long? = null
) {
    fun toDomain(totalCards: Int = 0, dueCardsCount: Int = 0): StudyDeck = StudyDeck(
        id = id,
        courseId = courseId,
        courseName = courseName,
        title = title,
        description = description,
        totalCards = totalCards,
        dueCardsCount = dueCardsCount,
        createdAtEpochMs = createdAtTimestamp,
        lastReviewedAtEpochMs = lastReviewedTimestamp
    )

    companion object {
        fun fromDomain(deck: StudyDeck): StudyDeckEntity = StudyDeckEntity(
            id = deck.id,
            courseId = deck.courseId,
            courseName = deck.courseName,
            title = deck.title,
            description = deck.description,
            createdAtTimestamp = if (deck.createdAtEpochMs > 0L) deck.createdAtEpochMs else me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis(),
            lastReviewedTimestamp = deck.lastReviewedAtEpochMs
        )
    }
}
