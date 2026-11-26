package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults as M3ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.copilot_action_delete
import me.joxquin.notivas.shared.resources.copilot_action_rename
import me.joxquin.notivas.shared.resources.copilot_btn_cancel
import me.joxquin.notivas.shared.resources.copilot_delete_query_confirm_message
import me.joxquin.notivas.shared.resources.copilot_btn_new_query
import me.joxquin.notivas.shared.resources.copilot_btn_save
import me.joxquin.notivas.shared.resources.copilot_history_empty_hint
import me.joxquin.notivas.shared.resources.copilot_history_no_results
import me.joxquin.notivas.shared.resources.copilot_history_search_placeholder
import me.joxquin.notivas.shared.resources.copilot_history_title
import me.joxquin.notivas.shared.resources.copilot_no_past_queries
import me.joxquin.notivas.shared.resources.copilot_rename_query_title
import me.joxquin.notivas.shared.resources.copilot_tokens_count
import me.joxquin.notivas.shared.resources.delete
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.components.miuix.GlassConfirmDialog
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import me.joxquin.notivas.ui.components.miuix.SearchModeEasing
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.BookOutline
import top.yukonga.miuix.kmp.icon.glass.Close as GlassClose
import top.yukonga.miuix.kmp.icon.glass.Search as GlassSearch
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Overlay de historial de consultas de Copilot con input de busqueda reactivo integrado.
 */
