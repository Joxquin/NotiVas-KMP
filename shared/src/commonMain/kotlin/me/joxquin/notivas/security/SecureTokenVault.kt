package me.joxquin.notivas.security

import me.joxquin.notivas.data.local.PreferencesManager

expect class SecureTokenVault(preferencesManager: PreferencesManager) {
    fun saveCanvasToken(token: String): Boolean
    fun getCanvasToken(): String?
    fun clearCanvasToken(): Boolean

    fun saveGoogleDriveTokens(accessToken: String, refreshToken: String?, userEmail: String?): Boolean
    fun getGoogleDriveAccessToken(): String?
    fun getGoogleDriveRefreshToken(): String?
    fun getGoogleDriveEmail(): String?
    fun clearGoogleDriveTokens(): Boolean
}
