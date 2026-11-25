package me.joxquin.notivas.ui.notas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.GlassSheetHeaderAction
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.navigationBarsPadding
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
fun GroupCreationChoiceBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelectManual: () -> Unit,
    onSelectGitHub: () -> Unit,
    onSelectCopilot: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    var pendingSelection by remember { mutableStateOf<(() -> Unit)?>(null) }
    val choiceScrollState = rememberScrollState()
    val headerScrollOffset = remember(choiceScrollState) {
        derivedStateOf { choiceScrollState.value.toFloat() }
    }

    val sheetContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(choiceScrollState)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(Res.string.groups_create_choice_desc),
                fontSize = 13.sp,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Opción 1: Generar con Copilot (Destacado)
            CreationOptionCard(
                title = stringResource(Res.string.groups_create_copilot_title),
                subtitle = stringResource(Res.string.groups_create_copilot_desc),
                icon = Icons.Outlined.AutoAwesome,
                isFeatured = true,
                themeStyle = themeStyle,
                onClick = {
                    pendingSelection = { onSelectCopilot() }
                    onDismiss()
                }
            )

            // Opción 2: Plantillas GitHub
            CreationOptionCard(
                title = stringResource(Res.string.groups_create_github_title),
                subtitle = stringResource(Res.string.groups_create_github_desc),
                icon = Icons.Outlined.CloudDownload,
                isFeatured = false,
                themeStyle = themeStyle,
                onClick = {
                    pendingSelection = { onSelectGitHub() }
                    onDismiss()
                }
            )

            // Opción 3: Manual
            CreationOptionCard(
                title = stringResource(Res.string.groups_create_manual_title),
                subtitle = stringResource(Res.string.groups_create_manual_desc),
                icon = Icons.Outlined.AddCircleOutline,
                isFeatured = false,
                themeStyle = themeStyle,
                onClick = {
                    pendingSelection = { onSelectManual() }
                    onDismiss()
                }
            )
        }
    }

    val finishSelection: () -> Unit = {
        val action = pendingSelection
        pendingSelection = null
        action?.invoke()
    }

    if (isMiuix) {
        GlassBottomSheet(
            title = stringResource(Res.string.groups_create_choice_title),
            show = show,
            onDismissRequest = onDismiss,
            backdrop = backdrop,
            progressiveBlur = true,
            scrollOffset = { headerScrollOffset.value },
            onDismissFinished = finishSelection,
            startAction = {
                val dismissState = LocalDismissState.current
                GlassSheetHeaderAction(
                    onClick = { dismissState?.invoke() ?: onDismiss() },
                    imageVector = MiuixIcons.Glass.Close,
                    contentDescription = stringResource(Res.string.close),
                )
            },
            content = { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    sheetContent()
                }
            }
        )
    } else {
        val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
        val dragHandleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        WindowBottomSheet(
            title = stringResource(Res.string.groups_create_choice_title),
            show = show,
            onDismissRequest = onDismiss,
            onDismissFinished = finishSelection,
            backgroundColor = backgroundColor,
            dragHandleColor = dragHandleColor,
            content = sheetContent
        )
    }
}

@Composable
private fun CreationOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isFeatured: Boolean,
    themeStyle: AppThemeStyle,
    onClick: () -> Unit
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val cardColor = if (isMiuix) {
        if (isFeatured) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainer
    } else {
        if (isFeatured) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer
    }
    val iconBgColor = if (isMiuix) {
        if (isFeatured) MiuixTheme.colorScheme.primary.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        if (isFeatured) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconTintColor = if (isMiuix) {
        if (isFeatured) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
    } else {
        if (isFeatured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    }
    val titleColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val arrowColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = cardColor),
        insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = arrowColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
