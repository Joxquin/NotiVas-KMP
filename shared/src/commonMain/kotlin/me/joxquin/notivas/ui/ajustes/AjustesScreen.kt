package me.joxquin.notivas.ui.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import me.joxquin.notivas.data.local.AppThemeStyle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.ui.ajustes.components.AjustesAppInfoFooter
import me.joxquin.notivas.ui.ajustes.components.CopilotSettingsSection
import me.joxquin.notivas.ui.ajustes.components.GoogleDriveSettingsSection
import me.joxquin.notivas.ui.ajustes.components.GranularNotificationsSection
import me.joxquin.notivas.ui.ajustes.components.SecurityAndKeystoreSettingsSection
import me.joxquin.notivas.ui.ajustes.components.StudentIdentitySettingsCard
import me.joxquin.notivas.ui.ajustes.components.LanguageSettingsSection
import me.joxquin.notivas.ui.ajustes.components.ThemeAppearanceSettingsSection
import me.joxquin.notivas.ui.profile.ProfileViewModel
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.nestedscroll.nestedScroll
import me.joxquin.notivas.ui.components.miuix.rememberMiuixAutoSettleScrollConnection
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import top.yukonga.miuix.kmp.window.WindowDialog
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_title
import me.joxquin.notivas.shared.resources.settings_subtitle
import me.joxquin.notivas.shared.resources.logout_dialog_title
import me.joxquin.notivas.shared.resources.logout_dialog_text
import me.joxquin.notivas.shared.resources.logout_confirm
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.sync

