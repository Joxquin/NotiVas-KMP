package me.joxquin.notivas.ui.foros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.util.DateTimeUtil
import kotlin.text.RegexOption

enum class DiscussionFilterTab {
    TODOS,
    CON_PUNTOS,
    SIN_PUNTOS,
    VENCIDOS
}

enum class DiscussionSortOption(val displayName: String) {
    DUE_DATE_ASC("Fecha de vencimiento (Próximos)"),
    DUE_DATE_DESC("Fecha de vencimiento (Lejanos)"),
    RECENT_POSTED("Publicados recientemente"),
    MOST_REPLIES("Mayor número de intervenciones"),
    TITLE_ASC("Nombre del tema (A - Z)")
}

data class CopilotForoStrategy(
    val learningObjective: String,
    val technicalRigor: String,
    val counterExample: String,
    val activeInteraction: String,
    val phase1Title: String,
    val phase1Desc: String,
    val phase2Title: String,
    val phase2Desc: String,
    val phase3Title: String,
    val phase3Desc: String,
    val suggestedDraft: String,
    val wordCount: Int,
    val activeModelName: String
)

data class ForosUiState(
    val discussions: List<CanvasDiscussionTopic> = emptyList(),
    val filteredDiscussions: List<CanvasDiscussionTopic> = emptyList(),
    val availableCourses: List<Course> = emptyList(),
    val selectedCourseId: Long? = null,
    val selectedFilterTab: DiscussionFilterTab = DiscussionFilterTab.TODOS,
    val sortOption: DiscussionSortOption = DiscussionSortOption.DUE_DATE_ASC,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val totalCount: Int = 0,
    val withPointsCount: Int = 0,
    val withoutPointsCount: Int = 0,
    val expiredCount: Int = 0,
    val pendingCount: Int = 0,
    val selectedDiscussion: CanvasDiscussionTopic? = null,
    val selectedDiscussionReplies: List<me.joxquin.notivas.data.model.CanvasDiscussionEntry> = emptyList(),
    val isLoadingReplies: Boolean = false,
    val currentDraft: String = "",
    val isDraftSavedMessage: Boolean = false,
    val isPublishing: Boolean = false,
    val publishSuccessMessage: Boolean = false,
    val publishErrorMessage: String? = null,
    val isGeneratingCopilot: Boolean = false,
    val copilotStrategy: CopilotForoStrategy? = null,
    val copilotErrorMessage: String? = null,
    val activeCopilotModel: String = "google/gemini-2.5-flash",
    val userProfile: me.joxquin.notivas.data.model.UserProfile? = null,
    val respondedForumIds: Set<Long> = emptySet()
)

