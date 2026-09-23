package me.joxquin.notivas.ui.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.repository.CanvasRepository
import kotlin.math.roundToInt

data class SimEvaluation(
    val id: Long,
    val name: String,
    val weight: Float,
    val isGraded: Boolean,
    val actualScore: Float?,
    val simulatedScore: Float,
    val pointsPossible: Double?,
    val gradedAt: String? = null,
    val dueAt: String? = null
)

enum class RiskLevel {
    Safe, ModerateRisk, HighRisk
}

data class SimulationGroupUiModel(
    val group: SimulationGroup,
    val items: List<SimulationItem>,
    val groupAverage: Float = 0f
)

data class NotasUiState(
    val courses: List<Course> = emptyList(),
    val selectedCourse: Course? = null,
    val evaluations: List<SimEvaluation> = emptyList(),
    val currentAverage: Float = 0f,
    val evaluatedProgressRatio: Float = 0f,
    val projectedFinalGrade: Float = 0f,
    val riskLevel: RiskLevel = RiskLevel.Safe,
    val simulationGroups: List<SimulationGroupUiModel> = emptyList(),
    val totalConfiguredWeight: Float = 0f,
    val groupCurrentAverage: Float = 0f,
    val groupEvaluatedProgressRatio: Float = 0f,
    val groupSimulatedFinalGrade: Float = 0f,
    val availableCourseAssignments: List<Assignment> = emptyList(),
    val latestGradedAssignmentName: String? = null,
    val latestGradedScoreFormatted: String? = null,
    val latestGradedCourseName: String? = null
)

