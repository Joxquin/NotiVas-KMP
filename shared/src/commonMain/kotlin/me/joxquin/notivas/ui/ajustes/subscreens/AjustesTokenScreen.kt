package me.joxquin.notivas.ui.ajustes.subscreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.AuthMode
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_token_subtitle
import me.joxquin.notivas.shared.resources.settings_token_title
import me.joxquin.notivas.shared.resources.token_active_expires_format
import me.joxquin.notivas.shared.resources.token_active_expires_label
import me.joxquin.notivas.shared.resources.token_active_info_title
import me.joxquin.notivas.shared.resources.token_active_no_expiration
import me.joxquin.notivas.shared.resources.token_active_value_label
import me.joxquin.notivas.shared.resources.token_auto_renew_desc
import me.joxquin.notivas.shared.resources.token_auto_renew_title
import me.joxquin.notivas.shared.resources.token_auto_renew_web_disabled_hint
import me.joxquin.notivas.shared.resources.token_banner_web_session_desc
import me.joxquin.notivas.shared.resources.token_banner_web_session_title
import me.joxquin.notivas.shared.resources.token_copy_clipboard
import me.joxquin.notivas.shared.resources.token_create_student_token_btn
import me.joxquin.notivas.shared.resources.token_create_student_token_error
import me.joxquin.notivas.shared.resources.token_create_student_token_loading
import me.joxquin.notivas.shared.resources.token_create_student_token_success
import me.joxquin.notivas.shared.resources.token_new_error
import me.joxquin.notivas.shared.resources.token_new_input_label
import me.joxquin.notivas.shared.resources.token_new_save_btn
import me.joxquin.notivas.shared.resources.token_new_saving_btn
import me.joxquin.notivas.shared.resources.token_new_section_title
import me.joxquin.notivas.shared.resources.token_new_success
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.navigation.miuix.LocalMiuixSnackbarHostState
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesTokenScreen(
    authMode: AuthMode,
    currentToken: String?,
    autoRenewToken: Boolean,
    tokenExpiresAt: Long,
    isGeneratingToken: Boolean,
    isUpdatingToken: Boolean,
    onAutoRenewChange: (Boolean) -> Unit,
    onCreateStudentToken: suspend () -> Result<String>,
    onUpdateManualToken: suspend (String) -> Result<Boolean>,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val title = stringResource(Res.string.settings_token_title)
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isWebSession = authMode == AuthMode.COOKIE_SESSION

    var newTokenInput by remember { mutableStateOf("") }
    var isTokenVisible by remember { mutableStateOf(false) }

    // Notificaciones de feedback
    val miuixSnackbarHostState = LocalMiuixSnackbarHostState.current
    val materialSnackbarHostState = remember { SnackbarHostState() }

    val strTokenCopied = stringResource(Res.string.token_copy_clipboard)
    val strCreatedSuccess = stringResource(Res.string.token_create_student_token_success)
    val strCreatedError = stringResource(Res.string.token_create_student_token_error)
    val strUpdatedSuccess = stringResource(Res.string.token_new_success)
    val strUpdatedError = stringResource(Res.string.token_new_error)

    fun showFeedback(message: String) {
        coroutineScope.launch {
            if (themeStyle == AppThemeStyle.MIUIX) {
                miuixSnackbarHostState.showSnackbar(message)
            } else {
                materialSnackbarHostState.showSnackbar(message)
            }
        }
    }

    fun handleCopyToken(token: String) {
        clipboardManager.setText(AnnotatedString(token))
        showFeedback(strTokenCopied)
    }

    fun handleGenerateToken() {
        coroutineScope.launch {
            val res = onCreateStudentToken()
            if (res.isSuccess) {
                showFeedback(strCreatedSuccess)
            } else {
                showFeedback(strCreatedError)
            }
        }
    }

    fun handleSaveNewToken() {
        if (newTokenInput.isBlank()) return
        coroutineScope.launch {
            val res = onUpdateManualToken(newTokenInput)
            if (res.isSuccess) {
                newTokenInput = ""
                showFeedback(strUpdatedSuccess)
            } else {
                showFeedback(strUpdatedError)
            }
        }
    }

    // Cálculo de vencimiento
    val nowMillis = remember { DateTimeUtil.nowEpochMillis() }
    val daysRemaining = if (tokenExpiresAt > nowMillis) {
        ((tokenExpiresAt - nowMillis) / (24L * 60 * 60 * 1000)).toInt()
    } else {
        0
    }
    val formattedExpirationDate = if (tokenExpiresAt > 0L) {
        try {
            val date = DateTimeUtil.formatIso(tokenExpiresAt)
            date.take(10)
        } catch (_: Throwable) {
            "28 días"
        }
    } else {
        null
    }

    // Enmascaramiento de token
    val maskedToken = remember(currentToken) {
        if (currentToken.isNullOrBlank()) {
            "••••••••••••••••"
        } else if (currentToken.length > 8) {
            "••••••••" + currentToken.takeLast(6)
        } else {
            "••••••••"
        }
    }

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
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixPageScrollModifiers(scrollBehavior)
                        .miuixBackdropModifier(backdrop),
                    contentPadding = contentPadding
                ) {
                    // Si la sesión es web: banner informativo + acción principal destacada
                    if (isWebSession) {
                        item {
                            MiuixCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                insideMargin = PaddingValues(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(Res.string.token_banner_web_session_title),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(Res.string.token_banner_web_session_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        MiuixTextButton(
                                            text = if (isGeneratingToken) {
                                                stringResource(Res.string.token_create_student_token_loading)
                                            } else {
                                                stringResource(Res.string.token_create_student_token_btn)
                                            },
                                            onClick = { if (!isGeneratingToken) handleGenerateToken() },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.textButtonColorsPrimary()
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Tarjeta de información de token activo
                        item {
                            SmallTitle(text = stringResource(Res.string.token_active_info_title))
                        }

                        item {
                            MiuixCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                insideMargin = PaddingValues(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(Res.string.token_active_value_label),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isTokenVisible) (currentToken ?: "") else maskedToken,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MiuixTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                                Icon(
                                                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = MiuixTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            if (!currentToken.isNullOrBlank()) {
                                                IconButton(onClick = { handleCopyToken(currentToken) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = null,
                                                        tint = MiuixTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Vencimiento
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (tokenExpiresAt > 0L && formattedExpirationDate != null) {
                                                stringResource(Res.string.token_active_expires_format, daysRemaining, formattedExpirationDate)
                                            } else {
                                                stringResource(Res.string.token_active_no_expiration)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Autorenovar switch
                    item {
                        SmallTitle(text = stringResource(Res.string.token_auto_renew_title))
                    }

                    item {
                        val disabledAlpha = if (isWebSession) 0.5f else 1f
                        Box(modifier = Modifier.alpha(disabledAlpha)) {
                            MiuixCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)
                            ) {
                                SwitchPreference(
                                    title = stringResource(Res.string.token_auto_renew_title),
                                    summary = if (isWebSession) {
                                        stringResource(Res.string.token_auto_renew_web_disabled_hint)
                                    } else {
                                        stringResource(Res.string.token_auto_renew_desc)
                                    },
                                    checked = if (isWebSession) false else autoRenewToken,
                                    onCheckedChange = { if (!isWebSession) onAutoRenewChange(it) }
                                )
                            }
                        }
                    }

                    // Entrada de nuevo token
                    item {
                        SmallTitle(text = stringResource(Res.string.token_new_section_title))
                    }

                    item {
                        val disabledAlpha = if (isWebSession) 0.5f else 1f
                        Box(modifier = Modifier.alpha(disabledAlpha)) {
                            MiuixCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                insideMargin = PaddingValues(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    val fieldColors = MiuixTextFieldDefaults.textFieldColors()

                                    MiuixTextField(
                                        value = newTokenInput,
                                        onValueChange = { if (!isWebSession) newTokenInput = it },
                                        label = stringResource(Res.string.token_new_input_label),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = fieldColors
                                    )

                                    MiuixTextButton(
                                        text = if (isUpdatingToken) {
                                            stringResource(Res.string.token_new_saving_btn)
                                        } else {
                                            stringResource(Res.string.token_new_save_btn)
                                        },
                                        onClick = {
                                            if (!isWebSession && !isUpdatingToken) {
                                                handleSaveNewToken()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColorsPrimary()
                                    )
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
        return
    }

    // ─── MODO MATERIAL DESIGN 3 EXPRESSIVE ──────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(materialSnackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 40.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Banner web session en M3
            if (isWebSession) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.token_banner_web_session_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(Res.string.token_banner_web_session_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { if (!isGeneratingToken) handleGenerateToken() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isGeneratingToken) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(stringResource(Res.string.token_create_student_token_loading))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.token_create_student_token_btn))
                                }
                            }
                        }
                    }
                }
            } else {
                // Info Token Activo en M3
                item {
                    Text(
                        text = stringResource(Res.string.token_active_info_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.token_active_value_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isTokenVisible) (currentToken ?: "") else maskedToken,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                        Icon(
                                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (!currentToken.isNullOrBlank()) {
                                        IconButton(onClick = { handleCopyToken(currentToken) }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (tokenExpiresAt > 0L && formattedExpirationDate != null) {
                                        stringResource(Res.string.token_active_expires_format, daysRemaining, formattedExpirationDate)
                                    } else {
                                        stringResource(Res.string.token_active_no_expiration)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Switch de autorenovar en M3
            item {
                Text(
                    text = stringResource(Res.string.token_auto_renew_title),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                val disabledAlpha = if (isWebSession) 0.5f else 1f
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(disabledAlpha),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(Res.string.token_auto_renew_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isWebSession) {
                                        stringResource(Res.string.token_auto_renew_web_disabled_hint)
                                    } else {
                                        stringResource(Res.string.token_auto_renew_desc)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = if (isWebSession) false else autoRenewToken,
                            onCheckedChange = { if (!isWebSession) onAutoRenewChange(it) },
                            enabled = !isWebSession,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Entrada de nuevo token en M3
            item {
                Text(
                    text = stringResource(Res.string.token_new_section_title),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                val disabledAlpha = if (isWebSession) 0.5f else 1f
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(disabledAlpha),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = newTokenInput,
                            onValueChange = { if (!isWebSession) newTokenInput = it },
                            label = { Text(stringResource(Res.string.token_new_input_label)) },
                            singleLine = true,
                            enabled = !isWebSession,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        )

                        FilledTonalButton(
                            onClick = {
                                if (!isWebSession && !isUpdatingToken) {
                                    handleSaveNewToken()
                                }
                            },
                            enabled = !isWebSession && newTokenInput.isNotBlank() && !isUpdatingToken,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isUpdatingToken) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(stringResource(Res.string.token_new_saving_btn))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(Res.string.token_new_save_btn))
                            }
                        }
                    }
                }
            }
        }
    }
}
