package me.joxquin.notivas.data.cloud

import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CloudCourseSnapshotTest {

    private fun localGroups(groupId: Long, firstItemId: Long): List<SimulationGroupWithItems> = listOf(
        SimulationGroupWithItems(
            group = SimulationGroup(id = groupId, courseId = 100L, name = "Examenes", weightPercentage = 60f, orderIndex = 1),
            items = listOf(
                SimulationItem(id = firstItemId, groupId = groupId, name = "Parcial", canvasAssignmentId = 555L, manualScore = 14.5f, orderIndex = 0),
                SimulationItem(id = firstItemId + 1, groupId = groupId, name = "Final", simulatedScore = 13.1f, isSimulated = true, orderIndex = 1)
            )
        ),
        SimulationGroupWithItems(
            group = SimulationGroup(id = groupId + 1, courseId = 100L, name = "Laboratorios", weightPercentage = 40f, dropLowest = true, orderIndex = 0),
            items = listOf(SimulationItem(id = firstItemId + 2, groupId = groupId + 1, name = "Lab 1", simulatedScore = 0.1f, isSimulated = true))
        )
    )

    @Test
    fun testSnapshotIgnoresLocalIds() {
        // Mismo contenido en dos dispositivos con ids de Room distintos.
        assertEquals(
            CloudCourseSnapshot.fromLocal(100L, localGroups(groupId = 1L, firstItemId = 1L)),
            CloudCourseSnapshot.fromLocal(100L, localGroups(groupId = 40L, firstItemId = 90L))
        )
    }

    @Test
    fun testMapRoundTripKeepsValues() {
        val snapshot = CloudCourseSnapshot.fromLocal(100L, localGroups(1L, 1L))
        val data = snapshot.toMap("Programación en Móviles")

        assertEquals("Programación en Móviles", data[CloudCourseSnapshot.FIELD_COURSE_NAME])
        assertEquals(snapshot, CloudCourseSnapshot.fromMap(data))
    }

    @Test
    fun testFloatsAreStoredAsReadableDecimals() {
        val snapshot = CloudCourseSnapshot.fromLocal(100L, localGroups(1L, 1L))
        val labs = (snapshot.toMap(null)[CloudCourseSnapshot.FIELD_GROUPS] as List<*>).first() as Map<*, *>
        val firstItem = (labs["items"] as List<*>).first() as Map<*, *>

        assertEquals("Laboratorios", labs["name"])
        assertEquals(0.1, firstItem["simulatedScore"])
    }

    @Test
    fun testAcceptsFirestoreNumberTypes() {
        // Un peso escrito a mano en la consola llega como Long, no como Double.
        val data = mapOf(
            "courseId" to 100L,
            "groups" to listOf(
                mapOf("name" to "Proyecto", "weightPercentage" to 30L, "items" to emptyList<Any>())
            )
        )
        val snapshot = CloudCourseSnapshot.fromMap(data)
        assertEquals(30f, snapshot?.groups?.single()?.weightPercentage)
    }

    @Test
    fun testMalformedDocumentReturnsNull() {
        assertNull(CloudCourseSnapshot.fromMap(mapOf("groups" to emptyList<Any>())))
        assertNull(CloudCourseSnapshot.fromMap(mapOf("courseId" to 1L, "groups" to listOf(mapOf("name" to "Sin peso")))))
    }

    @Test
    fun testRestoreDropsCanvasLinksMissingOnDevice() {
        val snapshot = CloudCourseSnapshot.fromLocal(100L, localGroups(1L, 1L))

        val withoutAssignment = snapshot.toLocalGroups(existingAssignmentIds = emptySet())
        val withAssignment = snapshot.toLocalGroups(existingAssignmentIds = setOf(555L))

        val parcialSinTarea = withoutAssignment.flatMap { it.items }.single { it.name == "Parcial" }
        val parcialConTarea = withAssignment.flatMap { it.items }.single { it.name == "Parcial" }
        assertNull(parcialSinTarea.canvasAssignmentId)
        assertEquals(555L, parcialConTarea.canvasAssignmentId)
        assertEquals(14.5f, parcialConTarea.manualScore)
        assertEquals(100L, withAssignment.first().group.courseId)
    }
}
