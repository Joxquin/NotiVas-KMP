package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.fileDropTarget(
    enabled: Boolean = true,
    allowedExtensions: List<String>? = null,
    onDragStateChange: (Boolean) -> Unit = {},
    onDocumentDropped: (SelectedDocument) -> Unit
): Modifier
