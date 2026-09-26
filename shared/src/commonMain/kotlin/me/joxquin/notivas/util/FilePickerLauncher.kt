package me.joxquin.notivas.util

import androidx.compose.runtime.Composable

expect class FilePickerLauncher {
    fun launchFilePicker()
    fun launchFileCreator(defaultFileName: String)
}

@Composable
expect fun rememberFilePickerLauncher(
    onFileSelected: (content: String) -> Unit,
    onRequestSaveContent: (onReadyToWrite: (String) -> Unit) -> Unit
): FilePickerLauncher
