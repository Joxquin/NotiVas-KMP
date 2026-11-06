package me.joxquin.notivas.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import me.joxquin.notivas.data.local.db.NotivasDatabase
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity
import me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryLocalStoreTest {
    @Test
    fun deletingOneAssignmentPreservesOtherAssignmentsAndTheirDrafts() = withStore { database, store ->
        database.assignmentDao().upsert(
            listOf(
                AssignmentEntity(id = 1, courseId = 10, name = "Selected assignment"),
                AssignmentEntity(id = 2, courseId = 10, name = "Same course assignment"),
                AssignmentEntity(id = 3, courseId = 20, name = "Other course assignment")
            )
        )
        database.assignmentDraftDao().upsert(draft(1))
        database.assignmentDraftDao().upsert(draft(2))

        store.deleteAssignmentById(1)

        assertEquals(setOf(2L, 3L), database.assignmentDao().getAll().map { it.id }.toSet())
        assertNull(database.assignmentDraftDao().getByAssignmentId(1))
        assertNotNull(database.assignmentDraftDao().getByAssignmentId(2))
    }

    @Test
    fun deletingAnUnknownAssignmentLeavesExistingAssignmentsUntouched() = withStore { database, store ->
        val assignment = AssignmentEntity(id = 1, courseId = 10, name = "Existing assignment")
        database.assignmentDao().upsert(assignment)

        store.deleteAssignmentById(999)

        assertEquals(listOf(assignment), database.assignmentDao().getAll())
    }

    @Test
    fun clearingStorageRemovesActivityAndAcademicDataBeforeReturning() = withStore { database, store ->
        database.courseDao().upsert(CourseEntity(id = 10, name = "Previous account course"))
        database.assignmentDao().upsert(AssignmentEntity(id = 1, courseId = 10, name = "Assignment"))
        database.assignmentDraftDao().upsert(draft(1))
        database.activityLogDao().insert(
            ActivityLogEntity(
                type = "ASSIGNMENT_SUBMISSION",
                title = "Previous account activity",
                description = "Private activity from the previous session"
            )
        )

        store.clearDiskStorage()

        assertEquals(0, database.activityLogDao().count())
        assertTrue(database.courseDao().getAll().isEmpty())
        assertTrue(database.assignmentDao().getAll().isEmpty())
        assertNull(database.assignmentDraftDao().getByAssignmentId(1))
    }

    @Test
    fun replacingCourseGroupsSwapsOnlyThatCourseAndKeepsItems() = withStore { database, store ->
        val course = Course(id = 10, name = "Target course", totalWeeks = 16, passingGrade = 13f)
        database.courseDao().upsert(CourseEntity(id = 20, name = "Other course"))
        store.replaceMultipleCourseGroups(listOf(course to listOf(groupWithItems(10, "Old group", 1))))
        store.replaceMultipleCourseGroups(listOf(Course(id = 20, name = "Other course") to listOf(groupWithItems(20, "Other group", 1))))

        store.replaceMultipleCourseGroups(
            listOf(course to listOf(groupWithItems(10, "Lab", 2), groupWithItems(10, "Exam", 1)))
        )

        val target = database.simulationDao().getGroupsWithItems(10)
        assertEquals(setOf("Lab", "Exam"), target.map { it.group.name }.toSet())
        assertEquals(3, target.sumOf { it.items.size })
        assertEquals(listOf("Other group"), database.simulationDao().getGroupsWithItems(20).map { it.group.name })
        val saved = assertNotNull(database.courseDao().getById(10))
        assertEquals(16, saved.totalWeeks)
        assertEquals(13f, saved.passingGrade)
    }

    private fun groupWithItems(courseId: Long, name: String, itemCount: Int) = SimulationGroupWithItems(
        group = SimulationGroup(courseId = courseId, name = name, weightPercentage = 50f),
        items = (1..itemCount).map { SimulationItem(groupId = 0, name = "$name $it") }
    )

    private fun draft(assignmentId: Long) = AssignmentDraftEntity(
        assignmentId = assignmentId,
        fileName = "draft-$assignmentId.txt",
        driveUrl = "https://drive.google.com/file/d/test-$assignmentId/view",
        accountEmail = "student@example.com"
    )

    private fun withStore(block: suspend (NotivasDatabase, InMemoryLocalStore) -> Unit) = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<NotivasDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        // Disable profile file I/O and background observers; exercise awaited database operations only.
        val inactiveScope = CoroutineScope(Job().apply { cancel() })
        try {
            block(database, InMemoryLocalStore(database, inactiveScope))
        } finally {
            database.close()
        }
    }
}
