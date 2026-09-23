package me.joxquin.notivas.domain.template

import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem

object TemplateAssignmentMatcher {

    /**
     * Convierte una plantilla de evaluación a grupos e items reales para un curso,
     * y auto-vincula de forma inteligente las tareas de Canvas existentes por regex.
     */
    fun instantiateTemplate(
        course: Course,
        template: CourseEvaluationTemplate,
        canvasAssignments: List<Assignment>
    ): Pair<Course, List<SimulationGroupWithItems>> {
        val updatedCourse = course.copy(
            totalWeeks = template.totalWeeks,
            passingGrade = template.passingGrade
        )

        val matchedAssignmentIds = mutableSetOf<Long>()

        val groupsWithItems = template.groups.mapIndexed { groupIdx, groupTmpl ->
            val group = SimulationGroup(
                id = 0L,
                courseId = course.id,
                name = groupTmpl.name,
                weightPercentage = groupTmpl.weightPercentage,
                targetAssessments = groupTmpl.targetAssessments,
                dropLowest = groupTmpl.dropLowest,
                minToDrop = groupTmpl.minToDrop,
                calculationMode = groupTmpl.calculationMode,
                orderIndex = groupIdx
            )

            val items = groupTmpl.items.mapIndexed { itemIdx, itemTmpl ->
                // Buscar coincidencia en assignments de Canvas no reclamadas previamente
                val matchedAssignment = canvasAssignments.firstOrNull { assignment ->
                    if (assignment.id in matchedAssignmentIds) return@firstOrNull false
                    val assignName = assignment.name
                    itemTmpl.matchingPatterns.any { pattern ->
                        try {
                            Regex(pattern).containsMatchIn(assignName)
                        } catch (_: Exception) {
                            assignName.contains(itemTmpl.name, ignoreCase = true)
                        }
                    }
                }

                if (matchedAssignment != null) {
                    matchedAssignmentIds.add(matchedAssignment.id)
                }

                // Regla de Precedencia: Canvas score oficial si existe, o null
                val canvasScore = matchedAssignment?.score?.toFloat()
                    ?: matchedAssignment?.submission?.score?.toFloat()

                SimulationItem(
                    id = 0L,
                    groupId = 0L,
                    canvasAssignmentId = matchedAssignment?.id,
                    name = itemTmpl.name,
                    isPlaceholder = matchedAssignment == null,
                    weekNumber = itemTmpl.weekNumber,
                    manualScore = canvasScore,
                    simulatedScore = canvasScore ?: 0f,
                    isSimulated = false,
                    maxScore = itemTmpl.maxScore,
                    internalWeight = itemTmpl.internalWeight,
                    orderIndex = itemIdx
                )
            }

            SimulationGroupWithItems(
                group = group,
                items = items
            )
        }

        return Pair(updatedCourse, groupsWithItems)
    }

    /**
     * Auto-vincula tareas de Canvas a items de simulación existentes respetando notas manuales previas.
     */
    fun autoMatchExistingItems(
        currentItems: List<SimulationItem>,
        canvasAssignments: List<Assignment>
    ): List<SimulationItem> {
        val assignedIds = currentItems.mapNotNull { it.canvasAssignmentId }.toMutableSet()

        return currentItems.map { item ->
            if (item.canvasAssignmentId != null) {
                // Ya vinculado: actualizar nota de Canvas si está disponible sin borrar manualScore
                val matched = canvasAssignments.find { it.id == item.canvasAssignmentId }
                val canvasScore = matched?.score?.toFloat() ?: matched?.submission?.score?.toFloat()
                if (canvasScore != null) {
                    item.copy(manualScore = canvasScore, isPlaceholder = false)
                } else {
                    item
                }
            } else {
                // Intentar encontrar tarea por nombre
                val matched = canvasAssignments.firstOrNull { a ->
                    a.id !in assignedIds && a.name.contains(item.name, ignoreCase = true)
                }
                if (matched != null) {
                    assignedIds.add(matched.id)
                    val canvasScore = matched.score?.toFloat() ?: matched.submission?.score?.toFloat()
                    item.copy(
                        canvasAssignmentId = matched.id,
                        manualScore = canvasScore ?: item.manualScore,
                        isPlaceholder = false
                    )
                } else {
                    item
                }
            }
        }
    }
}
