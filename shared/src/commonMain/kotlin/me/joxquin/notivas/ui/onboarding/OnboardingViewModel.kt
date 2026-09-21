package me.joxquin.notivas.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.repository.CanvasRepository

class OnboardingViewModel(
    private val repository: CanvasRepository
) : ViewModel() {

    private val _universityUrl = MutableStateFlow("https://canvas.utp.edu.pe")
    val universityUrl: StateFlow<String> = _universityUrl.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _verificationSuccess = MutableStateFlow<Boolean?>(null)
    val verificationSuccess: StateFlow<Boolean?> = _verificationSuccess.asStateFlow()

    private val _userProfile = MutableStateFlow<me.joxquin.notivas.data.model.UserProfile?>(null)
    val userProfile: StateFlow<me.joxquin.notivas.data.model.UserProfile?> = _userProfile.asStateFlow()

    private val _coursesCount = MutableStateFlow(0)
    val coursesCount: StateFlow<Int> = _coursesCount.asStateFlow()

    fun updateUniversityUrl(url: String) {
        _universityUrl.value = url
    }

    fun updateAccessToken(token: String) {
        _accessToken.value = token
    }

    fun verifyConnection() {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationSuccess.value = null
            try {
                val success = repository.verifyAndSave(_universityUrl.value, _accessToken.value)
                if (success) {
                    try {
                        val profile = repository.getProfile()
                        _userProfile.value = profile
                    } catch (_: Exception) { }

                    try {
                        repository.fetchAndSaveData()
                    } catch (_: Exception) { }

                    try {
                        val count = repository.allCourses.first().size
                        _coursesCount.value = count
                    } catch (_: Exception) { }

                    _verificationSuccess.value = true
                } else {
                    _verificationSuccess.value = false
                }
            } catch (_: Exception) {
                _verificationSuccess.value = false
            } finally {
                _isVerifying.value = false
            }
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun resetVerification() {
        _verificationSuccess.value = null
    }
}
