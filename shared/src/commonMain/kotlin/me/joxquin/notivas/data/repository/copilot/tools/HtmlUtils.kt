package me.joxquin.notivas.data.repository.copilot.tools

object HtmlUtils {
    /**
     * Limpia etiquetas HTML y normaliza espacios y saltos de línea de forma multiplataforma pura.
     */
    fun cleanHtml(html: String): String {
        return html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)</li>"), "\n")
            .replace(Regex("(?i)<li>"), "• ")
            .replace(Regex("(?i)<pre><code>([\\s\\S]*?)</code></pre>")) { "```\n${it.groupValues[1]}\n```" }
            .replace(Regex("(?i)<code>([^<]+)</code>")) { "`" + it.groupValues[1] + "`" }
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
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    /**
     * Convierte texto plano con Markdown (saltos de línea, negritas, cursivas, código `code`) a HTML compatible con Canvas LMS.
     */
    fun textToHtml(plainText: String): String {
        val trimmed = plainText.trim()
        if (trimmed.isEmpty()) return ""
        // Si el texto ya parece ser HTML completo (contiene etiquetas de cierre comunes), se envía tal cual
        if (trimmed.contains(Regex("(?i)</(p|div|ul|ol|li|table|h[1-6])>"))) {
            return trimmed
        }

        // Divide por bloques de líneas vacías (párrafos)
        val paragraphs = trimmed.split(Regex("(\\r?\\n){2,}"))
        return paragraphs.joinToString("") { rawParagraph ->
            var text = rawParagraph
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")

            // Triple backtick code blocks: ```code```
            text = text.replace(Regex("```([\\s\\S]*?)```")) { match ->
                val code = match.groupValues[1]
                "<pre><code>$code</code></pre>"
            }

            // Single backtick inline code: `code`
            text = text.replace(Regex("`([^`]+)`")) { match ->
                val code = match.groupValues[1]
                "<code>$code</code>"
            }

            // Bold: **text** or __text__
            text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
            text = text.replace(Regex("__(.*?)__"), "<strong>$1</strong>")

            // Italic: *text* or _text_
            text = text.replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), "<em>$1</em>")
            text = text.replace(Regex("(?<!_)_(?!_)(.*?)(?<!_)_(?!_)"), "<em>$1</em>")

            // Saltos de línea simples dentro del párrafo
            text = text.replace(Regex("\\r?\\n"), "<br/>")

            "<p>$text</p>"
        }
    }
}
