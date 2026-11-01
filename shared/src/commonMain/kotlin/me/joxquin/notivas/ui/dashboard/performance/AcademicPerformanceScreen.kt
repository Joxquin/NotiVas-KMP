package me.joxquin.notivas.ui.dashboard.performance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.AcademicPerformanceStats
import me.joxquin.notivas.data.model.DayBlockInfo
import me.joxquin.notivas.data.model.DayTaskStatus
import me.joxquin.notivas.data.model.GradePerformanceLevel
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.util.DateComponents
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicPerformanceScreen(
    viewModel: DashboardViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onNavigateToCourseProgreso: (Long) -> Unit,
    onNavigateToAssignmentDraft: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val stats by viewModel.performanceStats.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val weeklyBlocks by viewModel.weeklyDayBlocks.collectAsState()
    val monthlyBlocks by viewModel.monthlyDayBlocks.collectAsState()
    val selectedDay by viewModel.selectedPerformanceDay.collectAsState()
    val selectedDayTasks by viewModel.selectedPerformanceDayTasks.collectAsState()
    val weekOffset by viewModel.selectedPerformanceWeekOffset.collectAsState()
    val dynamicColorEnabled by AppModule.preferencesManager.dynamicColorEnabled.collectAsState(initial = true)

    var isMonthlyView by remember { mutableStateOf(false) }

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val completedColor = if (dynamicColorEnabled) primaryColor else Color(0xFF10B981)
    val overdueColor = Color(0xFFE53935)
    val pendingColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest
    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = if (isMiuix) MiuixTheme.colorScheme.outline else MaterialTheme.colorScheme.outline

    Scaffold(
        topBar = {
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
                            style = MaterialTheme.typography.labelSmall,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isMiuix) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = if (isMiuix) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── 1. Tarjeta Hero de Promedio Global & KPIs ─────────────────
            item {
                PerformanceHeroKpiCard(
                    stats = stats,
                    primaryColor = primaryColor,
                    completedColor = completedColor,
                    overdueColor = overdueColor,
                    pendingColor = pendingColor,
                    containerColor = containerColor,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor,
                    isMiuix = isMiuix
                )
            }

            // ─── 2. Selector Semanal vs Mensual ─────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth(0.85f)) {
                        SegmentedButton(
                            selected = !isMonthlyView,
                            onClick = { isMonthlyView = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(stringResource(Res.string.performance_weekly))
                        }
                        SegmentedButton(
                            selected = isMonthlyView,
                            onClick = { isMonthlyView = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(stringResource(Res.string.performance_monthly))
                        }
                    }
                }
            }

            // ─── 3. Cuadrícula de Bloques Interactiva (Heatmap) ────────────
            item {
                PerformanceHeatmapCard(
                    isMonthlyView = isMonthlyView,
                    weeklyBlocks = weeklyBlocks,
                    monthlyBlocks = monthlyBlocks,
                    selectedDay = selectedDay,
                    weekOffset = weekOffset,
                    primaryColor = primaryColor,
                    completedColor = completedColor,
                    overdueColor = overdueColor,
                    pendingColor = pendingColor,
                    outlineColor = outlineColor,
                    containerColor = containerColor,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor,
                    isMiuix = isMiuix,
                    onSelectDay = { viewModel.selectPerformanceDay(it) },
                    onPrevWeek = { viewModel.previousPerformanceWeek() },
                    onNextWeek = { viewModel.nextPerformanceWeek() },
                    onResetWeek = { viewModel.resetPerformanceWeek() }
                )
            }

            // ─── 4. Tareas del Día Seleccionado ─────────────────────────────
            if (selectedDay != null) {
                item {
                    PerformanceSelectedDaySection(
                        selectedDay = selectedDay!!,
                        tasks = selectedDayTasks,
                        primaryColor = primaryColor,
                        completedColor = completedColor,
                        overdueColor = overdueColor,
                        pendingColor = pendingColor,
                        containerColor = containerColor,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        isMiuix = isMiuix,
                        onTaskClick = { task ->
                            onNavigateToAssignmentDraft(task.assignment.id, task.courseName)
                        }
                    )
                }
            }

            // ─── 5. Desglose de Cursos y Calificaciones ─────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.performance_courses_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor
                    )
                }
            }

            items(courseStats, key = { it.course.id }) { stat ->
                PerformanceCourseItemCard(
                    courseStat = stat,
                    primaryColor = primaryColor,
                    containerColor = containerColor,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor,
                    isMiuix = isMiuix,
                    onConfigureClick = { onNavigateToCourseProgreso(stat.course.id) }
                )
            }
        }
    }
}

