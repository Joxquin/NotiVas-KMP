package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum
import me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo

@Entity(
    tableName = "assignment_drafts",
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("assignment_id", unique = true)
    ]
)
data class AssignmentDraftEntity(
    @PrimaryKey
    @ColumnInfo(name = "assignment_id")
    val assignmentId: Long,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "drive_url")
    val driveUrl: String,
    @ColumnInfo(name = "drive_file_id")
    val driveFileId: String? = null,
    @ColumnInfo(name = "account_email")
    val accountEmail: String,
    @ColumnInfo(name = "detected_template")
    val detectedTemplate: String? = null,
    @ColumnInfo(name = "course_folder")
    val courseFolder: String = "General",
    @ColumnInfo(name = "draft_status")
    val draftStatus: String = DraftStatusEnum.IN_PROGRESS.name,
    @ColumnInfo(name = "last_edited_time")
    val lastEditedTime: String = "Hace un momento",
    @ColumnInfo(name = "updated_at_timestamp")
    val updatedAtTimestamp: Long = 0L
) {
    fun toDomain(hasSubmitted: Boolean): DraftSyncInfo {
        val status = try {
            DraftStatusEnum.valueOf(draftStatus)
        } catch (_: Exception) {
            DraftStatusEnum.IN_PROGRESS
        }
        return DraftSyncInfo(
            fileName = fileName,
            driveUrl = driveUrl,
            driveFileId = driveFileId,
            lastEditedTime = lastEditedTime,
            accountEmail = accountEmail,
            detectedTemplate = detectedTemplate,
            courseFolder = courseFolder,
            draftStatus = if (hasSubmitted) DraftStatusEnum.SUBMITTED else status,
            hasSubmitted = hasSubmitted
        )
    }

    companion object {
        fun fromDomain(assignmentId: Long, info: DraftSyncInfo): AssignmentDraftEntity {
            return AssignmentDraftEntity(
                assignmentId = assignmentId,
                fileName = info.fileName,
                driveUrl = info.driveUrl,
                driveFileId = info.driveFileId,
                accountEmail = info.accountEmail,
                detectedTemplate = info.detectedTemplate,
                courseFolder = info.courseFolder,
                draftStatus = info.draftStatus.name,
                lastEditedTime = info.lastEditedTime,
                updatedAtTimestamp = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
            )
        }
    }
}
