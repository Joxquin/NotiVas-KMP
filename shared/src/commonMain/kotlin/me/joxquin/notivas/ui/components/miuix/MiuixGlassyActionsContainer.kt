package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * Contenedor canónico de acciones para la barra superior (TopAppBar) de Xiaomi HyperOS / Compose Miuix UI.
 * Dispone los botones de acción (`GlassIconButton`) con separación estándar de 8.dp.
 */
@Composable
fun MiuixGlassyActionsContainer(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = CircleShape,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
