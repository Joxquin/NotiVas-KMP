package me.joxquin.notivas.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import me.joxquin.notivas.data.local.db.dao.SimulationDao
import me.joxquin.notivas.data.local.db.entities.SimulationGroupEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupWithItemsRelation
import me.joxquin.notivas.data.local.db.entities.SimulationItemEntity
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class SimulationRepositoryTest {
    @Test
    fun observeAllGroupsWithItemsEmitsDomainGroupsWithItemsInOrder() {
        val group = SimulationGroup(id = 1L, courseId = 10L, name = "Labs", weightPercentage = 40f)
        val first = SimulationItem(id = 2L, groupId = group.id, name = "Lab 1", orderIndex = 0)
        val second = SimulationItem(id = 3L, groupId = group.id, name = "Lab 2", orderIndex = 1)
        val relation = SimulationGroupWithItemsRelation(
            group = SimulationGroupEntity.fromDomain(group),
            items = listOf(SimulationItemEntity.fromDomain(second), SimulationItemEntity.fromDomain(first))
        )
        val repository = SimulationRepository(FakeSimulationDao(flowOf(emptyList(), listOf(relation))))

        var emissions: List<List<SimulationGroupWithItems>>? = null
        suspend { repository.observeAllGroupsWithItems().toList() }
            .startCoroutine(Continuation(EmptyCoroutineContext) { emissions = it.getOrThrow() })

        val expected = SimulationGroupWithItems(group, listOf(first, second))
        assertEquals(listOf(emptyList(), listOf(expected)), emissions)
    }

    private class FakeSimulationDao(
        private val groups: Flow<List<SimulationGroupWithItemsRelation>>
    ) : SimulationDao {
        override fun observeAllGroupsWithItems() = groups

        override fun observeGroupsWithItems(courseId: Long): Flow<List<SimulationGroupWithItemsRelation>> = unexpectedCall()
        override suspend fun getGroupsWithItems(courseId: Long): List<SimulationGroupWithItemsRelation> = unexpectedCall()
        override suspend fun getAllGroupsWithItems(): List<SimulationGroupWithItemsRelation> = unexpectedCall()
        override suspend fun getAllGroups(): List<SimulationGroupEntity> = unexpectedCall()
        override suspend fun getAllItems(): List<SimulationItemEntity> = unexpectedCall()
        override suspend fun insertGroup(group: SimulationGroupEntity): Long = unexpectedCall()
        override suspend fun insertGroups(groups: List<SimulationGroupEntity>): List<Long> = unexpectedCall()
        override suspend fun updateGroup(group: SimulationGroupEntity): Unit = unexpectedCall()
        override suspend fun upsertGroups(groups: List<SimulationGroupEntity>): Unit = unexpectedCall()
        override suspend fun deleteGroup(groupId: Long): Unit = unexpectedCall()
        override suspend fun insertItem(item: SimulationItemEntity): Long = unexpectedCall()
        override suspend fun insertItems(items: List<SimulationItemEntity>): List<Long> = unexpectedCall()
        override suspend fun updateItem(item: SimulationItemEntity): Unit = unexpectedCall()
        override suspend fun upsertItems(items: List<SimulationItemEntity>): Unit = unexpectedCall()
        override suspend fun deleteItem(itemId: Long): Unit = unexpectedCall()
        override suspend fun deleteGroupsForCourse(courseId: Long): Unit = unexpectedCall()
        override suspend fun deleteAllGroups(): Unit = unexpectedCall()
        override suspend fun deleteAllItems(): Unit = unexpectedCall()

        private fun unexpectedCall(): Nothing = error("Unexpected DAO call")
    }
}
