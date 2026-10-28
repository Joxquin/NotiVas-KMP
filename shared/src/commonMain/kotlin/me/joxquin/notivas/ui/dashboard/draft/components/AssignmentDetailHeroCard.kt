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

    val gradingText = remember(assignment.pointsPossible) {
        val pts = assignment.pointsPossible
        if (pts != null && pts > 0) {
            val ptsFormatted = if (pts % 1.0 == 0.0) "${pts.toInt()} pts" else "$pts pts"
            "Calificado $ptsFormatted"
        } else {
            "No calificado"
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila de Pills (Vencimiento + Puntos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill Vencimiento
                val dueBg = if (isUrgent) MiuixTheme.colorScheme.errorContainer else MiuixTheme.colorScheme.surfaceContainerHigh
                val dueColor = if (isUrgent) MiuixTheme.colorScheme.onErrorContainer else MiuixTheme.colorScheme.onSurfaceVariantSummary

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
                            text = gradingText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                            text = gradingText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
