package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixGreen = Color(0xFF4CAF50)
private val UrgentRed = Color(0xFFE53935)

@Composable
fun AssignmentDetailHeroCard(
    assignment: Assignment,
    courseName: String,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val nowMs = DateTimeUtil.nowEpochMillis()
    val dueStr = assignment.dueAt ?: assignment.lockAt

    val (dueText, isUrgent) = remember(dueStr) {
        if (dueStr == null) {
            "Sin fecha" to false
        } else {
            val dueComp = DateTimeUtil.parseIsoToComponents(dueStr)
            if (dueComp != null) {
                val diffMs = dueComp.epochMillis - nowMs
                if (diffMs <= 0) {
                    "Vencida" to true
                } else {
                    val totalMinutes = diffMs / (1000 * 60)
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60
                    val days = hours / 24
                    val timeStr = "${dueComp.hour.toString().padStart(2, '0')}:${dueComp.minute.toString().padStart(2, '0')}"
                    when {
                        days > 1 -> "Vence en ${days}d • ${dueStr.take(10)}" to false
                        days == 1L -> "Vence mañana • $timeStr" to true
                        hours > 0 -> "Vence en ${hours}h ${minutes}m • Hoy $timeStr" to true
                        else -> "Vence en ${minutes}m • Hoy $timeStr" to true
                    }
                }
            } else {
                (dueStr.take(10)) to false
            }
        }
    }

    val pointsText = remember(assignment.pointsPossible) {
        assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin calificar"
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila Superior: DETALLE DE TAREA + Badge Sincronizado Canvas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiuixText(
                    text = stringResource(Res.string.task_detail_header_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )

                // Badge Sincronizado Canvas
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MiuixGreen.copy(alpha = 0.12f),
                    modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MiuixGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.canvas_synced_badge),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixGreen
                        )
                    }
                }
            }

            // Pill del Curso
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.clip(RoundedCornerShape(9999.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    MiuixText(
                        text = courseName.ifBlank { "Canvas LMS" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Título de la Tarea
            MiuixText(
                text = assignment.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            // Fila de Pills (Vencimiento + Puntos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill Vencimiento
                val dueBg = if (isUrgent) UrgentRed.copy(alpha = 0.12f) else MiuixTheme.colorScheme.surfaceContainerHigh
                val dueColor = if (isUrgent) UrgentRed else MiuixTheme.colorScheme.onSurfaceVariantSummary

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = dueBg,
                    modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = dueColor,
                            modifier = Modifier.size(14.dp)
                        )
                        MiuixText(
                            text = dueText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dueColor
                        )
                    }
                }

                // Pill Puntos / Calificado
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(14.dp)
                        )
                        MiuixText(
                            text = "$pointsText • ${stringResource(Res.string.canvas_graded_badge)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fila Superior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.task_detail_header_label),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(Res.string.canvas_synced_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Curso
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = courseName.ifBlank { "Canvas LMS" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Título
                Text(
                    text = assignment.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Fila de Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dueContainer = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHighest
                    val dueColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = dueContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = dueColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = dueText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = dueColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$pointsText • ${stringResource(Res.string.canvas_graded_badge)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
