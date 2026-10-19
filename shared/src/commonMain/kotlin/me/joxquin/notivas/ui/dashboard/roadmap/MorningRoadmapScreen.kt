package me.joxquin.notivas.ui.dashboard.roadmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.MorningBriefingData
import me.joxquin.notivas.data.model.TimelineItem
import me.joxquin.notivas.data.model.TimelineItemType
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.back
import me.joxquin.notivas.shared.resources.daily_roadmap_action_ask_ai
import me.joxquin.notivas.shared.resources.daily_roadmap_action_drive
import me.joxquin.notivas.shared.resources.daily_roadmap_action_explain_rubric
import me.joxquin.notivas.shared.resources.daily_roadmap_action_snooze
import me.joxquin.notivas.shared.resources.daily_roadmap_action_undo
import me.joxquin.notivas.shared.resources.daily_roadmap_at_risk_warning
import me.joxquin.notivas.shared.resources.daily_roadmap_executive_summary_title
import me.joxquin.notivas.shared.resources.daily_roadmap_impact_title
import me.joxquin.notivas.shared.resources.daily_roadmap_points_stake_format
import me.joxquin.notivas.shared.resources.daily_roadmap_timeline_title
import me.joxquin.notivas.shared.resources.daily_roadmap_title
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningRoadmapScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onNavigateToCopilotWithPrompt: (String) -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val briefing by viewModel.morningBriefing.collectAsState()
    val scope = rememberCoroutineScope()

    var showUndoSnackbar by remember { mutableStateOf(false) }
    var snoozedAssignmentTitle by remember { mutableStateOf<String?>(null) }

    val handleSnooze: (String) -> Unit = { title ->
        snoozedAssignmentTitle = title
        showUndoSnackbar = true
        scope.launch {
            delay(5000)
            showUndoSnackbar = false
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = statusBarTop + 68.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                briefing?.let { data ->
                    // 1. Resumen Ejecutivo del Asistente (IA / Heurística Local)
                    item {
                        SmallTitle(text = stringResource(Res.string.daily_roadmap_executive_summary_title))
                        MiuixCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.None
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (data.isAiGenerated) "Generado con Copiloto IA" else "Síntesis Estratégica Local",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MiuixTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = data.executiveSummary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MiuixTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }

                    // 2. Desglose de Impacto Académico (Módulo Predictivo)
                    item {
                        SmallTitle(text = stringResource(Res.string.daily_roadmap_impact_title))
                        MiuixCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.None
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(
                                        Res.string.daily_roadmap_points_stake_format,
                                        data.totalPointsAtStake.roundToInt().toString()
                                    ),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                if (data.atRiskCourseNames.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MiuixTheme.colorScheme.errorContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${stringResource(Res.string.daily_roadmap_at_risk_warning)}: ${data.atRiskCourseNames.joinToString()}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MiuixTheme.colorScheme.onErrorContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Cronograma y Bloques del Día
                    item {
                        SmallTitle(text = stringResource(Res.string.daily_roadmap_timeline_title))
                    }

                    if (data.timelineItems.isEmpty()) {
                        item {
                            MiuixCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                insideMargin = PaddingValues(16.dp),
                                pressFeedbackType = PressFeedbackType.None
                            ) {
                                Text(
                                    text = "No hay compromisos con hora límite para hoy.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                )
                            }
                        }
                    } else {
                        items(data.timelineItems, key = { it.id }) { item ->
                            RoadmapTimelineMiuixCard(
                                item = item,
                                onOpenDraft = {
                                    val assignmentUi = viewModel.allAssignmentsUi.value.find { it.assignment.id == item.assignmentId }
                                    if (assignmentUi != null) {
                                        viewModel.openDraft(assignmentUi)
                                    }
                                },
                                onSnooze = { handleSnooze(item.title) }
                            )
                        }
                    }

                    // 4. Acciones Rápidas (One-Tap Actions)
                    item {
                        SmallTitle(text = "Acciones Rápidas")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.heroTask?.let { heroUi ->
                                TextButton(
                                    text = stringResource(Res.string.daily_roadmap_action_drive),
                                    onClick = {
                                        viewModel.startOrCloneDraft(
                                            assignment = heroUi.assignment,
                                            courseName = heroUi.courseName,
                                            templateCandidate = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = MiuixButtonDefaults.textButtonColorsPrimary()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    text = "¿Cómo divido mi tiempo?",
                                    onClick = {
                                        onNavigateToCopilotWithPrompt("¿Cómo me sugieres dividir mi tiempo de estudio para las tareas de hoy?")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    text = "Explicar rúbrica",
                                    onClick = {
                                        val heroTitle = data.heroTask?.assignment?.name ?: "mi tarea de hoy"
                                        onNavigateToCopilotWithPrompt("Explícame los criterios clave y rúbrica para '$heroTitle'")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Snackbar de deshacer
            AnimatedVisibility(
                visible = showUndoSnackbar,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MiuixTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Alerta silenciada: ${snoozedAssignmentTitle ?: ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            text = stringResource(Res.string.daily_roadmap_action_undo),
                            onClick = { showUndoSnackbar = false },
                            colors = MiuixButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
        }
        return
    }

    // Material 3 Expressive Implementation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.daily_roadmap_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                briefing?.let { data ->
                    // 1. Resumen Ejecutivo
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
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
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (data.isAiGenerated) "Generado con Copiloto IA" else "Síntesis Estratégica Local",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = data.executiveSummary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }

                    // 2. Desglose de Impacto Académico
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.daily_roadmap_points_stake_format,
                                        data.totalPointsAtStake.roundToInt().toString()
                                    ),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (data.atRiskCourseNames.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${stringResource(Res.string.daily_roadmap_at_risk_warning)}: ${data.atRiskCourseNames.joinToString()}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Cronograma Timeline
                    item {
                        Text(
                            text = stringResource(Res.string.daily_roadmap_timeline_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    if (data.timelineItems.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No hay compromisos con hora límite para hoy.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(data.timelineItems, key = { it.id }) { item ->
                            RoadmapTimelineMaterialCard(
                                item = item,
                                onOpenDraft = {
                                    val assignmentUi = viewModel.allAssignmentsUi.value.find { it.assignment.id == item.assignmentId }
                                    if (assignmentUi != null) {
                                        viewModel.openDraft(assignmentUi)
                                    }
                                },
                                onSnooze = { handleSnooze(item.title) }
                            )
                        }
                    }

                    // 4. Acciones Rápidas
                    item {
                        Text(
                            text = "Acciones Rápidas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.heroTask?.let { heroUi ->
                                Button(
                                    onClick = {
                                        viewModel.startOrCloneDraft(
                                            assignment = heroUi.assignment,
                                            courseName = heroUi.courseName,
                                            templateCandidate = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.daily_roadmap_action_drive))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onNavigateToCopilotWithPrompt("¿Cómo me sugieres dividir mi tiempo de estudio para las tareas de hoy?")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    Text("Dividir tiempo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Button(
                                    onClick = {
                                        val heroTitle = data.heroTask?.assignment?.name ?: "mi tarea de hoy"
                                        onNavigateToCopilotWithPrompt("Explícame los criterios clave y rúbrica para '$heroTitle'")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    Text("Ver rúbrica", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showUndoSnackbar,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Alerta silenciada: ${snoozedAssignmentTitle ?: ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showUndoSnackbar = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(Res.string.daily_roadmap_action_undo))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapTimelineMiuixCard(
    item: TimelineItem,
    onOpenDraft: () -> Unit,
    onSnooze: () -> Unit
) {
    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
        colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(14.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onOpenDraft
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MiuixTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = item.timeLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusText = if (item.isSubmitted) "Entregada" else "Pendiente"
                    val statusColor = if (item.isSubmitted) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Medium)
                    )
                    if (item.isGroup) {
                        Text(
                            text = "• En equipo",
                            style = MaterialTheme.typography.labelSmall.copy(color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        )
                    }
                }
            }

            IconButton(onClick = onSnooze) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = stringResource(Res.string.daily_roadmap_action_snooze),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RoadmapTimelineMaterialCard(
    item: TimelineItem,
    onOpenDraft: () -> Unit,
    onSnooze: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onOpenDraft,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = item.timeLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusText = if (item.isSubmitted) "Entregada" else "Pendiente"
                    val statusColor = if (item.isSubmitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Medium)
                    )
                    if (item.isGroup) {
                        Text(
                            text = "• En equipo",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            IconButton(onClick = onSnooze) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = stringResource(Res.string.daily_roadmap_action_snooze),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
