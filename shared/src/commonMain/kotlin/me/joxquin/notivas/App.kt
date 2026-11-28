package me.joxquin.notivas

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.joxquin.notivas.data.local.AppLanguage
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.KtorHttpClientProvider
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.ui.components.brand.NotiVasBrandLogo
import me.joxquin.notivas.ui.demo.SemesterDemoScreen
import me.joxquin.notivas.ui.onboarding.OnboardingFlow
import me.joxquin.notivas.ui.onboarding.OnboardingViewModel
import me.joxquin.notivas.ui.splash.AppStartupSplash
import me.joxquin.notivas.ui.splash.LocalSplashLogoTarget
import me.joxquin.notivas.ui.theme.AppTheme
import me.joxquin.notivas.util.ProvideAppLocale

@Composable
fun App(startInDemo: Boolean = false) {
    var demoOpen by rememberSaveable { mutableStateOf(startInDemo) }
    var demoTheme by rememberSaveable { mutableStateOf(AppThemeStyle.MATERIAL.name) }
    var demoLanguage by rememberSaveable { mutableStateOf(AppLanguage.SYSTEM.name) }
    if (demoOpen) {
        val selectedTheme = AppThemeStyle.valueOf(demoTheme)
        ProvideAppLocale(language = AppLanguage.valueOf(demoLanguage)) {
            AppTheme(themeStyle = selectedTheme) {
                SemesterDemoScreen(themeStyle = selectedTheme, onExit = { demoOpen = false })
            }
        }
        return
    }

    // Contenedor compartido de dependencias de la aplicación
    val preferencesManager = remember { me.joxquin.notivas.di.AppModule.preferencesManager }
    val localStore = remember { me.joxquin.notivas.di.AppModule.localStore }
    val canvasRepository = remember { me.joxquin.notivas.di.AppModule.canvasRepository }
    val copilotRepository = remember { me.joxquin.notivas.di.AppModule.copilotRepository }
    val chatRepository = remember { me.joxquin.notivas.di.AppModule.copilotChatRepository }

    val onboardingViewModel = remember { OnboardingViewModel(canvasRepository) }
    val isPreferencesLoaded by preferencesManager.isLoaded.collectAsState(
        initial = preferencesManager.isLoadedSync()
    )
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
    var targetLogoBounds by remember { mutableStateOf<Rect?>(null) }
    val cachedCourses by canvasRepository.allCourses.collectAsState(initial = emptyList())

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted && !hasStartupSplashRun) {
            delay(400)
            isPreloadReady = true
        }
    }

    val isDataReady = cachedCourses.isNotEmpty() || isPreloadReady

    val effectiveThemeStyle = if (themeStyle == me.joxquin.notivas.data.local.AppThemeStyle.MIUIX && !me.joxquin.notivas.ui.theme.isMiuixThemeSupported()) {
        me.joxquin.notivas.data.local.AppThemeStyle.MATERIAL
    } else {
        themeStyle
    }

    ProvideAppLocale(language = appLanguage) {
        AppTheme(
            dynamicColor = isDynamicColorEnabled,
            themeStyle = effectiveThemeStyle
        ) {
            CompositionLocalProvider(
                LocalSplashLogoTarget provides { bounds ->
                    if (targetLogoBounds == null || targetLogoBounds != bounds) {
                        targetLogoBounds = bounds
                    }
                }
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!isPreferencesLoaded) {
                        val startLogoSize = 189.dp
                        val anchorYFraction = 75.5f / 140f
                        val logoVerticalOffset = -(startLogoSize * (anchorYFraction - 0.5f))
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = logoVerticalOffset)
                                    .size(startLogoSize),
                                contentAlignment = Alignment.Center
                            ) {
                                NotiVasBrandLogo(
                                    size = startLogoSize,
                                    transparentBackground = true,
                                    elevation = 0.dp
                                )
                            }
                        }
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
                                        onTryDemo = {
                                            demoTheme = effectiveThemeStyle.name
                                            demoLanguage = appLanguage.name
                                            demoOpen = true
                                        },
                                        onComplete = {
                                            onboardingViewModel.finishOnboarding()
                                        },
                                        themeStyle = effectiveThemeStyle
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Los promedios que suben y las listas escalonadas esperan a que termine la animación de arranque.
                                    CompositionLocalProvider(
                                        me.joxquin.notivas.ui.components.LocalEntranceAnimationsReady provides hasStartupSplashRun
                                    ) {
                                        me.joxquin.notivas.ui.MainAppShell(
                                            canvasRepository = canvasRepository,
                                            copilotRepository = copilotRepository,
                                            chatRepository = chatRepository,
                                            preferencesManager = preferencesManager,
                                            isEntranceTriggered = hasStartupSplashRun
                                        )
                                    }

                                    if (!hasStartupSplashRun) {
                                        AppStartupSplash(
                                            themeStyle = effectiveThemeStyle,
                                            isDataReady = isDataReady,
                                            targetLogoBounds = targetLogoBounds,
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
}
