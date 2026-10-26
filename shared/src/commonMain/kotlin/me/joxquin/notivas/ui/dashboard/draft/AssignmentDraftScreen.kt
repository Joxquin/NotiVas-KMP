package me.joxquin.notivas.ui.dashboard.draft

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.draft.components.AssignmentBottomActionBar
import me.joxquin.notivas.ui.dashboard.draft.components.AssignmentConsignaCard
import me.joxquin.notivas.ui.dashboard.draft.components.AssignmentDetailHeroCard
import me.joxquin.notivas.ui.dashboard.draft.components.AssignmentRubricCard
import me.joxquin.notivas.ui.dashboard.draft.components.AssignmentSubmissionCard
import me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusCard
import me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.shortenCourseSubtitle
import androidx.compose.foundation.layout.fillMaxHeight

/**
 * Pantalla dedicada de Detalle de Tarea y Gestión de Borrador Sincronizado.
 * Sigue la arquitectura unificada de Foros: se integra con el TopAppBar y FloatingNavigationBar del shell.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDraftScreen(
    assignmentUi: AssignmentUiModel,
    onBack: () -> Unit,
    viewModel: me.joxquin.notivas.ui.dashboard.DashboardViewModel? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val uriHandler = LocalUriHandler.current
    val assignment = assignmentUi.assignment
    val courseName = assignmentUi.courseName


    val onOpenCanvas: () -> Unit = {
        assignment.htmlUrl?.let { url ->
            try { uriHandler.openUri(url) } catch (_: Exception) {}
        }
    }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    val vmShowTemplatePicker by (viewModel?.showTemplatePicker?.collectAsState() ?: remember { mutableStateOf(false) })
    var localShowTemplatePicker by remember { mutableStateOf(false) }
    val showTemplatePicker = vmShowTemplatePicker || localShowTemplatePicker

    val courseTemplates by (viewModel?.courseTemplates?.collectAsState() ?: remember { mutableStateOf<List<me.joxquin.notivas.data.model.TemplateCandidate>>(emptyList()) })
    val isLoadingTemplates by (viewModel?.isLoadingTemplates?.collectAsState() ?: remember { mutableStateOf(false) })

    val currentAssignmentModuleName by (viewModel?.currentAssignmentModuleName?.collectAsState() ?: remember { mutableStateOf(null) })

    val draftInfo by (viewModel?.currentAssignmentDraft?.collectAsState() ?: remember { mutableStateOf<DraftSyncInfo?>(null) })

    val handleStartDraft: (Boolean, String?) -> Unit = { isTemplate, templateName ->
        viewModel?.startOrCloneDraft(
            assignment = assignment,
            courseName = courseName,
            templateName = if (isTemplate) templateName else null
        )
    }

    val handleCancelDraft: () -> Unit = {
        viewModel?.cancelDraft(assignment.id)
    }

    val handleOpenDocs: (String) -> Unit = { url ->
        viewModel?.onOpenDocsRequested(url) ?: run {
            try { uriHandler.openUri(url) } catch (_: Exception) {}
        }
    }

    val handleSubmitDraftToCanvas: () -> Unit = {
        val targetUrl = draftInfo?.driveUrl.orEmpty()
        viewModel?.submitDraftToCanvas(
            assignment = assignment,
            driveUrl = targetUrl,
            onSuccess = onOpenCanvas
        ) ?: onOpenCanvas()
    }

    val handleExploreTemplates: () -> Unit = {
        localShowTemplatePicker = true
        viewModel?.openTemplatePicker(
            courseId = assignment.courseId,
            assignmentId = assignment.id,
            assignmentDescription = assignment.description
        )
        Unit
    }

    val handleDismissTemplatePicker: () -> Unit = {
        localShowTemplatePicker = false
        viewModel?.closeTemplatePicker()
    }

    if (showTemplatePicker) {
        me.joxquin.notivas.ui.dashboard.components.draft.ModuleTemplatePickerSheet(
            courseName = courseName,
            templates = courseTemplates,
            isLoading = isLoadingTemplates,
            currentModuleName = currentAssignmentModuleName,
            themeStyle = themeStyle,
            onSelectTemplate = { selectedCandidate ->
                handleDismissTemplatePicker()
                viewModel?.startOrCloneDraft(
                    assignment = assignment,
                    courseName = courseName,
                    templateCandidate = selectedCandidate
                )
            },
            onDismissRequest = handleDismissTemplatePicker
        )
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val openCanvasLabel = stringResource(Res.string.task_menu_open_canvas)
        val exploreTemplatesLabel = stringResource(Res.string.task_menu_explore_templates)
        val copyCanvasLinkLabel = stringResource(Res.string.task_menu_copy_canvas_link)

        val dropdownEntries = remember(assignment, openCanvasLabel, exploreTemplatesLabel, copyCanvasLinkLabel) {
            listOf(
                DropdownEntry(
                    items = listOf(
                        DropdownItem(
                            text = openCanvasLabel,
                            onClick = { onOpenCanvas() }
                        ),
                        DropdownItem(
                            text = exploreTemplatesLabel,
                            onClick = handleExploreTemplates
                        ),
                        DropdownItem(
                            text = copyCanvasLinkLabel,
                            onClick = {
                                assignment.htmlUrl?.let { url ->
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(url))
                                }
                            }
                        )
                    )
                )
            )
        }

        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = stringResource(Res.string.assignment_detail_title),
                        subtitle = shortenCourseSubtitle(courseName.ifBlank { "Canvas LMS" }),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            BackNavigationIcon(onClick = onBack)
                        },
                        actions = {
                            WindowIconDropdownMenu(
                                entries = dropdownEntries,
                                collapseOnSelection = true
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Tune,
                                    contentDescription = stringResource(Res.string.assignment_detail_tab_options),
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(innerPadding)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .miuixBackdropModifier(backdrop)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixPageScrollModifiers(scrollBehavior),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Encabezado Hero del Detalle de Tarea
                    item(key = "task_hero") {
                        AssignmentDetailHeroCard(
                            assignment = assignment,
                            courseName = courseName,
                            themeStyle = themeStyle
                        )
                    }

                    // 2. Tarjeta Unificada de Entrega Directa y Borrador Google Drive
                    item(key = "task_submission") {
                        AssignmentSubmissionCard(
                            assignment = assignment,
                            courseName = courseName,
                            themeStyle = themeStyle,
                            viewModel = viewModel,
                            draftInfo = draftInfo,
                            onStartDraft = handleStartDraft,
                            onExploreTemplates = handleExploreTemplates,
                            onCancelDraft = handleCancelDraft,
                            onOpenDocs = handleOpenDocs,
                            onSubmitDraftToCanvas = handleSubmitDraftToCanvas,
                            onSubmitSuccess = {
                                viewModel?.refreshData()
                            }
                        )
                    }

                    // 3. Consigna de Canvas
                    item(key = "task_consigna") {
                        AssignmentConsignaCard(
                            assignment = assignment,
                            themeStyle = themeStyle
                        )
                    }

                    // 4. Criterios de Rúbrica Canvas (Cuadrícula 2x2)
                    item(key = "task_rubric") {
                        AssignmentRubricCard(
                            assignment = assignment,
                            themeStyle = themeStyle
                        )
                    }

                    // 5. Botones de Acción (Guardar para después)
                    item(key = "task_actions") {
                        AssignmentBottomActionBar(
                            onSaveForLater = onBack,
                            themeStyle = themeStyle
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                VerticalScrollBar(
                    adapter = rememberScrollBarAdapter(lazyListState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    trackPadding = contentPadding
                )
            }
        }
    } else {
        val topAppBarContainerColor by animateColorAsState(
            targetValue = if (isScrolled) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            animationSpec = tween(durationMillis = 250),
            label = "assignmentDetailMaterialTopBarColor"
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(Res.string.assignment_detail_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = shortenCourseSubtitle(courseName.ifBlank { "Canvas LMS" }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = stringResource(Res.string.assignment_detail_tab_options),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.task_menu_open_canvas)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onOpenCanvas()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.task_menu_explore_templates)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        handleExploreTemplates()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.task_menu_copy_canvas_link)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        assignment.htmlUrl?.let { url ->
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(url))
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarContainerColor,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = modifier
        ) { innerPadding ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Encabezado Hero del Detalle de Tarea
                item(key = "task_hero_material") {
                    AssignmentDetailHeroCard(
                        assignment = assignment,
                        courseName = courseName,
                        themeStyle = themeStyle
                    )
                }

                // 2. Tarjeta Unificada de Entrega Directa y Borrador Google Drive
                item(key = "task_submission_material") {
                    AssignmentSubmissionCard(
                        assignment = assignment,
                        courseName = courseName,
                        themeStyle = themeStyle,
                        viewModel = viewModel,
                        draftInfo = draftInfo,
                        onStartDraft = handleStartDraft,
                        onExploreTemplates = handleExploreTemplates,
                        onCancelDraft = handleCancelDraft,
                        onOpenDocs = handleOpenDocs,
                        onSubmitDraftToCanvas = handleSubmitDraftToCanvas,
                        onSubmitSuccess = {
                            viewModel?.refreshData()
                        }
                    )
                }

                // 3. Consigna de Canvas
                item(key = "task_consigna_material") {
                    AssignmentConsignaCard(
                        assignment = assignment,
                        themeStyle = themeStyle
                    )
                }

                // 4. Criterios de Rúbrica Canvas
                item(key = "task_rubric_material") {
                    AssignmentRubricCard(
                        assignment = assignment,
                        themeStyle = themeStyle
                    )
                }

                // 5. Botones de Acción (Guardar para después)
                item(key = "task_actions_material") {
                    AssignmentBottomActionBar(
                        onSaveForLater = onBack,
                        themeStyle = themeStyle
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
