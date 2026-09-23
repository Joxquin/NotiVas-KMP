package me.joxquin.notivas.data.local.db

import me.joxquin.notivas.data.local.db.converters.RoomConverters
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RoomConvertersTest {
    private val converters = RoomConverters()

    @Test
    fun testStringListSerialization() {
        val originalList = listOf("online_upload", "online_text_entry", "media_recording")
        val jsonString = converters.fromStringList(originalList)
        assertNotNull(jsonString)
        val deserializedList = converters.toStringList(jsonString)
        assertEquals(originalList, deserializedList)
    }

    @Test
    fun testCourseEntityDomainMapping() {
        val course = Course(
            id = 12345L,
            name = "Desarrollo Web Integrado",
            courseCode = "DWI-2026",
            enrollmentTermId = 99L
        )
        val entity = CourseEntity.fromDomain(course)
        assertEquals(12345L, entity.id)
        assertEquals("Desarrollo Web Integrado", entity.name)
        assertEquals("DWI-2026", entity.courseCode)

        val mappedBack = entity.toDomain()
        assertEquals(course, mappedBack)
    }

    @Test
    fun testAssignmentEntityDomainMapping() {
        val assignment = Assignment(
            id = 9876L,
            courseId = 12345L,
            name = "Avance de Proyecto 1",
            description = "Subir informe en PDF",
            dueAt = "2026-09-30T23:59:00Z",
            pointsPossible = 20.0,
            status = "upcoming",
            submissionTypes = listOf("online_upload")
        )
        val entity = AssignmentEntity.fromDomain(assignment)
        assertEquals(9876L, entity.id)
        assertEquals(12345L, entity.courseId)
        assertEquals("Avance de Proyecto 1", entity.name)

        val mappedBack = entity.toDomain()
        assertEquals(assignment.id, mappedBack.id)
        assertEquals(assignment.name, mappedBack.name)
        assertEquals(assignment.dueAt, mappedBack.dueAt)
        assertEquals(assignment.submissionTypes, mappedBack.submissionTypes)
    }
}
