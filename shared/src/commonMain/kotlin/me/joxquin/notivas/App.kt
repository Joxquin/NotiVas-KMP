package me.joxquin.notivas

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.KtorHttpClientProvider
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.ui.onboarding.OnboardingFlow
import me.joxquin.notivas.ui.onboarding.OnboardingViewModel
import me.joxquin.notivas.ui.splash.AppStartupSplash
import me.joxquin.notivas.ui.theme.AppTheme
import me.joxquin.notivas.util.ProvideAppLocale

@Composable
fun App() {
    // Contenedor compartido de dependencias de la aplicación
    val preferencesManager = remember { me.joxquin.notivas.di.AppModule.preferencesManager }
    val localStore = remember { me.joxquin.notivas.di.AppModule.localStore }
    val canvasRepository = remember { me.joxquin.notivas.di.AppModule.canvasRepository }
    val copilotRepository = remember { me.joxquin.notivas.di.AppModule.copilotRepository }
    val chatRepository = remember { me.joxquin.notivas.di.AppModule.copilotChatRepository }

    val onboardingViewModel = remember { OnboardingViewModel(canvasRepository) }
    val isPreferencesLoaded by preferencesManager.isLoaded.collectAsState(initial = false)
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(
        initial = preferencesManager.getIsOnboardingCompleted()
    )

    val isDynamicColorEnabled by preferencesManager.dynamicColorEnabled.collectAsState(initial = true)
    val themeStyle by preferencesManager.themeStyle.collectAsState(
        initial = preferencesManager.getThemeStyle()
    )
    val appLanguage by preferencesManager.appLanguage.collectAsState(
        initial = preferencesManager.getAppLanguage()
    )

    var hasStartupSplashRun by rememberSaveable { mutableStateOf(false) }
    var isPreloadReady by remember { mutableStateOf(false) }
    val cachedCourses by canvasRepository.allCourses.collectAsState(initial = emptyList())

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted && !hasStartupSplashRun) {
            delay(400)
            isPreloadReady = true
        }
    }

    val isDataReady = cachedCourses.isNotEmpty() || isPreloadReady

    ProvideAppLocale(language = appLanguage) {
        AppTheme(
            dynamicColor = isDynamicColorEnabled,
            themeStyle = themeStyle
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (!isPreferencesLoaded) {
                    Box(modifier = Modifier.fillMaxSize())
                } else {
                    androidx.compose.animation.AnimatedContent(
                        targetState = isOnboardingCompleted,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(450, easing = androidx.compose.animation.core.FastOutSlowInEasing)) +
                             androidx.compose.animation.scaleIn(initialScale = 0.94f, animationSpec = androidx.compose.animation.core.tween(450, easing = androidx.compose.animation.core.FastOutSlowInEasing)))
                                .togetherWith(
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                                    androidx.compose.animation.scaleOut(targetScale = 1.04f, animationSpec = androidx.compose.animation.core.tween(300))
                                )
                        },
                        label = "onboarding_to_dashboard_transition"
                    ) { completed ->
                        if (!completed) {
                            androidx.compose.runtime.key(completed) {
                                OnboardingFlow(
                                    viewModel = onboardingViewModel,
                                    onComplete = {
                                        onboardingViewModel.finishOnboarding()
                                    }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                me.joxquin.notivas.ui.MainAppShell(
                                    canvasRepository = canvasRepository,
                                    copilotRepository = copilotRepository,
                                    chatRepository = chatRepository,
                                    preferencesManager = preferencesManager,
                                    isEntranceTriggered = hasStartupSplashRun
                                )

                                if (!hasStartupSplashRun) {
                                    AppStartupSplash(
                                        themeStyle = themeStyle,
                                        isDataReady = isDataReady,
                                        onSplashFinished = {
                                            hasStartupSplashRun = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
