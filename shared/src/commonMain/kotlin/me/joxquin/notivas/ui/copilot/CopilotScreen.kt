package me.joxquin.notivas.ui.copilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import me.joxquin.notivas.data.local.CopilotProvider
import me.joxquin.notivas.ui.components.aura.MonetAuraBackground
import me.joxquin.notivas.ui.copilot.components.CopilotCustomInstructionsDialog
import me.joxquin.notivas.ui.copilot.components.CopilotHeaderBar
import me.joxquin.notivas.ui.copilot.components.CopilotHistoryOverlay
import me.joxquin.notivas.ui.copilot.components.CopilotInputBar
import me.joxquin.notivas.ui.copilot.components.CopilotMentionPopup
import me.joxquin.notivas.ui.copilot.components.CopilotMessageList
import me.joxquin.notivas.ui.copilot.components.CopilotModelPickerDialog
import me.joxquin.notivas.ui.copilot.components.CopilotProviderPickerDialog
import me.joxquin.notivas.ui.copilot.components.CopilotQuickChipsRow
import me.joxquin.notivas.ui.copilot.components.CopilotWarningBanner
import me.joxquin.notivas.ui.copilot.components.resolveModelLabel
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Tune
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassPopupItem
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.GlassTransformPopup
import top.yukonga.miuix.kmp.glass.glassPopupAnchor
import top.yukonga.miuix.kmp.glass.glassPopupAnchorContent
import top.yukonga.miuix.kmp.glass.rememberGlassPopupAnchor
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

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
    onOpenAssignmentDraft: ((Long, String) -> Unit)? = null,
    onOpenForoDetail: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val listState = lazyListState
    var isHistorySheetVisible by remember { mutableStateOf(false) }

    var showQuickSettingsMenu by remember { mutableStateOf(false) }
    var showMaterialSettingsMenu by remember { mutableStateOf(false) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showCustomInstructionsDialog by remember { mutableStateOf(false) }

    // Auto-scroll al final con cada mensaje nuevo o respuesta recibida
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Modal / Overlay de Historial con buscador integrado
    CopilotHistoryOverlay(
        expanded = isHistorySheetVisible || uiState.isHistorySheetVisible,
        onDismissRequest = {
            isHistorySheetVisible = false
            viewModel.dismissHistorySheet()
        },
        sessions = uiState.sessions,
        currentSessionId = uiState.currentSessionId,
        onSelectSession = { viewModel.loadSession(it) },
        onNewSession = { viewModel.startNewSession() },
        onRenameSession = { id, title -> viewModel.renameSession(id, title) },
        onDeleteSession = { viewModel.deleteSession(it) },
        themeStyle = themeStyle
    )

    // Diálogos de Ajustes Rápidos (Proveedor, Modelo y Personalización)
    CopilotProviderPickerDialog(
        visible = showProviderDialog,
        currentProvider = uiState.provider,
        onProviderSelected = { viewModel.setProvider(it) },
        onDismiss = { showProviderDialog = false },
        themeStyle = themeStyle
    )

    CopilotModelPickerDialog(
        visible = showModelDialog,
        provider = uiState.provider,
        currentModel = uiState.currentModel,
        onModelSelected = { viewModel.setModel(it) },
        onDismiss = { showModelDialog = false },
        themeStyle = themeStyle
    )

    CopilotCustomInstructionsDialog(
        visible = showCustomInstructionsDialog,
        initialInstructions = uiState.customInstructions,
        onSaveInstructions = { viewModel.setCustomInstructions(it) },
        onDismiss = { showCustomInstructionsDialog = false },
        themeStyle = themeStyle
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        // ─── EXPERIENCIA HYPEROS MIUIX (NOTIVAS Copilot) ────────────────────────
        val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
        val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val isImeOpen = imeBottom > 0.dp
        val floatingNavBottom = if (navBarBottom != 0.dp) 8.dp + navBarBottom else 36.dp
        // FloatingNavigationBar: altura 64dp + bottomPaddingValue (floatingNavBottom).
        // Separación uniforme entre FloatingNavigationBar y CopilotInputBar = 12.dp.
        val targetBottomPadding = when {
            isImeOpen -> (imeBottom + 12.dp)
            isWideScreen -> 16.dp
            else -> (floatingNavBottom + 64.dp + 12.dp)
        }
        val animatedBottomPadding by animateDpAsState(
            targetValue = targetBottomPadding,
            animationSpec = tween(durationMillis = 200),
            label = "copilotBottomPadding"
        )

        val auraAlpha by animateFloatAsState(
            targetValue = if (uiState.isLoading) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (uiState.isLoading) 600 else 800,
                easing = FastOutSlowInEasing
            ),
            label = "copilotMiuixAuraAlpha"
        )

        val backdrop = rememberMiuixBlurBackdrop(withSurfaceBackground = false)
        val menuAnchor = rememberGlassPopupAnchor()

        Box(
            modifier = modifier.fillMaxSize()
        ) {
            top.yukonga.miuix.kmp.basic.Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    MiuixBlurredBar(
                        backdrop = backdrop,
                    ) {
                        MiuixPageTopAppBar(
                            title = stringResource(Res.string.copilot_title),
                            color = Color.Transparent,
                            isContentScrolled = true,
                            progressiveBlur = true,
                            collapsed = true,
                            navigationIcon = {
                                GlassIconButton(
                                    backdrop = backdrop,
                                    onClick = { viewModel.startNewSession() }
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Create,
                                        contentDescription = stringResource(Res.string.copilot_new_chat),
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            actions = {
                                GlassIconButton(
                                    backdrop = backdrop,
                                    onClick = { showQuickSettingsMenu = true },
                                    modifier = Modifier.glassPopupAnchor(
                                        anchor = menuAnchor,
                                        cornerRadius = GlassTopAppBarDefaults.ButtonSize / 2,
                                    )
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Tune,
                                        contentDescription = stringResource(Res.string.copilot_quick_settings_title),
                                        modifier = Modifier.size(28.dp).glassPopupAnchorContent(menuAnchor),
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                // Capa 1: Flujo de mensajes Edge-to-Edge que se desliza por detrás del TopBar y del InputBar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixBackdropModifier(backdrop)
                ) {
                    MonetAuraBackground(
                        alpha = auraAlpha,
                        surfaceColor = MiuixTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxSize()
                    )

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
                        thinkingStatus = uiState.thinkingStatus,
                        onRegenerate = { viewModel.regenerateLastResponse() },
                        isMessageAnimated = { viewModel.isMessageAnimated(it) },
                        onMessageAnimated = { viewModel.markMessageAnimated(it) },
                        onOpenAssignmentDraft = onOpenAssignmentDraft,
                        onOpenForoDetail = onOpenForoDetail,
                        knownAssignments = allAssignments,
                        courses = uiState.courses,
                        themeStyle = AppThemeStyle.MIUIX,
                        bottomPadding = animatedBottomPadding,
                        modifier = Modifier.fillMaxSize()
                    )

                    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    top.yukonga.miuix.kmp.basic.VerticalScrollBar(
                        adapter = top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter(listState),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(
                                top = statusBarTop + 68.dp,
                                bottom = animatedBottomPadding + 60.dp
                            )
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

                    // Fila de sugerencias rápidas deslizable
                    if (uiState.hasApiKey && uiState.isCopilotEnabled && !uiState.isLoading && uiState.messages.isNotEmpty()) {
                        CopilotQuickChipsRow(
                            onChipClick = { prompt ->
                                viewModel.updateInputText(prompt)
                                viewModel.sendMessage()
                            },
                            themeStyle = AppThemeStyle.MIUIX
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

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

            val currentModelLabel = resolveModelLabel(uiState.provider, uiState.currentModel)

            GlassTransformPopup(
                show = showQuickSettingsMenu,
                onDismissRequest = { showQuickSettingsMenu = false },
                anchor = menuAnchor,
                backdrop = backdrop,
                anchorContent = {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MiuixTheme.colorScheme.onBackground,
                    )
                },
                gap = 0.dp,
            ) {
                GlassPopupItem(
                    text = "${stringResource(Res.string.copilot_provider_label)}: ${if (uiState.provider == CopilotProvider.OPENROUTER) "OpenRouter" else "Google AI"}",
                    icon = Icons.Default.AutoAwesome,
                    onClick = {
                        showQuickSettingsMenu = false
                        showProviderDialog = true
                    }
                )
                GlassPopupItem(
                    text = "${stringResource(Res.string.copilot_model_label)}: $currentModelLabel",
                    icon = Icons.Default.Psychology,
                    onClick = {
                        showQuickSettingsMenu = false
                        showModelDialog = true
                    }
                )
                GlassPopupItem(
                    text = stringResource(Res.string.copilot_custom_instructions_title),
                    icon = Icons.Default.Tune,
                    onClick = {
                        showQuickSettingsMenu = false
                        showCustomInstructionsDialog = true
                    }
                )
                GlassPopupItem(
                    text = stringResource(Res.string.nav_ajustes),
                    icon = Icons.Default.Settings,
                    onClick = {
                        showQuickSettingsMenu = false
                        onNavigateToSettings()
                    }
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
                                text = stringResource(Res.string.copilot_topbar_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.sessionTokens > 0) {
                                    stringResource(Res.string.tokens_used_format, uiState.sessionTokens)
                                } else {
                                    stringResource(Res.string.copilot_topbar_subtitle)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.startNewSession() }) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = stringResource(Res.string.copilot_new_chat),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMaterialSettingsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = stringResource(Res.string.copilot_quick_settings_title),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMaterialSettingsMenu,
                            onDismissRequest = { showMaterialSettingsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("${stringResource(Res.string.copilot_provider_label)}: ${if (uiState.provider == CopilotProvider.OPENROUTER) "OpenRouter" else "Google AI"}")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                },
                                onClick = {
                                    showMaterialSettingsMenu = false
                                    showProviderDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    val currentModelLabel = resolveModelLabel(uiState.provider, uiState.currentModel)
                                    Text("${stringResource(Res.string.copilot_model_label)}: $currentModelLabel")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Psychology, contentDescription = null)
                                },
                                onClick = {
                                    showMaterialSettingsMenu = false
                                    showModelDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(Res.string.copilot_custom_instructions_title))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Tune, contentDescription = null)
                                },
                                onClick = {
                                    showMaterialSettingsMenu = false
                                    showCustomInstructionsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(Res.string.nav_ajustes))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                },
                                onClick = {
                                    showMaterialSettingsMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
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
                    thinkingStatus = uiState.thinkingStatus,
                    onRegenerate = { viewModel.regenerateLastResponse() },
                    isMessageAnimated = { viewModel.isMessageAnimated(it) },
                    onMessageAnimated = { viewModel.markMessageAnimated(it) },
                    onOpenAssignmentDraft = onOpenAssignmentDraft,
                    onOpenForoDetail = onOpenForoDetail,
                    knownAssignments = allAssignments,
                    courses = uiState.courses,
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

            // Fila de sugerencias rápidas deslizable
            if (uiState.hasApiKey && uiState.isCopilotEnabled && !uiState.isLoading && uiState.messages.isNotEmpty()) {
                CopilotQuickChipsRow(
                    onChipClick = { prompt ->
                        viewModel.updateInputText(prompt)
                        viewModel.sendMessage()
                    },
                    themeStyle = AppThemeStyle.MATERIAL
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

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
