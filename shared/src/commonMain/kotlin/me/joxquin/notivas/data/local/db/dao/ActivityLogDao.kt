package me.joxquin.notivas.data.local.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE type = :type ORDER BY timestamp DESC")
    fun observeByType(type: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLogEntity>)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun count(): Int
}
