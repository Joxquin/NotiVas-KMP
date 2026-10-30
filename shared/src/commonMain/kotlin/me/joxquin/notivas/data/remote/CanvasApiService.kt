package me.joxquin.notivas.data.remote

import kotlinx.coroutines.CancellationException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.http.parameters
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

class CanvasAuthExpiredException(message: String = "Canvas session expired") : Exception(message)

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
        val directUrl = "$cleanBase/$cleanPath"
        return me.joxquin.notivas.util.resolveApiUrl(directUrl)
    }

    private fun checkAuth(status: HttpStatusCode, bodyText: String) {
        if (status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden) {
            throw CanvasAuthExpiredException("Canvas session or token unauthorized (${status.value})")
        }
        val trimmed = bodyText.trimStart().lowercase()
        if (trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html")) {
            if (trimmed.contains("/login") || trimmed.contains("pseudonym_session") || trimmed.contains("log in")) {
                throw CanvasAuthExpiredException("Canvas session expired, redirected to login page")
            }
        }
    }

    private fun HttpRequestBuilder.applyAuth(credentialOrToken: String, csrfToken: String? = null) {
        header("User-Agent", "CanvasFastClient/1.0")
        if (credentialOrToken.startsWith("Cookie: ") || credentialOrToken.contains("canvas_session") || credentialOrToken.contains("_csrf_token")) {
            val cookies = credentialOrToken.removePrefix("Cookie: ")
            header("Cookie", cookies)
            header("X-Forwarded-Cookie", cookies)
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
                header("X-Forwarded-Cookie", credential.cookies)
                credential.csrfToken?.let { header("X-CSRF-Token", it) }
            }
        }
    }

    suspend fun generateStudentAccessToken(
        baseUrl: String,
        cookies: String,
        csrfToken: String?,
        purpose: String = "NotiVas App"
    ): String? {
        return try {
            val cleanCookies = cookies.removePrefix("Cookie: ").trim()
            val url = formatUrl(baseUrl, "api/v1/users/self/tokens")
            val response = client.submitForm(
                url = url,
                formParameters = parameters {
                    append("token[purpose]", purpose)
                }
            ) {
                header("User-Agent", "CanvasFastClient/1.0")
                header("Cookie", cleanCookies)
                header("X-Forwarded-Cookie", cleanCookies)
                csrfToken?.let { header("X-CSRF-Token", it) }
            }
            val text = response.body<String>()
            println("[CanvasApiService] generateStudentAccessToken status: ${response.status.value}, body: ${text.take(300)}")
            if (response.status.isSuccess() && text.trimStart().startsWith("{")) {
                val json = KtorHttpClientProvider.jsonConfig.parseToJsonElement(text) as? JsonObject
                val token = json?.get("token")?.let { (it as? JsonPrimitive)?.contentOrNull }
                    ?: json?.get("visible_token")?.let { (it as? JsonPrimitive)?.contentOrNull }
                if (!token.isNullOrBlank()) {
                    println("[CanvasApiService] Token de estudiante autogenerado con éxito")
                    return token
                }
            }
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            println("[CanvasApiService] No se pudo generar token de estudiante: ${e.message}")
            null
        }
    }

    suspend fun getCourses(
        baseUrl: String,
        token: String,
        state: String = "active",
        perPage: Int = 100
    ): List<Course> {
        val response = client.get(formatUrl(baseUrl, "api/v1/courses")) {
            applyAuth(token)
            parameter("enrollment_state", state)
            parameter("per_page", perPage)
        }
        val text = response.body<String>()
        checkAuth(response.status, text)
        return KtorHttpClientProvider.jsonConfig.decodeFromString<List<Course>>(text)
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
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
        fileIds: List<Long>? = null,
        comment: String? = null,
        csrfToken: String? = null
    ): SubmissionDetails {
        val formUrl = formatUrl(baseUrl, "api/v1/courses/$courseId/assignments/$assignmentId/submissions")
        return client.submitForm(
            url = formUrl,
            formParameters = io.ktor.http.parameters {
                append("submission[submission_type]", submissionType)
                url?.let { append("submission[url]", it) }
                bodyText?.let { append("submission[body]", it) }
                fileIds?.forEach { append("submission[file_ids][]", it.toString()) }
                comment?.takeIf { it.isNotBlank() }?.let { append("comment[text_comment]", it) }
            }
        ) {
            applyAuth(token, csrfToken)
        }.body()
    }

    suspend fun uploadSubmissionFile(
        baseUrl: String,
        token: String,
        courseId: Long,
        assignmentId: Long,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String = "application/octet-stream",
        csrfToken: String? = null
    ): Long? {
        val initUrl = formatUrl(baseUrl, "api/v1/courses/$courseId/assignments/$assignmentId/submissions/self/files")
        val initResponse = client.submitForm(
            url = initUrl,
            formParameters = io.ktor.http.parameters {
                append("name", fileName)
                append("size", fileBytes.size.toString())
                append("content_type", mimeType)
            }
        ) {
            applyAuth(token, csrfToken)
        }

        val initBodyText = initResponse.body<String>()
        val initJson = try {
            KtorHttpClientProvider.jsonConfig.parseToJsonElement(initBodyText) as? JsonObject
        } catch (_: Exception) { null } ?: return null

        val uploadUrl = (initJson["upload_url"] as? JsonPrimitive)?.contentOrNull ?: return null
        val uploadParams = initJson["upload_params"] as? JsonObject ?: JsonObject(emptyMap())

        val resolvedUploadUrl = me.joxquin.notivas.util.resolveApiUrl(uploadUrl)
        val uploadResponse = client.submitFormWithBinaryData(
            url = resolvedUploadUrl,
            formData = formData {
                uploadParams.forEach { (key, element) ->
                    val value = (element as? JsonPrimitive)?.contentOrNull
                    if (value != null) {
                        append(key, value)
                    }
                }
                append(
                    key = "file",
                    value = fileBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, mimeType)
                    }
                )
            }
        )

        val uploadResponseBody = uploadResponse.body<String>()
        val fileJson = try {
            KtorHttpClientProvider.jsonConfig.parseToJsonElement(uploadResponseBody) as? JsonObject
        } catch (_: Exception) { null }

        return (fileJson?.get("id") as? JsonPrimitive)?.contentOrNull?.toLongOrNull()
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
        checkAuth(response.status, text)
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
        val response = client.get(formatUrl(baseUrl, "api/v1/planner/items")) {
            applyAuth(token)
            parameter("start_date", startDate)
            parameter("per_page", perPage)
        }
        val text = response.body<String>()
        checkAuth(response.status, text)
        return KtorHttpClientProvider.jsonConfig.decodeFromString<List<PlannerItem>>(text)
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
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
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

                val resolvedDownloadUrl = me.joxquin.notivas.util.resolveApiUrl(currentUrl)
                val response = client.get(resolvedDownloadUrl) {
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                println("[CanvasApiService] Error descargando archivo desde $currentUrl: ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun getCourseAnnouncements(
        baseUrl: String,
        token: String,
        courseId: Long,
        perPage: Int = 10
    ): List<CanvasDiscussionTopic> {
        return try {
            client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/discussion_topics")) {
                applyAuth(token)
                parameter("only_announcements", true)
                parameter("per_page", perPage)
            }.body()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun getCourseUsers(
        baseUrl: String,
        token: String,
        courseId: Long,
        perPage: Int = 20
    ): List<UserProfile> {
        return try {
            client.get(formatUrl(baseUrl, "api/v1/courses/$courseId/users")) {
                applyAuth(token)
                parameter("enrollment_type[]", "student")
                parameter("per_page", perPage)
            }.body()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
