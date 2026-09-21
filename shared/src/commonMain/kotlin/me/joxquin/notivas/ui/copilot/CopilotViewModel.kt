package me.joxquin.notivas.ui.copilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotChatRepository
import me.joxquin.notivas.data.repository.CopilotMessageItem
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.data.repository.CopilotRole
import me.joxquin.notivas.data.repository.OpenRouterAccountBalance
import me.joxquin.notivas.util.DateTimeUtil

enum class MentionStep {
    COURSES,
    COURSE_RESOURCES
}

enum class MentionResourceType {
    ALL_COURSE,
    ASSIGNMENT,
    MODULE,
    FILE,
    DISCUSSION
}

data class MentionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: MentionResourceType,
    val rawItem: Any? = null
)

data class CopilotUiState(
    val courses: List<Course> = emptyList(),
    val selectedCourseId: Long? = null,
    val messages: List<CopilotMessageItem> = emptyList(),
    val isLoading: Boolean = false,
    val isCopilotEnabled: Boolean = true,
    val hasApiKey: Boolean = false,
    val currentModel: String = "google/gemini-2.5-flash",
    val inputText: String = "",
    val errorMessage: String? = null,
    // Interactive @ mention state
    val isMentionPopupVisible: Boolean = false,
    val mentionStep: MentionStep = MentionStep.COURSES,
    val mentionQuery: String = "",
    val selectedCourseForMention: Course? = null,
    val mentionResourceList: List<MentionItem> = emptyList(),
    val courseAssignments: List<Assignment> = emptyList(),
    val coursePlannerItems: List<PlannerItem> = emptyList(),
    val courseModules: List<CanvasModule> = emptyList(),
    // Chat sessions and history
    val currentSessionId: String? = null,
    val sessions: List<CopilotSession> = emptyList(),
    val isHistorySheetVisible: Boolean = false,
    // Token & Credit stats
    val sessionTokens: Int = 0,
    val totalAccountTokens: Long = 0L,
    val balance: Double? = null,
    val openRouterBalance: OpenRouterAccountBalance? = null
)

