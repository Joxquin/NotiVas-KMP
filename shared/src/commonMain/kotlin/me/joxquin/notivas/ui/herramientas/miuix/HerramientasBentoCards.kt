package me.joxquin.notivas.ui.herramientas.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

// ─── CONSTANTES DE COLOR MIUIX ──────────────────────────────────────────────
private val MiuixBlue = Color(0xFF3482FF)
private val MiuixRed = Color(0xFFFF5449)
private val MiuixAmber = Color(0xFFFFB74D)
private val MiuixGreen = Color(0xFF66BB6A)
private val MiuixPurple = Color(0xFF8B5CF6)

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

    val courseName = uiState?.latestGradedCourseName ?: uiState?.selectedCourse?.name ?: "Curso activo"
    val taskName = uiState?.latestGradedAssignmentName ?: "Sin evaluaciones calificadas"
    val taskScore = uiState?.latestGradedScoreFormatted ?: "Pendiente"

    val (statusLabel, statusColor, statusBg) = when {
        currentAvg >= 15.0f -> Triple("Buen rendimiento", Color(0xFF2E7D32), Color(0xFF2E7D32).copy(alpha = 0.12f))
        currentAvg >= 12.0f -> Triple("Arriba del promedio", MiuixBlue, MiuixBlue.copy(alpha = 0.12f))
        currentAvg > 0f -> Triple("En riesgo", MiuixRed, MiuixRed.copy(alpha = 0.12f))
        else -> Triple("Sin evaluar", MiuixTheme.colorScheme.onSurfaceVariantSummary, MiuixTheme.colorScheme.surfaceContainerHigh)
    }

    val evaluatedPct = (progressRatio * 100f).toInt()
    val formattedProj = ((projectedGrade * 10f).toInt() / 10f).toString()
    val formattedAvg = ((currentAvg * 10f).toInt() / 10f).toString()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 22.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 22.dp
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
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
                                color = MiuixBlue.copy(alpha = 0.15f),
                                cornerRadius = 12.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixBlue.copy(alpha = 0.35f),
                                cornerRadius = 12.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        M3Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = null,
                            tint = MiuixBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Progreso",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Notas y promedio actual",
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

            // Chip interno: Curso y Última Tarea Calificada
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
                            color = if (currentAvg >= 12f) MiuixBlue else MiuixRed
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
                        val progressColor = if (currentAvg >= 12f) Color(0xFF4CAF50) else MiuixRed
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
                            text = "Evaluada: $evaluatedPct%",
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = "Proyectada: $formattedProj",
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = "Promedio: $formattedAvg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentAvg >= 12f) MiuixBlue else MiuixRed
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 22.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 22.dp
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
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
                            .size(34.dp)
                            .squircleSurface(
                                color = MiuixGreen.copy(alpha = 0.15f),
                                cornerRadius = 10.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixGreen.copy(alpha = 0.3f),
                                cornerRadius = 10.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        M3Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MiuixGreen,
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
                            text = "7 Cursos",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Simulador",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Nota mínima y escenarios de aprobación",
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
                        text = "PROYECCIÓN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(
                        text = "16.2 / 20",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixGreen
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
                            .background(MiuixGreen)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Promedio ponderado estimado",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

// ─── CARD: Foros de Discusión ───────────────────────────────────────────────
@Composable
fun ForosBentoCard(
    forosUiState: me.joxquin.notivas.ui.foros.ForosUiState? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onOpenTopic: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null
) {
    val discussions = forosUiState?.discussions ?: emptyList()
    val now = java.time.ZonedDateTime.now()

    // Encontrar el foro activo más próximo a cerrar
    val nextClosingForum = discussions
        .filter { topic ->
            if (topic.locked == true) return@filter false
            val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt ?: return@filter false
            try {
                val dueDate = java.time.ZonedDateTime.parse(dueStr)
                dueDate.isAfter(now)
            } catch (_: Exception) {
                false
            }
        }
        .minByOrNull { topic ->
            val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt!!
            java.time.ZonedDateTime.parse(dueStr).toInstant().toEpochMilli()
        } ?: discussions.firstOrNull()

    val pendingCount = forosUiState?.pendingCount ?: discussions.size
    val activeBadgesText = if (pendingCount > 0) "$pendingCount activos" else "Al día"

    var dueBadgeText = "Sin fecha de cierre"
    var isUrgent = false

    if (nextClosingForum != null) {
        val dueStr = nextClosingForum.assignment?.dueAt ?: nextClosingForum.lockAt ?: nextClosingForum.assignment?.lockAt
        if (dueStr != null) {
            try {
                val dueDate = java.time.ZonedDateTime.parse(dueStr)
                val duration = java.time.Duration.between(now, dueDate)
                if (now.isAfter(dueDate) || nextClosingForum.locked == true) {
                    dueBadgeText = "Cerrado"
                } else {
                    val hours = duration.toHours()
                    val days = duration.toDays()
                    if (hours in 0..24) {
                        isUrgent = true
                        val minutes = duration.toMinutes()
                        dueBadgeText = if (hours > 0) "Hoy (~$hours h)" else "Hoy (~$minutes min)"
                    } else if (days > 0) {
                        dueBadgeText = "En $days días"
                    } else {
                        dueBadgeText = "Pronto"
                    }
                }
            } catch (_: Exception) {
                dueBadgeText = "Fecha fijada"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 22.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 22.dp
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fila Superior: Icono, Título y Badge de Estado Activo
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
                                color = MiuixBlue.copy(alpha = 0.15f),
                                cornerRadius = 11.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixBlue.copy(alpha = 0.35f),
                                cornerRadius = 11.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        M3Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MiuixBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Foros Académicos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = "Debates y respuestas con asistencia Copilot IA",
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
                            color = MiuixBlue.copy(alpha = 0.12f),
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = MiuixBlue.copy(alpha = 0.25f),
                            cornerRadius = 9999.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = activeBadgesText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixBlue
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
                                        .background(if (isUrgent) MiuixRed else MiuixBlue)
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

                            Text(
                                text = dueBadgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUrgent) MiuixRed else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        // Banner sutil Copilot IA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MiuixPurple.copy(alpha = 0.10f))
                                .border(0.5.dp, MiuixPurple.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val points = nextClosingForum.assignment?.pointsPossible
                                val rubricInfo = if (points != null && points > 0) "Rúbrica: ${points.toInt()} pts" else "Participación activa"
                                Text(
                                    text = "✨ Copilot: $rubricInfo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixPurple,
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
                                text = "$repliesCount aportes registrados",
                                fontSize = 10.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )

                            Text(
                                text = "Ir al debate →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixBlue
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
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 22.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 22.dp
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
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
                    M3Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Historial & Archivo de Tareas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Descartadas y entregas pasadas (12 archivadas)",
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
                    text = "12 Archivadas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}