package me.joxquin.notivas.data.repository.copilot.tools

object HtmlUtils {
    /**
     * Limpia etiquetas HTML y normaliza espacios y saltos de línea de forma multiplataforma pura,
     * preservando bloques de código, tablas HTML (convertidas a tablas Markdown estándar)
     * y formato Markdown.
     */
    fun cleanHtml(html: String): String {
        val preBlocks = mutableListOf<String>()
        val inlineCodeBlocks = mutableListOf<String>()
        val tableBlocks = mutableListOf<String>()

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

        // 2. Extraer y convertir tablas HTML <table>...</table> a tablas Markdown
        val tableRegex = Regex("(?i)<table[^>]*>([\\s\\S]*?)</table>")
        text = text.replace(tableRegex) { match ->
            val tableHtml = match.groupValues[1]
            val mdTable = htmlTableToMarkdown(tableHtml)
            val token = "__HTML_UTILS_TABLE_${tableBlocks.size}__"
            tableBlocks.add("\n\n$mdTable\n\n")
            token
        }

        // 3. Proteger código inline <code>
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

        // 4. Procesar saltos de línea, formato y limpiar etiquetas HTML restantes
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

        // 5. Restaurar bloques protegidos intactos
        inlineCodeBlocks.forEachIndexed { index, code ->
            text = text.replace("__HTML_UTILS_CODE_${index}__", code)
        }
        tableBlocks.forEachIndexed { index, tbl ->
            text = text.replace("__HTML_UTILS_TABLE_${index}__", tbl)
        }
        preBlocks.forEachIndexed { index, pre ->
            text = text.replace("__HTML_UTILS_PRE_${index}__", pre)
        }

        return text.trim()
    }

    private fun htmlTableToMarkdown(innerHtml: String): String {
        val rowRegex = Regex("(?i)<tr[^>]*>([\\s\\S]*?)</tr>")
        val cellRegex = Regex("(?i)<(td|th)[^>]*>([\\s\\S]*?)</\\1>")

        val allRows = mutableListOf<List<String>>()
        var hasExplicitHeader = false

        for (rowMatch in rowRegex.findAll(innerHtml)) {
            val rowContent = rowMatch.groupValues[1]
            val cells = mutableListOf<String>()
            var isHeaderRow = false
            for (cellMatch in cellRegex.findAll(rowContent)) {
                val tag = cellMatch.groupValues[1].lowercase()
                if (tag == "th") isHeaderRow = true
                val rawCell = cellMatch.groupValues[2]
                // Limpiar etiquetas internas de la celda conservando texto plano
                val cleanCell = rawCell
                    .replace(Regex("(?i)<br\\s*/?>"), " ")
                    .replace(Regex("<[^>]*>"), " ")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .replace("&#x27;", "'")
                    .replace("|", "\\|")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                cells.add(cleanCell)
            }
            if (cells.isNotEmpty()) {
                if (isHeaderRow) hasExplicitHeader = true
                allRows.add(cells)
            }
        }

        if (allRows.isEmpty()) return ""

        val maxCols = allRows.maxOf { it.size }
        if (maxCols == 0) return ""

        // Normalizar filas para que todas tengan el mismo número de columnas
        val normalizedRows = allRows.map { row ->
            if (row.size < maxCols) row + List(maxCols - row.size) { "" } else row
        }

        val headerRow = normalizedRows.first()
        val dataRows = if (normalizedRows.size > 1) normalizedRows.drop(1) else emptyList()

        val sb = StringBuilder()
        sb.append("| ").append(headerRow.joinToString(" | ")).append(" |\n")
        sb.append("|").append(" :--- |".repeat(maxCols)).append("\n")

        for (row in dataRows) {
            sb.append("| ").append(row.joinToString(" | ")).append(" |\n")
        }

        return sb.toString().trimEnd('\n')
    }

