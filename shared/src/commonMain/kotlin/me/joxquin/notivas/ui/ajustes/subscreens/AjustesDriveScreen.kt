package me.joxquin.notivas.ui.ajustes.subscreens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_category_drive
import me.joxquin.notivas.ui.ajustes.components.GoogleDriveSettingsSection
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesDriveScreen(
    connected: Boolean,
    email: String?,
    lastSyncText: String,
    cacheSize: String,
    onAuthSuccess: (token: String, refresh: String?, email: String?) -> Unit,
    onUnlink: () -> Unit,
    onClearCache: () -> Unit,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val title = stringResource(Res.string.settings_category_drive)

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
            val contentPadding = miuixPageContentPadding(
                innerPadding = paddingValues,
                extraTop = 16.dp,
                extraBottom = 32.dp
            )

            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixBackdropModifier(backdrop)
                        .miuixPageScrollModifiers(scrollBehavior)
                ) {
                    item {
                        GoogleDriveSettingsSection(
                            connected = connected,
                            email = email,
                            lastSyncText = lastSyncText,
                            cacheSize = cacheSize,
                            onAuthSuccess = onAuthSuccess,
                            onUnlink = onUnlink,
                            onClearCache = onClearCache,
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 32.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    GoogleDriveSettingsSection(
                        connected = connected,
                        email = email,
                        lastSyncText = lastSyncText,
                        cacheSize = cacheSize,
                        onAuthSuccess = onAuthSuccess,
                        onUnlink = onUnlink,
                        onClearCache = onClearCache,
                        themeStyle = AppThemeStyle.MATERIAL
                    )
                }
            }
        }
    }
}
