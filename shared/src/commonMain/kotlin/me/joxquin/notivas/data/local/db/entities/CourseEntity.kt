package me.joxquin.notivas.data.local.db.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
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
    val enrollmentTermId: Long? = null,
    @ColumnInfo(name = "total_weeks", defaultValue = "18")
    val totalWeeks: Int = 18,
    @ColumnInfo(name = "passing_grade", defaultValue = "12.0")
    val passingGrade: Float = 12.0f,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null
) {
    fun toDomain(): Course = Course(
        id = id,
        name = name,
        courseCode = courseCode,
        enrollmentTermId = enrollmentTermId,
        imageUrl = imageUrl,
        totalWeeks = totalWeeks,
        passingGrade = passingGrade
    )

    companion object {
        fun fromDomain(course: Course): CourseEntity = CourseEntity(
            id = course.id,
            name = course.name,
            courseCode = course.courseCode,
            enrollmentTermId = course.enrollmentTermId,
            imageUrl = course.imageUrl,
            totalWeeks = course.totalWeeks,
            passingGrade = course.passingGrade
        )
    }
}
