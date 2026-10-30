package me.joxquin.notivas.data.local.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.CourseEntity

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY name ASC")
    suspend fun getAll(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CourseEntity?

    @Upsert
    suspend fun upsert(courses: List<CourseEntity>)

    @Upsert
    suspend fun upsert(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM courses WHERE id NOT IN (:activeIds)")
    suspend fun deleteNotIn(activeIds: List<Long>)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
