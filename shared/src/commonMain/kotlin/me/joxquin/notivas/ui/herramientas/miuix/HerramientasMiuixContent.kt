package me.joxquin.notivas.ui.herramientas.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.foros.ForosViewModel
import me.joxquin.notivas.ui.notas.NotasViewModel

@Composable
fun HerramientasMiuixContent(
    viewModel: NotasViewModel? = null,
    forosViewModel: ForosViewModel? = null,
    onOpenProgreso: () -> Unit = {},
    onOpenForos: () -> Unit = {},
    onOpenForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
    onOpenArchive: () -> Unit = {},
    onOpenTuneSettings: () -> Unit = {},
    onOpenCatalogImport: () -> Unit = {},
    onImportJsonClick: () -> Unit = {},
    onExportJsonClick: () -> Unit = {},
    onResetGroups: () -> Unit = {},
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(null) })
    val forosUiState by (forosViewModel?.uiState?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(null) })

    val backdrop = rememberMiuixBlurBackdrop()
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            MiuixBlurredBar(
                backdrop = backdrop,
                scrollBehavior = scrollBehavior
            ) {
                MiuixPageTopAppBar(
                    title = stringResource(Res.string.herramientas_title),
                    subtitle = stringResource(Res.string.herramientas_subtitle),
                    scrollBehavior = scrollBehavior
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
        val contentPadding = miuixPageContentPadding(
            innerPadding = innerPadding,
            extraTop = 8.dp,
            extraStart = 0.dp,
            extraEnd = 0.dp,
            extraBottom = if (isWideScreen) 24.dp else 120.dp
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
                .miuixBackdropModifier(backdrop)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .miuixPageScrollModifiers(scrollBehavior),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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


                // ─── CARD 3: Foros de Discusión ─────────────────────────
                item {
                    ForosBentoCard(
                        forosUiState = forosUiState,
                        onClick = onOpenForos,
                        onOpenTopic = onOpenForoDetail
                    )
                }

                // ─── CARD 4: Historial & Archivo de Tareas ──────────────
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
}
