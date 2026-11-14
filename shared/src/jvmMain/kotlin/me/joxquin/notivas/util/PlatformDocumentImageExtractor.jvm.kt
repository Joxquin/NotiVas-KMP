package me.joxquin.notivas.util

import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.ZipInputStream

actual object PlatformDocumentImageExtractor {
    actual fun extractImages(bytes: ByteArray, filename: String, maxImages: Int): List<ExtractedDocumentImage> {
        val nameLower = filename.lowercase()
        return when {
            nameLower.endsWith(".docx") -> extractDocxImages(bytes, maxImages)
            nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".webp") -> {
                val mime = when {
                    nameLower.endsWith(".png") -> "image/png"
                    nameLower.endsWith(".webp") -> "image/webp"
                    else -> "image/jpeg"
                }
                listOf(ExtractedDocumentImage(Base64.getEncoder().encodeToString(bytes), mime, filename))
            }
            else -> emptyList()
        }
    }

    private fun extractDocxImages(bytes: ByteArray, maxImages: Int): List<ExtractedDocumentImage> {
        val result = mutableListOf<ExtractedDocumentImage>()
        try {
            val bais = ByteArrayInputStream(bytes)
            val zis = ZipInputStream(bais)
            var entry = zis.nextEntry
            val allImages = mutableListOf<Pair<String, ByteArray>>()
            while (entry != null) {
                val nameLower = entry.name.lowercase()
                if (nameLower.startsWith("word/media/") && (nameLower.endsWith(".png") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".jpg"))) {
                    val imgBytes = zis.readBytes()
                    if (imgBytes.size in 15_000..3_000_000) {
                        allImages.add(entry.name to imgBytes)
                    }
                }
                entry = zis.nextEntry
            }
            zis.close()

            if (allImages.isEmpty()) return emptyList()

            val numRegex = Regex("""\d+""")
            allImages.sortBy { (name, _) ->
                numRegex.find(name)?.value?.toIntOrNull() ?: 0
            }

            val selectedImages = if (allImages.size <= maxImages) {
                allImages
            } else {
                val step = (allImages.size.toFloat() - 1f) / (maxImages - 1).coerceAtLeast(1)
                (0 until maxImages).map { (it * step).toInt().coerceIn(0, allImages.size - 1) }.distinct().map { allImages[it] }
            }

            for ((name, imgBytes) in selectedImages) {
                val nameLower = name.lowercase()
                val mime = if (nameLower.endsWith(".png")) "image/png" else "image/jpeg"
                val b64 = Base64.getEncoder().encodeToString(imgBytes)
                val fileName = name.substringAfterLast('/')
                result.add(ExtractedDocumentImage(b64, mime, "Figura: $fileName"))
            }
        } catch (_: Exception) {}
        return result
    }
}
