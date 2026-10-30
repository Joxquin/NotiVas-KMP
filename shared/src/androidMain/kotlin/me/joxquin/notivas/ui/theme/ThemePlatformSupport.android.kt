package me.joxquin.notivas.ui.theme

import android.os.Build

actual fun isMiuixThemeSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