@Composable
private fun PerformanceHeroKpiCard(
    stats: AcademicPerformanceStats,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    containerColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    isMiuix: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila superior: Promedio Ponderado + Badge de Desempeño
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Promedio Ponderado Real",
                        style = MaterialTheme.typography.labelMedium,
                        color = onVariantColor
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        val scoreDisplay = if (stats.overallAverage > 0.0) {
                            ((stats.overallAverage * 10).toInt().toDouble() / 10).toString()
                        } else "--"
                        Text(
                            text = scoreDisplay,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = onSurfaceColor
                        )
                        Text(
                            text = " / 20",
                            style = MaterialTheme.typography.titleMedium,
                            color = onVariantColor,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                // Badge de Nivel
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
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Barra de progreso de cumplimiento
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
                        text = "${stats.completionRatePercentage.toInt()}%",
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
                    trackColor = onSurfaceColor.copy(alpha = 0.08f)
                )
            }

            // Grid de 3 mini-métricas: Entregadas, Pendientes, Vencidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    count = stats.completedAssignments,
                    label = stringResource(Res.string.performance_legend_completed),
                    color = completedColor
                )
                MetricPill(
                    count = stats.pendingAssignments,
                    label = stringResource(Res.string.performance_legend_pending),
                    color = onVariantColor
                )
                MetricPill(
                    count = stats.missingAssignments,
                    label = stringResource(Res.string.performance_legend_overdue),
                    color = overdueColor
                )
            }
        }
    }
}

@Composable
private fun MetricPill(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PerformanceHeatmapCard(
    isMonthlyView: Boolean,
    weeklyBlocks: List<DayBlockInfo>,
    monthlyBlocks: List<List<DayBlockInfo>>,
    selectedDay: DateComponents?,
    weekOffset: Int,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    outlineColor: Color,
    containerColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    isMiuix: Boolean,
    onSelectDay: (DateComponents) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetWeek: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isMonthlyView) {
                // Header Semanal con controles de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevWeek) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(Res.string.performance_prev_week),
                            tint = onSurfaceColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (weekOffset == 0) stringResource(Res.string.performance_current_week)
                            else if (weekOffset < 0) "Hace ${-weekOffset} semanas"
                            else "En ${weekOffset} semanas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = onSurfaceColor
                        )
                        if (weekOffset != 0) {
                            Text(
                                text = "Volver a la actual",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                modifier = Modifier.clickable { onResetWeek() }
                            )
                        }
                    }

                    IconButton(onClick = onNextWeek) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(Res.string.performance_next_week),
                            tint = onSurfaceColor
                        )
                    }
                }

                // Tira de 7 días Semanales Ampliados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyBlocks.forEach { block ->
                        val isSelected = selectedDay == block.date
                        DayBlockItem(
                            block = block,
                            isSelected = isSelected,
                            primaryColor = primaryColor,
                            completedColor = completedColor,
                            overdueColor = overdueColor,
                            pendingColor = pendingColor,
                            outlineColor = outlineColor,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            onSelect = { onSelectDay(block.date) }
                        )
                    }
                }
            } else {
                // Vista Mensual (4 semanas x 7 días)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cabecera de días
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = onVariantColor,
                                modifier = Modifier.width(32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // 4 Filas de Semanas
                    monthlyBlocks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { block ->
                                val isSelected = selectedDay == block.date
                                MonthGridBlock(
                                    block = block,
                                    isSelected = isSelected,
                                    primaryColor = primaryColor,
                                    completedColor = completedColor,
                                    overdueColor = overdueColor,
                                    pendingColor = pendingColor,
                                    outlineColor = outlineColor,
                                    onSurfaceColor = onSurfaceColor,
                                    onSelect = { onSelectDay(block.date) }
                                )
                            }
                        }
                    }
                }
            }

            // Leyenda de 4 Estados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator(completedColor, stringResource(Res.string.performance_legend_completed))
                LegendIndicator(overdueColor, stringResource(Res.string.performance_legend_overdue))
                LegendIndicator(pendingColor, stringResource(Res.string.performance_legend_pending))
                LegendIndicator(outlineColor.copy(alpha = 0.35f), stringResource(Res.string.performance_legend_empty), isOutline = true)
            }
        }
    }
}

