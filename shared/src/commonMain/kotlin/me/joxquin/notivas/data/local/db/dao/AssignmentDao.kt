package me.joxquin.notivas.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY due_at ASC")
    fun observeAll(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE course_id = :courseId ORDER BY due_at ASC")
    fun observeByCourse(courseId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY due_at ASC")
    suspend fun getAll(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE course_id = :courseId ORDER BY due_at ASC")
    suspend fun getByCourse(courseId: Long): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AssignmentEntity?

    @Upsert
    suspend fun upsert(assignments: List<AssignmentEntity>)

    @Upsert
    suspend fun upsert(assignment: AssignmentEntity)

    @Query("UPDATE assignments SET notified_24h = :notif24h, notified_3h = :notif3h, notified_30m = :notif30m WHERE id = :id")
    suspend fun updateNotificationFlags(id: Long, notif24h: Boolean, notif3h: Boolean, notif30m: Boolean)

    @Query("DELETE FROM assignments WHERE course_id = :courseId")
    suspend fun deleteByCourse(courseId: Long)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}
