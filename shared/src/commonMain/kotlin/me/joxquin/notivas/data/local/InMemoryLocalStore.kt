package me.joxquin.notivas.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.db.DatabaseMigrator
import me.joxquin.notivas.data.local.db.NotivasDatabase
import me.joxquin.notivas.data.local.db.createRoomDatabase
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CopilotMessageEntity
import me.joxquin.notivas.data.local.db.entities.CopilotSessionEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.local.db.entities.PlannerItemEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupEntity
import me.joxquin.notivas.data.local.db.entities.SimulationItemEntity
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CopilotMessage
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.util.DateTimeUtil

/**
 * Almacén de datos respaldado por SQLite mediante Room Multiplatform.
 * Mantiene compatibilidad total con la API reactiva y ejecuta operaciones atómicas en SQLite.
 */
class InMemoryLocalStore(
    val database: NotivasDatabase = createRoomDatabase(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val userProfileFile: String by lazy {
        "${FileStorageProvider.getAppDataDirectory()}/notivas_profile.json"
    }

    // ─── Perfil de Usuario ──────────────────────────────────────────────────
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: Flow<UserProfile?> = _userProfile.asStateFlow()

    fun getUserProfileOnce(): UserProfile? = _userProfile.value

    fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        scope.launch {
            try {
                PlatformFileSystem.writeString(userProfileFile, json.encodeToString(UserProfile.serializer(), profile))
            } catch (_: Exception) {}
        }
    }

    fun clearUserProfile() {
        _userProfile.value = null
        scope.launch {
            try {
                PlatformFileSystem.deleteFile(userProfileFile)
            } catch (_: Exception) {}
        }
    }

    // ─── Cursos (Room) ──────────────────────────────────────────────────────
    val courses: Flow<List<Course>> = database.courseDao().observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getAllCourses(): Flow<List<Course>> = courses

    fun getCourseList(): List<Course> {
        return kotlinx.coroutines.runBlocking {
            database.courseDao().getAll().map { it.toDomain() }
        }
    }

    fun upsertCourses(newCourses: List<Course>) {
        scope.launch {
            database.courseDao().upsert(newCourses.map { CourseEntity.fromDomain(it) })
        }
    }

    fun deleteCourses() {
        scope.launch {
            database.courseDao().deleteAll()
        }
    }

    // ─── Tareas / Assignments (Room) ────────────────────────────────────────
    val assignments: Flow<List<Assignment>> = database.assignmentDao().observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getAllAssignments(): Flow<List<Assignment>> = assignments

    fun getAssignmentList(): List<Assignment> {
        return kotlinx.coroutines.runBlocking {
            database.assignmentDao().getAll().map { it.toDomain() }
        }
    }

    fun getAssignmentsByCourse(courseId: Long): Flow<List<Assignment>> =
        database.assignmentDao().observeByCourse(courseId).map { list ->
            list.map { it.toDomain() }
        }

    fun getAssignmentsForCourseOnce(courseId: Long): List<Assignment> {
        return kotlinx.coroutines.runBlocking {
            database.assignmentDao().getByCourse(courseId).map { it.toDomain() }
        }
    }

    fun insertAssignments(items: List<Assignment>) {
        scope.launch {
            database.assignmentDao().upsert(items.map { AssignmentEntity.fromDomain(it) })
        }
    }

    fun updateNotificationSent(id: Long, sent: Boolean) {
        scope.launch {
            val existing = database.assignmentDao().getById(id) ?: return@launch
            database.assignmentDao().upsert(existing.copy(notificationSent = sent))
        }
    }

    fun markNotified24h(id: Long) {
        scope.launch {
            val existing = database.assignmentDao().getById(id) ?: return@launch
            database.assignmentDao().upsert(existing.copy(notified24h = true))
        }
    }

    fun markNotified3h(id: Long) {
        scope.launch {
            val existing = database.assignmentDao().getById(id) ?: return@launch
            database.assignmentDao().upsert(existing.copy(notified3h = true))
        }
    }

    fun markNotified30m(id: Long) {
        scope.launch {
            val existing = database.assignmentDao().getById(id) ?: return@launch
            database.assignmentDao().upsert(existing.copy(notified30m = true))
        }
    }

    fun deleteAssignmentById(id: Long) {
        scope.launch {
            val existing = database.assignmentDao().getById(id) ?: return@launch
            database.assignmentDao().deleteByCourse(existing.courseId)
        }
    }

    fun deleteAssignments() {
        scope.launch {
            database.assignmentDao().deleteAll()
        }
    }

    // ─── Agenda y Planificación (Room) ──────────────────────────────────────
    val plannerItems: Flow<List<PlannerItem>> = database.plannerDao().observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getAllPlannerItems(): Flow<List<PlannerItem>> = plannerItems

    fun getPlannerItemsByCourse(courseId: Long): Flow<List<PlannerItem>> =
        database.plannerDao().observeAll().map { list ->
            list.filter { it.courseId == courseId }.map { it.toDomain() }
        }

    fun insertPlannerItems(items: List<PlannerItem>) {
        scope.launch {
            database.plannerDao().upsert(items.map { PlannerItemEntity.fromDomain(it) })
        }
    }

    fun deletePlannerItems() {
        scope.launch {
            database.plannerDao().deleteAll()
        }
    }

    // ─── Simulador de Notas (Room) ──────────────────────────────────────────
    fun getGroupsWithItemsByCourse(courseId: Long): Flow<List<SimulationGroupWithItems>> =
        database.simulationDao().observeGroupsWithItems(courseId).map { relations ->
            relations.map { it.toDomain() }
        }

    fun insertGroup(group: SimulationGroup): Long {
        val groupEntity = SimulationGroupEntity.fromDomain(group)
        return kotlinx.coroutines.runBlocking {
            database.simulationDao().insertGroup(groupEntity)
        }
    }

    fun updateGroup(group: SimulationGroup) {
        scope.launch {
            database.simulationDao().updateGroup(SimulationGroupEntity.fromDomain(group))
        }
    }

    fun deleteGroup(group: SimulationGroup) {
        scope.launch {
            database.simulationDao().deleteGroup(group.id)
        }
    }

    fun insertItem(item: SimulationItem): Long {
        val itemEntity = SimulationItemEntity.fromDomain(item)
        return kotlinx.coroutines.runBlocking {
            database.simulationDao().insertItem(itemEntity)
        }
    }

    fun updateItem(item: SimulationItem) {
        scope.launch {
            database.simulationDao().updateItem(SimulationItemEntity.fromDomain(item))
        }
    }

    fun deleteItem(item: SimulationItem) {
        scope.launch {
            database.simulationDao().deleteItem(item.id)
        }
    }

    fun updateItemScore(itemId: Long, score: Float, isSimulated: Boolean = true) {
        scope.launch {
            val allItems = database.simulationDao().getAllItems()
            val existing = allItems.find { it.id == itemId } ?: return@launch
            database.simulationDao().updateItem(
                existing.copy(
                    simulatedScore = score,
                    isSimulated = isSimulated
                )
            )
        }
    }

    fun linkItemWithCanvasAssignment(itemId: Long, canvasAssignmentId: Long, name: String, manualScore: Float? = null) {
        scope.launch {
            val allItems = database.simulationDao().getAllItems()
            val existing = allItems.find { it.id == itemId } ?: return@launch
            database.simulationDao().updateItem(
                existing.copy(
                    canvasAssignmentId = canvasAssignmentId,
                    isPlaceholder = false,
                    name = name,
                    manualScore = manualScore ?: existing.manualScore
                )
            )
        }
    }

    fun replaceCourseGroupsWithTemplate(
        course: Course,
        groupsWithItems: List<SimulationGroupWithItems>
    ) {
        scope.launch {
            database.courseDao().upsert(CourseEntity.fromDomain(course))
            database.simulationDao().deleteGroupsForCourse(course.id)
            groupsWithItems.forEach { g ->
                val groupEntity = SimulationGroupEntity.fromDomain(g.group.copy(id = 0L, courseId = course.id))
                val groupId = database.simulationDao().insertGroup(groupEntity)
                val itemEntities = g.items.map { item ->
                    SimulationItemEntity.fromDomain(item.copy(id = 0L, groupId = groupId))
                }
                database.simulationDao().insertItems(itemEntities)
            }
        }
    }

    fun createGroupWithItems(
        group: SimulationGroup,
        items: List<SimulationItem>
    ) {
        scope.launch {
            val groupEntity = SimulationGroupEntity.fromDomain(group.copy(id = 0L))
            val groupId = database.simulationDao().insertGroup(groupEntity)
            val itemEntities = items.map { SimulationItemEntity.fromDomain(it.copy(id = 0L, groupId = groupId)) }
            database.simulationDao().insertItems(itemEntities)
        }
    }


    // ─── Copilot Chat (Room) ────────────────────────────────────────────────
    fun getAllSessions(): Flow<List<CopilotSession>> =
        database.copilotChatDao().observeSessions().map { list ->
            list.map { it.toDomain() }
        }

    fun getSessionById(sessionId: String): CopilotSession? {
        return kotlinx.coroutines.runBlocking {
            database.copilotChatDao().getSessionById(sessionId)?.toDomain()
        }
    }

    fun getMessagesForSession(sessionId: String): Flow<List<CopilotMessage>> =
        database.copilotChatDao().observeMessages(sessionId).map { list ->
            list.map { it.toDomain() }
        }

    fun getMessagesForSessionOnce(sessionId: String): List<CopilotMessage> {
        return kotlinx.coroutines.runBlocking {
            database.copilotChatDao().getMessages(sessionId).map { it.toDomain() }
        }
    }

    fun insertSession(session: CopilotSession) {
        scope.launch {
            database.copilotChatDao().upsertSession(CopilotSessionEntity.fromDomain(session))
        }
    }

    fun insertMessage(message: CopilotMessage) {
        scope.launch {
            database.copilotChatDao().insertMessage(CopilotMessageEntity.fromDomain(message))
        }
    }

    fun insertMessages(messages: List<CopilotMessage>) {
        scope.launch {
            database.copilotChatDao().insertMessages(messages.map { CopilotMessageEntity.fromDomain(it) })
        }
    }

    fun updateSessionTitle(sessionId: String, newTitle: String, updatedAt: Long = DateTimeUtil.nowEpochMillis()) {
        scope.launch {
            val session = database.copilotChatDao().getSessionById(sessionId) ?: return@launch
            database.copilotChatDao().upsertSession(session.copy(title = newTitle, updatedAt = updatedAt))
        }
    }

    fun updateSessionTokens(sessionId: String, tokens: Int, updatedAt: Long = DateTimeUtil.nowEpochMillis()) {
        scope.launch {
            val session = database.copilotChatDao().getSessionById(sessionId) ?: return@launch
            database.copilotChatDao().upsertSession(session.copy(totalTokens = tokens, updatedAt = updatedAt))
        }
    }

    fun updateSessionTimestamp(sessionId: String, updatedAt: Long = DateTimeUtil.nowEpochMillis()) {
        scope.launch {
            val session = database.copilotChatDao().getSessionById(sessionId) ?: return@launch
            database.copilotChatDao().upsertSession(session.copy(updatedAt = updatedAt))
        }
    }

    fun deleteSession(sessionId: String) {
        scope.launch {
            database.copilotChatDao().deleteSession(sessionId)
        }
    }

    fun deleteMessagesForSession(sessionId: String) {
        scope.launch {
            database.copilotChatDao().deleteMessagesForSession(sessionId)
        }
    }

    fun deleteAllSessions() {
        scope.launch {
            database.copilotChatDao().deleteAllSessions()
            database.copilotChatDao().deleteAllMessages()
        }
    }

    // ─── Inicialización y Migración Automática ──────────────────────────────
    init {
        scope.launch {
            loadUserProfile()
            DatabaseMigrator(database).migrateIfNeeded()
        }
    }

    private fun loadUserProfile() {
        try {
            val content = PlatformFileSystem.readString(userProfileFile) ?: return
            _userProfile.value = json.decodeFromString<UserProfile>(content)
        } catch (_: Exception) {}
    }

    fun clearDiskStorage() {
        clearUserProfile()
        scope.launch {
            database.courseDao().deleteAll()
            database.assignmentDao().deleteAll()
            database.plannerDao().deleteAll()
            database.copilotChatDao().deleteAllSessions()
            database.copilotChatDao().deleteAllMessages()
            database.simulationDao().deleteAllGroups()
            database.simulationDao().deleteAllItems()
        }
    }
}


