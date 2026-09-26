package me.joxquin.notivas.data.repository

import me.joxquin.notivas.data.model.GitHubReleaseAsset
import me.joxquin.notivas.data.model.UpdateInfo
import me.joxquin.notivas.data.remote.UpdateApiService
import me.joxquin.notivas.util.AppVersion
import me.joxquin.notivas.util.DeviceAbi

class UpdateRepository(
    private val apiService: UpdateApiService = UpdateApiService(),
    private val currentVersion: String = AppVersion.get().versionName
) {
    suspend fun checkForUpdates(): UpdateInfo {
        // Try fetching from GitHub Releases API first
        val githubResult = apiService.getLatestGitHubRelease()
        if (githubResult.isSuccess) {
            val release = githubResult.getOrThrow()
            val latestTag = release.tagName.trimStart('v', 'V')
            if (isVersionNewer(latestTag, currentVersion)) {
                val apkAssets = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
                val targetAsset = selectBestApkAsset(apkAssets) ?: release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }

                return UpdateInfo(
                    isUpdateAvailable = true,
                    latestVersion = latestTag,
                    releaseTitle = release.name ?: "NotiVas v$latestTag",
                    releaseNotes = release.body ?: "Nueva versión disponible en GitHub.",
                    downloadUrl = targetAsset?.downloadUrl ?: release.htmlUrl,
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

    private fun selectBestApkAsset(assets: List<GitHubReleaseAsset>): GitHubReleaseAsset? {
        if (assets.isEmpty()) return null
        val deviceAbi = DeviceAbi.getPreferredAbi()?.lowercase()

        if (!deviceAbi.isNullOrBlank()) {
            // 1. Check for exact ABI match in asset filename (e.g., "arm64-v8a", "armeabi-v7a", "x86_64")
            val abiMatch = assets.firstOrNull { asset ->
                val name = asset.name.lowercase()
                name.contains(deviceAbi) || (deviceAbi == "arm64-v8a" && (name.contains("arm64") || name.contains("v8a")))
            }
            if (abiMatch != null) return abiMatch
        }

        // 2. Check for universal APK fallback
        val universalMatch = assets.firstOrNull { it.name.contains("universal", ignoreCase = true) }
        if (universalMatch != null) return universalMatch

        // 3. Default to first APK
        return assets.firstOrNull()
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
