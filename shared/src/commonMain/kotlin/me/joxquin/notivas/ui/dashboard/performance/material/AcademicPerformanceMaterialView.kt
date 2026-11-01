package me.joxquin.notivas.ui.dashboard.performance.material

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import me.joxquin.notivas.data.model.CourseStat
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
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateComponents
import org.jetbrains.compose.resources.stringResource

/**
 * Vista nativa dedicada de Rendimiento Académico para Material 3 Expressive.
 * Aplica tarjetas redondeadas (24.dp), elevaciones tonales, `SingleChoiceSegmentedButtonRow`,
 * animaciones de contenedor reactivo al desplazamiento y tipografía de Material Theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicPerformanceMaterialView(
    stats: AcademicPerformanceStats,
    courseStats: List<CourseStat>,
    weeklyBlocks: List<DayBlockInfo>,
    monthlyBlocks: List<DayBlockInfo>,
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
    val lazyListState = rememberLazyListState()
    var isMonthlyView by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val completedColor = if (dynamicColorEnabled) primaryColor else Color(0xFF10B981)
    val overdueColor = Color(0xFFE53935)
    val pendingColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    val topAppBarContainerColor by animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 240),
        label = "materialTopBarContainerColor"
    )

    Scaffold(
        topBar = {
            Surface(
                color = topAppBarContainerColor,
                shadowElevation = if (isScrolled) 3.dp else 0.dp
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(Res.string.performance_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = onSurfaceColor
                            )
                            Text(
                                text = stringResource(Res.string.performance_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = onVariantColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = onSurfaceColor
                            )
                        }
                    },
                    actions = {
                        if (weekOffset != 0 && !isMonthlyView) {
                            IconButton(onClick = onResetWeek) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(Res.string.performance_current_week),
                                    tint = primaryColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarContainerColor,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero KPI Card de Rendimiento Global
            item(key = "material_hero_kpi") {
                MaterialHeroKpiCard(
                    stats = stats,
                    primaryColor = primaryColor,
                    completedColor = completedColor,
                    overdueColor = overdueColor,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor
                )
            }

            // 2. Selector de Período y Heatmap (Semanal / Mensual)
            item(key = "material_heatmap_card") {
                MaterialHeatmapCard(
                    isMonthlyView = isMonthlyView,
                    onToggleMonthly = { isMonthlyView = it },
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
                item(key = "material_selected_day_tasks") {
                    MaterialSelectedDaySection(
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

            // 4. Desglose Detallado por Cursos
            item(key = "material_courses_title") {
                Text(
                    text = stringResource(Res.string.performance_courses_breakdown),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            items(
                items = courseStats,
                key = { "material_course_${it.course.id}" }
            ) { stat ->
                MaterialCourseItemCard(
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
    }
}

/**
 * Tarjeta Hero KPI de Rendimiento en Material 3 Expressive.
 */
