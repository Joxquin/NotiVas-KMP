package me.joxquin.notivas.ui.herramientas.miuix

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.herramientas_title
import me.joxquin.notivas.shared.resources.herramientas_subtitle
import me.joxquin.notivas.shared.resources.herramientas_modules_header

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.joxquin.notivas.ui.foros.ForosViewModel
import me.joxquin.notivas.ui.notas.NotasViewModel

import androidx.compose.ui.input.nestedscroll.nestedScroll
import me.joxquin.notivas.ui.components.miuix.rememberMiuixAutoSettleScrollConnection

@Composable
fun HerramientasMiuixContent(
    viewModel: NotasViewModel? = null,
    forosViewModel: ForosViewModel? = null,
    onOpenProgreso: () -> Unit = {},
    onOpenForos: () -> Unit = {},
    onOpenForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
    onOpenArchive: () -> Unit = {},
    onOpenTuneSettings: () -> Unit = {},
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(null) })
    val forosUiState by (forosViewModel?.uiState?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(null) })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
        val contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp,
            bottom = if (isWideScreen) 24.dp else 120.dp
        )

        val autoSettleConnection = rememberMiuixAutoSettleScrollConnection(
            listState = lazyListState,
            thresholdDp = 72.dp
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(autoSettleConnection),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ─── LARGE TITLE CANÓNICO HYPEROS MIUIX (32.sp) ─────────
            item {
                val firstIndex = lazyListState.firstVisibleItemIndex
                val firstOffset = lazyListState.firstVisibleItemScrollOffset
                val isHeaderCollapsed = firstIndex > 0 || firstOffset >= 72

                val largeTitleAlpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isHeaderCollapsed) 0f else 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                    label = "LargeTitleAlpha"
                )
                val largeTitleTranslationY by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isHeaderCollapsed) -20f else 0f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                    label = "LargeTitleTranslationY"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = largeTitleAlpha
                            translationY = largeTitleTranslationY
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.herramientas_title),
                            fontSize = MiuixTheme.textStyles.title1.fontSize,
                            fontWeight = FontWeight.Normal,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.herramientas_subtitle),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }

            item {
                SmallTitle(
                    text = stringResource(Res.string.herramientas_modules_header),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ─── CARD 1: Hero Bento Progreso & Notas ─────────────────
            item {
                PlannerHeroBentoCard(
                    uiState = uiState,
                    onClick = onOpenProgreso
                )
            }

            // ─── CARD 2: Simulador What-If ──────────────────────────
            item {
                WhatIfBentoCard(
                    onClick = onOpenProgreso
                )
            }

            // ─── CARD 3: Foros de Discusión ─────────────────────────
            item {
                ForosBentoCard(
                    forosUiState = forosUiState,
                    onClick = onOpenForos,
                    onOpenTopic = onOpenForoDetail
                )
            }


            // ─── CARD 3: Historial & Archivo de Tareas ──────────────
            item {
                ArchiveBentoCard(onClick = onOpenArchive)
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }

        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(lazyListState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            trackPadding = contentPadding
        )
    }
}
