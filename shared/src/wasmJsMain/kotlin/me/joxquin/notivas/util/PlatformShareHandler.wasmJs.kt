package me.joxquin.notivas.util

actual object PlatformShareHandler {
    actual fun shareText(text: String, title: String?) {
        // En Wasm/Web, no-op o share nativo si soportado
    }
}