class NotasViewModel(
    private val repository: CanvasRepository
) : ViewModel() {

    private val _selectedCourse = MutableStateFlow<Course?>(null)

    val courses: StateFlow<List<Course>> =
        repository.allCourses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<Assignment>> =
        repository.allAssignments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NotasUiState> = combine(
        courses,
        _selectedCourse,
        assignments
    ) { courseList: List<Course>, selectedCourse: Course?, allAssignments: List<Assignment> ->
        val activeCourse = selectedCourse ?: courseList.firstOrNull()

        // Obtener la última tarea calificada a nivel global
        val globalLatestGraded = allAssignments
            .filter { it.score != null }
            .sortedByDescending { it.gradedAt ?: it.dueAt ?: "" }
            .firstOrNull()

        val globalLatestCourse = courseList.find { it.id == globalLatestGraded?.courseId }
        val latestName = globalLatestGraded?.name
        val latestScoreFormatted = if (globalLatestGraded?.score != null) {
            val scoreVal = ((globalLatestGraded.score / (globalLatestGraded.pointsPossible ?: 20.0).coerceAtLeast(1.0)) * 20.0)
            val rounded = (scoreVal * 10.0).roundToInt() / 10.0
            "$rounded / 20 pts"
        } else null

        NotasUiState(
            courses = courseList,
            selectedCourse = activeCourse,
            latestGradedAssignmentName = latestName,
            latestGradedScoreFormatted = latestScoreFormatted,
            latestGradedCourseName = globalLatestCourse?.name ?: globalLatestCourse?.courseCode ?: activeCourse?.name
        )
    }.flatMapLatest { baseState ->
        val courseId = baseState.selectedCourse?.id
        if (courseId == null) {
            flowOf(baseState)
        } else {
            combine(
                repository.getAssignmentsForCourse(courseId),
                repository.getSimulationGroupsWithItems(courseId)
            ) { assignments, groupsWithItems ->
                val evals = assignments.map { a ->
                    val isGraded = a.score != null
                    val rawScore = a.score?.toFloat() ?: 0f
                    val maxPoints = (a.pointsPossible ?: 20.0).toFloat().coerceAtLeast(1f)
                    val normalizedScore = (rawScore / maxPoints) * 20f

                    SimEvaluation(
                        id = a.id,
                        name = a.name,
                        weight = 0f,
                        isGraded = isGraded,
                        actualScore = if (isGraded) normalizedScore else null,
                        simulatedScore = if (isGraded) normalizedScore else 0f,
                        pointsPossible = a.pointsPossible,
                        gradedAt = a.gradedAt,
                        dueAt = a.dueAt
                    )
                }

                val graded = evals.filter { it.isGraded }
                val currentAvg = if (graded.isNotEmpty()) graded.mapNotNull { it.actualScore }.average().toFloat() else 0f
                val progressRatio = if (evals.isNotEmpty()) graded.size.toFloat() / evals.size.toFloat() else 0f

                // Cálculo de grupos y porcentajes
                var totalWeight = 0f
                var weightedSum = 0f
                val groupUiList = groupsWithItems.map { g ->
                    val weight = g.group.weightPercentage
                    totalWeight += weight
                    val avg = if (g.items.isNotEmpty()) g.items.map { it.simulatedScore }.average().toFloat() else 0f
                    weightedSum += (avg * (weight / 100f))
                    SimulationGroupUiModel(
                        group = g.group,
                        items = g.items,
                        groupAverage = avg
                    )
                }
                val groupSimulatedFinal = if (totalWeight > 0f) (weightedSum / (totalWeight / 100f)) else currentAvg

                // Si el curso actual tiene tareas calificadas, actualizamos el latest graded para ser contextual
                val courseGraded = graded.sortedByDescending { it.gradedAt ?: it.dueAt ?: "" }.firstOrNull()
                val courseLatestName = courseGraded?.name ?: baseState.latestGradedAssignmentName
                val courseLatestScore = if (courseGraded?.actualScore != null) {
                    val rounded = (courseGraded.actualScore * 10f).roundToInt() / 10f
                    "$rounded / 20 pts"
                } else baseState.latestGradedScoreFormatted

                baseState.copy(
                    evaluations = evals,
                    currentAverage = currentAvg,
                    evaluatedProgressRatio = progressRatio,
                    projectedFinalGrade = if (groupUiList.isNotEmpty()) groupSimulatedFinal else currentAvg,
                    riskLevel = if (currentAvg >= 14f) RiskLevel.Safe else if (currentAvg >= 10.5f) RiskLevel.ModerateRisk else RiskLevel.HighRisk,
                    simulationGroups = groupUiList,
                    totalConfiguredWeight = totalWeight,
                    groupCurrentAverage = currentAvg,
                    groupEvaluatedProgressRatio = progressRatio,
                    groupSimulatedFinalGrade = groupSimulatedFinal,
                    availableCourseAssignments = assignments,
                    latestGradedAssignmentName = courseLatestName,
                    latestGradedScoreFormatted = courseLatestScore,
                    latestGradedCourseName = baseState.selectedCourse?.name ?: baseState.latestGradedCourseName
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotasUiState())

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun applyTemplate(template: me.joxquin.notivas.domain.template.CourseEvaluationTemplate) {
        val course = _selectedCourse.value ?: return
        viewModelScope.launch {
            repository.applyTemplate(course, template)
        }
    }

    fun addSimulationGroup(name: String, weight: Float) {
        val courseId = _selectedCourse.value?.id ?: return
        viewModelScope.launch {
            repository.createSimulationGroup(
                SimulationGroup(courseId = courseId, name = name, weightPercentage = weight)
            )
        }
    }

    fun createGroupWithItems(
        name: String,
        weight: Float,
        targetAssessments: Int,
        dropLowest: Boolean,
        minToDrop: Int
    ) {
        val courseId = _selectedCourse.value?.id ?: return
        viewModelScope.launch {
            val group = SimulationGroup(
                courseId = courseId,
                name = name,
                weightPercentage = weight,
                targetAssessments = targetAssessments,
                dropLowest = dropLowest,
                minToDrop = minToDrop
            )
            val items = (1..targetAssessments).map { i ->
                SimulationItem(
                    groupId = 0L,
                    name = "$name $i",
                    isPlaceholder = true,
                    simulatedScore = 15f,
                    isSimulated = false,
                    maxScore = 20f,
                    orderIndex = i - 1
                )
            }
            repository.createGroupWithItems(group, items)
        }
    }

    fun updateSimulationGroup(group: SimulationGroup) {
        viewModelScope.launch {
            repository.updateSimulationGroup(group)
        }
    }

    fun addExistingAssignmentToGroup(groupId: Long, assignment: Assignment) {
        viewModelScope.launch {
            val rawScore = assignment.score?.toFloat() ?: assignment.submission?.score?.toFloat()
            val maxPoints = (assignment.pointsPossible ?: 20.0).toFloat().coerceAtLeast(1f)
            val normalizedScore = if (rawScore != null) (rawScore / maxPoints) * 20f else null

            repository.addSimulationItem(
                SimulationItem(
                    groupId = groupId,
                    canvasAssignmentId = assignment.id,
                    name = assignment.name,
                    isPlaceholder = false,
                    manualScore = normalizedScore,
                    simulatedScore = normalizedScore ?: 15f,
                    isSimulated = false,
                    maxScore = 20f
                )
            )
        }
    }

    fun addPlaceholderItemToGroup(groupId: Long, name: String, estimatedScore: Float) {
        viewModelScope.launch {
            repository.addSimulationItem(
                SimulationItem(
                    groupId = groupId,
                    canvasAssignmentId = null,
                    name = name,
                    isPlaceholder = true,
                    manualScore = null,
                    simulatedScore = estimatedScore,
                    isSimulated = true,
                    maxScore = 20f
                )
            )
        }
    }

    fun linkSimulationItem(itemId: Long, assignmentId: Long, name: String, manualScore: Float? = null) {
        viewModelScope.launch {
            repository.linkSimulationItemWithCanvas(itemId, assignmentId, name, manualScore)
        }
    }

    fun addSimulationItem(groupId: Long, name: String, score: Float = 0f, maxScore: Float = 20f) {
        viewModelScope.launch {
            repository.addSimulationItem(
                SimulationItem(groupId = groupId, name = name, simulatedScore = score, maxScore = maxScore)
            )
        }
    }

    fun updateSimulationItemScore(itemId: Long, score: Float) {
        viewModelScope.launch {
            repository.updateSimulationItemScore(itemId, score)
        }
    }

    fun deleteSimulationGroup(group: SimulationGroup) {
        viewModelScope.launch {
            repository.deleteSimulationGroup(group)
        }
    }

    fun deleteSimulationItem(item: SimulationItem) {
        viewModelScope.launch {
            repository.deleteSimulationItem(item)
        }
    }
}
