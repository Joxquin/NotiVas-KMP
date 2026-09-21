package me.joxquin.notivas.security

import me.joxquin.notivas.data.local.PreferencesManager

expect class SecureTokenVault(preferencesManager: PreferencesManager) {
    fun saveCanvasToken(token: String): Boolean
    fun getCanvasToken(): String?
    fun clearCanvasToken(): Boolean
}