    /**
     * Convierte texto plano con Markdown (saltos de línea, negritas, cursivas, código `code`,
     * bloques ```code``` y tablas Markdown) a HTML enriquecido compatible con Canvas LMS.
     */
    fun textToHtml(plainText: String): String {
        val trimmed = plainText.trim()
        if (trimmed.isEmpty()) return ""

        val normalized = trimmed.replace("\r\n", "\n").replace("\r", "\n")

        // 1. Extraer bloques de código multilínea (fenced code blocks) y reemplazarlos por marcadores
        val codeBlocks = mutableListOf<String>()
        val codeBlockRegex = Regex("```(?:[a-zA-Z0-9_+#.-]+)?\\s*\\n?([\\s\\S]*?)```")

        val textWithCodeTokens = codeBlockRegex.replace(normalized) { matchResult ->
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

        // 2. Extraer tablas Markdown y reemplazarlas por marcadores HTML de tabla Canvas LMS
        val tableBlocks = mutableListOf<String>()
        val lines = textWithCodeTokens.lines()
        val processedLines = mutableListOf<String>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val lineTrim = line.trim()

            if (lineTrim.startsWith("|") && lineTrim.endsWith("|") && i + 1 < lines.size) {
                val nextTrim = lines[i + 1].trim()
                if (isMarkdownTableSeparator(nextTrim)) {
                    val headerRow = parseMarkdownTableRow(lineTrim)
                    i += 2 // Saltar encabezado y separador
                    val rows = mutableListOf<List<String>>()
                    while (i < lines.size) {
                        val rowLine = lines[i].trim()
                        if (rowLine.startsWith("|") && rowLine.endsWith("|")) {
                            rows.add(parseMarkdownTableRow(rowLine))
                            i++
                        } else {
                            break
                        }
                    }

                    val htmlTable = buildCanvasHtmlTable(headerRow, rows)
                    val tokenIndex = tableBlocks.size
                    tableBlocks.add(htmlTable)
                    processedLines.add("\n\n__CANVAS_TABLE_BLOCK_${tokenIndex}__\n\n")
                    continue
                }
            }

            processedLines.add(line)
            i++
        }

        val textWithAllTokens = processedLines.joinToString("\n")

        // 3. Dividir por párrafos (doble o más saltos de línea)
        val rawParagraphs = textWithAllTokens.split(Regex("\\n{2,}"))
        val codeTokenRegex = Regex("^__CANVAS_CODE_BLOCK_(\\d+)__$")
        val tableTokenRegex = Regex("^__CANVAS_TABLE_BLOCK_(\\d+)__$")

        return rawParagraphs.mapNotNull { rawParagraph ->
            val pTrim = rawParagraph.trim()
            if (pTrim.isEmpty()) return@mapNotNull null

            val codeMatch = codeTokenRegex.find(pTrim)
            if (codeMatch != null) {
                val index = codeMatch.groupValues[1].toIntOrNull()
                if (index != null && index in codeBlocks.indices) {
                    return@mapNotNull codeBlocks[index]
                }
            }

            val tableMatch = tableTokenRegex.find(pTrim)
            if (tableMatch != null) {
                val index = tableMatch.groupValues[1].toIntOrNull()
                if (index != null && index in tableBlocks.indices) {
                    return@mapNotNull tableBlocks[index]
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

    private fun isMarkdownTableSeparator(line: String): Boolean {
        if (!line.startsWith("|") || !line.endsWith("|")) return false
        val inner = line.removeSurrounding("|")
        val cells = inner.split("|")
        if (cells.isEmpty()) return false
        return cells.all { cell ->
            cell.trim().matches(Regex("^:?-+:?$"))
        }
    }

    private fun parseMarkdownTableRow(line: String): List<String> {
        val inner = line.removeSurrounding("|")
        return inner.split("|").map { it.trim() }
    }

    private fun buildCanvasHtmlTable(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("<table border=\"1\" style=\"border-collapse: collapse; width: 100%; margin: 12px 0;\">")
        sb.append("<thead><tr style=\"background-color: #f2f2f2;\">")
        for (h in headers) {
            val escH = h.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
            sb.append("<th style=\"padding: 8px; border: 1px solid #ddd; text-align: left;\">$escH</th>")
        }
        sb.append("</tr></thead>")
        sb.append("<tbody>")
        for (row in rows) {
            sb.append("<tr>")
            for (colIndex in 0 until headers.size) {
                val cell = row.getOrNull(colIndex) ?: ""
                val escCell = cell.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                sb.append("<td style=\"padding: 8px; border: 1px solid #ddd;\">$escCell</td>")
            }
            sb.append("</tr>")
        }
        sb.append("</tbody></table>")
        return sb.toString()
    }
}
