package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

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
    val currentOnDocumentSelected = rememberUpdatedState(onDocumentSelected)

    return remember {
        DocumentPickerLauncher { allowedExtensions ->
            try {
                val input = document.createElement("input") as? HTMLInputElement ?: return@DocumentPickerLauncher
                input.type = "file"
                if (!allowedExtensions.isNullOrEmpty()) {
                    input.accept = allowedExtensions.joinToString(",") { ext ->
                        if (ext.startsWith(".")) ext else ".$ext"
                    }
                }
                input.onchange = {
                    val files = input.files
                    if (files != null && files.length > 0) {
                        val file = files[0]
                        if (file != null) {
                            readFileAsSelectedDocument(file) { doc ->
                                currentOnDocumentSelected.value(doc)
                            }
                        }
                    }
                }
                input.click()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
