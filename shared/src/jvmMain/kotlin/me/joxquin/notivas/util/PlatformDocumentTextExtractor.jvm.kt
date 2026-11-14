package me.joxquin.notivas.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

actual object PlatformDocumentTextExtractor {
    actual fun extractText(bytes: ByteArray, filename: String): String {
        val nameLower = filename.lowercase()
        return when {
            nameLower.endsWith(".docx") -> extractDocx(bytes, filename)
            nameLower.endsWith(".pdf") -> extractPdf(bytes, filename)
            nameLower.endsWith(".zip") -> extractZip(bytes, filename)
            nameLower.endsWith(".txt") || nameLower.endsWith(".md") ||
            nameLower.endsWith(".json") || nameLower.endsWith(".csv") ||
            nameLower.endsWith(".html") || nameLower.endsWith(".xml") ||
            nameLower.endsWith(".kt") || nameLower.endsWith(".java") ||
            nameLower.endsWith(".py") || nameLower.endsWith(".js") ||
            nameLower.endsWith(".ts") || nameLower.endsWith(".c") ||
            nameLower.endsWith(".cpp") || nameLower.endsWith(".cs") ||
            nameLower.endsWith(".sql") || nameLower.endsWith(".properties") -> {
                try {
                    bytes.decodeToString()
                } catch (_: Exception) {
                    "Archivo de texto: $filename (${bytes.size} bytes)."
                }
            }
            else -> "Archivo adjunto: $filename (${bytes.size} bytes)."
        }
    }

    private fun extractDocx(bytes: ByteArray, filename: String): String {
        return try {
            val bais = ByteArrayInputStream(bytes)
            val zis = ZipInputStream(bais)
            var entry = zis.nextEntry
            var docXml: String? = null
            var relsXml: String? = null

            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    docXml = zis.readBytes().decodeToString()
                } else if (entry.name == "word/_rels/document.xml.rels") {
                    relsXml = zis.readBytes().decodeToString()
                }
                if (docXml != null && relsXml != null) break
                entry = zis.nextEntry
            }
            zis.close()

            if (docXml.isNullOrBlank()) {
                return "Documento Word ($filename, ${bytes.size} bytes). No se encontraron párrafos de texto."
            }

            val relsMap = mutableMapOf<String, String>()
            if (!relsXml.isNullOrBlank()) {
                val relRegex = Regex("""Id=["']([^"']+)["'][^>]*Target=["']([^"']+)["']""")
                for (match in relRegex.findAll(relsXml)) {
                    val id = match.groupValues[1]
                    val target = match.groupValues[2]
                    relsMap[id] = target
                }
                val relRegexRev = Regex("""Target=["']([^"']+)["'][^>]*Id=["']([^"']+)["']""")
                for (match in relRegexRev.findAll(relsXml)) {
                    val target = match.groupValues[1]
                    val id = match.groupValues[2]
                    if (!relsMap.containsKey(id)) {
                        relsMap[id] = target
                    }
                }
            }

            val textRegex = Regex("""<w:t[^>]*>([^<]+)</w:t>""")
            val hyperlinkRegex = Regex("""<w:hyperlink\b([^>]*)>([\s\S]*?)</w:hyperlink>""")
            val rIdRegex = Regex("""r:id=["']([^"']+)["']""")

            val withHyperlinks = if (relsMap.isNotEmpty()) {
                hyperlinkRegex.replace(docXml) { hMatch ->
                    val attrs = hMatch.groupValues[1]
                    val body = hMatch.groupValues[2]
                    val rId = rIdRegex.find(attrs)?.groupValues?.get(1)
                    val targetUrl = rId?.let { relsMap[it] }
                    val linkText = textRegex.findAll(body).map { it.groupValues[1] }.joinToString("").trim()
                    if (!targetUrl.isNullOrBlank()) {
                        val cleanUrl = targetUrl.replace("&amp;", "&")
                        val display = if (linkText.isNotBlank() && !linkText.equals(cleanUrl, ignoreCase = true)) {
                            "$linkText ($cleanUrl)"
                        } else {
                            cleanUrl
                        }
                        val safeDisplay = display.replace("&", "&amp;").replace("<", "").replace(">", "")
                        "<w:t xml:space=\"preserve\"> $safeDisplay </w:t>"
                    } else if (linkText.isNotBlank()) {
                        "<w:t xml:space=\"preserve\"> $linkText </w:t>"
                    } else {
                        ""
                    }
                }
            } else {
                docXml
            }

            val normalizedXml = withHyperlinks.replace("<w:br/>", "\n").replace("<w:cr/>", "\n")
            val paragraphs = normalizedXml.split("</w:p>")
            val sb = StringBuilder()
            for (p in paragraphs) {
                val matches = textRegex.findAll(p).map { it.groupValues[1] }.toList()
                if (matches.isNotEmpty()) {
                    val paragraphText = matches.joinToString("")
                    if (paragraphText.isNotBlank()) {
                        sb.appendLine(paragraphText.trim())
                    }
                }
            }

            val result = sb.toString().trim()
            if (result.isNotBlank()) {
                result
            } else {
                "Documento Word ($filename, ${bytes.size} bytes). No se encontraron párrafos de texto."
            }
        } catch (_: Exception) {
            "Documento Word ($filename, ${bytes.size} bytes)."
        }
    }

    private val STREAM_BYTES = "stream".toByteArray(Charsets.US_ASCII)
    private val ENDSTREAM_BYTES = "endstream".toByteArray(Charsets.US_ASCII)
    private val BT_BYTES = "BT".toByteArray(Charsets.US_ASCII)
    private val IMAGE_BYTES = "/Image".toByteArray(Charsets.US_ASCII)

    private fun indexOfBytes(source: ByteArray, target: ByteArray, fromIndex: Int = 0): Int {
        if (target.isEmpty()) return fromIndex
        val max = source.size - target.size
        val first = target[0]
        var i = fromIndex
        while (i <= max) {
            while (i <= max && source[i] != first) {
                i++
            }
            if (i <= max) {
                var j = 1
                while (j < target.size && source[i + j] == target[j]) {
                    j++
                }
                if (j == target.size) return i
                i++
            }
        }
        return -1
    }

    private fun containsBytesInRange(source: ByteArray, target: ByteArray, fromIndex: Int, toIndex: Int): Boolean {
        if (target.isEmpty()) return true
        val max = (toIndex - target.size).coerceAtMost(source.size - target.size)
        val start = fromIndex.coerceAtLeast(0)
        val first = target[0]
        var i = start
        while (i <= max) {
            while (i <= max && source[i] != first) {
                i++
            }
            if (i <= max) {
                var j = 1
                while (j < target.size && source[i + j] == target[j]) {
                    j++
                }
                if (j == target.size) return true
                i++
            }
        }
        return false
    }

    private fun containsBytes(source: ByteArray, target: ByteArray): Boolean {
        return indexOfBytes(source, target, 0) != -1
    }

    private fun cleanPdfString(raw: String): String {
        return raw.replace("\\)", ")")
            .replace("\\(", "(")
            .replace("\\\\", "\\")
            .replace("\\r", "\r")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
    }

    private fun extractPdf(bytes: ByteArray, filename: String): String {
        return try {
            val textLines = mutableListOf<String>()
            val arrayTjRegex = Regex("""\[(.*?)\]\s*TJ""")
            val subParenRegex = Regex("""\(((?:\\.|[^)])*)\)""")
            val tjRegex = Regex("""\(((?:\\.|[^)])*)\)\s*(?:Tj|'|")""")

            // 1. Escanear streams comprimidos FlateDecode delimitados por stream/endstream
            var pos = 0
            val maxStreams = 80
            var streamsFound = 0
            while (pos < bytes.size - 10 && streamsFound < maxStreams) {
                val sIdx = indexOfBytes(bytes, STREAM_BYTES, pos)
                if (sIdx == -1) break

                // Si el stream es una imagen (/Subtype /Image), saltarlo inmediatamente
                val dictStart = (sIdx - 160).coerceAtLeast(0)
                if (containsBytesInRange(bytes, IMAGE_BYTES, dictStart, sIdx)) {
                    val nextEnd = indexOfBytes(bytes, ENDSTREAM_BYTES, sIdx + 6)
                    if (nextEnd == -1) break
                    pos = nextEnd + 9
                    continue
                }

                var start = sIdx + 6
                while (start < bytes.size && (bytes[start] == 10.toByte() || bytes[start] == 13.toByte())) {
                    start++
                }
                val eIdx = indexOfBytes(bytes, ENDSTREAM_BYTES, start)
                if (eIdx == -1) break

                val streamLen = eIdx - start
                if (streamLen > 2 && bytes[start] == 0x78.toByte()) {
                    try {
                        val inflater = Inflater()
                        inflater.setInput(bytes, start, streamLen)
                        val buffer = ByteArray(8192)
                        val baos = ByteArrayOutputStream()
                        var decompressedSize = 0
                        while (!inflater.finished() && !inflater.needsInput() && decompressedSize < 65536) {
                            val count = inflater.inflate(buffer)
                            if (count > 0) {
                                baos.write(buffer, 0, count)
                                decompressedSize += count
                            } else break
                        }
                        inflater.end()
                        val decompressedBytes = baos.toByteArray()
                        if (containsBytes(decompressedBytes, BT_BYTES)) {
                            streamsFound++
                            val decompressed = decompressedBytes.toString(Charsets.ISO_8859_1)
                            for (line in decompressed.lines()) {
                                if (line.contains("TJ")) {
                                    for (m in arrayTjRegex.findAll(line)) {
                                        val arrayContent = m.groupValues[1]
                                        val pieces = subParenRegex.findAll(arrayContent).map { cleanPdfString(it.groupValues[1]) }.toList()
                                        if (pieces.isNotEmpty()) {
                                            val combined = pieces.joinToString("").trim()
                                            if (isReadableAscii(combined)) {
                                                textLines.add(combined)
                                            }
                                        }
                                    }
                                } else if (line.contains("Tj") || line.endsWith("'") || line.endsWith("\"")) {
                                    for (m in tjRegex.findAll(line)) {
                                        val candidate = cleanPdfString(m.groupValues[1]).trim()
                                        if (isReadableAscii(candidate)) {
                                            textLines.add(candidate)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                pos = eIdx + 9
            }

            // 2. Si no hubo streams zlib con texto, escanear texto sin comprimir
            if (textLines.size < 5) {
                val raw = bytes.toString(Charsets.ISO_8859_1)
                for (line in raw.lines()) {
                    if (line.contains("TJ")) {
                        for (m in arrayTjRegex.findAll(line)) {
                            val arrayContent = m.groupValues[1]
                            val pieces = subParenRegex.findAll(arrayContent).map { cleanPdfString(it.groupValues[1]) }.toList()
                            if (pieces.isNotEmpty()) {
                                val combined = pieces.joinToString("").trim()
                                if (isReadableAscii(combined)) {
                                    textLines.add(combined)
                                }
                            }
                        }
                    } else if (line.contains("Tj")) {
                        for (m in tjRegex.findAll(line)) {
                            val candidate = cleanPdfString(m.groupValues[1]).trim()
                            if (isReadableAscii(candidate)) {
                                textLines.add(candidate)
                            }
                        }
                    }
                }
            }

            val rawPdfString = try { bytes.decodeToString() } catch (_: Exception) { "" }
            if (rawPdfString.isNotBlank()) {
                val uriRegex = Regex("""/URI\s*\(((?:\\.|[^)])+)\)""")
                val foundUris = uriRegex.findAll(rawPdfString)
                    .map { cleanPdfString(it.groupValues[1]).trim() }
                    .filter { it.startsWith("http://", ignoreCase = true) || it.startsWith("https://", ignoreCase = true) }
                    .distinct()
                    .toList()
                if (foundUris.isNotEmpty()) {
                    textLines.add("")
                    textLines.add("Enlaces detectados en el documento:")
                    for (u in foundUris) {
                        textLines.add("- $u")
                    }
                }
            }

            val filtered = textLines.filter { it.isNotBlank() && isReadableAscii(it) }.distinct().take(400)
            if (filtered.isNotEmpty()) {
                filtered.joinToString("\n")
            } else {
                "Documento PDF ($filename, ${bytes.size} bytes)."
            }
        } catch (_: Exception) {
            "Documento PDF ($filename, ${bytes.size} bytes)."
        }
    }

    private fun extractZip(bytes: ByteArray, filename: String): String {
        return try {
            val bais = ByteArrayInputStream(bytes)
            val zis = ZipInputStream(bais)
            var entry = zis.nextEntry
            val sb = StringBuilder()
            sb.appendLine("Estructura de archivos del proyecto comprimido ($filename):")
            var count = 0
            val codeSnippets = mutableListOf<String>()
            while (entry != null && count < 30) {
                if (!entry.isDirectory) {
                    val nameLow = entry.name.lowercase()
                    if (!nameLow.startsWith("__macosx") && !nameLow.contains(".git/") && !nameLow.endsWith(".class") && !nameLow.endsWith(".jar")) {
                        sb.appendLine("- ${entry.name} (${entry.size} bytes)")
                        count++
                        if (codeSnippets.size < 4 &&
                            (nameLow.endsWith(".java") || nameLow.endsWith(".kt") || nameLow.endsWith(".py") ||
                             nameLow.endsWith(".xml") || nameLow.endsWith("readme.md") || nameLow.endsWith(".properties"))
                        ) {
                            try {
                                val text = zis.readBytes().decodeToString().take(800)
                                if (text.isNotBlank()) {
                                    codeSnippets.add("--- Archivo: ${entry.name} ---\n$text")
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
                entry = zis.nextEntry
            }
            zis.close()
            if (codeSnippets.isNotEmpty()) {
                sb.appendLine("\nExtractos de código clave detectados:")
                for (snippet in codeSnippets) {
                    sb.appendLine(snippet)
                }
            }
            sb.toString().trim()
        } catch (_: Exception) {
            "Archivo comprimido ZIP ($filename, ${bytes.size} bytes)."
        }
    }

    private fun isReadableAscii(s: String): Boolean {
        if (s.isBlank()) return false
        if (s.contains("JFIF", ignoreCase = true) || s.contains("Exif", ignoreCase = true)) return false
        var readable = 0
        for (c in s) {
            if (c in ' '..'~' || c in 'á'..'ú' || c in 'Á'..'Ú' || c == 'ñ' || c == 'Ñ' || c == 'ü' || c == 'Ü' || c == '\n' || c == '\t' || c == '\r') {
                readable++
            }
        }
        return readable.toDouble() / s.length > 0.8
    }
}
