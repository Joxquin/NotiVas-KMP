package me.joxquin.notivas.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _biometricLock = MutableStateFlow(false)
    val biometricLock: Flow<Boolean> = _biometricLock.asStateFlow()

    private val _openRouterApiKey = MutableStateFlow<String?>("")
    val openRouterApiKey: Flow<String?> = _openRouterApiKey.asStateFlow()

    private val _openRouterModel = MutableStateFlow("google/gemini-2.5-flash")
    val openRouterModel: Flow<String> = _openRouterModel.asStateFlow()

    private val _copilotEnabled = MutableStateFlow(true)
    val copilotEnabled: Flow<Boolean> = _copilotEnabled.asStateFlow()

    private val _totalCopilotTokens = MutableStateFlow(0L)
    val totalCopilotTokens: Flow<Long> = _totalCopilotTokens.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(true)
    val dynamicColorEnabled: Flow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _themeStyle = MutableStateFlow(AppThemeStyle.MATERIAL)
    val themeStyle: Flow<AppThemeStyle> = _themeStyle.asStateFlow()
    fun getThemeStyle(): AppThemeStyle = _themeStyle.value

    private val _unconfiguredCoursesBannerDismissedUntil = MutableStateFlow(0L)
    val unconfiguredCoursesBannerDismissedUntil: Flow<Long> = _unconfiguredCoursesBannerDismissedUntil.asStateFlow()
    fun getUnconfiguredCoursesBannerDismissedUntil(): Long = _unconfiguredCoursesBannerDismissedUntil.value


    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            val content = PlatformFileSystem.readString(prefsFile) ?: return
            val snapshot = json.decodeFromString<PersistentPreferencesSnapshot>(content)
            _universityUrl.value = snapshot.universityUrl
            _accessToken.value = snapshot.accessToken
            _isOnboardingCompleted.value = snapshot.isOnboardingCompleted
            _syncIntervalMinutes.value = snapshot.syncIntervalMinutes
            _notif24h.value = snapshot.notif24h
            _notif3h.value = snapshot.notif3h
            _notif30m.value = snapshot.notif30m
            _notifMorning.value = snapshot.notifMorning
            _autoFocusMode.value = snapshot.autoFocusMode
            _pomodoroDurationMinutes.value = snapshot.pomodoroDurationMinutes
            _biometricLock.value = snapshot.biometricLock
            _openRouterApiKey.value = snapshot.openRouterApiKey
            _openRouterModel.value = snapshot.openRouterModel
            _copilotEnabled.value = snapshot.copilotEnabled
            _totalCopilotTokens.value = snapshot.totalCopilotTokens
            _dynamicColorEnabled.value = snapshot.dynamicColorEnabled
            _themeStyle.value = snapshot.themeStyle
            _unconfiguredCoursesBannerDismissedUntil.value = snapshot.unconfiguredCoursesBannerDismissedUntil
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
                isOnboardingCompleted = _isOnboardingCompleted.value,
                syncIntervalMinutes = _syncIntervalMinutes.value,
                notif24h = _notif24h.value,
                notif3h = _notif3h.value,
                notif30m = _notif30m.value,
                notifMorning = _notifMorning.value,
                autoFocusMode = _autoFocusMode.value,
                pomodoroDurationMinutes = _pomodoroDurationMinutes.value,
                biometricLock = _biometricLock.value,
                openRouterApiKey = _openRouterApiKey.value,
                openRouterModel = _openRouterModel.value,
                copilotEnabled = _copilotEnabled.value,
                totalCopilotTokens = _totalCopilotTokens.value,
                dynamicColorEnabled = _dynamicColorEnabled.value,
                themeStyle = _themeStyle.value,
                unconfiguredCoursesBannerDismissedUntil = _unconfiguredCoursesBannerDismissedUntil.value
            )
            val jsonString = json.encodeToString(PersistentPreferencesSnapshot.serializer(), snapshot)
            PlatformFileSystem.writeString(prefsFile, jsonString)
        } catch (_: Exception) {
        }
    }


    suspend fun saveUniversityUrl(url: String) {
        _universityUrl.value = url
        schedulePersist()
    }

    suspend fun saveAccessToken(token: String) {
        _accessToken.value = token
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

    suspend fun setBiometricLock(enabled: Boolean) {
        _biometricLock.value = enabled
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

    suspend fun dismissUnconfiguredCoursesBannerFor7Days() {
        val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
        _unconfiguredCoursesBannerDismissedUntil.value = me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis() + sevenDaysMs
        schedulePersist()
    }

    suspend fun clear() {

        _accessToken.value = null
        _universityUrl.value = null
        _isOnboardingCompleted.value = false
        PlatformFileSystem.deleteFile(prefsFile)
    }
}
