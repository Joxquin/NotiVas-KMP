package me.joxquin.notivas.ui.ajustes

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppLanguage
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.logout_confirm
import me.joxquin.notivas.shared.resources.logout_dialog_text
import me.joxquin.notivas.shared.resources.logout_dialog_title
import me.joxquin.notivas.shared.resources.settings_categories_section
import me.joxquin.notivas.shared.resources.settings_category_about
import me.joxquin.notivas.shared.resources.settings_category_about_desc
import me.joxquin.notivas.shared.resources.settings_category_appearance
import me.joxquin.notivas.shared.resources.settings_category_appearance_desc
import me.joxquin.notivas.shared.resources.settings_category_copilot
import me.joxquin.notivas.shared.resources.settings_category_copilot_desc
import me.joxquin.notivas.shared.resources.settings_category_drive
import me.joxquin.notivas.shared.resources.settings_category_drive_desc
import me.joxquin.notivas.shared.resources.settings_category_notifications
import me.joxquin.notivas.shared.resources.settings_category_notifications_desc
import me.joxquin.notivas.shared.resources.settings_subtitle
import me.joxquin.notivas.shared.resources.settings_title
import me.joxquin.notivas.shared.resources.sync
import me.joxquin.notivas.ui.ajustes.components.PairWebSessionDialog
import me.joxquin.notivas.ui.ajustes.components.SettingsProfileHeroHeader
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.profile.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

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
 * Presenta el perfil y hero trio de acciones arriba, y navega a subpantallas temáticas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: ProfileViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToCopilot: () -> Unit = {},
    onNavigateToDrive: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPairWebDialog by remember { mutableStateOf(false) }

    if (showPairWebDialog) {
        PairWebSessionDialog(
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

    // ─── MODO MIUIX ─────────────────────────────────────────────────────────
    if (themeStyle == AppThemeStyle.MIUIX) {
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
                                        contentDescription = stringResource(Res.string.sync),
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                                    )
                                }
                            }
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = paddingValues,
                extraTop = 16.dp,
                extraBottom = 32.dp
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixBackdropModifier(backdrop)
                        .miuixPageScrollModifiers(scrollBehavior)
                ) {
                    // 1. Hero Header con Avatar y Trío de Acciones
                    item {
                        SettingsProfileHeroHeader(
                            uiState = uiState,
                            onVerifyToken = { viewModel.verifyToken() },
                            onOpenDrive = onNavigateToDrive,
                            onPairWeb = { showPairWebDialog = true },
                            onRequestLogout = { showLogoutDialog = true },
                            themeStyle = AppThemeStyle.MIUIX
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. Secciones / Categorías
                    item {
                        SmallTitle(text = stringResource(Res.string.settings_categories_section))
                    }

                    item {
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            val appearanceSummary = when (uiState.themeStyle) {
                                AppThemeStyle.MIUIX -> "HyperOS MIUIX"
                                AppThemeStyle.MATERIAL -> "Material 3"
                            } + " • " + when (uiState.appLanguage) {
                                AppLanguage.SPANISH -> "Español"
                                AppLanguage.ENGLISH -> "English"
                                AppLanguage.SYSTEM -> "Sistema"
                            }

                            ArrowPreference(
                                title = stringResource(Res.string.settings_category_appearance),
                                summary = appearanceSummary,
                                onClick = onNavigateToAppearance
                            )

                            val notifSummary = "${uiState.syncIntervalMinutes} min"
                            ArrowPreference(
                                title = stringResource(Res.string.settings_category_notifications),
                                summary = notifSummary,
                                onClick = onNavigateToNotifications
                            )

                            val copilotSummary = if (uiState.copilotEnabled) {
                                when (uiState.copilotProvider) {
                                    me.joxquin.notivas.data.local.CopilotProvider.OPENROUTER -> "OpenRouter"
                                    me.joxquin.notivas.data.local.CopilotProvider.GOOGLE_AI_STUDIO -> "Google AI Studio"
                                }
                            } else {
                                "Desactivado"
                            }
                            ArrowPreference(
                                title = stringResource(Res.string.settings_category_copilot),
                                summary = copilotSummary,
                                onClick = onNavigateToCopilot
                            )

                            val driveSummary = if (uiState.googleDriveConnected) {
                                uiState.googleDriveEmail ?: "Conectado"
                            } else {
                                "Sin vincular"
                            }
                            ArrowPreference(
                                title = stringResource(Res.string.settings_category_drive),
                                summary = driveSummary,
                                onClick = onNavigateToDrive
                            )

                            ArrowPreference(
                                title = stringResource(Res.string.settings_category_about),
                                summary = stringResource(Res.string.settings_category_about_desc),
                                onClick = onNavigateToAbout
                            )
                        }
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
                                text = stringResource(Res.string.settings_title),
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
                            contentDescription = stringResource(Res.string.sync),
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Contextual
            item {
                AjustesHeaderSection()
            }

            // 2. Hero Header con Avatar y Trío de Acciones
            item {
                SettingsProfileHeroHeader(
                    uiState = uiState,
                    onVerifyToken = { viewModel.verifyToken() },
                    onOpenDrive = onNavigateToDrive,
                    onPairWeb = { showPairWebDialog = true },
                    onRequestLogout = { showLogoutDialog = true },
                    themeStyle = AppThemeStyle.MATERIAL
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 3. Título de Secciones
            item {
                Text(
                    text = stringResource(Res.string.settings_categories_section),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // 4. Categorías en Tarjetas M3
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val appearanceSummary = when (uiState.themeStyle) {
                        AppThemeStyle.MIUIX -> "HyperOS MIUIX"
                        AppThemeStyle.MATERIAL -> "Material 3"
                    } + " • " + when (uiState.appLanguage) {
                        AppLanguage.SPANISH -> "Español"
                        AppLanguage.ENGLISH -> "English"
                        AppLanguage.SYSTEM -> "Sistema"
                    }

                    MaterialCategoryCard(
                        title = stringResource(Res.string.settings_category_appearance),
                        subtitle = appearanceSummary,
                        icon = Icons.Default.Palette,
                        onClick = onNavigateToAppearance
                    )

                    val notifSummary = "${uiState.syncIntervalMinutes} min"
                    MaterialCategoryCard(
                        title = stringResource(Res.string.settings_category_notifications),
                        subtitle = notifSummary,
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotifications
                    )

                    val copilotSummary = if (uiState.copilotEnabled) {
                        when (uiState.copilotProvider) {
                            me.joxquin.notivas.data.local.CopilotProvider.OPENROUTER -> "OpenRouter"
                            me.joxquin.notivas.data.local.CopilotProvider.GOOGLE_AI_STUDIO -> "Google AI Studio"
                        }
                    } else {
                        "Desactivado"
                    }
                    MaterialCategoryCard(
                        title = stringResource(Res.string.settings_category_copilot),
                        subtitle = copilotSummary,
                        icon = Icons.Default.AutoAwesome,
                        onClick = onNavigateToCopilot
                    )

                    val driveSummary = if (uiState.googleDriveConnected) {
                        uiState.googleDriveEmail ?: "Conectado"
                    } else {
                        "Sin vincular"
                    }
                    MaterialCategoryCard(
                        title = stringResource(Res.string.settings_category_drive),
                        subtitle = driveSummary,
                        icon = Icons.Default.Cloud,
                        onClick = onNavigateToDrive
                    )

                    MaterialCategoryCard(
                        title = stringResource(Res.string.settings_category_about),
                        subtitle = stringResource(Res.string.settings_category_about_desc),
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
