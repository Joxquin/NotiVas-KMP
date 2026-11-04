package me.joxquin.notivas.ui.dashboard.performance.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.AcademicPerformanceStats
import me.joxquin.notivas.ui.dashboard.CourseStat
import me.joxquin.notivas.data.model.DayBlockInfo
import me.joxquin.notivas.data.model.DayTaskStatus
import me.joxquin.notivas.data.model.GradePerformanceLevel
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.performance_average_label
import me.joxquin.notivas.shared.resources.performance_average_scale
import me.joxquin.notivas.shared.resources.performance_completion_rate
import me.joxquin.notivas.shared.resources.performance_configure_formula
import me.joxquin.notivas.shared.resources.performance_courses_breakdown
import me.joxquin.notivas.shared.resources.performance_current_week
import me.joxquin.notivas.shared.resources.performance_day_deliverables
import me.joxquin.notivas.shared.resources.performance_heatmap_title
import me.joxquin.notivas.shared.resources.performance_highest_score
import me.joxquin.notivas.shared.resources.performance_legend_completed
import me.joxquin.notivas.shared.resources.performance_legend_empty
import me.joxquin.notivas.shared.resources.performance_legend_overdue
import me.joxquin.notivas.shared.resources.performance_legend_pending
import me.joxquin.notivas.shared.resources.performance_lowest_score
import me.joxquin.notivas.shared.resources.performance_monthly
import me.joxquin.notivas.shared.resources.performance_no_tasks_day
import me.joxquin.notivas.shared.resources.performance_score_prefix
import me.joxquin.notivas.shared.resources.performance_subtitle
import me.joxquin.notivas.shared.resources.performance_title
import me.joxquin.notivas.shared.resources.performance_view_simulation
import me.joxquin.notivas.shared.resources.performance_weekly
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateComponents
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * Vista nativa dedicada de Rendimiento Académico para Xiaomi HyperOS / Compose MIUIX UI.
 * Aplica componentes oficiales de MIUIX (top.yukonga.miuix.kmp), TopBar con desenfoque
 * progresivo, cápsula Liquid Glass en botones de acción y navegación, y cuadrículas temáticas.
 */
