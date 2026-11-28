package me.joxquin.notivas

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new URLSearchParams(window.location.search).get('demo') === '1'")
private external fun shouldOpenSemesterDemo(): Boolean

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val startInDemo = shouldOpenSemesterDemo()
    ComposeViewport {
        App(startInDemo = startInDemo)
    }
}

