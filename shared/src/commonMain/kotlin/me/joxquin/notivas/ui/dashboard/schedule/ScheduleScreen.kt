package me.joxquin.notivas.ui.dashboard.schedule

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.back
import me.joxquin.notivas.shared.resources.no_tasks_for_selected_day
import me.joxquin.notivas.shared.resources.schedule_completed_status
import me.joxquin.notivas.shared.resources.schedule_in_progress_status
import me.joxquin.notivas.shared.resources.schedule_screen_title
import me.joxquin.notivas.shared.resources.schedule_upcoming_status
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.DaySchedule
import me.joxquin.notivas.util.DateComponents
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun ScheduleScreen(
    viewModel: DashboardViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = false, onBack = onBack)

    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allAssignments by viewModel.allAssignmentsUi.collectAsState()

    // Si no hay fecha explícitamente seleccionada, mostrar el día actual o el primero de la semana
    val effectiveDate = remember(selectedDate, weeklySchedule) {
        selectedDate
            ?: weeklySchedule.firstOrNull { it.isToday }?.date
            ?: weeklySchedule.firstOrNull()?.date
            ?: DateTimeUtil.nowLocalDate()
    }

    // Filtrar tareas que vencen en la fecha activa
    val dayAssignments = remember(allAssignments, effectiveDate) {
        allAssignments.filter { ui ->
            val due = ui.assignment.dueAt ?: ui.assignment.lockAt ?: return@filter false
            val comp = DateTimeUtil.parseIsoToComponents(due) ?: return@filter false
            comp.date == effectiveDate
        }.sortedBy { ui ->
            val due = ui.assignment.dueAt ?: ui.assignment.lockAt ?: ""
            DateTimeUtil.parseIsoToComponents(due)?.epochMillis ?: Long.MAX_VALUE
        }
    }

    val monthYearText = remember(effectiveDate) {
        DateTimeUtil.formatMonthYear(effectiveDate)
    }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

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
                        title = stringResource(Res.string.schedule_screen_title),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            BackNavigationIcon(onClick = onBack)
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
                    // ─── 1. Selector de Mes y Días Semanales (Estilo Stitch) ────────────
                    item {
                        MiuixCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.None
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = monthYearText,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                }

                                // Tira horizontal de los 7 días
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    weeklySchedule.forEach { day ->
                                        val isSelected = day.date == effectiveDate
                                        ScheduleDayPill(
                                            day = day,
                                            isSelected = isSelected,
                                            isMiuix = true,
                                            onClick = { viewModel.selectDate(day.date) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ─── 2. Lista de Tareas y Cronograma de Horas del Día ───────────────
                    if (dayAssignments.isEmpty()) {
                        item {
                            MiuixCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                insideMargin = PaddingValues(32.dp),
                                pressFeedbackType = PressFeedbackType.None
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = stringResource(Res.string.no_tasks_for_selected_day),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    } else {
                        items(dayAssignments, key = { it.assignment.id }) { item ->
                            ScheduleAssignmentTimelineCard(
                                item = item,
                                isMiuix = true,
                                onClick = { onAssignmentClick(item) }
                            )
                        }
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
        // Material Design 3 Expressive Scaffold
        val topAppBarContainerColor by animateColorAsState(
            targetValue = if (isScrolled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
            animationSpec = tween(durationMillis = 250),
            label = "scheduleTopBarColor"
        )

        Scaffold(
            topBar = {
                Surface(
                    color = topAppBarContainerColor,
                    shadowElevation = if (isScrolled) 2.dp else 0.dp
                ) {
                    @OptIn(ExperimentalMaterial3Api::class)
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
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(Res.string.schedule_screen_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Planificador y entregas",
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
                                    contentDescription = stringResource(Res.string.back),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = topAppBarContainerColor,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── 1. Selector de Días Semanales (Card Material 3) ────────────────
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = monthYearText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                weeklySchedule.forEach { day ->
                                    val isSelected = day.date == effectiveDate
                                    ScheduleDayPill(
                                        day = day,
                                        isSelected = isSelected,
                                        isMiuix = false,
                                        onClick = { viewModel.selectDate(day.date) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── 2. Tareas del Día ──────────────────────────────────────────────
                if (dayAssignments.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.no_tasks_for_selected_day),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(dayAssignments, key = { it.assignment.id }) { item ->
                        ScheduleAssignmentTimelineCard(
                            item = item,
                            isMiuix = false,
                            onClick = { onAssignmentClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDayPill(
    day: DaySchedule,
    isSelected: Boolean,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    val activeBgColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val activeTextColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val inactiveTextColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) activeBgColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text(
            text = day.dayName.take(3),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) activeTextColor else inactiveTextColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = day.dayNumber.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) activeTextColor else (if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (day.taskCount > 0) {
                        if (isSelected) activeTextColor else (if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary)
                    } else Color.Transparent
                )
        )
    }
}

@Composable
private fun ScheduleAssignmentTimelineCard(
    item: AssignmentUiModel,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    val dueRaw = item.assignment.dueAt ?: item.assignment.lockAt ?: ""
    val timeFormatted = remember(dueRaw) {
        val comp = DateTimeUtil.parseIsoToComponents(dueRaw)
        if (comp != null) "${comp.hour.toString().padStart(2, '0')}:${comp.minute.toString().padStart(2, '0')}"
        else "23:59"
    }

    val isCompleted = item.assignment.isCompleted || item.assignment.status == "completed"
    val statusLabel = if (isCompleted) {
        stringResource(Res.string.schedule_completed_status)
    } else {
        stringResource(Res.string.schedule_upcoming_status)
    }

    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val accentColor = if (isCompleted) {
        if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    } else {
        if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Columna de hora lateral (Estilo Agenda Timeline)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(48.dp).padding(top = 10.dp)
        ) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tarjeta con barra vertical indicadora (como en el diseño de Schedule)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = containerColor,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra de color lateral
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.assignment.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.courseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(if (isMiuix) MiuixTheme.colorScheme.dividerLine else MaterialTheme.colorScheme.outlineVariant)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = accentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantActions else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
