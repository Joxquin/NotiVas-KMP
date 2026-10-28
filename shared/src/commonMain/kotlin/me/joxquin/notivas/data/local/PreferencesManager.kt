package me.joxquin.notivas.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Gestor de preferencias reactivo Multiplatform con persistencia automática en disco.
 */
class PreferencesManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val prefsFile: String by lazy {
        "${FileStorageProvider.getAppDataDirectory()}/notivas_prefs.json"
    }

    private var saveJob: Job? = null

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: Flow<Boolean> = _isLoaded.asStateFlow()

    private val _universityUrl = MutableStateFlow<String?>("https://canvas.utp.edu.pe")
    val universityUrl: Flow<String?> = _universityUrl.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: Flow<String?> = _accessToken.asStateFlow()
    fun getAccessToken(): String? = _accessToken.value

    private val _authMode = MutableStateFlow(me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION)
    val authMode: Flow<me.joxquin.notivas.data.model.AuthMode> = _authMode.asStateFlow()
    fun getAuthMode(): me.joxquin.notivas.data.model.AuthMode = _authMode.value

    private val _sessionCookies = MutableStateFlow<String?>(null)
    val sessionCookies: Flow<String?> = _sessionCookies.asStateFlow()
    fun getSessionCookies(): String? = _sessionCookies.value

    private val _csrfToken = MutableStateFlow<String?>(null)
    val csrfToken: Flow<String?> = _csrfToken.asStateFlow()
    fun getCsrfToken(): String? = _csrfToken.value

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()
    fun getIsSessionExpired(): Boolean = _isSessionExpired.value

    fun setSessionExpired(expired: Boolean) {
        _isSessionExpired.value = expired
    }

    fun getAuthCredential(): me.joxquin.notivas.data.model.CanvasAuthCredential? {
        return when (_authMode.value) {
            me.joxquin.notivas.data.model.AuthMode.TOKEN -> {
                _accessToken.value?.takeIf { it.isNotBlank() }?.let { me.joxquin.notivas.data.model.CanvasAuthCredential.BearerToken(it) }
            }
            me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION -> {
                _sessionCookies.value?.takeIf { it.isNotBlank() }?.let {
                    me.joxquin.notivas.data.model.CanvasAuthCredential.SessionCookies(it, _csrfToken.value)
                } ?: _accessToken.value?.takeIf { it.isNotBlank() }?.let { me.joxquin.notivas.data.model.CanvasAuthCredential.BearerToken(it) }
            }
        }
    }

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()
    fun getIsOnboardingCompleted(): Boolean = _isOnboardingCompleted.value

    private val _syncIntervalMinutes = MutableStateFlow(15L)
    val syncIntervalMinutes: Flow<Long> = _syncIntervalMinutes.asStateFlow()

    private val _notif24h = MutableStateFlow(true)
    val notif24h: Flow<Boolean> = _notif24h.asStateFlow()

    private val _notif3h = MutableStateFlow(true)
    val notif3h: Flow<Boolean> = _notif3h.asStateFlow()

    private val _notif30m = MutableStateFlow(false)
    val notif30m: Flow<Boolean> = _notif30m.asStateFlow()

    private val _notifMorning = MutableStateFlow(true)
    val notifMorning: Flow<Boolean> = _notifMorning.asStateFlow()

    private val _autoFocusMode = MutableStateFlow(true)
    val autoFocusMode: Flow<Boolean> = _autoFocusMode.asStateFlow()

    private val _pomodoroDurationMinutes = MutableStateFlow(25)
    val pomodoroDurationMinutes: Flow<Int> = _pomodoroDurationMinutes.asStateFlow()

    private val _copilotProvider = MutableStateFlow(CopilotProvider.OPENROUTER)
    val copilotProvider: Flow<CopilotProvider> = _copilotProvider.asStateFlow()
    fun getCopilotProvider(): CopilotProvider = _copilotProvider.value

    private val _openRouterApiKey = MutableStateFlow<String?>("")
    val openRouterApiKey: Flow<String?> = _openRouterApiKey.asStateFlow()

    private val _openRouterModel = MutableStateFlow("google/gemini-2.5-flash")
    val openRouterModel: Flow<String> = _openRouterModel.asStateFlow()
    fun getOpenRouterModel(): String = _openRouterModel.value

    private val _googleAiStudioApiKey = MutableStateFlow<String?>("")
    val googleAiStudioApiKey: Flow<String?> = _googleAiStudioApiKey.asStateFlow()

    private val _googleAiStudioModel = MutableStateFlow("gemini-3.5-flash-lite")
    val googleAiStudioModel: Flow<String> = _googleAiStudioModel.asStateFlow()
    fun getGoogleAiStudioModel(): String = _googleAiStudioModel.value

    private val _copilotEnabled = MutableStateFlow(true)
    val copilotEnabled: Flow<Boolean> = _copilotEnabled.asStateFlow()

    private val _totalCopilotTokens = MutableStateFlow(0L)
    val totalCopilotTokens: Flow<Long> = _totalCopilotTokens.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(true)
    val dynamicColorEnabled: Flow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _themeStyle = MutableStateFlow(AppThemeStyle.MATERIAL)
    val themeStyle: Flow<AppThemeStyle> = _themeStyle.asStateFlow()
    fun getThemeStyle(): AppThemeStyle = _themeStyle.value

    private val _appLanguage = MutableStateFlow(AppLanguage.SYSTEM)
    val appLanguage: Flow<AppLanguage> = _appLanguage.asStateFlow()
    fun getAppLanguage(): AppLanguage = _appLanguage.value

    private val _unconfiguredCoursesBannerDismissedUntil = MutableStateFlow(0L)
    val unconfiguredCoursesBannerDismissedUntil: Flow<Long> = _unconfiguredCoursesBannerDismissedUntil.asStateFlow()
    fun getUnconfiguredCoursesBannerDismissedUntil(): Long = _unconfiguredCoursesBannerDismissedUntil.value

    private val _googleDriveConnected = MutableStateFlow(false)
    val googleDriveConnected: Flow<Boolean> = _googleDriveConnected.asStateFlow()
    fun isGoogleDriveConnected(): Boolean = _googleDriveConnected.value

    private val _googleDriveEmail = MutableStateFlow<String?>(null)
    val googleDriveEmail: Flow<String?> = _googleDriveEmail.asStateFlow()
    fun getGoogleDriveEmail(): String? = _googleDriveEmail.value

    private val _googleDriveLastSyncTimestamp = MutableStateFlow(0L)
    val googleDriveLastSyncTimestamp: Flow<Long> = _googleDriveLastSyncTimestamp.asStateFlow()
    fun getGoogleDriveLastSyncTimestamp(): Long = _googleDriveLastSyncTimestamp.value

    private val _googleDriveAccessToken = MutableStateFlow<String?>(null)
    val googleDriveAccessToken: Flow<String?> = _googleDriveAccessToken.asStateFlow()
    fun getGoogleDriveAccessToken(): String? = _googleDriveAccessToken.value

    private val _googleDriveRefreshToken = MutableStateFlow<String?>(null)
    val googleDriveRefreshToken: Flow<String?> = _googleDriveRefreshToken.asStateFlow()
    fun getGoogleDriveRefreshToken(): String? = _googleDriveRefreshToken.value

    private val _discardedAssignmentIds = MutableStateFlow<Set<Long>>(emptySet())
    val discardedAssignmentIds: Flow<Set<Long>> = _discardedAssignmentIds.asStateFlow()
    fun getDiscardedAssignmentIds(): Set<Long> = _discardedAssignmentIds.value

    suspend fun discardAssignment(assignmentId: Long) {
        _discardedAssignmentIds.value = _discardedAssignmentIds.value + assignmentId
        schedulePersist()
    }

    suspend fun restoreAssignment(assignmentId: Long) {
        _discardedAssignmentIds.value = _discardedAssignmentIds.value - assignmentId
        schedulePersist()
    }

    suspend fun clearDiscardedAssignments() {
        _discardedAssignmentIds.value = emptySet()
        schedulePersist()
    }

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: Flow<List<String>> = _recentSearches.asStateFlow()
    fun getRecentSearches(): List<String> = _recentSearches.value

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = _recentSearches.value
        val updated = (listOf(trimmed) + (current - trimmed)).take(10)
        _recentSearches.value = updated
        schedulePersist()
    }

    suspend fun removeRecentSearch(query: String) {
        _recentSearches.value = _recentSearches.value - query.trim()
        schedulePersist()
    }

    suspend fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        schedulePersist()
    }

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            val content = PlatformFileSystem.readString(prefsFile) ?: return
            val snapshot = json.decodeFromString<PersistentPreferencesSnapshot>(content)
            _universityUrl.value = snapshot.universityUrl
            _accessToken.value = snapshot.accessToken
            _authMode.value = snapshot.authMode
            _sessionCookies.value = snapshot.sessionCookies
            _csrfToken.value = snapshot.csrfToken
            _isOnboardingCompleted.value = snapshot.isOnboardingCompleted
            _syncIntervalMinutes.value = snapshot.syncIntervalMinutes
            _notif24h.value = snapshot.notif24h
            _notif3h.value = snapshot.notif3h
            _notif30m.value = snapshot.notif30m
            _notifMorning.value = snapshot.notifMorning
            _autoFocusMode.value = snapshot.autoFocusMode
            _pomodoroDurationMinutes.value = snapshot.pomodoroDurationMinutes
            _copilotProvider.value = snapshot.copilotProvider
            _openRouterApiKey.value = snapshot.openRouterApiKey
            _openRouterModel.value = snapshot.openRouterModel
            _googleAiStudioApiKey.value = snapshot.googleAiStudioApiKey
            _googleAiStudioModel.value = if (snapshot.googleAiStudioModel.contains("1.5") ||
                snapshot.googleAiStudioModel.contains("2.0") ||
                snapshot.googleAiStudioModel == "gemini-2.5-flash-lite"
            ) {
                "gemini-3.5-flash-lite"
            } else {
                snapshot.googleAiStudioModel
            }
            _copilotEnabled.value = snapshot.copilotEnabled
            _totalCopilotTokens.value = snapshot.totalCopilotTokens
            _dynamicColorEnabled.value = snapshot.dynamicColorEnabled
            _themeStyle.value = snapshot.themeStyle
            _appLanguage.value = snapshot.appLanguage
            _unconfiguredCoursesBannerDismissedUntil.value = snapshot.unconfiguredCoursesBannerDismissedUntil
            _googleDriveConnected.value = snapshot.googleDriveConnected
            _googleDriveEmail.value = snapshot.googleDriveEmail
            _googleDriveLastSyncTimestamp.value = snapshot.googleDriveLastSyncTimestamp
            _googleDriveAccessToken.value = snapshot.googleDriveAccessToken
            _googleDriveRefreshToken.value = snapshot.googleDriveRefreshToken
            _discardedAssignmentIds.value = snapshot.discardedAssignmentIds
            _recentSearches.value = snapshot.recentSearches
        } catch (_: Exception) {
        } finally {
            _isLoaded.value = true
        }
    }

    private fun schedulePersist() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(150)
            persistToDisk()
        }
    }

    private fun persistToDisk() {
        try {
            val snapshot = PersistentPreferencesSnapshot(
                universityUrl = _universityUrl.value,
                accessToken = _accessToken.value,
                authMode = _authMode.value,
                sessionCookies = _sessionCookies.value,
                csrfToken = _csrfToken.value,
                isOnboardingCompleted = _isOnboardingCompleted.value,
                syncIntervalMinutes = _syncIntervalMinutes.value,
                notif24h = _notif24h.value,
                notif3h = _notif3h.value,
                notif30m = _notif30m.value,
                notifMorning = _notifMorning.value,
                autoFocusMode = _autoFocusMode.value,
                pomodoroDurationMinutes = _pomodoroDurationMinutes.value,
                copilotProvider = _copilotProvider.value,
                openRouterApiKey = _openRouterApiKey.value,
                openRouterModel = _openRouterModel.value,
                googleAiStudioApiKey = _googleAiStudioApiKey.value,
                googleAiStudioModel = _googleAiStudioModel.value,
                copilotEnabled = _copilotEnabled.value,
                totalCopilotTokens = _totalCopilotTokens.value,
                dynamicColorEnabled = _dynamicColorEnabled.value,
                themeStyle = _themeStyle.value,
                appLanguage = _appLanguage.value,
                unconfiguredCoursesBannerDismissedUntil = _unconfiguredCoursesBannerDismissedUntil.value,
                googleDriveConnected = _googleDriveConnected.value,
                googleDriveEmail = _googleDriveEmail.value,
                googleDriveLastSyncTimestamp = _googleDriveLastSyncTimestamp.value,
                googleDriveAccessToken = _googleDriveAccessToken.value,
                googleDriveRefreshToken = _googleDriveRefreshToken.value,
                discardedAssignmentIds = _discardedAssignmentIds.value,
                recentSearches = _recentSearches.value
            )
            val jsonString = json.encodeToString(PersistentPreferencesSnapshot.serializer(), snapshot)
            PlatformFileSystem.writeString(prefsFile, jsonString)
        } catch (_: Exception) {
        }
    }

    suspend fun saveGoogleDriveTokens(
        accessToken: String,
        refreshToken: String?,
        userEmail: String?
    ) {
        _googleDriveConnected.value = true
        _googleDriveAccessToken.value = accessToken
        if (refreshToken != null) {
            _googleDriveRefreshToken.value = refreshToken
        }
        if (userEmail != null) {
            _googleDriveEmail.value = userEmail
        }
        _googleDriveLastSyncTimestamp.value = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
        schedulePersist()
    }

    suspend fun updateGoogleDriveLastSync() {
        _googleDriveLastSyncTimestamp.value = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis()
        schedulePersist()
    }

    suspend fun unlinkGoogleDrive() {
        _googleDriveConnected.value = false
        _googleDriveEmail.value = null
        _googleDriveAccessToken.value = null
        _googleDriveRefreshToken.value = null
        _googleDriveLastSyncTimestamp.value = 0L
        schedulePersist()
    }

    suspend fun saveUniversityUrl(url: String) {
        _universityUrl.value = url
        schedulePersist()
    }

    suspend fun saveAccessToken(token: String) {
        _accessToken.value = token
        _authMode.value = me.joxquin.notivas.data.model.AuthMode.TOKEN
        _isSessionExpired.value = false
        schedulePersist()
    }

    suspend fun saveSessionCookies(cookies: String, csrfToken: String? = null) {
        _sessionCookies.value = cookies
        _csrfToken.value = csrfToken
        _authMode.value = me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION
        _isSessionExpired.value = false
        schedulePersist()
    }

    suspend fun setAuthMode(mode: me.joxquin.notivas.data.model.AuthMode) {
        _authMode.value = mode
        schedulePersist()
    }

    suspend fun setNotif24h(enabled: Boolean) {
        _notif24h.value = enabled
        schedulePersist()
    }

    suspend fun setNotif3h(enabled: Boolean) {
        _notif3h.value = enabled
        schedulePersist()
    }

    suspend fun setNotif30m(enabled: Boolean) {
        _notif30m.value = enabled
        schedulePersist()
    }

    suspend fun setNotifMorning(enabled: Boolean) {
        _notifMorning.value = enabled
        schedulePersist()
    }

    suspend fun setAutoFocusMode(enabled: Boolean) {
        _autoFocusMode.value = enabled
        schedulePersist()
    }

    suspend fun setPomodoroDurationMinutes(minutes: Int) {
        _pomodoroDurationMinutes.value = minutes
        schedulePersist()
    }

    suspend fun setSyncIntervalMinutes(minutes: Long) {
        _syncIntervalMinutes.value = minutes
        schedulePersist()
    }

    suspend fun setCopilotProvider(provider: CopilotProvider) {
        _copilotProvider.value = provider
        schedulePersist()
    }

    suspend fun setOpenRouterApiKey(key: String?) {
        _openRouterApiKey.value = key
        schedulePersist()
    }

    suspend fun setOpenRouterModel(model: String) {
        _openRouterModel.value = model
        schedulePersist()
    }

    suspend fun setGoogleAiStudioApiKey(key: String?) {
        _googleAiStudioApiKey.value = key
        schedulePersist()
    }

    suspend fun setGoogleAiStudioModel(model: String) {
        _googleAiStudioModel.value = model
        schedulePersist()
    }

    suspend fun setCopilotEnabled(enabled: Boolean) {
        _copilotEnabled.value = enabled
        schedulePersist()
    }

    suspend fun addCopilotTokens(tokens: Long) {
        if (tokens <= 0) return
        _totalCopilotTokens.value += tokens
        schedulePersist()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
        schedulePersist()
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        _dynamicColorEnabled.value = enabled
        schedulePersist()
    }

    suspend fun setThemeStyle(style: AppThemeStyle) {
        _themeStyle.value = style
        schedulePersist()
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        schedulePersist()
    }

    suspend fun dismissUnconfiguredCoursesBannerFor7Days() {
        val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
        _unconfiguredCoursesBannerDismissedUntil.value = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis() + sevenDaysMs
        schedulePersist()
    }

    suspend fun clear() {
        _accessToken.value = null
        _sessionCookies.value = null
        _csrfToken.value = null
        _authMode.value = me.joxquin.notivas.data.model.AuthMode.COOKIE_SESSION
        _universityUrl.value = null
        _isOnboardingCompleted.value = false
        _googleDriveConnected.value = false
        _googleDriveEmail.value = null
        _googleDriveAccessToken.value = null
        _googleDriveRefreshToken.value = null
        _googleDriveLastSyncTimestamp.value = 0L
        PlatformFileSystem.deleteFile(prefsFile)
    }
}
