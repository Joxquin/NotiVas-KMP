package me.joxquin.notivas.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.joxquin.notivas.data.model.Course

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "course_code")
    val courseCode: String? = null,
    @ColumnInfo(name = "enrollment_term_id")
    val enrollmentTermId: Long? = null
) {
    fun toDomain(): Course = Course(
        id = id,
        name = name,
        courseCode = courseCode,
        enrollmentTermId = enrollmentTermId
    )

    companion object {
        fun fromDomain(course: Course): CourseEntity = CourseEntity(
            id = course.id,
            name = course.name,
            courseCode = course.courseCode,
            enrollmentTermId = course.enrollmentTermId
        )
    }
}
