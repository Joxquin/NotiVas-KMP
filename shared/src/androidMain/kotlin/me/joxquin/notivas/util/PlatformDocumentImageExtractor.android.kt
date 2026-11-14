package me.joxquin.notivas.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

actual object PlatformDocumentImageExtractor {
    actual fun extractImages(bytes: ByteArray, filename: String, maxImages: Int): List<ExtractedDocumentImage> {
        val nameLower = filename.lowercase()
        return when {
            nameLower.endsWith(".pdf") -> extractPdfPages(bytes, maxImages)
            nameLower.endsWith(".docx") -> extractDocxImages(bytes, maxImages)
            nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".webp") -> {
                val mime = when {
                    nameLower.endsWith(".png") -> "image/png"
                    nameLower.endsWith(".webp") -> "image/webp"
                    else -> "image/jpeg"
                }
                listOf(ExtractedDocumentImage(Base64.encodeToString(bytes, Base64.NO_WRAP), mime, filename))
            }
            else -> emptyList()
        }
    }

    private fun extractPdfPages(bytes: ByteArray, maxImages: Int): List<ExtractedDocumentImage> {
        val result = mutableListOf<ExtractedDocumentImage>()
        var tempFile: File? = null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            tempFile = File.createTempFile("pdf_page_", ".tmp")
            tempFile.writeBytes(bytes)
            pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            if (pageCount == 0) return emptyList()

            val indices = if (pageCount <= maxImages) {
                (0 until pageCount).toList()
            } else {
                val step = (pageCount.toFloat() - 1f) / (maxImages - 1).coerceAtLeast(1)
                (0 until maxImages).map { (it * step).toInt().coerceIn(0, pageCount - 1) }.distinct()
            }

            for (p in indices) {
                if (result.size >= maxImages) break
                try {
                    val page = renderer.openPage(p)
                    val maxDim = 800f
                    val scale = (maxDim / page.width.coerceAtLeast(page.height)).coerceAtMost(1f)
                    val w = (page.width * scale).toInt().coerceAtLeast(1)
                    val h = (page.height * scale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    bitmap.recycle()

                    val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    result.add(ExtractedDocumentImage(b64, "image/jpeg", "Página ${p + 1} del documento"))
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
            try { tempFile?.delete() } catch (_: Exception) {}
        }
        return result
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
                val b64 = Base64.encodeToString(imgBytes, Base64.NO_WRAP)
                val fileName = name.substringAfterLast('/')
                result.add(ExtractedDocumentImage(b64, mime, "Figura: $fileName"))
            }
        } catch (_: Exception) {}
        return result
    }
}
