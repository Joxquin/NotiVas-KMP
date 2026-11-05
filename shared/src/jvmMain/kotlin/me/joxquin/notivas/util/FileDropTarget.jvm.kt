package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.fileDropTarget(
    enabled: Boolean,
    allowedExtensions: List<String>?,
    onDragStateChange: (Boolean) -> Unit,
    onDocumentDropped: (SelectedDocument) -> Unit
): Modifier = this
