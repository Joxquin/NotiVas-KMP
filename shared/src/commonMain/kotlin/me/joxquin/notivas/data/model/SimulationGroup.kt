package me.joxquin.notivas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SimulationGroup(
    val id: Long = 0,
    val courseId: Long,
    val name: String,
    val weightPercentage: Float,
    val targetAssessments: Int = 1,
    val dropLowest: Boolean = false,
    val minToDrop: Int = 3,
    val calculationMode: String = "SIMPLE", // "SIMPLE", "DROP_LOWEST", "PAIRED"
    val orderIndex: Int = 0
)

@Serializable
data class SimulationItem(
    val id: Long = 0,
    val groupId: Long,
    val canvasAssignmentId: Long? = null,
    val name: String,
    val isPlaceholder: Boolean = false,
    val weekNumber: Int? = null,
    val manualScore: Float? = null,
    val simulatedScore: Float = 0f, // 0.0 to 20.0
    val isSimulated: Boolean = false,
    val maxScore: Float = 20f,
    val internalWeight: Float = 1.0f,
    val orderIndex: Int = 0
) {
    /**
     * Devuelve la nota efectiva a considerar en el cálculo (la nota simulada si está en modo simulación,
     * o la nota manual/real si está registrada).
     */
    val effectiveScore: Float
        get() = if (isSimulated) simulatedScore else (manualScore ?: simulatedScore)
}

@Serializable
data class SimulationGroupWithItems(
    val group: SimulationGroup,
    val items: List<SimulationItem>
)
