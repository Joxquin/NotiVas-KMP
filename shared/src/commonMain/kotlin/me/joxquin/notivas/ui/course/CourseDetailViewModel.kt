package me.joxquin.notivas.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.joxquin.notivas.util.AppDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.CanvasModuleItem
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase
import kotlin.math.roundToInt

data class CourseModuleItemUi(
    val id: Long,
    val title: String,
    val type: String,
    val isCompleted: Boolean,
    val isPending: Boolean,
    val metaSubtitle: String,
    val dueSubtitle: String? = null,
    val pointsText: String? = null,
    val assignmentId: Long? = null,
    val contentId: Long? = null,
    val actionUrl: String? = null,
    val fileExtension: String? = null
)

data class CourseModuleSectionUi(
    val title: String,
    val items: List<CourseModuleItemUi>
)

data class CourseModuleUi(
    val id: Long,
    val name: String,
    val badgeLabel: String,
    val description: String,
    val sections: List<CourseModuleSectionUi>,
    val allItems: List<CourseModuleItemUi>,
    val previewItems: List<CourseModuleItemUi> = emptyList(),
    val items: List<CourseModuleItemUi> = allItems,
    val completedDeliverablesCount: Int,
    val totalDeliverablesCount: Int,
    val progressPercentage: Int,
    val isCompleted: Boolean,
    val isLocked: Boolean = false,
    val unlockAtFormatted: String? = null,
    val isExpanded: Boolean = false
)

data class CourseDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val course: Course? = null,
    val professorName: String = "Prof. Marco Alarcón",
    val departmentTag: String = "DESARROLLO DE SOFTWARE",
    val promedioFormatted: String = "S/C",
    val avancePercentage: Int = 0,
    val completedDeliverablesCount: Int = 0,
    val totalDeliverablesCount: Int = 0,
    val completedModulesCount: Int = 0,
    val totalModulesCount: Int = 0,
    val modules: List<CourseModuleUi> = emptyList(),
    val activeModule: CourseModuleUi? = null,
    val latestAnnouncement: CanvasDiscussionTopic? = null,
    val unreadAnnouncementsCount: Int = 0,
    val coachTip: String = "",
    val studentsCount: Int = 0,
    val studentAvatars: List<UserProfile> = emptyList(),
    val error: String? = null
)

