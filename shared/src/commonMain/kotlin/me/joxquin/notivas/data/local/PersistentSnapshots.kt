package me.joxquin.notivas.data.local

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("", "Predeterminado del sistema"),
    SPANISH("es", "Español"),
    ENGLISH("en", "English")
}

@Serializable
data class PersistentPreferencesSnapshot(
    val universityUrl: String? = "https://canvas.utp.edu.pe",
    val accessToken: String? = null,
    val isOnboardingCompleted: Boolean = false,
    val syncIntervalMinutes: Long = 15L,
    val notif24h: Boolean = true,
    val notif3h: Boolean = true,
    val notif30m: Boolean = false,
    val notifMorning: Boolean = true,
    val autoFocusMode: Boolean = true,
    val pomodoroDurationMinutes: Int = 25,
    val biometricLock: Boolean = false,
    val openRouterApiKey: String? = "",
    val openRouterModel: String = "google/gemini-2.5-flash",
    val copilotEnabled: Boolean = true,
    val totalCopilotTokens: Long = 0L,
    val dynamicColorEnabled: Boolean = true,
    val themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val unconfiguredCoursesBannerDismissedUntil: Long = 0L,
    val authMode: me.joxquin.notivas.data.model.AuthMode = me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION,
    val sessionCookies: String? = null,
    val csrfToken: String? = null
)

