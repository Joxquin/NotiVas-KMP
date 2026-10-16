package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusCard
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixBlue = Color(0xFF3482FF)
private val MiuixCardBg = Color(0xFF16171A)
private val MiuixSurfaceDark = Color(0xFF1E293B)

private enum class AssignmentDetailTab {
    DRAFT,
    INSTRUCTIONS,
    OPTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailSheet(
    assignmentUi: AssignmentUiModel?,
    viewModel: me.joxquin.notivas.ui.dashboard.DashboardViewModel? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (assignmentUi == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current

    val draftInfo: me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo? =
        viewModel?.currentAssignmentDraft?.collectAsState()?.value

    var showTemplatePicker by remember { mutableStateOf(false) }
    val courseTemplates by (viewModel?.courseTemplates?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val isLoadingTemplates by (viewModel?.isLoadingTemplates?.collectAsState() ?: remember { mutableStateOf(false) })

    val currentAssignmentModuleName by (viewModel?.currentAssignmentModuleName?.collectAsState() ?: remember { mutableStateOf(null) })

    if (showTemplatePicker) {
        me.joxquin.notivas.ui.dashboard.components.draft.ModuleTemplatePickerSheet(
            courseName = assignmentUi.courseName,
            templates = courseTemplates,
            isLoading = isLoadingTemplates,
            currentModuleName = currentAssignmentModuleName,
            themeStyle = themeStyle,
            onSelectTemplate = { selectedCandidate ->
                showTemplatePicker = false
                viewModel?.startOrCloneDraft(
                    assignment = assignmentUi.assignment,
                    courseName = assignmentUi.courseName,
                    templateCandidate = selectedCandidate
                )
            },
            onDismissRequest = { showTemplatePicker = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (themeStyle == AppThemeStyle.MIUIX) Color(0xFF121316) else MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        var selectedTab by remember { mutableStateOf(AssignmentDetailTab.DRAFT) }

        val handleExploreTemplates: () -> Unit = {
            showTemplatePicker = true
            viewModel?.loadTemplatesForCourse(
                courseId = assignmentUi.assignment.courseId,
                assignmentId = assignmentUi.assignment.id,
                assignmentDescription = assignmentUi.assignment.description
            )
            Unit
        }

        if (themeStyle == AppThemeStyle.MIUIX) {
            MiuixAssignmentDetailContent(
                assignmentUi = assignmentUi,
                viewModel = viewModel,
                draftInfo = draftInfo,
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                onDismiss = onDismissRequest,
                onExploreTemplates = handleExploreTemplates,
                onOpenCanvas = {
                    assignmentUi.assignment.htmlUrl?.let { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                }
            )
        } else {
            MaterialAssignmentDetailContent(
                assignmentUi = assignmentUi,
                viewModel = viewModel,
                draftInfo = draftInfo,
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                onDismiss = onDismissRequest,
                onExploreTemplates = handleExploreTemplates,
                onOpenCanvas = {
                    assignmentUi.assignment.htmlUrl?.let { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                }
            )
        }
    }
}

@Composable
private fun MiuixAssignmentDetailContent(
    assignmentUi: AssignmentUiModel,
    viewModel: me.joxquin.notivas.ui.dashboard.DashboardViewModel? = null,
    draftInfo: me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo? = null,
    selectedTab: AssignmentDetailTab,
    onTabSelect: (AssignmentDetailTab) -> Unit,
    onDismiss: () -> Unit,
    onExploreTemplates: () -> Unit = {},
    onOpenCanvas: () -> Unit
) {
    val assignment = assignmentUi.assignment
    val courseName = assignmentUi.courseName
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MiuixText(
                    text = courseName.ifBlank { "Canvas LMS" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixBlue
                )
                Spacer(Modifier.height(2.dp))
                MiuixText(
                    text = assignment.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .squircleSurface(
                        color = Color.White.copy(alpha = 0.08f),
                        cornerRadius = 10.dp
                    )
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Metadata Pills Row (Points & Due date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pointsText = assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin calificar"
            Row(
                modifier = Modifier
                    .squircleSurface(
                        color = Color.White.copy(alpha = 0.05f),
                        cornerRadius = 8.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FactCheck,
                    contentDescription = null,
                    tint = MiuixBlue,
                    modifier = Modifier.size(13.dp)
                )
                MiuixText(
                    text = pointsText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            val dueText = assignment.dueAt?.take(10) ?: "Sin fecha"
            Row(
                modifier = Modifier
                    .squircleSurface(
                        color = Color.White.copy(alpha = 0.05f),
                        cornerRadius = 8.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Rule,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(13.dp)
                )
                MiuixText(
                    text = "${stringResource(Res.string.task_due_label)} $dueText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }

        // Miuix Segmented Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = Color.White.copy(alpha = 0.05f),
                    cornerRadius = 14.dp
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MiuixTabPill(
                title = stringResource(Res.string.assignment_detail_tab_draft),
                isSelected = selectedTab == AssignmentDetailTab.DRAFT,
                onClick = { onTabSelect(AssignmentDetailTab.DRAFT) },
                modifier = Modifier.weight(1f)
            )
            MiuixTabPill(
                title = stringResource(Res.string.assignment_detail_tab_instructions),
                isSelected = selectedTab == AssignmentDetailTab.INSTRUCTIONS,
                onClick = { onTabSelect(AssignmentDetailTab.INSTRUCTIONS) },
                modifier = Modifier.weight(1f)
            )
            MiuixTabPill(
                title = stringResource(Res.string.assignment_detail_tab_options),
                isSelected = selectedTab == AssignmentDetailTab.OPTIONS,
                onClick = { onTabSelect(AssignmentDetailTab.OPTIONS) },
                modifier = Modifier.weight(1f)
            )
        }

        // Tab Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "MiuixAssignmentDetailTab"
        ) { tab ->
            when (tab) {
                AssignmentDetailTab.DRAFT -> {
                    DraftStatusCard(
                        assignment = assignment,
                        courseName = courseName,
                        themeStyle = AppThemeStyle.MIUIX,
                        draftInfo = draftInfo,
                        onStartDraft = { isTemplate, templateName ->
                            viewModel?.startOrCloneDraft(
                                assignment = assignment,
                                courseName = courseName,
                                templateName = if (isTemplate) templateName else null
                            )
                        },
                        onExploreTemplates = onExploreTemplates,
                        onCancelDraft = {
                            viewModel?.cancelDraft(assignment.id)
                        },
                        onOpenDocs = { url ->
                            viewModel?.onOpenDocsRequested(url) ?: run {
                                try { uriHandler.openUri(url) } catch (_: Exception) {}
                            }
                        },
                        onSubmitToCanvas = {
                            val targetUrl = draftInfo?.driveUrl.orEmpty()
                            viewModel?.submitDraftToCanvas(
                                assignment = assignment,
                                driveUrl = targetUrl,
                                onSuccess = onOpenCanvas
                            ) ?: onOpenCanvas()
                        }
                    )
                }

                AssignmentDetailTab.INSTRUCTIONS -> {
                    MiuixInstructionsSection(assignment = assignment)
                }

                AssignmentDetailTab.OPTIONS -> {
                    MiuixSubmenuSection(
                        assignment = assignment,
                        onOpenCanvas = onOpenCanvas,
                        onDismiss = onDismiss
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun MiuixTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .squircleSurface(
                color = if (isSelected) MiuixBlue else Color.Transparent,
                cornerRadius = 10.dp
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        MiuixText(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

@Composable
private fun MiuixInstructionsSection(assignment: Assignment) {
    val cleanDesc = remember(assignment.description) {
        assignment.description?.replace(Regex("<[^>]*>"), "")?.trim()?.ifBlank { null }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(
                color = MiuixCardBg,
                cornerRadius = 18.dp
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MiuixBlue,
                modifier = Modifier.size(18.dp)
            )
            MiuixText(
                text = stringResource(Res.string.assignment_detail_tab_instructions),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        }

        MiuixText(
            text = cleanDesc ?: stringResource(Res.string.task_no_instructions),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            lineHeight = 18.sp
        )

        assignment.submissionTypes?.let { types ->
            if (types.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiuixText(
                        text = stringResource(Res.string.task_submission_types_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    MiuixText(
                        text = types.joinToString(", "),
                        fontSize = 11.sp,
                        color = MiuixBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixSubmenuSection(
    assignment: Assignment,
    onOpenCanvas: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(
                color = MiuixCardBg,
                cornerRadius = 18.dp
            )
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        MiuixSubmenuItem(
            icon = Icons.Default.OpenInBrowser,
            title = stringResource(Res.string.task_submenu_open_canvas),
            iconTint = MiuixBlue,
            onClick = {
                onOpenCanvas()
                onDismiss()
            }
        )
        MiuixSubmenuItem(
            icon = Icons.Default.FactCheck,
            title = stringResource(Res.string.task_submenu_view_rubric),
            iconTint = Color(0xFF83DA85),
            onClick = { onDismiss() }
        )
        MiuixSubmenuItem(
            icon = Icons.Default.CheckCircle,
            title = stringResource(Res.string.task_submenu_mark_completed),
            iconTint = Color(0xFF66BB6A),
            onClick = { onDismiss() }
        )
        MiuixSubmenuItem(
            icon = Icons.Default.Archive,
            title = stringResource(Res.string.task_submenu_archive),
            iconTint = Color(0xFFFFB74D),
            onClick = { onDismiss() }
        )
        MiuixSubmenuItem(
            icon = Icons.Default.Share,
            title = stringResource(Res.string.task_submenu_share),
            iconTint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            onClick = { onDismiss() }
        )
    }
}

@Composable
private fun MiuixSubmenuItem(
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
        Box(
            modifier = Modifier
                .size(34.dp)
                .squircleSurface(
                    color = iconTint.copy(alpha = 0.12f),
                    cornerRadius = 10.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        MiuixText(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MaterialAssignmentDetailContent(
    assignmentUi: AssignmentUiModel,
    viewModel: me.joxquin.notivas.ui.dashboard.DashboardViewModel? = null,
    draftInfo: me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo? = null,
    selectedTab: AssignmentDetailTab,
    onTabSelect: (AssignmentDetailTab) -> Unit,
    onDismiss: () -> Unit,
    onExploreTemplates: () -> Unit = {},
    onOpenCanvas: () -> Unit
) {
    val assignment = assignmentUi.assignment
    val courseName = assignmentUi.courseName
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName.ifBlank { "Canvas LMS" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = assignment.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Metadata Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pointsText = assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin calificar"
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
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

            val dueText = assignment.dueAt?.take(10) ?: "Sin fecha"
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
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

        // Material Segmented Tab Bar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MaterialTabPill(
                    title = stringResource(Res.string.assignment_detail_tab_draft),
                    isSelected = selectedTab == AssignmentDetailTab.DRAFT,
                    onClick = { onTabSelect(AssignmentDetailTab.DRAFT) },
                    modifier = Modifier.weight(1f)
                )
                MaterialTabPill(
                    title = stringResource(Res.string.assignment_detail_tab_instructions),
                    isSelected = selectedTab == AssignmentDetailTab.INSTRUCTIONS,
                    onClick = { onTabSelect(AssignmentDetailTab.INSTRUCTIONS) },
                    modifier = Modifier.weight(1f)
                )
                MaterialTabPill(
                    title = stringResource(Res.string.assignment_detail_tab_options),
                    isSelected = selectedTab == AssignmentDetailTab.OPTIONS,
                    onClick = { onTabSelect(AssignmentDetailTab.OPTIONS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "MaterialAssignmentDetailTab"
        ) { tab ->
            when (tab) {
                AssignmentDetailTab.DRAFT -> {
                    DraftStatusCard(
                        assignment = assignment,
                        courseName = courseName,
                        themeStyle = AppThemeStyle.MATERIAL,
                        draftInfo = draftInfo,
                        onStartDraft = { isTemplate, templateName ->
                            viewModel?.startOrCloneDraft(
                                assignment = assignment,
                                courseName = courseName,
                                templateName = if (isTemplate) templateName else null
                            )
                        },
                        onExploreTemplates = onExploreTemplates,
                        onCancelDraft = {
                            viewModel?.cancelDraft(assignment.id)
                        },
                        onOpenDocs = { url ->
                            viewModel?.onOpenDocsRequested(url) ?: run {
                                try { uriHandler.openUri(url) } catch (_: Exception) {}
                            }
                        },
                        onSubmitToCanvas = {
                            val targetUrl = draftInfo?.driveUrl.orEmpty()
                            viewModel?.submitDraftToCanvas(
                                assignment = assignment,
                                driveUrl = targetUrl,
                                onSuccess = onOpenCanvas
                            ) ?: onOpenCanvas()
                        }
                    )
                }

                AssignmentDetailTab.INSTRUCTIONS -> {
                    MaterialInstructionsSection(assignment = assignment)
                }

                AssignmentDetailTab.OPTIONS -> {
                    MaterialSubmenuSection(
                        assignment = assignment,
                        onOpenCanvas = onOpenCanvas,
                        onDismiss = onDismiss
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun MaterialTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MaterialInstructionsSection(assignment: Assignment) {
    val cleanDesc = remember(assignment.description) {
        assignment.description?.replace(Regex("<[^>]*>"), "")?.trim()?.ifBlank { null }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(Res.string.assignment_detail_tab_instructions),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = cleanDesc ?: stringResource(Res.string.task_no_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            assignment.submissionTypes?.let { types ->
                if (types.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.task_submission_types_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = types.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialSubmenuSection(
    assignment: Assignment,
    onOpenCanvas: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MaterialSubmenuItem(
                icon = Icons.Default.OpenInBrowser,
                title = stringResource(Res.string.task_submenu_open_canvas),
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = {
                    onOpenCanvas()
                    onDismiss()
                }
            )
            MaterialSubmenuItem(
                icon = Icons.Default.FactCheck,
                title = stringResource(Res.string.task_submenu_view_rubric),
                iconTint = Color(0xFF66BB6A),
                onClick = { onDismiss() }
            )
            MaterialSubmenuItem(
                icon = Icons.Default.CheckCircle,
                title = stringResource(Res.string.task_submenu_mark_completed),
                iconTint = Color(0xFF66BB6A),
                onClick = { onDismiss() }
            )
            MaterialSubmenuItem(
                icon = Icons.Default.Archive,
                title = stringResource(Res.string.task_submenu_archive),
                iconTint = Color(0xFFFFB74D),
                onClick = { onDismiss() }
            )
            MaterialSubmenuItem(
                icon = Icons.Default.Share,
                title = stringResource(Res.string.task_submenu_share),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onDismiss() }
            )
        }
    }
}

@Composable
private fun MaterialSubmenuItem(
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
