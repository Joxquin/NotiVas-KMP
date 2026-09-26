package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme

import me.joxquin.notivas.util.AppVersion

private const val TAP_TARGET = 8
private const val TAP_RESET_WINDOW_MS = 3_000L

@Composable
fun AjustesAppInfoFooter(
    versionName: String = remember { AppVersion.get().versionName },
    versionCode: Long = remember { AppVersion.get().versionCode },
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onNavigateToDebug: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var firstTapTime by remember { mutableLongStateOf(0L) }
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline
    }

    val primaryTextColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val secondaryTextColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                val now = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
                if (tapCount == 0 || (now - firstTapTime) > TAP_RESET_WINDOW_MS) {
                    firstTapTime = now
                    tapCount = 1
                } else {
                    tapCount++
                }
                if (tapCount >= TAP_TARGET) {
                    tapCount = 0
                    firstTapTime = 0L
                    onNavigateToDebug()
                }
            }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "NotiVas v$versionName ($versionCode)",
                style = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "Compose Multiplatform (Android/JVM/iOS)",
            style = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.textStyles.footnote2 else MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp
            ),
            color = secondaryTextColor,
            textAlign = TextAlign.Center
        )
    }
}
