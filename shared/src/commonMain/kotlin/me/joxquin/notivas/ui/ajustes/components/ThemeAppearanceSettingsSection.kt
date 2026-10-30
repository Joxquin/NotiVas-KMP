package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.theme.isDynamicColorAvailable
import me.joxquin.notivas.ui.theme.isMiuixThemeSupported
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference

import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_theme_section_title
import me.joxquin.notivas.shared.resources.settings_theme_title
import me.joxquin.notivas.shared.resources.settings_theme_summary
import me.joxquin.notivas.shared.resources.settings_theme_dialog_title
import me.joxquin.notivas.shared.resources.settings_theme_dialog_desc
import me.joxquin.notivas.shared.resources.theme_material
import me.joxquin.notivas.shared.resources.theme_material_desc
import me.joxquin.notivas.shared.resources.theme_miuix
import me.joxquin.notivas.shared.resources.theme_miuix_desc
import me.joxquin.notivas.shared.resources.theme_miuix_unsupported_hint
import me.joxquin.notivas.shared.resources.dynamic_color_title
import me.joxquin.notivas.shared.resources.dynamic_color_summary_on
import me.joxquin.notivas.shared.resources.dynamic_color_summary_off
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.change
import me.joxquin.notivas.shared.resources.selected

@Composable
fun ThemeAppearanceSettingsSection(
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    themeStyle: AppThemeStyle,
    onThemeStyleChange: (AppThemeStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val miuixSupported = remember { isMiuixThemeSupported() }
    val materialLabel = stringResource(Res.string.theme_material)
    val miuixLabel = stringResource(Res.string.theme_miuix)

    val themeOptions = remember(materialLabel, miuixLabel) {
        listOf(
            AppThemeStyle.MATERIAL to materialLabel,
            AppThemeStyle.MIUIX to miuixLabel
        )
    }

    val selectedThemeIndex = if (themeStyle == AppThemeStyle.MATERIAL || !miuixSupported) 0 else 1
    val currentThemeLabel = if (!miuixSupported) materialLabel else themeOptions[selectedThemeIndex].second

    var showMaterialThemeDialog by remember { mutableStateOf(false) }

    if (showMaterialThemeDialog && miuixSupported) {
        AlertDialog(
            onDismissRequest = { showMaterialThemeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.settings_theme_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_theme_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    themeOptions.forEach { (style, label) ->
                        val isSelected = style == themeStyle
                        val isMiuixOption = style == AppThemeStyle.MIUIX
                        val isOptionEnabled = !isMiuixOption || miuixSupported

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = isOptionEnabled) {
                                    if (isOptionEnabled) {
                                        onThemeStyleChange(style)
                                        showMaterialThemeDialog = false
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        text = if (style == AppThemeStyle.MATERIAL) {
                                            stringResource(Res.string.theme_material_desc)
                                        } else {
                                            stringResource(Res.string.theme_miuix_desc)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(Res.string.selected),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMaterialThemeDialog = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }

    if (themeStyle == AppThemeStyle.MIUIX && miuixSupported) {
        Column(modifier = modifier.fillMaxWidth()) {
            SmallTitle(text = stringResource(Res.string.settings_theme_section_title))
            MiuixCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                WindowDropdownPreference(
                    title = stringResource(Res.string.settings_theme_title),
                    summary = stringResource(Res.string.settings_theme_summary),
                    items = themeOptions.map { it.second },
                    selectedIndex = selectedThemeIndex,
                    onSelectedIndexChange = { index ->
                        val newStyle = themeOptions[index].first
                        onThemeStyleChange(newStyle)
                    }
                )
                if (isDynamicColorAvailable()) {
                    SwitchPreference(
                        title = stringResource(Res.string.dynamic_color_title),
                        summary = stringResource(Res.string.dynamic_color_summary_on),
                        checked = dynamicColorEnabled,
                        onCheckedChange = onDynamicColorChange
                    )
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.settings_theme_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Selector de Estilo de Diseño (Fila clickeable tipo preferencia Material si MIUIX es soportado)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = miuixSupported) {
                            if (miuixSupported) {
                                showMaterialThemeDialog = true
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_theme_title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (!miuixSupported) {
                                    "$currentThemeLabel • ${stringResource(Res.string.theme_miuix_unsupported_hint)}"
                                } else {
                                    currentThemeLabel
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (miuixSupported) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Opción Monet si estamos en Android 12+ y en Material
                if (isDynamicColorAvailable()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    MaterialNotificationToggleRow(
                        icon = Icons.Default.ColorLens,
                        title = stringResource(Res.string.dynamic_color_title),
                        subtitle = if (dynamicColorEnabled) {
                            stringResource(Res.string.dynamic_color_summary_on)
                        } else {
                            stringResource(Res.string.dynamic_color_summary_off)
                        },
                        checked = dynamicColorEnabled,
                        onCheckedChange = onDynamicColorChange
                    )
                }
            }
        }
    }
}

