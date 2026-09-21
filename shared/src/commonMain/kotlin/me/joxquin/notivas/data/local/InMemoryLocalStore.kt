package me.joxquin.notivas.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CopilotMessage
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.model.UserProfile

/**
 * Almacén local reactivo con persistencia automática en disco (Android & Desktop).
 * Combina un SSOT en memoria ultrarrápido con serialización asíncrona a archivo JSON.
 */
class InMemoryLocalStore(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val storageFile: String by lazy {
        "${FileStorageProvider.getAppDataDirectory()}/notivas_store.json"
    }

    private var saveDebounceJob: Job? = null

    // ─── Perfil de Usuario ──────────────────────────────────────────────────
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: Flow<UserProfile?> = _userProfile.asStateFlow()

    fun getUserProfileOnce(): UserProfile? = _userProfile.value

    fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        schedulePersist()
    }

    fun clearUserProfile() {
        _userProfile.value = null
        schedulePersist()
    }

    // ─── Cursos ─────────────────────────────────────────────────────────────
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: Flow<List<Course>> = _courses.asStateFlow()

    fun getAllCourses(): Flow<List<Course>> = courses

    fun getCourseList(): List<Course> = _courses.value

    fun upsertCourses(newCourses: List<Course>) {
        val currentMap = _courses.value.associateBy { it.id }.toMutableMap()
        for (c in newCourses) {
            currentMap[c.id] = c
        }
        _courses.value = currentMap.values.toList()
        schedulePersist()
    }

    fun deleteCourses() {
        _courses.value = emptyList()
        schedulePersist()
    }

    // ─── Tareas (Assignments) ───────────────────────────────────────────────
    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: Flow<List<Assignment>> = _assignments.asStateFlow()

    fun getAllAssignments(): Flow<List<Assignment>> =
        _assignments.map { list -> list.sortedBy { it.dueAt ?: "" } }

    fun getAssignmentList(): List<Assignment> =
        _assignments.value.sortedBy { it.dueAt ?: "" }

    fun getAssignmentsByCourse(courseId: Long): Flow<List<Assignment>> =
        _assignments.map { list ->
            list.filter { it.courseId == courseId }.sortedBy { it.dueAt ?: "" }
        }

    fun getAssignmentsForCourseOnce(courseId: Long): List<Assignment> =
        _assignments.value.filter { it.courseId == courseId }.sortedBy { it.dueAt ?: "" }

    fun insertAssignments(items: List<Assignment>) {
        val currentMap = _assignments.value.associateBy { it.id }.toMutableMap()
        for (item in items) {
            currentMap[item.id] = item
        }
        _assignments.value = currentMap.values.toList()
        schedulePersist()
    }

    fun updateNotificationSent(id: Long, sent: Boolean) {
        _assignments.value = _assignments.value.map {
            if (it.id == id) it.copy(notificationSent = sent) else it
        }
        schedulePersist()
    }

    fun markNotified24h(id: Long) {
        _assignments.value = _assignments.value.map {
            if (it.id == id) it.copy(notified24h = true) else it
        }
        schedulePersist()
    }

    fun markNotified3h(id: Long) {
        _assignments.value = _assignments.value.map {
            if (it.id == id) it.copy(notified3h = true) else it
        }
        schedulePersist()
    }

    fun markNotified30m(id: Long) {
        _assignments.value = _assignments.value.map {
            if (it.id == id) it.copy(notified30m = true) else it
        }
        schedulePersist()
    }

    fun deleteAssignmentById(id: Long) {
        _assignments.value = _assignments.value.filterNot { it.id == id }
        schedulePersist()
    }

    fun deleteAssignments() {
        _assignments.value = emptyList()
        schedulePersist()
    }

    // ─── Agenda y Planificación (Planner Items) ──────────────────────────────
    private val _plannerItems = MutableStateFlow<List<PlannerItem>>(emptyList())
    val plannerItems: Flow<List<PlannerItem>> = _plannerItems.asStateFlow()

    fun getAllPlannerItems(): Flow<List<PlannerItem>> =
        _plannerItems.map { list -> list.sortedBy { it.plannableDate ?: "" } }

    fun getPlannerItemsByCourse(courseId: Long): Flow<List<PlannerItem>> =
        _plannerItems.map { list ->
            list.filter { it.courseId == courseId }.sortedBy { it.plannableDate ?: "" }
        }

    fun insertPlannerItems(items: List<PlannerItem>) {
        val currentMap = _plannerItems.value.associateBy { it.plannableId }.toMutableMap()
        for (item in items) {
            currentMap[item.plannableId] = item
        }
        _plannerItems.value = currentMap.values.toList()
        schedulePersist()
    }

    fun deletePlannerItems() {
        _plannerItems.value = emptyList()
        schedulePersist()
    }

    // ─── Simulador de Notas ──────────────────────────────────────────────────
    private val _simulationGroups = MutableStateFlow<List<SimulationGroup>>(emptyList())
    private val _simulationItems = MutableStateFlow<List<SimulationItem>>(emptyList())

    fun getGroupsWithItemsByCourse(courseId: Long): Flow<List<SimulationGroupWithItems>> =
        _simulationGroups.map { groups ->
            val items = _simulationItems.value
            groups.filter { it.courseId == courseId }.map { g ->
                SimulationGroupWithItems(
                    group = g,
                    items = items.filter { it.groupId == g.id }
                )
            }
        }

    fun insertGroup(group: SimulationGroup): Long {
        val newId = if (group.id == 0L) (DateTimeUtilMillis() + _simulationGroups.value.size) else group.id
        val created = group.copy(id = newId)
        _simulationGroups.value = _simulationGroups.value + created
        schedulePersist()
        return newId
    }

    fun updateGroup(group: SimulationGroup) {
        _simulationGroups.value = _simulationGroups.value.map {
            if (it.id == group.id) group else it
        }
        schedulePersist()
    }

    fun deleteGroup(group: SimulationGroup) {
        _simulationGroups.value = _simulationGroups.value.filterNot { it.id == group.id }
        _simulationItems.value = _simulationItems.value.filterNot { it.groupId == group.id }
        schedulePersist()
    }

    fun insertItem(item: SimulationItem): Long {
        val newId = if (item.id == 0L) (DateTimeUtilMillis() + _simulationItems.value.size) else item.id
        val created = item.copy(id = newId)
        _simulationItems.value = _simulationItems.value + created
        schedulePersist()
        return newId
    }

    fun updateItem(item: SimulationItem) {
        _simulationItems.value = _simulationItems.value.map {
            if (it.id == item.id) item else it
        }
        schedulePersist()
    }

    fun deleteItem(item: SimulationItem) {
        _simulationItems.value = _simulationItems.value.filterNot { it.id == item.id }
        schedulePersist()
    }

    fun updateItemScore(itemId: Long, score: Float) {
        _simulationItems.value = _simulationItems.value.map {
            if (it.id == itemId) it.copy(simulatedScore = score) else it
        }
        schedulePersist()
    }

    fun linkItemWithCanvasAssignment(itemId: Long, canvasAssignmentId: Long, name: String) {
        _simulationItems.value = _simulationItems.value.map {
            if (it.id == itemId) it.copy(canvasAssignmentId = canvasAssignmentId, isPlaceholder = false, name = name) else it
        }
        schedulePersist()
    }

    // ─── Copilot Chat (Sesiones y Mensajes) ──────────────────────────────────
    private val _copilotSessions = MutableStateFlow<List<CopilotSession>>(emptyList())
    private val _copilotMessages = MutableStateFlow<List<CopilotMessage>>(emptyList())

    fun getAllSessions(): Flow<List<CopilotSession>> =
        _copilotSessions.map { list -> list.sortedByDescending { it.updatedAt } }

    fun getSessionById(sessionId: String): CopilotSession? =
        _copilotSessions.value.find { it.id == sessionId }

    fun getMessagesForSession(sessionId: String): Flow<List<CopilotMessage>> =
        _copilotMessages.map { list ->
            list.filter { it.sessionId == sessionId }.sortedBy { it.timestamp }
        }

    fun getMessagesForSessionOnce(sessionId: String): List<CopilotMessage> =
        _copilotMessages.value.filter { it.sessionId == sessionId }.sortedBy { it.timestamp }

    fun insertSession(session: CopilotSession) {
        _copilotSessions.value = _copilotSessions.value.filterNot { it.id == session.id } + session
        schedulePersist()
    }

    fun insertMessage(message: CopilotMessage) {
        _copilotMessages.value = _copilotMessages.value.filterNot { it.id == message.id } + message
        schedulePersist()
    }

    fun insertMessages(messages: List<CopilotMessage>) {
        val map = _copilotMessages.value.associateBy { it.id }.toMutableMap()
        for (m in messages) map[m.id] = m
        _copilotMessages.value = map.values.toList()
        schedulePersist()
    }

    fun updateSessionTitle(sessionId: String, newTitle: String, updatedAt: Long = DateTimeUtilMillis()) {
        _copilotSessions.value = _copilotSessions.value.map {
            if (it.id == sessionId) it.copy(title = newTitle, updatedAt = updatedAt) else it
        }
        schedulePersist()
    }

    fun updateSessionTokens(sessionId: String, tokens: Int, updatedAt: Long = DateTimeUtilMillis()) {
        _copilotSessions.value = _copilotSessions.value.map {
            if (it.id == sessionId) it.copy(totalTokens = tokens, updatedAt = updatedAt) else it
        }
        schedulePersist()
    }

    fun updateSessionTimestamp(sessionId: String, updatedAt: Long = DateTimeUtilMillis()) {
        _copilotSessions.value = _copilotSessions.value.map {
            if (it.id == sessionId) it.copy(updatedAt = updatedAt) else it
        }
        schedulePersist()
    }

    fun deleteSession(sessionId: String) {
        _copilotSessions.value = _copilotSessions.value.filterNot { it.id == sessionId }
        _copilotMessages.value = _copilotMessages.value.filterNot { it.sessionId == sessionId }
        schedulePersist()
    }

    fun deleteMessagesForSession(sessionId: String) {
        _copilotMessages.value = _copilotMessages.value.filterNot { it.sessionId == sessionId }
        schedulePersist()
    }

    fun deleteAllSessions() {
        _copilotSessions.value = emptyList()
        _copilotMessages.value = emptyList()
        schedulePersist()
    }

    // ─── Carga y Persistencia en Disco ───────────────────────────────────────
    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            val content = PlatformFileSystem.readString(storageFile) ?: return
            val snapshot = json.decodeFromString<PersistentStoreSnapshot>(content)
            _userProfile.value = snapshot.userProfile
            _courses.value = snapshot.courses
            _assignments.value = snapshot.assignments
            _plannerItems.value = snapshot.plannerItems
            _simulationGroups.value = snapshot.simulationGroups
            _simulationItems.value = snapshot.simulationItems
            _copilotSessions.value = snapshot.copilotSessions
            _copilotMessages.value = snapshot.copilotMessages
        } catch (_: Exception) {
        }
    }

    private fun schedulePersist() {
        saveDebounceJob?.cancel()
        saveDebounceJob = scope.launch {
            delay(150)
            persistToDisk()
        }
    }

    private fun persistToDisk() {
        try {
            val snapshot = PersistentStoreSnapshot(
                userProfile = _userProfile.value,
                courses = _courses.value,
                assignments = _assignments.value,
                plannerItems = _plannerItems.value,
                simulationGroups = _simulationGroups.value,
                simulationItems = _simulationItems.value,
                copilotSessions = _copilotSessions.value,
                copilotMessages = _copilotMessages.value
            )
            val jsonString = json.encodeToString(PersistentStoreSnapshot.serializer(), snapshot)
            PlatformFileSystem.writeString(storageFile, jsonString)
        } catch (_: Exception) {
        }
    }

    fun clearDiskStorage() {
        PlatformFileSystem.deleteFile(storageFile)
    }

    private fun DateTimeUtilMillis(): Long {
        return me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
    }
}
