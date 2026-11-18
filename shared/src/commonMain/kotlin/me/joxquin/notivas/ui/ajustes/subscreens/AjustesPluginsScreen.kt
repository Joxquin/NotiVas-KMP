package me.joxquin.notivas.ui.ajustes.subscreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.CardDefaults as M3CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.plugins.tecsup.api.TecsupApiClient
import me.joxquin.notivas.plugins.tecsup.api.TecsupEndpoints
import me.joxquin.notivas.plugins.tecsup.model.TecsupUser
import me.joxquin.notivas.plugins.tecsup.session.TecsupSessionManager
import me.joxquin.notivas.plugins.tecsup.session.TecsupSessionState
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.TecsupAuthWebView
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesPluginsScreen(
    themeStyle: AppThemeStyle,
    isTecsupEnabled: Boolean,
    onTecsupEnabledChange: (Boolean) -> Unit,
    sessionManager: TecsupSessionManager,
    apiClient: TecsupApiClient,
    onNavigateToTecsupDetail: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<TecsupUser?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }

    var showWebViewLogin by remember { mutableStateOf(false) }
    var showManualTokenDialog by remember { mutableStateOf(false) }
    var manualTokenInput by remember { mutableStateOf("") }

    LaunchedEffect(sessionState) {
        if (sessionState is TecsupSessionState.Connected) {
            isLoadingUser = true
            apiClient.fetchUserInfo().onSuccess {
                user = it
            }
            isLoadingUser = false
        } else {
            user = null
        }
    }

    val title = stringResource(Res.string.plugins_title)

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()

        MiuixScaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = title,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            BackNavigationIcon(onClick = onBack)
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
            val contentPadding = miuixPageContentPadding(
                innerPadding = paddingValues,
                extraTop = 8.dp,
                extraStart = 0.dp,
                extraEnd = 0.dp,
                extraBottom = if (isWideScreen) 24.dp else 32.dp
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixBackdropModifier(backdrop)
                        .miuixPageScrollModifiers(scrollBehavior),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        SmallTitle(
                            text = stringResource(Res.string.plugins_available_section),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.None
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MiuixTheme.colorScheme.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(Res.string.plugin_tecsup_title),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isTecsupEnabled) {
                                                stringResource(Res.string.plugins_active_badge)
                                            } else {
                                                stringResource(Res.string.plugins_inactive_badge)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isTecsupEnabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                    MiuixSwitch(
                                        checked = isTecsupEnabled,
                                        onCheckedChange = onTecsupEnabledChange
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = stringResource(Res.string.plugin_tecsup_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )

                                AnimatedVisibility(visible = isTecsupEnabled) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MiuixTheme.colorScheme.dividerLine)
                                        )
                                        Spacer(Modifier.height(12.dp))

                                        when (sessionState) {
                                            is TecsupSessionState.Connected -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(MiuixTheme.colorScheme.primary)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = stringResource(Res.string.plugin_tecsup_session_connected),
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                            color = MiuixTheme.colorScheme.onSurface
                                                        )
                                                        user?.let { u ->
                                                            Text(
                                                                text = "${u.name} (${u.studentCode})",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    MiuixTextButton(
                                                        text = stringResource(Res.string.plugin_tecsup_action_open_panel),
                                                        onClick = onNavigateToTecsupDetail,
                                                        colors = MiuixButtonDefaults.textButtonColorsPrimary(),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    MiuixTextButton(
                                                        text = stringResource(Res.string.plugin_tecsup_action_logout),
                                                        onClick = {
                                                            scope.launch { sessionManager.logout() }
                                                        },
                                                        colors = MiuixButtonDefaults.textButtonColors(),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            else -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = stringResource(Res.string.plugin_tecsup_session_disconnected),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                    )
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    MiuixTextButton(
                                                        text = stringResource(Res.string.plugin_tecsup_action_login),
                                                        onClick = { showWebViewLogin = true },
                                                        colors = MiuixButtonDefaults.textButtonColorsPrimary(),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    MiuixTextButton(
                                                        text = stringResource(Res.string.plugin_tecsup_action_manual_token),
                                                        onClick = { showManualTokenDialog = true },
                                                        colors = MiuixButtonDefaults.textButtonColors(),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 32.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.plugins_available_section),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                item {
                    M3Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = M3CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.plugin_tecsup_title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isTecsupEnabled) {
                                            stringResource(Res.string.plugins_active_badge)
                                        } else {
                                            stringResource(Res.string.plugins_inactive_badge)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isTecsupEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                M3Switch(
                                    checked = isTecsupEnabled,
                                    onCheckedChange = onTecsupEnabledChange
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = stringResource(Res.string.plugin_tecsup_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(visible = isTecsupEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    when (sessionState) {
                                        is TecsupSessionState.Connected -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = stringResource(Res.string.plugin_tecsup_session_connected),
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    user?.let { u ->
                                                        Text(
                                                            text = "${u.name} (${u.studentCode})",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.height(14.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onNavigateToTecsupDetail,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(stringResource(Res.string.plugin_tecsup_action_open_panel))
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        scope.launch { sessionManager.logout() }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(stringResource(Res.string.plugin_tecsup_action_logout))
                                                }
                                            }
                                        }
                                        else -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.outline)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(Res.string.plugin_tecsup_session_disconnected),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(Modifier.height(14.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { showWebViewLogin = true },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(stringResource(Res.string.plugin_tecsup_action_login))
                                                }
                                                OutlinedButton(
                                                    onClick = { showManualTokenDialog = true },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(stringResource(Res.string.plugin_tecsup_action_manual_token))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for WebView Login
    if (showWebViewLogin) {
        Dialog(
            onDismissRequest = { showWebViewLogin = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.plugin_tecsup_auth_webview_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showWebViewLogin = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.cancel)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        TecsupAuthWebView(
                            authUrl = TecsupEndpoints.LOGIN_AZURE_URL,
                            onTokenReceived = { token ->
                                scope.launch {
                                    sessionManager.saveToken(token)
                                    showWebViewLogin = false
                                }
                            },
                            onCancel = { showWebViewLogin = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Dialog for Manual Token Entry
    if (showManualTokenDialog) {
        if (themeStyle == AppThemeStyle.MIUIX) {
            WindowDialog(
                show = true,
                title = stringResource(Res.string.plugin_tecsup_dialog_manual_title),
                summary = stringResource(Res.string.plugin_tecsup_dialog_manual_desc),
                onDismissRequest = { showManualTokenDialog = false },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiuixTextField(
                            value = manualTokenInput,
                            onValueChange = { manualTokenInput = it },
                            label = stringResource(Res.string.plugin_tecsup_dialog_manual_label),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiuixTextButton(
                                text = stringResource(Res.string.cancel),
                                onClick = { showManualTokenDialog = false },
                                modifier = Modifier.weight(1f)
                            )
                            MiuixTextButton(
                                text = stringResource(Res.string.confirm),
                                onClick = {
                                    val token = manualTokenInput.trim()
                                    if (token.isNotEmpty()) {
                                        scope.launch {
                                            sessionManager.saveToken(token)
                                            showManualTokenDialog = false
                                            manualTokenInput = ""
                                        }
                                    }
                                },
                                colors = MiuixButtonDefaults.textButtonColorsPrimary(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showManualTokenDialog = false },
                title = { Text(stringResource(Res.string.plugin_tecsup_dialog_manual_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.plugin_tecsup_dialog_manual_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = manualTokenInput,
                            onValueChange = { manualTokenInput = it },
                            label = { Text(stringResource(Res.string.plugin_tecsup_dialog_manual_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val token = manualTokenInput.trim()
                            if (token.isNotEmpty()) {
                                scope.launch {
                                    sessionManager.saveToken(token)
                                    showManualTokenDialog = false
                                    manualTokenInput = ""
                                }
                            }
                        }
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualTokenDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }
}