@Composable
private fun DayBlockItem(
    block: DayBlockInfo,
    isSelected: Boolean,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    onSelect: () -> Unit
) {
    val blockBg = when (block.status) {
        DayTaskStatus.COMPLETED -> completedColor
        DayTaskStatus.OVERDUE -> overdueColor
        DayTaskStatus.PENDING -> pendingColor
        DayTaskStatus.NONE -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(
            text = block.dayLetter,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (block.isToday) primaryColor else onVariantColor
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(blockBg)
                .then(
                    if (isSelected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                    else if (block.status == DayTaskStatus.NONE) Modifier.border(1.dp, outlineColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${block.dayNumber}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (block.isToday) FontWeight.ExtraBold else FontWeight.Medium
                ),
                color = when (block.status) {
                    DayTaskStatus.COMPLETED, DayTaskStatus.OVERDUE -> Color.White
                    else -> onSurfaceColor
                }
            )
        }

        if (block.isToday) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
private fun MonthGridBlock(
    block: DayBlockInfo,
    isSelected: Boolean,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
    onSelect: () -> Unit
) {
    val blockBg = when (block.status) {
        DayTaskStatus.COMPLETED -> completedColor
        DayTaskStatus.OVERDUE -> overdueColor
        DayTaskStatus.PENDING -> pendingColor
        DayTaskStatus.NONE -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(blockBg)
            .then(
                if (isSelected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(6.dp))
                else if (block.status == DayTaskStatus.NONE) Modifier.border(1.dp, outlineColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                else Modifier
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${block.dayNumber}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (block.isToday) FontWeight.Bold else FontWeight.Normal
            ),
            color = when (block.status) {
                DayTaskStatus.COMPLETED, DayTaskStatus.OVERDUE -> Color.White
                else -> onSurfaceColor
            }
        )
    }
}

@Composable
private fun LegendIndicator(color: Color, text: String, isOutline: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .then(
                    if (isOutline) Modifier.border(1.dp, color, RoundedCornerShape(2.dp))
                    else Modifier.background(color)
                )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PerformanceSelectedDaySection(
    selectedDay: DateComponents,
    tasks: List<AssignmentUiModel>,
    primaryColor: Color,
    completedColor: Color,
    overdueColor: Color,
    pendingColor: Color,
    containerColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    isMiuix: Boolean,
    onTaskClick: (AssignmentUiModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${stringResource(Res.string.performance_day_tasks_title)} (${DateTimeUtil.formatFullDate(selectedDay)})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = onSurfaceColor
            )
        }

        if (tasks.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = completedColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.performance_no_tasks_day),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onVariantColor
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { task ->
                    val isDone = task.assignment.status == "completed" || task.assignment.isCompleted
                    val isMissing = task.assignment.status == "missing"

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskClick(task) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = task.courseName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor
                                )
                                Text(
                                    text = task.assignment.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = onSurfaceColor
                                )
                                task.assignment.dueAt?.let { due ->
                                    DateTimeUtil.parseIsoToComponents(due)?.let { comp ->
                                        val m = if (comp.minute < 10) "0${comp.minute}" else "${comp.minute}"
                                        Text(
                                            text = "Vence: ${comp.hour}:$m hrs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onVariantColor
                                        )
                                    }
                                }
                            }

                            // Estado / Nota
                            Column(horizontalAlignment = Alignment.End) {
                                if (task.assignment.score != null) {
                                    val pts = task.assignment.pointsPossible ?: 20.0
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = completedColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${task.assignment.score} / $pts",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = completedColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    val (statusText, statusColor) = when {
                                        isDone -> "Entregada" to completedColor
                                        isMissing -> "Vencida" to overdueColor
                                        else -> "Pendiente" to onVariantColor
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceCourseItemCard(
    courseStat: CourseStat,
    primaryColor: Color,
    containerColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color,
    isMiuix: Boolean,
    onConfigureClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = courseStat.course.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor
                    )
                    courseStat.course.courseCode?.let { code ->
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelSmall,
                            color = onVariantColor
                        )
                    }
                }

                // Nota promedio
                Column(horizontalAlignment = Alignment.End) {
                    val avgDisplay = courseStat.averageScore?.let {
                        ((it * 10).toInt().toDouble() / 10).toString()
                    } ?: "--"
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = avgDisplay,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = onSurfaceColor
                        )
                        Text(
                            text = " /20",
                            style = MaterialTheme.typography.labelSmall,
                            color = onVariantColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // Tareas desglose
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${courseStat.completedCount} entregadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor
                )
                Text(
                    text = "${courseStat.pendingCount} pendientes",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor
                )
                if (courseStat.missingCount > 0) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = onVariantColor
                    )
                    Text(
                        text = "${courseStat.missingCount} vencidas",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE53935)
                    )
                }
            }

            // Botón a Simulación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfigureClick() }
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (courseStat.hasConfiguredGroups) "Ver simulación & grupos" else "Configurar fórmulas",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
            }
        }
    }
}
