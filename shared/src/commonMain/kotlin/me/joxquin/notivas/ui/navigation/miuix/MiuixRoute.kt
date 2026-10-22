package me.joxquin.notivas.ui.navigation.miuix

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

/**
 * Rutas tipadas seguras para miuix-nav.
 * Cada destino es un NavKey serializable compatible con guardado y restauración de estado en el back stack.
 */
@Serializable
sealed interface MiuixRoute : NavKey {

    @Serializable
    data object Main : MiuixRoute

    @Serializable
    data object RecentActivity : MiuixRoute

    @Serializable
    data object Schedule : MiuixRoute

    @Serializable
    data object MorningRoadmap : MiuixRoute

    @Serializable
    data class AssignmentDraft(
        val assignmentId: Long,
        val courseName: String
    ) : MiuixRoute

    @Serializable
    data class CourseProgreso(val courseId: Long) : MiuixRoute

    @Serializable
    data object ForosHub : MiuixRoute

    @Serializable
    data class ForoDetail(val topicId: Long) : MiuixRoute

    @Serializable
    data object TaskHistoryArchive : MiuixRoute
}
