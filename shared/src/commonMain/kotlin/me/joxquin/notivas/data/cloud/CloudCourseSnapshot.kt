package me.joxquin.notivas.data.cloud

import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem

/**
 * Copia en la nube de las ponderaciones y notas predictivas de un curso de Canvas.
 * No guarda los ids locales de Room (son autogenerados y cambian entre dispositivos),
 * por eso dos snapshots con el mismo contenido son iguales aunque vengan de equipos distintos.
 */
data class CloudCourseSnapshot(
    val courseId: Long,
    val groups: List<CloudGroup>
) {
    val hasCanvasLinks: Boolean
        get() = groups.any { group -> group.items.any { it.canvasAssignmentId != null } }

    /** Documento para Firestore: solo tipos primitivos, listas y mapas. */
    fun toMap(courseName: String?): Map<String, Any?> = mapOf(
        FIELD_SCHEMA to SCHEMA_VERSION,
        FIELD_COURSE_ID to courseId,
        FIELD_COURSE_NAME to courseName,
        FIELD_GROUPS to groups.map { it.toMap() }
    )

    /**
     * Convierte el snapshot en grupos listos para insertar en Room.
     * Los vínculos con tareas de Canvas que no existen en el dispositivo se descartan
     * para no violar la llave foránea de `simulation_items.canvas_assignment_id`.
     */
    fun toLocalGroups(existingAssignmentIds: Set<Long>): List<SimulationGroupWithItems> =
        groups.map { group ->
            SimulationGroupWithItems(
                group = SimulationGroup(
                    courseId = courseId,
                    name = group.name,
                    weightPercentage = group.weightPercentage,
                    targetAssessments = group.targetAssessments,
                    dropLowest = group.dropLowest,
                    minToDrop = group.minToDrop,
                    calculationMode = group.calculationMode,
                    orderIndex = group.orderIndex
                ),
                items = group.items.map { item ->
                    SimulationItem(
                        groupId = 0L,
                        canvasAssignmentId = item.canvasAssignmentId?.takeIf { it in existingAssignmentIds },
                        name = item.name,
                        isPlaceholder = item.isPlaceholder,
                        weekNumber = item.weekNumber,
                        manualScore = item.manualScore,
                        simulatedScore = item.simulatedScore,
                        isSimulated = item.isSimulated,
                        maxScore = item.maxScore,
                        internalWeight = item.internalWeight,
                        orderIndex = item.orderIndex
                    )
                }
            )
        }

    companion object {
        const val SCHEMA_VERSION = 1L
        const val FIELD_SCHEMA = "schemaVersion"
        const val FIELD_COURSE_ID = "courseId"
        const val FIELD_COURSE_NAME = "courseName"
        const val FIELD_GROUPS = "groups"

        fun fromLocal(courseId: Long, groups: List<SimulationGroupWithItems>): CloudCourseSnapshot =
            CloudCourseSnapshot(
                courseId = courseId,
                groups = groups
                    .map { CloudGroup.fromLocal(it) }
                    .sortedWith(compareBy({ it.orderIndex }, { it.name }, { it.weightPercentage }))
            )

        /** Lee un documento de Firestore. Devuelve null si el formato no es reconocible. */
        fun fromMap(data: Map<String, Any?>): CloudCourseSnapshot? {
            val courseId = data[FIELD_COURSE_ID].asLong() ?: return null
            val rawGroups = data[FIELD_GROUPS] as? List<*> ?: return null
            val groups = rawGroups.map { raw ->
                CloudGroup.fromMap(raw as? Map<*, *> ?: return null) ?: return null
            }
            return CloudCourseSnapshot(courseId, groups)
        }
    }
}

