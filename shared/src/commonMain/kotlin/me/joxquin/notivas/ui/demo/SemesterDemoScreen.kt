package me.joxquin.notivas.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.domain.sync.DeadlineRadarEntry
import me.joxquin.notivas.domain.sync.detectAssignmentDeadlineChanges
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase
import me.joxquin.notivas.domain.usecase.BuildStudyPlanUseCase
import me.joxquin.notivas.domain.usecase.StudyPlanGradeScenario
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.AcademicFeatureButton
import me.joxquin.notivas.ui.components.AcademicFeatureCard
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.DeadlineRadarUiItem
import me.joxquin.notivas.ui.dashboard.components.DashboardDeadlineRadarCard
import me.joxquin.notivas.ui.dashboard.components.DashboardStudyPlanCard
import me.joxquin.notivas.ui.dashboard.draft.components.TeamChecklistCard
import me.joxquin.notivas.ui.dashboard.draft.components.parseConsignaSections
import me.joxquin.notivas.ui.dashboard.uiState
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SemesterDemoScreen(themeStyle: AppThemeStyle, onExit: () -> Unit) {
    BackHandler(onBack = onExit)
    val title = stringResource(Res.string.semester_demo_title)
    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()
        MiuixScaffold(
            popupHost = {},
            topBar = {
                MiuixBlurredBar(backdrop = backdrop, scrollBehavior = scrollBehavior) {
                    MiuixPageTopAppBar(
                        title = title,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = { BackNavigationIcon(onClick = onExit) }
                    )
                }
            }
        ) { padding ->
            SemesterDemoContent(themeStyle, Modifier.padding(padding).miuixBackdropModifier(backdrop))
        }
    } else {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        }) { padding ->
            SemesterDemoContent(themeStyle, Modifier.padding(padding))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SemesterDemoContent(themeStyle: AppThemeStyle, modifier: Modifier) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurface = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val secondary = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val nowMillis = rememberSaveable { DateTimeUtil.nowEpochMillis() }
    val now = remember(nowMillis) { Instant.fromEpochMilliseconds(nowMillis) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var available by rememberSaveable { mutableStateOf(60) }
    var reportMinutes by rememberSaveable { mutableStateOf(45) }
    var summaryMinutes by rememberSaveable { mutableStateOf(15) }
    var labMinutes by rememberSaveable { mutableStateOf(30) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var projectedScore by rememberSaveable { mutableStateOf(16) }
    val networks = stringResource(Res.string.semester_demo_networks)
    val development = stringResource(Res.string.semester_demo_development)
    val report = stringResource(Res.string.semester_demo_report)
    val summary = stringResource(Res.string.semester_demo_summary)
    val lab = stringResource(Res.string.semester_demo_lab)
    val baseline = listOf(
        Assignment(101, report, dueAt = (now + 100.minutes).toString(), courseId = 1),
        Assignment(102, summary, dueAt = (now + 120.minutes).toString(), courseId = 2),
        Assignment(103, lab, dueAt = (now + 180.minutes).toString(), courseId = 2)
    )
    val assignments = if (advanced) {
        listOf(baseline[0].copy(dueAt = (now + 25.minutes).toString()), baseline[1], baseline[2])
    } else baseline
    val assignmentUi = assignments.map { AssignmentUiModel(it, if (it.courseId == 1L) networks else development) }
    val calculator = remember { AcademicCalculatorUseCase() }
    val studyPlan = remember { BuildStudyPlanUseCase() }
    val gradeImpact = remember(calculator) { StudyPlanGradeScenario(calculator) }
    val course = Course(1L, networks)
    val groups = listOf(
        SimulationGroupWithItems(SimulationGroup(1, course.id, networks, 80f), listOf(
            SimulationItem(1, 1, name = networks, manualScore = 14f)
        )),
        SimulationGroupWithItems(SimulationGroup(2, course.id, report, 20f), listOf(
            SimulationItem(2, 2, canvasAssignmentId = 101L, name = report, simulatedScore = projectedScore.toFloat(), isSimulated = true)
        ))
    )
    val plan = studyPlan.uiState(
        assignmentUi, mapOf(101L to reportMinutes, 102L to summaryMinutes, 103L to labMinutes), available, now
    ) { task -> gradeImpact.improvement(course, groups, task.id) }
    val changes = detectAssignmentDeadlineChanges(baseline, assignments).map { change ->
        val task = assignmentUi.first { it.assignment.id == change.assignment.id }
        DeadlineRadarUiItem(
            DeadlineRadarEntry(1L, change.assignment.id, change.assignment.name, task.courseName, change.previous, change.current),
            task
        )
    }
    val selected = assignmentUi.firstOrNull { it.assignment.id == selectedId }?.assignment
    val projectedGrade = calculator.calculateCourseSummary(course, groups).projectedFinalGrade
    val checklistSteps = listOf(
        stringResource(Res.string.semester_demo_step_outline),
        stringResource(Res.string.semester_demo_step_evidence),
        stringResource(Res.string.semester_demo_step_review)
    )
    val sampleInstructions = remember(checklistSteps) {
        parseConsignaSections(checklistSteps.joinToString(separator = "", prefix = "<ul>", postfix = "</ul>") { "<li>$it</li>" })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "demo_change") {
            AcademicFeatureCard(themeStyle) {
                Text(stringResource(Res.string.semester_demo_intro), style = MaterialTheme.typography.bodyMedium, color = secondary)
                AcademicFeatureButton(
                    text = stringResource(if (advanced) Res.string.semester_demo_reset_date else Res.string.semester_demo_move_date),
                    themeStyle = themeStyle,
                    onClick = { advanced = !advanced }
                )
            }
        }
        if (changes.isNotEmpty()) {
            item(key = "demo_radar") {
                DashboardDeadlineRadarCard(changes, themeStyle, { selectedId = it.assignment.id })
            }
        }
        item(key = "demo_plan") {
            DashboardStudyPlanCard(
                state = plan,
                themeStyle = themeStyle,
                onAvailableMinutesChange = { available = it },
                onEstimatedMinutesChange = { id, value ->
                    when (id) {
                        101L -> reportMinutes = value
                        102L -> summaryMinutes = value
                        103L -> labMinutes = value
                    }
                },
                onTaskClick = { selectedId = it.assignment.id },
                initiallyExpanded = true
            )
        }
        item(key = "demo_grade") {
            AcademicFeatureCard(themeStyle) {
                Text(stringResource(Res.string.semester_demo_grade_title), style = MaterialTheme.typography.titleMedium, color = onSurface)
                Text(stringResource(Res.string.semester_demo_grade_input, projectedScore), color = secondary)
                Text(stringResource(Res.string.semester_demo_grade_result, projectedGrade.toString()), color = onSurface)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AcademicFeatureButton(
                        text = stringResource(Res.string.semester_demo_grade_lower),
                        themeStyle = themeStyle,
                        onClick = { projectedScore-- },
                        enabled = projectedScore > 0
                    )
                    AcademicFeatureButton(
                        text = stringResource(Res.string.semester_demo_grade_raise),
                        themeStyle = themeStyle,
                        onClick = { projectedScore++ },
                        enabled = projectedScore < 20
                    )
                }
            }
        }
        if (selected != null) {
            item(key = "demo_checklist_${selected.id}") {
                TeamChecklistCard(selected, sampleInstructions, baseUrl = null, themeStyle = themeStyle)
            }
        }
    }
}