@Composable
fun AjustesHeaderSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(Res.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Orquestador declarativo de la pantalla de Ajustes y Perfil.
 * Soporta de manera reactiva los modos Material Design 3 Monet y HyperOS Miuix.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: ProfileViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPairWebDialog by remember { mutableStateOf(false) }

    if (showPairWebDialog) {
        me.joxquin.notivas.ui.ajustes.components.PairWebSessionDialog(
            themeStyle = themeStyle,
            onDismissRequest = { showPairWebDialog = false },
            onAuthorizePairing = { pin -> viewModel.pairWebSession(pin) }
        )
    }

    if (showLogoutDialog) {
        if (themeStyle == AppThemeStyle.MIUIX) {
            WindowDialog(
                show = showLogoutDialog,
                title = stringResource(Res.string.logout_dialog_title),
                summary = stringResource(Res.string.logout_dialog_text),
                onDismissRequest = { showLogoutDialog = false },
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiuixTextButton(
                            text = stringResource(Res.string.cancel),
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(16.dp))
                        MiuixTextButton(
                            text = stringResource(Res.string.logout_confirm),
                            onClick = {
                                showLogoutDialog = false
                                viewModel.logout()
                            },
                            modifier = Modifier.weight(1f),
                            colors = MiuixButtonDefaults.textButtonColors(
                                color = MiuixTheme.colorScheme.errorContainer,
                                textColor = MiuixTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(Res.string.logout_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(Res.string.logout_dialog_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(Res.string.logout_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()

        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = stringResource(Res.string.settings_title),
                        subtitle = stringResource(Res.string.settings_subtitle),
                        scrollBehavior = scrollBehavior,
                        actions = {
                            MiuixGlassyActionsContainer(
                                backdrop = backdrop
                            ) {
                                top.yukonga.miuix.kmp.basic.IconButton(
                                    onClick = { viewModel.verifyToken() }
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Refresh,
                                        contentDescription = "Sincronizar",
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = innerPadding,
                extraTop = 8.dp,
                extraStart = 0.dp,
                extraEnd = 0.dp,
                extraBottom = if (isWideScreen) 24.dp else 100.dp
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                // 1. Identidad Estudiantil Canvas
                item {
                    StudentIdentitySettingsCard(
                        uiState = uiState,
                        onVerifyToken = { viewModel.verifyToken() },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 2. Idioma & Región
                item {
                    LanguageSettingsSection(
                        appLanguage = uiState.appLanguage,
                        onLanguageChange = { viewModel.setAppLanguage(it) },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 3. Sistema Visual & Temas
                item {
                    ThemeAppearanceSettingsSection(
                        dynamicColorEnabled = uiState.dynamicColorEnabled,
                        onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
                        themeStyle = uiState.themeStyle,
                        onThemeStyleChange = { viewModel.setThemeStyle(it) }
                    )
                }

                // 3. Notificaciones Granulares
                item {
                    GranularNotificationsSection(
                        notif24h = uiState.notif24h,
                        notif3h = uiState.notif3h,
                        notif30m = uiState.notif30m,
                        notifNewAssignments = uiState.notifNewAssignments,
                        notifNewQuizzes = uiState.notifNewQuizzes,
                        notifNewForos = uiState.notifNewForos,
                        syncIntervalMinutes = uiState.syncIntervalMinutes,
                        onNotif24hChange = { viewModel.setNotif24h(it) },
                        onNotif3hChange = { viewModel.setNotif3h(it) },
                        onNotif30mChange = { viewModel.setNotif30m(it) },
                        onNotifNewAssignmentsChange = { viewModel.setNotifNewAssignments(it) },
                        onNotifNewQuizzesChange = { viewModel.setNotifNewQuizzes(it) },
                        onNotifNewForosChange = { viewModel.setNotifNewForos(it) },
                        onSyncIntervalChange = { viewModel.setSyncIntervalMinutes(it) },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 4. Configuración de IA (Copilot / OpenRouter / Google AI Studio)
                item {
                    CopilotSettingsSection(
                        enabled = uiState.copilotEnabled,
                        provider = uiState.copilotProvider,
                        apiKey = uiState.openRouterApiKey,
                        selectedModel = uiState.openRouterModel,
                        googleAiStudioApiKey = uiState.googleAiStudioApiKey,
                        googleAiStudioModel = uiState.googleAiStudioModel,
                        totalTokens = uiState.totalCopilotTokens,
                        balance = uiState.openRouterBalance,
                        isLoadingBalance = uiState.isLoadingBalance,
                        onEnabledChange = { viewModel.setCopilotEnabled(it) },
                        onProviderChange = { viewModel.setCopilotProvider(it) },
                        onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                        onModelChange = { viewModel.setOpenRouterModel(it) },
                        onGoogleAiStudioApiKeyChange = { viewModel.setGoogleAiStudioApiKey(it) },
                        onGoogleAiStudioModelChange = { viewModel.setGoogleAiStudioModel(it) },
                        onRefreshBalance = { viewModel.refreshOpenRouterBalance() },
                        onAnalyzeModel = { viewModel.analyzeCustomModel(it) },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 4.1 Google Drive y Borradores
                item {
                    GoogleDriveSettingsSection(
                        connected = uiState.googleDriveConnected,
                        email = uiState.googleDriveEmail,
                        lastSyncText = uiState.lastSyncAgoText,
                        cacheSize = uiState.googleDriveCacheSize,
                        onAuthSuccess = { token, refresh, email ->
                            viewModel.saveGoogleDriveAuth(token, refresh, email)
                        },
                        onUnlink = { viewModel.unlinkGoogleDrive() },
                        onClearCache = { viewModel.clearDraftsCache() },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 5. Seguridad y Zona Crítica
                item {
                    SecurityAndKeystoreSettingsSection(
                        onRequestLogout = { showLogoutDialog = true },
                        onRequestPairWeb = { showPairWebDialog = true },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 6. Pie de Página con Versión
                item {
                    AjustesAppInfoFooter(
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }
            }

            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                trackPadding = contentPadding
            )
        }
    }
    return
}

    // ─── MODO MATERIAL DESIGN 3 ─────────────────────────────────────────────
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
        label = "ajustesTopBarContainerColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isScrolled,
                        enter = fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                                slideInVertically(animationSpec = tween(250)) { it / 2 },
                        exit = fadeOut(animationSpec = tween(150)) +
                                slideOutVertically(animationSpec = tween(180)) { it / 2 }
                    ) {
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
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Ajustes & Perfil",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.verifyToken() }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
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
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Header Contextual
            item {
                AjustesHeaderSection()
            }

            // 2. Identidad Estudiantil Canvas
            item {
                StudentIdentitySettingsCard(
                    uiState = uiState,
                    onVerifyToken = { viewModel.verifyToken() },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 3. Idioma & Región
            item {
                LanguageSettingsSection(
                    appLanguage = uiState.appLanguage,
                    onLanguageChange = { viewModel.setAppLanguage(it) },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 4. Sistema Visual & Temas
            item {
                ThemeAppearanceSettingsSection(
                    dynamicColorEnabled = uiState.dynamicColorEnabled,
                    onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
                    themeStyle = uiState.themeStyle,
                    onThemeStyleChange = { viewModel.setThemeStyle(it) }
                )
            }

            // 4. Notificaciones Granulares
            item {
                GranularNotificationsSection(
                    notif24h = uiState.notif24h,
                    notif3h = uiState.notif3h,
                    notif30m = uiState.notif30m,
                    notifNewAssignments = uiState.notifNewAssignments,
                    notifNewQuizzes = uiState.notifNewQuizzes,
                    notifNewForos = uiState.notifNewForos,
                    syncIntervalMinutes = uiState.syncIntervalMinutes,
                    onNotif24hChange = { viewModel.setNotif24h(it) },
                    onNotif3hChange = { viewModel.setNotif3h(it) },
                    onNotif30mChange = { viewModel.setNotif30m(it) },
                    onNotifNewAssignmentsChange = { viewModel.setNotifNewAssignments(it) },
                    onNotifNewQuizzesChange = { viewModel.setNotifNewQuizzes(it) },
                    onNotifNewForosChange = { viewModel.setNotifNewForos(it) },
                    onSyncIntervalChange = { viewModel.setSyncIntervalMinutes(it) },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 5. Configuración de IA (Copilot / OpenRouter / Google AI Studio)
            item {
                CopilotSettingsSection(
                    enabled = uiState.copilotEnabled,
                    provider = uiState.copilotProvider,
                    apiKey = uiState.openRouterApiKey,
                    selectedModel = uiState.openRouterModel,
                    googleAiStudioApiKey = uiState.googleAiStudioApiKey,
                    googleAiStudioModel = uiState.googleAiStudioModel,
                    totalTokens = uiState.totalCopilotTokens,
                    balance = uiState.openRouterBalance,
                    isLoadingBalance = uiState.isLoadingBalance,
                    onEnabledChange = { viewModel.setCopilotEnabled(it) },
                    onProviderChange = { viewModel.setCopilotProvider(it) },
                    onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                    onModelChange = { viewModel.setOpenRouterModel(it) },
                    onGoogleAiStudioApiKeyChange = { viewModel.setGoogleAiStudioApiKey(it) },
                    onGoogleAiStudioModelChange = { viewModel.setGoogleAiStudioModel(it) },
                    onRefreshBalance = { viewModel.refreshOpenRouterBalance() },
                    onAnalyzeModel = { viewModel.analyzeCustomModel(it) },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 5.1 Google Drive y Borradores
            item {
                GoogleDriveSettingsSection(
                    connected = uiState.googleDriveConnected,
                    email = uiState.googleDriveEmail,
                    lastSyncText = uiState.lastSyncAgoText,
                    cacheSize = uiState.googleDriveCacheSize,
                    onAuthSuccess = { token, refresh, email ->
                        viewModel.saveGoogleDriveAuth(token, refresh, email)
                    },
                    onUnlink = { viewModel.unlinkGoogleDrive() },
                    onClearCache = { viewModel.clearDraftsCache() },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 6. Seguridad y Zona Crítica
            item {
                SecurityAndKeystoreSettingsSection(
                    onRequestLogout = { showLogoutDialog = true },
                    onRequestPairWeb = { showPairWebDialog = true },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 7. Pie de Página con Versión
            item {
                AjustesAppInfoFooter(
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }
        }
    }
}
