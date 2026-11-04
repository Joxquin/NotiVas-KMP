package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("lock_at") val lockAt: String? = null,
    @SerialName("course_id") val courseId: Long,
    @SerialName("points_possible") val pointsPossible: Double? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("submission_types") val submissionTypes: List<String>? = null,
    @SerialName("allowed_extensions") val allowedExtensions: List<String>? = null,
    @SerialName("has_submitted_submissions") val isCompleted: Boolean = false,
    @SerialName("locked_for_user") val isLocked: Boolean = false,
    @SerialName("submission") val submission: SubmissionDetails? = null,
    @SerialName("allowed_attempts") val allowedAttempts: Int? = null,
    val status: String = "upcoming", // "upcoming", "completed", "missing"
    val submittedAt: String? = null,
    val gradedAt: String? = null,
    val score: Double? = null,
    val grade: String? = null,
    val notificationSent: Boolean = false,
    val notified24h: Boolean = false,
    val notified3h: Boolean = false,
    val notified30m: Boolean = false
) {
    val isQuiz: Boolean
        get() = submissionTypes?.contains("online_quiz") == true

    val supportsDirectSubmission: Boolean
        get() = submissionTypes?.any { it in listOf("online_upload", "online_text_entry", "online_url", "media_recording") } == true
}

@Serializable
data class SubmissionDetails(
    @SerialName("id") val id: Long? = null,
    @SerialName("workflow_state") val workflowState: String? = null,
    @SerialName("submission_type") val submissionType: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("attempt") val attempt: Int? = null,
    @SerialName("attachments") val attachments: List<CanvasAttachment>? = null,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null,
    @SerialName("score") val score: Double? = null,
    @SerialName("grade") val grade: String? = null,
    @SerialName("late") val late: Boolean? = null,
    @SerialName("missing") val missing: Boolean? = null
)

@Serializable
data class CanvasAttachment(
    @SerialName("id") val id: Long? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("content-type") val contentType: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null
)

