package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.SubmissionDetails

@Entity(
    tableName = "assignments",
    indices = [
        Index("course_id"),
        Index("due_at"),
        Index("status")
    ]
)
data class AssignmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "due_at")
    val dueAt: String? = null,
    @ColumnInfo(name = "lock_at")
    val lockAt: String? = null,
    @ColumnInfo(name = "points_possible")
    val pointsPossible: Double? = null,
    @ColumnInfo(name = "html_url")
    val htmlUrl: String? = null,
    @ColumnInfo(name = "submission_types")
    val submissionTypes: List<String>? = null,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = false,
    @ColumnInfo(name = "status")
    val status: String = "upcoming",
    @ColumnInfo(name = "submitted_at")
    val submittedAt: String? = null,
    @ColumnInfo(name = "graded_at")
    val gradedAt: String? = null,
    @ColumnInfo(name = "score")
    val score: Double? = null,
    @ColumnInfo(name = "grade")
    val grade: String? = null,
    @ColumnInfo(name = "submission_id")
    val submissionId: Long? = null,
    @ColumnInfo(name = "submission_workflow_state")
    val submissionWorkflowState: String? = null,
    @ColumnInfo(name = "submission_late")
    val submissionLate: Boolean? = null,
    @ColumnInfo(name = "submission_missing")
    val submissionMissing: Boolean? = null,
    @ColumnInfo(name = "notification_sent")
    val notificationSent: Boolean = false,
    @ColumnInfo(name = "notified_24h")
    val notified24h: Boolean = false,
    @ColumnInfo(name = "notified_3h")
    val notified3h: Boolean = false,
    @ColumnInfo(name = "notified_30m")
    val notified30m: Boolean = false
) {
    fun toDomain(): Assignment = Assignment(
        id = id,
        name = name,
        description = description,
        dueAt = dueAt,
        lockAt = lockAt,
        courseId = courseId,
        pointsPossible = pointsPossible,
        htmlUrl = htmlUrl,
        submissionTypes = submissionTypes,
        isCompleted = isCompleted,
        isLocked = isLocked,
        submission = if (submissionId != null || submissionWorkflowState != null || score != null || grade != null) {
            SubmissionDetails(
                id = submissionId,
                workflowState = submissionWorkflowState,
                submittedAt = submittedAt,
                gradedAt = gradedAt,
                score = score,
                grade = grade,
                late = submissionLate,
                missing = submissionMissing
            )
        } else null,
        status = status,
        submittedAt = submittedAt,
        gradedAt = gradedAt,
        score = score,
        grade = grade,
        notificationSent = notificationSent,
        notified24h = notified24h,
        notified3h = notified3h,
        notified30m = notified30m
    )

    companion object {
        fun fromDomain(a: Assignment): AssignmentEntity = AssignmentEntity(
            id = a.id,
            courseId = a.courseId,
            name = a.name,
            description = a.description,
            dueAt = a.dueAt,
            lockAt = a.lockAt,
            pointsPossible = a.pointsPossible,
            htmlUrl = a.htmlUrl,
            submissionTypes = a.submissionTypes,
            isCompleted = a.isCompleted,
            isLocked = a.isLocked,
            status = a.status,
            submittedAt = a.submittedAt ?: a.submission?.submittedAt,
            gradedAt = a.gradedAt ?: a.submission?.gradedAt,
            score = a.score ?: a.submission?.score,
            grade = a.grade ?: a.submission?.grade,
            submissionId = a.submission?.id,
            submissionWorkflowState = a.submission?.workflowState,
            submissionLate = a.submission?.late,
            submissionMissing = a.submission?.missing,
            notificationSent = a.notificationSent,
            notified24h = a.notified24h,
            notified3h = a.notified3h,
            notified30m = a.notified30m
        )
    }
}
