package me.joxquin.notivas.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.util.DateTimeUtil

enum class ActivityFilter(val typeKey: String?) {
    TODOS(null),
    SUBMISSIONS("ASSIGNMENT_SUBMISSION"),
    FORUMS("FORUM_POST"),
    NOTIFICATIONS("CANVAS_NOTIFICATION"),
    DRIVE("DRIVE_SYNC")
}

enum class ActivityTimeGroup {
    HOY,
    AYER,
    ESTA_SEMANA,
    ANTERIORES
}

data class ActivityUiState(
    val searchQuery: String = "",
    val selectedFilter: ActivityFilter = ActivityFilter.TODOS,
    val groupedActivities: Map<ActivityTimeGroup, List<ActivityLogEntity>> = emptyMap(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

class ActivityViewModel(
    private val repository: CanvasRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(ActivityFilter.TODOS)

    init {
        viewModelScope.launch {
            repository.seedInitialActivitiesIfEmpty()
        }
    }

    val uiState: StateFlow<ActivityUiState> = combine(
        repository.observeActivityLogs(),
        repository.allCourses,
        _searchQuery,
        _selectedFilter
    ) { logs, courses, query, filter ->
        val resolvedLogs = logs.map { log ->
            val matchingCourse = courses.find { c ->
                (log.secondaryId != null && c.id == log.secondaryId) ||
                (!log.courseName.isNullOrBlank() && c.name.equals(log.courseName, ignoreCase = true)) ||
                (!log.courseCode.isNullOrBlank() && (c.name.equals(log.courseCode, ignoreCase = true) || c.courseCode.equals(log.courseCode, ignoreCase = true)))
            }
            val shortCode = matchingCourse?.courseCode?.takeIf { it.isNotBlank() }
                ?: log.courseCode?.takeIf { it.isNotBlank() && it.length <= 16 }
                ?: matchingCourse?.name?.take(10)
                ?: log.courseName?.takeIf { it.isNotBlank() }?.let { if (it.length > 14) it.take(10) + "…" else it }
                ?: "ProgMov"

            if (log.courseCode != shortCode) {
                log.copy(courseCode = shortCode, courseName = log.courseName ?: matchingCourse?.name)
            } else {
                log
            }
        }

        val filtered = resolvedLogs.filter { log ->
            val matchesFilter = when (filter) {
                ActivityFilter.TODOS -> true
                else -> log.type == filter.typeKey
            }
            val matchesQuery = query.isBlank() ||
                    log.title.contains(query, ignoreCase = true) ||
                    log.description.contains(query, ignoreCase = true) ||
                    (log.courseCode?.contains(query, ignoreCase = true) == true) ||
                    (log.courseName?.contains(query, ignoreCase = true) == true)

            matchesFilter && matchesQuery
        }

        val now = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sevenDaysMs = 7 * oneDayMs

        val grouped = linkedMapOf<ActivityTimeGroup, MutableList<ActivityLogEntity>>()

        filtered.forEach { item ->
            val diff = (now - item.timestamp).coerceAtLeast(0L)
            val group = when {
                diff < oneDayMs -> ActivityTimeGroup.HOY
                diff < 2 * oneDayMs -> ActivityTimeGroup.AYER
                diff < sevenDaysMs -> ActivityTimeGroup.ESTA_SEMANA
                else -> ActivityTimeGroup.ANTERIORES
            }
            grouped.getOrPut(group) { mutableListOf() }.add(item)
        }

        ActivityUiState(
            searchQuery = query,
            selectedFilter = filter,
            groupedActivities = grouped,
            totalCount = filtered.size,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityUiState(isLoading = true))

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: ActivityFilter) {
        _selectedFilter.value = filter
    }
}
