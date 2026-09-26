package me.joxquin.notivas.ui.foros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.ZonedDateTime

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
    val isGeneratingCopilot: Boolean = false,
    val copilotStrategy: CopilotForoStrategy? = null,
    val copilotErrorMessage: String? = null,
    val activeCopilotModel: String = "google/gemini-2.5-flash"
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

    private val _isGeneratingCopilot = MutableStateFlow(false)
    private val _copilotStrategy = MutableStateFlow<CopilotForoStrategy?>(null)
    private val _copilotErrorMessage = MutableStateFlow<String?>(null)
    private val _activeCopilotModel = MutableStateFlow("google/gemini-2.5-flash")

    // Almacenamiento local de borradores por ID de foro en memoria del ViewModel
    private val _draftsMemory = mutableMapOf<Long, String>()

    init {
        preferencesManager?.openRouterModel?.let { modelFlow ->
            viewModelScope.launch {
                modelFlow.collect { modelName ->
                    _activeCopilotModel.value = modelName
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
            _isGeneratingCopilot,
            _copilotStrategy,
            _copilotErrorMessage
        ) { draft, savedMsg, generating, strategy, errorMsg ->
            Tuple5(draft, savedMsg, generating, strategy, errorMsg)
        },
        _activeCopilotModel
    ) { (raw, courses, courseId, tab, sort), (query, loading, selDiscussion, replies, loadingReplies), (draft, savedMsg, generating, strategy, errorMsg), activeModel ->

        val now = ZonedDateTime.now()

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

            val isExpired = isTopicExpired(topic, now)
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
            DiscussionFilterTab.VENCIDOS -> list.filter { isTopicExpired(it, now) }
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
            isGeneratingCopilot = generating,
            copilotStrategy = strategy,
            copilotErrorMessage = errorMsg,
            activeCopilotModel = activeModel
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
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectDiscussion(topic: CanvasDiscussionTopic) {
        _selectedDiscussion.value = topic
        _currentDraft.value = _draftsMemory[topic.id] ?: ""
        _isDraftSavedMessage.value = false
        loadDiscussionEntries(topic)
    }

    fun clearSelectedDiscussion() {
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
            } catch (_: Exception) {
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
        _copilotStrategy.value = null
        _copilotErrorMessage.value = null
    }

    fun generateCopilotStrategy(
        topic: CanvasDiscussionTopic,
        toneInstruction: String? = null
    ) {
        if (copilotRepository == null) {
            _copilotErrorMessage.value = "CopilotRepository no está disponible."
            return
        }

        viewModelScope.launch {
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
                appendLine("Genera una respuesta en formato JSON EXACTO sin bloques markdown adicionales:")
                appendLine("""
{
  "learningObjective": "Resumen conciso en una o dos frases del objetivo conceptual clave que evalúa el profesor",
  "technicalRigor": "Nivel de profundidad teórica (ej. 100% • Alto)",
  "counterExample": "Breve mención del contraejemplo o caso límite (ej. Incluido o No requerido)",
  "activeInteraction": "Tipo de pregunta de cierre (ej. Pregunta Abierta de Debate)",
  "phase1Title": "Título de la fase 1 (ej. Tesis Técnica)",
  "phase1Desc": "Explicación concisa de lo que se sustenta en esta fase",
  "phase2Title": "Título de la fase 2 (ej. Demostración y Caso)",
  "phase2Desc": "Explicación concisa del caso práctico o contraejemplo",
  "phase3Title": "Título de la fase 3 (ej. Pregunta de Debate)",
  "phase3Desc": "Explicación de la pregunta constructiva hacia el aula",
  "suggestedDraft": "El texto formal completo y riguroso listo para publicar en el foro (incluye saludo, desarrollo de los puntos solicitados con solidez, fórmulas o complejidades si aplica, y una pregunta de cierre formal para los compañeros)."
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
                    val jsonString = if (text.contains("{") && text.contains("}")) {
                        text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1)
                    } else {
                        text
                    }

                    try {
                        val parsed = kotlinx.serialization.json.Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        }.decodeFromString<kotlinx.serialization.json.JsonObject>(jsonString)

                        val draft = parsed["suggestedDraft"]?.toString()?.trim('"')?.replace("\\n", "\n") ?: text
                        val words = draft.split(Regex("\\s+")).filter { it.isNotBlank() }.size

                        _copilotStrategy.value = CopilotForoStrategy(
                            learningObjective = parsed["learningObjective"]?.toString()?.trim('"') ?: "Análisis y fundamentación académica rigurosa",
                            technicalRigor = parsed["technicalRigor"]?.toString()?.trim('"') ?: "100% • Alto",
                            counterExample = parsed["counterExample"]?.toString()?.trim('"') ?: "Incluido",
                            activeInteraction = parsed["activeInteraction"]?.toString()?.trim('"') ?: "Pregunta Activa",
                            phase1Title = parsed["phase1Title"]?.toString()?.trim('"') ?: "Tesis Técnica",
                            phase1Desc = parsed["phase1Desc"]?.toString()?.trim('"') ?: "Fundamentación y conceptos teóricos esenciales",
                            phase2Title = parsed["phase2Title"]?.toString()?.trim('"') ?: "Demostración y Caso",
                            phase2Desc = parsed["phase2Desc"]?.toString()?.trim('"') ?: "Ejemplificación contextual y análisis de casos límite",
                            phase3Title = parsed["phase3Title"]?.toString()?.trim('"') ?: "Pregunta de Debate",
                            phase3Desc = parsed["phase3Desc"]?.toString()?.trim('"') ?: "Cuestionamiento constructivo para la discusión con los compañeros",
                            suggestedDraft = draft,
                            wordCount = words,
                            activeModelName = _activeCopilotModel.value
                        )
                    } catch (_: Exception) {
                        // Fallback si el modelo devolvió texto plano
                        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        _copilotStrategy.value = CopilotForoStrategy(
                            learningObjective = "Fundamentación y respuesta a la consigna",
                            technicalRigor = "Alto",
                            counterExample = "Considerado",
                            activeInteraction = "Intercambio constructivo",
                            phase1Title = "Planteamiento Inicial",
                            phase1Desc = "Desarrollo de los conceptos de la consigna",
                            phase2Title = "Desarrollo del Argumento",
                            phase2Desc = "Exposición de ideas y sustento",
                            phase3Title = "Cierre y Debate",
                            phase3Desc = "Pregunta constructiva para el aula",
                            suggestedDraft = text,
                            wordCount = words,
                            activeModelName = _activeCopilotModel.value
                        )
                    }
                }.onFailure { ex ->
                    _copilotErrorMessage.value = ex.message ?: "Error al conectar con la IA de OpenRouter."
                }
            } catch (e: Exception) {
                _copilotErrorMessage.value = e.message ?: "Error inesperado al generar la asistencia."
            } finally {
                _isGeneratingCopilot.value = false
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

    private fun isTopicExpired(topic: CanvasDiscussionTopic, now: ZonedDateTime): Boolean {
        if (topic.locked == true) return true
        val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
        if (dueStr != null) {
            try {
                val dueDate = ZonedDateTime.parse(dueStr)
                return now.isAfter(dueDate)
            } catch (_: Exception) {}
        }
        return false
    }

    private fun getTopicDueDate(topic: CanvasDiscussionTopic): String? {
        return topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
