package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.draft_export_cancel
import me.joxquin.notivas.shared.resources.draft_export_docx_desc
import me.joxquin.notivas.shared.resources.draft_export_docx_title
import me.joxquin.notivas.shared.resources.draft_export_not_allowed_badge
import me.joxquin.notivas.shared.resources.draft_export_pdf_desc
import me.joxquin.notivas.shared.resources.draft_export_pdf_title
import me.joxquin.notivas.shared.resources.draft_export_recommended_badge
import me.joxquin.notivas.shared.resources.draft_export_send
import me.joxquin.notivas.shared.resources.draft_export_send_format
import me.joxquin.notivas.shared.resources.draft_export_sheet_allowed_hint
import me.joxquin.notivas.shared.resources.draft_export_sheet_subtitle
import me.joxquin.notivas.shared.resources.draft_export_sheet_title
import me.joxquin.notivas.ui.components.miuix.GlassSheetHeaderAction
import me.joxquin.notivas.ui.dashboard.DraftExportFormat
import me.joxquin.notivas.data.local.AppThemeStyle
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassBottomSheet
import top.yukonga.miuix.kmp.glass.GlassBottomSheetDefaults
import top.yukonga.miuix.kmp.glass.GlassCard
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowBottomSheet

/**
 * Hoja unificada (idéntica en MIUIX y Material) para elegir el formato de conversión (.pdf o .docx)
 * antes de subir un borrador de Google Drive a Canvas LMS.
 */
