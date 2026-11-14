package me.joxquin.notivas.ui.utilidades

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.utilidades.material.UtilidadesMaterialContent
import me.joxquin.notivas.ui.utilidades.miuix.UtilidadesMiuixContent
import me.joxquin.notivas.ui.notas.NotasScreen
import me.joxquin.notivas.ui.notas.NotasViewModel

import me.joxquin.notivas.ui.foros.ForosScreen
import me.joxquin.notivas.ui.foros.ForosViewModel
import me.joxquin.notivas.ui.utilidades.archive.ArchiveViewModel
import me.joxquin.notivas.ui.utilidades.archive.TaskHistoryArchiveScreen

enum class UtilidadesSubDestination {
    HUB,
    PROGRESO_DETAIL,
    FOROS_LIST,
    FORO_DETAIL,
    ARCHIVE
}

/**
 * Hub de Utilidades, Simulador de Notas y Foros Académicos.
 * Soporta de manera reactiva tanto Material Design 3 Monet como HyperOS Miuix.
 */
@Composable
fun UtilidadesScreen(
    viewModel: NotasViewModel? = null,
    forosViewModel: ForosViewModel? = null,
    archiveViewModel: ArchiveViewModel? = null,
    showingNotasDetail: Boolean = false,
    showingForosDetail: Boolean = false,
    showingArchiveDetail: Boolean = false,
    onOpenProgreso: () -> Unit = {},
    onOpenForos: () -> Unit = {},
    onOpenForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
    onOpenArchive: () -> Unit = {},
    onOpenStudyHub: () -> Unit = {},
    studyDueCount: Int = 0,
    studyTotalCount: Int = 0,
    onOpenAssignmentDetail: ((me.joxquin.notivas.data.model.Assignment) -> Unit)? = null,
    onOpenGroupDetail: ((Long, Long) -> Unit)? = null,
    onOpenTuneSettings: () -> Unit = {},
    onOpenCatalogImport: () -> Unit = {},
    onImportJsonClick: () -> Unit = {},
    onExportJsonClick: () -> Unit = {},
    onResetGroups: () -> Unit = {},
    onBack: () -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val forosUiState = forosViewModel?.uiState?.collectAsState()?.value
    val currentSubDestination = when {
        showingArchiveDetail -> UtilidadesSubDestination.ARCHIVE
        showingForosDetail && forosUiState?.selectedDiscussion != null -> UtilidadesSubDestination.FORO_DETAIL
        showingForosDetail -> UtilidadesSubDestination.FOROS_LIST
        showingNotasDetail -> UtilidadesSubDestination.PROGRESO_DETAIL
        else -> UtilidadesSubDestination.HUB
    }

    val handleOpenForoDetail: (me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit = { topic ->
        if (onOpenForoDetail != null) {
            onOpenForoDetail(topic)
        } else if (forosViewModel != null) {
            forosViewModel.selectDiscussion(topic)
            onOpenForos()
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        UtilidadesMiuixContent(
            viewModel = viewModel,
            forosViewModel = forosViewModel,
            onOpenProgreso = onOpenProgreso,
            onOpenForos = onOpenForos,
            onOpenForoDetail = handleOpenForoDetail,
            onOpenArchive = onOpenArchive,
            onOpenStudyHub = onOpenStudyHub,
            studyDueCount = studyDueCount,
            studyTotalCount = studyTotalCount,
            onOpenTuneSettings = onOpenTuneSettings,
            lazyListState = lazyListState,
            modifier = modifier
        )
        return
    }

    AnimatedContent(
        targetState = currentSubDestination,
        transitionSpec = {
            when {
                // Navegando a detalle de foro desde la lista de foros
                initialState == UtilidadesSubDestination.FOROS_LIST && targetState == UtilidadesSubDestination.FORO_DETAIL -> {
                    (slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it / 3 } +
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        )
                }
                // Regresando del detalle de foro a la lista de foros
                initialState == UtilidadesSubDestination.FORO_DETAIL && targetState == UtilidadesSubDestination.FOROS_LIST -> {
                    (slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it / 3 } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it } +
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        )
                }
                // Entrando desde el HUB hacia alguna pantalla interna
                targetState != UtilidadesSubDestination.HUB -> {
                    (slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it / 3 } +
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        )
                }
                // Regresando al HUB
                else -> {
                    (slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it / 3 } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it } +
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        )
                }
            }
        },
        label = "utilidadesSubNavTransition",
        modifier = modifier
    ) { destination ->
        when (destination) {
            UtilidadesSubDestination.PROGRESO_DETAIL -> {
                if (viewModel != null) {
                    NotasScreen(
                        viewModel = viewModel,
                        themeStyle = themeStyle,
                        lazyListState = lazyListState,
                        onOpenGroupDetail = { gid, cid -> onOpenGroupDetail?.invoke(gid, cid) },
                        onBack = onBack
                    )
                }
            }
            UtilidadesSubDestination.FOROS_LIST -> {
                if (forosViewModel != null) {
                    ForosScreen(
                        viewModel = forosViewModel,
                        themeStyle = themeStyle,
                        lazyListState = lazyListState,
                        onBack = onBack
                    )
                }
            }
            UtilidadesSubDestination.FORO_DETAIL -> {
                if (forosViewModel != null) {
                    me.joxquin.notivas.ui.foros.detail.ForoDetailScreen(
                        viewModel = forosViewModel,
                        themeStyle = themeStyle,
                        lazyListState = lazyListState,
                        onBack = { forosViewModel.clearSelectedDiscussion() }
                    )
                }
            }
            UtilidadesSubDestination.ARCHIVE -> {
                if (archiveViewModel != null) {
                    TaskHistoryArchiveScreen(
                        viewModel = archiveViewModel,
                        themeStyle = themeStyle,
                        lazyListState = lazyListState,
                        onBack = onBack,
                        onOpenAssignmentDetail = onOpenAssignmentDetail
                    )
                }
            }
            UtilidadesSubDestination.HUB -> {
                if (themeStyle == AppThemeStyle.MIUIX) {
                    UtilidadesMiuixContent(
                        viewModel = viewModel,
                        forosViewModel = forosViewModel,
                        onOpenProgreso = onOpenProgreso,
                        onOpenForos = onOpenForos,
                        onOpenForoDetail = handleOpenForoDetail,
                        onOpenArchive = onOpenArchive,
                        onOpenTuneSettings = onOpenTuneSettings,
                        lazyListState = lazyListState
                    )
                } else {
                    UtilidadesMaterialContent(
                        viewModel = viewModel,
                        forosViewModel = forosViewModel,
                        onOpenProgreso = onOpenProgreso,
                        onOpenForos = onOpenForos,
                        onOpenForoDetail = handleOpenForoDetail,
                        onOpenArchive = onOpenArchive,
                        onOpenStudyHub = onOpenStudyHub,
                        studyDueCount = studyDueCount,
                        studyTotalCount = studyTotalCount,
                        onOpenTuneSettings = onOpenTuneSettings,
                        onOpenCatalogImport = onOpenCatalogImport,
                        onImportJsonClick = onImportJsonClick,
                        onExportJsonClick = onExportJsonClick,
                        onResetGroups = onResetGroups,
                        lazyListState = lazyListState
                    )
                }
            }
        }
    }
}