@Composable
fun AcademicPerformanceMiuixView(
    stats: AcademicPerformanceStats,
    courseStats: List<CourseStat>,
    weeklyBlocks: List<DayBlockInfo>,
    monthlyBlocks: List<List<DayBlockInfo>>,
    selectedDay: DateComponents?,
    selectedDayTasks: List<AssignmentUiModel>,
    weekOffset: Int,
    dynamicColorEnabled: Boolean,
    onBack: () -> Unit,
    onSelectDay: (DateComponents) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetWeek: () -> Unit,
    onNavigateToCourseProgreso: (Long) -> Unit,
    onNavigateToAssignmentDraft: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backdrop = rememberMiuixBlurBackdrop()
    val scrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()

    var isMonthlyView by remember { mutableStateOf(false) }

    val primaryColor = MiuixTheme.colorScheme.primary
    val completedColor = if (dynamicColorEnabled) primaryColor else Color(0xFF10B981)
    val overdueColor = Color(0xFFE53935)
    val pendingColor = MiuixTheme.colorScheme.surfaceContainerHigh
    val onSurfaceColor = MiuixTheme.colorScheme.onSurface
    val onVariantColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val outlineColor = MiuixTheme.colorScheme.outline

    Scaffold(
        topBar = {
            MiuixBlurredBar(
                backdrop = backdrop,
                scrollBehavior = scrollBehavior
            ) {
                MiuixPageTopAppBar(
                    title = stringResource(Res.string.performance_title),
                    subtitle = stringResource(Res.string.performance_subtitle),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        MiuixGlassyActionsContainer(backdrop = backdrop) {
                            BackNavigationIcon(onClick = onBack)
                        }
                    },
                    actions = {
                        if (weekOffset != 0 && !isMonthlyView) {
                            MiuixGlassyActionsContainer(backdrop = backdrop) {
                                IconButton(onClick = onResetWeek) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(Res.string.performance_current_week),
                                        tint = primaryColor
                                    )
                                }
                            }
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
            extraBottom = 28.dp
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
                contentPadding = contentPadding
            ) {
                // 1. Tarjeta Hero KPI de Rendimiento Global
                item(key = "miuix_hero_kpi") {
                    SmallTitle(text = stringResource(Res.string.performance_average_label).uppercase())
                    MiuixHeroKpiCard(
                        stats = stats,
                        primaryColor = primaryColor,
                        completedColor = completedColor,
                        overdueColor = overdueColor,
                        pendingColor = pendingColor,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor
                    )
                }

                // 2. Selector de Período & Heatmap (Semanal / Mensual)
                item(key = "miuix_heatmap_section") {
                    SmallTitle(text = stringResource(Res.string.performance_heatmap_title).uppercase())

                    val tabTitles = listOf(
                        stringResource(Res.string.performance_weekly),
                        stringResource(Res.string.performance_monthly)
                    )
                    TabRow(
                        tabs = tabTitles,
                        selectedTabIndex = if (isMonthlyView) 1 else 0,
                        onTabSelected = { isMonthlyView = (it == 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )

                    MiuixHeatmapCard(
                        isMonthlyView = isMonthlyView,
                        weeklyBlocks = weeklyBlocks,
                        monthlyBlocks = monthlyBlocks,
                        selectedDay = selectedDay,
                        weekOffset = weekOffset,
                        primaryColor = primaryColor,
                        completedColor = completedColor,
                        overdueColor = overdueColor,
                        pendingColor = pendingColor,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        outlineColor = outlineColor,
                        onSelectDay = onSelectDay,
                        onPreviousWeek = onPreviousWeek,
                        onNextWeek = onNextWeek,
                        onResetWeek = onResetWeek
                    )
                }

                // 3. Tareas Entregables del Día Seleccionado
                if (selectedDay != null) {
                    item(key = "miuix_selected_day_tasks") {
                        SmallTitle(text = stringResource(Res.string.performance_day_deliverables).uppercase())
                        MiuixSelectedDaySection(
                            selectedDay = selectedDay,
                            tasks = selectedDayTasks,
                            primaryColor = primaryColor,
                            completedColor = completedColor,
                            overdueColor = overdueColor,
                            pendingColor = pendingColor,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            onTaskClick = { task ->
                                onNavigateToAssignmentDraft(task.assignment.id, task.courseName)
                            }
                        )
                    }
                }

                // 4. Desglose Detallado por Curso
                item(key = "miuix_courses_breakdown_title") {
                    SmallTitle(text = stringResource(Res.string.performance_courses_breakdown).uppercase())
                }

                items(
                    items = courseStats,
                    key = { "miuix_course_${it.course.id}" }
                ) { stat ->
                    MiuixCourseItemCard(
                        courseStat = stat,
                        primaryColor = primaryColor,
                        completedColor = completedColor,
                        overdueColor = overdueColor,
                        pendingColor = pendingColor,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        outlineColor = outlineColor,
                        onCourseClick = { onNavigateToCourseProgreso(stat.course.id) },
                        onConfigureClick = { onNavigateToCourseProgreso(stat.course.id) }
                    )
                }
            }

            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * Tarjeta Hero KPI de Rendimiento en MIUIX.
 */
@Composable
private fun MiuixHeroKpiCard(
    stats: AcademicPerformanceStats,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Fila Principal: Promedio y Nivel Académico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.performance_average_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = onVariantColor
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (stats.overallAverage > 0.0) formatScoreMiuix(stats.overallAverage) else "--",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = stringResource(Res.string.performance_average_scale),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onVariantColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Badge de Nivel según escala académica Canvas
                val (levelText, levelColor) = when (stats.performanceLevel) {
                    GradePerformanceLevel.EXCELLENT -> "Sobresaliente" to completedColor
                    GradePerformanceLevel.PASSING -> "Aprobado" to primaryColor
                    GradePerformanceLevel.AT_RISK -> "En Riesgo" to overdueColor
                }

                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = levelColor.copy(alpha = 0.12f),
                            cornerRadius = 12.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = levelColor.copy(alpha = 0.35f),
                            cornerRadius = 12.dp
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = levelText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
            }

            // Tasa de Cumplimiento con ProgressIndicator de MIUIX
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.performance_completion_rate),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = onVariantColor
                    )
                    Text(
                        text = "${stats.completionRatePercentage.toInt()}% (${stats.completedAssignments}/${stats.totalAssignments})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                }
                LinearProgressIndicator(
                    progress = (stats.completionRatePercentage / 100f).coerceIn(0f, 1f),
                    colors = ProgressIndicatorDefaults.progressIndicatorColors(
                        foregroundColor = completedColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            // Mini Pills de Métricas Extremas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiuixMetricPill(
                    label = stringResource(Res.string.performance_highest_score),
                    value = stats.highestScore?.let { formatScoreMiuix(it) } ?: "--",
                    color = completedColor,
                    modifier = Modifier.weight(1f)
                )
                MiuixMetricPill(
                    label = stringResource(Res.string.performance_lowest_score),
                    value = stats.lowestScore?.let { formatScoreMiuix(it) } ?: "--",
                    color = overdueColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Píldora de métrica compacta para estadísticas extremas.
 */
@Composable
private fun MiuixMetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                cornerRadius = 10.dp
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Tarjeta de Heatmap en MIUIX (Semanal y Mensual).
 */
@Composable
private fun MiuixHeatmapCard(
    isMonthlyView: Boolean,
    weeklyBlocks: List<DayBlockInfo>,
    monthlyBlocks: List<List<DayBlockInfo>>,
    selectedDay: DateComponents?,
    weekOffset: Int,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    outlineColor: Color,
    onSelectDay: (DateComponents) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isMonthlyView) {
                // Navegación de Semanas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousWeek) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Semana anterior",
                            tint = onSurfaceColor
                        )
                    }

                    Text(
                        text = if (weekOffset == 0) stringResource(Res.string.performance_current_week)
                        else if (weekOffset > 0) "+$weekOffset semana"
                        else "$weekOffset semana",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor
                    )

                    IconButton(onClick = onNextWeek) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Semana siguiente",
                            tint = onSurfaceColor
                        )
                    }
                }

                // Fila de 7 Días Semanales (L M X J V S D)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyBlocks.forEach { block ->
                        val isSelected = selectedDay == block.date
                        MiuixDayBlockItem(
                            block = block,
                            isSelected = isSelected,
                            primaryColor = primaryColor,
                            completedColor = completedColor,
                            overdueColor = overdueColor,
                            pendingColor = pendingColor,
                            outlineColor = outlineColor,
                            onSurfaceColor = onSurfaceColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDay(block.date) }
                        )
                    }
                }
            } else {
                // Matriz Mensual de 4 semanas x 7 días = 28 bloques
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Encabezados de días
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { d ->
                            Text(
                                text = d,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = onVariantColor,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    monthlyBlocks.forEach { weekChunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekChunk.forEach { block ->
                                val isSelected = selectedDay == block.date
                                MiuixDayBlockItem(
                                    block = block,
                                    isSelected = isSelected,
                                    primaryColor = primaryColor,
                                    completedColor = completedColor,
                                    overdueColor = overdueColor,
                                    pendingColor = pendingColor,
                                    outlineColor = outlineColor,
                                    onSurfaceColor = onSurfaceColor,
                                    isMonthlyGrid = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onSelectDay(block.date) }
                                )
                            }
                        }
                    }
                }
            }

            // Leyenda de 4 Estados Semánticos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiuixLegendItem(color = completedColor, label = stringResource(Res.string.performance_legend_completed))
                MiuixLegendItem(color = overdueColor, label = stringResource(Res.string.performance_legend_overdue))
                MiuixLegendItem(color = pendingColor, label = stringResource(Res.string.performance_legend_pending))
                MiuixLegendItem(color = outlineColor.copy(alpha = 0.4f), label = stringResource(Res.string.performance_legend_empty))
            }
        }
    }
}

