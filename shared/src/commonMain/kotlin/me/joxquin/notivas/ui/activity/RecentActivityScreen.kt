package me.joxquin.notivas.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun RecentActivityScreen(
    viewModel: ActivityViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onNavigateToAssignment: (assignmentId: Long) -> Unit,
    onNavigateToForum: (topicId: Long) -> Unit,
    onOpenUrl: (url: String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val isScrolled by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        LazyColumn(
            state = lazyListState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarTop + 68.dp + 56.dp, // Espacio compensado para MiuixTopAppBar + InputField en bottomContent
                bottom = 120.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chips horizontales de filtrado rápido
            item {
                ActivityFilterChipsRow(
                    selectedFilter = uiState.selectedFilter,
                    totalCount = uiState.totalCount,
                    onFilterSelected = { viewModel.onFilterSelected(it) },
                    isMiuix = true
                )
            }

            // Contenido agrupado por fecha
            if (uiState.groupedActivities.isEmpty()) {
                item {
                    EmptyActivityPlaceholder(isMiuix = true)
                }
            } else {
                uiState.groupedActivities.forEach { (group, items) ->
                    item {
                        ActivityGroupHeader(
                            group = group,
                            count = items.size,
                            isMiuix = true
                        )
                    }

                    items(items, key = { it.id }) { log ->
                        MiuixActivityCard(
                            log = log,
                            onCardClick = {
                                handleItemClick(log, onNavigateToAssignment, onNavigateToForum, onOpenUrl)
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Material Design 3 Scaffold con TopAppBar y Buscador integrados en el Header
        val topAppBarContainerColor by androidx.compose.animation.animateColorAsState(
            targetValue = if (isScrolled) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
            label = "recentActivityMaterialTopBarColor"
        )

        Scaffold(
            topBar = {
                androidx.compose.material3.Surface(
                    color = topAppBarContainerColor,
                    shadowElevation = if (isScrolled) 2.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
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
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.recent_activity_title),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(Res.string.recent_activity_records_count, uiState.totalCount),
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

                        me.joxquin.notivas.ui.activity.components.ActivitySearchAndFilters(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            themeStyle = themeStyle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = modifier
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
                // Chips horizontales de filtrado rápido
                item {
                    ActivityFilterChipsRow(
                        selectedFilter = uiState.selectedFilter,
                        totalCount = uiState.totalCount,
                        onFilterSelected = { viewModel.onFilterSelected(it) },
                        isMiuix = false
                    )
                }

                // Contenido agrupado por fecha
                if (uiState.groupedActivities.isEmpty()) {
                    item {
                        EmptyActivityPlaceholder(isMiuix = false)
                    }
                } else {
                    uiState.groupedActivities.forEach { (group, items) ->
                        item {
                            ActivityGroupHeader(
                                group = group,
                                count = items.size,
                                isMiuix = false
                            )
                        }

                        items(items, key = { it.id }) { log ->
                            MaterialActivityCard(
                                log = log,
                                onCardClick = {
                                    handleItemClick(log, onNavigateToAssignment, onNavigateToForum, onOpenUrl)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleItemClick(
    log: ActivityLogEntity,
    onNavigateToAssignment: (Long) -> Unit,
    onNavigateToForum: (Long) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    when (log.type) {
        "ASSIGNMENT_SUBMISSION", "DRIVE_SYNC" -> {
            log.targetId?.let { onNavigateToAssignment(it) }
                ?: log.actionUrl?.let { onOpenUrl(it) }
        }
        "FORUM_POST" -> {
            log.targetId?.let { onNavigateToForum(it) }
                ?: log.actionUrl?.let { onOpenUrl(it) }
        }
        else -> {
            log.actionUrl?.let { onOpenUrl(it) }
        }
    }
}

@Composable
private fun ActivityFilterChipsRow(
    selectedFilter: ActivityFilter,
    totalCount: Int,
    onFilterSelected: (ActivityFilter) -> Unit,
    isMiuix: Boolean
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
                ActivityFilter.TODOS -> stringResource(Res.string.recent_activity_filter_all, totalCount)
                ActivityFilter.SUBMISSIONS -> stringResource(Res.string.recent_activity_filter_submissions)
                ActivityFilter.FORUMS -> stringResource(Res.string.recent_activity_filter_forums)
                ActivityFilter.NOTIFICATIONS -> stringResource(Res.string.recent_activity_filter_notifications)
                ActivityFilter.DRIVE -> stringResource(Res.string.recent_activity_filter_drive)
            }

            val bgColor = if (isMiuix) {
                if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainer
            } else {
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
            }

            val textColor = if (isMiuix) {
                if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary
            } else {
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun ActivityGroupHeader(
    group: ActivityTimeGroup,
    count: Int,
    isMiuix: Boolean
) {
    val title = when (group) {
        ActivityTimeGroup.HOY -> stringResource(Res.string.recent_activity_section_today)
        ActivityTimeGroup.AYER -> stringResource(Res.string.recent_activity_section_yesterday)
        ActivityTimeGroup.ESTA_SEMANA -> stringResource(Res.string.recent_activity_section_this_week)
        ActivityTimeGroup.ANTERIORES -> stringResource(Res.string.recent_activity_section_older)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val dotColor = if (isMiuix) {
                if (group == ActivityTimeGroup.HOY) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline
            } else {
                if (group == ActivityTimeGroup.HOY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = stringResource(Res.string.recent_activity_records_count, count),
            fontSize = 12.sp,
            color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiuixActivityCard(
    log: ActivityLogEntity,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onCardClick
    ) {
        ActivityCardContent(
            log = log,
            isMiuix = true,
            onCardClick = onCardClick
        )
    }
}

@Composable
private fun MaterialActivityCard(
    log: ActivityLogEntity,
    onCardClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            ActivityCardContent(
                log = log,
                isMiuix = false,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun ActivityCardContent(
    log: ActivityLogEntity,
    isMiuix: Boolean,
    onCardClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado del Card: Chip de Curso + Badge de Tipo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val courseText = log.courseCode ?: log.courseName ?: "General"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = courseText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                )
            }

            ActivityTypeBadge(type = log.type, isMiuix = isMiuix)
        }

        // Título y Descripción del Evento
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = log.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            Text(
                text = log.description,
                fontSize = 13.sp,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        // Pie de Card: Horas y Botón de Acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRelativeTime(log.timestamp),
                    fontSize = 12.sp,
                    color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón de Acción contextual
            ActivityActionButton(
                type = log.type,
                isMiuix = isMiuix,
                onClick = onCardClick
            )
        }
    }
}

@Composable
private fun ActivityTypeBadge(
    type: String,
    isMiuix: Boolean
) {
    val (label, icon, containerColor, contentColor) = when (type) {
        "ASSIGNMENT_SUBMISSION" -> {
            val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.secondaryContainer
            val text = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
            Quad("Entrega Canvas ✓", Icons.Default.CheckCircle, bg, text)
        }
        "FORUM_POST" -> {
            val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.secondaryContainer
            val text = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
            Quad("Foro participado 💬", Icons.Default.Forum, bg, text)
        }
        "CANVAS_NOTIFICATION" -> {
            val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.tertiaryContainer
            val text = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer
            Quad("Notificación Canvas 🔔", Icons.Default.NotificationsActive, bg, text)
        }
        "DRIVE_SYNC" -> {
            val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer
            val text = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
            Quad("Drive Sync ☁", Icons.Default.CloudSync, bg, text)
        }
        else -> {
            val bg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHigh
            val text = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            Quad("Actividad", Icons.Default.Star, bg, text)
        }
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = contentColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun ActivityActionButton(
    type: String,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    val (label, icon) = when (type) {
        "ASSIGNMENT_SUBMISSION" -> stringResource(Res.string.recent_activity_action_view_receipt) to Icons.AutoMirrored.Filled.ArrowForward
        "FORUM_POST" -> stringResource(Res.string.recent_activity_action_view_thread) to Icons.AutoMirrored.Filled.ArrowForward
        else -> stringResource(Res.string.recent_activity_action_open_document) to Icons.AutoMirrored.Outlined.OpenInNew
    }

    val containerColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val contentColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = contentColor
        )
    }
}

@Composable
private fun EmptyActivityPlaceholder(isMiuix: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(Res.string.recent_activity_empty_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(Res.string.recent_activity_empty_desc),
            fontSize = 13.sp,
            color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Reciente"
    val now = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)
    val mins = diff / (60 * 1000)
    val hours = diff / (3600 * 1000)
    val days = diff / (24 * 3600 * 1000)

    return when {
        mins < 5 -> "Hace un momento"
        mins < 60 -> "Hace $mins min"
        hours < 24 -> "Hoy, hace $hours h"
        days == 1L -> "Ayer"
        days < 7 -> "Hace $days días"
        else -> "Hace más de 1 semana"
    }
}
