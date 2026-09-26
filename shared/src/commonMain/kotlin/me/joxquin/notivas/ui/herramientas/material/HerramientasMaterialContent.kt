package me.joxquin.notivas.ui.herramientas.material

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.ui.notas.NotasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerramientasMaterialContent(
    viewModel: NotasViewModel? = null,
    onOpenProgreso: () -> Unit = {},
    onOpenTuneSettings: () -> Unit = {},
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel?.uiState?.collectAsState()?.value

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    val topAppBarContainerColor by animateColorAsState(
        targetValue = if (isScrolled) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250),
        label = "herramientasTopBarColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isScrolled,
                        enter = fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                                slideInVertically(animationSpec = tween(250)) { it / 2 }
                    ) {
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
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Herramientas",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Utilidades y módulos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = onOpenTuneSettings) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.Tune,
                            contentDescription = "Ajustes de Ponderación",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ─── 1. Header Contextual Material ───────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Herramientas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Utilidades y módulos NotiVas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = "MÓDULOS ACTIVOS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }

            // ─── CARD 1: Hero Bento Progreso & Notas ─────────────────────────
            item {
                MaterialHeroPlannerCard(
                    uiState = uiState,
                    onClick = onOpenProgreso,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ─── CARD 2: Simulador What-If ───────────────────────────────────
            item {
                MaterialWhatIfCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ─── CARD 3: Foros de Discusión ─────────────────────────────────
            item {
                MaterialForosCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ─── CARD 3: Historial & Archivo de Tareas ────────────────────────
            item {
                MaterialArchiveCard(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
