package me.joxquin.notivas.data.cloud

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.joxquin.notivas.data.local.db.NotivasDatabase
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimulationCloudSyncTest {

    @Test
    fun testRestoresBackupAfterLogoutAndLoginInSameProcess() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<NotivasDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        val backend = FakeCloudBackend()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            database.courseDao().upsert(CourseEntity(id = 10, name = "Moviles"))
            database.simulationDao().insertGroupsWithItems(listOf(group(courseId = 10, name = "Laboratorios")))
            // Como en la app, el debounce junta las emisiones intermedias de los tres flujos de Room
            // (con 0 se procesaría "curso sin grupos" a mitad del clearAllTables, cosa que la app nunca ve).
            SimulationCloudSync(database, scope, debounceMillis = DEBOUNCE_MS).start(backend)
            assertTrue(awaitUntil { backend.groupNames(10) == listOf("Laboratorios") }, "el curso nunca se respaldó")

            // Cerrar sesión vacía Room, pero el proceso y el SimulationCloudSync de AppModule siguen vivos.
            database.clearAllTables()
            delay(SETTLE_MS)

            // Volver a entrar: el sync de Canvas trae el curso otra vez, todavía sin ponderaciones.
            database.courseDao().upsert(CourseEntity(id = 10, name = "Moviles"))

            assertTrue(
                awaitUntil { database.simulationDao().getGroupsWithItems(10).isNotEmpty() },
                "no se restauró el respaldo; en la nube quedó ${backend.groupNames(10)}"
            )
            assertEquals(listOf("Laboratorios"), backend.groupNames(10))
            assertTrue(backend.errors.isEmpty(), "errores inesperados: ${backend.errors}")
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test
    fun testKeepsRetryingAfterConsecutiveFailures() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<NotivasDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        // Sin red al abrir la app: falla el sync inicial y los dos primeros reintentos.
        val backend = FakeCloudBackend(failingFetches = 3)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            database.courseDao().upsert(CourseEntity(id = 10, name = "Moviles"))
            database.simulationDao().insertGroupsWithItems(listOf(group(courseId = 10, name = "Laboratorios")))
            SimulationCloudSync(
                database, scope,
                debounceMillis = DEBOUNCE_MS,
                retryDelayMillis = RETRY_MS,
                maxRetryDelayMillis = RETRY_MS * 4
            ).start(backend)

            assertTrue(
                awaitUntil { backend.groupNames(10) == listOf("Laboratorios") },
                "dejó de reintentar después de ${backend.errors.size} fallos seguidos"
            )
            assertEquals(3, backend.errors.size)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    private fun group(courseId: Long, name: String) = SimulationGroupWithItems(
        group = SimulationGroup(courseId = courseId, name = name, weightPercentage = 40f),
        items = listOf(SimulationItem(groupId = 0, name = "$name 1", isPlaceholder = true))
    )

    private suspend fun awaitUntil(condition: suspend () -> Boolean): Boolean =
        withTimeoutOrNull(TIMEOUT_MS) {
            while (!condition()) delay(POLL_MS)
            true
        } ?: false

    /** Firestore en memoria: al leer devuelve lo último que se subió. Las primeras lecturas pueden fallar. */
    private class FakeCloudBackend(failingFetches: Int = 0) : CloudBackend {
        private val courses = ConcurrentHashMap<Long, Map<String, Any?>>()
        private val pendingFailures = AtomicInteger(failingFetches)
        val errors = CopyOnWriteArrayList<Throwable>()

        override val googleWebClientId: String? = null
        override val isLinkedWithGoogle = false
        override suspend fun ensureSignedIn() = USER_ID
        override suspend fun linkWithGoogle(idToken: String) = USER_ID
        override suspend fun fetchCourses(userId: String): Map<Long, Map<String, Any?>> {
            if (pendingFailures.getAndDecrement() > 0) throw IllegalStateException("sin conexión con Firestore")
            return courses.toMap()
        }
        override fun saveCourse(userId: String, courseId: Long, data: Map<String, Any?>) {
            courses[courseId] = data
        }
        override fun recordError(error: Throwable) {
            errors += error
        }
        override suspend fun <T> trace(name: String, block: suspend () -> T): T = block()

        fun groupNames(courseId: Long): List<String>? =
            courses[courseId]?.let { CloudCourseSnapshot.fromMap(it) }?.groups?.map { it.name }
    }

    private companion object {
        const val USER_ID = "student"
        const val DEBOUNCE_MS = 300L
        const val RETRY_MS = 100L
        const val SETTLE_MS = 1_500L
        const val POLL_MS = 50L
        const val TIMEOUT_MS = 10_000L
    }
}