data class CloudGroup(
    val name: String,
    val weightPercentage: Float,
    val targetAssessments: Int,
    val dropLowest: Boolean,
    val minToDrop: Int,
    val calculationMode: String,
    val orderIndex: Int,
    val items: List<CloudItem>
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "weightPercentage" to weightPercentage.toCloudDouble(),
        "targetAssessments" to targetAssessments.toLong(),
        "dropLowest" to dropLowest,
        "minToDrop" to minToDrop.toLong(),
        "calculationMode" to calculationMode,
        "orderIndex" to orderIndex.toLong(),
        "items" to items.map { it.toMap() }
    )

    companion object {
        fun fromLocal(local: SimulationGroupWithItems): CloudGroup = CloudGroup(
            name = local.group.name,
            weightPercentage = local.group.weightPercentage,
            targetAssessments = local.group.targetAssessments,
            dropLowest = local.group.dropLowest,
            minToDrop = local.group.minToDrop,
            calculationMode = local.group.calculationMode,
            orderIndex = local.group.orderIndex,
            items = local.items
                .map { CloudItem.fromLocal(it) }
                .sortedWith(compareBy({ it.orderIndex }, { it.name }, { it.weekNumber }))
        )

        fun fromMap(data: Map<*, *>): CloudGroup? {
            val rawItems = data["items"] as? List<*> ?: emptyList<Any?>()
            return CloudGroup(
                name = data["name"] as? String ?: return null,
                weightPercentage = data["weightPercentage"].asFloat() ?: return null,
                targetAssessments = data["targetAssessments"].asInt() ?: 1,
                dropLowest = data["dropLowest"] as? Boolean ?: false,
                minToDrop = data["minToDrop"].asInt() ?: 3,
                calculationMode = data["calculationMode"] as? String ?: "SIMPLE",
                orderIndex = data["orderIndex"].asInt() ?: 0,
                items = rawItems.map { raw -> CloudItem.fromMap(raw as? Map<*, *> ?: return null) ?: return null }
            )
        }
    }
}

data class CloudItem(
    val name: String,
    val canvasAssignmentId: Long?,
    val isPlaceholder: Boolean,
    val weekNumber: Int?,
    val manualScore: Float?,
    val simulatedScore: Float,
    val isSimulated: Boolean,
    val maxScore: Float,
    val internalWeight: Float,
    val orderIndex: Int
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "canvasAssignmentId" to canvasAssignmentId,
        "isPlaceholder" to isPlaceholder,
        "weekNumber" to weekNumber?.toLong(),
        "manualScore" to manualScore?.toCloudDouble(),
        "simulatedScore" to simulatedScore.toCloudDouble(),
        "isSimulated" to isSimulated,
        "maxScore" to maxScore.toCloudDouble(),
        "internalWeight" to internalWeight.toCloudDouble(),
        "orderIndex" to orderIndex.toLong()
    )

    companion object {
        fun fromLocal(item: SimulationItem): CloudItem = CloudItem(
            name = item.name,
            canvasAssignmentId = item.canvasAssignmentId,
            isPlaceholder = item.isPlaceholder,
            weekNumber = item.weekNumber,
            manualScore = item.manualScore,
            simulatedScore = item.simulatedScore,
            isSimulated = item.isSimulated,
            maxScore = item.maxScore,
            internalWeight = item.internalWeight,
            orderIndex = item.orderIndex
        )

        fun fromMap(data: Map<*, *>): CloudItem? = CloudItem(
            name = data["name"] as? String ?: return null,
            canvasAssignmentId = data["canvasAssignmentId"].asLong(),
            isPlaceholder = data["isPlaceholder"] as? Boolean ?: false,
            weekNumber = data["weekNumber"].asInt(),
            manualScore = data["manualScore"].asFloat(),
            simulatedScore = data["simulatedScore"].asFloat() ?: 0f,
            isSimulated = data["isSimulated"] as? Boolean ?: false,
            maxScore = data["maxScore"].asFloat() ?: 20f,
            internalWeight = data["internalWeight"].asFloat() ?: 1.0f,
            orderIndex = data["orderIndex"].asInt() ?: 0
        )
    }
}

// Firestore devuelve los enteros como Long y los decimales como Double.
private fun Any?.asLong(): Long? = (this as? Number)?.toLong()
private fun Any?.asInt(): Int? = (this as? Number)?.toInt()
private fun Any?.asFloat(): Float? = (this as? Number)?.toFloat()

// Pasar por texto evita que 0.1f se guarde como 0.10000000149011612 en la consola.
private fun Float.toCloudDouble(): Double = toString().toDouble()
