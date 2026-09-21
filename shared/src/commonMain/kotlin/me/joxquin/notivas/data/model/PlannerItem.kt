package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlannerItem(
    @SerialName("plannable_id") val plannableId: Long,
    @SerialName("plannable_type") val plannableType: String,
    @SerialName("plannable_date") val plannableDate: String? = null,
    @SerialName("context_name") val contextName: String? = null,
    @SerialName("course_id") val courseId: Long? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("plannable") val plannable: Plannable
)

@Serializable
data class Plannable(
    @SerialName("title") val title: String,
    @SerialName("id") val id: Long
)
