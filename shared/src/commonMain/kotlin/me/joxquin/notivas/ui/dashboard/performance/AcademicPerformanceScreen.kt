package me.joxquin.notivas.ui.dashboard.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.performance.material.AcademicPerformanceMaterialView
import me.joxquin.notivas.ui.dashboard.performance.miuix.AcademicPerformanceMiuixView

/**
 * Pantalla principal de Rendimiento Académico.
 * Actúa como enrutador y proveedor de estado desacoplado, despachando la interfaz
 * dedicada según el estilo de tema activo:
 * - [AcademicPerformanceMiuixView] para Xiaomi HyperOS / Compose MIUIX UI.
 * - [AcademicPerformanceMaterialView] para Material 3 Expressive.
 */
@Composable
fun AcademicPerformanceScreen(
    viewModel: DashboardViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onNavigateToCourseProgreso: (Long) -> Unit,
    onNavigateToAssignmentDraft: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = false, onBack = onBack)

    val stats by viewModel.performanceStats.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val weeklyBlocks by viewModel.weeklyDayBlocks.collectAsState()
    val monthlyBlocks by viewModel.monthlyDayBlocks.collectAsState()
    val selectedDay by viewModel.selectedPerformanceDay.collectAsState()
    val selectedDayTasks by viewModel.selectedPerformanceDayTasks.collectAsState()
    val weekOffset by viewModel.selectedPerformanceWeekOffset.collectAsState()
    val dynamicColorEnabled by AppModule.preferencesManager.dynamicColorEnabled.collectAsState(initial = true)

    if (themeStyle == AppThemeStyle.MIUIX) {
        AcademicPerformanceMiuixView(
            stats = stats,
            courseStats = courseStats,
            weeklyBlocks = weeklyBlocks,
            monthlyBlocks = monthlyBlocks,
            selectedDay = selectedDay,
            selectedDayTasks = selectedDayTasks,
            weekOffset = weekOffset,
            dynamicColorEnabled = dynamicColorEnabled,
            onBack = onBack,
            onSelectDay = { viewModel.selectPerformanceDay(it) },
            onPreviousWeek = { viewModel.previousPerformanceWeek() },
            onNextWeek = { viewModel.nextPerformanceWeek() },
            onResetWeek = { viewModel.resetPerformanceWeek() },
            onNavigateToCourseProgreso = onNavigateToCourseProgreso,
            onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
            modifier = modifier
        )
    } else {
        AcademicPerformanceMaterialView(
            stats = stats,
            courseStats = courseStats,
            weeklyBlocks = weeklyBlocks,
            monthlyBlocks = monthlyBlocks,
            selectedDay = selectedDay,
            selectedDayTasks = selectedDayTasks,
            weekOffset = weekOffset,
            dynamicColorEnabled = dynamicColorEnabled,
            onBack = onBack,
            onSelectDay = { viewModel.selectPerformanceDay(it) },
            onPreviousWeek = { viewModel.previousPerformanceWeek() },
            onNextWeek = { viewModel.nextPerformanceWeek() },
            onResetWeek = { viewModel.resetPerformanceWeek() },
            onNavigateToCourseProgreso = onNavigateToCourseProgreso,
            onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
            modifier = modifier
        )
    }
}
