package me.joxquin.notivas.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import me.joxquin.notivas.data.local.db.converters.RoomConverters
import me.joxquin.notivas.data.local.db.dao.AssignmentDao
import me.joxquin.notivas.data.local.db.dao.CopilotChatDao
import me.joxquin.notivas.data.local.db.dao.CourseDao
import me.joxquin.notivas.data.local.db.dao.PlannerDao
import me.joxquin.notivas.data.local.db.dao.SimulationDao
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CopilotMessageEntity
import me.joxquin.notivas.data.local.db.entities.CopilotSessionEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.local.db.entities.PlannerItemEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupEntity
import me.joxquin.notivas.data.local.db.entities.SimulationItemEntity

@Database(
    entities = [
        CourseEntity::class,
        AssignmentEntity::class,
        CopilotSessionEntity::class,
        CopilotMessageEntity::class,
        SimulationGroupEntity::class,
        SimulationItemEntity::class,
        PlannerItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(NotivasDatabaseConstructor::class)
abstract class NotivasDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun copilotChatDao(): CopilotChatDao
    abstract fun simulationDao(): SimulationDao
    abstract fun plannerDao(): PlannerDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NotivasDatabaseConstructor : RoomDatabaseConstructor<NotivasDatabase>
