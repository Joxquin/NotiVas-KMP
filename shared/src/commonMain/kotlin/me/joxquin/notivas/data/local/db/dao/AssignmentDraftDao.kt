package me.joxquin.notivas.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity

@Dao
interface AssignmentDraftDao {
    @Query("SELECT * FROM assignment_drafts WHERE assignment_id = :assignmentId LIMIT 1")
    fun observeByAssignmentId(assignmentId: Long): Flow<AssignmentDraftEntity?>

    @Query("SELECT * FROM assignment_drafts WHERE assignment_id = :assignmentId LIMIT 1")
    suspend fun getByAssignmentId(assignmentId: Long): AssignmentDraftEntity?

    @Query("SELECT * FROM assignment_drafts")
    fun observeAll(): Flow<List<AssignmentDraftEntity>>

    @Upsert
    suspend fun upsert(draft: AssignmentDraftEntity)

    @Query("DELETE FROM assignment_drafts WHERE assignment_id = :assignmentId")
    suspend fun deleteByAssignmentId(assignmentId: Long)

    @Query("DELETE FROM assignment_drafts")
    suspend fun deleteAll()
}
