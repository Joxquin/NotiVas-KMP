package me.joxquin.notivas.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CourseUtilTest {

    @Test
    fun testCleanCourseNameStripsSectionSuffixes() {
        val raw = "Desarrollo de Aplicaciones Empresariales - C24 4to C-A - C24 4to D-A-A"
        val cleaned = CourseUtil.cleanCourseName(raw)
        assertEquals("Desarrollo de Aplicaciones Empresariales", cleaned)

        val raw2 = "Tecnologías Emergentes - C24 4to C-A - C24 4to D-A-A"
        val cleaned2 = CourseUtil.cleanCourseName(raw2)
        assertEquals("Tecnologías Emergentes", cleaned2)
    }

    @Test
    fun testExtractCourseTagGeneratesDynamicAcronyms() {
        // Desarrollo de Aplicaciones Empresariales -> DAE
        val tagDae = CourseUtil.extractCourseTag(
            courseCode = null,
            courseName = "Desarrollo de Aplicaciones Empresariales - C24 4to C-A - C24 4to D-A-A"
        )
        assertEquals("DAE", tagDae)

        // Tecnologías Emergentes -> TE
        val tagTe = CourseUtil.extractCourseTag(
            courseCode = null,
            courseName = "Tecnologías Emergentes - C24 4to C-A - C24 4to D-A-A"
        )
        assertEquals("TE", tagTe)

        // Programación Móvil -> PM
        val tagPm = CourseUtil.extractCourseTag(
            courseCode = null,
            courseName = "Programación Móvil - C24 4to C-A"
        )
        assertEquals("PM", tagPm)
    }

    @Test
    fun testExtractCourseTagPrefersValidShortCourseCode() {
        val tag = CourseUtil.extractCourseTag(
            courseCode = "C24-DAE",
            courseName = "Desarrollo de Aplicaciones Empresariales"
        )
        assertEquals("C24-DAE", tag)
    }

    @Test
    fun testExtractCourseTagShortNamePreserved() {
        val tag = CourseUtil.extractCourseTag(
            courseCode = null,
            courseName = "Física II"
        )
        assertEquals("Física II", tag)
    }

    @Test
    fun testExtractCourseTagNeverReturnsHardcodedProgMovForOtherCourses() {
        val tag = CourseUtil.extractCourseTag(
            courseCode = null,
            courseName = "Tecnologías Emergentes"
        )
        kotlin.test.assertNotEquals("ProgMov", tag)
    }
}
