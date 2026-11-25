package me.joxquin.notivas.ui.components.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Acción de cabecera para [top.yukonga.miuix.kmp.glass.GlassBottomSheet].
 *
 * GlassIconButton hereda el material, el backdrop y la animación floating del header
 * cuando la hoja usa progressiveBlur, igual que en el ejemplo oficial de Miuix.
 */
@Composable
fun GlassSheetHeaderAction(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = MiuixTheme.colorScheme.onSurface,
) {
    GlassIconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
