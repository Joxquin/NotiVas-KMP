package me.joxquin.notivas.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasAuthCredential
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.model.SubmissionDetails
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.remote.CanvasApiService

/**
 * Repositorio centralizado para Canvas LMS en Kotlin Multiplatform.
 * Aplica arquitectura Offline-First usando [InMemoryLocalStore] como SSOT y [CanvasApiService] para sincronización.
 */
class CanvasRepository(
    private val apiService: CanvasApiService,
    private val localStore: InMemoryLocalStore,
    private val preferencesManager: PreferencesManager
) {
    val userProfile: Flow<UserProfile?> = localStore.userProfile
    fun getUserProfileOnce(): UserProfile? = localStore.getUserProfileOnce()
    val allCourses: Flow<List<Course>> = localStore.getAllCourses()
    val allAssignments: Flow<List<Assignment>> = localStore.getAllAssignments()
    val allPlannerItems: Flow<List<PlannerItem>> = localStore.getAllPlannerItems()
    val universityUrl: Flow<String?> = preferencesManager.universityUrl

    private fun getAuthToken(): String? {
        val credential = preferencesManager.getAuthCredential() ?: return null
        return when (credential) {
            is CanvasAuthCredential.BearerToken -> "Bearer ${credential.token}"
            is CanvasAuthCredential.SessionCookies -> "Cookie: ${credential.cookies}"
        }
    }

    fun getAssignmentsForCourse(courseId: Long): Flow<List<Assignment>> =
        localStore.getAssignmentsByCourse(courseId)

    fun getPlannerItemsForCourse(courseId: Long): Flow<List<PlannerItem>> =
        localStore.getPlannerItemsByCourse(courseId)

    fun getSimulationGroupsWithItems(courseId: Long): Flow<List<SimulationGroupWithItems>> =
        localStore.getGroupsWithItemsByCourse(courseId)

    suspend fun createSimulationGroup(group: SimulationGroup): Long =
        localStore.insertGroup(group)

    suspend fun updateSimulationGroup(group: SimulationGroup) =
        localStore.updateGroup(group)

    suspend fun deleteSimulationGroup(group: SimulationGroup) =
        localStore.deleteGroup(group)

    suspend fun addSimulationItem(item: SimulationItem): Long =
        localStore.insertItem(item)

    suspend fun deleteSimulationItem(item: SimulationItem) =
        localStore.deleteItem(item)

    suspend fun updateSimulationItemScore(itemId: Long, score: Float) =
        localStore.updateItemScore(itemId, score)

    suspend fun applyTemplate(course: Course, template: me.joxquin.notivas.domain.template.CourseEvaluationTemplate) {
        val assignments = localStore.getAssignmentsByCourse(course.id).first()
        val (updatedCourse, groupsWithItems) = me.joxquin.notivas.domain.template.TemplateAssignmentMatcher.instantiateTemplate(
            course = course,
            template = template,
            canvasAssignments = assignments
        )
        localStore.replaceCourseGroupsWithTemplate(updatedCourse, groupsWithItems)
    }

    suspend fun applyTemplateToAllCourses(template: me.joxquin.notivas.domain.template.CourseEvaluationTemplate): Int = withContext(Dispatchers.IO) {
        val courses = localStore.getAllCourses().first()
        val updates = courses.map { course ->
            val assignments = localStore.getAssignmentsByCourse(course.id).first()
            me.joxquin.notivas.domain.template.TemplateAssignmentMatcher.instantiateTemplate(
                course = course,
                template = template,
                canvasAssignments = assignments
            )
        }
        localStore.replaceMultipleCourseGroups(updates)
        courses.size
    }

    suspend fun exportGlobalGroupsToJson(): String = withContext(Dispatchers.IO) {
        val courses = localStore.getAllCourses().first()
        val allGroupsWithItems = localStore.getAllGroupsWithItemsList()

        val courseExports = courses.map { course ->
            val courseGroups = allGroupsWithItems.filter { it.group.courseId == course.id }
            me.joxquin.notivas.domain.template.CourseGroupsExport(
                courseId = course.id,
                courseCode = course.courseCode,
                courseName = course.name,
                passingGrade = course.passingGrade,
                totalWeeks = course.totalWeeks,
                groups = courseGroups.map { g ->
                    me.joxquin.notivas.domain.template.GlobalGroupExportGroup(
                        name = g.group.name,
                        weightPercentage = g.group.weightPercentage,
                        targetAssessments = g.group.targetAssessments,
                        dropLowest = g.group.dropLowest,
                        minToDrop = g.group.minToDrop,
                        calculationMode = g.group.calculationMode,
                        items = g.items.map { item ->
                            me.joxquin.notivas.domain.template.GlobalGroupExportItem(
                                name = item.name,
                                weekNumber = item.weekNumber,
                                maxScore = item.maxScore,
                                internalWeight = item.internalWeight
                            )
                        }
                    )
                }
            )
        }

        val exportFile = me.joxquin.notivas.domain.template.GlobalGroupsExportFile(
            version = 1,
            courses = courseExports
        )

        val json = kotlinx.serialization.json.Json { prettyPrint = true; ignoreUnknownKeys = true }
        json.encodeToString(me.joxquin.notivas.domain.template.GlobalGroupsExportFile.serializer(), exportFile)
    }

    suspend fun importGlobalGroupsFromJson(jsonContent: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val exportFile = json.decodeFromString(me.joxquin.notivas.domain.template.GlobalGroupsExportFile.serializer(), jsonContent)
            val currentCourses = localStore.getAllCourses().first()

            val updates = mutableListOf<Pair<Course, List<SimulationGroupWithItems>>>()

            if (exportFile.defaultTemplate != null) {
                // Aplicar la plantilla por defecto a todos los cursos
                currentCourses.forEach { course ->
                    val assignments = localStore.getAssignmentsByCourse(course.id).first()
                    val result = me.joxquin.notivas.domain.template.TemplateAssignmentMatcher.instantiateTemplate(
                        course = course,
                        template = exportFile.defaultTemplate,
                        canvasAssignments = assignments
                    )
                    updates.add(result)
                }
            } else {
                // Mapear por courseCode o courseId o nombre
                currentCourses.forEach { course ->
                    val matchedExport = exportFile.courses.firstOrNull { exp ->
                        (exp.courseId != null && exp.courseId == course.id) ||
                        (!exp.courseCode.isNullOrBlank() && exp.courseCode.equals(course.courseCode, ignoreCase = true)) ||
                        (!exp.courseName.isNullOrBlank() && exp.courseName.equals(course.name, ignoreCase = true))
                    } ?: exportFile.courses.firstOrNull() // Si sólo hay 1 configuración genérica

                    if (matchedExport != null && matchedExport.groups.isNotEmpty()) {
                        val template = me.joxquin.notivas.domain.template.CourseEvaluationTemplate(
                            id = "imported_${course.id}",
                            name = matchedExport.courseName ?: course.name,
                            institution = "Importado",
                            description = "Configuración importada desde archivo",
                            totalWeeks = matchedExport.totalWeeks,
                            passingGrade = matchedExport.passingGrade,
                            groups = matchedExport.groups.map { g ->
                                me.joxquin.notivas.domain.template.GroupTemplate(
                                    name = g.name,
                                    weightPercentage = g.weightPercentage,
                                    targetAssessments = g.targetAssessments,
                                    dropLowest = g.dropLowest,
                                    minToDrop = g.minToDrop,
                                    calculationMode = g.calculationMode,
                                    items = g.items.map { item ->
                                        me.joxquin.notivas.domain.template.ItemTemplate(
                                            name = item.name,
                                            weekNumber = item.weekNumber,
                                            maxScore = item.maxScore,
                                            internalWeight = item.internalWeight,
                                            matchingPatterns = item.matchingPatterns
                                        )
                                    }
                                )
                            }
                        )
                        val assignments = localStore.getAssignmentsByCourse(course.id).first()
                        val result = me.joxquin.notivas.domain.template.TemplateAssignmentMatcher.instantiateTemplate(
                            course = course,
                            template = template,
                            canvasAssignments = assignments
                        )
                        updates.add(result)
                    }
                }
            }

            if (updates.isNotEmpty()) {
                localStore.replaceMultipleCourseGroups(updates)
                Result.success(updates.size)
            } else {
                Result.failure(Exception("No se encontraron cursos coincidentes en el archivo JSON."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetAllSimulationGroups() = withContext(Dispatchers.IO) {
        localStore.deleteAllSimulationGroups()
    }

    suspend fun createGroupWithItems(group: SimulationGroup, items: List<SimulationItem>) {
        localStore.createGroupWithItems(group, items)
    }

    suspend fun linkSimulationItemWithCanvas(itemId: Long, assignmentId: Long, name: String, manualScore: Float? = null) =
        localStore.linkItemWithCanvasAssignment(itemId, assignmentId, name, manualScore)

    suspend fun fetchCourseModules(courseId: Long): List<CanvasModule> = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        return@withContext try {
            apiService.getModulesWithItems(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCourseAnnouncements(courseId: Long): List<me.joxquin.notivas.data.model.CanvasDiscussionTopic> = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        return@withContext try {
            apiService.getCourseAnnouncements(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCourseUsers(courseId: Long): List<UserProfile> = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        return@withContext try {
            apiService.getCourseUsers(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Extrae y compila plantillas y documentos candidatos del curso:
     * 1. Detecta enlaces directos a Google Docs/Drive o archivos en la consigna de la tarea.
     * 2. Recorre todos los módulos del curso buscando archivos (.docx, .doc, .xlsx, .pdf, .gdoc) o enlaces externos.
     */
    suspend fun getCourseTemplateCandidates(
        courseId: Long,
        assignmentId: Long? = null,
        assignmentDescription: String? = null
    ): List<me.joxquin.notivas.data.model.TemplateCandidate> = withContext(Dispatchers.IO) {
        val baseUrl = preferencesManager.universityUrl.first() ?: ""
        val candidates = mutableListOf<me.joxquin.notivas.data.model.TemplateCandidate>()
        var currentModuleName: String? = null

        // 1. Analizar módulos del curso primero para poder identificar el módulo de la tarea
        val moduleCandidates = mutableListOf<me.joxquin.notivas.data.model.TemplateCandidate>()
        try {
            val modules = fetchCourseModules(courseId)
            for (module in modules) {
                val items = module.items ?: emptyList()
                val containsAssignment = if (assignmentId != null) {
                    items.any { item ->
                        (item.type == "Assignment" || item.type == "SubHeader") &&
                                (item.contentId == assignmentId || item.id == assignmentId)
                    }
                } else false

                if (containsAssignment) {
                    currentModuleName = module.name
                }

                for (item in items) {
                    when (item.type) {
                        "File" -> {
                            val lowerTitle = item.title.lowercase()
                            val isDoc = lowerTitle.endsWith(".docx") || lowerTitle.endsWith(".doc") ||
                                    lowerTitle.endsWith(".xlsx") || lowerTitle.endsWith(".pdf")
                            if (isDoc) {
                                val ext = lowerTitle.substringAfterLast(".", "")
                                moduleCandidates.add(
                                    me.joxquin.notivas.data.model.TemplateCandidate(
                                        title = item.title,
                                        type = me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_FILE,
                                        url = item.url ?: item.htmlUrl ?: "",
                                        moduleName = module.name,
                                        fileExtension = ext,
                                        canvasFileId = item.contentId ?: item.id,
                                        isCurrentAssignmentModule = containsAssignment
                                    )
                                )
                            }
                        }
                        "ExternalUrl" -> {
                            val url = item.externalUrl ?: item.url ?: item.htmlUrl ?: ""
                            val lowerUrl = url.lowercase()
                            val isDrive = lowerUrl.contains("docs.google.com") || lowerUrl.contains("drive.google.com")
                            val isDocExt = lowerUrl.endsWith(".docx") || lowerUrl.endsWith(".doc") ||
                                    lowerUrl.endsWith(".xlsx") || lowerUrl.endsWith(".pdf")
                            val ext = when {
                                lowerUrl.contains("spreadsheets") || lowerUrl.endsWith(".xlsx") -> "gsheet"
                                lowerUrl.endsWith(".pdf") -> "pdf"
                                else -> "gdoc"
                            }
                            moduleCandidates.add(
                                me.joxquin.notivas.data.model.TemplateCandidate(
                                    title = item.title,
                                    type = if (isDrive) me.joxquin.notivas.data.model.TemplateSourceType.DRIVE_URL else me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_FILE,
                                    url = url,
                                    moduleName = module.name,
                                    fileExtension = ext,
                                    isCurrentAssignmentModule = containsAssignment
                                )
                            )
                        }
                        "Page" -> {
                            val pageUrl = item.pageUrl ?: item.url?.substringAfterLast("/") ?: ""
                            val lowerTitle = item.title.lowercase()
                            val looksLikeGuideOrTemplate = lowerTitle.contains("guía") ||
                                    lowerTitle.contains("guia") ||
                                    lowerTitle.contains("lab") ||
                                    lowerTitle.contains("plantilla") ||
                                    lowerTitle.contains("formato") ||
                                    lowerTitle.contains("recurso")

                            if (looksLikeGuideOrTemplate && pageUrl.isNotBlank()) {
                                moduleCandidates.add(
                                    me.joxquin.notivas.data.model.TemplateCandidate(
                                        title = item.title,
                                        type = me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_PAGE,
                                        url = item.htmlUrl ?: item.url ?: "",
                                        moduleName = module.name,
                                        fileExtension = "gdoc",
                                        isCurrentAssignmentModule = containsAssignment
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Si se determinó el módulo de la tarea, asegurar que todos sus candidatos tengan el flag activo
        val resolvedModuleCandidates = if (!currentModuleName.isNullOrBlank()) {
            moduleCandidates.map { c ->
                if (c.moduleName == currentModuleName) c.copy(isCurrentAssignmentModule = true) else c
            }
        } else {
            moduleCandidates
        }

        // 2. Analizar enlaces o adjuntos en la descripción de la tarea actual
        if (!assignmentDescription.isNullOrBlank()) {
            val driveUrlRegex = Regex("""https://(?:docs|drive)\.google\.com/[^\s"'>]+""")
            driveUrlRegex.findAll(assignmentDescription).forEach { match ->
                val url = match.value
                val isSpreadsheet = url.contains("spreadsheets")
                candidates.add(
                    me.joxquin.notivas.data.model.TemplateCandidate(
                        title = if (isSpreadsheet) "Hoja de cálculo en Google Drive" else "Documento en Google Drive",
                        type = me.joxquin.notivas.data.model.TemplateSourceType.DRIVE_URL,
                        url = url,
                        moduleName = currentModuleName ?: "Consigna de la Tarea",
                        fileExtension = if (isSpreadsheet) "gsheet" else "gdoc",
                        isCurrentAssignmentModule = true
                    )
                )
            }

            // 2a. Extraer enlaces HTML directos a archivos de Canvas: <a ... href="..." ...>nombre</a>
            val anchorFileRegex = Regex("""<a\s+[^>]*href=["']([^"']+)["'][^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE)
            anchorFileRegex.findAll(assignmentDescription).forEach { match ->
                val rawHref = match.groupValues[1]
                val cleanHref = rawHref.replace("&amp;", "&")
                val anchorText = match.groupValues[2].replace(Regex("<[^>]*>"), "").trim()
                val docExt = cleanHref.substringBefore("?").substringAfterLast(".", "").lowercase()
                val textExt = anchorText.substringAfterLast(".", "").lowercase()
                val ext = if (docExt in listOf("docx", "doc", "xlsx", "xls", "pdf")) docExt
                          else if (textExt in listOf("docx", "doc", "xlsx", "xls", "pdf")) textExt
                          else ""

                val fileIdFromHref = Regex("""/files/(\d+)""").find(cleanHref)?.groupValues?.get(1)?.toLongOrNull()
                val isCanvasFileLink = cleanHref.contains("/files/") || cleanHref.contains("download") || fileIdFromHref != null

                if (ext.isNotBlank() || isCanvasFileLink) {
                    val resolvedTitle = if (anchorText.isNotBlank() && !anchorText.startsWith("http")) {
                        anchorText
                    } else if (fileIdFromHref != null) {
                        "Plantilla_Canvas_$fileIdFromHref.${ext.ifBlank { "docx" }}"
                    } else {
                        cleanHref.substringBefore("?").substringAfterLast("/")
                    }

                    val fullDownloadUrl = if (fileIdFromHref != null && !cleanHref.contains("/download")) {
                        val verifier = Regex("""verifier=([^&"'>]+)""").find(cleanHref)?.groupValues?.get(1)
                        val hostBase = if (cleanHref.startsWith("http://") || cleanHref.startsWith("https://")) {
                            cleanHref.substringBefore("/courses/").substringBefore("/files/")
                        } else {
                            baseUrl.trimEnd('/')
                        }
                        "$hostBase/files/$fileIdFromHref/download?download_frd=1${if (verifier != null) "&verifier=$verifier" else ""}"
                    } else when {
                        cleanHref.startsWith("http://") || cleanHref.startsWith("https://") -> cleanHref
                        baseUrl.isNotBlank() -> "${baseUrl.trimEnd('/')}/${cleanHref.trimStart('/')}"
                        else -> cleanHref
                    }

                    val matchingModuleCandidate = resolvedModuleCandidates.find {
                        (fileIdFromHref != null && it.canvasFileId == fileIdFromHref) ||
                        it.title.equals(resolvedTitle, ignoreCase = true) ||
                        it.title.contains(resolvedTitle.substringBeforeLast("."), ignoreCase = true)
                    }

                    candidates.add(
                        me.joxquin.notivas.data.model.TemplateCandidate(
                            title = resolvedTitle,
                            type = me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_FILE,
                            url = fullDownloadUrl,
                            moduleName = currentModuleName ?: "Consigna de la Tarea",
                            fileExtension = ext.ifBlank { matchingModuleCandidate?.fileExtension ?: "docx" },
                            canvasFileId = fileIdFromHref ?: matchingModuleCandidate?.canvasFileId,
                            isCurrentAssignmentModule = true
                        )
                    )
                }
            }

            // 2b. Archivos nombrados en texto plano si no se detectaron como enlaces HTML
            val docxRegex = Regex("""([\w\-_]+\.(?:docx|doc|xlsx|pdf))""", RegexOption.IGNORE_CASE)
            docxRegex.findAll(assignmentDescription).forEach { match ->
                val fileName = match.value
                val ext = fileName.substringAfterLast(".", "").lowercase()
                val alreadyAdded = candidates.any { it.title.equals(fileName, ignoreCase = true) }
                if (!alreadyAdded) {
                    val matchingModuleCandidate = resolvedModuleCandidates.find {
                        it.title.equals(fileName, ignoreCase = true) || it.title.contains(fileName.substringBeforeLast("."), ignoreCase = true)
                    }

                    candidates.add(
                        me.joxquin.notivas.data.model.TemplateCandidate(
                            title = fileName,
                            type = me.joxquin.notivas.data.model.TemplateSourceType.CANVAS_FILE,
                            url = matchingModuleCandidate?.url ?: fileName,
                            moduleName = currentModuleName ?: "Consigna de la Tarea",
                            fileExtension = ext,
                            canvasFileId = matchingModuleCandidate?.canvasFileId,
                            isCurrentAssignmentModule = true
                        )
                    )
                }
            }
        }

        candidates.addAll(resolvedModuleCandidates)

        // Retornar lista deduplicada dando prioridad a los que tengan canvasFileId o URL válida
        candidates.distinctBy { it.title.lowercase() }
    }

    suspend fun fetchAndSaveData() = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext

        try {
            // 1. Fetch Planner Items
            try {
                val plannerItems = apiService.getPlannerItems(
                    baseUrl = baseUrl,
                    token = token,
                    startDate = "2024-01-01"
                )
                localStore.insertPlannerItems(plannerItems)
            } catch (_: Exception) { }

            // 2. Fetch Active Courses & Dashboard Cards (con portadas firmadas de Canvas)
            val dashboardCards = try {
                apiService.getDashboardCards(baseUrl, token)
            } catch (_: Exception) {
                emptyList()
            }
            val cardImagesByCourseId = buildMap {
                dashboardCards.forEach { card ->
                    val image = card.image?.takeIf { it.isNotBlank() } ?: return@forEach
                    val courseId = card.id ?: card.pagesUrl?.let { url ->
                        Regex("""/courses/(\d+)""").find(url)?.groupValues?.getOrNull(1)?.toLongOrNull()
                    }
                    if (courseId != null) {
                        put(courseId, image)
                    }
                }
            }

            val rawCoursesList = apiService.getCourses(
                baseUrl = baseUrl,
                token = token
            )
            val allCoursesList = rawCoursesList.map { course ->
                val cardImage = cardImagesByCourseId[course.id]
                val finalImageUrl = cardImage ?: course.imageUrl
                course.copy(imageUrl = finalImageUrl)
            }
            println("[CanvasRepository] Cursos actualizados: ${allCoursesList.size}. Con portada: ${allCoursesList.count { !it.imageUrl.isNullOrBlank() }}")
            allCoursesList.forEach {
                println("[CanvasRepository] Curso ${it.id} (${it.courseCode}): imageUrl=${it.imageUrl}")
            }
            localStore.upsertCourses(allCoursesList)

            // 3. Fetch assignments for courses in parallel
            val currentAssignmentsList = coroutineScope {
                allCoursesList.map { course ->
                    async(Dispatchers.IO) {
                        try {
                            val rawAssignments = apiService.getAssignmentsForCourse(
                                baseUrl = baseUrl,
                                token = token,
                                courseId = course.id,
                                include = "submission",
                                orderBy = "due_at"
                            )
                            rawAssignments.mapNotNull { a ->
                                val isForum = a.submissionTypes?.contains("discussion_topic") == true ||
                                        a.name.contains("FORO", ignoreCase = true) ||
                                        a.name.contains("FORUM", ignoreCase = true)
                                if (isForum) return@mapNotNull null

                                val sub = a.submission
                                val isSubmitted = sub != null &&
                                        (!sub.submittedAt.isNullOrBlank() || sub.workflowState in listOf("submitted", "graded"))
                                val status = if (isSubmitted) "completed" else "upcoming"
                                val effectiveDueAt = a.dueAt ?: a.lockAt

                                a.copy(
                                    dueAt = effectiveDueAt,
                                    status = status,
                                    isCompleted = isSubmitted,
                                    submittedAt = sub?.submittedAt,
                                    gradedAt = sub?.gradedAt,
                                    score = sub?.score,
                                    grade = sub?.grade
                                )
                            }
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }.awaitAll().flatten()
            }

            // 4. Fetch Missing Submissions
            val missingMap = try {
                val rawMissing = apiService.getMissingSubmissions(baseUrl = baseUrl, token = token)
                rawMissing.mapNotNull { a ->
                    val isForum = a.submissionTypes?.contains("discussion_topic") == true ||
                            a.name.contains("FORO", ignoreCase = true) ||
                            a.name.contains("FORUM", ignoreCase = true)
                    if (isForum) return@mapNotNull null
                    a.id to a.copy(status = "missing")
                }.toMap()
            } catch (_: Exception) {
                emptyMap()
            }

            val currentWithMissing = currentAssignmentsList.map { a ->
                if (missingMap.containsKey(a.id) && a.status != "completed") {
                    a.copy(status = "missing")
                } else {
                    a
                }
            }
            val remainingMissing = missingMap.values.filter { m -> currentAssignmentsList.none { it.id == m.id } }
            val combinedAssignments = currentWithMissing + remainingMissing

            localStore.insertAssignments(combinedAssignments)
            preferencesManager.setSessionExpired(false)

            // Si está en sesión de cookies, intentar migrar silenciosamente a Token permanente
            if (preferencesManager.getAuthMode() == me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION) {
                try {
                    tryUpgradeSessionToToken()
                } catch (_: Exception) {}
            }
        } catch (e: me.joxquin.notivas.data.remote.CanvasAuthExpiredException) {
            preferencesManager.setSessionExpired(true)
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun tryUpgradeSessionToToken(): Boolean = withContext(Dispatchers.IO) {
        if (preferencesManager.getAuthMode() == me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION) {
            val cookies = preferencesManager.getSessionCookies() ?: return@withContext false
            val csrf = preferencesManager.getCsrfToken()
            val url = preferencesManager.universityUrl.first() ?: return@withContext false
            val token = apiService.generateStudentAccessToken(url, cookies, csrf)
            if (!token.isNullOrBlank()) {
                preferencesManager.saveAccessToken(token)
                println("[CanvasRepository] Migración exitosa de COOKIE_SESSION a BearerToken permanente")
                return@withContext true
            }
        }
        false
    }

    suspend fun verifyAndSave(url: String, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.verifyToken(url, "Bearer $token")
            preferencesManager.saveUniversityUrl(url)
            preferencesManager.saveAccessToken(token)
            preferencesManager.setSessionExpired(false)
            true
        } catch (e: Exception) {
            println("CanvasRepository: verifyAndSave failed for url='$url': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun verifyAndSaveSession(url: String, cookies: String, csrfToken: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.verifyToken(url, "Cookie: $cookies")
            preferencesManager.saveUniversityUrl(url)

            // Primera opción: Intentar obtener el token de estudiante permanente vía Canvas API
            val studentToken = apiService.generateStudentAccessToken(
                baseUrl = url,
                cookies = cookies,
                csrfToken = csrfToken,
                purpose = "NotiVas App"
            )

            if (!studentToken.isNullOrBlank()) {
                println("[CanvasRepository] Primera opción exitosa: Token de estudiante autogenerado. Guardando BearerToken.")
                preferencesManager.saveAccessToken(studentToken)
            } else {
                println("[CanvasRepository] Creación de token no disponible en la institución. Guardando SessionCookies como fallback.")
                preferencesManager.saveSessionCookies(cookies, csrfToken)
            }
            preferencesManager.setSessionExpired(false)
            true
        } catch (e: Exception) {
            println("CanvasRepository: verifyAndSaveSession failed for url='$url': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun completeOnboarding() {
        preferencesManager.setOnboardingCompleted(true)
    }

    suspend fun saveGoogleDriveAuth(accessToken: String, refreshToken: String?, email: String?) {
        preferencesManager.saveGoogleDriveTokens(accessToken, refreshToken, email)
    }

    suspend fun getProfile(): UserProfile = withContext(Dispatchers.IO) {
        val baseUrl = preferencesManager.universityUrl.first() ?: ""
        val token = getAuthToken() ?: throw IllegalStateException("No auth credential found")
        try {
            val remoteProfile = apiService.getProfile(baseUrl, token)
            localStore.saveUserProfile(remoteProfile)
            remoteProfile
        } catch (e: Exception) {
            localStore.getUserProfileOnce() ?: throw e
        }
    }

    suspend fun markNotificationSent(assignmentId: Long) {
        localStore.updateNotificationSent(assignmentId, true)
    }

    suspend fun insertMockAssignment(assignment: Assignment) {
        localStore.insertAssignments(listOf(assignment))
    }

    suspend fun deleteAssignmentById(assignmentId: Long) {
        localStore.deleteAssignmentById(assignmentId)
    }

    suspend fun markNotified24h(assignmentId: Long) {
        localStore.markNotified24h(assignmentId)
    }

    suspend fun markNotified3h(assignmentId: Long) {
        localStore.markNotified3h(assignmentId)
    }

    suspend fun markNotified30m(assignmentId: Long) {
        localStore.markNotified30m(assignmentId)
    }

    suspend fun getDiscussionsForCourses(): List<me.joxquin.notivas.data.model.CanvasDiscussionTopic> = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        val courses = localStore.getAllCourses().first()

        coroutineScope {
            courses.map { course ->
                async {
                    try {
                        val topics = apiService.getDiscussionTopics(
                            baseUrl = baseUrl,
                            token = token,
                            courseId = course.id,
                            perPage = 50
                        )
                        topics.map { topic ->
                            topic.copy(
                                courseId = course.id,
                                courseName = course.name
                            )
                        }
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
        }
    }

    suspend fun getDiscussionTopic(courseId: Long, topicId: Long): me.joxquin.notivas.data.model.CanvasDiscussionTopic? = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext null
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext null
        try {
            val topic = apiService.getDiscussionTopic(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId,
                topicId = topicId
            )
            val courses = localStore.getAllCourses().first()
            val course = courses.find { it.id == courseId }
            topic.copy(
                courseId = courseId,
                courseName = course?.name ?: topic.courseName
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getDiscussionEntries(courseId: Long, topicId: Long): List<me.joxquin.notivas.data.model.CanvasDiscussionEntry> = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        try {
            apiService.getDiscussionEntries(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId,
                topicId = topicId
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun postDiscussionEntry(
        courseId: Long,
        topicId: Long,
        message: String
    ): Result<me.joxquin.notivas.data.model.CanvasDiscussionEntry> = withContext(Dispatchers.IO) {
        val credential = preferencesManager.getAuthCredential()
            ?: return@withContext Result.failure(Exception("No autenticado en Canvas"))
        val baseUrl = preferencesManager.universityUrl.first()
            ?: return@withContext Result.failure(Exception("URL institucional no configurada"))

        val token = when (credential) {
            is CanvasAuthCredential.BearerToken -> "Bearer ${credential.token}"
            is CanvasAuthCredential.SessionCookies -> "Cookie: ${credential.cookies}"
        }
        val csrfToken = when (credential) {
            is CanvasAuthCredential.BearerToken -> null
            is CanvasAuthCredential.SessionCookies -> credential.csrfToken
        }

        try {
            val formattedMessage = me.joxquin.notivas.data.repository.copilot.tools.HtmlUtils.textToHtml(message)
            val result = apiService.postDiscussionEntry(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId,
                topicId = topicId,
                message = formattedMessage,
                csrfToken = csrfToken
            )
            // Registrar evento de actividad en Room
            val course = localStore.courses.firstOrNull()?.find { it.id == courseId }
            localStore.saveActivityLog(
                me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                    type = "FORUM_POST",
                    title = "Participación en Foro Académico",
                    description = "Respuesta publicada en Canvas LMS: \"${message.take(120).trim()}...\"",
                    courseCode = course?.courseCode ?: "Curso $courseId",
                    courseName = course?.name,
                    timestamp = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis(),
                    targetId = topicId,
                    secondaryId = courseId,
                    extraInfo = "ID de entrada #${result.id}"
                )
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitAssignment(
        courseId: Long,
        assignmentId: Long,
        submissionType: String = "online_url",
        url: String? = null,
        bodyText: String? = null,
        fileBytes: ByteArray? = null,
        fileName: String? = null,
        mimeType: String = "application/octet-stream",
        comment: String? = null
    ): Result<SubmissionDetails> = withContext(Dispatchers.IO) {
        val credential = preferencesManager.getAuthCredential()
            ?: return@withContext Result.failure(Exception("No autenticado en Canvas"))
        val baseUrl = preferencesManager.universityUrl.first()
            ?: return@withContext Result.failure(Exception("URL institucional no configurada"))

        val token = when (credential) {
            is CanvasAuthCredential.BearerToken -> "Bearer ${credential.token}"
            is CanvasAuthCredential.SessionCookies -> "Cookie: ${credential.cookies}"
        }
        val csrfToken = when (credential) {
            is CanvasAuthCredential.BearerToken -> null
            is CanvasAuthCredential.SessionCookies -> credential.csrfToken
        }

        try {
            var fileIds: List<Long>? = null
            if (submissionType == "online_upload" && fileBytes != null && !fileName.isNullOrBlank()) {
                val uploadedId = apiService.uploadSubmissionFile(
                    baseUrl = baseUrl,
                    token = token,
                    courseId = courseId,
                    assignmentId = assignmentId,
                    fileName = fileName,
                    fileBytes = fileBytes,
                    mimeType = mimeType,
                    csrfToken = csrfToken
                ) ?: return@withContext Result.failure(Exception("Error al subir archivo a Canvas LMS"))
                fileIds = listOf(uploadedId)
            }

            val formattedBody = bodyText?.let { me.joxquin.notivas.data.repository.copilot.tools.HtmlUtils.textToHtml(it) }
            val result = apiService.submitAssignmentSubmission(
                baseUrl = baseUrl,
                token = token,
                courseId = courseId,
                assignmentId = assignmentId,
                submissionType = submissionType,
                url = url,
                bodyText = formattedBody,
                fileIds = fileIds,
                comment = comment,
                csrfToken = csrfToken
            )
            // Actualizar assignment localmente de inmediato con prevalencia canónica de entrega
            val assignments = localStore.getAssignmentsByCourse(courseId).first()
            val existing = assignments.find { it.id == assignmentId }
            if (existing != null) {
                val updated = existing.copy(
                    isCompleted = true,
                    status = "completed",
                    submittedAt = result.submittedAt ?: me.joxquin.notivas.util.DateTimeUtil.nowLocalDate().toIsoDateString(),
                    submission = result
                )
                localStore.insertAssignments(listOf(updated))
            }
            // Registrar evento de actividad en Room
            val course = localStore.courses.firstOrNull()?.find { it.id == courseId }
            localStore.saveActivityLog(
                me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                    type = "ASSIGNMENT_SUBMISSION",
                    title = existing?.name ?: "Entrega de Tarea",
                    description = if (!url.isNullOrBlank()) "Enlace enviado a Canvas LMS: $url" else "Texto en línea entregado a Canvas LMS",
                    courseCode = course?.courseCode ?: "Curso $courseId",
                    courseName = course?.name,
                    timestamp = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis(),
                    targetId = assignmentId,
                    secondaryId = courseId,
                    extraInfo = result.id?.let { "Recibo #$it" } ?: "Enviado con éxito",
                    actionUrl = url ?: existing?.htmlUrl
                )
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAssignmentDraft(assignmentId: Long): Flow<me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity?> =
        localStore.observeDraftByAssignmentId(assignmentId)

    fun observeAllAssignmentDrafts(): Flow<List<me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity>> =
        localStore.observeAllDrafts()

    suspend fun getAssignmentDraft(assignmentId: Long): me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity? =
        localStore.getDraftByAssignmentId(assignmentId)

    fun saveAssignmentDraft(draft: me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity) {
        localStore.saveAssignmentDraft(draft)
        // Registrar evento de sincronización de borrador con nombre corto de curso si es posible
        val courses = localStore.getCourseList()
        val matchingCourse = courses.find { c ->
            (!draft.courseFolder.isBlank() && (c.name.equals(draft.courseFolder, ignoreCase = true) || c.courseCode.equals(draft.courseFolder, ignoreCase = true)))
        }
        val shortCourseCode = me.joxquin.notivas.util.CourseUtil.extractCourseTag(
            matchingCourse?.courseCode,
            matchingCourse?.name ?: draft.courseFolder
        )
        val courseName = matchingCourse?.name ?: draft.courseFolder

        localStore.saveActivityLog(
            me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                type = "DRIVE_SYNC",
                title = "Borrador de Tarea Sincronizado",
                description = "Archivo \"${draft.fileName}\" guardado en tu carpeta de Google Drive",
                courseCode = shortCourseCode,
                courseName = courseName,
                timestamp = draft.updatedAtTimestamp.takeIf { it > 0 } ?: me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis(),
                targetId = draft.assignmentId,
                secondaryId = matchingCourse?.id,
                extraInfo = draft.draftStatus,
                actionUrl = draft.driveUrl
            )
        )
    }

    suspend fun deleteAssignmentDraft(assignmentId: Long) =
        localStore.deleteAssignmentDraft(assignmentId)

    suspend fun deleteAllAssignmentDrafts() =
        localStore.deleteAllAssignmentDrafts()

    // ─── Actividad Reciente ─────────────────────────────────────────────────
    fun observeActivityLogs(): Flow<List<me.joxquin.notivas.data.local.db.entities.ActivityLogEntity>> =
        localStore.observeActivityLogs()

    suspend fun logActivity(
        type: String,
        title: String,
        description: String,
        courseCode: String? = null,
        courseName: String? = null,
        targetId: Long? = null,
        secondaryId: Long? = null,
        extraInfo: String? = null,
        actionUrl: String? = null
    ) {
        localStore.saveActivityLog(
            me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                type = type,
                title = title,
                description = description,
                courseCode = courseCode,
                courseName = courseName,
                timestamp = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis(),
                targetId = targetId,
                secondaryId = secondaryId,
                extraInfo = extraInfo,
                actionUrl = actionUrl
            )
        )
    }

    suspend fun getAssignment(id: Long): Assignment? =
        localStore.getAssignmentById(id)

    suspend fun seedInitialActivitiesIfEmpty() = withContext(Dispatchers.IO) {
        if (localStore.countActivityLogs() > 0) return@withContext

        val allAssignments = localStore.getAllAssignments().firstOrNull().orEmpty()
        val allDrafts = localStore.observeAllDrafts().firstOrNull().orEmpty()
        val courses = localStore.courses.firstOrNull().orEmpty()

        val seedLogs = mutableListOf<me.joxquin.notivas.data.local.db.entities.ActivityLogEntity>()

        // Sembrar entregas existentes
        val completedAssignments = allAssignments.filter { it.isCompleted }
        completedAssignments.take(8).forEachIndexed { index, a ->
            val course = courses.find { it.id == a.courseId }
            val fakeOffsetHours = (index * 6).toLong()
            val time = (me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis() - fakeOffsetHours * 3600 * 1000).coerceAtLeast(1000L)
            val code = me.joxquin.notivas.util.CourseUtil.extractCourseTag(course?.courseCode, course?.name)
            seedLogs.add(
                me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                    type = "ASSIGNMENT_SUBMISSION",
                    title = a.name,
                    description = "Entrega registrada en Canvas LMS con calificación ${a.submission?.score?.toString() ?: a.pointsPossible?.toString() ?: "completada"}",
                    courseCode = code,
                    courseName = course?.name,
                    timestamp = time,
                    targetId = a.id,
                    secondaryId = a.courseId,
                    extraInfo = a.submission?.id?.let { "Recibo #$it" } ?: "Entrega Canvas ✓",
                    actionUrl = a.htmlUrl
                )
            )
        }

        // Sembrar borradores existentes
        allDrafts.take(4).forEachIndexed { index, d ->
            val assignment = allAssignments.find { it.id == d.assignmentId }
            val course = courses.find { c ->
                (assignment != null && c.id == assignment.courseId) ||
                (!d.courseFolder.isBlank() && (c.name.equals(d.courseFolder, ignoreCase = true) || c.courseCode.equals(d.courseFolder, ignoreCase = true)))
            }
            val shortCode = me.joxquin.notivas.util.CourseUtil.extractCourseTag(
                course?.courseCode,
                course?.name ?: d.courseFolder
            )
            val fakeOffsetHours = ((index + 1) * 14).toLong()
            val time = (me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis() - fakeOffsetHours * 3600 * 1000).coerceAtLeast(1000L)
            seedLogs.add(
                me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                    type = "DRIVE_SYNC",
                    title = "Borrador de Tarea Sincronizado",
                    description = "Archivo \"${d.fileName}\" guardado en tu carpeta de Google Drive",
                    courseCode = shortCode,
                    courseName = course?.name ?: d.courseFolder,
                    timestamp = time,
                    targetId = d.assignmentId,
                    secondaryId = assignment?.courseId,
                    extraInfo = d.draftStatus,
                    actionUrl = d.driveUrl
                )
            )
        }

        if (seedLogs.isNotEmpty()) {
            localStore.saveActivityLogs(seedLogs)
        }
    }

    suspend fun downloadCanvasFileBytes(fileDownloadUrl: String): ByteArray? = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext null
        try {
            apiService.downloadFileBytes(fileDownloadUrl, token)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCanvasFileDownloadUrl(courseId: Long, fileId: Long): String? = withContext(Dispatchers.IO) {
        val token = getAuthToken() ?: return@withContext null
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext null
        try {
            val fileDetail = apiService.getFileDetails(baseUrl, token, courseId, fileId)
            fileDetail.url ?: fileDetail.publicUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun logout() {
        preferencesManager.clear()
        localStore.deleteCourses()
        localStore.deleteAssignments()
        localStore.deletePlannerItems()
        localStore.deleteAllSessions()
        localStore.clearDiskStorage()
    }
}
