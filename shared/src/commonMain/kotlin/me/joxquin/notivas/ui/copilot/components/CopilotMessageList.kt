package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.repository.CopilotMessageItem
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CopilotMessageList(
    messages: List<CopilotMessageItem>,
    isLoading: Boolean,
    listState: LazyListState,
    coursesCount: Int,
    currentModel: String,
    onSuggestionClick: (String) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    bottomPadding: androidx.compose.ui.unit.Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val statusBarTop =
        androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInset = if (themeStyle == AppThemeStyle.MIUIX) statusBarTop + 68.dp else 10.dp
    // bottomPadding es la posición del TextInput. Le sumamos la altura del TextInput (48dp) + 12dp de margen entre TextInput y mensajes.
    val bottomInset = if (themeStyle == AppThemeStyle.MIUIX) bottomPadding + 60.dp else bottomPadding

    if (messages.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = topInset,
                    bottom = bottomInset
                ),
            verticalArrangement = Arrangement.Top
        ) {
            CopilotEmptyHeroSection(
                coursesCount = coursesCount,
                currentModel = currentModel,
                themeStyle = themeStyle
            )

            CopilotQuickSuggestionsSection(
                onSuggestionClick = onSuggestionClick,
                themeStyle = themeStyle
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = topInset, bottom = bottomInset)
        ) {
            items(messages, key = { it.id }) { message ->
                CopilotMessageBubble(
                    message = message,
                    themeStyle = themeStyle
                )
            }

            if (isLoading) {
                item(key = "loading_indicator") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (themeStyle == AppThemeStyle.MIUIX) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1B1C20))
                                    .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF3482FF)
                                    )
                                    Text(
                                        text = "Copilot está pensando...",
                                        style = MiuixTheme.textStyles.footnote2,
                                        color = Color(0xFF9E9EA7)
                                    )
                                }
                            }
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Copilot está pensando...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
