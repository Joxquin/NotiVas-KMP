package me.joxquin.notivas.ui.herramientas.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

// ─── CARD 1: Large Hero Bento (Planificador & Tareas Canvas) ────────────────
@Composable
fun PlannerHeroBentoCard(
    uiState: me.joxquin.notivas.ui.notas.NotasUiState? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val currentAvg = uiState?.currentAverage ?: 0f
    val progressRatio = uiState?.evaluatedProgressRatio ?: 0f
    val projectedGrade = uiState?.projectedFinalGrade ?: 0f

    val defaultCourse = stringResource(Res.string.bento_active_course)
    val defaultNoAssignments = stringResource(Res.string.bento_no_graded_assignments)
    val courseName = uiState?.latestGradedCourseName ?: uiState?.selectedCourse?.name ?: defaultCourse
    val taskName = uiState?.latestGradedAssignmentName ?: defaultNoAssignments
    val taskScore = uiState?.latestGradedScoreFormatted ?: stringResource(Res.string.pending_badge)

    val (statusLabel, statusColor, statusBg) = when {
        currentAvg >= 15.0f -> Triple(
            stringResource(Res.string.status_good_performance),
            MiuixTheme.colorScheme.primary,
            MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        currentAvg >= 12.0f -> Triple(
            stringResource(Res.string.status_above_average),
            MiuixTheme.colorScheme.primary,
            MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        currentAvg > 0f -> Triple(
            stringResource(Res.string.status_at_risk),
            MiuixTheme.colorScheme.error,
            MiuixTheme.colorScheme.error.copy(alpha = 0.12f)
        )
        else -> Triple(
            stringResource(Res.string.status_unevaluated),
            MiuixTheme.colorScheme.onSurfaceVariantSummary,
            MiuixTheme.colorScheme.surfaceContainerHigh
        )
    }

    val evaluatedPct = (progressRatio * 100f).toInt()
    val formattedProj = ((projectedGrade * 10f).toInt() / 10f).toString()
    val formattedAvg = ((currentAvg * 10f).toInt() / 10f).toString()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = if (onClick != null) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Fila Superior: Icono Squircle, Título y Badge de Rendimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.14f),
                                cornerRadius = 12.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.35f),
                                cornerRadius = 12.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = stringResource(Res.string.bento_progress_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.bento_progress_subtitle),
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Badge de rendimiento dinámico
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .squircleSurface(
                            color = statusBg,
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = statusColor.copy(alpha = 0.35f),
                            cornerRadius = 9999.dp
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        maxLines = 1
                    )
                }
            }

            // Sub-tarjeta de Curso y Última Tarea Calificada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 14.dp
                    )
                    .squircleBorder(
                        width = 0.5.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 14.dp
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Parte de arriba: Nombre del curso
                    Text(
                        text = courseName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Abajo: Lo último que se calificó y la nota
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = taskName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Text(
                            text = taskScore,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentAvg >= 12f) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                        )
                    }

                    // Barra progresiva de avance
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.4f))
                    ) {
                        val progressColor = if (currentAvg >= 12f) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio.coerceIn(0.05f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9999.dp))
                                .background(progressColor)
                        )
                    }

                    // Fila de métricas: Evaluada %, Proyectable y Promedio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.bento_evaluated_prefix, evaluatedPct),
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = stringResource(Res.string.bento_projected_prefix, formattedProj),
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = stringResource(Res.string.bento_average_prefix, formattedAvg),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentAvg >= 12f) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ─── CARD 2: Simulador What-If ───────────────────────────────────────────────
@Composable
fun WhatIfBentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = if (onClick != null) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.14f),
                                cornerRadius = 10.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.3f),
                                cornerRadius = 10.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                cornerRadius = 9999.dp
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.bento_courses_count, 7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.bento_simulator_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.bento_simulator_subtitle),
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    lineHeight = 14.sp
                )
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.bento_projection_label),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(
                        text = "16.2 / 20",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.81f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MiuixTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.bento_weighted_avg_desc),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

