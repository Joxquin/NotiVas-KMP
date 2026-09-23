package me.joxquin.notivas.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.PlannerItemEntity

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_items ORDER BY plannable_date ASC")
    fun observeAll(): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items ORDER BY plannable_date ASC")
    suspend fun getAll(): List<PlannerItemEntity>

    @Upsert
    suspend fun upsert(items: List<PlannerItemEntity>)

    @Query("DELETE FROM planner_items")
    suspend fun deleteAll()
}
