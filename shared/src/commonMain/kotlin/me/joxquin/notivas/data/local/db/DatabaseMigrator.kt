package me.joxquin.notivas.data.local.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.FileStorageProvider
import me.joxquin.notivas.data.local.PersistentStoreSnapshot
import me.joxquin.notivas.data.local.PlatformFileSystem
import me.joxquin.notivas.data.local.db.entities.AssignmentEntity
import me.joxquin.notivas.data.local.db.entities.CopilotMessageEntity
import me.joxquin.notivas.data.local.db.entities.CopilotSessionEntity
import me.joxquin.notivas.data.local.db.entities.CourseEntity
import me.joxquin.notivas.data.local.db.entities.PlannerItemEntity
import me.joxquin.notivas.data.local.db.entities.SimulationGroupEntity
import me.joxquin.notivas.data.local.db.entities.SimulationItemEntity

class DatabaseMigrator(
    private val database: NotivasDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val jsonFilePath = "${FileStorageProvider.getAppDataDirectory()}/notivas_store.json"
            val jsonContent = PlatformFileSystem.readString(jsonFilePath) ?: return@withContext

            val snapshot = try {
                json.decodeFromString<PersistentStoreSnapshot>(jsonContent)
            } catch (e: Exception) {
                return@withContext
            }

            // 1. Migrate courses
            if (snapshot.courses.isNotEmpty()) {
                database.courseDao().upsert(snapshot.courses.map { CourseEntity.fromDomain(it) })
            }

            // 2. Migrate assignments
            if (snapshot.assignments.isNotEmpty()) {
                database.assignmentDao().upsert(snapshot.assignments.map { AssignmentEntity.fromDomain(it) })
            }

            // 3. Migrate copilot sessions & messages
            if (snapshot.copilotSessions.isNotEmpty()) {
                database.copilotChatDao().upsertSessions(snapshot.copilotSessions.map { CopilotSessionEntity.fromDomain(it) })
            }
            if (snapshot.copilotMessages.isNotEmpty()) {
                database.copilotChatDao().insertMessages(snapshot.copilotMessages.map { CopilotMessageEntity.fromDomain(it) })
            }

            // 4. Migrate simulation groups & items
            if (snapshot.simulationGroups.isNotEmpty()) {
                database.simulationDao().upsertGroups(snapshot.simulationGroups.map { SimulationGroupEntity.fromDomain(it) })
            }
            if (snapshot.simulationItems.isNotEmpty()) {
                database.simulationDao().upsertItems(snapshot.simulationItems.map { SimulationItemEntity.fromDomain(it) })
            }

            // 5. Migrate planner items
            if (snapshot.plannerItems.isNotEmpty()) {
                database.plannerDao().upsert(snapshot.plannerItems.map { PlannerItemEntity.fromDomain(it) })
            }

            // Rename migrated JSON file as backup
            val backupFilePath = "$jsonFilePath.migrated"
            PlatformFileSystem.writeString(backupFilePath, jsonContent)
            PlatformFileSystem.deleteFile(jsonFilePath)
        } catch (e: Exception) {
            // Migration failed or not needed, fail-safe
        }
    }
}
