package me.joxquin.notivas.domain.team

import kotlinx.serialization.Serializable

@Serializable
internal data class TeamChecklistItem(
    val id: Int,
    val task: String = "",
    val owner: String = "",
    val deadline: String = "",
    val done: Boolean = false
)

private val canvasOrigin = Regex("""^https?://[a-zA-Z0-9.-]+(?::\d+)?(?=/|$)""", RegexOption.IGNORE_CASE)

internal fun teamChecklistCanvasLink(site: String?, courseId: Long, assignmentId: Long): String? {
    if (courseId <= 0 || assignmentId <= 0) return null
    val origin = site?.let { canvasOrigin.find(it.trim())?.value } ?: return null
    return "$origin/courses/$courseId/assignments/$assignmentId"
}

internal fun formatTeamChecklist(
    heading: String,
    items: List<TeamChecklistItem>,
    ownerLabel: String,
    deadlineLabel: String,
    sourceUrl: String?
): String = buildString {
    appendLine(heading)
    items.filter { it.task.isNotBlank() }.forEach { item ->
        append(if (item.done) "[x] " else "[ ] ")
        append(item.task.trim())
        if (item.owner.isNotBlank()) append(" · $ownerLabel ${item.owner.trim()}")
        if (item.deadline.isNotBlank()) append(" · $deadlineLabel ${item.deadline.trim()}")
        appendLine()
    }
    if (sourceUrl != null) {
        appendLine()
        append("Canvas $sourceUrl")
    }
}.trimEnd()