class CopilotViewModel(
    private val canvasRepository: CanvasRepository,
    private val copilotRepository: CopilotRepository,
    private val chatRepository: CopilotChatRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    private val _messages = MutableStateFlow<List<CopilotMessageItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _inputText = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _currentSessionId = MutableStateFlow<String?>(null)
    private val _isHistorySheetVisible = MutableStateFlow(false)
    private val _sessionTokens = MutableStateFlow(0)
    private val _openRouterBalance = MutableStateFlow<OpenRouterAccountBalance?>(null)

    // @ Mention State
    private val _isMentionPopupVisible = MutableStateFlow(false)
    private val _mentionStep = MutableStateFlow(MentionStep.COURSES)
    private val _mentionQuery = MutableStateFlow("")
    private val _selectedCourseForMention = MutableStateFlow<Course?>(null)
    private val _courseAssignments = MutableStateFlow<List<Assignment>>(emptyList())
    private val _coursePlannerItems = MutableStateFlow<List<PlannerItem>>(emptyList())
    private val _courseModules = MutableStateFlow<List<CanvasModule>>(emptyList())

    init {
        refreshOpenRouterBalance()
    }

    fun refreshOpenRouterBalance() {
        viewModelScope.launch {
            _openRouterBalance.value = copilotRepository.getOpenRouterBalance()
        }
    }

    val courses: StateFlow<List<Course>> =
        canvasRepository.allCourses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<CopilotUiState> = combine(
        combine(
            courses,
            _selectedCourseId,
            _messages,
            _isLoading,
            _inputText
        ) { c, sId, msgs, loading, input ->
            Tuple5(c, sId, msgs, loading, input)
        },
        combine(
            preferencesManager.copilotEnabled,
            preferencesManager.openRouterApiKey,
            preferencesManager.openRouterModel,
            _errorMessage
        ) { enabled, apiKey, model, error ->
            Tuple4(enabled, !apiKey.isNullOrBlank(), model, error)
        },
        combine(
            _isMentionPopupVisible,
            _mentionStep,
            _mentionQuery,
            _selectedCourseForMention
        ) { showMenu, step, query, activeCourse ->
            Tuple4(showMenu, step, query, activeCourse)
        },
        combine(
            combine(
                _courseAssignments,
                _coursePlannerItems,
                _courseModules
            ) { assignments, plannerItems, modules ->
                Triple(assignments, plannerItems, modules)
            },
            combine(
                _currentSessionId,
                chatRepository.allSessions,
                _isHistorySheetVisible
            ) { currentSessionId, savedSessions, showHistory ->
                Triple(currentSessionId, savedSessions, showHistory)
            },
            combine(
                _sessionTokens,
                preferencesManager.totalCopilotTokens,
                _openRouterBalance
            ) { sTokens, tTokens, balance ->
                Triple(sTokens, tTokens, balance)
            }
        ) { resources, sessionInfo, tokenInfo ->
            Triple(resources, sessionInfo, tokenInfo)
        }
    ) { (c, sId, msgs, loading, input),
        (enabled, hasApiKey, model, error),
        (showMenu, step, query, activeCourse),
        (resources, sessionInfo, tokenInfo) ->
        val (assignments, plannerItems, modules) = resources
        val (currentSessionId, savedSessions, showHistory) = sessionInfo
        val (sessionTokens, totalTokens, balance) = tokenInfo

        // Generar lista de recursos para autocompletar si un curso está seleccionado
        val resourceList = mutableListOf<MentionItem>()
        if (activeCourse != null) {
            resourceList.add(
                MentionItem(
                    id = "course_${activeCourse.id}",
                    title = "Todo el Curso",
                    subtitle = "Contexto general de ${activeCourse.name}",
                    type = MentionResourceType.ALL_COURSE,
                    rawItem = activeCourse
                )
            )
            assignments.forEach { a ->
                resourceList.add(
                    MentionItem(
                        id = "assignment_${a.id}",
                        title = a.name,
                        subtitle = "Tarea / Entrega (${a.dueAt ?: "Sin fecha"})",
                        type = MentionResourceType.ASSIGNMENT,
                        rawItem = a
                    )
                )
            }
            modules.forEach { m ->
                val count = m.itemsCount ?: m.items?.size ?: 0
                resourceList.add(
                    MentionItem(
                        id = "module_${m.id}",
                        title = m.name,
                        subtitle = "Módulo ($count elementos)",
                        type = MentionResourceType.MODULE,
                        rawItem = m
                    )
                )
            }
        }

        // Filtrar cursos por query si estamos en paso COURSES
        val filteredCourses = if (query.isNotBlank() && step == MentionStep.COURSES) {
            c.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.courseCode?.contains(query, ignoreCase = true) == true)
            }
        } else {
            c
        }

        CopilotUiState(
            courses = filteredCourses,
            selectedCourseId = sId,
            messages = msgs,
            isLoading = loading,
            isCopilotEnabled = enabled,
            hasApiKey = hasApiKey,
            currentModel = model,
            inputText = input,
            errorMessage = error,
            isMentionPopupVisible = showMenu,
            mentionStep = step,
            mentionQuery = query,
            selectedCourseForMention = activeCourse,
            mentionResourceList = resourceList,
            courseAssignments = assignments,
            coursePlannerItems = plannerItems,
            courseModules = modules,
            currentSessionId = currentSessionId,
            sessions = savedSessions,
            isHistorySheetVisible = showHistory,
            sessionTokens = sessionTokens,
            totalAccountTokens = totalTokens,
            balance = balance?.remainingCredits,
            openRouterBalance = balance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CopilotUiState()
    )

    fun startNewSession() {
        _currentSessionId.value = null
        _messages.value = emptyList()
        _sessionTokens.value = 0
        _inputText.value = ""
        _errorMessage.value = null
        _isHistorySheetVisible.value = false
    }

    fun showHistorySheet() {
        _isHistorySheetVisible.value = true
    }

    fun dismissHistorySheet() {
        _isHistorySheetVisible.value = false
    }

    fun clearChat() {
        startNewSession()
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val session = chatRepository.getSessionById(sessionId)
            val messages = chatRepository.getMessagesForSession(sessionId)
            _currentSessionId.value = sessionId
            _selectedCourseId.value = session?.courseId
            _messages.value = messages
            val sessionTokens = if (session != null && session.totalTokens > 0) {
                session.totalTokens
            } else {
                messages.sumOf { it.tokens }
            }
            _sessionTokens.value = sessionTokens
            _isHistorySheetVisible.value = false
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isNotBlank()) {
            chatRepository.updateSessionTitle(sessionId, trimmed)
        }
    }

    fun deleteSession(sessionId: String) {
        chatRepository.deleteSession(sessionId)
        if (_currentSessionId.value == sessionId) {
            startNewSession()
        }
    }

    fun selectCourse(courseId: Long?) {
        _selectedCourseId.value = courseId
    }

    fun updateInputText(text: String) {
        _inputText.value = text

        val lastAtIndex = text.lastIndexOf('@')
        if (lastAtIndex != -1) {
            val afterAt = text.substring(lastAtIndex + 1)
            if (!afterAt.contains(' ')) {
                _mentionQuery.value = afterAt
                if (!_isMentionPopupVisible.value) {
                    _isMentionPopupVisible.value = true
                    _mentionStep.value = MentionStep.COURSES
                }
            } else {
                if (_mentionStep.value == MentionStep.COURSES) {
                    _isMentionPopupVisible.value = false
                }
            }
        } else {
            _isMentionPopupVisible.value = false
            _selectedCourseForMention.value = null
        }
    }

    fun triggerMention() {
        val currentText = _inputText.value
        val newText = if (currentText.isEmpty() || currentText.endsWith(" ")) {
            "$currentText@"
        } else {
            "$currentText @"
        }
        _inputText.value = newText
        _mentionQuery.value = ""
        _mentionStep.value = MentionStep.COURSES
        _isMentionPopupVisible.value = true
    }

    fun selectMentionCourse(course: Course) {
        _selectedCourseForMention.value = course
        _selectedCourseId.value = course.id
        _mentionStep.value = MentionStep.COURSE_RESOURCES
        _courseModules.value = emptyList()

        viewModelScope.launch {
            canvasRepository.getAssignmentsForCourse(course.id).firstOrNull()?.let {
                _courseAssignments.value = it
            }
            canvasRepository.getPlannerItemsForCourse(course.id).firstOrNull()?.let {
                _coursePlannerItems.value = it
            }
            _courseModules.value = canvasRepository.fetchCourseModules(course.id)
        }
    }

    fun selectMentionResource(item: MentionItem) {
        val course = _selectedCourseForMention.value ?: return
        if (item.type == MentionResourceType.ALL_COURSE) {
            applyCourseMention(course)
        } else {
            applyResourceMention(course, item.title)
        }
    }

    fun backToCourseSelection() {
        _mentionStep.value = MentionStep.COURSES
        _selectedCourseForMention.value = null
        _courseModules.value = emptyList()
    }

    fun applyCourseMention(course: Course) {
        val currentText = _inputText.value
        val lastAtIndex = currentText.lastIndexOf('@')
        val prefix = if (lastAtIndex != -1) currentText.substring(0, lastAtIndex) else currentText
        val tag = "@[${course.courseCode ?: course.name}] "
        _inputText.value = prefix + tag
        _selectedCourseId.value = course.id
        dismissMentionPopup()
    }

    fun applyResourceMention(course: Course, resourceName: String) {
        val currentText = _inputText.value
        val lastAtIndex = currentText.lastIndexOf('@')
        val prefix = if (lastAtIndex != -1) currentText.substring(0, lastAtIndex) else currentText
        val courseLabel = course.courseCode ?: course.name
        val tag = "@[$courseLabel > $resourceName] "
        _inputText.value = prefix + tag
        _selectedCourseId.value = course.id
        dismissMentionPopup()
    }

    fun dismissMentionPopup() {
        _isMentionPopupVisible.value = false
        _mentionStep.value = MentionStep.COURSES
        _selectedCourseForMention.value = null
        _mentionQuery.value = ""
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun sendMessage(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _inputText.value).trim()
        if (prompt.isBlank() || _isLoading.value) return

        dismissMentionPopup()

        val sessionId = _currentSessionId.value ?: DateTimeUtil.nowEpochMillis().toString().also {
            _currentSessionId.value = it
        }

        val userMessage = CopilotMessageItem(
            id = DateTimeUtil.nowEpochMillis().toString(),
            role = CopilotRole.USER,
            text = prompt,
            timestamp = DateTimeUtil.nowEpochMillis()
        )

        val updatedMessages = _messages.value + userMessage
        _messages.value = updatedMessages
        _inputText.value = ""
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val currentSession = chatRepository.getSessionById(sessionId)
            if (currentSession == null) {
                val title = prompt.take(40) + if (prompt.length > 40) "..." else ""
                chatRepository.createOrUpdateSession(
                    sessionId = sessionId,
                    title = title,
                    courseId = _selectedCourseId.value
                )
            }
            chatRepository.saveMessage(sessionId, userMessage)

            val history = updatedMessages
                .dropLast(1)
                .map { msg ->
                    OpenRouterMessage(
                        role = if (msg.role == CopilotRole.USER) "user" else "assistant",
                        content = msg.text
                    )
                }

            val result = copilotRepository.queryCopilot(
                history = history,
                userPrompt = prompt,
                selectedCourseId = _selectedCourseId.value
            )

            _isLoading.value = false

            result.fold(
                onSuccess = { response ->
                    val assistantMessage = CopilotMessageItem(
                        id = (DateTimeUtil.nowEpochMillis() + 1).toString(),
                        role = CopilotRole.ASSISTANT,
                        text = response.reply,
                        sources = response.sources,
                        actionFeedback = response.actionFeedback,
                        tokens = response.totalTokens,
                        timestamp = DateTimeUtil.nowEpochMillis()
                    )
                    _messages.value = _messages.value + assistantMessage
                    val newSessionTokens = _sessionTokens.value + response.totalTokens
                    _sessionTokens.value = newSessionTokens
                    chatRepository.saveMessage(sessionId, assistantMessage)
                    chatRepository.updateSessionTokens(sessionId, newSessionTokens)
                    refreshOpenRouterBalance()
                },
                onFailure = { error ->
                    val errorText = error.message ?: "Ocurrió un error inesperado."
                    _errorMessage.value = errorText
                    val assistantErrorMessage = CopilotMessageItem(
                        id = (DateTimeUtil.nowEpochMillis() + 1).toString(),
                        role = CopilotRole.ASSISTANT,
                        text = "⚠️ No se pudo procesar tu solicitud: $errorText\n\nPor favor, verifica tu API Key de OpenRouter y tu conexión.",
                        timestamp = DateTimeUtil.nowEpochMillis()
                    )
                    _messages.value = _messages.value + assistantErrorMessage
                    chatRepository.saveMessage(sessionId, assistantErrorMessage)
                }
            )
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