@Composable
fun CopilotHistoryOverlay(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sessions: List<CopilotSession>,
    currentSessionId: String?,
    onSelectSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onRenameSession: (String, String) -> Unit,
    onDeleteSession: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
) {
    var searchQuery by remember { mutableStateOf("") }
    var sessionToRename by remember { mutableStateOf<CopilotSession?>(null) }
    var renameText by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<CopilotSession?>(null) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(expanded) {
        if (!expanded) {
            searchQuery = ""
            focusManager.clearFocus()
        }
    }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            val query = searchQuery.trim()
            sessions.filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    // Dialogo para renombrar sesion
    val renameShow = sessionToRename != null
    val deleteShow = sessionToDelete != null

    if (themeStyle == AppThemeStyle.MIUIX) {
        val renameSession = sessionToRename
        GlassDialog(
            show = renameShow,
            backdrop = backdrop,
            title = stringResource(Res.string.copilot_rename_query_title),
            onDismissRequest = { sessionToRename = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiuixTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassDialogActions {
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        onClick = { sessionToRename = null },
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.copilot_btn_cancel),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        role = GlassDialogButtonRole.Primary,
                        onClick = {
                            if (renameShow && renameSession != null && renameText.isNotBlank()) {
                                onRenameSession(renameSession.id, renameText.trim())
                            }
                            if (renameShow) {
                                sessionToRename = null
                            }
                        },
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.copilot_btn_save),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    } else if (renameShow) {
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text(stringResource(Res.string.copilot_rename_query_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val session = sessionToRename
                        if (session != null && renameText.isNotBlank()) {
                            onRenameSession(session.id, renameText.trim())
                        }
                        sessionToRename = null
                    }
                ) {
                    Text(stringResource(Res.string.copilot_btn_save))
                }
            },
            dismissButton = {
                M3TextButton(onClick = { sessionToRename = null }) {
                    Text(stringResource(Res.string.copilot_btn_cancel))
                }
            }
        )
    }

    // Dialogo para confirmar eliminacion de sesion
    if (themeStyle == AppThemeStyle.MIUIX) {
        val deleteSession = sessionToDelete
        GlassConfirmDialog(
            show = deleteShow,
            backdrop = backdrop,
            title = stringResource(Res.string.copilot_action_delete),
            summary = deleteSession?.title?.let {
                stringResource(Res.string.copilot_delete_query_confirm_message, it)
            },
            cancelLabel = stringResource(Res.string.copilot_btn_cancel),
            confirmLabel = stringResource(Res.string.delete),
            onDismissRequest = { sessionToDelete = null },
            onConfirm = {
                if (deleteShow && deleteSession != null) {
                    onDeleteSession(deleteSession.id)
                    sessionToDelete = null
                }
            },
            confirmRole = GlassDialogButtonRole.Destructive,
        )
    } else if (deleteShow) {
        val session = sessionToDelete!!
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text(stringResource(Res.string.copilot_action_delete)) },
            text = { Text(stringResource(Res.string.copilot_delete_query_confirm_message, session.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSession(session.id)
                        sessionToDelete = null
                    },
                    colors = M3ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                M3TextButton(onClick = { sessionToDelete = null }) {
                    Text(stringResource(Res.string.copilot_btn_cancel))
                }
            }
        )
    }

    BackHandler(enabled = expanded) {
        onDismissRequest()
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val transition = updateTransition(expanded, label = "copilotGlassSearch")
        val progress = transition.animateFloat(
            transitionSpec = { tween(400, easing = SearchModeEasing) },
            label = "copilotSearchGeometry",
        ) { if (it) 1f else 0f }

        val cancelAlpha = transition.animateFloat(
            transitionSpec = { tween(150, delayMillis = if (targetState) 100 else 0, easing = LinearEasing) },
            label = "copilotSearchCancelMaterial",
        ) { if (it) 1f else 0f }

        val active = expanded || transition.currentState || transition.isRunning

        val density = LocalDensity.current
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(expanded) {
            if (expanded) {
                focusRequester.requestFocus()
                keyboard?.show()
            } else if (active) {
                focusManager.clearFocus()
                keyboard?.hide()
            }
        }

        if (!active) return

        val direction = LocalLayoutDirection.current
        val statusBarHeight = WindowInsets.statusBars.getTop(density)
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val colors = MiuixTheme.colorScheme
        val cancelDesc = stringResource(Res.string.cancel)

        Box(
            modifier = modifier
                .fillMaxSize()
                .onPreviewKeyEvent {
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                        onDismissRequest()
                        true
                    } else {
                        false
                    }
                }
        ) {
            // Scrim que atenúa el fondo detrás del overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value }
                    .background(colors.surface)
                    .clickable(interactionSource = null, indication = null) {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .imePadding()
            ) {
                // Cabecera elástica que desciende desde el status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .graphicsLayer { translationY = -statusBarHeight * (1f - progress.value) }
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        enabled = expanded,
                        singleLine = true,
                        textStyle = MiuixTheme.textStyles.body1.copy(color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboard?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .layout { measurable, constraints ->
                                val endSpace = (54.dp.toPx() * progress.value).toInt()
                                val width = (constraints.maxWidth - endSpace).coerceAtLeast(0)
                                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                                layout(constraints.maxWidth, placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .height(44.dp)
                            .focusRequester(focusRequester),
                        decorationBox = { field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.surfaceContainerHigh, CircleShape)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MiuixIcon(
                                    imageVector = MiuixIcons.Glass.GlassSearch,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = colors.onSurfaceVariantSummary
                                )
                                Box(Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        MiuixText(
                                            text = stringResource(Res.string.copilot_history_search_placeholder),
                                            color = colors.onSurfaceVariantSummary,
                                            style = MiuixTheme.textStyles.body1
                                        )
                                    }
                                    field()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    MiuixIconButton(
                                        onClick = {
                                            searchQuery = ""
                                            focusRequester.requestFocus()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        MiuixIcon(
                                            imageVector = MiuixIcons.Glass.GlassClose,
                                            contentDescription = cancelDesc,
                                            modifier = Modifier.size(18.dp),
                                            tint = colors.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Botón circular Cancelar / Cerrar con animación angular lateral y blur
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .graphicsLayer {
                                val fraction = progress.value
                                translationX = 66.dp.toPx() * (1f - fraction) * if (direction == LayoutDirection.Ltr) 1f else -1f
                                scaleX = fraction
                                scaleY = fraction
                                alpha = cancelAlpha.value
                                val blur = 50f * (1f - cancelAlpha.value)
                                renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                            }
                    ) {
                        MiuixIconButton(
                            onClick = onDismissRequest,
                            enabled = expanded,
                            modifier = Modifier
                                .size(44.dp)
                                .background(colors.surfaceContainerHigh, CircleShape)
                        ) {
                            MiuixIcon(
                                imageVector = MiuixIcons.Glass.GlassClose,
                                contentDescription = cancelDesc,
                                modifier = Modifier.size(24.dp),
                                tint = colors.onSurface
                            )
                        }
                    }
                }

                // Cuerpo del historial: Botón nueva consulta y lista de sesiones con desvanecimiento coordinado
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = progress.value }
                ) {
                    val listState = rememberLazyListState()

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Acción rápida: Nueva consulta
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .squircleSurface(
                                        color = colors.primary.copy(alpha = 0.12f),
                                        cornerRadius = 14.dp
                                    )
                                    .clickable {
                                        onNewSession()
                                        onDismissRequest()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MiuixIcon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    MiuixText(
                                        text = stringResource(Res.string.copilot_btn_new_query),
                                        style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        // Lista o estado vacío
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when {
                                sessions.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 32.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        MiuixIcon(
                                            imageVector = MiuixIcons.Glass.BookOutline,
                                            contentDescription = null,
                                            tint = colors.onSurfaceVariantSummary,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        MiuixText(
                                            text = stringResource(Res.string.copilot_no_past_queries),
                                            style = MiuixTheme.textStyles.title3,
                                            color = colors.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        MiuixText(
                                            text = stringResource(Res.string.copilot_history_empty_hint),
                                            style = MiuixTheme.textStyles.footnote1,
                                            color = colors.onSurfaceVariantSummary
                                        )
                                    }
                                }
                                filteredSessions.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 32.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        MiuixText(
                                            text = stringResource(Res.string.copilot_history_no_results, searchQuery),
                                            style = MiuixTheme.textStyles.body1,
                                            color = colors.onSurfaceVariantSummary
                                        )
                                    }
                                }
                                else -> {
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = navBarBottom + 24.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        item {
                                            SmallTitle(
                                                text = stringResource(Res.string.copilot_history_title),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }

                                        items(filteredSessions, key = { it.id }) { session ->
                                            val isSelected = session.id == currentSessionId
                                            HistorySessionRow(
                                                session = session,
                                                isSelected = isSelected,
                                                onSelect = {
                                                    onSelectSession(session.id)
                                                    onDismissRequest()
                                                },
                                                onRename = {
                                                    sessionToRename = session
                                                    renameText = session.title
                                                },
                                                onDelete = {
                                                    sessionToDelete = session
                                                },
                                                themeStyle = AppThemeStyle.MIUIX
                                            )
                                        }
                                    }

                                    VerticalScrollBar(
                                        adapter = rememberScrollBarAdapter(listState),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .fillMaxHeight(),
                                        trackPadding = PaddingValues(top = 8.dp, bottom = navBarBottom + 24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(280)) { it / 3 },
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(220)) { it / 3 },
            modifier = modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val listState = rememberLazyListState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = statusBarTop + 8.dp)
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(Res.string.copilot_history_search_placeholder),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        M3TextButton(onClick = onDismissRequest) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }

                    // Accion rapida: Nueva consulta (Material 3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onNewSession()
                                    onDismissRequest()
                                },
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.copilot_btn_new_query),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Contenido principal de la lista (Material 3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when {
                            sessions.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 32.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(Res.string.copilot_no_past_queries),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(Res.string.copilot_history_empty_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            filteredSessions.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 32.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(Res.string.copilot_history_no_results, searchQuery),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 8.dp,
                                        bottom = navBarBottom + 24.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            text = stringResource(Res.string.copilot_history_title),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }

                                    items(filteredSessions, key = { it.id }) { session ->
                                        val isSelected = session.id == currentSessionId
                                        HistorySessionRow(
                                            session = session,
                                            isSelected = isSelected,
                                            onSelect = {
                                                onSelectSession(session.id)
                                                onDismissRequest()
                                            },
                                            onRename = {
                                                sessionToRename = session
                                                renameText = session.title
                                            },
                                            onDelete = {
                                                sessionToDelete = session
                                            },
                                            themeStyle = AppThemeStyle.MATERIAL
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
}

@Composable
private fun HistorySessionRow(
    session: CopilotSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    themeStyle: AppThemeStyle
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = if (isSelected) MiuixTheme.colorScheme.surfaceContainerHigh else MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 14.dp
                )
                .squircleBorder(
                    width = if (isSelected) 1.5.dp else 0.5.dp,
                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.dividerLine,
                    cornerRadius = 14.dp
                )
                .clickable(onClick = onSelect)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiuixIcon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        MiuixText(
                            text = session.title,
                            style = MiuixTheme.textStyles.body1.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (session.totalTokens > 0) {
                            MiuixText(
                                text = stringResource(Res.string.copilot_tokens_count, session.totalTokens),
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRename, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.copilot_action_rename),
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.copilot_action_delete),
                            tint = MiuixTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onSelect),
            color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (session.totalTokens > 0) {
                            Text(
                                text = stringResource(Res.string.copilot_tokens_count, session.totalTokens),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRename, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.copilot_action_rename),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.copilot_action_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
