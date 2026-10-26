package me.joxquin.notivas.ui.course.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.course.CourseModuleItemUi
import me.joxquin.notivas.ui.course.CourseModuleUi
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun CourseLearningRoadmapCard(
    activeModule: CourseModuleUi?,
    totalModulesCount: Int,
    themeStyle: AppThemeStyle,
    onClick: () -> Unit,
    onItemClick: ((CourseModuleItemUi) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val innerCardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
    else MaterialTheme.colorScheme.surfaceContainerHigh

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    val successColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else Color(0xFF4CAF50)

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado de la tarjeta principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.course_detail_learning_path),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )

                if (totalModulesCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$totalModulesCount módulos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta Incrustada: Muestra únicamente la Semana Activa / Última
            if (activeModule != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(innerCardColor)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Título del módulo directamente provisto por Canvas
                        Text(
                            text = activeModule.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (activeModule.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeModule.description,
                                fontSize = 12.sp,
                                color = onSurfaceVariantColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de progreso del módulo
                        val animatedProgress by animateFloatAsState(
                            targetValue = (activeModule.progressPercentage / 100f).coerceIn(0f, 1f),
                            animationSpec = tween(400)
                        )

                        if (isMiuix) {
                            MiuixLinearProgressIndicator(
                                progress = animatedProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = primaryColor,
                                trackColor = onSurfaceVariantColor.copy(alpha = 0.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Texto de entregables listos y porcentaje
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.course_detail_deliverables_format,
                                    activeModule.completedDeliverablesCount,
                                    activeModule.totalDeliverablesCount
                                ),
                                fontSize = 11.sp,
                                color = onSurfaceVariantColor
                            )
                            Text(
                                text = stringResource(
                                    Res.string.course_detail_percent_completed_format,
                                    activeModule.progressPercentage
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                        }

                        // Preview de hasta 2 entregables / recursos del módulo
                        val previewItems = activeModule.allItems.take(2)
                        if (previewItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                previewItems.forEach { item ->
                                    RoadmapItemPreviewRow(
                                        item = item,
                                        onSurfaceColor = onSurfaceColor,
                                        onSurfaceVariantColor = onSurfaceVariantColor,
                                        successColor = successColor,
                                        primaryColor = primaryColor,
                                        onClick = { onItemClick?.invoke(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(Res.string.course_detail_empty_modules),
                    fontSize = 13.sp,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pie de tarjeta interactivo para ver todos los módulos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.course_detail_view_all_modules, totalModulesCount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (isMiuix) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = cardColor),
            insideMargin = PaddingValues(0.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onClick
        ) {
            content()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardColor)
                .clickable(onClick = onClick)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
private fun RoadmapItemPreviewRow(
    item: CourseModuleItemUi,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    successColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de estado
        val statusBg = if (item.isCompleted) successColor.copy(alpha = 0.15f)
        else primaryColor.copy(alpha = 0.15f)
        val statusIconTint = if (item.isCompleted) successColor else primaryColor

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(statusBg),
            contentAlignment = Alignment.Center
        ) {
            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = statusIconTint,
                    modifier = Modifier.size(14.dp)
                )
            } else if (item.isPending) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = statusIconTint,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = statusIconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.metaSubtitle,
                fontSize = 10.sp,
                color = onSurfaceVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
