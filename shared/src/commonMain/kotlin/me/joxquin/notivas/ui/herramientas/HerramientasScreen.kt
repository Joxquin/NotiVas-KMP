package me.joxquin.notivas.ui.herramientas

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
import me.joxquin.notivas.ui.herramientas.material.HerramientasMaterialContent
import me.joxquin.notivas.ui.herramientas.miuix.HerramientasMiuixContent
import me.joxquin.notivas.ui.notas.NotasScreen
import me.joxquin.notivas.ui.notas.NotasViewModel

import me.joxquin.notivas.ui.foros.ForosScreen
import me.joxquin.notivas.ui.foros.ForosViewModel

enum class HerramientasSubDestination {
    HUB,
    PROGRESO_DETAIL,
    FOROS_DETAIL
}

/**
 * Hub de Herramientas, Simulador de Notas y Foros Académicos.
 * Soporta de manera reactiva tanto Material Design 3 Monet como HyperOS Miuix.
 */
@Composable
fun HerramientasScreen(
    viewModel: NotasViewModel? = null,
    forosViewModel: ForosViewModel? = null,
    showingNotasDetail: Boolean = false,
    showingForosDetail: Boolean = false,
    onOpenProgreso: () -> Unit = {},
    onOpenForos: () -> Unit = {},
    onOpenTuneSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val currentSubDestination = when {
        showingForosDetail -> HerramientasSubDestination.FOROS_DETAIL
        showingNotasDetail -> HerramientasSubDestination.PROGRESO_DETAIL
        else -> HerramientasSubDestination.HUB
    }

    AnimatedContent(
        targetState = currentSubDestination,
        transitionSpec = {
            if (targetState != HerramientasSubDestination.HUB) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "herramientasSubNavTransition",
        modifier = modifier
    ) { destination ->
        when (destination) {
            HerramientasSubDestination.PROGRESO_DETAIL -> {
                if (viewModel != null) {
                    NotasScreen(
                        viewModel = viewModel,
                        themeStyle = themeStyle,
                        lazyListState = lazyListState,
                        onBack = onBack
                    )
                }
            }
            HerramientasSubDestination.FOROS_DETAIL -> {
                if (forosViewModel != null) {
                    val forosUiState by forosViewModel.uiState.collectAsState()
                    if (forosUiState.selectedDiscussion != null) {
                        me.joxquin.notivas.ui.foros.detail.ForoDetailScreen(
                            viewModel = forosViewModel,
                            themeStyle = themeStyle,
                            lazyListState = lazyListState,
                            onBack = { forosViewModel.clearSelectedDiscussion() }
                        )
                    } else {
                        ForosScreen(
                            viewModel = forosViewModel,
                            themeStyle = themeStyle,
                            lazyListState = lazyListState,
                            onBack = onBack
                        )
                    }
                }
            }
            HerramientasSubDestination.HUB -> {
                if (themeStyle == AppThemeStyle.MIUIX) {
                    HerramientasMiuixContent(
                        viewModel = viewModel,
                        onOpenProgreso = onOpenProgreso,
                        onOpenForos = onOpenForos,
                        onOpenTuneSettings = onOpenTuneSettings,
                        lazyListState = lazyListState
                    )
                } else {
                    HerramientasMaterialContent(
                        viewModel = viewModel,
                        onOpenProgreso = onOpenProgreso,
                        onOpenForos = onOpenForos,
                        onOpenTuneSettings = onOpenTuneSettings,
                        lazyListState = lazyListState
                    )
                }
            }
        }
    }
}
