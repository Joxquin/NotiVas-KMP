package me.joxquin.notivas

class WasmJsPlatform : Platform {
    override val name: String = "Web / Wasm"
}

actual fun getPlatform(): Platform = WasmJsPlatform()