class ForosViewModel(
    private val canvasRepository: CanvasRepository,
    private val copilotRepository: me.joxquin.notivas.data.repository.CopilotRepository? = null,
    private val preferencesManager: me.joxquin.notivas.data.local.PreferencesManager? = null
) : ViewModel() {

    private val _rawDiscussions = MutableStateFlow<List<CanvasDiscussionTopic>>(emptyList())
    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    private val _selectedFilterTab = MutableStateFlow(DiscussionFilterTab.TODOS)
    private val _sortOption = MutableStateFlow(DiscussionSortOption.DUE_DATE_ASC)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    private val _selectedDiscussion = MutableStateFlow<CanvasDiscussionTopic?>(null)
    private val _selectedDiscussionReplies = MutableStateFlow<List<me.joxquin.notivas.data.model.CanvasDiscussionEntry>>(emptyList())
    private val _isLoadingReplies = MutableStateFlow(false)
    private val _currentDraft = MutableStateFlow("")
    private val _isDraftSavedMessage = MutableStateFlow(false)
    private val _isPublishing = MutableStateFlow(false)
    private val _publishSuccessMessage = MutableStateFlow(false)
    private val _publishErrorMessage = MutableStateFlow<String?>(null)

    private val _isGeneratingCopilot = MutableStateFlow(false)
    private val _copilotStrategy = MutableStateFlow<CopilotForoStrategy?>(null)
    private val _copilotErrorMessage = MutableStateFlow<String?>(null)
    private val _activeCopilotModel = MutableStateFlow(
        if (preferencesManager?.getCopilotProvider() == me.joxquin.notivas.data.local.CopilotProvider.GOOGLE_AI_STUDIO) {
            preferencesManager.getGoogleAiStudioModel()
        } else {
            preferencesManager?.getOpenRouterModel() ?: "gemini-3.5-flash-lite"
        }
    )

    private val _respondedForumIds = MutableStateFlow<Set<Long>>(emptySet())

    // Almacenamiento local de borradores y estrategias por ID de foro en memoria del ViewModel
    private val _draftsMemory = mutableMapOf<Long, String>()
    private val _copilotStrategiesMemory = mutableMapOf<Long, CopilotForoStrategy>()
    private var copilotGenerationJob: Job? = null

    private val _isFiltersExpanded = MutableStateFlow(true)
    val isFiltersExpanded: StateFlow<Boolean> = _isFiltersExpanded.asStateFlow()

    fun setFiltersExpanded(expanded: Boolean) {
        _isFiltersExpanded.value = expanded
    }

    private val _showFilterBottomSheet = MutableStateFlow(false)
    val showFilterBottomSheet: StateFlow<Boolean> = _showFilterBottomSheet.asStateFlow()

    fun setShowFilterBottomSheet(show: Boolean) {
        _showFilterBottomSheet.value = show
    }

    private val _showCourseDialog = MutableStateFlow(false)
    val showCourseDialog: StateFlow<Boolean> = _showCourseDialog.asStateFlow()

    fun setShowCourseDialog(show: Boolean) {
        _showCourseDialog.value = show
    }

    private val _showSortDialog = MutableStateFlow(false)
    val showSortDialog: StateFlow<Boolean> = _showSortDialog.asStateFlow()

    fun setShowSortDialog(show: Boolean) {
        _showSortDialog.value = show
    }

    init {
        preferencesManager?.let { prefs ->
            viewModelScope.launch {
                combine(
                    prefs.copilotProvider,
                    prefs.googleAiStudioModel,
                    prefs.openRouterModel
                ) { provider, googleModel, openRouterModel ->
                    if (provider == me.joxquin.notivas.data.local.CopilotProvider.GOOGLE_AI_STUDIO) {
                        googleModel
                    } else {
                        openRouterModel
                    }
                }.collect { modelName ->
                    if (_activeCopilotModel.value != modelName) {
                        _activeCopilotModel.value = modelName
                        _copilotStrategiesMemory.clear()
                        _copilotStrategy.value = null
                    }
                }
            }
        }
    }

    val uiState: StateFlow<ForosUiState> = combine(
        combine(
            _rawDiscussions,
            canvasRepository.allCourses,
            _selectedCourseId,
            _selectedFilterTab,
            _sortOption
        ) { raw, courses, courseId, tab, sort ->
            Tuple5(raw, courses, courseId, tab, sort)
        },
        combine(
            _searchQuery,
            _isLoading,
            _selectedDiscussion,
            _selectedDiscussionReplies,
            _isLoadingReplies
        ) { query, loading, selDiscussion, replies, loadingReplies ->
            Tuple5(query, loading, selDiscussion, replies, loadingReplies)
        },
        combine(
            _currentDraft,
            _isDraftSavedMessage,
            _isPublishing,
            _publishSuccessMessage,
            _publishErrorMessage
        ) { draft, savedMsg, publishing, pubSuccess, pubError ->
            Tuple5(draft, savedMsg, publishing, pubSuccess, pubError)
        },
        combine(
            _isGeneratingCopilot,
            _copilotStrategy,
            _copilotErrorMessage,
            _activeCopilotModel
        ) { generating, strategy, errorMsg, activeModel ->
            Tuple4(generating, strategy, errorMsg, activeModel)
        },
        combine(
            canvasRepository.userProfile,
            _respondedForumIds
        ) { profile, respondedIds ->
            profile to respondedIds
        }
    ) { (raw, courses, courseId, tab, sort), (query, loading, selDiscussion, replies, loadingReplies), (draft, savedMsg, publishing, pubSuccess, pubError), (generating, strategy, errorMsg, activeModel), (userProfile, respondedIds) ->

        val nowMs = DateTimeUtil.nowEpochMillis()

        // 1. Conteo global antes de filtrar
        var total = 0
        var withPoints = 0
        var withoutPoints = 0
        var expired = 0
        var pending = 0

        raw.forEach { topic ->
            total++
            val points = topic.assignment?.pointsPossible
            val isGraded = points != null && points > 0
            if (isGraded) withPoints++ else withoutPoints++

            val isExpired = isTopicExpired(topic, nowMs)
            if (isExpired) expired++ else pending++
        }

        // 2. Filtrado por curso
        var list = if (courseId != null) {
            raw.filter { it.courseId == courseId }
        } else {
            raw
        }

        // 3. Filtrado por tab
        list = when (tab) {
            DiscussionFilterTab.TODOS -> list
            DiscussionFilterTab.CON_PUNTOS -> list.filter {
                (it.assignment?.pointsPossible ?: 0.0) > 0 || it.assignmentId != null
            }
            DiscussionFilterTab.SIN_PUNTOS -> list.filter {
                (it.assignment?.pointsPossible ?: 0.0) == 0.0 && it.assignmentId == null
            }
            DiscussionFilterTab.VENCIDOS -> list.filter { isTopicExpired(it, nowMs) }
        }

        // 4. Filtrado por query de búsqueda
        val trimmedQuery = query.trim().lowercase()
        if (trimmedQuery.isNotBlank()) {
            list = list.filter { topic ->
                topic.title.lowercase().contains(trimmedQuery) ||
                        (topic.message?.lowercase()?.contains(trimmedQuery) == true) ||
                        (topic.userName?.lowercase()?.contains(trimmedQuery) == true) ||
                        (topic.author?.displayName?.lowercase()?.contains(trimmedQuery) == true) ||
                        (topic.courseName?.lowercase()?.contains(trimmedQuery) == true)
            }
        }

        // 5. Ordenamiento
        list = when (sort) {
            DiscussionSortOption.DUE_DATE_ASC -> list.sortedWith(compareBy(nullsLast()) { getTopicDueDate(it) })
            DiscussionSortOption.DUE_DATE_DESC -> list.sortedWith(compareByDescending(nullsLast()) { getTopicDueDate(it) })
            DiscussionSortOption.RECENT_POSTED -> list.sortedWith(compareByDescending(nullsLast()) { it.postedAt })
            DiscussionSortOption.MOST_REPLIES -> list.sortedByDescending { it.discussionSubentryCount ?: 0 }
            DiscussionSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
        }

        ForosUiState(
            discussions = raw,
            filteredDiscussions = list,
            availableCourses = courses,
            selectedCourseId = courseId,
            selectedFilterTab = tab,
            sortOption = sort,
            searchQuery = query,
            isLoading = loading,
            totalCount = total,
            withPointsCount = withPoints,
            withoutPointsCount = withoutPoints,
            expiredCount = expired,
            pendingCount = pending,
            selectedDiscussion = selDiscussion,
            selectedDiscussionReplies = replies,
            isLoadingReplies = loadingReplies,
            currentDraft = draft,
            isDraftSavedMessage = savedMsg,
            isPublishing = publishing,
            publishSuccessMessage = pubSuccess,
            publishErrorMessage = pubError,
            isGeneratingCopilot = generating,
            copilotStrategy = strategy,
            copilotErrorMessage = errorMsg,
            activeCopilotModel = activeModel,
            userProfile = userProfile,
            respondedForumIds = respondedIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ForosUiState())

    init {
        loadDiscussions()
    }

    fun loadDiscussions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = canvasRepository.getDiscussionsForCourses()
                _rawDiscussions.value = result
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectDiscussion(topic: CanvasDiscussionTopic) {
        copilotGenerationJob?.cancel()
        copilotGenerationJob = null
        _isGeneratingCopilot.value = false
        _copilotErrorMessage.value = null
        _copilotStrategy.value = _copilotStrategiesMemory[topic.id]

        _selectedDiscussion.value = topic
        _currentDraft.value = _draftsMemory[topic.id] ?: ""
        _isDraftSavedMessage.value = false
        loadDiscussionEntries(topic)
    }

    fun clearSelectedDiscussion() {
        copilotGenerationJob?.cancel()
        copilotGenerationJob = null
        _isGeneratingCopilot.value = false
        _copilotErrorMessage.value = null
        _copilotStrategy.value = null

        _selectedDiscussion.value = null
        _selectedDiscussionReplies.value = emptyList()
        _currentDraft.value = ""
        _isDraftSavedMessage.value = false
    }

    private fun loadDiscussionEntries(topic: CanvasDiscussionTopic) {
        val courseId = topic.courseId ?: return
        viewModelScope.launch {
            _isLoadingReplies.value = true
            try {
                val entries = canvasRepository.getDiscussionEntries(courseId, topic.id)
                _selectedDiscussionReplies.value = entries

                val user = canvasRepository.getUserProfileOnce()
                val hasUserReplied = entries.any { entry ->
                    (user?.id != null && (entry.userId == user.id || entry.user?.id == user.id)) ||
                            (user?.name != null && entry.userName?.equals(user.name, ignoreCase = true) == true)
                }
                if (hasUserReplied) {
                    _respondedForumIds.value = _respondedForumIds.value + topic.id
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _selectedDiscussionReplies.value = emptyList()
            } finally {
                _isLoadingReplies.value = false
            }
        }
    }

    fun updateDraft(text: String) {
        _currentDraft.value = text
        _selectedDiscussion.value?.let { topic ->
            _draftsMemory[topic.id] = text
        }
    }

    fun saveDraft() {
        _selectedDiscussion.value?.let { topic ->
            _draftsMemory[topic.id] = _currentDraft.value
            _isDraftSavedMessage.value = true
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _isDraftSavedMessage.value = false
            }
        }
    }

    fun applyCopilotSuggestion(suggestion: String) {
        _currentDraft.value = suggestion
        _selectedDiscussion.value?.let { topic ->
            _draftsMemory[topic.id] = suggestion
        }
    }

    fun clearCopilotStrategy() {
        copilotGenerationJob?.cancel()
        copilotGenerationJob = null
        _isGeneratingCopilot.value = false
        _copilotStrategy.value = null
        _copilotErrorMessage.value = null
        _selectedDiscussion.value?.let { topic ->
            _copilotStrategiesMemory.remove(topic.id)
        }
    }

    fun generateCopilotStrategy(
        topic: CanvasDiscussionTopic,
        toneInstruction: String? = null
    ) {
        if (copilotRepository == null) {
            _copilotErrorMessage.value = "CopilotRepository no está disponible."
            return
        }

        copilotGenerationJob?.cancel()
        copilotGenerationJob = viewModelScope.launch {
            _isGeneratingCopilot.value = true
            _copilotErrorMessage.value = null

            val cleanMessage = topic.message
                ?.replace(Regex("<[^>]*>"), "")
                ?.replace("&nbsp;", " ")
                ?.trim() ?: "Sin descripción"

            val repliesSummary = _selectedDiscussionReplies.value.take(4).mapIndexed { i, r ->
                val author = r.user?.displayName ?: r.userName ?: "Compañero $i"
                val cleanReply = r.message?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
                "Aporte de $author: $cleanReply"
            }.joinToString("\n")

            val rubricInfo = topic.assignment?.rubric?.map {
                "- ${it.description ?: "Criterio"}: ${it.longDescription ?: ""} (${it.points ?: 0} pts)"
            }?.joinToString("\n") ?: "Sin rúbrica específica adjunta."

            val prompt = buildString {
                appendLine("Actúa como un asistente académico universitario de élite para foros de debate en Canvas LMS.")
                appendLine("Curso: ${topic.courseName ?: "General"}")
                appendLine("Título del Foro: ${topic.title}")
                appendLine("Consigna del Profesor:\n$cleanMessage")
                appendLine("Rúbrica oficial Canvas:\n$rubricInfo")
                if (repliesSummary.isNotBlank()) {
                    appendLine("Intervenciones previas de compañeros:\n$repliesSummary")
                }
                if (!toneInstruction.isNullOrBlank()) {
                    appendLine("Instrucción de estilo o tono solicitado por el estudiante: $toneInstruction")
                }
                appendLine()
                appendLine("REGLAS ESTRICTAS DE RESPUESTA (suggestedDraft):")
                appendLine("1. PROHIBIDO incluir saludos o frases de cortesía iniciales (NO 'Estimado docente', 'Hola profesor', 'Buenas tardes', etc.).")
                appendLine("2. PROHIBIDO incluir despedidas o firmas al final (NO 'Saludos cordiales', 'Atentamente', 'Espero sus comentarios', etc.).")
                appendLine("3. PROHIBIDO incluir preguntas de cierre conversacionales al usuario o compañeros.")
                appendLine("4. 'suggestedDraft' debe ser EXCLUSIVAMENTE el desarrollo analítico, directo, sustantivo y estructurado que responde con el mayor rigor a la consigna académica y los criterios de evaluación.")
                appendLine("5. 'suggestedDraft' debe redactarse en el mismo idioma de la consigna del profesor o del curso (ej. si la consigna o curso es en inglés, redacta en inglés; si es en español, en español).")
                appendLine()
                appendLine("Genera una respuesta en formato JSON EXACTO sin bloques markdown adicionales:")
                appendLine("""
{
  "learningObjective": "Resumen conciso en una o dos frases del objetivo conceptual clave que evalúa el profesor",
  "technicalRigor": "Nivel de profundidad teórica (ej. 100% • Alto)",
  "counterExample": "Breve mención del contraejemplo o caso analizado",
  "activeInteraction": "Enfoque argumental desarrollado",
  "phase1Title": "Título de la fase 1 (ej. Marco Teórico / Premisa)",
  "phase1Desc": "Explicación concisa de la tesis o premisa principal",
  "phase2Title": "Título de la fase 2 (ej. Demostración y Análisis)",
  "phase2Desc": "Explicación concisa del desarrollo o caso analizado",
  "phase3Title": "Título de la fase 3 (ej. Síntesis y Conclusión)",
  "phase3Desc": "Explicación concisa del cierre o conclusión técnica",
  "suggestedDraft": "Texto académico directo, argumentado y riguroso para publicar en el foro. Cero saludos, cero despedidas, cero preguntas de cierre. Solo el argumento sustancial."
}
                """.trimIndent())
            }

            try {
                val result = copilotRepository.queryCopilot(
                    history = emptyList(),
                    userPrompt = prompt,
                    selectedCourseId = topic.courseId
                )

                result.onSuccess { copilotRes ->
                    val text = copilotRes.reply.trim()
                    
                    // Función auxiliar para extraer valores de campos con regex si falla el parser estricto
                    fun extractField(fieldName: String, default: String): String {
                        val pattern = Regex("(?s)\"$fieldName\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"")
                        val match = pattern.find(text)
                        return if (match != null) {
                            match.groupValues[1]
                                .replace("\\\"", "\"")
                                .replace("\\n", "\n")
                                .replace("\\r", "")
                                .replace("\\t", "\t")
                                .trim()
                        } else {
                            // Intento con comillas simples o bloques de texto
                            val fallbackPattern = Regex("(?s)\"$fieldName\"\\s*:\\s*`([^`]*)`")
                            fallbackPattern.find(text)?.groupValues?.get(1)?.trim() ?: default
                        }
                    }

                    // Limpieza previa del JSON
                    val cleanedJson = text
                        .replace(Regex("(?m)^```(?:json)?"), "")
                        .replace(Regex("(?m)```$"), "")
                        .trim()

                    val jsonString = if (cleanedJson.contains("{") && cleanedJson.contains("}")) {
                        cleanedJson.substring(cleanedJson.indexOf("{"), cleanedJson.lastIndexOf("}") + 1)
                    } else {
                        cleanedJson
                    }

                    val strategy = try {
                        val parsed = kotlinx.serialization.json.Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                        }.decodeFromString<kotlinx.serialization.json.JsonObject>(jsonString)

                        fun getCleanString(key: String, fallback: String): String {
                            val element = parsed[key] ?: return fallback
                            val raw = element.toString()
                            return if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length >= 2) {
                                raw.substring(1, raw.length - 1)
                                    .replace("\\\"", "\"")
                                    .replace("\\n", "\n")
                                    .replace("\\r", "")
                                    .replace("\\t", "\t")
                            } else {
                                raw.trim('"')
                            }
                        }

                        val draft = getCleanString("suggestedDraft", extractField("suggestedDraft", text))
                        val words = draft.split(Regex("\\s+")).filter { it.isNotBlank() }.size

                        CopilotForoStrategy(
                            learningObjective = getCleanString("learningObjective", extractField("learningObjective", "Análisis y fundamentación académica")),
                            technicalRigor = getCleanString("technicalRigor", extractField("technicalRigor", "100% • Alto")),
                            counterExample = getCleanString("counterExample", extractField("counterExample", "Análisis de caso")),
                            activeInteraction = getCleanString("activeInteraction", extractField("activeInteraction", "Enfoque Riguroso")),
                            phase1Title = getCleanString("phase1Title", extractField("phase1Title", "Marco Teórico")),
                            phase1Desc = getCleanString("phase1Desc", extractField("phase1Desc", "Fundamentación y conceptos teóricos")),
                            phase2Title = getCleanString("phase2Title", extractField("phase2Title", "Análisis y Demostración")),
                            phase2Desc = getCleanString("phase2Desc", extractField("phase2Desc", "Desarrollo y contrastación")),
                            phase3Title = getCleanString("phase3Title", extractField("phase3Title", "Síntesis y Conclusión")),
                            phase3Desc = getCleanString("phase3Desc", extractField("phase3Desc", "Cierre analítico y resolución técnica")),
                            suggestedDraft = draft,
                            wordCount = words,
                            activeModelName = _activeCopilotModel.value
                        )
                    } catch (_: Exception) {
                        // Fallback 1: Extraer cada campo con Expresiones Regulares
                        val regexDraft = extractField("suggestedDraft", "")
                        val isJsonLooking = text.trimStart().startsWith("{")

                        val cleanDraft = if (regexDraft.isNotBlank()) {
                            regexDraft
                        } else if (isJsonLooking) {
                            // Si parece un JSON pero no se pudo extraer suggestedDraft, extraer cualquier texto sustancial
                            text.replace(Regex("\"[a-zA-Z0-9_]+\"\\s*:\\s*"), "")
                                .replace(Regex("[{}\",]"), "")
                                .trim()
                        } else {
                            text
                        }

                        val words = cleanDraft.split(Regex("\\s+")).filter { it.isNotBlank() }.size

                        CopilotForoStrategy(
                            learningObjective = extractField("learningObjective", "Fundamentación y respuesta a la consigna académica"),
                            technicalRigor = extractField("technicalRigor", "100% • Alto"),
                            counterExample = extractField("counterExample", "Análisis de caso"),
                            activeInteraction = extractField("activeInteraction", "Análisis Directo"),
                            phase1Title = extractField("phase1Title", "Marco Teórico"),
                            phase1Desc = extractField("phase1Desc", "Desarrollo de los conceptos principales"),
                            phase2Title = extractField("phase2Title", "Desarrollo del Argumento"),
                            phase2Desc = extractField("phase2Desc", "Exposición de ideas y sustento técnico"),
                            phase3Title = extractField("phase3Title", "Síntesis y Conclusión"),
                            phase3Desc = extractField("phase3Desc", "Resolución y cierre analítico"),
                            suggestedDraft = cleanDraft,
                            wordCount = words,
                            activeModelName = _activeCopilotModel.value
                        )
                    }

                    _copilotStrategiesMemory[topic.id] = strategy
                    if (_selectedDiscussion.value?.id == topic.id) {
                        _copilotStrategy.value = strategy
                    }
                }.onFailure { ex ->
                    if (_selectedDiscussion.value?.id == topic.id) {
                        _copilotErrorMessage.value = ex.message ?: "Error al conectar con el asistente de IA."
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (_selectedDiscussion.value?.id == topic.id) {
                    _copilotErrorMessage.value = e.message ?: "Error inesperado al generar la asistencia."
                }
            } finally {
                if (_selectedDiscussion.value?.id == topic.id) {
                    _isGeneratingCopilot.value = false
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTabSelected(tab: DiscussionFilterTab) {
        _selectedFilterTab.value = tab
    }

    fun onCourseSelected(courseId: Long?) {
        _selectedCourseId.value = courseId
    }

    fun onSortOptionSelected(option: DiscussionSortOption) {
        _sortOption.value = option
    }

    private fun isTopicExpired(topic: CanvasDiscussionTopic, nowMs: Long): Boolean {
        if (topic.locked == true) return true
        val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
        if (dueStr != null) {
            val epochMs = DateTimeUtil.parseIsoToComponents(dueStr)?.epochMillis
            if (epochMs != null) {
                return nowMs > epochMs
            }
        }
        return false
    }

    fun publishForumReply(topic: CanvasDiscussionTopic) {
        val courseId = topic.courseId ?: return
        val message = _currentDraft.value.trim()
        if (message.isBlank()) {
            _publishErrorMessage.value = "No se puede publicar una respuesta vacía."
            return
        }

        if (_isPublishing.value) return

        viewModelScope.launch {
            _isPublishing.value = true
            _publishErrorMessage.value = null
            _publishSuccessMessage.value = false

            val result = canvasRepository.postDiscussionEntry(
                courseId = courseId,
                topicId = topic.id,
                message = message
            )

            result.fold(
                onSuccess = { newEntry ->
                    // Limpiar borrador local
                    _currentDraft.value = ""
                    _draftsMemory.remove(topic.id)
                    _respondedForumIds.value = _respondedForumIds.value + topic.id
                    _publishSuccessMessage.value = true
                    _publishErrorMessage.value = null

                    // Agregar entrada inmediatamente o recargar
                    val currentReplies = _selectedDiscussionReplies.value.toMutableList()
                    currentReplies.add(0, newEntry)
                    _selectedDiscussionReplies.value = currentReplies

                    // Refrescar entradas de la API para sincronizar
                    loadDiscussionEntries(topic)

                    // Ocultar mensaje de éxito tras 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _publishSuccessMessage.value = false
                },
                onFailure = { error ->
                    _publishErrorMessage.value = error.message ?: "Error al publicar la respuesta en Canvas."
                    _publishSuccessMessage.value = false
                }
            )
            _isPublishing.value = false
        }
    }

    fun clearPublishMessages() {
        _publishSuccessMessage.value = false
        _publishErrorMessage.value = null
    }

    private fun getTopicDueDate(topic: CanvasDiscussionTopic): String? {
        return topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
    }
}

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
