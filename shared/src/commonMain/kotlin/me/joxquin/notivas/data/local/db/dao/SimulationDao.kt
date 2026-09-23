package me.joxquin.notivas.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.SimulationGroupEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupWithItemsRelation
import me.joxquin.notivas.data.local.db.entities.SimulationItemEntity

@Dao
interface SimulationDao {
    @Transaction
    @Query("SELECT * FROM simulation_groups WHERE course_id = :courseId ORDER BY id ASC")
    fun observeGroupsWithItems(courseId: Long): Flow<List<SimulationGroupWithItemsRelation>>

    @Transaction
    @Query("SELECT * FROM simulation_groups WHERE course_id = :courseId ORDER BY id ASC")
    suspend fun getGroupsWithItems(courseId: Long): List<SimulationGroupWithItemsRelation>

    @Transaction
    @Query("SELECT * FROM simulation_groups ORDER BY id ASC")
    suspend fun getAllGroupsWithItems(): List<SimulationGroupWithItemsRelation>

    @Query("SELECT * FROM simulation_groups ORDER BY id ASC")
    suspend fun getAllGroups(): List<SimulationGroupEntity>

    @Query("SELECT * FROM simulation_items ORDER BY id ASC")
    suspend fun getAllItems(): List<SimulationItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SimulationGroupEntity): Long

    @Update
    suspend fun updateGroup(group: SimulationGroupEntity)

    @Upsert
    suspend fun upsertGroups(groups: List<SimulationGroupEntity>)

    @Query("DELETE FROM simulation_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SimulationItemEntity): Long

    @Update
    suspend fun updateItem(item: SimulationItemEntity)

    @Upsert
    suspend fun upsertItems(items: List<SimulationItemEntity>)

    @Query("DELETE FROM simulation_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM simulation_groups WHERE course_id = :courseId")
    suspend fun deleteGroupsForCourse(courseId: Long)

    @Query("DELETE FROM simulation_groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM simulation_items")
    suspend fun deleteAllItems()
}
