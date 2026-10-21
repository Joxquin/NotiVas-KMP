package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import me.joxquin.notivas.data.local.AppLanguage
import me.joxquin.notivas.data.local.AppThemeStyle
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_language_section_title
import me.joxquin.notivas.shared.resources.settings_language_title
import me.joxquin.notivas.shared.resources.settings_language_dialog_title
import me.joxquin.notivas.shared.resources.settings_language_dialog_desc
import me.joxquin.notivas.shared.resources.language_system
import me.joxquin.notivas.shared.resources.language_system_summary
import me.joxquin.notivas.shared.resources.language_spanish
import me.joxquin.notivas.shared.resources.language_spanish_summary
import me.joxquin.notivas.shared.resources.language_english
import me.joxquin.notivas.shared.resources.language_english_summary
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.change
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference

@Composable
fun LanguageSettingsSection(
    appLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    val languageOptions = remember {
        listOf(
            AppLanguage.SYSTEM,
            AppLanguage.SPANISH,
            AppLanguage.ENGLISH
        )
    }

    val selectedLanguageIndex = languageOptions.indexOf(appLanguage).coerceAtLeast(0)

    val languageLabels = mapOf(
        AppLanguage.SYSTEM to stringResource(Res.string.language_system),
        AppLanguage.SPANISH to stringResource(Res.string.language_spanish),
        AppLanguage.ENGLISH to stringResource(Res.string.language_english)
    )

    val languageSummaries = mapOf(
        AppLanguage.SYSTEM to stringResource(Res.string.language_system_summary),
        AppLanguage.SPANISH to stringResource(Res.string.language_spanish_summary),
        AppLanguage.ENGLISH to stringResource(Res.string.language_english_summary)
    )

    val currentLanguageLabel = languageLabels[appLanguage] ?: stringResource(Res.string.language_system)
    var showMaterialLanguageDialog by remember { mutableStateOf(false) }

    if (showMaterialLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showMaterialLanguageDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.settings_language_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_language_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    languageOptions.forEach { lang ->
                        val isSelected = lang == appLanguage
                        val label = languageLabels[lang] ?: lang.displayName
                        val summary = languageSummaries[lang] ?: ""

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showMaterialLanguageDialog = false
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
                                        text = summary,
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
                                        contentDescription = "Seleccionado",
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
                TextButton(onClick = { showMaterialLanguageDialog = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(modifier = modifier.fillMaxWidth()) {
            SmallTitle(text = stringResource(Res.string.settings_language_section_title))
            MiuixCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                WindowDropdownPreference(
                    title = stringResource(Res.string.settings_language_title),
                    summary = currentLanguageLabel,
                    items = languageOptions.map { languageLabels[it] ?: it.displayName },
                    selectedIndex = selectedLanguageIndex,
                    onSelectedIndexChange = { index ->
                        val newLang = languageOptions[index]
                        onLanguageChange(newLang)
                    }
                )
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
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.settings_language_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Selector de Idioma (Fila clickeable tipo preferencia Material)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showMaterialLanguageDialog = true }
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
                                    imageVector = Icons.Default.Translate,
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
                                text = stringResource(Res.string.settings_language_title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentLanguageLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

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
        }
    }
}
