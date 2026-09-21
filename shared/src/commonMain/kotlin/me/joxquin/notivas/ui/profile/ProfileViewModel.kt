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
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.data.repository.OpenRouterAccountBalance

data class ProfileUiState(
    val profile: UserProfile? = null,
    val universityHost: String = "Canvas LMS",
    val notif24h: Boolean = true,
    val notif3h: Boolean = true,
    val notif30m: Boolean = false,
    val notifMorning: Boolean = true,
    val autoFocusMode: Boolean = true,
    val pomodoroDurationMinutes: Int = 25,
    val syncIntervalMinutes: Long = 15L,
    val biometricLock: Boolean = false,
    val openRouterApiKey: String? = null,
    val openRouterModel: String = "google/gemini-2.5-flash",
    val copilotEnabled: Boolean = true,
    val totalCopilotTokens: Long = 0L,
    val openRouterBalance: OpenRouterAccountBalance? = null,
    val isLoadingBalance: Boolean = false,
    val isVerifyingToken: Boolean = false,
    val lastSyncAgoText: String = "hace 5 min",
    val apiLatencyMs: Int = 42,
    val dynamicColorEnabled: Boolean = true,
    val themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
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
        preferencesManager.openRouterApiKey,
        preferencesManager.openRouterModel,
        preferencesManager.copilotEnabled,
        preferencesManager.totalCopilotTokens,
        _openRouterBalance,
        _isLoadingBalance,
        _isVerifyingToken,
        preferencesManager.dynamicColorEnabled,
        preferencesManager.themeStyle
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
            openRouterApiKey = args[10] as String?,
            openRouterModel = args[11] as String,
            copilotEnabled = args[12] as Boolean,
            totalCopilotTokens = args[13] as Long,
            openRouterBalance = args[14] as OpenRouterAccountBalance?,
            isLoadingBalance = args[15] as Boolean,
            isVerifyingToken = args[16] as Boolean,
            dynamicColorEnabled = args[17] as Boolean,
            themeStyle = args[18] as AppThemeStyle
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

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
        viewModelScope.launch { preferencesManager.setSyncIntervalMinutes(minutes) }
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

    fun setOpenRouterApiKey(key: String?) {
        viewModelScope.launch {
            preferencesManager.setOpenRouterApiKey(key)
            refreshOpenRouterBalance()
        }
    }

    fun setOpenRouterModel(model: String) {
        viewModelScope.launch { preferencesManager.setOpenRouterModel(model) }
    }

    fun setCopilotEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setCopilotEnabled(enabled) }
    }

    fun logout() {
        viewModelScope.launch {
            canvasRepository.logout()
            _isLoggedOut.value = true
        }
    }
}
