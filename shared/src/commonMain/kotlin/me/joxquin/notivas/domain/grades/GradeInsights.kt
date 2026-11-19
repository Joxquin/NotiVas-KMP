package me.joxquin.notivas.domain.grades

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.domain.usecase.AcademicCalculatorUseCase
import kotlin.math.roundToInt

/** De dónde sale el promedio de un curso. */
enum class AverageSource { SIMULATOR, CANVAS, NONE }

/** Nota de una evaluación ya convertida a escala 0-20. */
data class GradeEntry(val name: String, val score20: Double, val gradedAt: String?)

/** Estado de notas de un curso con las mismas reglas que la pantalla Rendimiento. */
data class CourseGradeSnapshot(
    val course: Course,
    val currentAverage: Double?,
    val averageSource: AverageSource,
    val gradedCount: Int,
    val pendingCount: Int,
    val missingCount: Int,
    /** Con ponderaciones: avance ponderado del simulador. Sin ellas: % de tareas ya calificadas. */
    val evaluatedPercent: Double,
    /** true solo si el simulador tiene grupos con notas reales y por tanto pesos que usar. */
    val weightsConfigured: Boolean,
    val projectedFinal: Double?,
    val recentGrades: List<GradeEntry>
)

/** Resultado de "cuánto necesito": promedio mínimo en lo que falta evaluar para llegar a la meta. */
data class NeededGrade(
    val target: Double,
    val currentAverage: Double?,
    val evaluatedPercent: Double,
    val remainingPercent: Double,
    val neededAverage: Double?,
    val outcome: Outcome
) {
    enum class Outcome { ALREADY_SECURED, REACHABLE, IMPOSSIBLE, CLOSED_PASSED, CLOSED_FAILED }
}

object GradeInsights {

    const val MAX_SCORE = 20.0

    /** Nota de una tarea en escala 20, o null si no está calificada o no tiene puntaje máximo. */
    fun score20(assignment: Assignment): Double? {
        val score = assignment.score ?: return null
        val points = assignment.pointsPossible ?: return null
        return if (points > 0.0) score / points * MAX_SCORE else null
    }

    /** Promedio simple de las tareas calificadas en Canvas (el mismo que usa Rendimiento sin simulador). */
    fun canvasAverage(assignments: List<Assignment>): Double? =
        assignments.mapNotNull(::score20).takeIf { it.isNotEmpty() }?.average()

    fun snapshot(
        course: Course,
        courseAssignments: List<Assignment>,
        courseGroups: List<SimulationGroupWithItems>,
        calculator: AcademicCalculatorUseCase
    ): CourseGradeSnapshot {
        val graded = courseAssignments.mapNotNull { a -> score20(a)?.let { a to it } }
        val pending = courseAssignments.count { it.status == "upcoming" }
        val missing = courseAssignments.count { it.status == "missing" }
        val canvasAvg = canvasAverage(courseAssignments)
        val recent = graded
            .sortedByDescending { (a, _) -> a.gradedAt ?: a.dueAt ?: "" }
            .take(5)
            .map { (a, s) -> GradeEntry(a.name, round1(s), a.gradedAt) }
        val countedTotal = graded.size + pending + missing
        val countBasedProgress = if (countedTotal > 0) graded.size * 100.0 / countedTotal else 0.0

        val summary = if (courseGroups.isNotEmpty()) calculator.calculateCourseSummary(course, courseGroups) else null
        val simulatorAverage = summary?.currentAverage?.toDouble()

        // Grupos sin notas reales (por ejemplo, plantillas o datos de prueba) no dan un promedio:
        // en ese caso se usa el de Canvas en vez de mostrar el curso vacío.
        return if (summary != null && simulatorAverage != null) {
            CourseGradeSnapshot(
                course = course,
                currentAverage = simulatorAverage,
                averageSource = AverageSource.SIMULATOR,
                gradedCount = graded.size,
                pendingCount = pending,
                missingCount = missing,
                evaluatedPercent = summary.evaluatedProgressPercentage.toDouble(),
                weightsConfigured = true,
                projectedFinal = summary.projectedFinalGrade.toDouble(),
                recentGrades = recent
            )
        } else {
            CourseGradeSnapshot(
                course = course,
                currentAverage = canvasAvg,
                averageSource = if (canvasAvg != null) AverageSource.CANVAS else AverageSource.NONE,
                gradedCount = graded.size,
                pendingCount = pending,
                missingCount = missing,
                evaluatedPercent = countBasedProgress,
                weightsConfigured = false,
                projectedFinal = null,
                recentGrades = recent
            )
        }
    }

    /** Promedio general como en Inicio: media de los cursos que ya tienen promedio. */
    fun overallAverage(snapshots: List<CourseGradeSnapshot>): Double? =
        snapshots.mapNotNull { it.currentAverage }.filter { it > 0.0 }.takeIf { it.isNotEmpty() }?.average()

    /**
     * Promedio mínimo que hace falta en lo que queda por evaluar para terminar con [target]:
     * final = (actual * evaluado + X * restante) / 100  =>  X = (100 * meta - actual * evaluado) / restante.
     */
    fun neededAverage(currentAverage: Double?, evaluatedPercent: Double, target: Double): NeededGrade {
        val evaluated = if (currentAverage == null) 0.0 else evaluatedPercent.coerceIn(0.0, 100.0)
        val remaining = 100.0 - evaluated
        if (remaining < 0.5) {
            val avg = currentAverage ?: 0.0
            return NeededGrade(
                target, currentAverage, evaluated, 0.0, null,
                if (avg >= target) NeededGrade.Outcome.CLOSED_PASSED else NeededGrade.Outcome.CLOSED_FAILED
            )
        }
        val raw = (100.0 * target - (currentAverage ?: 0.0) * evaluated) / remaining
        val outcome = when {
            raw <= 0.0 -> NeededGrade.Outcome.ALREADY_SECURED
            raw > MAX_SCORE -> NeededGrade.Outcome.IMPOSSIBLE
            else -> NeededGrade.Outcome.REACHABLE
        }
        return NeededGrade(
            target = target,
            currentAverage = currentAverage,
            evaluatedPercent = round1(evaluated),
            remainingPercent = round1(remaining),
            neededAverage = round1(raw.coerceAtLeast(0.0)),
            outcome = outcome
        )
    }

    fun round1(value: Double): Double = (value * 10.0).roundToInt() / 10.0
}
