package me.joxquin.notivas.ui.notas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.remote.PonderacionApiService
import me.joxquin.notivas.domain.template.CourseEvaluationTemplate
import me.joxquin.notivas.domain.template.EvaluationTemplatesCatalog
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportTemplateDialog(
    onDismiss: () -> Unit,
    showApplyToAllToggle: Boolean = false,
    onApplyTemplate: (template: CourseEvaluationTemplate, applyToAll: Boolean) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    var templates by remember { mutableStateOf(EvaluationTemplatesCatalog.templates) }
    var selectedTemplate by remember { mutableStateOf<CourseEvaluationTemplate?>(EvaluationTemplatesCatalog.templates.firstOrNull()) }
    var applyToAllCourses by remember { mutableStateOf(showApplyToAllToggle) }
    var isLoadingRemote by remember { mutableStateOf(true) }

    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        val service = PonderacionApiService()
        service.getRemoteTemplates().onSuccess { fetched ->
            if (fetched.isNotEmpty()) {
                templates = fetched
                if (selectedTemplate == null || !fetched.any { it.id == selectedTemplate?.id }) {
                    selectedTemplate = fetched.firstOrNull()
                }
            }
        }
        isLoadingRemote = false
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            title = stringResource(Res.string.import_template_title),
            summary = stringResource(Res.string.import_template_summary),
            onDismissRequest = onDismiss,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(templates) { template ->
                        val isSelected = selectedTemplate?.id == template.id
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { selectedTemplate = template },
                            colors = MiuixCardDefaults.defaultColors(
                                color = if (isSelected) MiuixTheme.colorScheme.secondaryContainer
                                else MiuixTheme.colorScheme.surfaceContainerHigh,
                            ),
                            insideMargin = PaddingValues(14.dp),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MiuixTheme.colorScheme.surfaceContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            MiuixIcon(
                                                imageVector = Icons.Outlined.AccountBalance,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        MiuixText(
                                            text = template.institution,
                                            style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Bold),
                                            color = MiuixTheme.colorScheme.primary,
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MiuixTheme.colorScheme.surfaceContainer),
                                        ) {
                                            MiuixText(
                                                text = stringResource(Res.string.import_template_weeks_format, template.totalWeeks),
                                                style = MiuixTheme.textStyles.footnote2,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                        if (isSelected) {
                                            MiuixIcon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = stringResource(Res.string.selected),
                                                tint = MiuixTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                MiuixText(
                                    text = template.name,
                                    style = MiuixTheme.textStyles.subtitle.copy(fontWeight = FontWeight.Bold),
                                    color = MiuixTheme.colorScheme.onSurface,
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                MiuixText(
                                    text = template.description,
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    template.groups.forEach { g ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MiuixTheme.colorScheme.surfaceContainer),
                                        ) {
                                            MiuixText(
                                                text = stringResource(
                                                    Res.string.import_template_group_weight_format,
                                                    g.name,
                                                    g.weightPercentage.toInt(),
                                                ),
                                                style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Medium),
                                                color = MiuixTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showApplyToAllToggle) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { applyToAllCourses = !applyToAllCourses }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                MiuixText(
                                    text = stringResource(Res.string.import_template_apply_all_title),
                                    style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.SemiBold),
                                )
                                MiuixText(
                                    text = stringResource(Res.string.import_template_apply_all_desc),
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            MiuixSwitch(
                                checked = applyToAllCourses,
                                onCheckedChange = null,
                            )
                        }
                    }
                }
            }
            GlassDialogActions {
                GlassDialogButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    MiuixText(
                        text = stringResource(Res.string.cancel),
                        style = MiuixTheme.textStyles.button,
                    )
                }
                GlassDialogButton(
                    onClick = {
                        selectedTemplate?.let { onApplyTemplate(it, applyToAllCourses) }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedTemplate != null,
                    role = GlassDialogButtonRole.Primary,
                ) {
                    MiuixText(
                        text = if (applyToAllCourses) {
                            stringResource(Res.string.import_template_apply_all_action)
                        } else {
                            stringResource(Res.string.import_template_apply_action)
                        },
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
        }
    } else if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.import_template_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(Res.string.import_template_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(templates) { template ->
                            val isSelected = selectedTemplate?.id == template.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedTemplate = template },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.AccountBalance,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = template.institution,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.import_template_weeks_format, template.totalWeeks),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = stringResource(Res.string.selected),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Resumen de grupos con FlowRow flexible para evitar deformación
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        template.groups.forEach { g ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = stringResource(
                                                        Res.string.import_template_group_weight_format,
                                                        g.name,
                                                        g.weightPercentage.toInt(),
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showApplyToAllToggle) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { applyToAllCourses = !applyToAllCourses }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.import_template_apply_all_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(Res.string.import_template_apply_all_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = applyToAllCourses,
                                    onCheckedChange = { applyToAllCourses = it }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTemplate?.let { onApplyTemplate(it, applyToAllCourses) }
                    },
                    enabled = selectedTemplate != null
                ) {
                    Text(
                        if (applyToAllCourses) {
                            stringResource(Res.string.import_template_apply_all_action)
                        } else {
                            stringResource(Res.string.import_template_apply_action)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
