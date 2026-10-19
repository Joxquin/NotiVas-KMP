package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel

enum class TimelineItemType {
    ASSIGNMENT,
    FORUM,
    CLASS_EVENT
}

@Serializable
data class TimelineItem(
    val id: String,
    val title: String,
    val courseName: String,
    val courseColor: String? = null,
    val timeLabel: String,
    val type: TimelineItemType,
    val isSubmitted: Boolean = false,
    val isGroup: Boolean = false,
    val pointsPossible: Double? = null,
    val pointsWeight: Double? = null,
    val assignmentId: Long? = null,
    val courseId: Long? = null,
    val discussionTopicId: Long? = null,
    val rubricHtml: String? = null
)

data class MorningBriefingData(
    val greeting: String,
    val studentName: String,
    val criticalTasksCount: Int,
    val upcomingTasksCount: Int,
    val closingForumsCount: Int,
    val todayClassesCount: Int,
    val heroTask: AssignmentUiModel?,
    val heroTaskCountdown: String?,
    val heroTaskCourseColor: String?,
    val heroTaskPointsLabel: String?,
    val lastUpdatedAt: String,
    val executiveSummary: String,
    val isAiGenerated: Boolean = false,
    val totalPointsAtStake: Double = 0.0,
    val atRiskCourseNames: List<String> = emptyList(),
    val timelineItems: List<TimelineItem> = emptyList()
)
