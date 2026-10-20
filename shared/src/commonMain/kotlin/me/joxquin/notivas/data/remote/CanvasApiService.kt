package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasAssignmentDetailResponse
import me.joxquin.notivas.data.model.CanvasAuthCredential
import me.joxquin.notivas.data.model.CanvasDiscussionEntry
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

    private fun HttpRequestBuilder.applyAuth(credentialOrToken: String, csrfToken: String? = null) {
        header("User-Agent", "CanvasFastClient/1.0")
        if (credentialOrToken.startsWith("Cookie: ") || credentialOrToken.contains("canvas_session") || credentialOrToken.contains("_csrf_token")) {
            val cookies = credentialOrToken.removePrefix("Cookie: ")
            header("Cookie", cookies)
            csrfToken?.let { header("X-CSRF-Token", it) }
            println("[CanvasApiService] applyAuth usando Cookie (longitud: ${cookies.length})")
        } else {
            val cleanToken = credentialOrToken.removePrefix("Bearer ").trim()
            header("Authorization", "Bearer $cleanToken")
            println("[CanvasApiService] applyAuth usando Bearer token (prefijo: ${cleanToken.take(10)}...)")
        }
    }

    fun HttpRequestBuilder.applyCredential(credential: CanvasAuthCredential) {
        when (credential) {
            is CanvasAuthCredential.BearerToken -> {
                val bearer = if (credential.token.startsWith("Bearer ")) credential.token else "Bearer ${credential.token}"
                header("Authorization", bearer)
            }
            is CanvasAuthCredential.SessionCookies -> {
                header("Cookie", credential.cookies)
                credential.csrfToken?.let { header("X-CSRF-Token", it) }
            }
        }
    }

    suspend fun getCourses(
        baseUrl: String,
        token: String,
        state: String = "active",
        perPage: Int = 100
    ): List<Course> {
        return client.get(formatUrl(baseUrl, "api/v1/courses")) {
            applyAuth(token)
            parameter("enrollment_state", state)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getDashboardCards(
        baseUrl: String,
        token: String
    ): List<me.joxquin.notivas.data.model.CanvasDashboardCard> {
        return try {
            val url = formatUrl(baseUrl, "api/v1/dashboard/dashboard_cards")
            val httpResponse = client.get(url) {
                applyAuth(token)
            }
            val text = httpResponse.body<String>()
            println("[CanvasApiService] getDashboardCards status: ${httpResponse.status}, body: ${text.take(300)}")
            if (text.trimStart().startsWith("[")) {
                val list = KtorHttpClientProvider.jsonConfig.decodeFromString<List<me.joxquin.notivas.data.model.CanvasDashboardCard>>(text)
                println("[CanvasApiService] getDashboardCards éxito: ${list.size} tarjetas")
                list
            } else {
                println("[CanvasApiService] getDashboardCards no devolvió array JSON: $text")
                emptyList()
            }
        } catch (e: Exception) {
            println("[CanvasApiService] getDashboardCards error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUpcomingAssignments(
        baseUrl: String,
        token: String
    ): List<Assignment> {
        return client.get(formatUrl(baseUrl, "api/v1/users/self/upcoming_assignments")) {
            applyAuth(token)
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
            applyAuth(token)
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
            applyAuth(token)
        }.body()
    }

    suspend fun submitAssignmentSubmission(
        baseUrl: String,
        token: String,
        courseId: Long,
        assignmentId: Long,
        submissionType: String = "online_url",
        url: String? = null,
        bodyText: String? = null,
        csrfToken: String? = null
    ): SubmissionDetails {
        val formUrl = formatUrl(baseUrl, "api/v1/courses/$courseId/assignments/$assignmentId/submissions")
        return client.submitForm(
            url = formUrl,
            formParameters = io.ktor.http.parameters {
                append("submission[submission_type]", submissionType)
                url?.let { append("submission[url]", it) }
                bodyText?.let { append("submission[body]", it) }
            }
        ) {
            applyAuth(token, csrfToken)
        }.body()
    }

    suspend fun getMissingSubmissions(
        baseUrl: String,
        token: String,
        perPage: Int = 100
    ): List<Assignment> {
        return client.get(formatUrl(baseUrl, "api/v1/users/self/missing_submissions")) {
            applyAuth(token)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getProfile(
        baseUrl: String,
        token: String
    ): UserProfile {
        val url = formatUrl(baseUrl, "api/v1/users/self/profile")
        val response = client.get(url) {
            applyAuth(token)
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
            applyAuth(token)
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
            applyAuth(token)
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
            applyAuth(token)
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
            applyAuth(token)
        }.body()
    }

    suspend fun getFileDetails(
        baseUrl: String,
        token: String,
        courseId: Long,
        fileId: Long
    ): CanvasFileDetail {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/files/$fileId")) {
            applyAuth(token)
        }.body()
    }

    suspend fun getDiscussionTopics(
        baseUrl: String,
        token: String,
        courseId: Long,
        perPage: Int = 50
    ): List<CanvasDiscussionTopic> {
        return client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics")) {
            applyAuth(token)
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
            applyAuth(token)
        }.body()
    }

    suspend fun getDiscussionEntries(
        baseUrl: String,
        token: String,
        courseId: Long,
        topicId: Long,
        perPage: Int = 30
    ): List<CanvasDiscussionEntry> {
        return try {
            client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics/$topicId/entries")) {
                applyAuth(token)
                parameter("per_page", perPage)
            }.body()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun postDiscussionEntry(
        baseUrl: String,
        token: String,
        courseId: Long,
        topicId: Long,
        message: String,
        csrfToken: String? = null
    ): CanvasDiscussionEntry {
        val formUrl = formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics/$topicId/entries")
        return client.submitForm(
            url = formUrl,
            formParameters = io.ktor.http.parameters {
                append("message", message)
            }
        ) {
            applyAuth(token, csrfToken)
        }.body()
    }

    suspend fun downloadFileBytes(
        downloadUrl: String,
        token: String
    ): ByteArray? {
        var currentUrl = downloadUrl.replace("&amp;", "&").trim()
        val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

        var attempts = 0
        while (attempts < 4) {
            attempts++
            try {
                val isExternalStorage = currentUrl.contains("inscloudgate.net") ||
                        currentUrl.contains("amazonaws.com") ||
                        currentUrl.contains("canvas-user-content.com") ||
                        currentUrl.contains("google.com")

                val response = client.get(currentUrl) {
                    if (!isExternalStorage) {
                        applyAuth(token)
                    }
                    headers[HttpHeaders.Accept] = "*/*"
                    headers.remove(HttpHeaders.ContentType)
                }

                val bytes: ByteArray = response.body()
                if (bytes.isEmpty()) {
                    return null
                }

                val contentType = response.headers[HttpHeaders.ContentType] ?: ""
                val isJson = contentType.contains("application/json", ignoreCase = true) ||
                        (bytes.size < 65536 && bytes.take(20).toByteArray().decodeToString().trimStart().startsWith("{"))

                if (isJson) {
                    val jsonStr = bytes.decodeToString()
                    println("[CanvasApiService] downloadFileBytes recibio JSON en vez de binario (${bytes.size} bytes). Parseando URL interna...")
                    val jsonElement = try {
                        jsonParser.parseToJsonElement(jsonStr)
                    } catch (_: Exception) {
                        null
                    }

                    val nextUrl = when (jsonElement) {
                        is JsonObject -> {
                            val attachment = jsonElement["attachment"] as? JsonObject
                            attachment?.get("url")?.let { (it as? JsonPrimitive)?.contentOrNull }
                                ?: attachment?.get("public_url")?.let { (it as? JsonPrimitive)?.contentOrNull }
                                ?: jsonElement["url"]?.let { (it as? JsonPrimitive)?.contentOrNull }
                                ?: jsonElement["public_url"]?.let { (it as? JsonPrimitive)?.contentOrNull }
                        }
                        else -> null
                    }

                    if (!nextUrl.isNullOrBlank() && nextUrl != currentUrl) {
                        println("[CanvasApiService] Redirigiendo descarga a URL extraida del JSON: $nextUrl")
                        currentUrl = nextUrl.replace("&amp;", "&")
                        continue
                    } else {
                        println("[CanvasApiService] No se pudo encontrar una URL de descarga valida dentro del JSON recibido.")
                        return null
                    }
                }

                println("[CanvasApiService] Descarga exitosa de ${bytes.size} bytes desde $currentUrl")
                return bytes
            } catch (e: Exception) {
                println("[CanvasApiService] Error descargando archivo desde $currentUrl: ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return null
    }
}
