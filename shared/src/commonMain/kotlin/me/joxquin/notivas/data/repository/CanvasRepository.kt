package me.joxquin.notivas.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CanvasModule
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
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
    val allCourses: Flow<List<Course>> = localStore.getAllCourses()
    val allAssignments: Flow<List<Assignment>> = localStore.getAllAssignments()
    val allPlannerItems: Flow<List<PlannerItem>> = localStore.getAllPlannerItems()
    val universityUrl: Flow<String?> = preferencesManager.universityUrl

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

    suspend fun createGroupWithItems(group: SimulationGroup, items: List<SimulationItem>) {
        localStore.createGroupWithItems(group, items)
    }

    suspend fun linkSimulationItemWithCanvas(itemId: Long, assignmentId: Long, name: String, manualScore: Float? = null) =
        localStore.linkItemWithCanvasAssignment(itemId, assignmentId, name, manualScore)

    suspend fun fetchCourseModules(courseId: Long): List<CanvasModule> = withContext(Dispatchers.IO) {
        val rawToken = preferencesManager.accessToken.first() ?: return@withContext emptyList()
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext emptyList()
        return@withContext try {
            apiService.getModulesWithItems(
                baseUrl = baseUrl,
                token = "Bearer $rawToken",
                courseId = courseId
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAndSaveData() = withContext(Dispatchers.IO) {
        val rawToken = preferencesManager.accessToken.first() ?: return@withContext
        val baseUrl = preferencesManager.universityUrl.first() ?: return@withContext
        val token = "Bearer $rawToken"

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

            // 2. Fetch Active Courses
            val allCoursesList = apiService.getCourses(
                baseUrl = baseUrl,
                token = token
            )
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
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun verifyAndSave(url: String, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.verifyToken(url, "Bearer $token")
            preferencesManager.saveUniversityUrl(url)
            preferencesManager.saveAccessToken(token)
            true
        } catch (e: Exception) {
            println("CanvasRepository: verifyAndSave failed for url='$url': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun completeOnboarding() {
        preferencesManager.setOnboardingCompleted(true)
    }

    suspend fun getProfile(): UserProfile = withContext(Dispatchers.IO) {
        val baseUrl = preferencesManager.universityUrl.first() ?: ""
        val token = "Bearer ${preferencesManager.accessToken.first()}"
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

    suspend fun logout() {
        preferencesManager.clear()
        localStore.deleteCourses()
        localStore.deleteAssignments()
        localStore.deletePlannerItems()
        localStore.deleteAllSessions()
        localStore.clearDiskStorage()
    }
}
