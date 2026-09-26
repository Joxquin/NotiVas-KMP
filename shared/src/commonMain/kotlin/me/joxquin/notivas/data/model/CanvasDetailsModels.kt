package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CanvasAssignmentDetailResponse(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("points_possible") val pointsPossible: Double? = null,
    @SerialName("submission_types") val submissionTypes: List<String>? = null,
    @SerialName("rubric") val rubric: List<CanvasRubricCriterion>? = null,
    @SerialName("submission") val submission: CanvasSubmissionDetail? = null
)

@Serializable
data class CanvasSubmissionDetail(
    @SerialName("id") val id: Long? = null,
    @SerialName("workflow_state") val workflowState: String? = null,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null,
    @SerialName("score") val score: Double? = null,
    @SerialName("grade") val grade: String? = null,
    @SerialName("late") val late: Boolean? = null,
    @SerialName("missing") val missing: Boolean? = null,
    @SerialName("submission_comments") val submissionComments: List<CanvasSubmissionComment>? = null,
    @SerialName("rubric_assessment") val rubricAssessment: Map<String, CanvasRubricAssessmentItem>? = null
)

@Serializable
data class CanvasSubmissionComment(
    @SerialName("id") val id: Long? = null,
    @SerialName("author_id") val authorId: Long? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CanvasRubricAssessmentItem(
    @SerialName("points") val points: Double? = null,
    @SerialName("rating_id") val ratingId: String? = null,
    @SerialName("comments") val comments: String? = null
)

@Serializable
data class CanvasRubricCriterion(
    @SerialName("id") val id: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("long_description") val longDescription: String? = null,
    @SerialName("points") val points: Double? = null,
    @SerialName("ratings") val ratings: List<CanvasRubricRating>? = null
)

@Serializable
data class CanvasRubricRating(
    @SerialName("id") val id: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("points") val points: Double? = null
)

@Serializable
data class CanvasModule(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("position") val position: Int? = null,
    @SerialName("items_count") val itemsCount: Int? = null,
    @SerialName("items") val items: List<CanvasModuleItem>? = null
)

@Serializable
data class CanvasModuleItem(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("type") val type: String, // "File", "Page", "Discussion", "Assignment", "ExternalUrl", "Quiz"
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("page_url") val pageUrl: String? = null,
    @SerialName("content_id") val contentId: Long? = null
)

@Serializable
data class CanvasPageDetail(
    @SerialName("url") val url: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CanvasFileDetail(
    @SerialName("id") val id: Long? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("content-type") val contentType: String? = null,
    @SerialName("size") val size: Long? = null
)

@Serializable
data class CanvasDiscussionTopic(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("message") val message: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("posted_at") val postedAt: String? = null,
    @SerialName("last_reply_at") val lastReplyAt: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("discussion_type") val discussionType: String? = null,
    @SerialName("discussion_subentry_count") val discussionSubentryCount: Int? = null,
    @SerialName("unread_count") val unreadCount: Int? = null,
    @SerialName("assignment_id") val assignmentId: Long? = null,
    @SerialName("assignment") val assignment: CanvasDiscussionAssignment? = null,
    @SerialName("locked") val locked: Boolean? = null,
    @SerialName("lock_at") val lockAt: String? = null,
    @SerialName("author") val author: CanvasDiscussionAuthor? = null,
    // Contextual fields added during local processing
    val courseId: Long? = null,
    val courseName: String? = null
)

@Serializable
data class CanvasDiscussionAuthor(
    @SerialName("id") val id: Long? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_image_url") val avatarImageUrl: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null
)

@Serializable
data class CanvasDiscussionAssignment(
    @SerialName("id") val id: Long? = null,
    @SerialName("points_possible") val pointsPossible: Double? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("lock_at") val lockAt: String? = null,
    @SerialName("has_submitted_submissions") val isCompleted: Boolean? = null,
    @SerialName("submission") val submission: CanvasDiscussionSubmission? = null
)

@Serializable
data class CanvasDiscussionSubmission(
    @SerialName("score") val score: Double? = null,
    @SerialName("grade") val grade: String? = null,
    @SerialName("workflow_state") val workflowState: String? = null
)

@Serializable
data class CanvasDiscussionEntry(
    @SerialName("id") val id: Long,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("rating_sum") val ratingSum: Int? = null,
    @SerialName("user") val user: CanvasDiscussionAuthor? = null
)

@Serializable
data class CanvasDiscussionViewResponse(
    @SerialName("view") val view: List<CanvasDiscussionEntry> = emptyList(),
    @SerialName("unread_entries") val unreadEntries: List<Long> = emptyList()
)

