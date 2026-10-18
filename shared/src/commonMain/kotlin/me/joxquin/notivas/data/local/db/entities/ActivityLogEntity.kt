package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    indices = [
        Index("timestamp"),
        Index("type")
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: String, // ASSIGNMENT_SUBMISSION, FORUM_POST, DRIVE_SYNC, CANVAS_NOTIFICATION, GRADE_POSTED
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "course_code")
    val courseCode: String? = null,
    @ColumnInfo(name = "course_name")
    val courseName: String? = null,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L,
    @ColumnInfo(name = "target_id")
    val targetId: Long? = null,
    @ColumnInfo(name = "secondary_id")
    val secondaryId: Long? = null,
    @ColumnInfo(name = "extra_info")
    val extraInfo: String? = null,
    @ColumnInfo(name = "action_url")
    val actionUrl: String? = null
)
