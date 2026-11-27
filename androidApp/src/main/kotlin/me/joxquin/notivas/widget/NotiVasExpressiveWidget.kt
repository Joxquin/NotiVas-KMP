package me.joxquin.notivas.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.joxquin.notivas.MainActivity
import me.joxquin.notivas.R
import java.util.Locale

class NotiVasExpressiveWidget : GlanceAppWidget() {

    companion object {
        private val COMPACT_SIZE = DpSize(180.dp, 110.dp)
        private val MEDIUM_SIZE = DpSize(270.dp, 120.dp)
        private val LARGE_SIZE = DpSize(270.dp, 210.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT_SIZE, MEDIUM_SIZE, LARGE_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = NotiVasWidgetDataProvider.load(context)
        provideContent {
            GlanceTheme {
                NotiVasExpressiveWidgetContent(state = state)
            }
        }
    }
}

class NotiVasExpressiveWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotiVasExpressiveWidget()
}

object NotiVasExpressiveWidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun requestUpdate(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(appContext)
                val ids = manager.getGlanceIds(NotiVasExpressiveWidget::class.java)
                ids.forEach { glanceId ->
                    NotiVasExpressiveWidget().update(appContext, glanceId)
                }
            } catch (_: Throwable) {
                // Ignorar si no hay widgets activos
            }
        }
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        NotiVasExpressiveWidget().update(context, glanceId)
    }
}

@Composable
private fun NotiVasExpressiveWidgetContent(state: NotiVasWidgetState) {
    val size = LocalSize.current
    val isCompact = size.width < 240.dp && size.height < 180.dp
    val isTall = size.height >= 180.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(GlanceTheme.colors.surface)
            .padding(10.dp)
    ) {
        if (isCompact) {
            CompactExpressiveLayout(state = state)
        } else {
            ExpandedExpressiveLayout(
                state = state,
                maxTasks = if (isTall) 4 else 2
            )
        }
    }
}

