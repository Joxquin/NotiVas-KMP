package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class FilePickerLauncher {
    actual fun launchFilePicker() {}
    actual fun launchFileCreator(defaultFileName: String) {}
}

@Composable
actual fun rememberFilePickerLauncher(
    onFileSelected: (content: String) -> Unit,
    onRequestSaveContent: (onReadyToWrite: (String) -> Unit) -> Unit
): FilePickerLauncher {
    return remember { FilePickerLauncher() }
}
