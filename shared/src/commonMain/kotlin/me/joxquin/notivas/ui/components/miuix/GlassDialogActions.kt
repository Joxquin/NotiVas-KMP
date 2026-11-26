package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Fila de acciones canónica para [GlassDialog], con el espaciado del ejemplo oficial de Miuix.
 */
@Composable
fun GlassDialogActions(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/**
 * Diálogo de confirmación glass con cancelar y confirmar, siguiendo el patrón
 * `Glass Component` → `Show Dialog` del ejemplo oficial de Miuix.
 *
 * No debe envolverse en `if (show)`: el propio componente gestiona la animación de salida.
 */
@Composable
fun GlassConfirmDialog(
    show: Boolean,
    title: String,
    cancelLabel: String,
    confirmLabel: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    summary: String? = null,
    confirmRole: GlassDialogButtonRole = GlassDialogButtonRole.Primary,
    cancelEnabled: Boolean = true,
    confirmEnabled: Boolean = true,
    onDismissFinished: (() -> Unit)? = null,
) {
    GlassDialog(
        show = show,
        backdrop = backdrop,
        modifier = modifier,
        title = title,
        summary = summary,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
    ) {
        GlassDialogActions {
            GlassDialogButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
                enabled = cancelEnabled,
            ) {
                Text(
                    text = cancelLabel,
                    style = MiuixTheme.textStyles.button,
                )
            }
            GlassDialogButton(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = confirmEnabled,
                role = confirmRole,
            ) {
                Text(
                    text = confirmLabel,
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}
