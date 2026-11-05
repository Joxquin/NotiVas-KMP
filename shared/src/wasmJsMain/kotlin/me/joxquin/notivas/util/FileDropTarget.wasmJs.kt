@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@JsFun("""
(onDragOverChange, onFileDropped) => {
    let isCurrentlyInside = false;

    const onDragOver = (e) => {
        if (!window.__notivasDropEnabled) return;
        const types = e.dataTransfer ? e.dataTransfer.types : null;
        let hasFiles = false;
        if (types) {
            for (let i = 0; i < types.length; i++) {
                if (types[i] === 'Files') {
                    hasFiles = true;
                    break;
                }
            }
        }
        if (!hasFiles) return;

        const bounds = window.__notivasDropBounds;
        if (!bounds) return;

        const x = e.clientX;
        const y = e.clientY;
        const inside = x >= bounds.left && x <= bounds.right && y >= bounds.top && y <= bounds.bottom;

        if (inside) {
            e.preventDefault();
            if (!isCurrentlyInside) {
                isCurrentlyInside = true;
                onDragOverChange(true);
            }
        } else if (isCurrentlyInside) {
            isCurrentlyInside = false;
            onDragOverChange(false);
        }
    };

    const onDragLeave = () => {
        if (isCurrentlyInside) {
            isCurrentlyInside = false;
            onDragOverChange(false);
        }
    };

    const onDrop = (e) => {
        if (isCurrentlyInside) {
            isCurrentlyInside = false;
            onDragOverChange(false);
        }
        if (!window.__notivasDropEnabled) return;

        const bounds = window.__notivasDropBounds;
        if (!bounds) return;

        const x = e.clientX;
        const y = e.clientY;
        const inside = x >= bounds.left && x <= bounds.right && y >= bounds.top && y <= bounds.bottom;
        if (!inside) return;

        e.preventDefault();
        e.stopPropagation();

        const files = e.dataTransfer ? e.dataTransfer.files : null;
        if (files && files.length > 0) {
            const file = files[0];
            const reader = new FileReader();
            reader.onload = () => {
                const result = reader.result;
                const base64Str = result.split(',')[1] || '';
                onFileDropped(file.name, base64Str, file.type || 'application/octet-stream', file.size);
            };
            reader.readAsDataURL(file);
        }
    };

    window.addEventListener('dragover', onDragOver);
    window.addEventListener('dragleave', onDragLeave);
    window.addEventListener('drop', onDrop);

    return () => {
        if (isCurrentlyInside) {
            isCurrentlyInside = false;
            onDragOverChange(false);
        }
        window.removeEventListener('dragover', onDragOver);
        window.removeEventListener('dragleave', onDragLeave);
        window.removeEventListener('drop', onDrop);
    };
}
""")
private external fun jsSetupDropListeners(
    onDragOverChange: (isInside: Boolean) -> Unit,
    onFileDropped: (fileName: String, base64Data: String, mimeType: String, sizeBytes: Double) -> Unit
): () -> Unit

@JsFun("""
(enabled, left, top, right, bottom) => {
    window.__notivasDropEnabled = enabled;
    window.__notivasDropBounds = { left, top, right, bottom };
}
""")
private external fun jsUpdateDropBounds(
    enabled: Boolean,
    left: Double,
    top: Double,
    right: Double,
    bottom: Double
)

@OptIn(ExperimentalEncodingApi::class)
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

    DisposableEffect(Unit) {
        val cleanup = jsSetupDropListeners(
            onDragOverChange = { isInside ->
                currentOnDragStateChange.value(isInside)
            },
            onFileDropped = { fileName, base64Data, mimeType, sizeBytes ->
                try {
                    val ext = fileName.substringAfterLast('.', "").lowercase()
                    val allowed = currentAllowedExtensions.value
                    if (allowed.isNullOrEmpty() || ext in allowed) {
                        val bytes = Base64.decode(base64Data)
                        currentOnDocumentDropped.value(
                            SelectedDocument(
                                name = fileName,
                                sizeBytes = if (sizeBytes > 0) sizeBytes.toLong() else bytes.size.toLong(),
                                mimeType = mimeType,
                                bytes = bytes
                            )
                        )
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        )

        onDispose {
            cleanup()
            jsUpdateDropBounds(false, 0.0, 0.0, 0.0, 0.0)
        }
    }

    return this.onGloballyPositioned { coordinates: LayoutCoordinates ->
        val bounds = coordinates.boundsInWindow()
        jsUpdateDropBounds(
            enabled = currentEnabled.value,
            left = bounds.left.toDouble(),
            top = bounds.top.toDouble(),
            right = bounds.right.toDouble(),
            bottom = bounds.bottom.toDouble()
        )
    }
}
