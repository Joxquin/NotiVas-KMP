package me.joxquin.notivas.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class OnboardingStep {
    Domain,
    Token,
    Verifying,
    Success,
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
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    val coursesCount by viewModel.coursesCount.collectAsState(initial = 0)

    var currentStep by remember { mutableStateOf(OnboardingStep.Domain) }

    val effectiveStep = when {
        isVerifying -> OnboardingStep.Verifying
        verificationSuccess == true -> OnboardingStep.Success
        verificationSuccess == false -> OnboardingStep.Error
        else -> currentStep
    }

    AnimatedContent(
        targetState = effectiveStep,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { step ->
        when (step) {
            OnboardingStep.Domain -> {
                OnboardingDomainScreen(
                    currentUrl = universityUrl,
                    onContinue = { url ->
                        viewModel.updateUniversityUrl(url)
                        currentStep = OnboardingStep.Token
                    },
                    onBack = { /* Primer paso */ }
                )
            }
            OnboardingStep.Token -> {
                OnboardingTokenScreen(
                    currentToken = accessToken,
                    onContinue = { token ->
                        viewModel.updateAccessToken(token)
                        viewModel.verifyConnection()
                    },
                    onBack = { currentStep = OnboardingStep.Domain }
                )
            }
            OnboardingStep.Verifying -> {
                OnboardingVerifyingScreen()
            }
            OnboardingStep.Success -> {
                OnboardingSuccessScreen(
                    userProfile = userProfile,
                    coursesCount = coursesCount,
                    onStartApp = onComplete
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
                        currentStep = OnboardingStep.Domain
                    }
                )
            }
        }
    }
}
