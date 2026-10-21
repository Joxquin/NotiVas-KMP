package me.joxquin.notivas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.local.db.dao.SimulationDao
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.GradePerformanceLevel
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase
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
    val averageScore: Double?,
    val hasConfiguredGroups: Boolean = false,
    val evaluatedProgressPercentage: Float = 0f,
    val performanceLevel: GradePerformanceLevel = GradePerformanceLevel.AT_RISK
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
    val inspectedCourse: Course? = null,
    val unconfiguredCoursesCount: Int = 0,
    val firstUnconfiguredCourse: Course? = null,
    val isUnconfiguredBannerVisible: Boolean = false
)

class DashboardViewModel(
    private val repository: CanvasRepository = AppModule.canvasRepository,
    private val preferencesManager: PreferencesManager = AppModule.preferencesManager,
    private val simulationDao: SimulationDao = AppModule.simulationDao,
    private val academicCalculatorUseCase: AcademicCalculatorUseCase = AppModule.academicCalculatorUseCase
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

    private val _selectedAssignmentForDraft = MutableStateFlow<AssignmentUiModel?>(null)
    val selectedAssignmentForDraft: StateFlow<AssignmentUiModel?> = _selectedAssignmentForDraft.asStateFlow()

    private val googleDriveApiService = me.joxquin.notivas.data.remote.GoogleDriveApiService(
        preferencesManager = preferencesManager
    )

    // Efectos de interfaz unidireccionales (Apertura de URLs nativas, etc.)
    sealed interface UiEffect {
        data class OpenUrl(val url: String) : UiEffect
        data class ShowToast(val message: String) : UiEffect
    }

    private val _uiEffect = kotlinx.coroutines.flow.MutableSharedFlow<UiEffect>(extraBufferCapacity = 64)
    val uiEffect: kotlinx.coroutines.flow.SharedFlow<UiEffect> = _uiEffect

    // Observar borrador de la tarea activa
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentAssignmentDraft: StateFlow<me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo?> =
        _selectedAssignmentForDraft.flatMapLatest { ui: AssignmentUiModel? ->
            if (ui == null) flowOf(null)
            else repository.observeAssignmentDraft(ui.assignment.id).map { entity ->
                val isSubmitted = ui.assignment.status == "completed" ||
                        ui.assignment.isCompleted ||
                        (ui.assignment.submission?.workflowState in listOf("submitted", "graded")) ||
                        (!ui.assignment.submittedAt.isNullOrBlank()) ||
                        (!ui.assignment.submission?.submittedAt.isNullOrBlank())
                entity?.toDomain(isSubmitted)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun openDraft(assignmentUi: AssignmentUiModel) {
        _selectedAssignmentForDraft.value = assignmentUi
    }

    fun closeDraft() {
        _selectedAssignmentForDraft.value = null
    }

    fun onOpenDocsRequested(driveUrl: String) {
        if (driveUrl.isBlank() || driveUrl.contains("sample_")) {
            _uiEffect.tryEmit(UiEffect.ShowToast("El documento aún no está listo en Google Drive o no existe."))
            return
        }
        _uiEffect.tryEmit(UiEffect.OpenUrl(driveUrl))
    }

    /**
     * Creación / Clonación de Borrador en Google Drive.
     * Valida autenticación previa, gestiona creación real y emite mensajes claros de éxito y error.
     */
    private val _courseTemplates = MutableStateFlow<List<me.joxquin.notivas.data.model.TemplateCandidate>>(emptyList())
    val courseTemplates: StateFlow<List<me.joxquin.notivas.data.model.TemplateCandidate>> = _courseTemplates.asStateFlow()

    private val _showTemplatePicker = MutableStateFlow(false)
    val showTemplatePicker: StateFlow<Boolean> = _showTemplatePicker.asStateFlow()

    fun openTemplatePicker(courseId: Long, assignmentId: Long? = null, assignmentDescription: String? = null) {
        _showTemplatePicker.value = true
        loadTemplatesForCourse(courseId, assignmentId, assignmentDescription)
    }

    fun closeTemplatePicker() {
        _showTemplatePicker.value = false
    }

    private val _currentAssignmentModuleName = MutableStateFlow<String?>(null)
    val currentAssignmentModuleName: StateFlow<String?> = _currentAssignmentModuleName.asStateFlow()

    private val _isLoadingTemplates = MutableStateFlow(false)
    val isLoadingTemplates: StateFlow<Boolean> = _isLoadingTemplates.asStateFlow()

    fun loadTemplatesForCourse(courseId: Long, assignmentId: Long? = null, assignmentDescription: String? = null) {
        viewModelScope.launch {
            _isLoadingTemplates.value = true
            try {
                val candidates = repository.getCourseTemplateCandidates(courseId, assignmentId, assignmentDescription)
                _courseTemplates.value = candidates
                val match = candidates.find { it.isCurrentAssignmentModule }
                _currentAssignmentModuleName.value = match?.moduleName
            } catch (e: Exception) {
                e.printStackTrace()
                _courseTemplates.value = emptyList()
                _currentAssignmentModuleName.value = null
            } finally {
                _isLoadingTemplates.value = false
            }
        }
    }

    /**
     * Creación / Clonación de Borrador en Google Drive.
     * Soporta creación en blanco o clonación a partir de un TemplateCandidate (Drive URL o archivo de Canvas).
     */
    fun startOrCloneDraft(
        assignment: Assignment,
        courseName: String,
        templateName: String? = null,
        templateCandidate: me.joxquin.notivas.data.model.TemplateCandidate? = null,
        onOptimisticCompleted: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Verificar si Google Drive está vinculado
            if (!googleDriveApiService.hasValidAuth()) {
                _uiEffect.tryEmit(UiEffect.ShowToast("Debes vincular tu cuenta de Google Drive en Ajustes primero."))
                return@launch
            }

            val userEmail = preferencesManager.getGoogleDriveEmail() ?: "usuario@institucional.edu"
            val cleanAssignmentName = assignment.name.replace(Regex("""[^\w\s\-]"""), "").trim().replace(Regex("""\s+"""), "_").take(30)
            val matchingCourse = courses.value.find { it.id == assignment.courseId }
            val courseCodeClean = (matchingCourse?.courseCode ?: matchingCourse?.name ?: courseName)
                .replace(Regex("""[^\w\-]"""), "_")
                .trim('_')
                .ifBlank { "CURSO" }
                .take(16)

            val courseFolder = courseName.ifBlank { "General" }

            // Si se pasa templateName pero no templateCandidate, resolverlo dinámicamente desde el repositorio
            var effectiveCandidate = templateCandidate
            if (effectiveCandidate == null && !templateName.isNullOrBlank()) {
                val cachedCandidates = courseTemplates.value
                effectiveCandidate = cachedCandidates.find {
                    it.title.equals(templateName, ignoreCase = true) || it.title.contains(templateName, ignoreCase = true)
                }

                if (effectiveCandidate == null) {
                    try {
                        val fetchedCandidates = repository.getCourseTemplateCandidates(
                            courseId = assignment.courseId,
                            assignmentId = assignment.id,
                            assignmentDescription = assignment.description
                        )
                        _courseTemplates.value = fetchedCandidates
                        effectiveCandidate = fetchedCandidates.find {
                            it.title.equals(templateName, ignoreCase = true) ||
                                    it.title.contains(templateName.substringBeforeLast("."), ignoreCase = true) ||
                                    templateName.contains(it.title.substringBeforeLast("."), ignoreCase = true)
                        } ?: fetchedCandidates.firstOrNull { it.isCurrentAssignmentModule }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (effectiveCandidate == null) {
                    // Buscar en descripción si hay enlace directo de Google Drive
                    val desc = assignment.description ?: ""
                    val driveMatch = Regex("""https://(?:docs|drive)\.google\.com/[^\s"'>]+""").find(desc)?.value
                    if (driveMatch != null) {
                        effectiveCandidate = me.joxquin.notivas.data.model.TemplateCandidate(
                            title = templateName,
                            type = me.joxquin.notivas.data.model.TemplateSourceType.DRIVE_URL,
                            url = driveMatch,
                            moduleName = "Consigna",
                            fileExtension = if (driveMatch.contains("spreadsheets")) "gsheet" else "gdoc",
                            isCurrentAssignmentModule = true
                        )
                    }
                }
            }

            val templateDisplayName = effectiveCandidate?.title ?: templateName
            val isCloning = effectiveCandidate != null || !templateName.isNullOrBlank()
            val extension = if (effectiveCandidate?.fileExtension == "gsheet") "gsheet" else "gdoc"
            val generatedFileName = "${courseCodeClean}_${cleanAssignmentName}_${if (isCloning) "Copia" else "v1"}.$extension"

            // 1. Registro optimista sin URL ficticia (esperando respuesta de Drive)
            val optimisticInfo = me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo(
                fileName = generatedFileName,
                driveUrl = "",
                driveFileId = null,
                lastEditedTime = "Sincronizando...",
                accountEmail = userEmail,
                detectedTemplate = templateDisplayName,
                courseFolder = courseFolder,
                draftStatus = me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum.IN_PROGRESS,
                hasSubmitted = assignment.status == "completed" || !assignment.submittedAt.isNullOrBlank()
            )

            val optimisticEntity = me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity.fromDomain(
                assignmentId = assignment.id,
                info = optimisticInfo
            )
            repository.saveAssignmentDraft(optimisticEntity)
            onOptimisticCompleted()

            // 2. Creación / Clonación con timeout de 30 segundos
            try {
                val createdFile = kotlinx.coroutines.withTimeoutOrNull(30_000L) {
                    val folder = googleDriveApiService.getOrCreateCourseFolder(courseFolder)
                    val folderId = folder?.id ?: "root"

                    if (effectiveCandidate != null) {
                        when (effectiveCandidate.type) {
                            me.joxquin.notivas.data.model.TemplateSourceType.DRIVE_URL -> {
                                // Clonar directamente desde URL pública/compartida de Google Drive
                                val cloned = googleDriveApiService.cloneFromDriveUrlOrId(
                                    sourceUrlOrId = effectiveCandidate.url,
                                    newFileName = generatedFileName,
                                    parentFolderId = folderId
                                )
                                if (cloned == null) {
                                    _uiEffect.tryEmit(UiEffect.ShowToast("No se pudo clonar la plantilla. Verifica que estés conectado con la cuenta institucional que tiene acceso al enlace."))
                                }
                                cloned
                            }
                            me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_FILE -> {
                                // Obtener URL de descarga del archivo de Canvas y descargarlo
                                val downloadUrl = if (effectiveCandidate.canvasFileId != null) {
                                    repository.getCanvasFileDownloadUrl(assignment.courseId, effectiveCandidate.canvasFileId)
                                } else {
                                    effectiveCandidate.url
                                }

                                val fileBytes = if (!downloadUrl.isNullOrBlank()) {
                                    repository.downloadCanvasFileBytes(downloadUrl)
                                } else null

                                if (fileBytes != null && fileBytes.isNotEmpty()) {
                                    val isSheet = effectiveCandidate.fileExtension == "xlsx" || effectiveCandidate.fileExtension == "xls"
                                    val targetMime = if (isSheet) {
                                        me.joxquin.notivas.data.remote.GoogleDriveApiService.MIME_TYPE_GSHEET
                                    } else {
                                        me.joxquin.notivas.data.remote.GoogleDriveApiService.MIME_TYPE_GDOC
                                    }
                                    val sourceMime = if (isSheet) {
                                        me.joxquin.notivas.data.remote.GoogleDriveApiService.MIME_TYPE_XLSX
                                    } else {
                                        me.joxquin.notivas.data.remote.GoogleDriveApiService.MIME_TYPE_DOCX
                                    }

                                    googleDriveApiService.uploadAndConvertFile(
                                        fileName = generatedFileName,
                                        parentFolderId = folderId,
                                        fileBytes = fileBytes,
                                        targetMimeType = targetMime,
                                        sourceMimeType = sourceMime
                                    ) ?: googleDriveApiService.createBlankDocument(generatedFileName, folderId)
                                } else {
                                    // Si no se pudo descargar el archivo binario, crear documento base
                                    googleDriveApiService.createBlankDocument(generatedFileName, folderId)
                                }
                            }
                            me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_PAGE -> {
                                // Para páginas de Canvas, crear documento con referencia y título
                                googleDriveApiService.createBlankDocument(generatedFileName, folderId)
                            }
                        }
                    } else {
                        // Crear documento en blanco
                        googleDriveApiService.createBlankDocument(generatedFileName, folderId)
                    }
                }

                if (createdFile != null) {
                    val realUrl = createdFile.webViewLink ?: "https://docs.google.com/document/d/${createdFile.id}/edit"
                    val persistedInfo = optimisticInfo.copy(
                        driveFileId = createdFile.id,
                        driveUrl = realUrl,
                        lastEditedTime = "Sincronizado ahora"
                    )
                    repository.saveAssignmentDraft(
                        me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity.fromDomain(
                            assignmentId = assignment.id,
                            info = persistedInfo
                        )
                    )
                    _uiEffect.tryEmit(UiEffect.ShowToast("Borrador listo en Google Drive: $generatedFileName"))
                    _uiEffect.tryEmit(UiEffect.OpenUrl(realUrl))
                } else {
                    // Revertir borrador local porque falló la creación remota o expiró el tiempo
                    repository.deleteAssignmentDraft(assignment.id)
                    _uiEffect.tryEmit(UiEffect.ShowToast("No se pudo crear el documento en Google Drive. Revisa tu cuenta o conexión."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                repository.deleteAssignmentDraft(assignment.id)
                _uiEffect.tryEmit(UiEffect.ShowToast("Fallo de conexión con Google Drive: ${e.message ?: "desconocido"}"))
            }
        }
    }

    /**
     * Cancela la creación de un borrador y limpia el estado de sincronización local.
     */
    fun cancelDraft(assignmentId: Long) {
        viewModelScope.launch {
            repository.deleteAssignmentDraft(assignmentId)
            _uiEffect.tryEmit(UiEffect.ShowToast("Sincronización cancelada."))
        }
    }

    /**
     * Despacho Formal a Canvas LMS.
     * Envía la entrega al endpoint de Canvas, actualiza localmente y retira la tarea de la lista de urgentes.
     */
    fun submitDraftToCanvas(
        assignment: Assignment,
        driveUrl: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (driveUrl.isBlank() || driveUrl.contains("sample_")) {
            val errorMsg = "No se puede entregar: el enlace del borrador no es válido o no se ha sincronizado."
            _uiEffect.tryEmit(UiEffect.ShowToast(errorMsg))
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            val result = repository.submitAssignment(
                courseId = assignment.courseId,
                assignmentId = assignment.id,
                submissionType = "online_url",
                url = driveUrl,
                bodyText = "Entrega de borrador desde NotiVas: $driveUrl"
            )
            result.onSuccess {
                // Actualizar draft local a SUBMITTED
                val currentDraft = repository.getAssignmentDraft(assignment.id)
                if (currentDraft != null) {
                    repository.saveAssignmentDraft(
                        currentDraft.copy(
                            draftStatus = me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum.SUBMITTED.name
                        )
                    )
                }
                // Actualizar selección activa si coincide
                _selectedAssignmentForDraft.value?.let { current ->
                    if (current.assignment.id == assignment.id) {
                        _selectedAssignmentForDraft.value = current.copy(
                            assignment = current.assignment.copy(isCompleted = true, status = "completed")
                        )
                    }
                }
                _uiEffect.tryEmit(UiEffect.ShowToast("Entrega enviada exitosamente a Canvas"))
                onSuccess()
            }.onFailure { error ->
                val errorMsg = error.message ?: "Error al enviar la entrega a Canvas"
                _uiEffect.tryEmit(UiEffect.ShowToast(errorMsg))
                onError(errorMsg)
            }
        }
    }

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

    // ─── Estadísticas de Cursos con Motor Académico (3 Estados) ─────────────
    val courseStats: StateFlow<List<CourseStat>> =
        combine(
            courses,
            repository.allAssignments,
            simulationDao.observeAllGroupsWithItems()
        ) { courseList, assignmentList, allGroupRelations ->
            val relationsByCourse = allGroupRelations.groupBy { it.group.courseId }

            courseList.map { course ->
                val courseAssignments = assignmentList.filter { it.courseId == course.id }
                val pending = courseAssignments.count { it.status == "upcoming" }
                val completed = courseAssignments.count { it.status == "completed" }
                val missing = courseAssignments.count { it.status == "missing" }

                val groups = relationsByCourse[course.id]?.map { it.toDomain() } ?: emptyList()

                if (groups.isEmpty()) {
                    // Estado A: Sin grupos configurados
                    CourseStat(
                        course = course,
                        pendingCount = pending,
                        completedCount = completed,
                        missingCount = missing,
                        averageScore = null,
                        hasConfiguredGroups = false,
                        evaluatedProgressPercentage = 0f,
                        performanceLevel = GradePerformanceLevel.AT_RISK
                    )
                } else {
                    // Calcular con motor académico estricto
                    val summary = academicCalculatorUseCase.calculateCourseSummary(course, groups)
                    CourseStat(
                        course = course,
                        pendingCount = pending,
                        completedCount = completed,
                        missingCount = missing,
                        averageScore = summary.currentAverage?.toDouble(),
                        hasConfiguredGroups = true,
                        evaluatedProgressPercentage = summary.evaluatedProgressPercentage,
                        performanceLevel = summary.performanceLevel
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Banner Contextual de Cursos sin Configurar ─────────────────────────
    private val _bannerDismissedLocally = MutableStateFlow(false)

    val unconfiguredBannerState: StateFlow<Triple<Int, Course?, Boolean>> =
        combine(
            courses,
            simulationDao.observeAllGroupsWithItems(),
            preferencesManager.unconfiguredCoursesBannerDismissedUntil,
            _bannerDismissedLocally
        ) { courseList, allGroupRelations, dismissedUntil, locallyDismissed ->
            if (locallyDismissed) {
                Triple(0, null, false)
            } else {
                val relationsByCourse = allGroupRelations.groupBy { it.group.courseId }
                val unconfigured = courseList.filter { relationsByCourse[it.id].isNullOrEmpty() }
                val count = unconfigured.size
                val first = unconfigured.firstOrNull()
                val isSnoozed = DateTimeUtil.nowEpochMillis() < dismissedUntil
                val isVisible = count > 0 && !isSnoozed
                Triple(count, first, isVisible)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0, null, false))

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

    fun dismissUnconfiguredBanner() {
        _bannerDismissedLocally.value = true
        viewModelScope.launch {
            preferencesManager.dismissUnconfiguredCoursesBannerFor7Days()
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
