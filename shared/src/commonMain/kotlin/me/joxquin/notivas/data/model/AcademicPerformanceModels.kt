package me.joxquin.notivas.data.model

import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateComponents

/**
 * Estados de entrega para los bloques de rendimiento del calendario.
 */
enum class DayTaskStatus {
    NONE,        // Sin tareas programadas ese día
    COMPLETED,   // Verde o Monet primario: todas las tareas completadas
    OVERDUE,     // Rojo: al menos una tarea no se completó o venció
    PENDING      // Gris: tareas pendientes por entregar dentro del plazo
}

/**
 * Representa un bloque de un día específico en el resumen semanal o mensual.
 */
data class DayBlockInfo(
    val date: DateComponents,
    val dayLetter: String, // "L", "M", "X", "J", "V", "S", "D"
    val dayNumber: Int,
    val status: DayTaskStatus,
    val tasks: List<AssignmentUiModel> = emptyList(),
    val isToday: Boolean = false,
    val isCurrentMonth: Boolean = true
)

/**
 * Estadísticas consolidadas de rendimiento académico global.
 */
data class AcademicPerformanceStats(
    val overallAverage: Double,
    val totalAssignments: Int,
    val completedAssignments: Int,
    val pendingAssignments: Int,
    val missingAssignments: Int,
    val completionRatePercentage: Float,
    val highestScore: Double? = null,
    val lowestScore: Double? = null,
    val performanceLevel: GradePerformanceLevel = GradePerformanceLevel.AT_RISK
)
