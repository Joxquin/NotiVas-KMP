package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class FilePickerLauncher(
    private val onPickFile: () -> Unit,
    private val onCreateFile: (String) -> Unit
) {
    actual fun launchFilePicker() {
        onPickFile()
    }

    actual fun launchFileCreator(defaultFileName: String) {
        onCreateFile(defaultFileName)
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    onFileSelected: (content: String) -> Unit,
    onRequestSaveContent: (onReadyToWrite: (String) -> Unit) -> Unit
): FilePickerLauncher {
    return remember {
        FilePickerLauncher(
            onPickFile = {},
            onCreateFile = {}
        )
    }
}
