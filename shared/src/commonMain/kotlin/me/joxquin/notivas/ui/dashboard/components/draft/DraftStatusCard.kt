package me.joxquin.notivas.ui.dashboard.components.draft

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DraftStatusCard(
    assignment: Assignment,
    courseName: String,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    accountEmail: String = "usuario@institucional.edu",
    draftInfo: DraftSyncInfo? = null,
    modifier: Modifier = Modifier,
    onStartDraft: (isTemplate: Boolean, templateName: String?) -> Unit = { _, _ -> },
    onExploreTemplates: () -> Unit = {},
    onCancelDraft: () -> Unit = {},
    onOpenDocs: (String) -> Unit = {},
    onSubmitToCanvas: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val isSubmitted = assignment.status == "completed" ||
            assignment.isCompleted ||
            (assignment.submission?.workflowState in listOf("submitted", "graded")) ||
            (!assignment.submittedAt.isNullOrBlank()) ||
            (!assignment.submission?.submittedAt.isNullOrBlank())

    // Detección contextual de plantilla docente en descripción HTML
    val detectedTemplateName = remember(assignment) {
        val desc = assignment.description ?: ""
        Regex("""([\w\-_]+\.docx)""").find(desc)?.value
            ?: if (desc.contains("docs.google.com")) "Plantilla_Docente_Enlace" else null
    }

    // Marca de tiempo relativa formateada
    val dueAtFormatted = remember(assignment.dueAt) {
        assignment.dueAt?.let { due ->
            val components = DateTimeUtil.parseIsoToComponents(due)
            if (components != null) {
                "${components.date.day.toString().padStart(2, '0')}/${components.date.month.toString().padStart(2, '0')} ${components.hour.toString().padStart(2, '0')}:${components.minute.toString().padStart(2, '0')}"
            } else due.take(10)
        } ?: "Sin fecha límite"
    }

    val relativeRemaining = remember(assignment.dueAt) {
        assignment.dueAt?.let { due ->
            val comp = DateTimeUtil.parseIsoToComponents(due)
            if (comp != null) {
                val diffHours = (comp.epochMillis - DateTimeUtil.nowEpochMillis()) / (1000 * 3600)
                if (diffHours < 0) "Vencida"
                else if (diffHours < 24) "$diffHours h restantes"
                else "${diffHours / 24} d restantes"
            } else null
        }
    }

    val currentInfo = remember(draftInfo, assignment, detectedTemplateName, accountEmail) {
        draftInfo ?: DraftSyncInfo(
            fileName = "${assignment.name.take(24)} - ${if (detectedTemplateName != null) "Plantilla" else "Borrador"}.gdoc",
            driveUrl = "",
            lastEditedTime = "Editado hace un momento",
            accountEmail = accountEmail,
            detectedTemplate = detectedTemplateName,
            courseFolder = courseName.ifBlank { "General" },
            draftStatus = if (isSubmitted) DraftStatusEnum.SUBMITTED else DraftStatusEnum.NOT_STARTED,
            hasSubmitted = isSubmitted
        )
    }

    var isOptimisticSyncing by remember { mutableStateOf(false) }

    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onPrimaryColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary

    val triggerStart: (Boolean) -> Unit = { isTemplate ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        isOptimisticSyncing = true
        onStartDraft(isTemplate, detectedTemplateName)
        scope.launch {
            delay(1200)
            isOptimisticSyncing = false
        }
    }

    val content = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fila de Metadatos de la Tarea (Puntos, Fecha Límite, Temporizador)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    AdaptiveText(
                        text = dueAtFormatted,
                        size = 11.sp,
                        weight = FontWeight.Medium,
                        isMiuix = isMiuix,
                        color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relativeRemaining?.let { rel ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = rel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    assignment.pointsPossible?.let { pts ->
                        val ptsFormatted = if (pts % 1.0 == 0.0) "${pts.toInt()} pts" else "$pts pts"
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = ptsFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = if (isMiuix) MiuixTheme.colorScheme.outline.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )

            // Estado Canónico: Si hasSubmitted == 1 -> Badge de Entrega Institucional
            if (isSubmitted || currentInfo.hasSubmitted) {
                StatusHeader(
                    icon = Icons.Default.CheckCircle,
                    iconColor = primaryColor,
                    title = stringResource(Res.string.draft_status_submitted_title),
                    subtitle = stringResource(Res.string.draft_status_submitted_desc),
                    isMiuix = isMiuix
                )
            } else if (isOptimisticSyncing) {
                // Feedback Optimista Inmediato (< 200 ms)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = primaryColor
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        AdaptiveText(stringResource(Res.string.draft_creating_in_drive), 14.sp, FontWeight.Bold, isMiuix)
                        AdaptiveText(stringResource(Res.string.draft_organizing_in_folder, currentInfo.courseFolder), 11.sp, FontWeight.Medium, isMiuix, primaryColor)
                    }
                }
            } else if (currentInfo.draftStatus == DraftStatusEnum.NOT_STARTED) {
                // Estado: Sin Iniciar
                StatusHeader(
                    icon = Icons.Default.CloudSync,
                    iconColor = primaryColor,
                    title = stringResource(Res.string.draft_status_card_title),
                    subtitle = stringResource(Res.string.draft_drive_sync_subtitle),
                    isMiuix = isMiuix
                )

                // Detección contextual de plantilla docente (Modalidad B)
                if (detectedTemplateName != null) {
                    InnerTemplateBox(
                        fileName = detectedTemplateName,
                        subtitle = stringResource(Res.string.draft_official_template_subtitle),
                        badgeFormat = if (detectedTemplateName.endsWith(".docx")) "DOCX" else "DOCS",
                        isMiuix = isMiuix
                    )

                    ActionButton(
                        text = stringResource(Res.string.draft_btn_start_template, detectedTemplateName),
                        icon = Icons.Default.GroupAdd,
                        bgColor = primaryColor,
                        textColor = onPrimaryColor,
                        isMiuix = isMiuix,
                        onClick = { triggerStart(true) }
                    )
                } else {
                    // Modalidad A (Borrador en blanco)
                    ActionButton(
                        text = stringResource(Res.string.draft_btn_start_blank_drive),
                        icon = Icons.Default.AddCircleOutline,
                        bgColor = primaryColor,
                        textColor = onPrimaryColor,
                        isMiuix = isMiuix,
                        onClick = { triggerStart(false) }
                    )
                }

                // Opción para examinar otros módulos de Canvas
                ActionButton(
                    text = stringResource(Res.string.draft_btn_explore_modules),
                    icon = Icons.Default.FolderOpen,
                    bgColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.secondaryContainer,
                    textColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
                    isMiuix = isMiuix,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onExploreTemplates()
                    }
                )
            } else {
                // Estado: IN_PROGRESS o READY_TO_SUBMIT (Trabajo Activo)
                val isDocumentReady = currentInfo.driveUrl.isNotBlank() && !currentInfo.driveUrl.contains("sample_")

                StatusHeader(
                    icon = Icons.Default.Description,
                    iconColor = primaryColor,
                    title = currentInfo.fileName,
                    subtitle = "Drive / NotiVas / ${currentInfo.courseFolder}",
                    isMiuix = isMiuix
                )

                // Badge de estado de sincronización en la nube
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isDocumentReady) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor)
                            )
                            AdaptiveText(
                                text = stringResource(Res.string.draft_synced_cloud_status),
                                size = 11.sp,
                                weight = FontWeight.Medium,
                                isMiuix = isMiuix,
                                color = primaryColor
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 2.dp,
                                color = primaryColor
                            )
                            AdaptiveText(
                                text = stringResource(Res.string.draft_syncing_cloud_status),
                                size = 11.sp,
                                weight = FontWeight.Medium,
                                isMiuix = isMiuix,
                                color = primaryColor
                            )
                        }
                    }

                    if (!isDocumentReady) {
                        Text(
                            text = stringResource(Res.string.draft_cancel_action),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCancelDraft()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        text = if (isDocumentReady) stringResource(Res.string.draft_open_docs_btn) else stringResource(Res.string.draft_preparing_docs),
                        icon = Icons.Default.Launch,
                        bgColor = primaryColor,
                        textColor = onPrimaryColor,
                        isMiuix = isMiuix,
                        enabled = isDocumentReady,
                        onClick = { onOpenDocs(currentInfo.driveUrl) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = stringResource(Res.string.draft_btn_send_to_canvas),
                        icon = Icons.Default.Send,
                        bgColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.secondaryContainer,
                        textColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
                        isMiuix = isMiuix,
                        enabled = isDocumentReady,
                        onClick = onSubmitToCanvas,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (isMiuix) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .squircleSurface(MiuixTheme.colorScheme.surfaceContainer, 24.dp)
                .squircleBorder(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.08f), 24.dp)
                .padding(18.dp)
        ) {
            content()
        }
    } else {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Box(modifier = Modifier.padding(18.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun StatusHeader(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isMiuix: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column {
            AdaptiveText(title, 14.sp, FontWeight.Bold, isMiuix)
            AdaptiveText(
                text = subtitle,
                size = 11.sp,
                weight = FontWeight.Normal,
                isMiuix = isMiuix,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InnerTemplateBox(
    fileName: String,
    subtitle: String,
    badgeFormat: String,
    isMiuix: Boolean
) {
    val shape = RoundedCornerShape(14.dp)
    val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = primaryColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                AdaptiveText(
                    text = fileName,
                    size = 12.sp,
                    weight = FontWeight.SemiBold,
                    isMiuix = isMiuix,
                    maxLines = 1
                )
                AdaptiveText(
                    text = subtitle,
                    size = 10.sp,
                    weight = FontWeight.Normal,
                    isMiuix = isMiuix,
                    color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = badgeFormat,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    isMiuix: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true
) {
    val effectiveBg = if (enabled) bgColor else bgColor.copy(alpha = 0.45f)
    val effectiveText = if (enabled) textColor else textColor.copy(alpha = 0.65f)
    if (isMiuix) {
        Row(
            modifier = modifier
                .squircleSurface(effectiveBg, 14.dp)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = effectiveText, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            MiuixText(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = effectiveText)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = bgColor,
                contentColor = textColor,
                disabledContainerColor = bgColor.copy(alpha = 0.45f),
                disabledContentColor = textColor.copy(alpha = 0.65f)
            )
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdaptiveText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    isMiuix: Boolean,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    if (isMiuix) {
        MiuixText(
            text = text,
            fontSize = size,
            fontWeight = weight,
            color = if (color == Color.Unspecified) MiuixTheme.colorScheme.onSurface else color,
            maxLines = maxLines,
            overflow = if (maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    } else {
        Text(
            text = text,
            fontSize = size,
            fontWeight = weight,
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color,
            maxLines = maxLines,
            overflow = if (maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    }
}
