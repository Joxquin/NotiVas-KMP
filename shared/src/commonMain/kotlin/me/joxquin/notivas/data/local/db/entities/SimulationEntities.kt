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
    indices = [
        Index("course_id")
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
    val weightPercentage: Float
) {
    fun toDomain(): SimulationGroup = SimulationGroup(
        id = id,
        courseId = courseId,
        name = name,
        weightPercentage = weightPercentage
    )

    companion object {
        fun fromDomain(group: SimulationGroup): SimulationGroupEntity = SimulationGroupEntity(
            id = group.id,
            courseId = group.courseId,
            name = group.name,
            weightPercentage = group.weightPercentage
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
        )
    ],
    indices = [
        Index("group_id"),
        Index("canvas_assignment_id")
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
    @ColumnInfo(name = "is_placeholder")
    val isPlaceholder: Boolean = false,
    @ColumnInfo(name = "simulated_score")
    val simulatedScore: Float = 0f,
    @ColumnInfo(name = "max_score")
    val maxScore: Float = 20f
) {
    fun toDomain(): SimulationItem = SimulationItem(
        id = id,
        groupId = groupId,
        canvasAssignmentId = canvasAssignmentId,
        name = name,
        isPlaceholder = isPlaceholder,
        simulatedScore = simulatedScore,
        maxScore = maxScore
    )

    companion object {
        fun fromDomain(item: SimulationItem): SimulationItemEntity = SimulationItemEntity(
            id = item.id,
            groupId = item.groupId,
            canvasAssignmentId = item.canvasAssignmentId,
            name = item.name,
            isPlaceholder = item.isPlaceholder,
            simulatedScore = item.simulatedScore,
            maxScore = item.maxScore
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
        items = items.map { it.toDomain() }
    )
}