@Composable
private fun CompactExpressiveLayout(state: NotiVasWidgetState) {
    val context = LocalContext.current
    val firstTask = state.pendingTasks.firstOrNull()

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            AverageHeroCard(
                overallAverage = state.overallAverage,
                gradedCourseCount = state.gradedCourseCount,
                compact = true,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight()
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            CopilotScallopButton(
                showLabel = false,
                modifier = GlanceModifier.size(64.dp)
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(18.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .clickable(
                    firstTask?.let { actionStartActivity(taskIntent(context, it)) }
                        ?: actionStartActivity(mainIntent(context))
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (firstTask != null) {
                val dayLabel = if (firstTask.dueDay == WidgetDueDay.TODAY) {
                    context.getString(R.string.widget_pending_today)
                } else {
                    context.getString(R.string.widget_pending_tomorrow)
                }
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "$dayLabel ${firstTask.dueTimeText}",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSecondaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = firstTask.title,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSecondaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            } else {
                Text(
                    text = context.getString(R.string.widget_pending_empty),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ExpandedExpressiveLayout(
    state: NotiVasWidgetState,
    maxTasks: Int
) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // Columna izquierda: Promedio general + Acceso directo Copilot
        Column(
            modifier = GlanceModifier.width(126.dp).fillMaxHeight(),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            AverageHeroCard(
                overallAverage = state.overallAverage,
                gradedCourseCount = state.gradedCourseCount,
                compact = false,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            CopilotPillButton(
                modifier = GlanceModifier.fillMaxWidth().height(46.dp)
            )
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        // Columna derecha: Pendientes de hoy y mañana
        PendingTasksCard(
            tasks = state.pendingTasks,
            maxTasks = maxTasks,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
        )
    }
}

@Composable
private fun AverageHeroCard(
    overallAverage: Double?,
    gradedCourseCount: Int,
    compact: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val formattedAverage = overallAverage?.let {
        String.format(Locale.getDefault(), "%.1f", it)
    } ?: context.getString(R.string.widget_average_empty)

    val statusText = when {
        overallAverage == null -> context.getString(R.string.widget_average_label)
        overallAverage >= 16.0 -> context.getString(R.string.widget_status_excellent)
        overallAverage >= 13.0 -> context.getString(R.string.widget_status_passing)
        else -> context.getString(R.string.widget_status_at_risk)
    }

    Column(
        modifier = modifier
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(actionStartActivity(mainIntent(context)))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = context.getString(R.string.widget_average_label),
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Row(verticalAlignment = Alignment.Vertical.Bottom) {
            Text(
                text = formattedAverage,
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = if (compact) 26.sp else 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            if (overallAverage != null) {
                Spacer(modifier = GlanceModifier.width(2.dp))
                Text(
                    text = "/20",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(bottom = 4.dp),
                    maxLines = 1
                )
            }
        }
        if (!compact) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            val subtitle = if (gradedCourseCount > 0) {
                "$statusText · ${context.getString(R.string.widget_average_courses_count, gradedCourseCount)}"
            } else {
                statusText
            }
            Text(
                text = subtitle,
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CopilotScallopButton(
    showLabel: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clickable(actionStartActivity(copilotIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_shape_scallop),
            contentDescription = context.getString(R.string.widget_copilot_label),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.tertiaryContainer),
            modifier = GlanceModifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_copilot),
                contentDescription = context.getString(R.string.widget_copilot_label),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                modifier = GlanceModifier.size(22.dp)
            )
            if (showLabel) {
                Text(
                    text = context.getString(R.string.widget_copilot_label),
                    style = TextStyle(
                        color = GlanceTheme.colors.onTertiaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CopilotPillButton(modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .cornerRadius(100.dp)
            .background(GlanceTheme.colors.tertiaryContainer)
            .clickable(actionStartActivity(copilotIntent(context)))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_copilot),
            contentDescription = context.getString(R.string.widget_copilot_label),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = context.getString(R.string.widget_copilot_label),
            style = TextStyle(
                color = GlanceTheme.colors.onTertiaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun PendingTasksCard(
    tasks: List<WidgetPendingTask>,
    maxTasks: Int,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val visibleTasks = tasks.take(maxTasks)

    Column(
        modifier = modifier
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.widget_pending_title),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            if (tasks.isNotEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(100.dp)
                        .background(GlanceTheme.colors.secondaryContainer)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tasks.size}",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSecondaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.width(4.dp))
            }
            Box(
                modifier = GlanceModifier
                    .size(22.dp)
                    .cornerRadius(100.dp)
                    .clickable(actionRunCallback<RefreshWidgetAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = context.getString(R.string.widget_refresh_cd),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        if (visibleTasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = context.getString(R.string.widget_pending_empty),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.Top
            ) {
                visibleTasks.forEachIndexed { index, task ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                    PendingTaskRow(task = task)
                }
            }
        }
    }
}

@Composable
private fun PendingTaskRow(task: WidgetPendingTask) {
    val context = LocalContext.current
    val isToday = task.dueDay == WidgetDueDay.TODAY
    val badgeBg = if (isToday) {
        GlanceTheme.colors.errorContainer
    } else {
        GlanceTheme.colors.secondaryContainer
    }
    val badgeFg = if (isToday) {
        GlanceTheme.colors.onErrorContainer
    } else {
        GlanceTheme.colors.onSecondaryContainer
    }
    val dayLabel = if (isToday) {
        context.getString(R.string.widget_pending_today)
    } else {
        context.getString(R.string.widget_pending_tomorrow)
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(14.dp)
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity(taskIntent(context, task)))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(100.dp)
                .background(badgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$dayLabel ${task.dueTimeText}",
                style = TextStyle(
                    color = badgeFg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
        Spacer(modifier = GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = task.title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            if (task.courseName.isNotBlank()) {
                Text(
                    text = task.courseName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun mainIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

private fun copilotIntent(context: Context): Intent =
    mainIntent(context).apply {
        putExtra("extra_nav_action", "copilot")
    }

private fun taskIntent(context: Context, task: WidgetPendingTask): Intent =
    mainIntent(context).apply {
        putExtra("extra_nav_action", "assignment")
        putExtra("extra_target_id", task.assignmentId)
        putExtra("extra_secondary_id", task.courseId)
        putExtra("extra_course_name", task.courseName)
    }
