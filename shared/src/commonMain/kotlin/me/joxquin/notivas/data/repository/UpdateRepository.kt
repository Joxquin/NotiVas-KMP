package me.joxquin.notivas.data.repository

import me.joxquin.notivas.data.model.UpdateInfo
import me.joxquin.notivas.data.remote.UpdateApiService

class UpdateRepository(
    private val apiService: UpdateApiService = UpdateApiService(),
    private val currentVersion: String = "1.0.0"
) {
    suspend fun checkForUpdates(): UpdateInfo {
        // Try fetching from GitHub Releases API first
        val githubResult = apiService.getLatestGitHubRelease()
        if (githubResult.isSuccess) {
            val release = githubResult.getOrThrow()
            val latestTag = release.tagName.trimStart('v', 'V')
            if (isVersionNewer(latestTag, currentVersion)) {
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                return UpdateInfo(
                    isUpdateAvailable = true,
                    latestVersion = latestTag,
                    releaseTitle = release.name ?: "NotiVas v$latestTag",
                    releaseNotes = release.body ?: "Nueva versión disponible en GitHub.",
                    downloadUrl = apkAsset?.downloadUrl ?: release.htmlUrl,
                    releaseUrl = release.htmlUrl
                )
            }
        }

        // Fallback to checking update.json in the repo
        val directResult = apiService.getDirectUpdateJson()
        if (directResult.isSuccess) {
            val json = directResult.getOrThrow()
            val latestVersion = json.version.trimStart('v', 'V')
            if (isVersionNewer(latestVersion, currentVersion)) {
                return UpdateInfo(
                    isUpdateAvailable = true,
                    latestVersion = latestVersion,
                    releaseTitle = json.title ?: "NotiVas v$latestVersion",
                    releaseNotes = json.notes ?: "Nueva actualización disponible.",
                    downloadUrl = json.downloadUrl.ifEmpty { "https://github.com/Joxquin/NotiVas-KMP/releases/latest" },
                    releaseUrl = "https://github.com/Joxquin/NotiVas-KMP/releases/latest"
                )
            }
        }

        return UpdateInfo(isUpdateAvailable = false)
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }
            
            val maxLength = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLength) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {
            return latest != current
        }
        return false
    }
}
