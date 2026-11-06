package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.joxquin.notivas.util.DateTimeUtil

@Serializable
data class ExternalToolTagAttributes(
    @SerialName("url") val url: String? = null,
    @SerialName("new_tab") val newTab: Boolean? = null,
    @SerialName("resource_link_id") val resourceLinkId: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("content_id") val contentId: Long? = null
)

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
    @SerialName("quiz_id") val quizId: Long? = null,
    @SerialName("is_quiz_assignment") val isQuizAssignment: Boolean? = null,
    @SerialName("is_quiz_lti_assignment") val isQuizLtiAssignment: Boolean? = null,
    @SerialName("external_tool_tag_attributes") val externalToolTagAttributes: ExternalToolTagAttributes? = null,
    @SerialName("url") val url: String? = null,
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
        get() = submissionTypes?.contains("online_quiz") == true ||
                submissionTypes?.contains("quiz_lti") == true ||
                isQuizAssignment == true ||
                isQuizLtiAssignment == true ||
                quizId != null ||
                externalToolTagAttributes?.url?.contains("quiz", ignoreCase = true) == true ||
                url?.contains("launch_type=assessment", ignoreCase = true) == true ||
                submission?.url?.contains("quiz-lti", ignoreCase = true) == true ||
                (submissionTypes?.contains("external_tool") == true && (
                    submission?.submissionType == "basic_lti_launch" &&
                    submission.url?.contains("quiz", ignoreCase = true) == true
                ))

    val supportsDirectSubmission: Boolean
        get() = !isQuiz && submissionTypes?.any { it in listOf("online_upload", "online_text_entry", "online_url", "media_recording") } == true

    fun isHardLocked(nowEpochMillis: Long = DateTimeUtil.nowEpochMillis()): Boolean {
        if (isLocked) return true
        val lockStr = lockAt ?: return false
        val comp = DateTimeUtil.parseIsoToComponents(lockStr) ?: return false
        return comp.epochMillis <= nowEpochMillis
    }

    fun isPastDue(nowEpochMillis: Long = DateTimeUtil.nowEpochMillis()): Boolean {
        val dueStr = dueAt ?: return false
        val comp = DateTimeUtil.parseIsoToComponents(dueStr) ?: return false
        return comp.epochMillis <= nowEpochMillis
    }

    fun hasRemainingAttempts(isSubmitted: Boolean = isCompleted): Boolean {
        val allowed = allowedAttempts
        if (allowed == null || allowed <= 0 || allowed == -1) return true
        val attemptsUsed = if (isSubmitted) (submission?.attempt ?: 1) else 0
        return attemptsUsed < allowed
    }

    fun canSubmit(nowEpochMillis: Long = DateTimeUtil.nowEpochMillis(), isSubmitted: Boolean = isCompleted): Boolean {
        if (!supportsDirectSubmission) return false
        if (isHardLocked(nowEpochMillis)) return false
        return hasRemainingAttempts(isSubmitted)
    }
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

