package me.joxquin.notivas.ui.foros.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.ZonedDateTime
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.foros.ForosViewModel
import me.joxquin.notivas.ui.foros.detail.components.ForoCopilotAssistantSheet
import me.joxquin.notivas.ui.foros.detail.components.ForoDetailConsignaCard
import me.joxquin.notivas.ui.foros.detail.components.ForoDetailEditorSection
import me.joxquin.notivas.ui.foros.detail.components.ForoDetailHeroCard
import me.joxquin.notivas.ui.foros.detail.components.ForoDetailRepliesSection
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForoDetailScreen(
    viewModel: ForosViewModel,
    onBack: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val uiState by viewModel.uiState.collectAsState()
    val topic = uiState.selectedDiscussion ?: return

    var showCopilotAssistant by remember { mutableStateOf(false) }

    val isExpired = remember(topic) {
        val now = ZonedDateTime.now()
        val dueStr = topic.assignment?.dueAt ?: topic.lockAt ?: topic.assignment?.lockAt
        if (topic.locked == true) {
            true
        } else if (dueStr != null) {
            try {
                val dueDate = ZonedDateTime.parse(dueStr)
                now.isAfter(dueDate)
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
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
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Encabezado Hero del Foro
                item(key = "foro_hero") {
                    ForoDetailHeroCard(
                        topic = topic,
                        themeStyle = themeStyle
                    )
                }

                // 2. Consigna Oficial y Rúbrica Colapsable
                item(key = "foro_consigna") {
                    ForoDetailConsignaCard(
                        topic = topic,
                        themeStyle = themeStyle
                    )
                }

                // 3. Aportes de Compañeros
                item(key = "foro_replies") {
                    ForoDetailRepliesSection(
                        replies = uiState.selectedDiscussionReplies,
                        isLoading = uiState.isLoadingReplies,
                        totalRepliesCount = topic.discussionSubentryCount ?: 0,
                        themeStyle = themeStyle
                    )
                }

                // 4. Editor de Respuesta y Asistencia Copilot
                item(key = "foro_editor") {
                    ForoDetailEditorSection(
                        topic = topic,
                        draftText = uiState.currentDraft,
                        onDraftChange = { viewModel.updateDraft(it) },
                        onSaveDraft = { viewModel.saveDraft() },
                        isDraftSaved = uiState.isDraftSavedMessage,
                        isExpired = isExpired,
                        onOpenCopilotAssistant = { showCopilotAssistant = true },
                        themeStyle = themeStyle
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    } else {
        // Material Design 3
        val topAppBarContainerColor by animateColorAsState(
            targetValue = if (isScrolled) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            animationSpec = tween(durationMillis = 250),
            label = "foroDetailMaterialTopBarColor"
        )

        Scaffold(
            topBar = {
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
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Detalle del Foro",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = topic.courseName ?: "Foro Académico Canvas",
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
                                contentDescription = "Atrás",
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Encabezado Hero del Foro
                item(key = "foro_hero") {
                    ForoDetailHeroCard(
                        topic = topic,
                        themeStyle = themeStyle
                    )
                }

                // 2. Consigna Oficial y Rúbrica Colapsable
                item(key = "foro_consigna") {
                    ForoDetailConsignaCard(
                        topic = topic,
                        themeStyle = themeStyle
                    )
                }

                // 3. Aportes de Compañeros
                item(key = "foro_replies") {
                    ForoDetailRepliesSection(
                        replies = uiState.selectedDiscussionReplies,
                        isLoading = uiState.isLoadingReplies,
                        totalRepliesCount = topic.discussionSubentryCount ?: 0,
                        themeStyle = themeStyle
                    )
                }

                // 4. Editor de Respuesta y Asistencia Copilot
                item(key = "foro_editor") {
                    ForoDetailEditorSection(
                        topic = topic,
                        draftText = uiState.currentDraft,
                        onDraftChange = { viewModel.updateDraft(it) },
                        onSaveDraft = { viewModel.saveDraft() },
                        isDraftSaved = uiState.isDraftSavedMessage,
                        isExpired = isExpired,
                        onOpenCopilotAssistant = { showCopilotAssistant = true },
                        themeStyle = themeStyle
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Modal Bottom Sheet de Copilot IA
    if (showCopilotAssistant) {
        ForoCopilotAssistantSheet(
            topic = topic,
            strategy = uiState.copilotStrategy,
            isGenerating = uiState.isGeneratingCopilot,
            errorMessage = uiState.copilotErrorMessage,
            activeModelName = uiState.activeCopilotModel,
            onDismiss = { showCopilotAssistant = false },
            onGenerate = { toneModifier ->
                viewModel.generateCopilotStrategy(topic, toneModifier)
            },
            onInsertDraft = { suggestion ->
                viewModel.applyCopilotSuggestion(suggestion)
                showCopilotAssistant = false
            },
            themeStyle = themeStyle
        )
    }
}
