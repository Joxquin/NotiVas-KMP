package me.joxquin.notivas.ui.herramientas.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.archive_action_empty_trash
import me.joxquin.notivas.shared.resources.archive_action_export_pdf
import me.joxquin.notivas.shared.resources.archive_action_restore_pending
import me.joxquin.notivas.shared.resources.archive_action_rubric
import me.joxquin.notivas.shared.resources.archive_action_view_submission
import me.joxquin.notivas.shared.resources.archive_auto_archived
import me.joxquin.notivas.shared.resources.archive_copilot_assisted
import me.joxquin.notivas.shared.resources.archive_discarded_date
import me.joxquin.notivas.shared.resources.archive_discarded_hint
import me.joxquin.notivas.shared.resources.archive_empty_discarded
import me.joxquin.notivas.shared.resources.archive_empty_expired
import me.joxquin.notivas.shared.resources.archive_empty_submitted
import me.joxquin.notivas.shared.resources.archive_graded_badge
import me.joxquin.notivas.shared.resources.archive_graded_by
import me.joxquin.notivas.shared.resources.archive_items_count
import me.joxquin.notivas.shared.resources.archive_max_score
import me.joxquin.notivas.shared.resources.archive_screen_title
import me.joxquin.notivas.shared.resources.archive_search_placeholder
import me.joxquin.notivas.shared.resources.archive_section_recents
import me.joxquin.notivas.shared.resources.archive_semester_tag
import me.joxquin.notivas.shared.resources.archive_stat_average
import me.joxquin.notivas.shared.resources.archive_stat_average_sub
import me.joxquin.notivas.shared.resources.archive_stat_discarded
import me.joxquin.notivas.shared.resources.archive_stat_discarded_sub
import me.joxquin.notivas.shared.resources.archive_stat_ontime
import me.joxquin.notivas.shared.resources.archive_stat_ontime_rate
import me.joxquin.notivas.shared.resources.archive_storage_card_desc
import me.joxquin.notivas.shared.resources.archive_storage_card_title
import me.joxquin.notivas.shared.resources.archive_summary_card_title
import me.joxquin.notivas.shared.resources.archive_sync_banner
import me.joxquin.notivas.shared.resources.archive_tab_discarded
import me.joxquin.notivas.shared.resources.archive_tab_expired
import me.joxquin.notivas.shared.resources.archive_tab_submitted
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryArchiveScreen(
    viewModel: ArchiveViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onBack: () -> Unit = {},
    onOpenAssignmentDetail: ((Assignment) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    if (isMiuix) {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = statusBarTop + 68.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── 1. Search & Tab Filter Track ───────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Miuix Native InputField
                        ArchiveSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onClear = { viewModel.clearSearch() },
                            placeholder = stringResource(Res.string.archive_search_placeholder),
                            isMiuix = true
                        )

                        // Segmented Tabs Pill Slider
                        ArchiveSegmentedPillTrack(
                            activeTab = uiState.activeTab,
                            submittedCount = uiState.stats.submittedCount,
                            discardedCount = uiState.stats.discardedCount,
                            expiredCount = uiState.stats.expiredCount,
                            isMiuix = true,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                }

                // ─── 2. Resumen & Analítica Micro-Stats Card ───────────────────
                item {
                    ArchiveSummaryCard(
                        stats = uiState.stats,
                        isMiuix = true
                    )
                }

                // ─── 3. Header de Sección ──────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallTitle(
                            text = stringResource(Res.string.archive_section_recents),
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            TextAdaptive(
                                text = stringResource(Res.string.archive_items_count, uiState.items.size),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                isMiuix = true
                            )
                        }
                    }
                }

                // ─── 4. Lista de Items de Archivo ──────────────────────────────
                if (uiState.items.isEmpty()) {
                    item {
                        val emptyMsg = when (uiState.activeTab) {
                            ArchiveTab.SUBMITTED -> stringResource(Res.string.archive_empty_submitted)
                            ArchiveTab.DISCARDED -> stringResource(Res.string.archive_empty_discarded)
                            ArchiveTab.EXPIRED -> stringResource(Res.string.archive_empty_expired)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextAdaptive(
                                text = emptyMsg,
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                isMiuix = true
                            )
                        }
                    }
                } else {
                    items(uiState.items, key = { it.id }) { item ->
                        when (item) {
                            is ArchiveItem.AssignmentItem -> {
                                AssignmentArchiveCard(
                                    item = item,
                                    isMiuix = true,
                                    onOpenAssignment = {
                                        onOpenAssignmentDetail?.invoke(item.assignment)
                                    },
                                    onOpenUrl = { url ->
                                        try {
                                            uriHandler.openUri(url)
                                        } catch (_: Exception) {
                                        }
                                    },
                                    onRestoreToPending = {
                                        viewModel.restoreAssignmentToPending(item.assignment.id)
                                    }
                                )
                            }

                            is ArchiveItem.ForumItem -> {
                                ForumArchiveCard(
                                    item = item,
                                    isMiuix = true
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ─── MATERIAL DESIGN 3 (SCAFFOLD CON TOP APP BAR) ─────────────────────
        Scaffold(
            topBar = {
                androidx.compose.material3.TopAppBar(
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
                                M3Text(
                                    text = stringResource(Res.string.archive_screen_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
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
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── 1. Search & Tab Filter Track ───────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ArchiveSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onClear = { viewModel.clearSearch() },
                            placeholder = stringResource(Res.string.archive_search_placeholder),
                            isMiuix = false
                        )

                        ArchiveSegmentedPillTrack(
                            activeTab = uiState.activeTab,
                            submittedCount = uiState.stats.submittedCount,
                            discardedCount = uiState.stats.discardedCount,
                            expiredCount = uiState.stats.expiredCount,
                            isMiuix = false,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                }

                // ─── 2. Resumen & Analítica Micro-Stats Card ───────────────────
                item {
                    ArchiveSummaryCard(
                        stats = uiState.stats,
                        isMiuix = false
                    )
                }

                // ─── 3. Header de Sección ──────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextAdaptive(
                            text = stringResource(Res.string.archive_section_recents),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            isMiuix = false
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            TextAdaptive(
                                text = stringResource(Res.string.archive_items_count, uiState.items.size),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                isMiuix = false
                            )
                        }
                    }
                }

                // ─── 4. Lista de Items de Archivo ──────────────────────────────
                if (uiState.items.isEmpty()) {
                    item {
                        val emptyMsg = when (uiState.activeTab) {
                            ArchiveTab.SUBMITTED -> stringResource(Res.string.archive_empty_submitted)
                            ArchiveTab.DISCARDED -> stringResource(Res.string.archive_empty_discarded)
                            ArchiveTab.EXPIRED -> stringResource(Res.string.archive_empty_expired)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextAdaptive(
                                text = emptyMsg,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline,
                                isMiuix = false
                            )
                        }
                    }
                } else {
                    items(uiState.items, key = { it.id }) { item ->
                        when (item) {
                            is ArchiveItem.AssignmentItem -> {
                                AssignmentArchiveCard(
                                    item = item,
                                    isMiuix = false,
                                    onOpenAssignment = {
                                        onOpenAssignmentDetail?.invoke(item.assignment)
                                    },
                                    onOpenUrl = { url ->
                                        try {
                                            uriHandler.openUri(url)
                                        } catch (_: Exception) {
                                        }
                                    },
                                    onRestoreToPending = {
                                        viewModel.restoreAssignmentToPending(item.assignment.id)
                                    }
                                )
                            }

                            is ArchiveItem.ForumItem -> {
                                ForumArchiveCard(
                                    item = item,
                                    isMiuix = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Search Bar ─────────────────────────────────────────────────────────────
@Composable
private fun ArchiveSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    isMiuix: Boolean
) {
    if (isMiuix) {
        InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onQueryChange,
            expanded = false,
            onExpandedChange = {},
            label = placeholder,
            modifier = Modifier.fillMaxWidth(),
            color = MiuixTheme.colorScheme.surfaceContainerHigh
        )
    } else {
        val barColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val textColor = MaterialTheme.colorScheme.onSurface
        val hintColor = MaterialTheme.colorScheme.outline

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(barColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = hintColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        TextAdaptive(
                            text = placeholder,
                            fontSize = 13.sp,
                            color = hintColor,
                            isMiuix = false
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = textColor
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = hintColor,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onClear)
                    )
                }
            }
        }
    }
}

// ─── Segmented Tabs Pill Slider ─────────────────────────────────────────────
@Composable
private fun ArchiveSegmentedPillTrack(
    activeTab: ArchiveTab,
    submittedCount: Int,
    discardedCount: Int,
    expiredCount: Int,
    isMiuix: Boolean,
    onTabSelected: (ArchiveTab) -> Unit
) {
    val trackBg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(trackBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ArchiveTabPill(
            title = stringResource(Res.string.archive_tab_submitted),
            count = submittedCount,
            isSelected = activeTab == ArchiveTab.SUBMITTED,
            isMiuix = isMiuix,
            onClick = { onTabSelected(ArchiveTab.SUBMITTED) },
            modifier = Modifier.weight(1f)
        )
        ArchiveTabPill(
            title = stringResource(Res.string.archive_tab_discarded),
            count = discardedCount,
            isSelected = activeTab == ArchiveTab.DISCARDED,
            isMiuix = isMiuix,
            onClick = { onTabSelected(ArchiveTab.DISCARDED) },
            modifier = Modifier.weight(1f)
        )
        ArchiveTabPill(
            title = stringResource(Res.string.archive_tab_expired),
            count = expiredCount,
            isSelected = activeTab == ArchiveTab.EXPIRED,
            isMiuix = isMiuix,
            onClick = { onTabSelected(ArchiveTab.EXPIRED) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ArchiveTabPill(
    title: String,
    count: Int,
    isSelected: Boolean,
    isMiuix: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBg = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val activeText = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val inactiveText =
        if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TextAdaptive(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) activeText else inactiveText,
                isMiuix = isMiuix
            )
            val badgeBg = if (isSelected) {
                if (isMiuix) MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.25f
                )
            } else {
                if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                TextAdaptive(
                    text = count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeText else inactiveText,
                    isMiuix = isMiuix
                )
            }
        }
    }
}

// ─── Summary Card ───────────────────────────────────────────────────────────
@Composable
private fun ArchiveSummaryCard(
    stats: ArchiveSummaryStats,
    isMiuix: Boolean
) {
    if (isMiuix) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.None
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top Header & Semester Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.archive_summary_card_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.archive_semester_tag),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }

                // 3 Micro-stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stat 1: A tiempo
                    ArchiveMicroStat(
                        value = stats.onTimeCount.toString(),
                        icon = Icons.Default.TaskAlt,
                        title = stringResource(Res.string.archive_stat_ontime),
                        subtitle = stringResource(Res.string.archive_stat_ontime_rate, stats.onTimePercentage),
                        primaryColor = MiuixTheme.colorScheme.primary,
                        isMiuix = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 2: Descartadas
                    ArchiveMicroStat(
                        value = stats.discardedCount.toString(),
                        icon = Icons.Default.VisibilityOff,
                        title = stringResource(Res.string.archive_stat_discarded),
                        subtitle = stringResource(Res.string.archive_stat_discarded_sub),
                        primaryColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        isMiuix = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 3: Promedio
                    ArchiveMicroStat(
                        value = stats.averageScore.toString(),
                        icon = Icons.Default.Grade,
                        title = stringResource(Res.string.archive_stat_average),
                        subtitle = stringResource(Res.string.archive_stat_average_sub, "20.0"),
                        primaryColor = MiuixTheme.colorScheme.primary,
                        isMiuix = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sync Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    MiuixText(
                        text = stringResource(Res.string.archive_sync_banner, stats.totalLocalRecords),
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.primary)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top Header & Semester Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        M3Text(
                            text = stringResource(Res.string.archive_summary_card_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        M3Text(
                            text = stringResource(Res.string.archive_semester_tag),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 3 Micro-stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stat 1: A tiempo
                    ArchiveMicroStat(
                        value = stats.onTimeCount.toString(),
                        icon = Icons.Default.TaskAlt,
                        title = stringResource(Res.string.archive_stat_ontime),
                        subtitle = stringResource(Res.string.archive_stat_ontime_rate, stats.onTimePercentage),
                        primaryColor = MaterialTheme.colorScheme.primary,
                        isMiuix = false,
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 2: Descartadas
                    ArchiveMicroStat(
                        value = stats.discardedCount.toString(),
                        icon = Icons.Default.VisibilityOff,
                        title = stringResource(Res.string.archive_stat_discarded),
                        subtitle = stringResource(Res.string.archive_stat_discarded_sub),
                        primaryColor = MaterialTheme.colorScheme.secondary,
                        isMiuix = false,
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 3: Promedio
                    ArchiveMicroStat(
                        value = stats.averageScore.toString(),
                        icon = Icons.Default.Grade,
                        title = stringResource(Res.string.archive_stat_average),
                        subtitle = stringResource(Res.string.archive_stat_average_sub, "20.0"),
                        primaryColor = MaterialTheme.colorScheme.primary,
                        isMiuix = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sync Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    M3Text(
                        text = stringResource(Res.string.archive_sync_banner, stats.totalLocalRecords),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveMicroStat(
    value: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    primaryColor: Color,
    isMiuix: Boolean,
    modifier: Modifier = Modifier
) {
    val statBg =
        if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(statBg)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextAdaptive(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    isMiuix = isMiuix
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            TextAdaptive(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                isMiuix = isMiuix
            )
            TextAdaptive(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.outline,
                isMiuix = isMiuix
            )
        }
    }
}

// ─── Assignment Card ────────────────────────────────────────────────────────
@Composable
private fun AssignmentArchiveCard(
    item: ArchiveItem.AssignmentItem,
    isMiuix: Boolean,
    onOpenAssignment: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRestoreToPending: () -> Unit
) {
    if (isMiuix) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onOpenAssignment
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Course Tag & Status Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Course pill
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        MiuixText(
                            text = "${item.courseCode} • ${item.courseName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Date badge
                    val dateFormatted = item.timestamp?.let { DateTimeUtil.parseIsoToComponents(it) }?.let {
                        "${it.date.day}/${it.date.month} ${it.hour.toString().padStart(2, '0')}:${
                            it.minute.toString().padStart(2, '0')
                        }"
                    } ?: "Finalizado"

                    if (item.isDiscarded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(12.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.archive_discarded_date, dateFormatted),
                                fontSize = 10.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            MiuixText(
                                text = dateFormatted,
                                fontSize = 10.sp,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Title & Score
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiuixText(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface
                    )

                    if (item.isDiscarded) {
                        MiuixText(
                            text = stringResource(Res.string.archive_discarded_hint),
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val score = item.assignment.score ?: item.assignment.submission?.score
                            val maxPoints = item.assignment.pointsPossible ?: 20.0
                            if (score != null) {
                                MiuixText(
                                    text = "$score / $maxPoints pts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.primary
                                )
                                val isMax = score >= maxPoints && maxPoints > 0
                                MiuixText(
                                    text = if (isMax) stringResource(Res.string.archive_max_score) else stringResource(
                                        Res.string.archive_graded_badge
                                    ),
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            } else {
                                MiuixText(
                                    text = stringResource(Res.string.archive_graded_badge),
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Linked Artifact / Actions Bar
                if (item.isDiscarded) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(13.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.archive_auto_archived),
                                fontSize = 11.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRestoreToPending)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.archive_action_restore_pending),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    val artifactName = item.draft?.fileName ?: "${item.title.take(20)}.docx"
                    val artifactUrl = item.draft?.driveUrl ?: item.assignment.htmlUrl

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            MiuixText(
                                text = artifactName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }

                        if (!artifactUrl.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.surfaceContainerHighest)
                                    .clickable { onOpenUrl(artifactUrl) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(13.dp)
                                )
                                MiuixText(
                                    text = stringResource(Res.string.archive_action_view_submission),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(onClick = onOpenAssignment)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Course Tag & Status Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Course pill
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        M3Text(
                            text = "${item.courseCode} • ${item.courseName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Date badge
                    val dateFormatted = item.timestamp?.let { DateTimeUtil.parseIsoToComponents(it) }?.let {
                        "${it.date.day}/${it.date.month} ${it.hour.toString().padStart(2, '0')}:${
                            it.minute.toString().padStart(2, '0')
                        }"
                    } ?: "Finalizado"

                    if (item.isDiscarded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                            M3Text(
                                text = stringResource(Res.string.archive_discarded_date, dateFormatted),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            M3Text(
                                text = dateFormatted,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Title & Score
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    M3Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.isDiscarded) {
                        M3Text(
                            text = stringResource(Res.string.archive_discarded_hint),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val score = item.assignment.score ?: item.assignment.submission?.score
                            val maxPoints = item.assignment.pointsPossible ?: 20.0
                            if (score != null) {
                                M3Text(
                                    text = "$score / $maxPoints pts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val isMax = score >= maxPoints && maxPoints > 0
                                M3Text(
                                    text = if (isMax) stringResource(Res.string.archive_max_score) else stringResource(
                                        Res.string.archive_graded_badge
                                    ),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                M3Text(
                                    text = stringResource(Res.string.archive_graded_badge),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Linked Artifact / Actions Bar
                if (item.isDiscarded) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(13.dp)
                            )
                            M3Text(
                                text = stringResource(Res.string.archive_auto_archived),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRestoreToPending)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            M3Text(
                                text = stringResource(Res.string.archive_action_restore_pending),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    val artifactName = item.draft?.fileName ?: "${item.title.take(20)}.docx"
                    val artifactUrl = item.draft?.driveUrl ?: item.assignment.htmlUrl

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            M3Text(
                                text = artifactName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!artifactUrl.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onOpenUrl(artifactUrl) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(13.dp)
                                )
                                M3Text(
                                    text = stringResource(Res.string.archive_action_view_submission),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Forum Card ─────────────────────────────────────────────────────────────
@Composable
private fun ForumArchiveCard(
    item: ArchiveItem.ForumItem,
    isMiuix: Boolean
) {
    if (isMiuix) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.Sink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        MiuixText(
                            text = "${item.courseCode} • ${item.courseName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        MiuixText(
                            text = item.timestamp?.take(10) ?: "",
                            fontSize = 10.sp,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }

                MiuixText(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )

                // Copilot Badge
                if (item.isCopilotAssisted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.archive_copilot_assisted),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        M3Text(
                            text = "${item.courseCode} • ${item.courseName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        M3Text(
                            text = item.timestamp?.take(10) ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                M3Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Copilot Badge
                if (item.isCopilotAssisted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        M3Text(
                            text = stringResource(Res.string.archive_copilot_assisted),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}


// ─── Adaptive Text Helper ───────────────────────────────────────────────────
@Composable
private fun TextAdaptive(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    isMiuix: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isMiuix) {
        val defaultColor = if (color == Color.Unspecified) MiuixTheme.colorScheme.onSurface else color
        MiuixText(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = defaultColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    } else {
        val defaultColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
        M3Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = defaultColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
}

