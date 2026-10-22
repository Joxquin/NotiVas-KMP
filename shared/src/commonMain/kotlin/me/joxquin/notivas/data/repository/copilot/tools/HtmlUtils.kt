package me.joxquin.notivas.data.repository.copilot.tools

object HtmlUtils {
    /**
     * Limpia etiquetas HTML y normaliza espacios y saltos de línea de forma multiplataforma pura,
     * preservando bloques de código y formato Markdown.
     */
    fun cleanHtml(html: String): String {
        val preBlocks = mutableListOf<String>()
        val inlineCodeBlocks = mutableListOf<String>()

        // 1. Proteger bloques <pre><code> para que sus etiquetas internas y espacios no sean alterados
        val preRegex = Regex("(?i)<pre[^>]*>(?:<code[^>]*>([\\s\\S]*?)</code>|([\\s\\S]*?))</pre>")
        var text = html.replace(preRegex) {
            val rawCode = it.groupValues[1].ifEmpty { it.groupValues[2] }
            val code = rawCode
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&amp;", "&")
                .trim()
            val token = "__HTML_UTILS_PRE_${preBlocks.size}__"
            preBlocks.add("\n```\n$code\n```\n")
            token
        }

        // 2. Proteger código inline <code>
        text = text.replace(Regex("(?i)<code[^>]*>([^<]+)</code>")) {
            val code = it.groupValues[1]
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&amp;", "&")
            val token = "__HTML_UTILS_CODE_${inlineCodeBlocks.size}__"
            inlineCodeBlocks.add("`$code`")
            token
        }

        // 3. Procesar saltos de línea, formato y limpiar etiquetas HTML restantes
        text = text
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)</li>"), "\n")
            .replace(Regex("(?i)<li>"), "• ")
            .replace(Regex("(?i)<strong>([^<]+)</strong>")) { "**" + it.groupValues[1] + "**" }
            .replace(Regex("(?i)<b>([^<]+)</b>")) { "**" + it.groupValues[1] + "**" }
            .replace(Regex("(?i)<em>([^<]+)</em>")) { "*" + it.groupValues[1] + "*" }
            .replace(Regex("(?i)<i>([^<]+)</i>")) { "*" + it.groupValues[1] + "*" }
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")

        // 4. Restaurar bloques protegidos intactos
        inlineCodeBlocks.forEachIndexed { index, code ->
            text = text.replace("__HTML_UTILS_CODE_${index}__", code)
        }
        preBlocks.forEachIndexed { index, pre ->
            text = text.replace("__HTML_UTILS_PRE_${index}__", pre)
        }

        return text.trim()
    }

    /**
     * Convierte texto plano con Markdown (saltos de línea, negritas, cursivas, código `code`, bloques ```code```)
     * a HTML enriquecido compatible con Canvas LMS.
     */
    fun textToHtml(plainText: String): String {
        val trimmed = plainText.trim()
        if (trimmed.isEmpty()) return ""

        val normalized = trimmed.replace("\r\n", "\n").replace("\r", "\n")

        // 1. Extraer bloques de código multilínea (fenced code blocks) y reemplazarlos por marcadores
        val codeBlocks = mutableListOf<String>()
        val codeBlockRegex = Regex("```(?:[a-zA-Z0-9_+#.-]+)?\\s*\\n?([\\s\\S]*?)```")

        val textWithTokens = codeBlockRegex.replace(normalized) { matchResult ->
            val codeContent = matchResult.groupValues[1].trimEnd('\n', '\r')
            val escapedCode = codeContent
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")

            val tokenIndex = codeBlocks.size
            codeBlocks.add("<pre><code>$escapedCode</code></pre>")
            "\n\n__CANVAS_CODE_BLOCK_${tokenIndex}__\n\n"
        }

        // 2. Dividir por párrafos (doble o más saltos de línea)
        val rawParagraphs = textWithTokens.split(Regex("\\n{2,}"))
        val tokenRegex = Regex("^__CANVAS_CODE_BLOCK_(\\d+)__$")

        return rawParagraphs.mapNotNull { rawParagraph ->
            val pTrim = rawParagraph.trim()
            if (pTrim.isEmpty()) return@mapNotNull null

            val tokenMatch = tokenRegex.find(pTrim)
            if (tokenMatch != null) {
                val index = tokenMatch.groupValues[1].toIntOrNull()
                if (index != null && index in codeBlocks.indices) {
                    return@mapNotNull codeBlocks[index]
                }
            }

            // Párrafo de texto normal: escapar entidades HTML básicas primero
            var text = pTrim
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")

            // Código en línea: `código`
            text = text.replace(Regex("`([^`]+)`")) { match ->
                "<code>${match.groupValues[1]}</code>"
            }

            // Negritas: **texto** o __texto__
            text = text.replace(Regex("\\*\\*(.+?)\\*\\*")) { "<strong>${it.groupValues[1]}</strong>" }
            text = text.replace(Regex("__(.+?)__")) { "<strong>${it.groupValues[1]}</strong>" }

            // Cursivas: *texto* o _texto_
            text = text.replace(Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")) { "<em>${it.groupValues[1]}</em>" }
            text = text.replace(Regex("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)")) { "<em>${it.groupValues[1]}</em>" }

            // Encabezados markdown (# H1, ## H2, ### H3)
            val headerMatch = Regex("^(#{1,3})\\s+(.+)$").find(text)
            if (headerMatch != null) {
                val level = headerMatch.groupValues[1].length
                val title = headerMatch.groupValues[2]
                return@mapNotNull "<h$level>$title</h$level>"
            }

            // Blockquote markdown (> cita)
            if (text.startsWith("&gt; ")) {
                val quote = text.removePrefix("&gt; ").trim()
                return@mapNotNull "<blockquote>$quote</blockquote>"
            }

            // Saltos de línea simples dentro del párrafo
            text = text.replace("\n", "<br/>")

            "<p>$text</p>"
        }.joinToString("")
    }
}
