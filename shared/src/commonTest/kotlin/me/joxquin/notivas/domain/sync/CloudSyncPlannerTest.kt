package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.cloud.CloudCourseSnapshot
import me.joxquin.notivas.data.cloud.CloudGroup
import me.joxquin.notivas.data.cloud.CloudItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CloudSyncPlannerTest {

    private fun item(name: String, score: Float, canvasId: Long? = null) = CloudItem(
        name = name,
        canvasAssignmentId = canvasId,
        isPlaceholder = canvasId == null,
        weekNumber = null,
        manualScore = null,
        simulatedScore = score,
        isSimulated = true,
        maxScore = 20f,
        internalWeight = 1f,
        orderIndex = 0
    )

    private fun snapshot(courseId: Long, weight: Float = 40f, canvasId: Long? = null) = CloudCourseSnapshot(
        courseId = courseId,
        groups = listOf(
            CloudGroup(
                name = "Laboratorios",
                weightPercentage = weight,
                targetAssessments = 1,
                dropLowest = false,
                minToDrop = 3,
                calculationMode = "SIMPLE",
                orderIndex = 0,
                items = listOf(item("Lab 1", 15f, canvasId))
            )
        )
    )

    @Test
    fun testPushesLocalCourseMissingInCloud() {
        val local = snapshot(100L)
        val plan = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = mapOf(100L to local),
            coursesWithAssignments = emptySet(),
            remote = emptyMap(),
            seenLocally = emptySet()
        )
        assertEquals(listOf(local), plan.pushes)
        assertTrue(plan.restores.isEmpty())
    }

    @Test
    fun testSkipsCourseAlreadyEqualInCloud() {
        val plan = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = mapOf(100L to snapshot(100L)),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to snapshot(100L)),
            seenLocally = emptySet()
        )
        assertTrue(plan.isEmpty)
    }

    @Test
    fun testLocalChangeWinsOverCloud() {
        val local = snapshot(100L, weight = 60f)
        val plan = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = mapOf(100L to local),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to snapshot(100L, weight = 40f)),
            seenLocally = setOf(100L)
        )
        assertEquals(listOf(local), plan.pushes)
    }

    @Test
    fun testRestoresEmptyCourseAfterReinstall() {
        val cloud = snapshot(100L)
        val plan = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = emptyMap(),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to cloud),
            seenLocally = emptySet()
        )
        assertEquals(listOf(cloud), plan.restores)
        assertTrue(plan.pushes.isEmpty())
    }

    @Test
    fun testWaitsForCanvasAssignmentsBeforeRestoringLinkedItems() {
        val cloud = snapshot(100L, canvasId = 555L)
        val waiting = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = emptyMap(),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to cloud),
            seenLocally = emptySet()
        )
        assertTrue(waiting.isEmpty)

        val ready = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = emptyMap(),
            coursesWithAssignments = setOf(100L),
            remote = mapOf(100L to cloud),
            seenLocally = emptySet()
        )
        assertEquals(listOf(cloud), ready.restores)
    }

    @Test
    fun testPushesEmptySnapshotWhenUserDeletesAllGroups() {
        val plan = CloudSyncPlanner.plan(
            localCourseIds = setOf(100L),
            localSnapshots = emptyMap(),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to snapshot(100L)),
            seenLocally = setOf(100L)
        )
        assertEquals(listOf(CloudCourseSnapshot(100L, emptyList())), plan.pushes)
        assertTrue(plan.restores.isEmpty())
    }

    @Test
    fun testKeepsBackupOfCoursesNotInRoom() {
        // Al cerrar sesión se borran los cursos de Room: el respaldo no debe vaciarse.
        val plan = CloudSyncPlanner.plan(
            localCourseIds = emptySet(),
            localSnapshots = emptyMap(),
            coursesWithAssignments = emptySet(),
            remote = mapOf(100L to snapshot(100L)),
            seenLocally = setOf(100L)
        )
        assertTrue(plan.isEmpty)
    }
}
