package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.swing.JFileChooser
import java.io.File

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
        DocumentPickerLauncher { allowedExtensions ->
            try {
                val chooser = JFileChooser()
                chooser.dialogTitle = "Seleccionar archivo para entrega"
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    if (file.exists() && file.isFile) {
                        val bytes = file.readBytes()
                        onDocumentSelected(
                            SelectedDocument(
                                name = file.name,
                                sizeBytes = file.length(),
                                mimeType = "application/octet-stream",
                                bytes = bytes
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