@Composable
fun DraftExportFormatBottomSheet(
    show: Boolean,
    allowedExtensions: List<String>,
    themeStyle: AppThemeStyle,
    backdrop: Backdrop?,
    onDismissRequest: () -> Unit,
    onConfirmExport: (DraftExportFormat) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isPdfAllowed = remember(allowedExtensions) {
        allowedExtensions.isEmpty() || "pdf" in allowedExtensions
    }
    val isDocxAllowed = remember(allowedExtensions) {
        allowedExtensions.isEmpty() || "docx" in allowedExtensions
    }

    var selectedFormat by remember(show, isPdfAllowed, isDocxAllowed) {
        mutableStateOf(
            when {
                isPdfAllowed -> DraftExportFormat.PDF
                isDocxAllowed -> DraftExportFormat.DOCX
                else -> null
            }
        )
    }

    LaunchedEffect(show, isPdfAllowed, isDocxAllowed) {
        if (show) {
            selectedFormat = when {
                isPdfAllowed -> DraftExportFormat.PDF
                isDocxAllowed -> DraftExportFormat.DOCX
                else -> null
            }
        }
    }

    var pendingConfirm by remember { mutableStateOf<DraftExportFormat?>(null) }

    val sheetBody: @Composable (dismissAction: () -> Unit) -> Unit = { dismissAction ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.draft_export_sheet_subtitle),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )

            if (allowedExtensions.isNotEmpty()) {
                val allowedText = remember(allowedExtensions) {
                    allowedExtensions.joinToString(", ") { ".${it.lowercase()}" }
                }
                Text(
                    text = stringResource(Res.string.draft_export_sheet_allowed_hint, allowedText),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.primary
                )
            }

            FormatOptionCard(
                title = stringResource(Res.string.draft_export_pdf_title),
                description = stringResource(Res.string.draft_export_pdf_desc),
                icon = Icons.Default.PictureAsPdf,
                badgeText = if (isPdfAllowed) {
                    stringResource(Res.string.draft_export_recommended_badge)
                } else {
                    stringResource(Res.string.draft_export_not_allowed_badge)
                },
                isAllowed = isPdfAllowed,
                isSelected = selectedFormat == DraftExportFormat.PDF,
                onClick = {
                    if (isPdfAllowed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedFormat = DraftExportFormat.PDF
                    }
                }
            )

            FormatOptionCard(
                title = stringResource(Res.string.draft_export_docx_title),
                description = stringResource(Res.string.draft_export_docx_desc),
                icon = Icons.Default.Description,
                badgeText = if (!isDocxAllowed) {
                    stringResource(Res.string.draft_export_not_allowed_badge)
                } else null,
                isAllowed = isDocxAllowed,
                isSelected = selectedFormat == DraftExportFormat.DOCX,
                onClick = {
                    if (isDocxAllowed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedFormat = DraftExportFormat.DOCX
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassDialogButton(
                    onClick = {
                        pendingConfirm = null
                        dismissAction()
                    },
                    modifier = Modifier.weight(1f),
                    role = GlassDialogButtonRole.Normal
                ) {
                    Text(
                        text = stringResource(Res.string.draft_export_cancel),
                        style = MiuixTheme.textStyles.button
                    )
                }

                val sendLabel = selectedFormat?.let {
                    stringResource(Res.string.draft_export_send_format, it.displayName)
                } ?: stringResource(Res.string.draft_export_send)

                GlassDialogButton(
                    onClick = {
                        val format = selectedFormat ?: return@GlassDialogButton
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pendingConfirm = format
                        dismissAction()
                    },
                    enabled = selectedFormat != null,
                    modifier = Modifier.weight(1f),
                    role = GlassDialogButtonRole.Primary
                ) {
                    Text(
                        text = sendLabel,
                        style = MiuixTheme.textStyles.button
                    )
                }
            }
        }
    }

    val onFinishDismiss: () -> Unit = {
        val chosen = pendingConfirm
        pendingConfirm = null
        if (chosen != null) {
            onConfirmExport(chosen)
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassBottomSheet(
            title = stringResource(Res.string.draft_export_sheet_title),
            show = show,
            onDismissRequest = onDismissRequest,
            backdrop = backdrop,
            progressiveBlur = true,
            insideMargin = GlassBottomSheetDefaults.InsideMargin,
            onDismissFinished = onFinishDismiss,
            startAction = {
                val dismissState = LocalDismissState.current
                GlassSheetHeaderAction(
                    onClick = {
                        pendingConfirm = null
                        dismissState?.invoke() ?: onDismissRequest()
                    },
                    imageVector = MiuixIcons.Glass.Close,
                    contentDescription = stringResource(Res.string.close)
                )
            }
        ) { innerPadding ->
            val dismissState = LocalDismissState.current
            Box(modifier = Modifier.padding(innerPadding)) {
                sheetBody {
                    dismissState?.invoke() ?: onDismissRequest()
                }
            }
        }
    } else {
        WindowBottomSheet(
            title = stringResource(Res.string.draft_export_sheet_title),
            show = show,
            onDismissRequest = onDismissRequest,
            onDismissFinished = onFinishDismiss,
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        ) {
            val dismissState = LocalDismissState.current
            sheetBody {
                dismissState?.invoke() ?: onDismissRequest()
            }
        }
    }
}

@Composable
private fun FormatOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String?,
    isAllowed: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected && isAllowed) {
        MiuixTheme.colorScheme.primary
    } else {
        MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.35f)
    }
    val cardColor = if (isSelected && isAllowed) {
        MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isAllowed) 1f else 0.5f)
            .border(
                width = if (isSelected && isAllowed) 1.5.dp else 0.8.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(14.dp),
        colors = CardDefaults.defaultColors(color = cardColor),
        pressFeedbackType = if (isAllowed) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = if (isAllowed) onClick else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected && isAllowed) {
                            MiuixTheme.colorScheme.primary.copy(alpha = 0.16f)
                        } else {
                            MiuixTheme.colorScheme.surfaceContainerHighest
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected && isAllowed) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isAllowed) {
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)
                                    } else {
                                        MiuixTheme.colorScheme.errorContainer
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAllowed) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            Icon(
                imageVector = if (isSelected && isAllowed) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = if (isSelected && isAllowed) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                },
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
