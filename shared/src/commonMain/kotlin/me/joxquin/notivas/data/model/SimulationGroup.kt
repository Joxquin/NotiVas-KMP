package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SimulationGroup(
    val id: Long = 0,
    val courseId: Long,
    val name: String,
    val weightPercentage: Float
)

@Serializable
data class SimulationItem(
    val id: Long = 0,
    val groupId: Long,
    val canvasAssignmentId: Long? = null,
    val name: String,
    val isPlaceholder: Boolean = false,
    val simulatedScore: Float = 0f, // 0.0 to 20.0
    val maxScore: Float = 20f
)

@Serializable
data class SimulationGroupWithItems(
    val group: SimulationGroup,
    val items: List<SimulationItem>
)
