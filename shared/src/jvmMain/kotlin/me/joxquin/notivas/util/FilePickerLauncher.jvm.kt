package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

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
            onPickFile = {
                try {
                    val chooser = JFileChooser()
                    chooser.dialogTitle = "Seleccionar archivo JSON"
                    chooser.fileFilter = FileNameExtensionFilter("Archivos JSON (*.json)", "json")
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        val file = chooser.selectedFile
                        if (file.exists() && file.isFile) {
                            onFileSelected(file.readText())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onCreateFile = { defaultFileName ->
                try {
                    val chooser = JFileChooser()
                    chooser.dialogTitle = "Guardar archivo JSON"
                    chooser.selectedFile = File(defaultFileName)
                    chooser.fileFilter = FileNameExtensionFilter("Archivos JSON (*.json)", "json")
                    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        val file = chooser.selectedFile
                        onRequestSaveContent { textToWrite ->
                            try {
                                file.writeText(textToWrite)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}
