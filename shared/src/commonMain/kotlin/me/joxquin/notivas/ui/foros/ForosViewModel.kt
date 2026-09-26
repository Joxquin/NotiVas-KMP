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
    val pendingCount: Int = 0
)

class ForosViewModel(
    private val canvasRepository: CanvasRepository
) : ViewModel() {

    private val _rawDiscussions = MutableStateFlow<List<CanvasDiscussionTopic>>(emptyList())
    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    private val _selectedFilterTab = MutableStateFlow(DiscussionFilterTab.TODOS)
    private val _sortOption = MutableStateFlow(DiscussionSortOption.DUE_DATE_ASC)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<ForosUiState> = combine(
        _rawDiscussions,
        canvasRepository.allCourses,
        _selectedCourseId,
        _selectedFilterTab,
        _sortOption,
        _searchQuery,
        _isLoading
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val raw = args[0] as List<CanvasDiscussionTopic>
        @Suppress("UNCHECKED_CAST")
        val courses = args[1] as List<Course>
        val courseId = args[2] as Long?
        val tab = args[3] as DiscussionFilterTab
        val sort = args[4] as DiscussionSortOption
        val query = (args[5] as String).trim().lowercase()
        val loading = args[6] as Boolean

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
        if (query.isNotBlank()) {
            list = list.filter { topic ->
                topic.title.lowercase().contains(query) ||
                        (topic.message?.lowercase()?.contains(query) == true) ||
                        (topic.userName?.lowercase()?.contains(query) == true) ||
                        (topic.author?.displayName?.lowercase()?.contains(query) == true) ||
                        (topic.courseName?.lowercase()?.contains(query) == true)
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
            pendingCount = pending
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
