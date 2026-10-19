package me.joxquin.notivas.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.background.BackgroundSyncScheduler
import me.joxquin.notivas.data.local.AppLanguage
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.AuthMode
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.data.repository.OpenRouterAccountBalance

data class ProfileUiState(
    val profile: UserProfile? = null,
    val universityHost: String = "Canvas LMS",
    val authMode: AuthMode = AuthMode.COOKIE_SESSION,
    val notif24h: Boolean = true,
    val notif3h: Boolean = true,
    val notif30m: Boolean = false,
    val notifMorning: Boolean = true,
    val autoFocusMode: Boolean = true,
    val pomodoroDurationMinutes: Int = 25,
    val syncIntervalMinutes: Long = 15L,
    val biometricLock: Boolean = false,
    val copilotProvider: me.joxquin.notivas.data.local.CopilotProvider = me.joxquin.notivas.data.local.CopilotProvider.OPENROUTER,
    val openRouterApiKey: String? = null,
    val openRouterModel: String = "google/gemini-2.5-flash",
    val googleAiStudioApiKey: String? = null,
    val googleAiStudioModel: String = "gemini-3.5-flash-lite",
    val copilotEnabled: Boolean = true,
    val totalCopilotTokens: Long = 0L,
    val openRouterBalance: OpenRouterAccountBalance? = null,
    val isLoadingBalance: Boolean = false,
    val isVerifyingToken: Boolean = false,
    val lastSyncAgoText: String = "hace 5 min",
    val apiLatencyMs: Int = 42,
    val dynamicColorEnabled: Boolean = true,
    val themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val googleDriveConnected: Boolean = false,
    val googleDriveEmail: String? = null,
    val googleDriveLastSyncTimestamp: Long = 0L,
    val googleDriveCacheSize: String = "0 KB"
)

