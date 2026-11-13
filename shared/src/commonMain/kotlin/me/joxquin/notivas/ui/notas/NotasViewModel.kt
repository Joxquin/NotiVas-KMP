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
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.di.AppModule
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.math.roundToInt

@Serializable
data class CopilotProposedItem(
    val name: String,
    val maxScore: Float = 20f,
    val canvasAssignmentId: Long? = null
)

@Serializable
data class CopilotProposedGroup(
    val name: String,
    val weightPercentage: Float,
    val items: List<CopilotProposedItem> = emptyList()
)

@Serializable
data class CopilotAnalysisResult(
    val groups: List<CopilotProposedGroup> = emptyList(),
    val reasoning: String = ""
)

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
    private val repository: CanvasRepository,
    private val preferencesManager: PreferencesManager = AppModule.preferencesManager,
    private val copilotRepository: CopilotRepository = AppModule.copilotRepository
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
            ) { assignments: List<Assignment>, groupsWithItems: List<SimulationGroupWithItems> ->
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

                val simulatedProjection = currentAvg
                val groupSimulatedFinal = if (totalWeight > 0f) (weightedSum / (totalWeight / 100f)) else simulatedProjection

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
                    projectedFinalGrade = if (groupUiList.isNotEmpty()) groupSimulatedFinal else simulatedProjection,
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

    private fun getTargetCourse(): Course? {
        return _selectedCourse.value ?: uiState.value.selectedCourse ?: uiState.value.courses.firstOrNull()
    }

    fun applyTemplate(template: me.joxquin.notivas.domain.template.CourseEvaluationTemplate) {
        val course = getTargetCourse() ?: return
        viewModelScope.launch {
            repository.applyTemplate(course, template)
        }
    }

    fun applyTemplateToAllCourses(
        template: me.joxquin.notivas.domain.template.CourseEvaluationTemplate,
        onResult: (count: Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val count = repository.applyTemplateToAllCourses(template)
            onResult(count)
        }
    }

    fun exportGlobalGroups(onExportReady: (json: String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportGlobalGroupsToJson()
            onExportReady(json)
        }
    }

    fun importGlobalGroupsFromJson(
        jsonContent: String,
        onResult: (Result<Int>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.importGlobalGroupsFromJson(jsonContent)
            onResult(result)
        }
    }

    fun resetAllSimulationGroups(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetAllSimulationGroups()
            onComplete()
        }
    }

    fun addSimulationGroup(name: String, weight: Float) {
        val courseId = getTargetCourse()?.id ?: return
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
        val courseId = getTargetCourse()?.id ?: return
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

    fun applyCopilotProposedGroups(courseId: Long, groups: List<CopilotProposedGroup>) {
        viewModelScope.launch {
            groups.forEachIndexed { groupIdx, proposed ->
                val group = SimulationGroup(
                    courseId = courseId,
                    name = proposed.name,
                    weightPercentage = proposed.weightPercentage,
                    targetAssessments = proposed.items.size.coerceAtLeast(1),
                    orderIndex = groupIdx
                )
                val items = proposed.items.mapIndexed { idx, itm ->
                    SimulationItem(
                        groupId = 0L,
                        canvasAssignmentId = itm.canvasAssignmentId,
                        name = itm.name,
                        isPlaceholder = itm.canvasAssignmentId == null,
                        simulatedScore = 15f,
                        isSimulated = false,
                        maxScore = itm.maxScore,
                        orderIndex = idx
                    )
                }
                repository.createGroupWithItems(group, items)
            }
        }
    }

    fun analyzeCourseWithCopilot(
        courseId: Long,
        userPromptOverride: String? = null,
        onSuccess: (CopilotAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val activeCourse = uiState.value.courses.find { it.id == courseId } ?: getTargetCourse()
                val courseName = activeCourse?.name ?: "Curso"
                val courseAssignments = repository.getAssignmentsForCourse(courseId).firstOrNull() ?: emptyList()

                val assignmentSummaries = courseAssignments.joinToString("\n") { 
                    "- ID: ${it.id}, Nombre: \"${it.name}\", Puntos: ${it.pointsPossible ?: 20.0}" 
                }

                val prompt = buildString {
                    append("Eres un asistente universitario experto. Analiza las siguientes evaluaciones del curso '")
                    append(courseName)
                    append("' y organízalas en grupos de evaluación académicos lógicos (ej. Evaluaciones Continuas, Examen Parcial, Examen Final, Laboratorios o Proyectos).\n")
                    if (!userPromptOverride.isNullOrBlank()) {
                        append("Requerimientos e instrucciones específicas del estudiante:\n\"")
                        append(userPromptOverride.trim())
                        append("\"\n")
                    }
                    append("Reglas estrictas:\n")
                    append("1. Las ponderaciones de los grupos deben sumar exactamente 100.0%.\n")
                    append("2. Incluye las evaluaciones encontradas de Canvas LMS dentro de sus respectivos grupos con su nombre y canvas_assignment_id.\n")
                    append("3. En el campo 'reasoning', explica de forma clara, concisa y profesional tu criterio académico y por qué decidiste agrupar y ponderar las evaluaciones de esta manera.\n")
                    append("4. Responde únicamente con un objeto JSON válido con la siguiente estructura, sin texto adicional ni bloques explicativos fuera del JSON:\n")
                    append("{\"reasoning\": \"Breve justificación del criterio de ponderación y agrupación\", \"groups\": [{\"name\": \"Nombre Grupo\", \"weight\": 30.0, \"items\": [{\"name\": \"Tarea 1\", \"max_score\": 20.0, \"canvas_assignment_id\": 12345}]}]}")
                    if (assignmentSummaries.isNotBlank()) {
                        append("\n\nTareas encontradas en Canvas LMS:\n")
                        append(assignmentSummaries)
                    }
                }

                val result = copilotRepository.queryCopilot(
                    history = emptyList(),
                    userPrompt = prompt,
                    selectedCourseId = courseId
                )

                val reply = result.getOrNull()?.reply.orEmpty()
                val parsedResult = if (reply.isNotBlank()) parseCopilotGroupsJson(reply, courseAssignments) else null
                if (parsedResult != null && parsedResult.groups.isNotEmpty()) {
                    onSuccess(parsedResult)
                } else {
                    val fallback = generateHeuristicGroups(courseName, courseAssignments)
                    onSuccess(fallback)
                }
            } catch (_: Exception) {
                val activeCourse = uiState.value.courses.find { it.id == courseId }
                val fallback = generateHeuristicGroups(activeCourse?.name ?: "Curso", emptyList())
                onSuccess(fallback)
            }
        }
    }

    private fun parseCopilotGroupsJson(
        reply: String,
        availableAssignments: List<Assignment>
    ): CopilotAnalysisResult? {
        return try {
            val cleanJson = reply
                .substringAfter("```json")
                .substringBefore("```")
                .trim()
                .ifEmpty { reply.trim() }

            val jsonElement = Json.parseToJsonElement(cleanJson)
            val jsonObject = jsonElement.jsonObject
            val reasoning = jsonObject["reasoning"]?.jsonPrimitive?.content.orEmpty()
            val groupsArray = jsonObject["groups"]?.jsonArray ?: return null

            var totalWeight = 0f
            val groups = groupsArray.mapNotNull { gObj ->
                val obj = gObj.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val weight = obj["weight"]?.jsonPrimitive?.doubleOrNull?.toFloat() ?: 0f
                val itemsArray = obj["items"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())

                val items = itemsArray.mapNotNull { iObj ->
                    val itm = iObj.jsonObject
                    val itmName = itm["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val maxScore = itm["max_score"]?.jsonPrimitive?.doubleOrNull?.toFloat() ?: 20f
                    val canvasId = itm["canvas_assignment_id"]?.jsonPrimitive?.longOrNull
                    CopilotProposedItem(
                        name = itmName,
                        maxScore = maxScore,
                        canvasAssignmentId = canvasId
                    )
                }

                totalWeight += weight
                CopilotProposedGroup(
                    name = name,
                    weightPercentage = weight,
                    items = items
                )
            }

            if (groups.isNotEmpty()) {
                val normalizedGroups = if (kotlin.math.abs(totalWeight - 100f) > 0.5f && totalWeight > 0f) {
                    val factor = 100f / totalWeight
                    groups.map { it.copy(weightPercentage = (it.weightPercentage * factor * 10f).roundToInt() / 10f) }
                } else {
                    groups
                }
                CopilotAnalysisResult(groups = normalizedGroups, reasoning = reasoning)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun generateHeuristicGroups(
        courseName: String,
        assignments: List<Assignment>
    ): CopilotAnalysisResult {
        if (assignments.isEmpty()) {
            return CopilotAnalysisResult(
                groups = listOf(
                    CopilotProposedGroup(
                        name = "Evaluación Continua",
                        weightPercentage = 40f,
                        items = listOf(
                            CopilotProposedItem("Práctica Calificada 1", 20f),
                            CopilotProposedItem("Práctica Calificada 2", 20f),
                            CopilotProposedItem("Trabajo Grupal", 20f)
                        )
                    ),
                    CopilotProposedGroup(
                        name = "Examen Parcial",
                        weightPercentage = 25f,
                        items = listOf(CopilotProposedItem("Examen Parcial", 20f))
                    ),
                    CopilotProposedGroup(
                        name = "Examen Final",
                        weightPercentage = 35f,
                        items = listOf(CopilotProposedItem("Examen Final", 20f))
                    )
                ),
                reasoning = "Estructura académica estándar para $courseName dividida en Evaluación Continua, Examen Parcial y Examen Final."
            )
        }

        val labs = mutableListOf<CopilotProposedItem>()
        val continuous = mutableListOf<CopilotProposedItem>()
        val exams = mutableListOf<CopilotProposedItem>()
        val other = mutableListOf<CopilotProposedItem>()

        assignments.forEach { a ->
            val lower = a.name.lowercase()
            val item = CopilotProposedItem(
                name = a.name,
                maxScore = (a.pointsPossible ?: 20.0).toFloat().coerceAtLeast(1f),
                canvasAssignmentId = a.id
            )
            when {
                lower.contains("lab") || lower.contains("taller") -> labs.add(item)
                lower.contains("parcial") || lower.contains("final") || lower.contains("examen") -> exams.add(item)
                lower.contains("pc") || lower.contains("práctica") || lower.contains("tarea") -> continuous.add(item)
                else -> other.add(item)
            }
        }

        val result = mutableListOf<CopilotProposedGroup>()
        val nonExams = labs + continuous + other
        if (nonExams.isNotEmpty()) {
            result.add(
                CopilotProposedGroup(
                    name = "Evaluaciones Continuas",
                    weightPercentage = if (exams.isNotEmpty()) 50f else 100f,
                    items = nonExams
                )
            )
        }
        if (exams.isNotEmpty()) {
            val parcial = exams.filter { it.name.contains("parcial", ignoreCase = true) }
            val finalExam = exams.filter { it.name.contains("final", ignoreCase = true) }
            val otherExams = exams.filter { it !in parcial && it !in finalExam }

            if (parcial.isNotEmpty() && finalExam.isNotEmpty()) {
                result.add(CopilotProposedGroup("Examen Parcial", 20f, parcial))
                result.add(CopilotProposedGroup("Examen Final", 30f, finalExam + otherExams))
            } else {
                result.add(CopilotProposedGroup("Exámenes", 50f, exams))
            }
        }

        if (result.isEmpty()) {
            result.add(
                CopilotProposedGroup(
                    name = "Evaluaciones del Curso",
                    weightPercentage = 100f,
                    items = assignments.map {
                        CopilotProposedItem(
                            it.name,
                            (it.pointsPossible ?: 20.0).toFloat().coerceAtLeast(1f),
                            it.id
                        )
                    }
                )
            )
        }

        return CopilotAnalysisResult(
            groups = result,
            reasoning = "Estructura calculada a partir de las ${assignments.size} tareas registradas en Canvas LMS, diferenciando evaluaciones continuas de los exámenes."
        )
    }
}
