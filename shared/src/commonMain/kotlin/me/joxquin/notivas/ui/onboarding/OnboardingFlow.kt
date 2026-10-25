package me.joxquin.notivas.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class OnboardingStep {
    WelcomeDomain,
    WebLogin,
    Verifying,
    Permissions,
    GoogleDrive,
    Error
}

@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val universityUrl by viewModel.universityUrl.collectAsState(initial = "https://canvas.utp.edu.pe")
    val accessToken by viewModel.accessToken.collectAsState(initial = "")
    val isVerifying by viewModel.isVerifying.collectAsState(initial = false)
    val verificationSuccess by viewModel.verificationSuccess.collectAsState(initial = null)

    var currentStep by remember { mutableStateOf(OnboardingStep.WelcomeDomain) }
    var hasCompletedAuth by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.resetState()
        currentStep = OnboardingStep.WelcomeDomain
        hasCompletedAuth = false
    }

    val effectiveStep = when {
        currentStep == OnboardingStep.GoogleDrive -> OnboardingStep.GoogleDrive
        currentStep == OnboardingStep.Permissions -> OnboardingStep.Permissions
        isVerifying -> OnboardingStep.Verifying
        verificationSuccess == true && !hasCompletedAuth -> OnboardingStep.Permissions
        verificationSuccess == false -> OnboardingStep.Error
        else -> currentStep
    }

    me.joxquin.notivas.ui.components.BackHandler(enabled = effectiveStep != OnboardingStep.WelcomeDomain) {
        when (effectiveStep) {
            OnboardingStep.WebLogin -> currentStep = OnboardingStep.WelcomeDomain
            OnboardingStep.Permissions -> {
                viewModel.resetVerification()
                hasCompletedAuth = false
                currentStep = OnboardingStep.WebLogin
            }
            OnboardingStep.GoogleDrive -> {
                currentStep = OnboardingStep.Permissions
            }
            OnboardingStep.Error -> {
                viewModel.resetVerification()
                currentStep = OnboardingStep.WelcomeDomain
            }
            else -> {}
        }
    }

    AnimatedContent(
        targetState = effectiveStep,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(350)))
            } else {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(350)))
            }
        },
        label = "onboarding_flow_transition"
    ) { step ->
        when (step) {
            OnboardingStep.WelcomeDomain -> {
                OnboardingWelcomeDomainScreen(
                    currentUrl = universityUrl,
                    currentToken = accessToken,
                    onContinue = { url ->
                        viewModel.updateUniversityUrl(url)
                        currentStep = OnboardingStep.WebLogin
                    },
                    onContinueWithToken = { url, token ->
                        viewModel.updateUniversityUrl(url)
                        viewModel.updateAccessToken(token)
                        viewModel.verifyConnection()
                    }
                )
            }
            OnboardingStep.WebLogin -> {
                OnboardingWebLoginScreen(
                    universityUrl = universityUrl,
                    onLoginSuccess = { cookies, csrfToken ->
                        viewModel.verifySessionConnection(cookies, csrfToken)
                    },
                    onUseManualToken = {
                        currentStep = OnboardingStep.WelcomeDomain
                    },
                    onBack = { currentStep = OnboardingStep.WelcomeDomain }
                )
            }
            OnboardingStep.Verifying -> {
                OnboardingVerifyingScreen()
            }
            OnboardingStep.Permissions -> {
                OnboardingPermissionsScreen(
                    onContinue = {
                        hasCompletedAuth = true
                        currentStep = OnboardingStep.GoogleDrive
                    }
                )
            }
            OnboardingStep.GoogleDrive -> {
                OnboardingGoogleDriveScreen(
                    onFinish = onComplete,
                    onBack = {
                        currentStep = OnboardingStep.Permissions
                    },
                    onSaveGoogleAuth = { token, refresh, email ->
                        viewModel.saveGoogleDriveAuth(token, refresh, email)
                    }
                )
            }
            OnboardingStep.Error -> {
                OnboardingErrorScreen(
                    initialToken = accessToken,
                    onRetry = { newToken ->
                        viewModel.updateAccessToken(newToken)
                        viewModel.verifyConnection()
                    },
                    onChangeDomain = {
                        viewModel.resetVerification()
                        currentStep = OnboardingStep.WelcomeDomain
                    }
                )
            }
        }
    }
}
