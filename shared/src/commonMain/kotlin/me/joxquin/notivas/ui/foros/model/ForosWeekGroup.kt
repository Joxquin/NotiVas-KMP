package me.joxquin.notivas.ui.foros.model

import me.joxquin.notivas.data.model.CanvasDiscussionTopic

data class ForosWeekGroup(
    val id: String,
    val title: String,
    val weekNumber: Int?,
    val topics: List<CanvasDiscussionTopic>,
    val isCompleted: Boolean,
    val hasPending: Boolean
)

object ForosGroupHelper {
    // Regex para identificar número de semana o módulo en títulos de foros
    // Ej: "Semana 1: Foro de debate", "S02 - Caso de estudio", "Módulo 3: Preguntas", "Week 4"
    private val WEEK_REGEX = Regex("""\b(?:semana|week|sem|s|módulo|modulo|unidad)\s*[:\-_]?\s*0*(\d+)\b""", RegexOption.IGNORE_CASE)

    fun groupDiscussions(
        discussions: List<CanvasDiscussionTopic>,
        nowMs: Long,
        generalGroupTitle: String,
        weekFormat: (Int) -> String,
        reverseOrder: Boolean = true
    ): List<ForosWeekGroup> {
        if (discussions.isEmpty()) return emptyList()

        val grouped = mutableMapOf<Int, MutableList<CanvasDiscussionTopic>>()
        val general = mutableListOf<CanvasDiscussionTopic>()

        for (topic in discussions) {
            val weekNum = extractWeekNumber(topic.title)
            if (weekNum != null) {
                grouped.getOrPut(weekNum) { mutableListOf() }.add(topic)
            } else {
                general.add(topic)
            }
        }

        val result = mutableListOf<ForosWeekGroup>()

        // 1. Grupos de semanas ordenados (por defecto descendente / al revés)
        val sortedWeeks = if (reverseOrder) grouped.keys.sortedDescending() else grouped.keys.sorted()
        for (week in sortedWeeks) {
            val topicsInWeek = grouped[week] ?: emptyList()
            var anyPending = false
            var allDone = true
            for (topic in topicsInWeek) {
                val done = isTopicDoneOrExpired(topic, nowMs)
                if (!done) {
                    anyPending = true
                    allDone = false
                }
            }
            val allCompleted = topicsInWeek.isNotEmpty() && allDone
            val hasPending = anyPending

            result.add(
                ForosWeekGroup(
                    id = "week_$week",
                    title = weekFormat(week),
                    weekNumber = week,
                    topics = topicsInWeek,
                    isCompleted = allCompleted,
                    hasPending = hasPending
                )
            )
        }

        // 2. Grupo General (para los foros que no tienen etiqueta de semana en el título)
        if (general.isNotEmpty()) {
            var anyPending = false
            var allDone = true
            for (topic in general) {
                val done = isTopicDoneOrExpired(topic, nowMs)
                if (!done) {
                    anyPending = true
                    allDone = false
                }
            }
            val allCompleted = general.isNotEmpty() && allDone
            val hasPending = anyPending

            result.add(
                ForosWeekGroup(
                    id = "general",
                    title = generalGroupTitle,
                    weekNumber = null,
                    topics = general,
                    isCompleted = allCompleted,
                    hasPending = hasPending
                )
            )
        }

        return result
    }

    private fun extractWeekNumber(title: String): Int? {
        val match = WEEK_REGEX.find(title) ?: return null
        return match.groupValues.getOrNull(1)?.toIntOrNull()
    }

    private fun isTopicDoneOrExpired(topic: CanvasDiscussionTopic, nowMs: Long): Boolean {
        if (topic.locked == true) return true
        val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt ?: return false
        val components = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(dueStr) ?: return false
        return components.epochMillis <= nowMs
    }
}

