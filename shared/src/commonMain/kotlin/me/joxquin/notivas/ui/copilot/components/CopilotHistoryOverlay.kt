package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.copilot_action_delete
import me.joxquin.notivas.shared.resources.copilot_action_rename
import me.joxquin.notivas.shared.resources.copilot_btn_cancel
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
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Book
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
    if (sessionToRename != null) {
        if (themeStyle == AppThemeStyle.MIUIX) {
            AlertDialog(
                onDismissRequest = { sessionToRename = null },
                title = {
                    MiuixText(
                        text = stringResource(Res.string.copilot_rename_query_title),
                        style = MiuixTheme.textStyles.title3,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiuixTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    MiuixTextButton(
                        text = stringResource(Res.string.copilot_btn_save),
                        onClick = {
                            val session = sessionToRename
                            if (session != null && renameText.isNotBlank()) {
                                onRenameSession(session.id, renameText.trim())
                            }
                            sessionToRename = null
                        },
                        colors = MiuixButtonDefaults.textButtonColorsPrimary()
                    )
                },
                dismissButton = {
                    MiuixTextButton(
                        text = stringResource(Res.string.copilot_btn_cancel),
                        onClick = { sessionToRename = null }
                    )
                }
            )
        } else {
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
    }

    // Dialogo para confirmar eliminacion de sesion
    if (sessionToDelete != null) {
        val session = sessionToDelete!!
        if (themeStyle == AppThemeStyle.MIUIX) {
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = {
                    MiuixText(
                        text = stringResource(Res.string.copilot_action_delete),
                        style = MiuixTheme.textStyles.title3,
                        color = MiuixTheme.colorScheme.error
                    )
                },
                text = {
                    MiuixText(
                        text = "¿Deseas eliminar permanentemente esta consulta: \"${session.title}\"?",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    MiuixTextButton(
                        text = stringResource(Res.string.delete),
                        onClick = {
                            onDeleteSession(session.id)
                            sessionToDelete = null
                        },
                        colors = MiuixButtonDefaults.textButtonColors(
                            color = MiuixTheme.colorScheme.errorContainer,
                            textColor = MiuixTheme.colorScheme.onErrorContainer
                        )
                    )
                },
                dismissButton = {
                    MiuixTextButton(
                        text = stringResource(Res.string.copilot_btn_cancel),
                        onClick = { sessionToDelete = null }
                    )
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = { Text(stringResource(Res.string.copilot_action_delete)) },
                text = { Text("¿Deseas eliminar permanentemente esta consulta: \"${session.title}\"?") },
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
    }

    BackHandler(enabled = expanded) {
        onDismissRequest()
    }

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(280)) { it / 3 },
        exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(220)) { it / 3 },
        modifier = modifier.fillMaxSize()
    ) {
        val backgroundColor = if (themeStyle == AppThemeStyle.MIUIX) {
            MiuixTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
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
                // Cabecera superior con barra de busqueda y boton Cancelar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search bar capsule
                    if (themeStyle == AppThemeStyle.MIUIX) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 22.dp
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiuixIcon(
                                imageVector = MiuixIcons.Glass.GlassSearch,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(20.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    MiuixText(
                                        text = stringResource(Res.string.copilot_history_search_placeholder),
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MiuixTheme.textStyles.body2.copy(
                                        color = MiuixTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .clickable { searchQuery = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MiuixIcon(
                                        imageVector = MiuixIcons.Glass.GlassClose,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        MiuixTextButton(
                            text = stringResource(Res.string.cancel),
                            onClick = onDismissRequest
                        )
                    } else {
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
                }

                // Accion rapida: Nueva consulta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (themeStyle == AppThemeStyle.MIUIX) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
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
                                    tint = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                MiuixText(
                                    text = stringResource(Res.string.copilot_btn_new_query),
                                    style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Bold),
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
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
                }

                // Contenido principal de la lista
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        sessions.isEmpty() -> {
                            // Estado sin historial
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (themeStyle == AppThemeStyle.MIUIX) {
                                    MiuixIcon(
                                        imageVector = MiuixIcons.Glass.Book,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MiuixText(
                                        text = stringResource(Res.string.copilot_no_past_queries),
                                        style = MiuixTheme.textStyles.title3,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    MiuixText(
                                        text = stringResource(Res.string.copilot_history_empty_hint),
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                } else {
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
                        }
                        filteredSessions.isEmpty() -> {
                            // Estado sin resultados para la busqueda
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (themeStyle == AppThemeStyle.MIUIX) {
                                    MiuixText(
                                        text = stringResource(Res.string.copilot_history_no_results, searchQuery),
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                } else {
                                    Text(
                                        text = stringResource(Res.string.copilot_history_no_results, searchQuery),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        else -> {
                            // Lista de sesiones
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
                                    if (themeStyle == AppThemeStyle.MIUIX) {
                                        SmallTitle(
                                            text = stringResource(Res.string.copilot_history_title),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(Res.string.copilot_history_title),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
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
                                        themeStyle = themeStyle
                                    )
                                }
                            }

                            if (themeStyle == AppThemeStyle.MIUIX) {
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
