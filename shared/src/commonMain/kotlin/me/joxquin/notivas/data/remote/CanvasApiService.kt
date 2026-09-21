package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasAssignmentDetailResponse
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.CanvasFileDetail
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.CanvasPageDetail
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SubmissionDetails
import me.joxquin.notivas.data.model.UserProfile

class CanvasApiService(
    private val client: HttpClient
) {
    private fun formatUrl(baseUrl: String, path: String): String {
        val trimmed = baseUrl.trim()
        val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
        val cleanBase = withScheme.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$cleanBase/$cleanPath"
    }

    suspend fun getCourses(
        baseUrl: String,
        token: String,
        state: String = "active",
        perPage: Int = 100
    ): List<Course> {
        return client.get(formatUrl(baseUrl, "api/v1/courses")) {
            header("Authorization", token)
            parameter("enrollment_state", state)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getUpcomingAssignments(
        baseUrl: String,
        token: String
    ): List<Assignment> {
        return client.get(formatUrl(baseUrl, "api/v1/users/self/upcoming_assignments")) {
            header("Authorization", token)
        }.body()
    }

    suspend fun getAssignmentsForCourse(
        baseUrl: String,
        token: String,
        courseId: Long,
        include: String? = "submission",
        orderBy: String? = "due_at",
        perPage: Int = 100
    ): List<Assignment> {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/assignments")) {
            header("Authorization", token)
            if (include != null) parameter("include[]", include)
            if (orderBy != null) parameter("order_by", orderBy)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getSubmissionForAssignment(
        baseUrl: String,
        token: String,
        courseId: Long,
        assignmentId: Long
    ): SubmissionDetails {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/assignments/$assignmentId/submissions/self")) {
            header("Authorization", token)
        }.body()
    }

    suspend fun getMissingSubmissions(
        baseUrl: String,
        token: String,
        perPage: Int = 100
    ): List<Assignment> {
        return client.get(formatUrl(baseUrl, "api/v1/users/self/missing_submissions")) {
            header("Authorization", token)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getProfile(
        baseUrl: String,
        token: String
    ): UserProfile {
        val url = formatUrl(baseUrl, "api/v1/users/self/profile")
        val response = client.get(url) {
            header("Authorization", token)
        }
        val text = response.body<String>()
        println("CanvasApiService: getProfile response from $url: $text")
        return KtorHttpClientProvider.jsonConfig.decodeFromString<UserProfile>(text)
    }

    suspend fun verifyToken(
        baseUrl: String,
        token: String
    ): UserProfile {
        return getProfile(baseUrl, token)
    }

    suspend fun getPlannerItems(
        baseUrl: String,
        token: String,
        startDate: String,
        perPage: Int = 100
    ): List<PlannerItem> {
        return client.get(formatUrl(baseUrl, "api/v1/planner/items")) {
            header("Authorization", token)
            parameter("start_date", startDate)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getAssignmentDetails(
        baseUrl: String,
        token: String,
        courseId: Long,
        assignmentId: Long,
        include: List<String> = listOf("rubric", "submission", "submission_comments", "rubric_assessment")
    ): CanvasAssignmentDetailResponse {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/assignments/$assignmentId")) {
            header("Authorization", token)
            include.forEach { inc ->
                parameter("include[]", inc)
            }
        }.body()
    }

    suspend fun getModulesWithItems(
        baseUrl: String,
        token: String,
        courseId: Long,
        include: List<String> = listOf("items"),
        perPage: Int = 50
    ): List<CanvasModule> {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/modules")) {
            header("Authorization", token)
            include.forEach { inc ->
                parameter("include[]", inc)
            }
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getPageDetails(
        baseUrl: String,
        token: String,
        courseId: Long,
        pageUrl: String
    ): CanvasPageDetail {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/pages/$pageUrl")) {
            header("Authorization", token)
        }.body()
    }

    suspend fun getFileDetails(
        baseUrl: String,
        token: String,
        courseId: Long,
        fileId: Long
    ): CanvasFileDetail {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/files/$fileId")) {
            header("Authorization", token)
        }.body()
    }

    suspend fun getDiscussionTopics(
        baseUrl: String,
        token: String,
        courseId: Long,
        perPage: Int = 50
    ): List<CanvasDiscussionTopic> {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics")) {
            header("Authorization", token)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getDiscussionTopic(
        baseUrl: String,
        token: String,
        courseId: Long,
        topicId: Long
    ): CanvasDiscussionTopic {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics/$topicId")) {
            header("Authorization", token)
        }.body()
    }
}
