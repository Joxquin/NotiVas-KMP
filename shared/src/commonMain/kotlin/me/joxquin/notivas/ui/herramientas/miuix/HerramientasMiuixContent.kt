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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.joxquin.notivas.ui.notas.NotasViewModel

@Composable
fun HerramientasMiuixContent(
    viewModel: NotasViewModel? = null,
    onOpenProgreso: () -> Unit = {},
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(null) })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ─── LARGE TITLE CANÓNICO HYPEROS MIUIX (32.sp) ─────────
            item {
                val firstIndex = lazyListState.firstVisibleItemIndex
                val firstOffset = lazyListState.firstVisibleItemScrollOffset
                val largeTitleAlpha = if (firstIndex == 0) {
                    (1f - (firstOffset / 90f)).coerceIn(0f, 1f)
                } else {
                    0f
                }
                val largeTitleTranslationY = if (firstIndex == 0) {
                    -firstOffset * 0.25f
                } else {
                    -25f
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .graphicsLayer {
                            alpha = largeTitleAlpha
                            translationY = largeTitleTranslationY
                        }
                ) {
                    Text(
                        text = "Herramientas",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                        fontWeight = FontWeight.Normal,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Utilidades y módulos NotiVas",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            item {
                SmallTitle(
                    text = "MÓDULOS ACTIVOS",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ─── CARD 1: Hero Bento Progreso & Notas ─────────────────
            item {
                PlannerHeroBentoCard(
                    uiState = uiState,
                    onClick = onOpenProgreso
                )
            }

            // ─── ROW ASIMÉTRICO: What-If & Fórmulas (2 Columnas) ────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WhatIfBentoCard(
                        modifier = Modifier.weight(1f)
                    )
                    FormulasBentoCard(
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // ─── CARD 3: Historial & Archivo de Tareas ──────────────
            item {
                ArchiveBentoCard()
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
