package me.joxquin.notivas.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Destinos a los que puede enlazar una notificacion externa o interna en NotiVas.
 */
sealed interface NotificationNavTarget {
    data class Assignment(
        val assignmentId: Long,
        val courseId: Long? = null,
        val courseName: String? = null
    ) : NotificationNavTarget

    data class Forum(
        val topicId: Long,
        val courseId: Long? = null
    ) : NotificationNavTarget

    data object RecentActivity : NotificationNavTarget
    data object Schedule : NotificationNavTarget
    data class Course(val courseId: Long) : NotificationNavTarget
}

/**
 * Despachador reactivo global para coordinar navegacion disparada por notificaciones
 * de sistema (Android PendingIntent) o eventos del ciclo de vida.
 */
object NotificationNavHandler {
    private val _pendingTarget = MutableStateFlow<NotificationNavTarget?>(null)
    val pendingTarget: StateFlow<NotificationNavTarget?> = _pendingTarget.asStateFlow()

    fun navigateTo(target: NotificationNavTarget) {
        _pendingTarget.value = target
    }

    fun consumePendingTarget(): NotificationNavTarget? {
        val target = _pendingTarget.value
        _pendingTarget.value = null
        return target
    }
}
