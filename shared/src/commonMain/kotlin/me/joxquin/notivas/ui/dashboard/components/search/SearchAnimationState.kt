package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.runtime.Stable

@Stable
enum class SearchAnimStatus {
    COLLAPSED,
    EXPANDING,
    EXPANDED,
    COLLAPSING;

    fun isExpand(): Boolean = this == EXPANDED
    fun isCollapsed(): Boolean = this == COLLAPSED
    fun shouldExpand(): Boolean = this == EXPANDED || this == EXPANDING
    fun shouldCollapse(): Boolean = this == COLLAPSED || this == COLLAPSING
    fun isAnimating(): Boolean = this == EXPANDING || this == COLLAPSING
}
