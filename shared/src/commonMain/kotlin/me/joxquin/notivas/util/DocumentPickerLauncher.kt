package me.joxquin.notivas.util

import androidx.compose.runtime.Composable

data class SelectedDocument(
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val bytes: ByteArray
)

expect class DocumentPickerLauncher {
    fun launchDocumentPicker(allowedExtensions: List<String>? = null)
}

@Composable
expect fun rememberDocumentPicker(
    onDocumentSelected: (SelectedDocument) -> Unit
): DocumentPickerLauncher
