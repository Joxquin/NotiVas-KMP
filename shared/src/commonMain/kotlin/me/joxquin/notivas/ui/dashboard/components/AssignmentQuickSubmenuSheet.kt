package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close as MiuixClose
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentQuickSubmenuSheet(
    assignmentUi: AssignmentUiModel?,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onDismissRequest: () -> Unit,
    onOpenDraftScreen: (AssignmentUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (assignmentUi == null) return

    val uriHandler = LocalUriHandler.current

    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowBottomSheet(
            title = stringResource(Res.string.task_actions_title),
            show = true,
            onDismissRequest = onDismissRequest,
            startAction = {
                val dismissState = LocalDismissState.current
                top.yukonga.miuix.kmp.basic.IconButton(
                    onClick = { dismissState?.invoke() }
                ) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.MiuixClose,
                        contentDescription = stringResource(Res.string.close),
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            },
            modifier = modifier
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollEndHaptic()
                    .overScrollVertical()
            ) {
                // Sección 1: Cabecera con Información de la Tarea
                item {
                    SmallTitle(
                        text = stringResource(Res.string.assignment_detail_title),
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = assignmentUi.courseName.ifBlank { "Canvas LMS" },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MiuixTheme.colorScheme.primary
                            )
                            Text(
                                text = assignmentUi.assignment.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MiuixTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pointsText = assignmentUi.assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin calificar"
                                Surface(
                                    shape = CircleShape,
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FactCheck,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = pointsText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }

                                val dueText = assignmentUi.assignment.dueAt?.take(10) ?: "Sin fecha"
                                Surface(
                                    shape = CircleShape,
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Rule,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB74D),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${stringResource(Res.string.task_due_label)} $dueText",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sección 2: Acción Destacada (Google Drive)
                item {
                    SmallTitle(
                        text = stringResource(Res.string.task_submenu_manage_draft),
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismissRequest()
                                    onOpenDraftScreen(assignmentUi)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.task_submenu_manage_draft),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MiuixTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = stringResource(Res.string.task_submenu_manage_draft_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp
                                    ),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Sección 3: Acciones Adicionales
                item {
                    SmallTitle(
                        text = stringResource(Res.string.task_actions_title),
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column {
                            MiuixSubmenuRow(
                                icon = Icons.Default.OpenInBrowser,
                                title = stringResource(Res.string.task_submenu_open_canvas),
                                iconTint = MiuixTheme.colorScheme.primary,
                                onClick = {
                                    assignmentUi.assignment.htmlUrl?.let { url ->
                                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                                    }
                                    onDismissRequest()
                                }
                            )
                            MiuixSubmenuRow(
                                icon = Icons.Default.FactCheck,
                                title = stringResource(Res.string.task_submenu_view_rubric),
                                iconTint = Color(0xFF83DA85),
                                onClick = onDismissRequest
                            )
                            MiuixSubmenuRow(
                                icon = Icons.Default.CheckCircle,
                                title = stringResource(Res.string.task_submenu_mark_completed),
                                iconTint = Color(0xFF66BB6A),
                                onClick = onDismissRequest
                            )
                            MiuixSubmenuRow(
                                icon = Icons.Default.Archive,
                                title = stringResource(Res.string.task_submenu_archive),
                                iconTint = Color(0xFFFFB74D),
                                onClick = onDismissRequest
                            )
                            MiuixSubmenuRow(
                                icon = Icons.Default.Share,
                                title = stringResource(Res.string.task_submenu_share),
                                iconTint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                onClick = onDismissRequest
                            )
                        }
                    }

                    Spacer(
                        Modifier.padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() + 16.dp
                        )
                    )
                }
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = null,
            modifier = modifier
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar con título, subtítulo e icono de cerrar
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
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(Res.string.task_actions_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = assignmentUi.courseName.ifBlank { "Canvas LMS" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // Task Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = assignmentUi.assignment.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pointsText = assignmentUi.assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin calificar"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FactCheck,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = pointsText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val dueText = assignmentUi.assignment.dueAt?.take(10) ?: "Sin fecha"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Rule,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB74D),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${stringResource(Res.string.task_due_label)} $dueText",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Acción Prioritaria: Gestionar Borrador en Drive
                item {
                    Text(
                        text = stringResource(Res.string.task_submenu_manage_draft),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onDismissRequest()
                                onOpenDraftScreen(assignmentUi)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.task_submenu_manage_draft),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = stringResource(Res.string.task_submenu_manage_draft_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Acciones Secundarias
                item {
                    Text(
                        text = stringResource(Res.string.task_actions_title),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            MaterialSubmenuRow(
                                icon = Icons.Default.OpenInBrowser,
                                title = stringResource(Res.string.task_submenu_open_canvas),
                                iconTint = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    assignmentUi.assignment.htmlUrl?.let { url ->
                                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                                    }
                                    onDismissRequest()
                                }
                            )
                            MaterialSubmenuRow(
                                icon = Icons.Default.FactCheck,
                                title = stringResource(Res.string.task_submenu_view_rubric),
                                iconTint = Color(0xFF66BB6A),
                                onClick = onDismissRequest
                            )
                            MaterialSubmenuRow(
                                icon = Icons.Default.CheckCircle,
                                title = stringResource(Res.string.task_submenu_mark_completed),
                                iconTint = Color(0xFF66BB6A),
                                onClick = onDismissRequest
                            )
                            MaterialSubmenuRow(
                                icon = Icons.Default.Archive,
                                title = stringResource(Res.string.task_submenu_archive),
                                iconTint = Color(0xFFFFB74D),
                                onClick = onDismissRequest
                            )
                            MaterialSubmenuRow(
                                icon = Icons.Default.Share,
                                title = stringResource(Res.string.task_submenu_share),
                                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = onDismissRequest
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MiuixSubmenuRow(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MaterialSubmenuRow(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
