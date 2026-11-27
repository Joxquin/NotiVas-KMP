package me.joxquin.notivas.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.joxquin.notivas.data.local.db.dao.SimulationDao
import me.joxquin.notivas.data.model.SimulationGroupWithItems

class SimulationRepository(private val simulationDao: SimulationDao) {
    fun observeAllGroupsWithItems(): Flow<List<SimulationGroupWithItems>> =
        simulationDao.observeAllGroupsWithItems().map { relations ->
            relations.map { it.toDomain() }
        }
}