// ─── CARD 3: Foros de Discusión ─────────────────────────────────────────────
@Composable
fun ForosBentoCard(
    forosUiState: me.joxquin.notivas.ui.foros.ForosUiState? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onOpenTopic: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null
) {
    val discussions = forosUiState?.discussions ?: emptyList()
    val nowMs = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
    val respondedIds = forosUiState?.respondedForumIds ?: emptySet()

    // 1. Foros activos que aún no han cerrado
    val activeForums = discussions.filter { topic ->
        if (topic.locked == true) return@filter false
        val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt ?: return@filter false
        val epochMs = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(dueStr)?.epochMillis ?: return@filter false
        epochMs > nowMs
    }

    // 2. Separar foros pendientes de responder de los ya respondidos
    val (pendingForums, answeredForums) = activeForums.partition { topic ->
        val isResponded = respondedIds.contains(topic.id) ||
                topic.assignment?.submission?.score != null ||
                topic.assignment?.submission?.workflowState in listOf("submitted", "graded")
        !isResponded
    }

    // 3. Priorizar el más próximo a cerrar que esté pendiente; si todos fueron respondidos, el más próximo respondido
    val isAllAnswered = activeForums.isNotEmpty() && pendingForums.isEmpty()
    val nextClosingForum = (if (pendingForums.isNotEmpty()) pendingForums else answeredForums)
        .minByOrNull { topic ->
            val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt ?: ""
            me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(dueStr)?.epochMillis ?: Long.MAX_VALUE
        } ?: discussions.firstOrNull()

    val pendingCount = forosUiState?.pendingCount ?: discussions.size
    val activeBadgesText = if (pendingCount > 0) stringResource(Res.string.bento_foros_active_count, pendingCount) else stringResource(Res.string.bento_foros_all_caught_up)

    var dueBadgeText = stringResource(Res.string.bento_foros_soon)
    var isUrgent = false

    if (nextClosingForum != null) {
        val dueStr = nextClosingForum.assignment?.dueAt ?: nextClosingForum.lockAt ?: nextClosingForum.assignment?.lockAt
        if (dueStr != null) {
            val dueComponents = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(dueStr)
            if (dueComponents != null) {
                val diffMs = dueComponents.epochMillis - nowMs
                if (diffMs <= 0 || nextClosingForum.locked == true) {
                    dueBadgeText = stringResource(Res.string.foro_due_closed)
                } else {
                    val hours = diffMs / (1000 * 60 * 60)
                    val days = hours / 24
                    if (hours in 0..24) {
                        isUrgent = true
                        val minutes = diffMs / (1000 * 60)
                        dueBadgeText = if (hours > 0) stringResource(Res.string.bento_foros_urgent_today_h, hours) else stringResource(Res.string.bento_foros_urgent_today_m, minutes)
                    } else if (days > 0) {
                        dueBadgeText = stringResource(Res.string.bento_foros_in_days, days)
                    } else {
                        dueBadgeText = stringResource(Res.string.bento_foros_soon)
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = if (onClick != null) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header: Icono, Título y Badge de Foros Activos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.14f),
                                cornerRadius = 11.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.35f),
                                cornerRadius = 11.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(Res.string.bento_foros_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = stringResource(Res.string.bento_foros_subtitle),
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.25f),
                            cornerRadius = 9999.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = activeBadgesText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }

            // Sub-tarjeta de Previsualización: Foro más próximo a cerrar
            if (nextClosingForum != null) {
                val onForumTopicClick = {
                    if (onOpenTopic != null) {
                        onOpenTopic(nextClosingForum)
                    } else if (onClick != null) {
                        onClick()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                            cornerRadius = 14.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.4f),
                            cornerRadius = 14.dp
                        )
                        .clickable(onClick = onForumTopicClick)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Título del tema y estado de urgencia/tiempo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isUrgent) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary)
                                )
                                Text(
                                    text = nextClosingForum.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val isThisTopicAnswered = respondedIds.contains(nextClosingForum.id) ||
                                    nextClosingForum.assignment?.submission?.score != null ||
                                    nextClosingForum.assignment?.submission?.workflowState in listOf("submitted", "graded")

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isThisTopicAnswered) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.foro_status_responded),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = dueBadgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isUrgent) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }

                        // Banner sutil Copilot IA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MiuixTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.60f))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val points = nextClosingForum.assignment?.pointsPossible
                                val rubricInfo = if (points != null && points > 0) stringResource(Res.string.bento_foros_rubric, points.toInt()) else stringResource(Res.string.bento_foros_active_part)
                                Text(
                                    text = "Copilot: $rubricInfo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = nextClosingForum.courseName ?: "Canvas LMS",
                                    fontSize = 9.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Footer informativo y Call to Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val repliesCount = nextClosingForum.discussionSubentryCount ?: 0
                            Text(
                                text = stringResource(Res.string.bento_foros_replies_count, repliesCount),
                                fontSize = 10.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )

                            Text(
                                text = stringResource(Res.string.bento_foros_go_to_discussion),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── CARD 4: Historial & Archivo de Tareas ───────────────────────────────────
@Composable
fun ArchiveBentoCard(
    count: Int = 12,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = if (onClick != null) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            cornerRadius = 10.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(Res.string.bento_archive_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.bento_archive_subtitle, count),
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 8.dp
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(Res.string.bento_archived_count, count),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}