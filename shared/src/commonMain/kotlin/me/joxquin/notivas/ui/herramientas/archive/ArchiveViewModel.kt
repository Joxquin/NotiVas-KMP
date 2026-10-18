package me.joxquin.notivas.ui.herramientas.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.util.DateTimeUtil

enum class ArchiveTab {
    SUBMITTED,
    DISCARDED,
    EXPIRED
}

sealed class ArchiveItem {
    abstract val id: String
    abstract val title: String
    abstract val courseName: String
    abstract val courseCode: String
    abstract val timestamp: String?
    abstract val pointsPossible: Double?

    data class AssignmentItem(
        val assignment: Assignment,
        override val courseName: String,
        override val courseCode: String,
        val draft: AssignmentDraftEntity? = null,
        val isDiscarded: Boolean = false,
        val isExpired: Boolean = false,
        val isSubmitted: Boolean = false
    ) : ArchiveItem() {
        override val id: String = "assignment_${assignment.id}"
        override val title: String = assignment.name
        override val timestamp: String? = assignment.submittedAt ?: assignment.dueAt
        override val pointsPossible: Double? = assignment.pointsPossible
    }

    data class ForumItem(
        val topicId: Long,
        override val title: String,
        override val courseName: String,
        override val courseCode: String,
        override val timestamp: String?,
        override val pointsPossible: Double?,
        val score: Double?,
        val isCopilotAssisted: Boolean = true
    ) : ArchiveItem() {
        override val id: String = "forum_$topicId"
    }
}

data class ArchiveSummaryStats(
    val onTimeCount: Int = 0,
    val onTimePercentage: Int = 100,
    val discardedCount: Int = 0,
    val averageScore: Double = 0.0,
    val totalLocalRecords: Int = 0,
    val submittedCount: Int = 0,
    val expiredCount: Int = 0
)

data class ArchiveUiState(
    val activeTab: ArchiveTab = ArchiveTab.SUBMITTED,
    val searchQuery: String = "",
    val items: List<ArchiveItem> = emptyList(),
    val stats: ArchiveSummaryStats = ArchiveSummaryStats(),
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val toastMessage: String? = null
)

class ArchiveViewModel(
    private val repository: CanvasRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _activeTab = MutableStateFlow(ArchiveTab.SUBMITTED)
    private val _searchQuery = MutableStateFlow("")
    private val _toastMessage = MutableStateFlow<String?>(null)

    private val _dataFlow = combine(
        repository.allAssignments,
        repository.allCourses,
        repository.observeAllAssignmentDrafts(),
        preferencesManager.discardedAssignmentIds
    ) { assignments, courses, drafts, discardedIds ->
        Tuple4(assignments, courses, drafts, discardedIds)
    }

    val uiState: StateFlow<ArchiveUiState> = combine(
        _activeTab,
        _searchQuery,
        _dataFlow
    ) { activeTab, query, data ->
        val (assignments, courses, drafts, discardedIds) = data
        val courseMap = courses.associateBy { it.id }
        val draftMap = drafts.associateBy { it.assignmentId }
        val now = DateTimeUtil.nowEpochMillis()

        val allItems = mutableListOf<ArchiveItem>()

        // 1. Mapear Assignments
        for (a in assignments) {
            val course = courseMap[a.courseId]
            val courseName = course?.name ?: "Curso"
            val courseCode = course?.courseCode ?: course?.name?.take(8) ?: "CURSO"
            val draft = draftMap[a.id]
            val isDiscarded = discardedIds.contains(a.id) || (a.pointsPossible == 0.0 && (a.status == "unsubmitted" || a.status == "upcoming"))

            val hasSubmitted = a.status == "completed" ||
                    a.isCompleted ||
                    !a.submittedAt.isNullOrBlank() ||
                    !a.submission?.submittedAt.isNullOrBlank() ||
                    a.submission?.workflowState in listOf("submitted", "graded")

            val dueEpoch = a.dueAt?.let { DateTimeUtil.parseIsoToComponents(it)?.epochMillis } ?: Long.MAX_VALUE
            val isExpired = !hasSubmitted && !isDiscarded && dueEpoch < now

            allItems.add(
                ArchiveItem.AssignmentItem(
                    assignment = a,
                    courseName = courseName,
                    courseCode = courseCode,
                    draft = draft,
                    isDiscarded = isDiscarded,
                    isExpired = isExpired,
                    isSubmitted = hasSubmitted
                )
            )
        }

        // 2. Calcular Estadísticas Globales
        val submittedItems = allItems.filterIsInstance<ArchiveItem.AssignmentItem>().filter { it.isSubmitted }
        val discardedItems = allItems.filterIsInstance<ArchiveItem.AssignmentItem>().filter { it.isDiscarded }
        val expiredItems = allItems.filterIsInstance<ArchiveItem.AssignmentItem>().filter { it.isExpired }

        var scoredCount = 0
        var totalScoreSum = 0.0
        var onTimeCount = 0

        for (item in submittedItems) {
            val score = item.assignment.score ?: item.assignment.submission?.score
            if (score != null && score > 0) {
                totalScoreSum += score
                scoredCount++
            }
            val late = item.assignment.submission?.late ?: false
            if (!late) {
                onTimeCount++
            }
        }

        val onTimeRate = if (submittedItems.isNotEmpty()) {
            ((onTimeCount.toDouble() / submittedItems.size) * 100).toInt()
        } else 100

        val averageScore = if (scoredCount > 0) {
            (totalScoreSum / scoredCount * 10).toInt() / 10.0
        } else 0.0

        val stats = ArchiveSummaryStats(
            onTimeCount = onTimeCount,
            onTimePercentage = onTimeRate,
            discardedCount = discardedItems.size,
            averageScore = averageScore,
            totalLocalRecords = allItems.size,
            submittedCount = submittedItems.size,
            expiredCount = expiredItems.size
        )

        // 3. Filtrar según la Pestaña Activa
        val tabFiltered = when (activeTab) {
            ArchiveTab.SUBMITTED -> submittedItems
            ArchiveTab.DISCARDED -> discardedItems
            ArchiveTab.EXPIRED -> expiredItems
        }

        // 4. Filtrar por Búsqueda
        val finalItems = if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            tabFiltered.filter {
                it.title.lowercase().contains(q) ||
                        it.courseName.lowercase().contains(q) ||
                        it.courseCode.lowercase().contains(q)
            }
        } else {
            tabFiltered
        }

        ArchiveUiState(
            activeTab = activeTab,
            searchQuery = query,
            items = finalItems,
            stats = stats,
            courses = courses,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchiveUiState(isLoading = true)
    )

    fun selectTab(tab: ArchiveTab) {
        _activeTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun restoreAssignmentToPending(assignmentId: Long) {
        viewModelScope.launch {
            preferencesManager.restoreAssignment(assignmentId)
            _toastMessage.value = "assignment_restored"
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            preferencesManager.clearDiscardedAssignments()
            _toastMessage.value = "trash_cleared"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)
