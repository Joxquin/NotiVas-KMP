package me.joxquin.notivas.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.UpdateInfo
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.update_dialog_changelog_label
import me.joxquin.notivas.shared.resources.update_dialog_icon_desc
import me.joxquin.notivas.shared.resources.update_dialog_later
import me.joxquin.notivas.shared.resources.update_dialog_notes_fallback
import me.joxquin.notivas.shared.resources.update_dialog_title
import me.joxquin.notivas.shared.resources.update_dialog_update
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import me.joxquin.notivas.ui.copilot.components.formatMarkdown
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
    show: Boolean = true
) {
    val isVisible = show && updateInfo.isUpdateAvailable
    val uriHandler = LocalUriHandler.current
    val notesText = updateInfo.releaseNotes.ifEmpty { stringResource(Res.string.update_dialog_notes_fallback) }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = isVisible,
            backdrop = backdrop,
            title = stringResource(Res.string.update_dialog_title),
            summary = updateInfo.releaseTitle.ifEmpty { null },
            onDismissRequest = onDismiss,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    MiuixText(
                        text = stringResource(Res.string.update_dialog_changelog_label),
                        style = MiuixTheme.textStyles.subtitle,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    MiuixText(
                        text = formatMarkdown(notesText),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassDialogActions {
                    GlassDialogButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.update_dialog_later),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                    GlassDialogButton(
                        onClick = {
                            if (isVisible) {
                                if (updateInfo.downloadUrl.isNotEmpty()) {
                                    uriHandler.openUri(updateInfo.downloadUrl)
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        role = GlassDialogButtonRole.Primary,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.update_dialog_update),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    } else if (isVisible) {
        BasicAlertDialog(
            onDismissRequest = onDismiss
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = stringResource(Res.string.update_dialog_icon_desc),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = stringResource(Res.string.update_dialog_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = updateInfo.releaseTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Release Notes Body
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = stringResource(Res.string.update_dialog_changelog_label),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatMarkdown(notesText),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(Res.string.update_dialog_later))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (isVisible) {
                                    if (updateInfo.downloadUrl.isNotEmpty()) {
                                        uriHandler.openUri(updateInfo.downloadUrl)
                                    }
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(Res.string.update_dialog_update))
                        }
                    }
                }
            }
        }
    }
}
