package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.back
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BackNavigationIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    GlassIconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                },
            imageVector = MiuixIcons.Glass.ChevronBackward,
            contentDescription = stringResource(Res.string.back),
            tint = MiuixTheme.colorScheme.onSurface,
        )
    }
}

