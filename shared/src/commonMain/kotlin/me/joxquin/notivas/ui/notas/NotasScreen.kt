package me.joxquin.notivas.ui.notas

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.notas.components.CopilotGroupAnalysisSheet
import me.joxquin.notivas.ui.notas.components.CreateGroupBottomSheet
import me.joxquin.notivas.ui.notas.components.EmptyGruposState
import me.joxquin.notivas.ui.notas.components.GroupCreationChoiceBottomSheet
import me.joxquin.notivas.ui.notas.components.HeroSummaryCard
import me.joxquin.notivas.ui.notas.components.ImportTemplateDialog
import me.joxquin.notivas.ui.notas.components.NotasHeaderSection
import me.joxquin.notivas.ui.notas.components.SimulationGroupSummaryCard
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotasViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    onOpenGroupDetail: (groupId: Long, courseId: Long) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = false, onBack = onBack)

    val uiState by viewModel.uiState.collectAsState()
    val activeCourse = uiState.selectedCourse

    var showCreationChoiceSheet by remember { mutableStateOf(false) }
    var showManualCreateBottomSheet by remember { mutableStateOf(false) }
    var showGitHubImportDialog by remember { mutableStateOf(false) }
    var showCopilotAnalysisSheet by remember { mutableStateOf(false) }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    // Modal de selección de método de creación
    if (showCreationChoiceSheet) {
        GroupCreationChoiceBottomSheet(
            onDismiss = { showCreationChoiceSheet = false },
            onSelectManual = { showManualCreateBottomSheet = true },
            onSelectGitHub = { showGitHubImportDialog = true },
            onSelectCopilot = { showCopilotAnalysisSheet = true },
            themeStyle = themeStyle
        )
    }

    // Creación manual
    if (showManualCreateBottomSheet) {
        CreateGroupBottomSheet(
            currentTotalWeight = uiState.totalConfiguredWeight,
            themeStyle = themeStyle,
            onDismiss = { showManualCreateBottomSheet = false },
            onSave = { name, weight, targetAssessments, dropLowest, minToDrop ->
                viewModel.createGroupWithItems(name, weight, targetAssessments, dropLowest, minToDrop)
                showManualCreateBottomSheet = false
            }
        )
    }

    // Importación de plantillas desde GitHub
    if (showGitHubImportDialog) {
        ImportTemplateDialog(
            onDismiss = { showGitHubImportDialog = false },
            showApplyToAllToggle = true,
            onApplyTemplate = { template, applyToAll ->
                if (applyToAll) {
                    viewModel.applyTemplateToAllCourses(template)
                } else {
                    viewModel.applyTemplate(template)
                }
                showGitHubImportDialog = false
            }
        )
    }

    // Generación con IA Copilot
    if (showCopilotAnalysisSheet && activeCourse != null) {
        CopilotGroupAnalysisSheet(
            course = activeCourse,
            viewModel = viewModel,
            themeStyle = themeStyle,
            onDismiss = { showCopilotAnalysisSheet = false },
            onGroupsApplied = { showCopilotAnalysisSheet = false }
        )
    }

    val totalWeight = uiState.totalConfiguredWeight.roundToInt()

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()

        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = stringResource(Res.string.grades_progreso_title),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            MiuixGlassyActionsContainer(backdrop = backdrop) {
                                BackNavigationIcon(onClick = onBack)
                            }
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = innerPadding,
                extraTop = 8.dp,
                extraStart = 0.dp,
                extraEnd = 0.dp,
                extraBottom = 24.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.surface)
                    .miuixBackdropModifier(backdrop)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixPageScrollModifiers(scrollBehavior),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ─── 1. Selector de Curso Dropdown ──────────────────────────────
                    item {
                        NotasHeaderSection(
                            courses = uiState.courses,
                            selectedCourse = uiState.selectedCourse,
                            onCourseSelect = { viewModel.selectCourse(it) },
                            themeStyle = themeStyle
                        )
                    }

                    // ─── 2. Resumen Hero Académico ──────────────────────────────────
                    item {
                        HeroSummaryCard(
                            currentAverage = uiState.currentAverage,
                            progressRatio = uiState.evaluatedProgressRatio,
                            riskLevel = uiState.riskLevel,
                            projectedGrade = uiState.projectedFinalGrade,
                            themeStyle = themeStyle
                        )
                    }

                    // ─── 3. Header de Grupos de Evaluación ──────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                SmallTitle(text = stringResource(Res.string.groups_section_title))
                                Text(
                                    text = stringResource(Res.string.groups_total_weight_format, totalWeight),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (totalWeight == 100) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                            if (uiState.simulationGroups.isNotEmpty()) {
                                TextButton(
                                    text = stringResource(Res.string.groups_add_group_btn),
                                    onClick = { showCreationChoiceSheet = true },
                                    colors = ButtonDefaults.textButtonColorsPrimary()
                                )
                            }
                        }
                    }

                    // ─── 4. Lista de Grupos de Evaluación ───────────────────────────
                    if (uiState.simulationGroups.isEmpty()) {
                        item {
                            EmptyGruposState(
                                onAddGroup = { showCreationChoiceSheet = true },
                                themeStyle = themeStyle
                            )
                        }
                    } else {
                        items(uiState.simulationGroups, key = { it.group.id }) { groupModel ->
                            val currentCourseId = activeCourse?.id ?: groupModel.group.courseId
                            SimulationGroupSummaryCard(
                                groupModel = groupModel,
                                onClick = { onOpenGroupDetail(groupModel.group.id, currentCourseId) },
                                themeStyle = themeStyle
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }

                VerticalScrollBar(
                    adapter = rememberScrollBarAdapter(lazyListState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp,
                            bottom = 24.dp
                        )
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
            label = "notasMaterialTopBarColor"
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Progreso",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.selectedCourse?.name ?: "Notas y simulador",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarContainerColor,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ─── 1. Selector de Curso Dropdown ──────────────────────────────
                item {
                    NotasHeaderSection(
                        courses = uiState.courses,
                        selectedCourse = uiState.selectedCourse,
                        onCourseSelect = { viewModel.selectCourse(it) },
                        themeStyle = themeStyle
                    )
                }

                // ─── 2. Resumen Hero Académico ──────────────────────────────────
                item {
                    HeroSummaryCard(
                        currentAverage = uiState.currentAverage,
                        progressRatio = uiState.evaluatedProgressRatio,
                        riskLevel = uiState.riskLevel,
                        projectedGrade = uiState.projectedFinalGrade,
                        themeStyle = themeStyle
                    )
                }

                // ─── 3. Header de Grupos de Evaluación ──────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Res.string.groups_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(Res.string.groups_total_weight_format, totalWeight),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (totalWeight == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.simulationGroups.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { showCreationChoiceSheet = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(Res.string.groups_add_group_btn))
                            }
                        }
                    }
                }

                // ─── 4. Lista de Grupos de Evaluación ───────────────────────────
                if (uiState.simulationGroups.isEmpty()) {
                    item {
                        EmptyGruposState(
                            onAddGroup = { showCreationChoiceSheet = true },
                            themeStyle = themeStyle
                        )
                    }
                } else {
                    items(uiState.simulationGroups, key = { it.group.id }) { groupModel ->
                        val currentCourseId = activeCourse?.id ?: groupModel.group.courseId
                        SimulationGroupSummaryCard(
                            groupModel = groupModel,
                            onClick = { onOpenGroupDetail(groupModel.group.id, currentCourseId) },
                            themeStyle = themeStyle
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
