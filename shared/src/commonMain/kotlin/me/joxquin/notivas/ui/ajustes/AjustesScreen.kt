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
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.ajustes.components.AjustesAppInfoFooter
import me.joxquin.notivas.ui.ajustes.components.CopilotSettingsSection
import me.joxquin.notivas.ui.ajustes.components.GranularNotificationsSection
import me.joxquin.notivas.ui.ajustes.components.SecurityAndKeystoreSettingsSection
import me.joxquin.notivas.ui.ajustes.components.StudentIdentitySettingsCard
import me.joxquin.notivas.ui.ajustes.components.ThemeAppearanceSettingsSection
import me.joxquin.notivas.ui.profile.ProfileViewModel
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
            text = "Ajustes & Perfil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Gestión de cuenta Canvas LMS y preferencias del sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Orquestador declarativo de la pantalla de Ajustes y Perfil (< 160 líneas).
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

    if (showLogoutDialog) {
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
                    text = "¿Desvincular cuenta?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Se cerrará tu sesión, se limpiará la base de datos local y se destruirá el token de acceso seguro.",
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
                    Text("Confirmar y salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 0. Large Title Canónico HyperOS Miuix (32.sp)
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
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .graphicsLayer {
                                alpha = largeTitleAlpha
                                translationY = largeTitleTranslationY
                            }
                    ) {
                        MiuixText(
                            text = "Ajustes & Perfil",
                            fontSize = MiuixTheme.textStyles.title1.fontSize,
                            fontWeight = FontWeight.Normal,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        MiuixText(
                            text = "Gestión de cuenta Canvas LMS y preferencias del sistema",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                // 1. Identidad Estudiantil Canvas
                item {
                    StudentIdentitySettingsCard(
                        uiState = uiState,
                        onVerifyToken = { viewModel.verifyToken() },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 2. Sistema Visual & Temas
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
                        notifMorning = uiState.notifMorning,
                        syncIntervalMinutes = uiState.syncIntervalMinutes,
                        onNotif24hChange = { viewModel.setNotif24h(it) },
                        onNotif3hChange = { viewModel.setNotif3h(it) },
                        onNotif30mChange = { viewModel.setNotif30m(it) },
                        onNotifMorningChange = { viewModel.setNotifMorning(it) },
                        onSyncIntervalChange = { viewModel.setSyncIntervalMinutes(it) },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 4. Configuración de IA (Copilot / OpenRouter)
                item {
                    CopilotSettingsSection(
                        enabled = uiState.copilotEnabled,
                        apiKey = uiState.openRouterApiKey,
                        selectedModel = uiState.openRouterModel,
                        totalTokens = uiState.totalCopilotTokens,
                        balance = uiState.openRouterBalance,
                        isLoadingBalance = uiState.isLoadingBalance,
                        onEnabledChange = { viewModel.setCopilotEnabled(it) },
                        onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                        onModelChange = { viewModel.setOpenRouterModel(it) },
                        onRefreshBalance = { viewModel.refreshOpenRouterBalance() },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                // 5. Seguridad y Zona Crítica
                item {
                    SecurityAndKeystoreSettingsSection(
                        biometricLock = uiState.biometricLock,
                        onBiometricLockChange = { viewModel.setBiometricLock(it) },
                        lastSyncText = uiState.lastSyncAgoText,
                        apiLatencyMs = uiState.apiLatencyMs,
                        onRequestLogout = { showLogoutDialog = true },
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

            // 3. Sistema Visual & Temas
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
                    notifMorning = uiState.notifMorning,
                    syncIntervalMinutes = uiState.syncIntervalMinutes,
                    onNotif24hChange = { viewModel.setNotif24h(it) },
                    onNotif3hChange = { viewModel.setNotif3h(it) },
                    onNotif30mChange = { viewModel.setNotif30m(it) },
                    onNotifMorningChange = { viewModel.setNotifMorning(it) },
                    onSyncIntervalChange = { viewModel.setSyncIntervalMinutes(it) },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 5. Configuración de IA (Copilot / OpenRouter)
            item {
                CopilotSettingsSection(
                    enabled = uiState.copilotEnabled,
                    apiKey = uiState.openRouterApiKey,
                    selectedModel = uiState.openRouterModel,
                    totalTokens = uiState.totalCopilotTokens,
                    balance = uiState.openRouterBalance,
                    isLoadingBalance = uiState.isLoadingBalance,
                    onEnabledChange = { viewModel.setCopilotEnabled(it) },
                    onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                    onModelChange = { viewModel.setOpenRouterModel(it) },
                    onRefreshBalance = { viewModel.refreshOpenRouterBalance() },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            // 6. Seguridad y Zona Crítica
            item {
                SecurityAndKeystoreSettingsSection(
                    biometricLock = uiState.biometricLock,
                    onBiometricLockChange = { viewModel.setBiometricLock(it) },
                    lastSyncText = uiState.lastSyncAgoText,
                    apiLatencyMs = uiState.apiLatencyMs,
                    onRequestLogout = { showLogoutDialog = true },
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
