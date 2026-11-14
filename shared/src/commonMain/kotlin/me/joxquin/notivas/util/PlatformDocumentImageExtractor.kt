package me.joxquin.notivas.util

data class ExtractedDocumentImage(
    val base64Data: String,
    val mimeType: String = "image/jpeg",
    val label: String = ""
)

expect object PlatformDocumentImageExtractor {
    fun extractImages(bytes: ByteArray, filename: String, maxImages: Int = 4): List<ExtractedDocumentImage>
}
