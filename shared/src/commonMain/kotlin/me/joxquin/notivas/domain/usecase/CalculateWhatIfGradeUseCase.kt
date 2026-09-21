package me.joxquin.notivas.domain.usecase

import me.joxquin.notivas.data.model.Assignment

class CalculateWhatIfGradeUseCase {
    operator fun invoke(
        assignments: List<Assignment>,
        simulatedScores: Map<Long, Double>
    ): Double {
        var totalPoints = 0.0
        var earnedPoints = 0.0

        for (assignment in assignments) {
            val possible = assignment.pointsPossible ?: continue
            if (possible <= 0) continue

            val score = simulatedScores[assignment.id] 
                ?: assignment.score 
                ?: assignment.submission?.score 
                ?: 0.0

            totalPoints += possible
            earnedPoints += score
        }

        return if (totalPoints > 0) (earnedPoints / totalPoints) * 100.0 else 0.0
    }
}
