package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationGroupWithItems
import me.joxquin.notivas.data.model.SimulationItem

@Entity(
    tableName = "simulation_groups",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("course_id"),
        Index("order_index")
    ]
)
data class SimulationGroupEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "weight_percentage")
    val weightPercentage: Float,
    @ColumnInfo(name = "target_assessments", defaultValue = "1")
    val targetAssessments: Int = 1,
    @ColumnInfo(name = "drop_lowest", defaultValue = "0")
    val dropLowest: Boolean = false,
    @ColumnInfo(name = "min_to_drop", defaultValue = "3")
    val minToDrop: Int = 3,
    @ColumnInfo(name = "calculation_mode", defaultValue = "'SIMPLE'")
    val calculationMode: String = "SIMPLE",
    @ColumnInfo(name = "order_index", defaultValue = "0")
    val orderIndex: Int = 0
) {
    fun toDomain(): SimulationGroup = SimulationGroup(
        id = id,
        courseId = courseId,
        name = name,
        weightPercentage = weightPercentage,
        targetAssessments = targetAssessments,
        dropLowest = dropLowest,
        minToDrop = minToDrop,
        calculationMode = calculationMode,
        orderIndex = orderIndex
    )

    companion object {
        fun fromDomain(group: SimulationGroup): SimulationGroupEntity = SimulationGroupEntity(
            id = group.id,
            courseId = group.courseId,
            name = group.name,
            weightPercentage = group.weightPercentage,
            targetAssessments = group.targetAssessments,
            dropLowest = group.dropLowest,
            minToDrop = group.minToDrop,
            calculationMode = group.calculationMode,
            orderIndex = group.orderIndex
        )
    }
}

@Entity(
    tableName = "simulation_items",
    foreignKeys = [
        ForeignKey(
            entity = SimulationGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["canvas_assignment_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("group_id"),
        Index("canvas_assignment_id"),
        Index("week_number"),
        Index("order_index")
    ]
)
data class SimulationItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "group_id")
    val groupId: Long,
    @ColumnInfo(name = "canvas_assignment_id")
    val canvasAssignmentId: Long? = null,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "is_placeholder", defaultValue = "0")
    val isPlaceholder: Boolean = false,
    @ColumnInfo(name = "week_number")
    val weekNumber: Int? = null,
    @ColumnInfo(name = "manual_score")
    val manualScore: Float? = null,
    @ColumnInfo(name = "simulated_score", defaultValue = "0.0")
    val simulatedScore: Float = 0f,
    @ColumnInfo(name = "is_simulated", defaultValue = "0")
    val isSimulated: Boolean = false,
    @ColumnInfo(name = "max_score", defaultValue = "20.0")
    val maxScore: Float = 20f,
    @ColumnInfo(name = "internal_weight", defaultValue = "1.0")
    val internalWeight: Float = 1.0f,
    @ColumnInfo(name = "order_index", defaultValue = "0")
    val orderIndex: Int = 0
) {
    fun toDomain(): SimulationItem = SimulationItem(
        id = id,
        groupId = groupId,
        canvasAssignmentId = canvasAssignmentId,
        name = name,
        isPlaceholder = isPlaceholder,
        weekNumber = weekNumber,
        manualScore = manualScore,
        simulatedScore = simulatedScore,
        isSimulated = isSimulated,
        maxScore = maxScore,
        internalWeight = internalWeight,
        orderIndex = orderIndex
    )

    companion object {
        fun fromDomain(item: SimulationItem): SimulationItemEntity = SimulationItemEntity(
            id = item.id,
            groupId = item.groupId,
            canvasAssignmentId = item.canvasAssignmentId,
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
}

data class SimulationGroupWithItemsRelation(
    @Embedded val group: SimulationGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "group_id"
    )
    val items: List<SimulationItemEntity>
) {
    fun toDomain(): SimulationGroupWithItems = SimulationGroupWithItems(
        group = group.toDomain(),
        items = items.sortedBy { it.orderIndex }.map { it.toDomain() }
    )
}
