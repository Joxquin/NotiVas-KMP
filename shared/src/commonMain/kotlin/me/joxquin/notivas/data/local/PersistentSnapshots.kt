package me.joxquin.notivas.data.local

import kotlinx.serialization.Serializable
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.CopilotMessage
import me.joxquin.notivas.data.model.CopilotSession
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.PlannerItem
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.data.model.UserProfile

@Serializable
data class PersistentStoreSnapshot(
    val userProfile: UserProfile? = null,
    val courses: List<Course> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val plannerItems: List<PlannerItem> = emptyList(),
    val simulationGroups: List<SimulationGroup> = emptyList(),
    val simulationItems: List<SimulationItem> = emptyList(),
    val copilotSessions: List<CopilotSession> = emptyList(),
    val copilotMessages: List<CopilotMessage> = emptyList()
)

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
    val unconfiguredCoursesBannerDismissedUntil: Long = 0L
)

