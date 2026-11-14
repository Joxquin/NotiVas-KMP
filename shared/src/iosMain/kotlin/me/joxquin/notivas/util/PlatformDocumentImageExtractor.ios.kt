package me.joxquin.notivas.util

actual object PlatformDocumentImageExtractor {
    actual fun extractImages(bytes: ByteArray, filename: String, maxImages: Int): List<ExtractedDocumentImage> {
        return emptyList()
    }
}
