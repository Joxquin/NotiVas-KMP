package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.repository.CopilotMessageItem
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun CopilotMessageList(
    messages: List<CopilotMessageItem>,
    isLoading: Boolean,
    listState: LazyListState,
    coursesCount: Int,
    currentModel: String,
    onSuggestionClick: (String) -> Unit,
    thinkingStatus: String? = null,
    onRegenerate: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
    isMessageAnimated: ((String) -> Boolean)? = null,
    onMessageAnimated: ((String) -> Unit)? = null,
    onOpenAssignmentDraft: ((Long, String) -> Unit)? = null,
    onOpenForoDetail: ((Long) -> Unit)? = null,
    knownAssignments: List<Assignment> = emptyList(),
    courses: List<Course> = emptyList(),
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
        val scrollModifier = if (themeStyle == AppThemeStyle.MIUIX) {
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .scrollEndHaptic()
                .overScrollVertical()
        } else {
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        }

        val lastAssistantMessageId = remember(messages) {
            messages.lastOrNull { it.role != me.joxquin.notivas.data.repository.CopilotRole.USER }?.id
        }

        LazyColumn(
            state = listState,
            modifier = modifier.then(scrollModifier),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = topInset, bottom = bottomInset)
        ) {
            items(messages, key = { it.id }) { message ->
                val isAnimatedAlready = isMessageAnimated?.invoke(message.id) == true
                CopilotMessageBubble(
                    message = message,
                    themeStyle = themeStyle,
                    onRegenerate = onRegenerate,
                    onShare = onShare,
                    animateReveal = (message.id == lastAssistantMessageId) && !isAnimatedAlready,
                    onAnimationFinished = onMessageAnimated,
                    onOpenAssignmentDraft = onOpenAssignmentDraft,
                    onOpenForoDetail = onOpenForoDetail,
                    knownAssignments = knownAssignments,
                    courses = courses
                )
            }

            if (isLoading) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AnimatedContent(
                            targetState = thinkingStatus,
                            transitionSpec = {
                                (fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(300)) { it / 3 })
                                    .togetherWith(
                                        fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) +
                                                slideOutVertically(animationSpec = androidx.compose.animation.core.tween(200)) { -it / 3 }
                                    )
                            },
                            label = "thinkingStatusAnimation"
                        ) { status ->
                            if (status.isNullOrBlank()) {
                                CopilotThinkingDots(themeStyle = themeStyle)
                            } else {
                                CopilotActivityStatus(
                                    statusText = status,
                                    themeStyle = themeStyle
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
fun CopilotThinkingDots(
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val dotColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val transition = rememberInfiniteTransition(label = "dotsTransition")

    val dot1Scale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1Scale, dot2Scale, dot3Scale).forEach { scale ->
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.4f + (scale * 0.6f)
                    }
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
fun CopilotActivityStatus(
    statusText: String,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val sparkleColor = Color(0xFFA855F7)
    val textColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val transition = rememberInfiniteTransition(label = "sparklePulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Row(
        modifier = modifier.padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = sparkleColor,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    alpha = alphaAnim
                }
        )
        Text(
            text = statusText,
            style = if (themeStyle == AppThemeStyle.MIUIX) {
                MiuixTheme.textStyles.footnote1
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = textColor
        )
    }
}
