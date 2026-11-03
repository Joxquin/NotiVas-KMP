package me.joxquin.notivas.domain.sync

import me.joxquin.notivas.data.cloud.CloudCourseSnapshot

/**
 * Decide qué cursos subir y cuáles restaurar comparando Room con lo último conocido en la nube.
 * Room es la fuente de verdad; la nube es el respaldo:
 * - Curso con grupos locales distintos a la nube: se sube (gana el dispositivo).
 * - Curso sin grupos locales que nunca tuvo grupos en esta sesión: se restaura desde la nube.
 * - Curso que tuvo grupos en esta sesión y quedó vacío: el usuario los borró, se sube vacío.
 * - Curso que ya no existe en Room (otro ciclo o sesión cerrada): no se toca su respaldo.
 */
object CloudSyncPlanner {

    data class Plan(
        val pushes: List<CloudCourseSnapshot>,
        val restores: List<CloudCourseSnapshot>
    ) {
        val isEmpty: Boolean get() = pushes.isEmpty() && restores.isEmpty()
    }

    fun plan(
        localCourseIds: Set<Long>,
        localSnapshots: Map<Long, CloudCourseSnapshot>,
        coursesWithAssignments: Set<Long>,
        remote: Map<Long, CloudCourseSnapshot>,
        seenLocally: Set<Long>
    ): Plan {
        val pushes = mutableListOf<CloudCourseSnapshot>()
        val restores = mutableListOf<CloudCourseSnapshot>()

        for (courseId in localCourseIds.sorted()) {
            val local = localSnapshots[courseId]?.takeIf { it.groups.isNotEmpty() }
            val cloud = remote[courseId]

            if (local != null) {
                if (local != cloud) pushes += local
                continue
            }
            if (cloud == null || cloud.groups.isEmpty()) continue

            when {
                courseId in seenLocally -> pushes += CloudCourseSnapshot(courseId, emptyList())
                // Si el respaldo apunta a tareas de Canvas, se espera a que el sync de Canvas las traiga.
                cloud.hasCanvasLinks && courseId !in coursesWithAssignments -> Unit
                else -> restores += cloud
            }
        }
        return Plan(pushes, restores)
    }
}
