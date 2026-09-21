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
}
