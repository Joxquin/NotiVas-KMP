package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseInfo(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("name") val name: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("assets") val assets: List<GitHubReleaseAsset> = emptyList()
)

@Serializable
data class GitHubReleaseAsset(
    @SerialName("name") val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = ""
)

@Serializable
data class DirectUpdateJson(
    @SerialName("version") val version: String = "",
    @SerialName("versionCode") val versionCode: Int = 1,
    @SerialName("title") val title: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("downloadUrl") val downloadUrl: String = ""
)

data class UpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val releaseUrl: String = ""
)
