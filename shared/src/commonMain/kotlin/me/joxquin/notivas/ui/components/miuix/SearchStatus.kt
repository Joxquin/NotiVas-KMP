package me.joxquin.notivas.ui.components.miuix

import androidx.compose.runtime.Immutable

/**
 * Representa el estado finito del buscador MIUIX, siguiendo la arquitectura
 * de referencia de compose-miuix-ui (miuix/example).
 */
@Immutable
data class SearchStatus(
    val label: String = "",
    val searchText: String = "",
    val current: Status = Status.COLLAPSED,
    val resultStatus: ResultStatus = ResultStatus.DEFAULT,
) {
    fun isExpand() = current == Status.EXPANDED
    fun isCollapsed() = current == Status.COLLAPSED
    fun shouldExpand() = current == Status.EXPANDED || current == Status.EXPANDING
    fun shouldCollapsed() = current == Status.COLLAPSED || current == Status.COLLAPSING
    fun isAnimatingExpand() = current == Status.EXPANDING

    fun onAnimationComplete(): SearchStatus = when (current) {
        Status.EXPANDING -> copy(current = Status.EXPANDED)
        Status.COLLAPSING -> copy(searchText = "", current = Status.COLLAPSED)
        else -> this
    }

    enum class Status {
        EXPANDED,
        EXPANDING,
        COLLAPSED,
        COLLAPSING,
    }

    enum class ResultStatus {
        DEFAULT,
        EMPTY,
        LOAD,
        SHOW,
    }
}
