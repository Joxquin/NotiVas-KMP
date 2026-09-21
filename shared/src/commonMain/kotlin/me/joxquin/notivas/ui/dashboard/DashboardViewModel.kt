package me.joxquin.notivas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.util.DateComponents
import me.joxquin.notivas.util.DateTimeUtil

enum class SyncStatus {
    SYNCING,
    SYNCED,
    FAILED
}

data class AssignmentUiModel(
    val assignment: Assignment,
    val courseName: String
)

data class CourseStat(
    val course: Course,
    val pendingCount: Int,
    val completedCount: Int,
    val missingCount: Int,
    val averageScore: Double?
)

data class DaySchedule(
    val date: DateComponents,
    val dayName: String,
    val dayNumber: Int,
    val taskCount: Int,
    val isToday: Boolean
)

data class DashboardUiState(
    val userProfile: UserProfile? = null,
    val isRefreshing: Boolean = false,
    val institutionName: String = "CANVAS",
    val courses: List<Course> = emptyList(),
    val urgentAssignments: List<AssignmentUiModel> = emptyList(),
    val allAssignments: List<AssignmentUiModel> = emptyList(),
    val courseStats: List<CourseStat> = emptyList(),
    val weeklySchedule: List<DaySchedule> = emptyList(),
    val selectedDate: DateComponents? = null,
    val selectedDateAssignments: List<AssignmentUiModel> = emptyList(),
    val inspectedCourse: Course? = null
)

class DashboardViewModel(
    private val repository: CanvasRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.SYNCING)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _inspectedCourse = MutableStateFlow<Course?>(null)
    val inspectedCourse: StateFlow<Course?> = _inspectedCourse.asStateFlow()

    private val _selectedDate = MutableStateFlow<DateComponents?>(null)
    val selectedDate: StateFlow<DateComponents?> = _selectedDate.asStateFlow()

    val courses: StateFlow<List<Course>> =
        repository.allCourses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allAssignmentsUi: StateFlow<List<AssignmentUiModel>> =
        combine(repository.allAssignments, courses) { allAssignments, allCourses ->
            allAssignments.map { assignment ->
                val course = allCourses.find { it.id == assignment.courseId }
                AssignmentUiModel(assignment, course?.name ?: "Curso")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Entregas urgentes: hoy y mañana (en tiempo local)
    val urgentAssignments: StateFlow<List<AssignmentUiModel>> =
        allAssignmentsUi.map { list ->
            val nowMillis = DateTimeUtil.nowEpochMillis()
            val today = DateTimeUtil.nowLocalDate()
            val tomorrow = DateTimeUtil.addDays(today, 1)

            list.filter { ui ->
                val a = ui.assignment
                if (a.status != "upcoming") return@filter false
                val dueRaw = a.dueAt ?: a.lockAt ?: return@filter false
                val components = DateTimeUtil.parseIsoToComponents(dueRaw) ?: return@filter false
                val dueDate = components.date
                (dueDate == today || dueDate == tomorrow) && components.epochMillis >= nowMillis
            }.sortedBy { ui ->
                val dueRaw = ui.assignment.dueAt ?: ui.assignment.lockAt ?: ""
                DateTimeUtil.parseIsoToComponents(dueRaw)?.epochMillis ?: Long.MAX_VALUE
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cronograma semanal de 7 días (desde el lunes de la semana actual)
    val weeklySchedule: StateFlow<List<DaySchedule>> =
        repository.allAssignments.map { allAssignments ->
            val today = DateTimeUtil.nowLocalDate()
            val dayOfWeek = DateTimeUtil.dayOfWeek(today) // 1 = LUN ... 7 = DOM
            val startOfWeek = DateTimeUtil.addDays(today, -(dayOfWeek - 1))

            (0..6).map { dayOffset ->
                val date = DateTimeUtil.addDays(startOfWeek, dayOffset)
                val count = allAssignments.count { a ->
                    if (a.dueAt == null) return@count false
                    val comp = DateTimeUtil.parseIsoToComponents(a.dueAt) ?: return@count false
                    comp.date == date
                }
                DaySchedule(
                    date = date,
                    dayName = DateTimeUtil.dayOfWeekName(date),
                    dayNumber = date.day,
                    taskCount = count,
                    isToday = date == today
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tareas para la fecha seleccionada en el cronograma semanal
    val selectedDateAssignments: StateFlow<List<AssignmentUiModel>> =
        combine(allAssignmentsUi, _selectedDate) { all, date ->
            if (date == null) emptyList()
            else all.filter { ui ->
                val due = ui.assignment.dueAt ?: return@filter false
                val comp = DateTimeUtil.parseIsoToComponents(due) ?: return@filter false
                comp.date == date
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseStats: StateFlow<List<CourseStat>> =
        combine(courses, repository.allAssignments) { courseList, assignmentList ->
            courseList.map { course ->
                val courseAssignments = assignmentList.filter { it.courseId == course.id }
                val pending = courseAssignments.count { it.status == "upcoming" }
                val completed = courseAssignments.count { it.status == "completed" }
                val missing = courseAssignments.count { it.status == "missing" }

                val scores = courseAssignments.mapNotNull { it.score }
                val avg = if (scores.isNotEmpty()) scores.average() else null

                CourseStat(
                    course = course,
                    pendingCount = pending,
                    completedCount = completed,
                    missingCount = missing,
                    averageScore = avg
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionName: StateFlow<String> = repository.universityUrl.map { url ->
        if (url.isNullOrBlank()) "CANVAS"
        else {
            val clean = url.removePrefix("https://").removePrefix("http://").trim()
            val host = clean.substringBefore('/')
            host.substringBefore('.').trim().uppercase()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "CANVAS")

    init {
        initialSync()
    }

    private fun initialSync() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            try {
                repository.getProfile()
                repository.fetchAndSaveData()
                _syncStatus.value = SyncStatus.SYNCED
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.FAILED
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                repository.getProfile()
            } catch (_: Exception) { }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _syncStatus.value = SyncStatus.SYNCING
            try {
                repository.fetchAndSaveData()
                repository.getProfile()
                _syncStatus.value = SyncStatus.SYNCED
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.FAILED
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun selectDate(date: DateComponents?) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun inspectCourse(course: Course?) {
        _inspectedCourse.value = course
    }
}
