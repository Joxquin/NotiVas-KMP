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
import androidx.compose.ui.Modifier
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.herramientas.material.HerramientasMaterialContent
import me.joxquin.notivas.ui.herramientas.miuix.HerramientasMiuixContent
import me.joxquin.notivas.ui.notas.NotasScreen
import me.joxquin.notivas.ui.notas.NotasViewModel

/**
 * Hub de Herramientas & Simulador de Notas.
 * Soporta de manera reactiva tanto Material Design 3 Monet como HyperOS Miuix.
 */
@Composable
fun HerramientasScreen(
    viewModel: NotasViewModel? = null,
    showingNotasDetail: Boolean = false,
    onOpenProgreso: () -> Unit = {},
    onOpenTuneSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = showingNotasDetail,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "herramientasSubNavTransition",
        modifier = modifier
    ) { isDetail ->
        if (isDetail && viewModel != null) {
            NotasScreen(
                viewModel = viewModel,
                themeStyle = themeStyle,
                lazyListState = lazyListState,
                onBack = onBack
            )
        } else {
            if (themeStyle == AppThemeStyle.MIUIX) {
                HerramientasMiuixContent(
                    viewModel = viewModel,
                    onOpenProgreso = onOpenProgreso,
                    onOpenTuneSettings = onOpenTuneSettings,
                    lazyListState = lazyListState
                )
            } else {
                HerramientasMaterialContent(
                    viewModel = viewModel,
                    onOpenProgreso = onOpenProgreso,
                    onOpenTuneSettings = onOpenTuneSettings,
                    lazyListState = lazyListState
                )
            }
        }
    }
}
