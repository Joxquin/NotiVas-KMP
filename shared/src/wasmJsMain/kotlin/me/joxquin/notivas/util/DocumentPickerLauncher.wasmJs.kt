@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@JsFun("""
(acceptStr, callback) => {
    try {
        const input = document.createElement('input');
        input.type = 'file';
        if (acceptStr) {
            input.accept = acceptStr;
        }
        input.onchange = () => {
            const files = input.files;
            if (files && files.length > 0) {
                const file = files[0];
                const reader = new FileReader();
                reader.onload = () => {
                    const result = reader.result;
                    const base64Str = result.split(',')[1] || '';
                    callback(file.name, base64Str, file.type || 'application/octet-stream', file.size);
                };
                reader.readAsDataURL(file);
            }
        };
        input.click();
    } catch (e) {
        console.error(e);
    }
}
""")
private external fun jsLaunchDocumentPicker(
    acceptStr: String,
    callback: (fileName: String, base64Data: String, mimeType: String, sizeBytes: Double) -> Unit
)

actual class DocumentPickerLauncher(
    private val onPick: (List<String>?) -> Unit
) {
    actual fun launchDocumentPicker(allowedExtensions: List<String>?) {
        onPick(allowedExtensions)
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberDocumentPicker(
    onDocumentSelected: (SelectedDocument) -> Unit
): DocumentPickerLauncher {
    val currentOnDocumentSelected = rememberUpdatedState(onDocumentSelected)

    return remember {
        DocumentPickerLauncher { allowedExtensions ->
            val acceptStr = allowedExtensions?.joinToString(",") { ext ->
                if (ext.startsWith(".")) ext else ".$ext"
            }.orEmpty()

            jsLaunchDocumentPicker(acceptStr) { fileName, base64Data, mimeType, sizeBytes ->
                try {
                    val bytes = Base64.decode(base64Data)
                    currentOnDocumentSelected.value(
                        SelectedDocument(
                            name = fileName,
                            sizeBytes = if (sizeBytes > 0) sizeBytes.toLong() else bytes.size.toLong(),
                            mimeType = mimeType,
                            bytes = bytes
                        )
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}
