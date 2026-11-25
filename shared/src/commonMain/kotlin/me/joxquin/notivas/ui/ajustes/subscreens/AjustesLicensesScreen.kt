package me.joxquin.notivas.ui.ajustes.subscreens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.back
import me.joxquin.notivas.shared.resources.licenses_close
import me.joxquin.notivas.shared.resources.licenses_footer_note
import me.joxquin.notivas.shared.resources.licenses_lib_notivas
import me.joxquin.notivas.shared.resources.licenses_load_error
import me.joxquin.notivas.shared.resources.licenses_loading
import me.joxquin.notivas.shared.resources.licenses_open_github
import me.joxquin.notivas.shared.resources.licenses_open_source_section
import me.joxquin.notivas.shared.resources.licenses_summary_format
import me.joxquin.notivas.shared.resources.licenses_title
import me.joxquin.notivas.shared.resources.licenses_app_section
import me.joxquin.notivas.ui.ajustes.LicenseEntry
import me.joxquin.notivas.ui.ajustes.LicenseKind
import me.joxquin.notivas.ui.ajustes.openSourceLicenseSections
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.util.LocalIsWideScreen
import me.joxquin.notivas.util.AppVersion
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesLicensesScreen(
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val title = stringResource(Res.string.licenses_title)
    val uriHandler = LocalUriHandler.current
    val appVersion = remember { AppVersion.get() }
    val licenseTexts = remember { mutableStateMapOf<String, String>() }
    var loadFailed by remember { mutableStateOf(false) }
    var dialogEntry by remember { mutableStateOf<LicenseEntry?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val appEntry = remember(appVersion.versionName) {
        LicenseEntry(
            nameRes = Res.string.licenses_lib_notivas,
            version = appVersion.versionName,
            kind = LicenseKind.MIT,
            url = "https://github.com/Joxquin/NotiVas-KMP"
        )
    }

    fun openUrl(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
        }
    }

    fun openLicense(entry: LicenseEntry) {
        dialogEntry = entry
        loadFailed = false
        showDialog = true
    }

    LaunchedEffect(dialogEntry, showDialog) {
        if (!showDialog) return@LaunchedEffect
        val entry = dialogEntry ?: return@LaunchedEffect
        val path = entry.kind.assetPath
        if (licenseTexts[path] == null) {
            runCatching { Res.readBytes(path).decodeToString() }
                .onSuccess { licenseTexts[path] = it }
                .onFailure { loadFailed = true }
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()
        val isWideScreen = LocalIsWideScreen.current

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
                extraTop = 8.dp,
                extraStart = 0.dp,
                extraEnd = 0.dp,
                extraBottom = if (isWideScreen) 24.dp else 32.dp
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = contentPadding,
                    overscrollEffect = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixBackdropModifier(backdrop)
                        .miuixPageScrollModifiers(scrollBehavior)
                ) {
                    item {
                        SmallTitle(text = stringResource(Res.string.licenses_app_section))
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            ArrowPreference(
                                title = stringResource(appEntry.nameRes),
                                summary = licenseSummaryText(appEntry.version, appEntry.kind),
                                onClick = { openLicense(appEntry) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SmallTitle(text = stringResource(Res.string.licenses_open_source_section))
                    }

                    openSourceLicenseSections.forEach { section ->
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SmallTitle(text = stringResource(section.category.titleRes))
                            MiuixCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                section.entries.forEach { entry ->
                                    ArrowPreference(
                                        title = stringResource(entry.nameRes),
                                        summary = licenseSummaryText(entry.version, entry.kind),
                                        onClick = { openLicense(entry) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        MiuixText(
                            text = stringResource(Res.string.licenses_footer_note),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
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
                                contentDescription = stringResource(Res.string.back),
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
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    MaterialLicenseSection(
                        title = stringResource(Res.string.licenses_app_section),
                        entries = listOf(appEntry),
                        onEntryClick = { openLicense(it) }
                    )
                }

                item {
                    Text(
                        text = stringResource(Res.string.licenses_open_source_section),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }

                openSourceLicenseSections.forEach { section ->
                    item {
                        MaterialLicenseSection(
                            title = stringResource(section.category.titleRes),
                            entries = section.entries,
                            onEntryClick = { openLicense(it) }
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.licenses_footer_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    val entry = dialogEntry
    val dialogSummary = if (entry != null) licenseSummaryText(entry.version, entry.kind) else null
    val dialogText = entry?.let { licenseTexts[it.kind.assetPath] }

    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowDialog(
            show = showDialog,
            title = if (entry != null) stringResource(entry.nameRes) else null,
            summary = dialogSummary,
            onDismissRequest = { showDialog = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LicenseTextView(
                    text = dialogText,
                    loadFailed = loadFailed
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiuixTextButton(
                        text = stringResource(Res.string.licenses_open_github),
                        onClick = { entry?.let { openUrl(it.url) } },
                        modifier = Modifier.weight(1f)
                    )
                    MiuixTextButton(
                        text = stringResource(Res.string.licenses_close),
                        onClick = { showDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    } else if (showDialog && entry != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(entry.nameRes),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dialogSummary.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    when {
                        dialogText != null -> Text(
                            text = dialogText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        loadFailed -> Text(
                            text = stringResource(Res.string.licenses_load_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        else -> Text(
                            text = stringResource(Res.string.licenses_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(Res.string.licenses_close))
                }
            },
            dismissButton = {
                TextButton(onClick = { openUrl(entry.url) }) {
                    Text(text = stringResource(Res.string.licenses_open_github))
                }
            }
        )
    }
}

@Composable
private fun licenseSummaryText(version: String, kind: LicenseKind): String {
    return stringResource(
        Res.string.licenses_summary_format,
        version,
        stringResource(kind.labelRes)
    )
}

@Composable
private fun LicenseTextView(
    text: String?,
    loadFailed: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when {
            text != null -> MiuixText(
                text = text,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MiuixTheme.colorScheme.onSurface
            )

            loadFailed -> MiuixText(
                text = stringResource(Res.string.licenses_load_error),
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.error
            )

            else -> MiuixText(
                text = stringResource(Res.string.licenses_loading),
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    }
}

@Composable
private fun MaterialLicenseSection(
    title: String,
    entries: List<LicenseEntry>,
    onEntryClick: (LicenseEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                entries.forEach { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEntryClick(entry) }
                            .padding(vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(entry.nameRes),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = licenseSummaryText(entry.version, entry.kind),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
