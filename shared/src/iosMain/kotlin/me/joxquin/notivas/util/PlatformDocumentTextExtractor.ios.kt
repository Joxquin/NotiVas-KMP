package me.joxquin.notivas.util

actual object PlatformDocumentTextExtractor {
    actual fun extractText(bytes: ByteArray, filename: String): String {
        val nameLower = filename.lowercase()
        return when {
            nameLower.endsWith(".txt") || nameLower.endsWith(".md") ||
            nameLower.endsWith(".json") || nameLower.endsWith(".csv") ||
            nameLower.endsWith(".html") || nameLower.endsWith(".xml") ||
            nameLower.endsWith(".kt") || nameLower.endsWith(".java") ||
            nameLower.endsWith(".py") || nameLower.endsWith(".js") ||
            nameLower.endsWith(".ts") || nameLower.endsWith(".sql") -> {
                try {
                    bytes.decodeToString()
                } catch (_: Exception) {
                    "Archivo de texto: $filename (${bytes.size} bytes)."
                }
            }
            else -> "Archivo adjunto: $filename (${bytes.size} bytes)."
        }
    }
}
