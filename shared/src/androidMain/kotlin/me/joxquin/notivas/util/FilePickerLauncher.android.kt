package me.joxquin.notivas.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current

    // SAF: Abrir documento
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    onFileSelected(text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // SAF: Crear documento JSON
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onRequestSaveContent { textToWrite ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.bufferedWriter().use { it.write(textToWrite) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    return remember(openDocumentLauncher, createDocumentLauncher) {
        FilePickerLauncher(
            onPickFile = {
                openDocumentLauncher.launch(arrayOf("application/json", "text/*"))
            },
            onCreateFile = { fileName ->
                createDocumentLauncher.launch(fileName)
            }
        )
    }
}
