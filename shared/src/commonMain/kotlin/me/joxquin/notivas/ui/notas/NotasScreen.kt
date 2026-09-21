package me.joxquin.notivas.ui.notas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.notas.components.AddGroupDialog
import me.joxquin.notivas.ui.notas.components.AddItemDialog
import me.joxquin.notivas.ui.notas.components.GruposPorcentajeCard
import me.joxquin.notivas.ui.notas.components.HeroSummaryCard
import me.joxquin.notivas.ui.notas.components.NotasHeaderSection
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotasViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val uiState by viewModel.uiState.collectAsState()
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var selectedGroupIdForNewItem by remember { mutableStateOf<Long?>(null) }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp,
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

                // ─── 2. Resumen Hero Académico & Evaluaciones Calificadas ───────
                item {
                    HeroSummaryCard(
                        currentAverage = uiState.currentAverage,
                        progressRatio = uiState.evaluatedProgressRatio,
                        riskLevel = uiState.riskLevel,
                        projectedGrade = uiState.projectedFinalGrade,
                        gradedEvaluations = uiState.evaluations.filter { it.isGraded },
                        themeStyle = themeStyle
                    )
                }

                // ─── 3. Grupos y Porcentaje (Simulador Ponderado) ───────────────
                item {
                    GruposPorcentajeCard(
                        simulationGroups = uiState.simulationGroups,
                        totalConfiguredWeight = uiState.totalConfiguredWeight,
                        onAddGroup = { showAddGroupDialog = true },
                        onAddItem = { groupId -> selectedGroupIdForNewItem = groupId },
                        onUpdateItemScore = { id, score -> viewModel.updateSimulationItemScore(id, score) },
                        onDeleteGroup = { viewModel.deleteSimulationGroup(it) },
                        onDeleteItem = { viewModel.deleteSimulationItem(it) },
                        themeStyle = themeStyle
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
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

                // ─── 2. Resumen Hero Académico & Evaluaciones Calificadas ───────
                item {
                    HeroSummaryCard(
                        currentAverage = uiState.currentAverage,
                        progressRatio = uiState.evaluatedProgressRatio,
                        riskLevel = uiState.riskLevel,
                        projectedGrade = uiState.projectedFinalGrade,
                        gradedEvaluations = uiState.evaluations.filter { it.isGraded },
                        themeStyle = themeStyle
                    )
                }

                // ─── 3. Grupos y Porcentaje (Simulador Ponderado) ───────────────
                item {
                    GruposPorcentajeCard(
                        simulationGroups = uiState.simulationGroups,
                        totalConfiguredWeight = uiState.totalConfiguredWeight,
                        onAddGroup = { showAddGroupDialog = true },
                        onAddItem = { groupId -> selectedGroupIdForNewItem = groupId },
                        onUpdateItemScore = { id, score -> viewModel.updateSimulationItemScore(id, score) },
                        onDeleteGroup = { viewModel.deleteSimulationGroup(it) },
                        onDeleteItem = { viewModel.deleteSimulationItem(it) },
                        themeStyle = themeStyle
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    if (showAddGroupDialog) {
        AddGroupDialog(
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { name, weight ->
                viewModel.addSimulationGroup(name, weight)
                showAddGroupDialog = false
            }
        )
    }

    selectedGroupIdForNewItem?.let { groupId ->
        AddItemDialog(
            groupId = groupId,
            onDismiss = { selectedGroupIdForNewItem = null },
            onConfirm = { gId, name, score, maxScore ->
                viewModel.addSimulationItem(gId, name, score, maxScore)
                selectedGroupIdForNewItem = null
            }
        )
    }
}
