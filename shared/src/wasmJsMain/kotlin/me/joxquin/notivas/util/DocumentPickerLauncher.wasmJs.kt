package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class DocumentPickerLauncher(
    private val onPick: (List<String>?) -> Unit
) {
    actual fun launchDocumentPicker(allowedExtensions: List<String>?) {
        onPick(allowedExtensions)
    }
}

@Composable
actual fun rememberDocumentPicker(
    onDocumentSelected: (SelectedDocument) -> Unit
): DocumentPickerLauncher {
    return remember {
        DocumentPickerLauncher {
            // Web picker stub
        }
    }
}
