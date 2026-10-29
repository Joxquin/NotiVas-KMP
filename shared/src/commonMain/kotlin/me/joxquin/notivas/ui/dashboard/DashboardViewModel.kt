package me.joxquin.notivas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.ui.components.miuix.SearchStatus
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
    FAILED,
    SESSION_EXPIRED
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
    private val academicCalculatorUseCase: AcademicCalculatorUseCase = AppModule.academicCalculatorUseCase,
    private val morningBriefingUseCase: me.joxquin.notivas.domain.usecase.MorningBriefingUseCase = AppModule.morningBriefingUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showingMorningRoadmap = MutableStateFlow(false)
    val showingMorningRoadmap: StateFlow<Boolean> = _showingMorningRoadmap.asStateFlow()

    fun openMorningRoadmap() {
        _showingMorningRoadmap.value = true
    }

    fun closeMorningRoadmap() {
        _showingMorningRoadmap.value = false
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.SYNCING)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    val isSessionExpired: StateFlow<Boolean> = preferencesManager.isSessionExpired
    private val _showQuickRelogin = MutableStateFlow(false)
    val showQuickRelogin: StateFlow<Boolean> = _showQuickRelogin.asStateFlow()

    fun openQuickRelogin() {
        _showQuickRelogin.value = true
    }

    fun closeQuickRelogin() {
        _showQuickRelogin.value = false
    }

    fun onReloginSuccess(cookies: String, csrfToken: String?) {
        viewModelScope.launch {
            _showQuickRelogin.value = false
            val url = preferencesManager.universityUrl.first() ?: "https://canvas.utp.edu.pe"
            val success = repository.verifyAndSaveSession(url, cookies, csrfToken)
            if (success) {
                refreshData()
            }
        }
    }

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

    val recentSearches: StateFlow<List<String>> = preferencesManager.recentSearches.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()

    private val _searchStatus = MutableStateFlow(SearchStatus(label = ""))
    val searchStatus: StateFlow<SearchStatus> = _searchStatus.asStateFlow()

    private val _searchOffsetY = MutableStateFlow(0.dp)
    val searchOffsetY: StateFlow<Dp> = _searchOffsetY.asStateFlow()

    private val _discussions = MutableStateFlow<List<me.joxquin.notivas.data.model.CanvasDiscussionTopic>>(emptyList())
    val discussions: StateFlow<List<me.joxquin.notivas.data.model.CanvasDiscussionTopic>> = _discussions.asStateFlow()

    fun setSearchOffsetY(offsetY: Dp) {
        _searchOffsetY.value = offsetY
    }

    fun updateSearchStatus(status: SearchStatus) {
        _searchStatus.value = status
        _isSearchExpanded.value = status.shouldExpand()
    }

    fun setSearchExpanded(expanded: Boolean) {
        _isSearchExpanded.value = expanded
        _searchStatus.value = if (expanded) {
            _searchStatus.value.copy(current = SearchStatus.Status.EXPANDED)
        } else {
            _searchStatus.value.copy(current = SearchStatus.Status.COLLAPSED)
        }
    }

    fun addRecentSearch(query: String) {
        viewModelScope.launch {
            preferencesManager.addRecentSearch(query)
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            preferencesManager.removeRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            preferencesManager.clearRecentSearches()
        }
    }

    private val secureTokenVault = me.joxquin.notivas.security.SecureTokenVault(preferencesManager)
    private val googleDriveApiService = me.joxquin.notivas.data.remote.GoogleDriveApiService(
        preferencesManager = preferencesManager,
        tokenRefresher = { secureTokenVault.refreshGoogleDriveAccessToken() }
    )

    private val _isDraftSyncing = MutableStateFlow(false)
    val isDraftSyncing: StateFlow<Boolean> = _isDraftSyncing.asStateFlow()

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
                if (entity != null && (entity.driveFileId.isNullOrBlank() || entity.driveUrl.contains("/d//edit"))) {
                    // Sanitizar registro residual corrupto en base de datos
                    viewModelScope.launch { repository.deleteAssignmentDraft(ui.assignment.id) }
                    null
                } else {
                    val isSubmitted = ui.assignment.status == "completed" ||
                            ui.assignment.isCompleted ||
                            (ui.assignment.submission?.workflowState in listOf("submitted", "graded")) ||
                            (!ui.assignment.submittedAt.isNullOrBlank()) ||
                            (!ui.assignment.submission?.submittedAt.isNullOrBlank())
                    entity?.toDomain(isSubmitted)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun openDraft(assignmentUi: AssignmentUiModel) {
        _selectedAssignmentForDraft.value = assignmentUi
        loadTemplatesForCourse(
            courseId = assignmentUi.assignment.courseId,
            assignmentId = assignmentUi.assignment.id,
            assignmentDescription = assignmentUi.assignment.description
        )
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
            _isDraftSyncing.value = true
            try {
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

                // 2. Creación / Clonación con timeout de 60 segundos
                val createdFile = kotlinx.coroutines.withTimeoutOrNull(60_000L) {
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
                                println("[DashboardViewModel] Procesando plantilla CANVAS_FILE: ${effectiveCandidate.title}, url=${effectiveCandidate.url}, canvasFileId=${effectiveCandidate.canvasFileId}")
                                var fileBytes: ByteArray? = null

                                var downloadUrl = effectiveCandidate.url.replace("&amp;", "&").trim()
                                val fileIdFromUrl = Regex("""/files/(\d+)""").find(downloadUrl)?.groupValues?.get(1)?.toLongOrNull() ?: effectiveCandidate.canvasFileId
                                if (fileIdFromUrl != null && !downloadUrl.contains("/download")) {
                                    val verifier = Regex("""verifier=([^&"'>]+)""").find(downloadUrl)?.groupValues?.get(1)
                                    if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
                                        val hostBase = downloadUrl.substringBefore("/courses/").substringBefore("/files/")
                                        downloadUrl = "$hostBase/files/$fileIdFromUrl/download?download_frd=1${if (verifier != null) "&verifier=$verifier" else ""}"
                                        println("[DashboardViewModel] URL transformada a endpoint directo de descarga: $downloadUrl")
                                    }
                                }

                                // 1. Descargar desde downloadUrl
                                if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
                                    println("[DashboardViewModel] Descargando desde downloadUrl: $downloadUrl")
                                    val bytes = repository.downloadCanvasFileBytes(downloadUrl)
                                    if (bytes != null && bytes.isNotEmpty() && !isJsonContent(bytes)) {
                                        fileBytes = bytes
                                    } else {
                                        println("[DashboardViewModel] Bytes obtenidos invalidos o es JSON (${bytes?.size} bytes).")
                                    }
                                }

                                // 2. Si no se descargo o devolvio JSON pero tiene canvasFileId, resolver URL via API
                                if (fileBytes == null && effectiveCandidate.canvasFileId != null) {
                                    println("[DashboardViewModel] Resolviendo URL via getCanvasFileDownloadUrl para fileId=${effectiveCandidate.canvasFileId}...")
                                    val resolvedUrl = repository.getCanvasFileDownloadUrl(assignment.courseId, effectiveCandidate.canvasFileId)
                                    if (!resolvedUrl.isNullOrBlank()) {
                                        println("[DashboardViewModel] URL resuelta: $resolvedUrl. Descargando...")
                                        val bytes = repository.downloadCanvasFileBytes(resolvedUrl)
                                        if (bytes != null && bytes.isNotEmpty() && !isJsonContent(bytes)) {
                                            fileBytes = bytes
                                        }
                                    }
                                }

                                if (fileBytes != null && fileBytes.isNotEmpty() && !isJsonContent(fileBytes)) {
                                    println("[DashboardViewModel] Archivo descargado exitosamente (${fileBytes.size} bytes). Subiendo y convirtiendo a Google Drive...")
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

                                    val uploaded = googleDriveApiService.uploadAndConvertFile(
                                        fileName = generatedFileName,
                                        parentFolderId = folderId,
                                        fileBytes = fileBytes,
                                        targetMimeType = targetMime,
                                        sourceMimeType = sourceMime
                                    )
                                    if (uploaded == null) {
                                        println("[DashboardViewModel] uploadAndConvertFile devolvio null. Creando documento en blanco como fallback...")
                                        _uiEffect.tryEmit(UiEffect.ShowToast("No se pudo convertir la plantilla en Google Docs. Se creo un documento base."))
                                        googleDriveApiService.createBlankDocument(generatedFileName, folderId)
                                    } else {
                                        uploaded
                                    }
                                } else {
                                    println("[DashboardViewModel] No se pudieron obtener los bytes del archivo Canvas. Fallback a documento en blanco.")
                                    _uiEffect.tryEmit(UiEffect.ShowToast("No se pudo descargar la plantilla de Canvas. Se creo un documento en blanco."))
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

                if (createdFile != null && createdFile.id.isNotBlank()) {
                    val realUrl = createdFile.webViewLink?.takeIf { it.isNotBlank() }
                        ?: "https://docs.google.com/document/d/${createdFile.id}/edit"
                    val persistedInfo = optimisticInfo.copy(
                        driveFileId = createdFile.id,
                        driveUrl = realUrl,
                        lastEditedTime = "Sincronizado ahora",
                        draftStatus = me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum.IN_PROGRESS
                    )
                    repository.saveAssignmentDraft(
                        me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity.fromDomain(
                            assignmentId = assignment.id,
                            info = persistedInfo
                        )
                    )
                    _uiEffect.tryEmit(UiEffect.ShowToast("Borrador creado y sincronizado en Google Drive"))
                    _uiEffect.tryEmit(UiEffect.OpenUrl(realUrl))
                } else {
                    // Revertir borrador local porque falló la creación remota o expiró el tiempo
                    repository.deleteAssignmentDraft(assignment.id)
                    _uiEffect.tryEmit(UiEffect.ShowToast("No se pudo crear el documento en Google Drive. Revisa tu cuenta o conexión."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                repository.deleteAssignmentDraft(assignment.id)
                val msg = e.message.orEmpty()
                val errorMsg = if (msg.contains("401") || msg.contains("auth", ignoreCase = true)) {
                    "Sesión de Google Drive expirada. Reconecta tu cuenta en Ajustes."
                } else {
                    "Fallo de conexión con Google Drive: ${msg.take(60)}"
                }
                _uiEffect.tryEmit(UiEffect.ShowToast(errorMsg))
            } finally {
                _isDraftSyncing.value = false
            }
        }
    }

    /**
     * Cancela la creación de un borrador, elimina el archivo remoto en Google Drive y limpia el estado local.
     */
    fun cancelDraft(assignmentId: Long) {
        viewModelScope.launch {
            val currentDraft = repository.getAssignmentDraft(assignmentId)
            val fileId = currentDraft?.driveFileId
            if (!fileId.isNullOrBlank()) {
                googleDriveApiService.deleteFile(fileId)
            }
            repository.deleteAssignmentDraft(assignmentId)
            _uiEffect.tryEmit(UiEffect.ShowToast("Borrador cancelado y archivo eliminado."))
        }
    }

    /**
     * Modifica el nombre de un borrador en Google Drive usando HTTP PATCH explícito
     */
    fun renameDraft(assignmentId: Long, newSuffix: String = "Actualizado") {
        viewModelScope.launch {
            val currentDraft = repository.getAssignmentDraft(assignmentId)
            val fileId = currentDraft?.driveFileId
            if (!fileId.isNullOrBlank()) {
                val updatedName = "Borrador - ${newSuffix} (v2)"
                val updated = googleDriveApiService.updateFileName(fileId, updatedName)
                if (updated != null) {
                    _uiEffect.tryEmit(UiEffect.ShowToast("Archivo renombrado vía PATCH en Google Drive"))
                } else {
                    _uiEffect.tryEmit(UiEffect.ShowToast("Fallo al actualizar archivo en Google Drive"))
                }
            } else {
                _uiEffect.tryEmit(UiEffect.ShowToast("No hay archivo remoto para actualizar."))
            }
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
            // Actualizar nombre del archivo en Google Drive usando HTTP PATCH explícito
            val currentDraft = repository.getAssignmentDraft(assignment.id)
            val fileId = currentDraft?.driveFileId
            if (!fileId.isNullOrBlank()) {
                val updatedTitle = "[ENTREGADO] ${assignment.name.take(40)}"
                googleDriveApiService.updateFileName(fileId, updatedTitle)
            }

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

    fun submitAssignmentDirect(
        assignment: Assignment,
        submissionType: String,
        fileBytes: ByteArray? = null,
        fileName: String? = null,
        mimeType: String = "application/octet-stream",
        bodyText: String? = null,
        comment: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.submitAssignment(
                courseId = assignment.courseId,
                assignmentId = assignment.id,
                submissionType = submissionType,
                bodyText = bodyText,
                fileBytes = fileBytes,
                fileName = fileName,
                mimeType = mimeType,
                comment = comment
            )
            result.onSuccess { subDetails ->
                val currentDraft = repository.getAssignmentDraft(assignment.id)
                if (currentDraft != null) {
                    repository.saveAssignmentDraft(
                        currentDraft.copy(
                            draftStatus = me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum.SUBMITTED.name
                        )
                    )
                }
                _selectedAssignmentForDraft.value?.let { current ->
                    if (current.assignment.id == assignment.id) {
                        _selectedAssignmentForDraft.value = current.copy(
                            assignment = current.assignment.copy(
                                isCompleted = true,
                                status = "completed",
                                submittedAt = subDetails.submittedAt ?: me.joxquin.notivas.util.DateTimeUtil.nowLocalDate().toIsoDateString()
                            )
                        )
                    }
                }
                refreshData()
                _uiEffect.tryEmit(UiEffect.ShowToast("Tarea entregada con éxito a Canvas"))
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

    val universityUrl: StateFlow<String> = preferencesManager.universityUrl.map {
        it ?: "https://canvas.utp.edu.pe"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://canvas.utp.edu.pe")

    init {
        initialSync()
        viewModelScope.launch {
            preferencesManager.isSessionExpired.collect { expired ->
                if (expired) {
                    _syncStatus.value = SyncStatus.SESSION_EXPIRED
                }
            }
        }
    }

    private fun initialSync() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            try {
                repository.getProfile()
                repository.fetchAndSaveData()
                _syncStatus.value = SyncStatus.SYNCED
            } catch (e: me.joxquin.notivas.data.remote.CanvasAuthExpiredException) {
                _syncStatus.value = SyncStatus.SESSION_EXPIRED
            } catch (e: Exception) {
                if (preferencesManager.getIsSessionExpired()) {
                    _syncStatus.value = SyncStatus.SESSION_EXPIRED
                } else {
                    _syncStatus.value = SyncStatus.FAILED
                }
            }
            loadDiscussionsQuietly()
        }
    }

    private fun loadDiscussionsQuietly() {
        viewModelScope.launch {
            try {
                val topics = repository.getDiscussionsForCourses()
                _discussions.value = topics
            } catch (_: Exception) {}
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
            } catch (e: me.joxquin.notivas.data.remote.CanvasAuthExpiredException) {
                _syncStatus.value = SyncStatus.SESSION_EXPIRED
            } catch (e: Exception) {
                if (preferencesManager.getIsSessionExpired()) {
                    _syncStatus.value = SyncStatus.SESSION_EXPIRED
                } else {
                    _syncStatus.value = SyncStatus.FAILED
                }
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

    private val _forceAiMorningRefresh = MutableStateFlow(0)

    val morningBriefing: StateFlow<me.joxquin.notivas.data.model.MorningBriefingData?> = combine(
        userProfile,
        courses,
        allAssignmentsUi,
        courseStats,
        _forceAiMorningRefresh
    ) { profile, courseList, assignments, stats, refreshCount ->
        morningBriefingUseCase.buildMorningBriefing(
            userProfile = profile,
            courses = courseList,
            allAssignments = assignments,
            courseStats = stats,
            forceAiRefresh = refreshCount > 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun refreshMorningBriefingAi() {
        viewModelScope.launch {
            _forceAiMorningRefresh.value += 1
        }
    }

    private fun isJsonContent(bytes: ByteArray): Boolean {
        if (bytes.size > 65536) return false
        val trimmed = bytes.take(20).toByteArray().decodeToString().trimStart()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }
}
