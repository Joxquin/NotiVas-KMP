package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

data class QuickChipItem(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun CopilotQuickChipsRow(
    onChipClick: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val chips = listOf(
        QuickChipItem(
            label = stringResource(Res.string.copilot_chip_web_label),
            prompt = stringResource(Res.string.copilot_chip_web_prompt),
            icon = Icons.Default.Search,
            iconTint = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        QuickChipItem(
            label = stringResource(Res.string.copilot_chip_papers_label),
            prompt = stringResource(Res.string.copilot_chip_papers_prompt),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            iconTint = if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        ),
        QuickChipItem(
            label = stringResource(Res.string.copilot_chip_report_label),
            prompt = stringResource(Res.string.copilot_chip_report_prompt),
            icon = Icons.Default.AutoAwesome,
            iconTint = if (isMiuix) MiuixTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.secondary
        ),
        QuickChipItem(
            label = stringResource(Res.string.copilot_chip_explain_label),
            prompt = stringResource(Res.string.copilot_chip_explain_prompt),
            icon = Icons.Default.Psychology,
            iconTint = if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
        )
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { chip ->
            if (isMiuix) {
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 0.8.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 9999.dp
                        )
                        .clip(CircleShape)
                        .clickable { onChipClick(chip.prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = null,
                            tint = chip.iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = chip.label,
                            style = MiuixTheme.textStyles.footnote2.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onChipClick(chip.prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = null,
                            tint = chip.iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
