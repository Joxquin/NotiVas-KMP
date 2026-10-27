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

    actual fun saveGoogleDriveTokens(accessToken: String, refreshToken: String?, userEmail: String?): Boolean {
        scope.launch { preferencesManager.saveGoogleDriveTokens(accessToken, refreshToken, userEmail) }
        return true
    }

    actual fun getGoogleDriveAccessToken(): String? {
        return preferencesManager.getGoogleDriveAccessToken()
    }

    actual fun getGoogleDriveRefreshToken(): String? {
        return preferencesManager.getGoogleDriveRefreshToken()
    }

    actual fun getGoogleDriveEmail(): String? {
        return preferencesManager.getGoogleDriveEmail()
    }

    actual fun clearGoogleDriveTokens(): Boolean {
        scope.launch { preferencesManager.unlinkGoogleDrive() }
        return true
    }

    actual suspend fun refreshGoogleDriveAccessToken(): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val app = try {
                Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null) as? android.app.Application
            } catch (_: Exception) {
                null
            }
            val context = app?.applicationContext ?: return@withContext null
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)?.account
                ?: return@withContext null

            val oldToken = preferencesManager.getGoogleDriveAccessToken()
            if (!oldToken.isNullOrBlank()) {
                try {
                    com.google.android.gms.auth.GoogleAuthUtil.clearToken(context, oldToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val scopeStr = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/userinfo.email"
            val newToken = com.google.android.gms.auth.GoogleAuthUtil.getToken(context, account, scopeStr)
            if (!newToken.isNullOrBlank()) {
                preferencesManager.saveGoogleDriveTokens(
                    accessToken = newToken,
                    refreshToken = preferencesManager.getGoogleDriveRefreshToken(),
                    userEmail = preferencesManager.getGoogleDriveEmail()
                )
                newToken
            } else {
                null
            }
        } catch (e: Exception) {
            println("SecureTokenVault: Failed to refresh Google token on Android: ${e.message}")
            null
        }
    }
}
