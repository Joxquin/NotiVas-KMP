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

import me.joxquin.notivas.data.local.db.dao.ActivityLogDao
import me.joxquin.notivas.data.local.db.dao.AssignmentDraftDao
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity
import me.joxquin.notivas.data.local.db.entities.AssignmentDraftEntity

@Database(
    entities = [
        CourseEntity::class,
        AssignmentEntity::class,
        AssignmentDraftEntity::class,
        ActivityLogEntity::class,
        CopilotSessionEntity::class,
        CopilotMessageEntity::class,
        SimulationGroupEntity::class,
        SimulationItemEntity::class,
        PlannerItemEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(NotivasDatabaseConstructor::class)
abstract class NotivasDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun assignmentDraftDao(): AssignmentDraftDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun copilotChatDao(): CopilotChatDao
    abstract fun simulationDao(): SimulationDao
    abstract fun plannerDao(): PlannerDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NotivasDatabaseConstructor : RoomDatabaseConstructor<NotivasDatabase> {
    override fun initialize(): NotivasDatabase
}
