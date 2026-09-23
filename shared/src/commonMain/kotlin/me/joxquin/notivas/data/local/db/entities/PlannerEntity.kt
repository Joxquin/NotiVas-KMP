package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import me.joxquin.notivas.data.model.Plannable
import me.joxquin.notivas.data.model.PlannerItem

@Entity(
    tableName = "planner_items",
    indices = [
        Index("plannable_date"),
        Index("course_id")
    ]
)
data class PlannerItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "plannable_id")
    val plannableId: Long,
    @ColumnInfo(name = "plannable_type")
    val plannableType: String,
    @ColumnInfo(name = "plannable_date")
    val plannableDate: String? = null,
    @ColumnInfo(name = "context_name")
    val contextName: String? = null,
    @ColumnInfo(name = "course_id")
    val courseId: Long? = null,
    @ColumnInfo(name = "html_url")
    val htmlUrl: String? = null,
    @ColumnInfo(name = "plannable_title")
    val plannableTitle: String,
    @ColumnInfo(name = "plannable_sub_id")
    val plannableSubId: Long
) {
    fun toDomain(): PlannerItem = PlannerItem(
        plannableId = plannableId,
        plannableType = plannableType,
        plannableDate = plannableDate,
        contextName = contextName,
        courseId = courseId,
        htmlUrl = htmlUrl,
        plannable = Plannable(
            title = plannableTitle,
            id = plannableSubId
        )
    )

    companion object {
        fun fromDomain(item: PlannerItem): PlannerItemEntity = PlannerItemEntity(
            plannableId = item.plannableId,
            plannableType = item.plannableType,
            plannableDate = item.plannableDate,
            contextName = item.contextName,
            courseId = item.courseId,
            htmlUrl = item.htmlUrl,
            plannableTitle = item.plannable.title,
            plannableSubId = item.plannable.id
        )
    }
}
