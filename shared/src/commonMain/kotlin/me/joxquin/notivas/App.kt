package me.joxquin.notivas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.KtorHttpClientProvider
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.ui.onboarding.OnboardingFlow
import me.joxquin.notivas.ui.onboarding.OnboardingViewModel
import me.joxquin.notivas.ui.theme.AppTheme

@Composable
fun App() {
    // Contenedor compartido de dependencias en memoria
    val preferencesManager = remember { PreferencesManager() }
    val localStore = remember { InMemoryLocalStore() }
    val httpClient = remember { KtorHttpClientProvider.createClient() }
    val canvasApiService = remember { CanvasApiService(httpClient) }
    val canvasRepository = remember {
        CanvasRepository(canvasApiService, localStore, preferencesManager)
    }
    val openRouterApiService = remember { me.joxquin.notivas.data.remote.OpenRouterApiService(httpClient) }
    val copilotRepository = remember {
        me.joxquin.notivas.data.repository.CopilotRepository(openRouterApiService, canvasApiService, localStore, preferencesManager)
    }
    val chatRepository = remember {
        me.joxquin.notivas.data.repository.CopilotChatRepository(localStore)
    }

    val onboardingViewModel = remember { OnboardingViewModel(canvasRepository) }
    val isPreferencesLoaded by preferencesManager.isLoaded.collectAsState(initial = false)
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(
        initial = preferencesManager.getIsOnboardingCompleted()
    )
    val isDynamicColorEnabled by preferencesManager.dynamicColorEnabled.collectAsState(initial = true)
    val themeStyle by preferencesManager.themeStyle.collectAsState(
        initial = preferencesManager.getThemeStyle()
    )

    AppTheme(
        dynamicColor = isDynamicColorEnabled,
        themeStyle = themeStyle
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!isPreferencesLoaded) {
                // Estado de carga inicial mientras se lee el almacenamiento en disco (evita el parpadeo de onboarding)
                Box(modifier = Modifier.fillMaxSize())
            } else if (!isOnboardingCompleted) {
                OnboardingFlow(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        onboardingViewModel.finishOnboarding()
                    }
                )
            } else {
                me.joxquin.notivas.ui.MainAppShell(
                    canvasRepository = canvasRepository,
                    copilotRepository = copilotRepository,
                    chatRepository = chatRepository,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
}