package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.browser.window
import org.w3c.dom.DragEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.files.File
import org.w3c.files.FileReader
import org.khronos.webgl.Int8Array
import org.khronos.webgl.ArrayBuffer

internal fun convertInt8ArrayToByteArray(int8Array: Int8Array): ByteArray {
    val len = int8Array.length
    val bytes = ByteArray(len)
    for (i in 0 until len) {
        bytes[i] = int8Array[i]
    }
    return bytes
}

internal fun readFileAsSelectedDocument(file: File, onLoaded: (SelectedDocument) -> Unit) {
    val name = file.name
    val size = file.size.toLong()
    val mimeType = file.type.ifBlank { "application/octet-stream" }
    val reader = FileReader()
    reader.onload = {
        try {
            val result = reader.result
            if (result != null) {
                val arrayBuffer = result.unsafeCast<ArrayBuffer>()
                val int8Array = Int8Array(arrayBuffer, 0, arrayBuffer.byteLength)
                val bytes = convertInt8ArrayToByteArray(int8Array)
                onLoaded(
                    SelectedDocument(
                        name = name,
                        sizeBytes = if (size > 0L) size else bytes.size.toLong(),
                        mimeType = mimeType,
                        bytes = bytes
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    reader.readAsArrayBuffer(file)
}

@Composable
actual fun Modifier.fileDropTarget(
    enabled: Boolean,
    allowedExtensions: List<String>?,
    onDragStateChange: (Boolean) -> Unit,
    onDocumentDropped: (SelectedDocument) -> Unit
): Modifier {
    val currentEnabled = rememberUpdatedState(enabled)
    val currentAllowedExtensions = rememberUpdatedState(allowedExtensions)
    val currentOnDragStateChange = rememberUpdatedState(onDragStateChange)
    val currentOnDocumentDropped = rememberUpdatedState(onDocumentDropped)

    val boundsRef = remember {
        object {
            var left: Float = 0f
            var top: Float = 0f
            var right: Float = 0f
            var bottom: Float = 0f
            var isInside: Boolean = false
        }
    }

    DisposableEffect(Unit) {
        val onDragOverListener = object : EventListener {
            override fun handleEvent(event: Event) {
                if (!currentEnabled.value) return
                val dragEvent = event as? DragEvent ?: return
                val types = dragEvent.dataTransfer?.types
                val hasFiles = types != null && run {
                    var found = false
                    val len = types.length
                    for (i in 0 until len) {
                        if (types[i] == "Files") {
                            found = true
                            break
                        }
                    }
                    found
                }
                if (!hasFiles) return

                val clientX = dragEvent.clientX.toFloat()
                val clientY = dragEvent.clientY.toFloat()
                val inside = clientX in boundsRef.left..boundsRef.right &&
                        clientY in boundsRef.top..boundsRef.bottom

                if (inside) {
                    dragEvent.preventDefault()
                    if (!boundsRef.isInside) {
                        boundsRef.isInside = true
                        currentOnDragStateChange.value(true)
                    }
                } else if (boundsRef.isInside) {
                    boundsRef.isInside = false
                    currentOnDragStateChange.value(false)
                }
            }
        }

        val onDragLeaveListener = object : EventListener {
            override fun handleEvent(event: Event) {
                if (boundsRef.isInside) {
                    boundsRef.isInside = false
                    currentOnDragStateChange.value(false)
                }
            }
        }

        val onDropListener = object : EventListener {
            override fun handleEvent(event: Event) {
                val dragEvent = event as? DragEvent ?: return
                val clientX = dragEvent.clientX.toFloat()
                val clientY = dragEvent.clientY.toFloat()
                val inside = clientX in boundsRef.left..boundsRef.right &&
                        clientY in boundsRef.top..boundsRef.bottom

                if (boundsRef.isInside) {
                    boundsRef.isInside = false
                    currentOnDragStateChange.value(false)
                }

                if (!currentEnabled.value || !inside) return

                dragEvent.preventDefault()
                dragEvent.stopPropagation()

                val files = dragEvent.dataTransfer?.files ?: return
                if (files.length > 0) {
                    val file = files[0] ?: return
                    val ext = file.name.substringAfterLast('.', "").lowercase()
                    val allowed = currentAllowedExtensions.value
                    if (allowed.isNullOrEmpty() || ext in allowed) {
                        readFileAsSelectedDocument(file) { doc ->
                            currentOnDocumentDropped.value(doc)
                        }
                    }
                }
            }
        }

        window.addEventListener("dragover", onDragOverListener)
        window.addEventListener("dragleave", onDragLeaveListener)
        window.addEventListener("drop", onDropListener)

        onDispose {
            if (boundsRef.isInside) {
                boundsRef.isInside = false
                currentOnDragStateChange.value(false)
            }
            window.removeEventListener("dragover", onDragOverListener)
            window.removeEventListener("dragleave", onDragLeaveListener)
            window.removeEventListener("drop", onDropListener)
        }
    }

    return this.onGloballyPositioned { coordinates: LayoutCoordinates ->
        val bounds = coordinates.boundsInWindow()
        boundsRef.left = bounds.left
        boundsRef.top = bounds.top
        boundsRef.right = bounds.right
        boundsRef.bottom = bounds.bottom
    }
}
