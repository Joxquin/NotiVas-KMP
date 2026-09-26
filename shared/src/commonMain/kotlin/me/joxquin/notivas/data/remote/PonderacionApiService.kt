package me.joxquin.notivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.Serializable
import me.joxquin.notivas.domain.template.CourseEvaluationTemplate
import me.joxquin.notivas.domain.template.EvaluationTemplatesCatalog
import me.joxquin.notivas.domain.template.GlobalGroupsExportFile

@Serializable
data class RemoteTemplateCatalogItem(
    val id: String,
    val name: String,
    val institution: String,
    val description: String,
    val file: String
)

@Serializable
data class RemoteTemplateCatalogIndex(
    val version: Int = 1,
    val templates: List<RemoteTemplateCatalogItem> = emptyList()
)

class PonderacionApiService(
    private val client: HttpClient = KtorHttpClientProvider.createClient(),
    private val owner: String = "Joxquin",
    private val repo: String = "NotiVas-KMP",
    private val branch: String = "main"
) {
    private val baseUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/ponderacion"

    suspend fun getRemoteTemplates(): Result<List<CourseEvaluationTemplate>> = runCatching {
        val indexUrl = "$baseUrl/index.json"
        val index = client.get(indexUrl) {
            header("User-Agent", "NotiVas-App")
        }.body<RemoteTemplateCatalogIndex>()

        val remoteList = mutableListOf<CourseEvaluationTemplate>()
        for (item in index.templates) {
            try {
                val fileUrl = "$baseUrl/${item.file}"
                val exportFile = client.get(fileUrl) {
                    header("User-Agent", "NotiVas-App")
                }.body<GlobalGroupsExportFile>()

                val template = exportFile.defaultTemplate ?: exportFile.courses.firstOrNull()?.let { exp ->
                    CourseEvaluationTemplate(
                        id = item.id,
                        name = item.name,
                        institution = item.institution,
                        description = item.description,
                        totalWeeks = exp.totalWeeks,
                        passingGrade = exp.passingGrade,
                        groups = exp.groups.map { g ->
                            me.joxquin.notivas.domain.template.GroupTemplate(
                                name = g.name,
                                weightPercentage = g.weightPercentage,
                                targetAssessments = g.targetAssessments,
                                dropLowest = g.dropLowest,
                                minToDrop = g.minToDrop,
                                calculationMode = g.calculationMode,
                                items = g.items.map { itm ->
                                    me.joxquin.notivas.domain.template.ItemTemplate(
                                        name = itm.name,
                                        weekNumber = itm.weekNumber,
                                        maxScore = itm.maxScore,
                                        internalWeight = itm.internalWeight,
                                        matchingPatterns = itm.matchingPatterns
                                    )
                                }
                            )
                        }
                    )
                }

                if (template != null) {
                    remoteList.add(template)
                }
            } catch (_: Exception) {}
        }

        if (remoteList.isEmpty()) {
            EvaluationTemplatesCatalog.templates
        } else {
            // Unir plantillas remotas con las locales evitando duplicados por ID
            val remoteIds = remoteList.map { it.id }.toSet()
            remoteList + EvaluationTemplatesCatalog.templates.filter { it.id !in remoteIds }
        }
    }
}
