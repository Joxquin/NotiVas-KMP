package me.joxquin.notivas.di

import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.remote.CanvasApiService
import me.joxquin.notivas.data.remote.KtorHttpClientProvider
import me.joxquin.notivas.data.remote.OpenRouterApiService
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotChatRepository
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.domain.usecase.CalculateWhatIfGradeUseCase
import me.joxquin.notivas.domain.usecase.GetUrgentAssignmentsUseCase
import me.joxquin.notivas.security.SecureTokenVault

object AppModule {
    val httpClient by lazy { KtorHttpClientProvider.createClient() }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager() }
    val localStore: InMemoryLocalStore by lazy { InMemoryLocalStore() }
    val secureTokenVault: SecureTokenVault by lazy { SecureTokenVault(preferencesManager) }
    
    val canvasApiService: CanvasApiService by lazy { CanvasApiService(httpClient) }
    val openRouterApiService: OpenRouterApiService by lazy { OpenRouterApiService(httpClient) }
    
    val canvasRepository: CanvasRepository by lazy {
        CanvasRepository(canvasApiService, localStore, preferencesManager)
    }
    
    val copilotRepository: CopilotRepository by lazy {
        CopilotRepository(openRouterApiService, canvasApiService, localStore, preferencesManager)
    }
    
    val copilotChatRepository: CopilotChatRepository by lazy {
        CopilotChatRepository(localStore)
    }

    val getUrgentAssignmentsUseCase: GetUrgentAssignmentsUseCase by lazy {
        GetUrgentAssignmentsUseCase()
    }
    
    val calculateWhatIfGradeUseCase: CalculateWhatIfGradeUseCase by lazy {
        CalculateWhatIfGradeUseCase()
    }
}
