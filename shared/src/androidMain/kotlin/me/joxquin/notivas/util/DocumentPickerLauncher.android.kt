package me.joxquin.notivas.util

import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                var name = "archivo"
                var size = 0L
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }

                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    if (size <= 0L) size = bytes.size.toLong()
                    onDocumentSelected(
                        SelectedDocument(
                            name = name,
                            sizeBytes = size,
                            mimeType = mimeType,
                            bytes = bytes
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    return remember(launcher) {
        DocumentPickerLauncher { allowedExtensions ->
            val mimeTypes = if (!allowedExtensions.isNullOrEmpty()) {
                allowedExtensions.mapNotNull { ext ->
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.removePrefix(".").lowercase())
                }.toTypedArray().takeIf { it.isNotEmpty() } ?: arrayOf("*/*")
            } else {
                arrayOf("*/*")
            }
            launcher.launch(mimeTypes)
        }
    }
}
