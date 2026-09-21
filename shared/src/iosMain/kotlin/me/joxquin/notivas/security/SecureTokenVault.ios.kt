package me.joxquin.notivas.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.PreferencesManager

actual class SecureTokenVault actual constructor(private val preferencesManager: PreferencesManager) {
    private val scope = CoroutineScope(Dispatchers.Main)

    actual fun saveCanvasToken(token: String): Boolean {
        scope.launch { preferencesManager.saveAccessToken(token) }
        return true
    }

    actual fun getCanvasToken(): String? {
        return preferencesManager.getAccessToken()
    }

    actual fun clearCanvasToken(): Boolean {
        scope.launch { preferencesManager.clear() }
        return true
    }
}
