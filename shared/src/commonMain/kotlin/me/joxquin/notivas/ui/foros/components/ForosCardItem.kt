package me.joxquin.notivas.ui.foros.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Duration
import java.time.ZonedDateTime

@Composable
fun ForosCardItem(
    topic: CanvasDiscussionTopic,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val now = ZonedDateTime.now()
    val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
    var isExpired = topic.locked == true
    var dueText = "Sin fecha de cierre"
    var isUrgent = false

    if (dueStr != null) {
        try {
            val dueDate = ZonedDateTime.parse(dueStr)
            if (now.isAfter(dueDate)) {
                isExpired = true
                dueText = "Cerrado"
            } else {
                val duration = Duration.between(now, dueDate)
                val hours = duration.toHours()
                val days = duration.toDays()

                if (hours <= 24) {
                    isUrgent = true
                    val minutes = duration.toMinutes()
                    dueText = if (hours > 0) "Vence hoy (~$hours h)" else "Vence hoy (~$minutes min)"
                } else {
                    dueText = "Vence en $days días"
                }
            }
        } catch (_: Exception) {
            dueText = "Fecha de entrega indicada"
        }
    }

    val points = topic.assignment?.pointsPossible
    val score = topic.assignment?.submission?.score
    val hasPoints = points != null && points > 0
    val isGraded = score != null

    val cardBg = if (themeStyle == AppThemeStyle.MIUIX) {
        if (isExpired) MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f) else MiuixTheme.colorScheme.surfaceContainer
    } else {
        if (isExpired) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceContainer
    }

    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val authorName = topic.author?.displayName ?: topic.userName ?: "Profesor del curso"
    val repliesCount = topic.discussionSubentryCount ?: 0

    // Limpieza rápida de HTML en el preview del mensaje
    val cleanMessage = topic.message
        ?.replace(Regex("<[^>]*>"), " ")
        ?.replace(Regex("&nbsp;"), " ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Accede al foro de debate para consultar la consigna y participar activamente con tus compañeros."

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila superior: Curso + Badge de Puntos / Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = topic.courseName ?: "Curso Canvas",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (isExpired) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isGraded) "Calificado • Cerrado" else "Cerrado / Vencido",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFE53935),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else if (hasPoints) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (isUrgent) Color(0xFFE53935).copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${points?.toInt() ?: points} Pts • Calificado",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isUrgent) Color(0xFFE53935) else primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "Formativo / Sin puntos",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Título del Foro
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                ),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Resumen de la consigna / mensaje
            Text(
                text = cleanMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bloque de nota obtenida si ya está calificado
            if (isGraded && score != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Calificación obtenida",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$score / ${points ?: score} pts",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }

            // Metadatos: Vencimiento, respuestas y docente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isUrgent) Icons.Default.Timer else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isUrgent) Color(0xFFE53935) else primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = if (isUrgent) Color(0xFFE53935) else primaryColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$repliesCount intervenciones",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Fila inferior: Estado de participación + Botón de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isExpired) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isUrgent) Color(0xFFE53935) else primaryColor)
                            )
                            Text(
                                text = "Disponible para participar",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Foro cerrado",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ingresar al foro",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
