package me.joxquin.notivas.ui.copilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.copilot.components.CopilotHeaderBar
import me.joxquin.notivas.ui.copilot.components.CopilotHistoryBottomSheet
import me.joxquin.notivas.ui.copilot.components.CopilotInputBar
import me.joxquin.notivas.ui.copilot.components.CopilotMentionPopup
import me.joxquin.notivas.ui.copilot.components.CopilotMessageList
import me.joxquin.notivas.ui.copilot.components.CopilotWarningBanner
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Orquestador UDF declarativo para el Asistente Académico Copilot / Copilot IA.
 * Soporta de forma nativa tanto Material Design 3 Expressive como HyperOS Miuix.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotScreen(
    viewModel: CopilotViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = lazyListState
    var isHistorySheetVisible by remember { mutableStateOf(false) }

    // Auto-scroll al final con cada mensaje nuevo o respuesta recibida
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Modal de Historial
    CopilotHistoryBottomSheet(
        isVisible = isHistorySheetVisible || uiState.isHistorySheetVisible,
        sessions = uiState.sessions,
        currentSessionId = uiState.currentSessionId,
        onSelectSession = { viewModel.loadSession(it) },
        onNewSession = { viewModel.startNewSession() },
        onRenameSession = { id, title -> viewModel.renameSession(id, title) },
        onDeleteSession = { viewModel.deleteSession(it) },
        onDismiss = {
            isHistorySheetVisible = false
            viewModel.dismissHistorySheet()
        },
        themeStyle = themeStyle
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        // ─── EXPERIENCIA HYPEROS MIUIX (NOTIVAS Copilot) ────────────────────────
        val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val isImeOpen = imeBottom > 0.dp
        val floatingNavBottom = if (navBarBottom != 0.dp) 8.dp + navBarBottom else 36.dp
        // FloatingNavigationBar: altura 64dp + bottomPaddingValue (floatingNavBottom).
        // Separación uniforme entre FloatingNavigationBar y CopilotInputBar = 12.dp.
        val targetBottomPadding = if (isImeOpen) (imeBottom + 12.dp) else (floatingNavBottom + 64.dp + 12.dp)
        val animatedBottomPadding by animateDpAsState(
            targetValue = targetBottomPadding,
            animationSpec = tween(durationMillis = 200),
            label = "copilotBottomPadding"
        )

        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
        ) {
            // Capa 1: Flujo de mensajes Edge-to-Edge que se desliza por detrás del TopBar y del InputBar
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CopilotMessageList(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    listState = listState,
                    coursesCount = uiState.courses.size,
                    currentModel = uiState.currentModel,
                    onSuggestionClick = { prompt ->
                        viewModel.updateInputText(prompt)
                        viewModel.sendMessage()
                    },
                    themeStyle = AppThemeStyle.MIUIX,
                    bottomPadding = animatedBottomPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Capa 2: Controles flotantes inferiores (Warning Banner si aplica, Popup de @ y Cápsula de entrada)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = animatedBottomPadding)
            ) {
                // Alerta de API Key o activación
                CopilotWarningBanner(
                    isCopilotDisabled = !uiState.isCopilotEnabled,
                    hasApiKey = uiState.hasApiKey,
                    onNavigateToSettings = onNavigateToSettings,
                    themeStyle = AppThemeStyle.MIUIX
                )

                // Popup interactivo para @ menciones de cursos y recursos
                CopilotMentionPopup(
                    isVisible = uiState.isMentionPopupVisible,
                    mentionStep = uiState.mentionStep,
                    selectedCourse = uiState.selectedCourseForMention,
                    courses = uiState.courses,
                    resources = uiState.mentionResourceList,
                    onSelectCourse = { viewModel.selectMentionCourse(it) },
                    onSelectResource = { viewModel.selectMentionResource(it) },
                    onBackToCourses = { viewModel.backToCourseSelection() },
                    onDismiss = { viewModel.dismissMentionPopup() },
                    themeStyle = AppThemeStyle.MIUIX
                )

                // Cápsula de entrada flotante Miuix
                CopilotInputBar(
                    inputText = uiState.inputText,
                    isLoading = uiState.isLoading,
                    enabled = uiState.hasApiKey && uiState.isCopilotEnabled,
                    onInputChange = { viewModel.updateInputText(it) },
                    onSend = { viewModel.sendMessage() },
                    onTriggerMention = { viewModel.triggerMention() },
                    themeStyle = AppThemeStyle.MIUIX
                )
            }
        }
        return
    }

    // ─── EXPERIENCIA MATERIAL DESIGN 3 MONET ──────────────────────────────
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    val topAppBarContainerColor by animateColorAsState(
        targetValue = if (isScrolled) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250),
        label = "copilotTopBarColor"
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Copilot Académico",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.sessionTokens > 0) "${uiState.sessionTokens} tokens usados" else "Asistente IA para tus cursos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isHistorySheetVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.startNewSession() }) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = "Nuevo Chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            CopilotWarningBanner(
                isCopilotDisabled = !uiState.isCopilotEnabled,
                hasApiKey = uiState.hasApiKey,
                onNavigateToSettings = onNavigateToSettings,
                themeStyle = AppThemeStyle.MATERIAL
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CopilotMessageList(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    listState = listState,
                    coursesCount = uiState.courses.size,
                    currentModel = uiState.currentModel,
                    onSuggestionClick = { prompt ->
                        viewModel.updateInputText(prompt)
                        viewModel.sendMessage()
                    },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // Popup interactivo para @ menciones de cursos y recursos
            CopilotMentionPopup(
                isVisible = uiState.isMentionPopupVisible,
                mentionStep = uiState.mentionStep,
                selectedCourse = uiState.selectedCourseForMention,
                courses = uiState.courses,
                resources = uiState.mentionResourceList,
                onSelectCourse = { viewModel.selectMentionCourse(it) },
                onSelectResource = { viewModel.selectMentionResource(it) },
                onBackToCourses = { viewModel.backToCourseSelection() },
                onDismiss = { viewModel.dismissMentionPopup() },
                themeStyle = AppThemeStyle.MATERIAL
            )

            CopilotInputBar(
                inputText = uiState.inputText,
                isLoading = uiState.isLoading,
                enabled = uiState.hasApiKey && uiState.isCopilotEnabled,
                onInputChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                onTriggerMention = { viewModel.triggerMention() },
                themeStyle = AppThemeStyle.MATERIAL,
                modifier = Modifier.imePadding()
            )
        }
    }
}
