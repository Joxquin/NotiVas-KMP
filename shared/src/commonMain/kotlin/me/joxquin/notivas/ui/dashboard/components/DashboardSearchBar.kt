package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_ask_ai
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onAskAi: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val placeholder = stringResource(Res.string.dashboard_search_hint)
    val askAiText = stringResource(Res.string.dashboard_ask_ai)

    val containerColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val textColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val placeholderColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val aiButtonBgColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val aiButtonTextColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = placeholderColor,
                modifier = Modifier.size(18.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = placeholderColor,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = placeholderColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") }
                )
            }

            // Botón/Chip inteligente "Preguntar a IA" (Copilot)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(aiButtonBgColor)
                    .clickable { onAskAi(query) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = askAiText,
                        tint = aiButtonTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = askAiText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = aiButtonTextColor
                    )
                }
            }
        }
    }
}
