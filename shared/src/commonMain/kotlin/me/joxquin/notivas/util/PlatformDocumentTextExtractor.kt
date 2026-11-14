package me.joxquin.notivas.util

expect object PlatformDocumentTextExtractor {
    fun extractText(bytes: ByteArray, filename: String): String
}
