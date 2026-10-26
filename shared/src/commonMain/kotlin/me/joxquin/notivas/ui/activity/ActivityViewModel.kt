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
        repository.allAssignments,
        _searchQuery,
        _selectedFilter
    ) { logs, courses, assignments, query, filter ->
        val resolvedLogs = logs.map { log ->
            var targetId = log.targetId
            var secondaryId = log.secondaryId
            var courseName = log.courseName

            // 1. Si courseName es nulo, extraerlo del título (ej. "Alerta de Urgencia · Tecnologías Emergentes...")
            if (courseName.isNullOrBlank() && log.title.contains(" · ")) {
                courseName = log.title.substringAfter(" · ").trim()
            }

            // 2. Identificar el curso correspondiente
            val matchingCourse = courses.find { c ->
                (secondaryId != null && c.id == secondaryId) ||
                (!courseName.isNullOrBlank() && (c.name.equals(courseName, ignoreCase = true) || courseName.contains(c.name, ignoreCase = true) || c.name.contains(courseName, ignoreCase = true))) ||
                (!log.courseCode.isNullOrBlank() && (c.name.equals(log.courseCode, ignoreCase = true) || c.courseCode.equals(log.courseCode, ignoreCase = true)))
            }

            if (secondaryId == null && matchingCourse != null) {
                secondaryId = matchingCourse.id
            }

            // 3. Si targetId es nulo o es notificación de Canvas, resolver la tarea precisa
            if (targetId == null && assignments.isNotEmpty()) {
                val candidateAssignments = if (matchingCourse != null) {
                    assignments.filter { it.courseId == matchingCourse.id }
                } else {
                    assignments
                }

                val rawPrefix = log.description.substringBefore(":").trim()
                val cleanPrefix = rawPrefix.replace("...", "").replace("…", "").trim()
                val prefixWeek = Regex("""\bS(\d+)\b""", RegexOption.IGNORE_CASE).find(cleanPrefix)?.groupValues?.get(1)

                val matchedAssignment = candidateAssignments.find { a ->
                    val aWeek = Regex("""\bS(\d+)\b""", RegexOption.IGNORE_CASE).find(a.name)?.groupValues?.get(1)
                    val weekMatches = prefixWeek == null || aWeek == null || prefixWeek == aWeek

                    weekMatches && (
                        a.name.startsWith(cleanPrefix, ignoreCase = true) ||
                        cleanPrefix.startsWith(a.name, ignoreCase = true) ||
                        (cleanPrefix.length >= 10 && a.name.contains(cleanPrefix, ignoreCase = true)) ||
                        log.title.contains(a.name, ignoreCase = true)
                    )
                } ?: candidateAssignments.find { a ->
                    val aWeek = Regex("""\bS(\d+)\b""", RegexOption.IGNORE_CASE).find(a.name)?.groupValues?.get(1)
                    val weekMatches = prefixWeek == null || aWeek == null || prefixWeek == aWeek
                    weekMatches && a.name.contains(cleanPrefix.take(12), ignoreCase = true)
                }

                if (matchedAssignment != null) {
                    targetId = matchedAssignment.id
                    if (secondaryId == null) {
                        secondaryId = matchedAssignment.courseId
                    }
                }
            }

            val finalCourseName = matchingCourse?.name ?: courseName
            val shortCode = me.joxquin.notivas.util.CourseUtil.extractCourseTag(
                matchingCourse?.courseCode ?: log.courseCode,
                finalCourseName
            )

            log.copy(
                targetId = targetId,
                secondaryId = secondaryId ?: matchingCourse?.id,
                courseCode = shortCode,
                courseName = finalCourseName
            )
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