class CourseDetailViewModel(
    private val courseId: Long,
    private val repository: CanvasRepository,
    private val academicCalculatorUseCase: AcademicCalculatorUseCase = AcademicCalculatorUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState(isLoading = true))
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseDetail()
    }

    fun loadCourseDetail(isRefresh: Boolean = false) {
        viewModelScope.launch(AppDispatchers.IO) {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            try {
                // 1. Obtener curso de la base de datos o preferencias
                val allCourses = repository.allCourses.firstOrNull().orEmpty()
                val course = allCourses.find { it.id == courseId }

                // 2. Obtener tareas y grupos de simulación para cálculo de promedio
                val assignments = repository.getAssignmentsForCourse(courseId).firstOrNull().orEmpty()
                val groupsWithItems = repository.getSimulationGroupsWithItems(courseId).firstOrNull().orEmpty()

                val summary = if (course != null && groupsWithItems.isNotEmpty()) {
                    academicCalculatorUseCase.calculateCourseSummary(course, groupsWithItems)
                } else null

                val promedioStr = if (summary?.currentAverage != null) {
                    val rounded = (summary.currentAverage * 10f).roundToInt() / 10f
                    rounded.toString()
                } else {
                    val gradedAssignments = assignments.filter { it.score != null }
                    if (gradedAssignments.isNotEmpty()) {
                        val avg = gradedAssignments.map { (it.score!! / (it.pointsPossible ?: 20.0).coerceAtLeast(1.0)) * 20.0 }.average()
                        ((avg * 10.0).roundToInt() / 10.0).toString()
                    } else {
                        "S/C"
                    }
                }

                // 3. Módulos de Canvas
                val rawModules = repository.fetchCourseModules(courseId)
                val mappedModules = mapModules(rawModules, assignments)

                // Contadores globales de entregables y módulos
                val totalDeliverables = mappedModules.sumOf { it.totalDeliverablesCount }
                val completedDeliverables = mappedModules.sumOf { it.completedDeliverablesCount }
                val avancePct = if (totalDeliverables > 0) {
                    ((completedDeliverables.toFloat() / totalDeliverables.toFloat()) * 100f).roundToInt()
                } else if (assignments.isNotEmpty()) {
                    val done = assignments.count { it.isCompleted || it.status == "completed" }
                    ((done.toFloat() / assignments.size.toFloat()) * 100f).roundToInt()
                } else {
                    0
                }

                val completedModules = mappedModules.count { it.totalDeliverablesCount > 0 && it.completedDeliverablesCount >= it.totalDeliverablesCount }
                val totalModules = mappedModules.size

                // 4. Anuncios del curso
                val announcements = repository.fetchCourseAnnouncements(courseId)
                val latestAnnouncement = announcements.firstOrNull()
                val unreadCount = announcements.sumOf { it.unreadCount ?: 0 }

                // 5. Estudiantes / Compañeros
                val students = repository.fetchCourseUsers(courseId)
                val studentCount = if (students.isNotEmpty()) students.size else 28

                // 6. Generar consejo contextual del Coach
                val coachTip = generateCoachTip(course?.name ?: "", assignments, avancePct, promedioStr)

                // 7. Etiqueta de carrera / código
                val codeTag = course?.courseCode?.takeIf { it.isNotBlank() } ?: "C7 - DESARROLLO DE SOFTWARE"

                // Seleccionar el último módulo desbloqueado que tenga contenido (o el último disponible)
                val activeModule = mappedModules.lastOrNull { !it.isLocked && it.allItems.isNotEmpty() }
                    ?: mappedModules.lastOrNull { !it.isLocked }
                    ?: mappedModules.lastOrNull()

                _uiState.value = CourseDetailUiState(
                    isLoading = false,
                    isRefreshing = false,
                    course = course,
                    professorName = "Prof. Marco Alarcón",
                    departmentTag = codeTag,
                    promedioFormatted = promedioStr,
                    avancePercentage = avancePct,
                    completedDeliverablesCount = completedDeliverables,
                    totalDeliverablesCount = totalDeliverables,
                    completedModulesCount = completedModules,
                    totalModulesCount = totalModules,
                    modules = mappedModules,
                    activeModule = activeModule,
                    latestAnnouncement = latestAnnouncement,
                    unreadAnnouncementsCount = unreadCount,
                    coachTip = coachTip,
                    studentsCount = studentCount,
                    studentAvatars = students.take(4),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleModuleExpanded(moduleId: Long) {
        val updated = _uiState.value.modules.map { mod ->
            if (mod.id == moduleId) mod.copy(isExpanded = !mod.isExpanded)
            else mod
        }
        _uiState.value = _uiState.value.copy(modules = updated)
    }

    private fun mapModules(
        modules: List<CanvasModule>,
        assignments: List<Assignment>
    ): List<CourseModuleUi> {
        val assignmentMap = assignments.associateBy { it.id }

        return modules.mapIndexed { index, module ->
            val items = module.items.orEmpty()
            var deliverablesCount = 0
            var completedCount = 0

            val sections = mutableListOf<CourseModuleSectionUi>()
            var currentSectionTitle = ""
            var currentSectionItems = mutableListOf<CourseModuleItemUi>()
            val allNonSubheaderItems = mutableListOf<CourseModuleItemUi>()

            for (item in items) {
                if (item.type.equals("SubHeader", ignoreCase = true)) {
                    if (currentSectionItems.isNotEmpty()) {
                        sections.add(CourseModuleSectionUi(title = currentSectionTitle, items = currentSectionItems))
                        currentSectionItems = mutableListOf()
                    }
                    currentSectionTitle = item.title
                } else {
                    val matchingAssignment = if (item.type.equals("Assignment", ignoreCase = true)) {
                        item.contentId?.let { assignmentMap[it] } ?: assignments.find { it.name.equals(item.title, ignoreCase = true) }
                    } else null

                    val isCompleted = matchingAssignment?.isCompleted == true ||
                            matchingAssignment?.status == "completed"

                    val isPending = matchingAssignment != null && !isCompleted

                    if (item.type.equals("Assignment", ignoreCase = true) || item.type.equals("Quiz", ignoreCase = true)) {
                        deliverablesCount++
                        if (isCompleted) completedCount++
                    }

                    val ext = when {
                        item.title.endsWith(".pdf", ignoreCase = true) -> "PDF"
                        item.title.endsWith(".docx", ignoreCase = true) || item.title.endsWith(".doc", ignoreCase = true) -> "DOCX"
                        item.title.endsWith(".xlsx", ignoreCase = true) -> "XLSX"
                        item.title.endsWith(".zip", ignoreCase = true) -> "ZIP"
                        else -> null
                    }

                    val meta = when {
                        ext != null -> "$ext • ${if (isCompleted) "Completado" else "Disponible"}"
                        matchingAssignment != null -> if (isCompleted) "Entregado en Canvas" else "Pendiente de subida"
                        item.type.equals("Page", ignoreCase = true) -> "Documento informativo"
                        item.type.equals("ExternalUrl", ignoreCase = true) -> "Enlace externo institucional"
                        item.type.equals("Discussion", ignoreCase = true) -> "Foro de debate académico"
                        else -> "Recurso del módulo"
                    }

                    val dueText = matchingAssignment?.dueAt?.let { due ->
                        val comp = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(due)
                        if (comp != null) "Vence ${comp.date.day}/${comp.date.month} ${comp.hour}:${comp.minute.toString().padStart(2, '0')}"
                        else null
                    }

                    val points = matchingAssignment?.pointsPossible?.let {
                        "${it.roundToInt()} pts"
                    }

                    val itemUi = CourseModuleItemUi(
                        id = item.id,
                        title = item.title,
                        type = item.type,
                        isCompleted = isCompleted,
                        isPending = isPending,
                        metaSubtitle = meta,
                        dueSubtitle = dueText,
                        pointsText = points,
                        assignmentId = matchingAssignment?.id,
                        contentId = item.contentId,
                        actionUrl = item.htmlUrl ?: item.url ?: item.externalUrl,
                        fileExtension = ext
                    )
                    currentSectionItems.add(itemUi)
                    allNonSubheaderItems.add(itemUi)
                }
            }
            if (currentSectionItems.isNotEmpty()) {
                sections.add(CourseModuleSectionUi(title = currentSectionTitle, items = currentSectionItems))
            }

            val nowMs = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
            val unlockComp = module.unlockAt?.let { me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(it) }
            val unlockEpochMs = unlockComp?.epochMillis
            val isLockedByDate = unlockEpochMs != null && unlockEpochMs > nowMs
            val isLocked = module.state.equals("locked", ignoreCase = true) || isLockedByDate

            val unlockFormatted = if (isLocked && unlockComp != null) {
                "${unlockComp.date.day}/${unlockComp.date.month} ${unlockComp.hour}:${unlockComp.minute.toString().padStart(2, '0')}"
            } else null

            val progressPct = if (deliverablesCount > 0) {
                ((completedCount.toFloat() / deliverablesCount.toFloat()) * 100f).roundToInt()
            } else 100

            val isModuleCompleted = !isLocked && if (deliverablesCount > 0) {
                completedCount >= deliverablesCount
            } else {
                allNonSubheaderItems.isNotEmpty() && allNonSubheaderItems.none { it.isPending }
            }

            // Generar lista de previsualización para la tarjeta resumen de Ruta de Aprendizaje:
            // Priorizar entregables (pendientes primero, luego completados), luego otros recursos
            val deliverableItems = allNonSubheaderItems.filter {
                it.type.equals("Assignment", ignoreCase = true) ||
                it.type.equals("Quiz", ignoreCase = true) ||
                it.assignmentId != null
            }
            val pendingDeliverables = deliverableItems.filter { it.isPending }
            val completedDeliverables = deliverableItems.filter { it.isCompleted }
            val otherItems = allNonSubheaderItems.filter { it !in deliverableItems }
            val modulePreviewItems = (pendingDeliverables + completedDeliverables + otherItems).take(2)

            // Generar título de semana o unidad
            val weekTag = if (module.name.startsWith("Semana", ignoreCase = true)) {
                module.name.substringBefore(":").substringBefore("-").trim()
            } else "Semana ${index + 1}"

            val description = ""

            CourseModuleUi(
                id = module.id,
                name = module.name,
                badgeLabel = weekTag,
                description = description,
                sections = sections,
                allItems = allNonSubheaderItems,
                previewItems = modulePreviewItems,
                completedDeliverablesCount = completedCount,
                totalDeliverablesCount = deliverablesCount,
                progressPercentage = progressPct,
                isCompleted = isModuleCompleted,
                isLocked = isLocked,
                unlockAtFormatted = unlockFormatted,
                isExpanded = false
            )
        }
    }

    private fun generateCoachTip(
        courseName: String,
        assignments: List<Assignment>,
        avancePct: Int,
        promedioStr: String
    ): String {
        val pendingAssignments = assignments.filter { it.status == "upcoming" && !it.isCompleted }
        val nextDue = pendingAssignments.minByOrNull { it.dueAt ?: "" }

        return when {
            nextDue != null -> {
                "Tienes una entrega próxima de \"${nextDue.name.take(30)}...\". Te recomiendo revisar la rúbrica y verificar casos límite antes de enviar."
            }
            avancePct >= 80 -> {
                "Excelente ritmo de estudio en este curso. Mantén el estándar en tus entregables finales para consolidar tu nota de $promedioStr."
            }
            promedioStr != "S/C" -> {
                "Tu promedio actual en $courseName es de $promedioStr. Revisa los módulos semanales para mantener tu nivel aprobatorio."
            }
            else -> {
                "Tu cobertura de pruebas y comprensión técnica avanzan favorablemente. Revisa el patrón AAA y las guías de laboratorio."
            }
        }
    }
}
