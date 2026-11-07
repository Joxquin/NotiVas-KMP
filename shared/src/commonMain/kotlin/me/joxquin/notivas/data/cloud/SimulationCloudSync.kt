package me.joxquin.notivas.data.cloud

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.joxquin.notivas.data.local.db.NotivasDatabase
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupWithItemsRelation
import me.joxquin.notivas.domain.sync.CloudSyncPlanner
import me.joxquin.notivas.util.AppDispatchers

/**
 * Respaldo de ponderaciones y notas predictivas en la nube.
 * Observa Room sin modificar el flujo actual del simulador: cada cambio local se sube tras una
 * pausa corta y los cursos vacíos se restauran cuando el usuario reinstala o vuelve a iniciar sesión.
 * Las notas reales siguen viniendo de Canvas.
 */
class SimulationCloudSync(
    private val database: NotivasDatabase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + AppDispatchers.IO),
    private val debounceMillis: Long = DEBOUNCE_MS
) {
    private val mutex = Mutex()
    private var backend: CloudBackend? = null
    private var userId: String? = null
    private var remote: MutableMap<Long, CloudCourseSnapshot>? = null
    private val seenLocally = mutableSetOf<Long>()
    private var latestState: LocalState? = null
    private var retryJob: Job? = null

    val googleWebClientId: String? get() = backend?.googleWebClientId
    val isLinkedWithGoogle: Boolean get() = backend?.isLinkedWithGoogle == true

    @OptIn(FlowPreview::class)
    fun start(backend: CloudBackend) {
        if (this.backend != null) return
        this.backend = backend
        scope.launch {
            combine(
                database.courseDao().observeAll(),
                database.assignmentDao().observeAll(),
                database.simulationDao().observeAllGroupsWithItems()
            ) { courses, assignments, groups -> LocalState.from(courses, assignments, groups) }
                .distinctUntilChanged()
                .debounce(debounceMillis)
                .collect { state ->
                    mutex.withLock {
                        latestState = state
                        sync(backend, state)
                    }
                }
        }
    }

    /** Llamado tras el login de Google de Drive: une la cuenta anónima con la de Google. */
    fun onGoogleIdToken(idToken: String) {
        val backend = backend ?: return
        scope.launch {
            mutex.withLock {
                val newUserId = try {
                    backend.linkWithGoogle(idToken)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    backend.recordError(e)
                    return@withLock
                }
                if (newUserId != userId) {
                    // Otra cuenta: hay que volver a leer su respaldo antes de subir o restaurar.
                    userId = newUserId
                    remote = null
                    seenLocally.clear()
                }
                latestState?.let { sync(backend, it) }
            }
        }
    }

    private suspend fun sync(backend: CloudBackend, state: LocalState) {
        // Un curso que salió de Room (sesión cerrada u otro ciclo) deja de contar como visto:
        // si vuelve sin grupos se restaura desde la nube en vez de subirse vacío.
        seenLocally.retainAll(state.courseNames.keys)
        try {
            val uid = userId ?: backend.ensureSignedIn().also { userId = it }
            val known = remote ?: backend.trace(TRACE_FETCH) { backend.fetchCourses(uid) }
                .mapNotNull { (courseId, data) -> CloudCourseSnapshot.fromMap(data)?.let { courseId to it } }
                .toMap(mutableMapOf())
                .also { remote = it }

            val plan = CloudSyncPlanner.plan(
                localCourseIds = state.courseNames.keys,
                localSnapshots = state.snapshots,
                coursesWithAssignments = state.assignmentIdsByCourse.keys,
                remote = known,
                seenLocally = seenLocally
            )
            for (snapshot in plan.restores) {
                val assignmentIds = state.assignmentIdsByCourse[snapshot.courseId].orEmpty()
                backend.trace(TRACE_RESTORE) {
                    database.simulationDao().insertGroupsWithItems(snapshot.toLocalGroups(assignmentIds))
                }
                known[snapshot.courseId] = snapshot
                seenLocally += snapshot.courseId
            }
            for (snapshot in plan.pushes) {
                backend.saveCourse(uid, snapshot.courseId, snapshot.toMap(state.courseNames[snapshot.courseId]))
                known[snapshot.courseId] = snapshot
            }
            seenLocally += state.snapshots.keys
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            backend.recordError(e)
            scheduleRetry(backend)
        }
    }

    private fun scheduleRetry(backend: CloudBackend) {
        if (retryJob?.isActive == true) return
        retryJob = scope.launch {
            delay(RETRY_DELAY_MS)
            mutex.withLock { latestState?.let { sync(backend, it) } }
        }
    }

    private data class LocalState(
        val courseNames: Map<Long, String>,
        val assignmentIdsByCourse: Map<Long, Set<Long>>,
        val snapshots: Map<Long, CloudCourseSnapshot>
    ) {
        companion object {
            fun from(
                courses: List<CourseEntity>,
                assignments: List<AssignmentEntity>,
                groups: List<SimulationGroupWithItemsRelation>
            ) = LocalState(
                courseNames = courses.associate { it.id to it.name },
                assignmentIdsByCourse = assignments.groupBy({ it.courseId }, { it.id }).mapValues { it.value.toSet() },
                snapshots = groups.map { it.toDomain() }
                    .groupBy { it.group.courseId }
                    .mapValues { (courseId, list) -> CloudCourseSnapshot.fromLocal(courseId, list) }
            )
        }
    }

    companion object {
        const val DEBOUNCE_MS = 3_000L
        const val RETRY_DELAY_MS = 60_000L
        const val TRACE_FETCH = "cloud_fetch_courses"
        const val TRACE_RESTORE = "cloud_restore_course"
    }
}
