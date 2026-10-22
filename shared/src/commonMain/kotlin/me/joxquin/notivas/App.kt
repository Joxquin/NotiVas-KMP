package me.joxquin.notivas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.KtorHttpClientProvider
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.security.rememberBiometricAuthenticator
import me.joxquin.notivas.ui.onboarding.OnboardingFlow
import me.joxquin.notivas.ui.onboarding.OnboardingViewModel
import me.joxquin.notivas.ui.theme.AppTheme
import me.joxquin.notivas.util.ProvideAppLocale
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.biometric_lock_title
import me.joxquin.notivas.shared.resources.biometric_lock_desc

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
    val isBiometricLockEnabled by preferencesManager.biometricLock.collectAsState(
        initial = preferencesManager.getBiometricLock()
    )
    var isBiometricAuthenticated by remember { mutableStateOf(false) }
    val biometricAuthenticator = rememberBiometricAuthenticator()

    val isDynamicColorEnabled by preferencesManager.dynamicColorEnabled.collectAsState(initial = true)
    val themeStyle by preferencesManager.themeStyle.collectAsState(
        initial = preferencesManager.getThemeStyle()
    )
    val appLanguage by preferencesManager.appLanguage.collectAsState(
        initial = preferencesManager.getAppLanguage()
    )

    LaunchedEffect(isPreferencesLoaded, isOnboardingCompleted, isBiometricLockEnabled) {
        if (isPreferencesLoaded && isOnboardingCompleted && isBiometricLockEnabled && !isBiometricAuthenticated) {
            if (biometricAuthenticator.canAuthenticate()) {
                biometricAuthenticator.authenticate(
                    title = "NotiVas",
                    subtitle = "Desbloquear con biometría",
                    onSuccess = { isBiometricAuthenticated = true },
                    onError = {}
                )
            } else {
                isBiometricAuthenticated = true
            }
        }
    }

    ProvideAppLocale(language = appLanguage) {
        AppTheme(
            dynamicColor = isDynamicColorEnabled,
            themeStyle = themeStyle
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (!isPreferencesLoaded) {
                    Box(modifier = Modifier.fillMaxSize())
                } else if (!isOnboardingCompleted) {
                    OnboardingFlow(
                        viewModel = onboardingViewModel,
                        onComplete = {
                            onboardingViewModel.finishOnboarding()
                        }
                    )
                } else if (isBiometricLockEnabled && !isBiometricAuthenticated) {
                    // Pantalla de bloqueo biométrico pendiente
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(Res.string.biometric_lock_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.biometric_lock_desc),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    biometricAuthenticator.authenticate(
                                        title = "NotiVas",
                                        subtitle = "Desbloquear con biometría",
                                        onSuccess = { isBiometricAuthenticated = true },
                                        onError = {}
                                    )
                                }
                            ) {
                                Text("Desbloquear")
                            }
                        }
                    }
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
}