@Composable
private fun MaterialHeroKpiCard(
    stats: AcademicPerformanceStats,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila de Promedio y Badge de Rendimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.performance_average_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = onVariantColor
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (stats.overallAverage > 0.0) formatScoreMaterial(stats.overallAverage) else "--",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                        Text(
                            text = stringResource(Res.string.performance_average_scale),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = onVariantColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Badge de Nivel según escala Canvas
                val (levelText, levelColor) = when (stats.performanceLevel) {
                    GradePerformanceLevel.EXCELLENT -> "Sobresaliente" to completedColor
                    GradePerformanceLevel.PASSING -> "Aprobado" to primaryColor
                    GradePerformanceLevel.AT_RISK -> "En Riesgo" to overdueColor
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = levelColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = levelText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Tasa de Cumplimiento
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.performance_completion_rate),
                        style = MaterialTheme.typography.bodySmall,
                        color = onVariantColor
                    )
                    Text(
                        text = "${stats.completionRatePercentage.toInt()}% (${stats.completedAssignments}/${stats.totalAssignments})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor
                    )
                }
                LinearProgressIndicator(
                    progress = { (stats.completionRatePercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = completedColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            // Mini Tarjetas de Métricas Extremas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialMetricCard(
                    label = stringResource(Res.string.performance_highest_score),
                    value = stats.highestScore?.let { formatScoreMaterial(it) } ?: "--",
                    color = completedColor,
                    modifier = Modifier.weight(1f)
                )
                MaterialMetricCard(
                    label = stringResource(Res.string.performance_lowest_score),
                    value = stats.lowestScore?.let { formatScoreMaterial(it) } ?: "--",
                    color = overdueColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MaterialMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

/**
 * Tarjeta de Heatmap en Material 3 Expressive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialHeatmapCard(
    isMonthlyView: Boolean,
    onToggleMonthly: (Boolean) -> Unit,
    weeklyBlocks: List<DayBlockInfo>,
    monthlyBlocks: List<DayBlockInfo>,
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selector Semanal / Mensual con SegmentedButton
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = !isMonthlyView,
                    onClick = { onToggleMonthly(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {}
                ) {
                    Text(
                        text = stringResource(Res.string.performance_weekly),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                SegmentedButton(
                    selected = isMonthlyView,
                    onClick = { onToggleMonthly(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {}
                ) {
                    Text(
                        text = stringResource(Res.string.performance_monthly),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

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
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
                        MaterialDayBlockItem(
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = onVariantColor,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    monthlyBlocks.chunked(7).forEach { weekChunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekChunk.forEach { block ->
                                val isSelected = selectedDay == block.date
                                MaterialDayBlockItem(
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
                MaterialLegendItem(color = completedColor, label = stringResource(Res.string.performance_legend_completed))
                MaterialLegendItem(color = overdueColor, label = stringResource(Res.string.performance_legend_overdue))
                MaterialLegendItem(color = pendingColor, label = stringResource(Res.string.performance_legend_pending))
                MaterialLegendItem(color = outlineColor.copy(alpha = 0.4f), label = stringResource(Res.string.performance_legend_empty))
            }
        }
    }
}

@Composable
private fun MaterialDayBlockItem(
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
        Modifier.border(2.dp, primaryColor, RoundedCornerShape(12.dp))
    } else if (block.isToday) {
        Modifier.border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    } else if (block.status == DayTaskStatus.NONE) {
        Modifier.border(1.dp, outlineColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    } else Modifier

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(blockBorder)
            .padding(vertical = if (isMonthlyGrid) 4.dp else 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = block.dayLetter,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .size(if (isMonthlyGrid) 22.dp else 26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(blockBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = block.dayNumber.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = when (block.status) {
                    DayTaskStatus.COMPLETED -> Color.White
                    DayTaskStatus.OVERDUE -> Color.White
                    DayTaskStatus.PENDING -> onSurfaceColor
                    DayTaskStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
private fun MaterialLegendItem(
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
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Sección de entregables del día seleccionado en Material 3 Expressive.
 */
@Composable
private fun MaterialSelectedDaySection(
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDay.day.toString().padStart(2, '0')}/${selectedDay.month.toString().padStart(2, '0')}/${selectedDay.year}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
                Text(
                    text = "${tasks.size} entregables",
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor
                )
            }

            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.performance_no_tasks_day),
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { ui ->
                        MaterialTaskRowItem(
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
private fun MaterialTaskRowItem(
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = taskUi.courseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (a.score != null) {
                Text(
                    text = stringResource(Res.string.performance_score_prefix, formatScoreMaterial(a.score)),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (a.score >= 13.0) completedColor else overdueColor
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = onVariantColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta de desglose por curso en Material 3 Expressive.
 */
@Composable
private fun MaterialCourseItemCard(
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
            .clickable { onCourseClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = courseStat.course.courseCode ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = onVariantColor
                    )
                }

                // Nota Promedio Real / 20
                if (courseStat.averageScore != null) {
                    val score = courseStat.averageScore
                    val isPassing = score >= courseStat.course.passingGrade
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPassing) completedColor.copy(alpha = 0.12f)
                        else overdueColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${formatScoreMaterial(score)} / 20",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPassing) completedColor else overdueColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Resumen de entregables por estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialTaskCounterChip(
                    count = courseStat.completedCount,
                    label = "Completadas",
                    color = completedColor,
                    modifier = Modifier.weight(1f)
                )
                MaterialTaskCounterChip(
                    count = courseStat.pendingCount,
                    label = "Pendientes",
                    color = onVariantColor,
                    modifier = Modifier.weight(1f)
                )
                MaterialTaskCounterChip(
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (courseStat.hasConfiguredGroups) stringResource(Res.string.performance_view_simulation)
                    else stringResource(Res.string.performance_configure_formula),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
            }
        }
    }
}

@Composable
private fun MaterialTaskCounterChip(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatScoreMaterial(value: Double): String {
    val rounded = (value * 10).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
