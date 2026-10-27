package me.joxquin.notivas.ui.onboarding

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import me.joxquin.notivas.ui.navigation.miuix.CrossActivityTransition
import me.joxquin.notivas.ui.navigation.miuix.MiuixNavigator
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius

@Serializable
sealed interface OnboardingRoute : NavKey {
    @Serializable
    data object WelcomeDomain : OnboardingRoute

    @Serializable
    data object WebLogin : OnboardingRoute

    @Serializable
    data object Verifying : OnboardingRoute

    @Serializable
    data object Permissions : OnboardingRoute

    @Serializable
    data object GoogleDrive : OnboardingRoute

    @Serializable
    data object Error : OnboardingRoute
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

    val onboardingBackStack = rememberNavBackStack<OnboardingRoute>(OnboardingRoute.WelcomeDomain)
    val onboardingNavigator = remember(onboardingBackStack) { MiuixNavigator(onboardingBackStack) }

    LaunchedEffect(Unit) {
        viewModel.resetState()
        onboardingNavigator.popUntil { it == OnboardingRoute.WelcomeDomain }
    }

    LaunchedEffect(isVerifying) {
        if (isVerifying) {
            if (onboardingNavigator.current() != OnboardingRoute.Verifying) {
                onboardingNavigator.push(OnboardingRoute.Verifying)
            }
        }
    }

    LaunchedEffect(verificationSuccess) {
        when (verificationSuccess) {
            true -> {
                if (onboardingNavigator.current() == OnboardingRoute.Verifying) {
                    onboardingNavigator.replace(OnboardingRoute.Permissions)
                }
            }
            false -> {
                if (onboardingNavigator.current() == OnboardingRoute.Verifying) {
                    onboardingNavigator.replace(OnboardingRoute.Error)
                }
            }
            null -> {}
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val navCornerRadius = rememberNavSystemCornerRadius()
    val onboardingAospEffects = remember(isDarkTheme, navCornerRadius) {
        NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = if (navCornerRadius <= 0.dp) 32.dp else navCornerRadius,
            cornerClipMode = NavCornerClipMode.All,
            dimAmount = if (isDarkTheme) 0.8f else 0.2f,
        )
    }

    val layoutDirection = LocalLayoutDirection.current
    val swipeBackDirection = if (layoutDirection == LayoutDirection.Rtl) {
        NavSwipeDirection.RightToLeft
    } else {
        NavSwipeDirection.LeftToRight
    }

    fun handleOnboardingBack(): Boolean {
        val current = onboardingNavigator.current()
        return when (current) {
            OnboardingRoute.Permissions -> {
                viewModel.resetVerification()
                onboardingNavigator.pop()
            }
            OnboardingRoute.Error -> {
                viewModel.resetVerification()
                onboardingNavigator.popUntil { it == OnboardingRoute.WelcomeDomain }
                true
            }
            else -> {
                onboardingNavigator.pop()
            }
        }
    }

    NavDisplay(
        backStack = onboardingBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { handleOnboardingBack() },
        transition = CrossActivityTransition,
        effects = onboardingAospEffects,
    ) {
        entry<OnboardingRoute.WelcomeDomain> {
            OnboardingWelcomeDomainScreen(
                currentUrl = universityUrl,
                currentToken = accessToken,
                onContinue = { url ->
                    viewModel.updateUniversityUrl(url)
                    onboardingNavigator.push(OnboardingRoute.WebLogin)
                },
                onContinueWithToken = { url, token ->
                    viewModel.updateUniversityUrl(url)
                    viewModel.updateAccessToken(token)
                    viewModel.verifyConnection()
                }
            )
        }

        entry<OnboardingRoute.WebLogin>(swipeDismiss = swipeBackDirection) {
            OnboardingWebLoginScreen(
                universityUrl = universityUrl,
                onLoginSuccess = { cookies, csrfToken ->
                    viewModel.verifySessionConnection(cookies, csrfToken)
                },
                onUseManualToken = {
                    onboardingNavigator.pop()
                },
                onBack = { handleOnboardingBack() }
            )
        }

        entry<OnboardingRoute.Verifying> {
            OnboardingVerifyingScreen()
        }

        entry<OnboardingRoute.Permissions>(swipeDismiss = swipeBackDirection) {
            OnboardingPermissionsScreen(
                onContinue = {
                    onboardingNavigator.push(OnboardingRoute.GoogleDrive)
                }
            )
        }

        entry<OnboardingRoute.GoogleDrive>(swipeDismiss = swipeBackDirection) {
            OnboardingGoogleDriveScreen(
                onFinish = onComplete,
                onBack = { handleOnboardingBack() },
                onSaveGoogleAuth = { token, refresh, email ->
                    viewModel.saveGoogleDriveAuth(token, refresh, email)
                }
            )
        }

        entry<OnboardingRoute.Error>(swipeDismiss = swipeBackDirection) {
            OnboardingErrorScreen(
                initialToken = accessToken,
                onRetry = { newToken ->
                    viewModel.updateAccessToken(newToken)
                    viewModel.verifyConnection()
                },
                onChangeDomain = {
                    viewModel.resetVerification()
                    onboardingNavigator.popUntil { it == OnboardingRoute.WelcomeDomain }
                }
            )
        }
    }
}
