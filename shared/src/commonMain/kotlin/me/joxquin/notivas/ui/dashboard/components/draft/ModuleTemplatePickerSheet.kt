package me.joxquin.notivas.ui.dashboard.components.draft

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults as MaterialCardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.TemplateCandidate
import me.joxquin.notivas.data.model.TemplateSourceType
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close as MiuixClose
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTemplatePickerSheet(
    courseName: String,
    templates: List<TemplateCandidate>,
    isLoading: Boolean,
    currentModuleName: String? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onSelectTemplate: (TemplateCandidate) -> Unit,
    onDismissRequest: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val miuixListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val materialListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val grouped = remember(templates) {
        templates.groupBy { it.moduleName ?: "General" }
    }

    LaunchedEffect(grouped, currentModuleName, isMiuix) {
        if (!currentModuleName.isNullOrBlank()) {
            if (isMiuix) {
                var miuixIndex = 0
                for ((modName, _) in grouped) {
                    if (modName.equals(currentModuleName, ignoreCase = true)) {
                        miuixListState.animateScrollToItem(miuixIndex)
                        break
                    }
                    // Cada módulo en MIUIX tiene 2 items en el LazyColumn: header item + card item
                    miuixIndex += 2
                }
            } else {
                var matIndex = 0
                for ((modName, itemsInMod) in grouped) {
                    if (modName.equals(currentModuleName, ignoreCase = true)) {
                        materialListState.animateScrollToItem(matIndex)
                        break
                    }
                    // Cada módulo en Material tiene 1 header item + N items
                    matIndex += 1 + itemsInMod.size
                }
            }
        }
    }

    if (isMiuix) {
        WindowBottomSheet(
            title = stringResource(Res.string.template_picker_title),
            show = true,
            onDismissRequest = onDismissRequest,
            startAction = {
                val dismissState = LocalDismissState.current
                MiuixIconButton(
                    onClick = {
                        dismissState?.invoke() ?: onDismissRequest()
                    }
                ) {
                    MiuixIcon(
                        imageVector = MiuixIcons.MiuixClose,
                        contentDescription = stringResource(Res.string.close),
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        ) {
            LazyColumn(
                state = miuixListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollEndHaptic()
                    .overScrollVertical()
            ) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MiuixTheme.colorScheme.primary
                                )
                                MiuixText(
                                    text = stringResource(Res.string.template_picker_loading),
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }
                    }
                } else if (templates.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FindInPage,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.size(32.dp)
                                )
                                MiuixText(
                                    text = stringResource(Res.string.template_picker_empty),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }
                    }
                } else {
                    grouped.forEach { (moduleName, itemsInModule) ->
                        val isCurrentModule = moduleName.equals(currentModuleName, ignoreCase = true) ||
                                itemsInModule.any { it.isCurrentAssignmentModule }

                        item(key = "header_$moduleName") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MiuixText(
                                        text = moduleName.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentModule) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (isCurrentModule) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            MiuixText(
                                                text = stringResource(Res.string.template_picker_current_module_badge),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MiuixTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "card_$moduleName") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                colors = CardDefaults.defaultColors(
                                    color = MiuixTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column {
                                    itemsInModule.forEachIndexed { index, template ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                color = MiuixTheme.colorScheme.dividerLine,
                                                thickness = 0.6.dp
                                            )
                                        }
                                        TemplateCandidateItemMiuix(
                                            template = template,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onSelectTemplate(template)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(
                            Modifier.padding(
                                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() + 20.dp
                            )
                        )
                    }
                }
            }
        }
    } else {
        WindowBottomSheet(
            show = true,
            onDismissRequest = onDismissRequest,
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            insideMargin = DpSize(0.dp, 0.dp),
            cornerRadius = 28.dp
        ) {
            val dismissState = LocalDismissState.current
            LazyColumn(
                state = materialListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = stringResource(Res.string.template_picker_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = courseName.ifBlank { "Canvas LMS" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = { dismissState?.invoke() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(Res.string.template_picker_loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (templates.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FindInPage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.template_picker_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    grouped.forEach { (moduleName, itemsInModule) ->
                        val isCurrentModule = moduleName.equals(currentModuleName, ignoreCase = true) ||
                            itemsInModule.any { it.isCurrentAssignmentModule }

                        item(key = "material_header_$moduleName") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = moduleName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentModule) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (isCurrentModule) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.template_picker_current_module_badge),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "material_card_$moduleName") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    itemsInModule.forEachIndexed { index, template ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                thickness = 0.8.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                        TemplateCandidateItemMaterial(
                                            template = template,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onSelectTemplate(template)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "material_bottom_spacer") {
                    Spacer(
                        Modifier.padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() + 16.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCandidateItemMiuix(
    template: TemplateCandidate,
    onClick: () -> Unit
) {
    val isDrive = template.type == TemplateSourceType.DRIVE_URL
    val badgeText = when {
        isDrive -> "DRIVE"
        template.fileExtension.equals("docx", ignoreCase = true) -> "DOCX"
        template.fileExtension.equals("xlsx", ignoreCase = true) -> "XLSX"
        template.fileExtension.equals("pdf", ignoreCase = true) -> "PDF"
        else -> "DOC"
    }

    val tintColor = MiuixTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tintColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDrive) Icons.Default.CloudSync else Icons.Default.Description,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                MiuixText(
                    text = template.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                MiuixText(
                    text = if (isDrive) stringResource(Res.string.template_picker_drive_desc) else stringResource(Res.string.template_picker_canvas_desc),
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = tintColor.copy(alpha = 0.12f),
            modifier = Modifier.wrapContentWidth()
        ) {
            MiuixText(
                text = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tintColor,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun TemplateCandidateItemMaterial(
    template: TemplateCandidate,
    onClick: () -> Unit
) {
    val isDrive = template.type == TemplateSourceType.DRIVE_URL
    val badgeText = when {
        isDrive -> "DRIVE"
        template.fileExtension.equals("docx", ignoreCase = true) -> "DOCX"
        template.fileExtension.equals("xlsx", ignoreCase = true) -> "XLSX"
        template.fileExtension.equals("pdf", ignoreCase = true) -> "PDF"
        else -> "DOC"
    }

    val tintColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tintColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDrive) Icons.Default.CloudSync else Icons.Default.Description,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isDrive) stringResource(Res.string.template_picker_drive_desc) else stringResource(Res.string.template_picker_canvas_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = tintColor.copy(alpha = 0.12f),
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = tintColor,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
}