class ProfileViewModel(
    private val canvasRepository: CanvasRepository,
    private val copilotRepository: CopilotRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _isVerifyingToken = MutableStateFlow(false)
    val isVerifyingToken: StateFlow<Boolean> = _isVerifyingToken.asStateFlow()

    private val _openRouterBalance = MutableStateFlow<OpenRouterAccountBalance?>(null)
    private val _isLoadingBalance = MutableStateFlow(false)
    private val _googleDriveCacheSize = MutableStateFlow("0 KB")

    init {
        fetchProfile()
        refreshOpenRouterBalance()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                canvasRepository.getProfile()
            } catch (_: Exception) { }
        }
    }

    fun refreshOpenRouterBalance() {
        viewModelScope.launch {
            _isLoadingBalance.value = true
            _openRouterBalance.value = copilotRepository.getOpenRouterBalance()
            _isLoadingBalance.value = false
        }
    }

    fun verifyToken() {
        viewModelScope.launch {
            _isVerifyingToken.value = true
            try {
                canvasRepository.getProfile()
            } catch (_: Exception) {
            } finally {
                _isVerifyingToken.value = false
            }
        }
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        canvasRepository.userProfile,
        canvasRepository.universityUrl,
        preferencesManager.notif24h,
        preferencesManager.notif3h,
        preferencesManager.notif30m,
        preferencesManager.notifMorning,
        preferencesManager.autoFocusMode,
        preferencesManager.pomodoroDurationMinutes,
        preferencesManager.syncIntervalMinutes,
        preferencesManager.biometricLock,
        preferencesManager.copilotProvider,
        preferencesManager.openRouterApiKey,
        preferencesManager.openRouterModel,
        preferencesManager.googleAiStudioApiKey,
        preferencesManager.googleAiStudioModel,
        preferencesManager.copilotEnabled,
        preferencesManager.totalCopilotTokens,
        _openRouterBalance,
        _isLoadingBalance,
        _isVerifyingToken,
        preferencesManager.dynamicColorEnabled,
        preferencesManager.themeStyle,
        preferencesManager.authMode,
        preferencesManager.appLanguage,
        preferencesManager.googleDriveConnected,
        preferencesManager.googleDriveEmail,
        preferencesManager.googleDriveLastSyncTimestamp,
        _googleDriveCacheSize
    ) { args: Array<Any?> ->
        val url = args[1] as String?
        val host = if (url.isNullOrBlank()) "Canvas LMS" else {
            val clean = url.removePrefix("https://").removePrefix("http://").trim()
            clean.substringBefore('/')
        }

        ProfileUiState(
            profile = args[0] as UserProfile?,
            universityHost = host,
            notif24h = args[2] as Boolean,
            notif3h = args[3] as Boolean,
            notif30m = args[4] as Boolean,
            notifMorning = args[5] as Boolean,
            autoFocusMode = args[6] as Boolean,
            pomodoroDurationMinutes = args[7] as Int,
            syncIntervalMinutes = args[8] as Long,
            biometricLock = args[9] as Boolean,
            copilotProvider = args[10] as me.joxquin.notivas.data.local.CopilotProvider,
            openRouterApiKey = args[11] as String?,
            openRouterModel = args[12] as String,
            googleAiStudioApiKey = args[13] as String?,
            googleAiStudioModel = args[14] as String,
            copilotEnabled = args[15] as Boolean,
            totalCopilotTokens = args[16] as Long,
            openRouterBalance = args[17] as OpenRouterAccountBalance?,
            isLoadingBalance = args[18] as Boolean,
            isVerifyingToken = args[19] as Boolean,
            dynamicColorEnabled = args[20] as Boolean,
            themeStyle = args[21] as AppThemeStyle,
            authMode = args[22] as AuthMode,
            appLanguage = args[23] as AppLanguage,
            googleDriveConnected = args[24] as Boolean,
            googleDriveEmail = args[25] as String?,
            googleDriveLastSyncTimestamp = args[26] as Long,
            googleDriveCacheSize = args[27] as String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun saveGoogleDriveAuth(accessToken: String, refreshToken: String?, email: String?) {
        viewModelScope.launch {
            preferencesManager.saveGoogleDriveTokens(
                accessToken = accessToken,
                refreshToken = refreshToken ?: "",
                userEmail = email ?: "Google Drive Conectado"
            )
        }
    }

    fun unlinkGoogleDrive() {
        viewModelScope.launch {
            preferencesManager.unlinkGoogleDrive()
            _googleDriveCacheSize.value = "0 KB"
        }
    }

    fun clearDraftsCache() {
        viewModelScope.launch {
            canvasRepository.deleteAllAssignmentDrafts()
            _googleDriveCacheSize.value = "0 KB"
            preferencesManager.updateGoogleDriveLastSync()
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesManager.setAppLanguage(language) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDynamicColorEnabled(enabled) }
    }

    fun setThemeStyle(style: AppThemeStyle) {
        viewModelScope.launch { preferencesManager.setThemeStyle(style) }
    }

    fun setNotif24h(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotif24h(enabled) }
    }

    fun setNotif3h(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotif3h(enabled) }
    }

    fun setNotif30m(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotif30m(enabled) }
    }

    fun setNotifMorning(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotifMorning(enabled) }
    }

    fun setSyncIntervalMinutes(minutes: Long) {
        viewModelScope.launch {
            preferencesManager.setSyncIntervalMinutes(minutes)
            BackgroundSyncScheduler.schedulePeriodicSync(minutes)
        }
    }

    fun setAutoFocusMode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoFocusMode(enabled) }
    }

    fun setPomodoroDuration(minutes: Int) {
        viewModelScope.launch { preferencesManager.setPomodoroDurationMinutes(minutes) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBiometricLock(enabled) }
    }

    fun setCopilotProvider(provider: me.joxquin.notivas.data.local.CopilotProvider) {
        viewModelScope.launch {
            preferencesManager.setCopilotProvider(provider)
            if (provider == me.joxquin.notivas.data.local.CopilotProvider.OPENROUTER) {
                refreshOpenRouterBalance()
            }
        }
    }

    fun setOpenRouterApiKey(key: String?) {
        viewModelScope.launch {
            preferencesManager.setOpenRouterApiKey(key)
            refreshOpenRouterBalance()
        }
    }

    fun setOpenRouterModel(model: String) {
        viewModelScope.launch { preferencesManager.setOpenRouterModel(model) }
    }

    fun setGoogleAiStudioApiKey(key: String?) {
        viewModelScope.launch { preferencesManager.setGoogleAiStudioApiKey(key) }
    }

    fun setGoogleAiStudioModel(model: String) {
        viewModelScope.launch { preferencesManager.setGoogleAiStudioModel(model) }
    }

    fun setCopilotEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setCopilotEnabled(enabled) }
    }

    suspend fun analyzeCustomModel(modelId: String): me.joxquin.notivas.data.model.CustomModelAnalysisResult {
        return copilotRepository.analyzeModelCompatibility(modelId)
    }

    fun logout() {
        viewModelScope.launch {
            BackgroundSyncScheduler.cancelPeriodicSync()
            canvasRepository.logout()
            _isLoggedOut.value = true
        }
    }
}
