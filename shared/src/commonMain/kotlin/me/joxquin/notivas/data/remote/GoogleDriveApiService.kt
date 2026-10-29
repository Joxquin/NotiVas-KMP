package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.joxquin.notivas.data.local.PreferencesManager

@Serializable
data class GoogleDriveFile(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("mimeType") val mimeType: String? = null,
    @SerialName("webViewLink") val webViewLink: String? = null,
    @SerialName("modifiedTime") val modifiedTime: String? = null
)

@Serializable
data class GoogleDriveFileListResponse(
    @SerialName("files") val files: List<GoogleDriveFile> = emptyList(),
    @SerialName("nextPageToken") val nextPageToken: String? = null
)

@Serializable
data class CreateGoogleDriveFileRequest(
    @SerialName("name") val name: String,
    @SerialName("mimeType") val mimeType: String? = null,
    @SerialName("parents") val parents: List<String>? = null
)

/**
 * Servicio API para Google Drive v3 compatible con Kotlin Multiplatform.
 * Permite buscar/crear carpetas jerárquicas (NotiVas / [Curso]), clonar archivos de plantilla DOCX
 * y crear documentos de Google Docs en blanco.
 */
class GoogleDriveApiService(
    private val httpClient: HttpClient = KtorHttpClientProvider.createClient(),
    private val authService: GoogleDriveAuthService = GoogleDriveAuthService(httpClient),
    private val preferencesManager: PreferencesManager? = null,
    private val tokenRefresher: (suspend () -> String?)? = null
) {
    companion object {
        const val DRIVE_FILES_API = "https://www.googleapis.com/drive/v3/files"
        const val DRIVE_UPLOAD_API = "https://www.googleapis.com/upload/drive/v3/files"
        const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
        const val MIME_TYPE_GDOC = "application/vnd.google-apps.document"
        const val MIME_TYPE_GSHEET = "application/vnd.google-apps.spreadsheet"
        const val MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }

    private suspend fun getValidAccessToken(token: String?, forceRefresh: Boolean = false): String? {
        if (!forceRefresh) {
            val currentToken = token ?: preferencesManager?.getGoogleDriveAccessToken()
            if (!currentToken.isNullOrBlank()) return currentToken
        }

        // 1. Intentar con tokenRefresher de plataforma (Android GoogleSignIn / SecureTokenVault)
        val refreshedPlatform = tokenRefresher?.invoke()
        if (!refreshedPlatform.isNullOrBlank()) {
            return refreshedPlatform
        }

        // 2. Intentar con authService KMP (refresh token de GoogleDriveAuthService)
        val refreshToken = preferencesManager?.getGoogleDriveRefreshToken() ?: return null
        val refreshed = authService.refreshAccessToken(refreshToken) ?: return null
        preferencesManager?.saveGoogleDriveTokens(
            accessToken = refreshed.accessToken,
            refreshToken = refreshed.refreshToken ?: refreshToken,
            userEmail = preferencesManager.getGoogleDriveEmail()
        )
        return refreshed.accessToken
    }

    suspend fun hasValidAuth(token: String? = null): Boolean {
        return getValidAccessToken(token) != null
    }

    suspend fun findFolder(folderName: String, parentId: String? = null, accessToken: String? = null): GoogleDriveFile? {
        var token = getValidAccessToken(accessToken) ?: return null
        val query = buildString {
            append("mimeType = '$MIME_TYPE_FOLDER' and name = '$folderName' and trashed = false")
            if (parentId != null) {
                append(" and '$parentId' in parents")
            }
        }
        for (attempt in 1..2) {
            try {
                val response = httpClient.get(DRIVE_FILES_API) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("q", query)
                    parameter("fields", "files(id, name, mimeType, webViewLink, modifiedTime)")
                    parameter("pageSize", 1)
                }
                if (response.status.value in 200..299) {
                    val listResponse: GoogleDriveFileListResponse = response.body()
                    return listResponse.files.firstOrNull { it.id.isNotBlank() }
                } else if (response.status.value == 401 && attempt == 1) {
                    println("GoogleDriveApiService: 401 en findFolder. Refrescando token...")
                    token = getValidAccessToken(null, forceRefresh = true) ?: return null
                    continue
                } else {
                    println("GoogleDriveApiService: findFolder error HTTP ${response.status.value}")
                    return null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun createFolder(folderName: String, parentId: String? = null, accessToken: String? = null): GoogleDriveFile? {
        var token = getValidAccessToken(accessToken) ?: return null
        val requestBody = CreateGoogleDriveFileRequest(
            name = folderName,
            mimeType = MIME_TYPE_FOLDER,
            parents = if (parentId != null) listOf(parentId) else null
        )
        for (attempt in 1..2) {
            try {
                val response = httpClient.post(DRIVE_FILES_API) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
                    setBody(requestBody)
                }
                if (response.status.value in 200..299) {
                    val file = response.body<GoogleDriveFile>()
                    return if (file.id.isNotBlank()) file else null
                } else if (response.status.value == 401 && attempt == 1) {
                    println("GoogleDriveApiService: 401 en createFolder. Refrescando token...")
                    token = getValidAccessToken(null, forceRefresh = true) ?: return null
                    continue
                } else {
                    println("GoogleDriveApiService: createFolder error HTTP ${response.status.value}")
                    return null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun getOrCreateCourseFolder(courseName: String, accessToken: String? = null): GoogleDriveFile? {
        val rootNotiVas = findFolder("NotiVas", null, accessToken)
            ?: createFolder("NotiVas", null, accessToken)
            ?: return null

        val safeCourseName = courseName.ifBlank { "General" }
        return findFolder(safeCourseName, rootNotiVas.id, accessToken)
            ?: createFolder(safeCourseName, rootNotiVas.id, accessToken)
    }

    suspend fun createBlankDocument(fileName: String, parentFolderId: String, accessToken: String? = null): GoogleDriveFile? {
        var token = getValidAccessToken(accessToken) ?: return null
        val requestBody = CreateGoogleDriveFileRequest(
            name = fileName,
            mimeType = MIME_TYPE_GDOC,
            parents = listOf(parentFolderId)
        )
        for (attempt in 1..2) {
            try {
                val response = httpClient.post(DRIVE_FILES_API) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
                    setBody(requestBody)
                }
                if (response.status.value in 200..299) {
                    val file = response.body<GoogleDriveFile>()
                    return if (file.id.isNotBlank()) file else null
                } else if (response.status.value == 401 && attempt == 1) {
                    println("GoogleDriveApiService: 401 en createBlankDocument. Refrescando token...")
                    token = getValidAccessToken(null, forceRefresh = true) ?: return null
                    continue
                } else {
                    println("GoogleDriveApiService: createBlankDocument error HTTP ${response.status.value}")
                    return null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun copyFile(
        fileId: String,
        newFileName: String,
        parentFolderId: String,
        accessToken: String? = null
    ): GoogleDriveFile? {
        var token = getValidAccessToken(accessToken) ?: return null
        val requestBody = CreateGoogleDriveFileRequest(
            name = newFileName,
            parents = listOf(parentFolderId)
        )
        for (attempt in 1..2) {
            try {
                val response = httpClient.post("$DRIVE_FILES_API/$fileId/copy") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    parameter("supportsAllDrives", true)
                    parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
                    setBody(requestBody)
                }
                val status = response.status.value
                if (status in 200..299) {
                    val file = response.body<GoogleDriveFile>()
                    return if (file.id.isNotBlank()) file else null
                } else if (status == 401 && attempt == 1) {
                    println("GoogleDriveApiService: 401 en copyFile. Refrescando token...")
                    token = getValidAccessToken(null, forceRefresh = true) ?: return null
                    continue
                } else {
                    val errorBody = try { response.body<String>() } catch (_: Exception) { "" }
                    println("GoogleDriveApiService: copyFile failed HTTP $status fileId=$fileId: $errorBody")
                    return null
                }
            } catch (e: Exception) {
                println("GoogleDriveApiService: copyFile exception: ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun getFileMetadata(fileId: String, accessToken: String? = null): GoogleDriveFile? {
        val token = getValidAccessToken(accessToken) ?: return null
        return try {
            httpClient.get("$DRIVE_FILES_API/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("supportsAllDrives", true)
                parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * UPDATE: Modifica el nombre de un archivo en Google Drive (HTTP PATCH)
     */
    suspend fun updateFileName(
        fileId: String,
        newName: String,
        accessToken: String? = null
    ): GoogleDriveFile? {
        val token = getValidAccessToken(accessToken) ?: return null
        return try {
            val response = httpClient.patch("$DRIVE_FILES_API/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                parameter("supportsAllDrives", true)
                parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
                setBody(mapOf("name" to newName))
            }
            if (response.status.value in 200..299) {
                response.body<GoogleDriveFile>()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * DELETE: Elimina definitivamente un archivo de Google Drive (HTTP DELETE)
     */
    suspend fun deleteFile(
        fileId: String,
        accessToken: String? = null
    ): Boolean {
        val token = getValidAccessToken(accessToken) ?: return false
        return try {
            val response = httpClient.delete("$DRIVE_FILES_API/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("supportsAllDrives", true)
            }
            response.status.value in 200..299 || response.status.value == 204
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extrae el ID de un archivo o documento de Google Drive a partir de una URL compartida/pública.
     * Soporta:
     * - https://docs.google.com/document/d/{ID}/edit
     * - https://docs.google.com/spreadsheets/d/{ID}/edit
     * - https://drive.google.com/file/d/{ID}/view
     * - https://drive.google.com/open?id={ID}
     */
    fun extractDriveFileId(url: String): String? {
        if (url.isBlank()) return null
        val docPattern = Regex("""/d/([a-zA-Z0-9_\-]+)""").find(url)?.groupValues?.get(1)
        if (!docPattern.isNullOrBlank()) return docPattern
        val idParamPattern = Regex("""[?&]id=([a-zA-Z0-9_\-]+)""").find(url)?.groupValues?.get(1)
        if (!idParamPattern.isNullOrBlank()) return idParamPattern
        return null
    }

    /**
     * Clona un archivo a partir de su enlace de Google Drive dentro de la carpeta especificada.
     * Si la API de Google Drive rechaza la copia directa (ej. el archivo es de otro dominio o requiere exportación),
     * intenta descargarlo públicamente mediante el endpoint /export?format=docx (o /export?format=xlsx)
     * y subirlo convertido a la cuenta vinculada del estudiante.
     */
    suspend fun cloneFromDriveUrlOrId(
        sourceUrlOrId: String,
        newFileName: String,
        parentFolderId: String,
        accessToken: String? = null
    ): GoogleDriveFile? {
        val fileId = extractDriveFileId(sourceUrlOrId) ?: sourceUrlOrId.trim()
        if (fileId.isBlank()) return null
        val token = getValidAccessToken(accessToken)

        // 1. Intentar copia nativa en Google Drive (con supportsAllDrives)
        val copied = copyFile(
            fileId = fileId,
            newFileName = newFileName,
            parentFolderId = parentFolderId,
            accessToken = token
        )
        if (copied != null) return copied

        val isSheet = sourceUrlOrId.contains("spreadsheets")
        val targetExportMime = if (isSheet) MIME_TYPE_XLSX else MIME_TYPE_DOCX

        // 2. Intentar exportación autenticada oficial de Google Drive API (files.export)
        if (token != null) {
            try {
                println("GoogleDriveApiService: intentando exportación autenticada oficial files.export para $fileId...")
                val apiExportResponse = httpClient.get("$DRIVE_FILES_API/$fileId/export") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("mimeType", targetExportMime)
                }
                if (apiExportResponse.status.value in 200..299) {
                    val bytes = apiExportResponse.body<ByteArray>()
                    if (bytes.isNotEmpty()) {
                        println("GoogleDriveApiService: descargados ${bytes.size} bytes vía files.export. Subiendo a Drive...")
                        val uploaded = uploadAndConvertFile(
                            fileName = newFileName,
                            parentFolderId = parentFolderId,
                            fileBytes = bytes,
                            targetMimeType = if (isSheet) MIME_TYPE_GSHEET else MIME_TYPE_GDOC,
                            sourceMimeType = targetExportMime,
                            accessToken = token
                        )
                        if (uploaded != null) return uploaded
                    }
                } else {
                    println("GoogleDriveApiService: files.export API falló con HTTP ${apiExportResponse.status.value}")
                }
            } catch (e: Exception) {
                println("GoogleDriveApiService: error en files.export: ${e.message}")
            }

            // 2.1 Si es un archivo binario en Drive (ej. Office subido con rtpof=true), descargar con alt=media
            try {
                println("GoogleDriveApiService: intentando descarga directa binaria files.get(alt=media) para $fileId...")
                val mediaResponse = httpClient.get("$DRIVE_FILES_API/$fileId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("alt", "media")
                    parameter("supportsAllDrives", true)
                }
                if (mediaResponse.status.value in 200..299) {
                    val bytes = mediaResponse.body<ByteArray>()
                    if (bytes.isNotEmpty()) {
                        println("GoogleDriveApiService: descargados ${bytes.size} bytes vía files.get(alt=media). Subiendo a Drive...")
                        val uploaded = uploadAndConvertFile(
                            fileName = newFileName,
                            parentFolderId = parentFolderId,
                            fileBytes = bytes,
                            targetMimeType = if (isSheet) MIME_TYPE_GSHEET else MIME_TYPE_GDOC,
                            sourceMimeType = targetExportMime,
                            accessToken = token
                        )
                        if (uploaded != null) return uploaded
                    }
                } else {
                    println("GoogleDriveApiService: files.get(alt=media) falló con HTTP ${mediaResponse.status.value}")
                }
            } catch (e: Exception) {
                println("GoogleDriveApiService: error en files.get(alt=media): ${e.message}")
            }
        }

        // 3. Fallback de descarga web (para documentos públicos o compartidos en web)
        val downloadUrls = listOfNotNull(
            if (isSheet) "https://docs.google.com/spreadsheets/d/$fileId/export?format=xlsx" else "https://docs.google.com/document/d/$fileId/export?format=docx",
            "https://drive.google.com/uc?export=download&id=$fileId"
        )

        for (webUrl in downloadUrls) {
            try {
                println("GoogleDriveApiService: intentando descarga desde $webUrl...")
                val exportResponse = httpClient.get(webUrl) {
                    if (token != null) {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                if (exportResponse.status.value in 200..299) {
                    val bytes = exportResponse.body<ByteArray>()
                    if (bytes.isNotEmpty()) {
                        println("GoogleDriveApiService: descargados ${bytes.size} bytes de plantilla. Subiendo a Drive con conversión...")
                        val uploaded = uploadAndConvertFile(
                            fileName = newFileName,
                            parentFolderId = parentFolderId,
                            fileBytes = bytes,
                            targetMimeType = if (isSheet) MIME_TYPE_GSHEET else MIME_TYPE_GDOC,
                            sourceMimeType = if (isSheet) MIME_TYPE_XLSX else MIME_TYPE_DOCX,
                            accessToken = token
                        )
                        if (uploaded != null) return uploaded
                    }
                } else {
                    println("GoogleDriveApiService: descarga web falló HTTP ${exportResponse.status.value} en $webUrl")
                }
            } catch (e: Exception) {
                println("GoogleDriveApiService: error en descarga web $webUrl: ${e.message}")
            }
        }

        return null
    }

    /**
     * Sube un archivo binario (ej. .docx o .xlsx de Canvas) a Google Drive y solicita su conversión
     * automática a Google Docs o Google Sheets nativo mediante multipart upload.
     */
    suspend fun uploadAndConvertFile(
        fileName: String,
        parentFolderId: String,
        fileBytes: ByteArray,
        targetMimeType: String = MIME_TYPE_GDOC,
        sourceMimeType: String = MIME_TYPE_DOCX,
        accessToken: String? = null
    ): GoogleDriveFile? {
        var token = getValidAccessToken(accessToken) ?: return null
        val boundary = "====NotiVasUploadBoundary${me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()}===="
        val metadataJson = """{"name":"$fileName","mimeType":"$targetMimeType","parents":["$parentFolderId"]}"""

        val boundaryBytes = "--$boundary\r\n".encodeToByteArray()
        val metaHeaderBytes = "Content-Type: application/json; charset=UTF-8\r\n\r\n".encodeToByteArray()
        val metaBodyBytes = "$metadataJson\r\n".encodeToByteArray()
        val fileHeaderBytes = "--$boundary\r\nContent-Type: $sourceMimeType\r\n\r\n".encodeToByteArray()
        val fileTrailerBytes = "\r\n--$boundary--\r\n".encodeToByteArray()

        val totalSize = boundaryBytes.size + metaHeaderBytes.size + metaBodyBytes.size +
                fileHeaderBytes.size + fileBytes.size + fileTrailerBytes.size
        val fullPayload = ByteArray(totalSize)

        var offset = 0
        fun appendBytes(src: ByteArray) {
            src.copyInto(fullPayload, destinationOffset = offset)
            offset += src.size
        }

        appendBytes(boundaryBytes)
        appendBytes(metaHeaderBytes)
        appendBytes(metaBodyBytes)
        appendBytes(fileHeaderBytes)
        appendBytes(fileBytes)
        appendBytes(fileTrailerBytes)

        for (attempt in 1..2) {
            try {
                val multipartContentType = ContentType.parse("multipart/related; boundary=$boundary")
                val response = httpClient.post("$DRIVE_UPLOAD_API?uploadType=multipart") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    parameter("fields", "id, name, mimeType, webViewLink, modifiedTime")
                    setBody(io.ktor.http.content.ByteArrayContent(fullPayload, multipartContentType))
                }
                val status = response.status.value
                if (status in 200..299) {
                    val file = response.body<GoogleDriveFile>()
                    return if (file.id.isNotBlank()) file else null
                } else if (status == 401 && attempt == 1) {
                    println("GoogleDriveApiService: 401 en uploadAndConvertFile. Refrescando token...")
                    token = getValidAccessToken(null, forceRefresh = true) ?: return null
                    continue
                } else {
                    val errorBody = try { response.body<String>() } catch (_: Exception) { "" }
                    println("GoogleDriveApiService: uploadAndConvertFile failed with HTTP $status: $errorBody")
                    return null
                }
            } catch (e: Exception) {
                println("GoogleDriveApiService: uploadAndConvertFile exception: ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return null
    }
}
