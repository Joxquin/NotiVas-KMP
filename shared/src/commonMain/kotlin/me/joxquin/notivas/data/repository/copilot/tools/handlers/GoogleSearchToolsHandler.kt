package me.joxquin.notivas.data.repository.copilot.tools.handlers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import me.joxquin.notivas.data.model.OpenRouterFunction
import me.joxquin.notivas.data.model.OpenRouterParameters
import me.joxquin.notivas.data.model.OpenRouterProperty
import me.joxquin.notivas.data.model.OpenRouterTool
import me.joxquin.notivas.data.repository.CopilotSource
import me.joxquin.notivas.data.repository.copilot.ToolExecutionResult
import me.joxquin.notivas.data.repository.copilot.tools.CopilotToolHandler

data class SearchResultItem(
    val title: String,
    val url: String,
    val snippet: String
)

/**
 * Manejador de la herramienta 'Grounding with Google Search' (google_search).
 * Permite al modelo buscar en la web en tiempo real para informes, tesis, artículos científicos y papers.
 */
class GoogleSearchToolsHandler(
    private val httpClient: HttpClient
) : CopilotToolHandler {

    override val supportedTools: List<OpenRouterTool> = listOf(
        OpenRouterTool(
            type = "function",
            function = OpenRouterFunction(
                name = "google_search",
                description = "Herramienta Grounding with Google Search. Realiza búsquedas en la web en tiempo real para encontrar artículos científicos, tesis, papers, libros, documentación técnica, conceptos o información externa para informes académicos.",
                parameters = OpenRouterParameters(
                    type = "object",
                    properties = mapOf(
                        "query" to OpenRouterProperty(
                            type = "string",
                            description = "Términos de búsqueda web precisos y optimizados (ej: 'arquitectura microservicios tesis pdf', 'articulos IEEE computacion cuantica 2024', 'teoria de grafos dijkstra informe')."
                        )
                    ),
                    required = listOf("query")
                )
            )
        )
    )

    override fun canHandle(toolName: String): Boolean =
        toolName == "google_search" || toolName == "web_search"

    override suspend fun execute(toolName: String, args: JsonObject, selectedCourseId: Long?): ToolExecutionResult {
        val query = args["query"]?.jsonPrimitive?.content?.trim() ?: ""
        if (query.isBlank()) {
            return ToolExecutionResult(
                resultJson = """{"error": "La consulta de búsqueda (query) está vacía."}"""
            )
        }

        return try {
            val results = searchWeb(query)
            if (results.isEmpty()) {
                ToolExecutionResult(
                    resultJson = buildJsonObject {
                        put("query", query)
                        put("total_results", 0)
                        putJsonArray("results") {}
                        put("message", "No se encontraron resultados específicos en la web para esta consulta.")
                    }.toString()
                )
            } else {
                val primarySource = results.firstOrNull()
                val source = primarySource?.let {
                    CopilotSource(
                        title = "Google Search: $query",
                        detail = "${results.size} fuentes y artículos encontrados",
                        url = it.url
                    )
                }

                val jsonResult = buildJsonObject {
                    put("query", query)
                    put("total_results", results.size)
                    putJsonArray("results") {
                        results.forEach { item ->
                            add(
                                buildJsonObject {
                                    put("title", item.title)
                                    put("url", item.url)
                                    put("snippet", item.snippet)
                                }
                            )
                        }
                    }
                }.toString()

                ToolExecutionResult(
                    resultJson = jsonResult,
                    source = source,
                    actionFeedback = "Búsqueda web completada: ${results.size} fuentes encontradas"
                )
            }
        } catch (e: Exception) {
            ToolExecutionResult(
                resultJson = """{"error": "Error al consultar la web: ${e.message?.replace("\"", "'")}"}"""
            )
        }
    }

    private suspend fun searchWeb(query: String): List<SearchResultItem> {
        val responseText: String = httpClient.submitForm(
            url = "https://lite.duckduckgo.com/lite/",
            formParameters = parameters {
                append("q", query)
            }
        ) {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
        }.body()

        return parseSearchResults(responseText)
    }

    private fun parseSearchResults(html: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val linkRegex = Regex("""<a[^>]*href=["']([^"']+)["'][^>]*class=['"]result-link['"][^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE)
        val snippetRegex = Regex("""<td[^>]*class=['"]result-snippet['"][^>]*>(.*?)</td>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

        val linkMatches = linkRegex.findAll(html).toList()
        val snippetMatches = snippetRegex.findAll(html).toList()

        for (i in 0 until minOf(linkMatches.size, 8)) {
            val linkMatch = linkMatches[i]
            val rawUrl = linkMatch.groupValues[1]
            val rawTitle = linkMatch.groupValues[2]
            val rawSnippet = snippetMatches.getOrNull(i)?.groupValues?.get(1) ?: ""

            val title = cleanHtml(rawTitle)
            val snippet = cleanHtml(rawSnippet)
            val cleanUrl = cleanUrl(rawUrl)

            if (cleanUrl.isNotBlank() && title.isNotBlank()) {
                results.add(SearchResultItem(title = title, url = cleanUrl, snippet = snippet))
            }
        }

        return results
    }

    private fun cleanUrl(url: String): String {
        return if (url.startsWith("//")) "https:$url" else url
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }
}
