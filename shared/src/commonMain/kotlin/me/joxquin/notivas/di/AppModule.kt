package me.joxquin.notivas.di

import me.joxquin.notivas.data.cloud.SimulationCloudSync
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.local.db.NotivasDatabase
import me.joxquin.notivas.data.local.db.createRoomDatabase
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
    val database: NotivasDatabase by lazy { createRoomDatabase() }
    val localStore: InMemoryLocalStore by lazy { InMemoryLocalStore(database) }
    val secureTokenVault: SecureTokenVault by lazy { SecureTokenVault(preferencesManager) }

    // Direct Room DAOs for convenient injection
    val courseDao by lazy { database.courseDao() }
    val assignmentDao by lazy { database.assignmentDao() }
    val copilotChatDao by lazy { database.copilotChatDao() }
    val simulationDao by lazy { database.simulationDao() }
    val plannerDao by lazy { database.plannerDao() }
    
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

    // Respaldo en la nube del simulador; solo se activa si la plataforma instala un CloudBackend.
    val simulationCloudSync: SimulationCloudSync by lazy { SimulationCloudSync(database) }

    val getUrgentAssignmentsUseCase: GetUrgentAssignmentsUseCase by lazy {
        GetUrgentAssignmentsUseCase()
    }
    
    val calculateWhatIfGradeUseCase: CalculateWhatIfGradeUseCase by lazy {
        CalculateWhatIfGradeUseCase()
    }

    val academicCalculatorUseCase: me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase by lazy {
        me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase()
    }

    val googleDriveApiService: me.joxquin.notivas.data.remote.GoogleDriveApiService by lazy {
        me.joxquin.notivas.data.remote.GoogleDriveApiService(
            httpClient = httpClient,
            preferencesManager = preferencesManager,
            tokenRefresher = { secureTokenVault.refreshGoogleDriveAccessToken() }
        )
    }

    val assignmentReviewUseCase: me.joxquin.notivas.domain.review.AssignmentReviewUseCase by lazy {
        me.joxquin.notivas.domain.review.AssignmentReviewUseCase(
            canvasApiService = canvasApiService,
            openRouterApiService = openRouterApiService,
            preferencesManager = preferencesManager,
            googleDriveApiService = googleDriveApiService
        )
    }
}

