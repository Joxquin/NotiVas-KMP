package me.joxquin.notivas.ui.notas

import kotlin.test.Test
import kotlin.test.assertEquals

class RiskLevelTest {

    @Test
    fun courseWithoutGradesIsNotFlaggedAsFailing() {
        // Antes un curso sin notas tenía promedio 0 y salía "Riesgo de desaprobación".
        assertEquals(RiskLevel.NoData, RiskLevel.from(hasGrades = false, currentAverage = 0f))
    }

    @Test
    fun gradedCoursesUseTheAverageThresholds() {
        assertEquals(RiskLevel.Safe, RiskLevel.from(hasGrades = true, currentAverage = 17.2f))
        assertEquals(RiskLevel.ModerateRisk, RiskLevel.from(hasGrades = true, currentAverage = 12f))
        assertEquals(RiskLevel.HighRisk, RiskLevel.from(hasGrades = true, currentAverage = 8f))
        assertEquals(RiskLevel.HighRisk, RiskLevel.from(hasGrades = true, currentAverage = 0f))
    }
}
