package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("course_code") val courseCode: String? = null,
    @SerialName("enrollment_term_id") val enrollmentTermId: Long? = null
)
