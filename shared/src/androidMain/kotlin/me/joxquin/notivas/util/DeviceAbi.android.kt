package me.joxquin.notivas.util

import android.os.Build

actual object DeviceAbi {
    actual fun getPreferredAbi(): String? {
        return Build.SUPPORTED_ABIS.firstOrNull()
    }
}
