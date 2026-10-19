package me.joxquin.notivas.domain.usecase

import kotlinx.coroutines.flow.first
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.GradePerformanceLevel
import me.joxquin.notivas.data.model.MorningBriefingData
import me.joxquin.notivas.data.model.OpenRouterChatRequest
import me.joxquin.notivas.data.model.OpenRouterMessage
import me.joxquin.notivas.data.model.TimelineItem
import me.joxquin.notivas.data.model.TimelineItemType
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import me.joxquin.notivas.util.DateTimeUtil
import kotlin.math.roundToInt

class MorningBriefingUseCase(
    private val openRouterApiService: OpenRouterApiService,
    private val preferencesManager: PreferencesManager
) {
    // Caché en memoria para evitar invocar la API en cada recomposición durante el mismo día
    private var cachedDateIso: String? = null
    private var cachedAiSummary: String? = null

    suspend fun buildMorningBriefing(
        userProfile: UserProfile?,
        courses: List<Course>,
        allAssignments: List<AssignmentUiModel>,
        courseStats: List<CourseStat>,
        forceAiRefresh: Boolean = false
    ): MorningBriefingData {
        val nowMillis = DateTimeUtil.nowEpochMillis()
        val today = DateTimeUtil.nowLocalDate()
        val todayIso = today.toIsoDateString()
        val studentName = userProfile?.name?.substringBefore(" ") ?: "Estudiante"

        val courseMap = courses.associateBy { it.id }

        // Filtrar tareas que vencen hoy o próximas
        var criticalCount = 0
        var upcomingCount = 0
        var totalPoints = 0.0
        val timeline = mutableListOf<TimelineItem>()
        val atRiskCourses = mutableSetOf<String>()

        val atRiskCourseIds = courseStats
            .filter { it.performanceLevel == GradePerformanceLevel.AT_RISK }
            .map { it.course.id }
            .toSet()

        // Identificar tareas pendientes
        val pendingAssignments = allAssignments.filter { ui ->
            val a = ui.assignment
            !a.isCompleted && a.status != "completed"
        }

        for (ui in pendingAssignments) {
            val a = ui.assignment
            val dueStr = a.dueAt ?: a.lockAt ?: continue
            val comp = DateTimeUtil.parseIsoToComponents(dueStr) ?: continue

            val diffMillis = comp.epochMillis - nowMillis
            val diffHours = diffMillis / (1000.0 * 60 * 60)

            if (diffHours in 0.0..12.0) {
                criticalCount++
            } else if (diffHours in 12.0..48.0) {
                upcomingCount++
            }

            // Si vence hoy
            if (comp.date == today) {
                val pts = a.pointsPossible ?: 0.0
                totalPoints += pts

                val course = courseMap[a.courseId]
                if (a.courseId in atRiskCourseIds && course != null) {
                    atRiskCourses.add(course.name)
                }

                val hourStr = comp.hour.toString().padStart(2, '0')
                val minStr = comp.minute.toString().padStart(2, '0')
                timeline.add(
                    TimelineItem(
                        id = "assignment_${a.id}",
                        title = a.name,
                        courseName = ui.courseName,
                        courseColor = null,
                        timeLabel = "$hourStr:$minStr",
                        type = TimelineItemType.ASSIGNMENT,
                        isSubmitted = a.isCompleted,
                        isGroup = false,
                        pointsPossible = a.pointsPossible,
                        pointsWeight = null,
                        assignmentId = a.id,
                        courseId = a.courseId,
                        rubricHtml = null
                    )
                )
            }
        }

        timeline.sortBy { it.timeLabel }

        // Identificar Hero Task (la más urgente con fecha límite más próxima)
        val heroTask = pendingAssignments
            .mapNotNull { ui ->
                val due = ui.assignment.dueAt ?: ui.assignment.lockAt ?: return@mapNotNull null
                val comp = DateTimeUtil.parseIsoToComponents(due) ?: return@mapNotNull null
                val diff = comp.epochMillis - nowMillis
                if (diff > 0) Pair(ui, comp) else null
            }
            .minByOrNull { it.second.epochMillis }

        val heroUi = heroTask?.first
        val heroComp = heroTask?.second
        val heroCourse = heroUi?.assignment?.courseId?.let { courseMap[it] }

        val countdownLabel = if (heroComp != null) {
            val diffMins = ((heroComp.epochMillis - nowMillis) / (1000 * 60)).coerceAtLeast(0)
            val h = diffMins / 60
            val m = diffMins % 60
            if (heroComp.date == today) {
                if (h > 0) "Vence en ${h}h ${m}m" else "Vence en ${m}m"
            } else {
                val hourStr = heroComp.hour.toString().padStart(2, '0')
                val minStr = heroComp.minute.toString().padStart(2, '0')
                "Hoy $hourStr:$minStr"
            }
        } else null

        val pointsLabel = heroUi?.assignment?.pointsPossible?.let { pts ->
            if (pts > 0) "${pts.roundToInt()} pts" else null
        }

        val greeting = if (criticalCount + upcomingCount > 0) {
            "¡Buen día, $studentName! Tienes ${criticalCount + upcomingCount} entregas próximas."
        } else {
            "¡Buen día, $studentName! No tienes entregas urgentes para hoy."
        }

        val heuristicSummary = buildHeuristicSummary(
            studentName = studentName,
            criticalCount = criticalCount,
            upcomingCount = upcomingCount,
            heroTask = heroUi,
            totalPoints = totalPoints,
            atRiskCourses = atRiskCourses.toList()
        )

        // Intento de enriquecimiento con OpenRouter AI
        var finalSummary = heuristicSummary
        var isAi = false

        val apiKey = preferencesManager.openRouterApiKey.first()
        if (!apiKey.isNullOrBlank()) {
            if (cachedDateIso == todayIso && cachedAiSummary != null && !forceAiRefresh) {
                finalSummary = cachedAiSummary!!
                isAi = true
            } else {
                val aiSummary = generateAiSummary(
                    apiKey = apiKey,
                    studentName = studentName,
                    criticalCount = criticalCount,
                    upcomingCount = upcomingCount,
                    heroTaskTitle = heroUi?.assignment?.name,
                    heroCourse = heroUi?.courseName,
                    totalPoints = totalPoints,
                    atRiskCourses = atRiskCourses.toList()
                )
                if (aiSummary != null) {
                    cachedDateIso = todayIso
                    cachedAiSummary = aiSummary
                    finalSummary = aiSummary
                    isAi = true
                }
            }
        }

        return MorningBriefingData(
            greeting = greeting,
            studentName = studentName,
            criticalTasksCount = criticalCount,
            upcomingTasksCount = upcomingCount,
            closingForumsCount = 0,
            todayClassesCount = 0,
            heroTask = heroUi,
            heroTaskCountdown = countdownLabel,
            heroTaskCourseColor = null,
            heroTaskPointsLabel = pointsLabel,
            lastUpdatedAt = "8:00 AM",
            executiveSummary = finalSummary,
            isAiGenerated = isAi,
            totalPointsAtStake = totalPoints,
            atRiskCourseNames = atRiskCourses.toList(),
            timelineItems = timeline
        )
    }

    private fun buildHeuristicSummary(
        studentName: String,
        criticalCount: Int,
        upcomingCount: Int,
        heroTask: AssignmentUiModel?,
        totalPoints: Double,
        atRiskCourses: List<String>
    ): String {
        return when {
            heroTask != null && criticalCount > 0 -> {
                val warning = if (atRiskCourses.isNotEmpty()) " Atención: ${atRiskCourses.first()} tiene promedio en riesgo." else ""
                "Hoy es un día de alta prioridad. Tu entrega número 1 es '${heroTask.assignment.name}' de ${heroTask.courseName}. Tienes $criticalCount tarea(s) en zona crítica (<12h).$warning"
            }
            upcomingCount > 0 -> {
                "Tienes una jornada con entregas en las próximas 48 horas. Aprovecha las primeras horas para avanzar en tus entregables y mantener tus cursos al día."
            }
            else -> {
                "Jornada despejada sin entregas inmediatas para hoy. Es un momento propicio para repasar materiales de estudio o avanzar en tus proyectos semanales."
            }
        }
    }

    private suspend fun generateAiSummary(
        apiKey: String,
        studentName: String,
        criticalCount: Int,
        upcomingCount: Int,
        heroTaskTitle: String?,
        heroCourse: String?,
        totalPoints: Double,
        atRiskCourses: List<String>
    ): String? {
        return try {
            val systemPrompt = """
                Eres el Copiloto Académico de NotiVas. Genera un único párrafo ejecutivo (máximo 45 palabras) con tono motivador, conciso y estratégico para el estudiante.
                Resalta cuál es su prioridad #1 del día, advierte con tacto si hay cursos en riesgo y ofrece un consejo de enfoque inmediato.
                No uses emojis ni listas. Solo texto puro de calidad profesional.
            """.trimIndent()

            val userContent = """
                Estudiante: $studentName
                Tareas críticas (<12h): $criticalCount
                Tareas próximas: $upcomingCount
                Prioridad principal: ${heroTaskTitle ?: "Ninguna"} (${heroCourse ?: "N/A"})
                Puntos en juego: ${totalPoints.roundToInt()}
                Cursos con promedio en riesgo: ${atRiskCourses.joinToString().ifEmpty { "Ninguno" }}
            """.trimIndent()

            val selectedModel = preferencesManager.openRouterModel.first() ?: "google/gemini-2.0-flash-001"
            val response = openRouterApiService.chatCompletion(
                apiKey = apiKey,
                request = OpenRouterChatRequest(
                    model = selectedModel,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(role = "user", content = userContent)
                    ),
                    temperature = 0.5,
                    maxTokens = 120
                )
            )

            response.choices?.firstOrNull()?.message?.content?.trim()
        } catch (_: Exception) {
            null
        }
    }
}