/**
 * Item individual de bloque de día en MIUIX.
 */
@Composable
private fun MiuixDayBlockItem(
    block: DayBlockInfo,
    isSelected: Boolean,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
    modifier: Modifier = Modifier,
    isMonthlyGrid: Boolean = false,
    onClick: () -> Unit
) {
    val blockBg = when (block.status) {
        DayTaskStatus.COMPLETED -> completedColor
        DayTaskStatus.OVERDUE -> overdueColor
        DayTaskStatus.PENDING -> pendingColor
        DayTaskStatus.NONE -> Color.Transparent
    }

    val blockBorder = if (isSelected) {
        Modifier.squircleBorder(width = 2.dp, color = primaryColor, cornerRadius = 10.dp)
    } else if (block.isToday) {
        Modifier.squircleBorder(width = 1.5.dp, color = primaryColor.copy(alpha = 0.6f), cornerRadius = 10.dp)
    } else if (block.status == DayTaskStatus.NONE) {
        Modifier.squircleBorder(width = 1.dp, color = outlineColor.copy(alpha = 0.25f), cornerRadius = 10.dp)
    } else Modifier

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .squircleSurface(
                color = Color.Transparent,
                cornerRadius = 10.dp
            )
            .clickable { onClick() }
            .then(blockBorder)
            .padding(vertical = if (isMonthlyGrid) 4.dp else 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = block.dayLetter,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) primaryColor else MiuixTheme.colorScheme.onSurfaceVariantSummary
        )

        Box(
            modifier = Modifier
                .size(if (isMonthlyGrid) 22.dp else 26.dp)
                .squircleSurface(
                    color = blockBg,
                    cornerRadius = 6.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = block.dayNumber.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (block.status) {
                    DayTaskStatus.COMPLETED -> Color.White
                    DayTaskStatus.OVERDUE -> Color.White
                    DayTaskStatus.PENDING -> onSurfaceColor
                    DayTaskStatus.NONE -> MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
private fun MiuixLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

/**
 * Sección de entregables del día seleccionado en MIUIX.
 */
@Composable
private fun MiuixSelectedDaySection(
    selectedDay: DateComponents,
    tasks: List<AssignmentUiModel>,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    onTaskClick: (AssignmentUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDay.day.toString().padStart(2, '0')}/${selectedDay.month.toString().padStart(2, '0')}/${selectedDay.year}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "${tasks.size} entregables",
                    fontSize = 11.sp,
                    color = onVariantColor
                )
            }

            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.performance_no_tasks_day),
                    fontSize = 12.sp,
                    color = onVariantColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tasks.forEach { ui ->
                        MiuixTaskRowItem(
                            taskUi = ui,
                            primaryColor = primaryColor,
                            completedColor = completedColor,
                            overdueColor = overdueColor,
                            pendingColor = pendingColor,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            onClick = { onTaskClick(ui) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixTaskRowItem(
    taskUi: AssignmentUiModel,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val a = taskUi.assignment
    val isDone = a.status == "completed" || a.isCompleted
    val isMissing = a.status == "missing"

    val statusColor = when {
        isDone -> completedColor
        isMissing -> overdueColor
        else -> pendingColor
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                cornerRadius = 10.dp
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = a.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = taskUi.courseName,
                    fontSize = 11.sp,
                    color = onVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (a.score != null) {
                val scoreText = if (a.pointsPossible != null && a.pointsPossible > 0.0) {
                    "${formatScoreMiuix(a.score)} / ${formatScoreMiuix(a.pointsPossible)}"
                } else {
                    formatScoreMiuix(a.score)
                }
                Text(
                    text = stringResource(Res.string.performance_score_prefix, scoreText),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (a.score >= 13.0) completedColor else overdueColor
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = onVariantColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta de desglose por curso en MIUIX.
 */
@Composable
private fun MiuixCourseItemCard(
    courseStat: CourseStat,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    outlineColor: Color,
    onCourseClick: () -> Unit,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        onClick = onCourseClick,
        pressFeedbackType = PressFeedbackType.Sink,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = courseStat.course.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = courseStat.course.courseCode ?: "",
                        fontSize = 11.sp,
                        color = onVariantColor
                    )
                }

                // Nota Promedio Real / 20
                if (courseStat.averageScore != null) {
                    val score = courseStat.averageScore
                    val isPassing = score >= courseStat.course.passingGrade
                    val badgeColor = if (isPassing) completedColor else overdueColor
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = badgeColor.copy(alpha = 0.12f),
                                cornerRadius = 8.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = badgeColor.copy(alpha = 0.35f),
                                cornerRadius = 8.dp
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${formatScoreMiuix(score)} / 20",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            // Resumen de entregables por estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiuixTaskCounterChip(
                    count = courseStat.completedCount,
                    label = "Completadas",
                    color = completedColor,
                    modifier = Modifier.weight(1f)
                )
                MiuixTaskCounterChip(
                    count = courseStat.pendingCount,
                    label = "Pendientes",
                    color = onVariantColor,
                    modifier = Modifier.weight(1f)
                )
                MiuixTaskCounterChip(
                    count = courseStat.missingCount,
                    label = "Vencidas",
                    color = overdueColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Botón a Simulación de Fórmulas o Progreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfigureClick() }
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (courseStat.hasConfiguredGroups) stringResource(Res.string.performance_view_simulation)
                    else stringResource(Res.string.performance_configure_formula),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }
    }
}

@Composable
private fun MiuixTaskCounterChip(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                cornerRadius = 8.dp
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatScoreMiuix(value: Double): String {
    val rounded = (value * 10).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
