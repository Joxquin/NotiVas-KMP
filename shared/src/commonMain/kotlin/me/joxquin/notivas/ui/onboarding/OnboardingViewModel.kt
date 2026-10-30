package me.joxquin.notivas.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
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
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    try {
                        repository.fetchAndSaveData()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    try {
                        val count = repository.allCourses.first().size
                        _coursesCount.value = count
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    _verificationSuccess.value = true
                } else {
                    _verificationSuccess.value = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _verificationSuccess.value = false
            } finally {
                _isVerifying.value = false
            }
        }
    }

    fun verifySessionConnection(cookies: String, csrfToken: String?) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationSuccess.value = null
            try {
                val success = repository.verifyAndSaveSession(_universityUrl.value, cookies, csrfToken)
                if (success) {
                    try {
                        val profile = repository.getProfile()
                        _userProfile.value = profile
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    try {
                        repository.fetchAndSaveData()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    try {
                        val count = repository.allCourses.first().size
                        _coursesCount.value = count
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) { }

                    _verificationSuccess.value = true
                } else {
                    _verificationSuccess.value = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
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

    fun saveGoogleDriveAuth(accessToken: String, refreshToken: String?, email: String?) {
        viewModelScope.launch {
            repository.saveGoogleDriveAuth(accessToken, refreshToken, email)
        }
    }

    fun resetVerification() {
        _verificationSuccess.value = null
    }

    fun resetState() {
        _universityUrl.value = "https://canvas.utp.edu.pe"
        _accessToken.value = ""
        _isVerifying.value = false
        _verificationSuccess.value = null
        _userProfile.value = null
        _coursesCount.value = 0
    }
}
