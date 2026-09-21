package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import me.joxquin.notivas.data.model.DirectUpdateJson
import me.joxquin.notivas.data.model.GitHubReleaseInfo

class UpdateApiService(
    private val client: HttpClient = KtorHttpClientProvider.createClient(),
    private val owner: String = "Joxquin",
    private val repo: String = "NotiVas-KMP"
) {
    suspend fun getLatestGitHubRelease(): Result<GitHubReleaseInfo> = runCatching {
        client.get("https://api.github.com/repos/$owner/$repo/releases/latest") {
            header("User-Agent", "NotiVas-App")
            header("Accept", "application/vnd.github+json")
        }.body()
    }

    suspend fun getDirectUpdateJson(): Result<DirectUpdateJson> = runCatching {
        client.get("https://raw.githubusercontent.com/$owner/$repo/main/update.json") {
            header("User-Agent", "NotiVas-App")
        }.body()
    }
}
