package me.joxquin.notivas.domain.study.fsrs

import me.joxquin.notivas.domain.study.model.CardPhase
import me.joxquin.notivas.domain.study.model.FsrsRating
import me.joxquin.notivas.domain.study.model.FsrsReviewState
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Motor determinista de repetición espaciada FSRS-4.5 (Free Spaced Repetition Scheduler).
 * Portado a Kotlin Multiplatform con paridad numérica matemática.
 */
class FsrsAlgorithm(
    val requestRetention: Double = 0.9,
    val maximumIntervalDays: Int = 365,
    val enableShortTerm: Boolean = true
) {
    companion object {
        const val DECAY = -0.5
        const val FACTOR = 19.0 / 81.0 // 0.9^(1/decay) - 1
        const val MIN_STABILITY = 0.01
        const val MAX_STABILITY = 36500.0
        const val MILLIS_PER_SECOND = 1000L
        const val SECONDS_PER_DAY = 86400.0
        const val MILLIS_PER_DAY = 86400000L

        // Pesos oficiales estándar de FSRS-4.5
        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.4872, 1.4003, 3.7145, 13.8206, 5.1618,
            1.2298, 0.8975, 0.031, 1.6474, 0.1367,
            1.0461, 2.1072, 0.0793, 0.3246, 1.587,
            0.2272, 2.8755
        )
    }

    private val w = DEFAULT_WEIGHTS
    private val intervalModifier: Double = (requestRetention.pow(1.0 / DECAY) - 1.0) / FACTOR
    private val learningStepsSeconds = if (enableShortTerm) intArrayOf(60, 600) else intArrayOf()
    private val relearningStepsSeconds = if (enableShortTerm) intArrayOf(600) else intArrayOf()

    /**
     * Calcula el nuevo estado de la tarjeta tras responder una calificación en el instante `nowEpochMs`.
     */
    fun next(state: FsrsReviewState, rating: FsrsRating, nowEpochMs: Long): FsrsReviewState {
        val s = repairState(state)
        return when (s.phase) {
            CardPhase.NEW -> newCardOutcome(s, rating, nowEpochMs)
            CardPhase.LEARNING -> stepOutcome(s, rating, nowEpochMs, learningStepsSeconds)
            CardPhase.RELEARNING -> stepOutcome(s, rating, nowEpochMs, relearningStepsSeconds)
            CardPhase.REVIEW -> reviewOutcome(s, rating, nowEpochMs)
        }
    }

    /**
     * Probabilidad estimada de recordar la tarjeta en el instante `nowEpochMs` (valor entre 0.0 y 1.0).
     */
    fun retrievability(state: FsrsReviewState, nowEpochMs: Long): Double {
        if (state.phase == CardPhase.NEW || state.stability <= 0.0 || state.lastReviewEpochMs == null) {
            return 1.0
        }
        val elapsedDays = max(0.0, (nowEpochMs - state.lastReviewEpochMs) / (SECONDS_PER_DAY * 1000.0))
        return calculateRetrievability(elapsedDays, state.stability)
    }

    fun calculateRetrievability(elapsedDays: Double, stability: Double): Double {
        if (stability <= 0.0) return 1.0
        return (1.0 + (FACTOR * max(0.0, elapsedDays)) / stability).pow(DECAY)
    }

    private fun initialStability(rating: FsrsRating): Double {
        return max(w[rating.value - 1], 0.1)
    }

    private fun initialDifficulty(rating: FsrsRating): Double {
        val raw = w[4] - (rating.value - 3) * w[5]
        return raw.coerceIn(1.0, 10.0)
    }

    private fun nextDifficulty(d: numberOrDefault, rating: FsrsRating): Double {
        val nextD = d - w[6] * (rating.value - 3)
        val raw = w[7] * w[4] + (1.0 - w[7]) * nextD
        return raw.coerceIn(1.0, 10.0)
    }

    private fun recallStability(d: Double, s: Double, r: Double, rating: FsrsRating): Double {
        val hardPenalty = if (rating == FsrsRating.HARD) w[15] else 1.0
        val easyBonus = if (rating == FsrsRating.EASY) w[16] else 1.0
        val inc = exp(w[8]) * (11.0 - d) * s.pow(-w[9]) * (exp((1.0 - r) * w[10]) - 1.0) * hardPenalty * easyBonus
        val result = s * (1.0 + inc)
        return result.coerceIn(MIN_STABILITY, MAX_STABILITY)
    }

    private fun forgetStability(d: Double, s: Double, r: Double): Double {
        val result = w[11] * d.pow(-w[12]) * ((s + 1.0).pow(w[13]) - 1.0) * exp((1.0 - r) * w[14])
        return result.coerceIn(MIN_STABILITY, MAX_STABILITY)
    }

    private fun intervalDays(stability: Double): Int {
        val raw = stability * intervalModifier
        val days = raw.roundToLong().toInt()
        return days.coerceIn(1, maximumIntervalDays)
    }

    private fun newCardOutcome(st: FsrsReviewState, rating: FsrsRating, nowEpochMs: Long): FsrsReviewState {
        val initS = initialStability(rating)
        val initD = initialDifficulty(rating)

        if (!enableShortTerm || rating == FsrsRating.EASY) {
            val days = intervalDays(initS)
            return st.copy(
                phase = CardPhase.REVIEW,
                dueEpochMs = nowEpochMs + days * MILLIS_PER_DAY,
                stability = initS,
                difficulty = initD,
                reps = st.reps + 1,
                lapses = if (rating == FsrsRating.AGAIN) 1 else 0,
                lastReviewEpochMs = nowEpochMs,
                step = 0
            )
        }

        // Pasos cortos de aprendizaje
        val step = if (rating == FsrsRating.AGAIN) 0 else 1
        val delaySec = if (rating == FsrsRating.AGAIN) {
            learningStepsSeconds.getOrElse(0) { 60 }
        } else {
            learningStepsSeconds.getOrElse(1) { 600 }
        }

        return st.copy(
            phase = CardPhase.LEARNING,
            dueEpochMs = nowEpochMs + delaySec * MILLIS_PER_SECOND,
            stability = initS,
            difficulty = initD,
            reps = st.reps + 1,
            lapses = if (rating == FsrsRating.AGAIN) 1 else 0,
            lastReviewEpochMs = nowEpochMs,
            step = step
        )
    }

    private fun stepOutcome(st: FsrsReviewState, rating: FsrsRating, nowEpochMs: Long, steps: IntArray): FsrsReviewState {
        if (steps.isEmpty()) {
            return reviewOutcome(st, rating, nowEpochMs)
        }

        return when (rating) {
            FsrsRating.AGAIN -> {
                st.copy(
                    phase = st.phase,
                    dueEpochMs = nowEpochMs + steps[0] * MILLIS_PER_SECOND,
                    reps = st.reps + 1,
                    lapses = st.lapses + 1,
                    lastReviewEpochMs = nowEpochMs,
                    step = 0
                )
            }
            FsrsRating.HARD -> {
                val delay = if (steps.size >= 2) (steps[0] + steps[1]) / 2 else (steps[0] * 1.5).toInt()
                st.copy(
                    phase = st.phase,
                    dueEpochMs = nowEpochMs + delay * MILLIS_PER_SECOND,
                    reps = st.reps + 1,
                    lastReviewEpochMs = nowEpochMs
                )
            }
            FsrsRating.GOOD -> {
                val nextStep = st.step + 1
                if (nextStep < steps.size) {
                    st.copy(
                        phase = st.phase,
                        dueEpochMs = nowEpochMs + steps[nextStep] * MILLIS_PER_SECOND,
                        reps = st.reps + 1,
                        lastReviewEpochMs = nowEpochMs,
                        step = nextStep
                    )
                } else {
                    // Gradúa al modo de repaso
                    val days = intervalDays(st.stability)
                    st.copy(
                        phase = CardPhase.REVIEW,
                        dueEpochMs = nowEpochMs + days * MILLIS_PER_DAY,
                        reps = st.reps + 1,
                        lastReviewEpochMs = nowEpochMs,
                        step = 0
                    )
                }
            }
            FsrsRating.EASY -> {
                // Fácil gradúa inmediatamente con bono de estabilidad
                val bonusS = (st.stability * 1.3).coerceIn(MIN_STABILITY, MAX_STABILITY)
                val days = intervalDays(bonusS)
                st.copy(
                    phase = CardPhase.REVIEW,
                    dueEpochMs = nowEpochMs + days * MILLIS_PER_DAY,
                    stability = bonusS,
                    reps = st.reps + 1,
                    lastReviewEpochMs = nowEpochMs,
                    step = 0
                )
            }
        }
    }

    private fun reviewOutcome(st: FsrsReviewState, rating: FsrsRating, nowEpochMs: Long): FsrsReviewState {
        val lastMs = st.lastReviewEpochMs ?: nowEpochMs
        val elapsedDays = max(0.0, (nowEpochMs - lastMs) / (SECONDS_PER_DAY * 1000.0))
        val r = calculateRetrievability(elapsedDays, st.stability)
        val nextD = nextDifficulty(st.difficulty, rating)

        return when (rating) {
            FsrsRating.AGAIN -> {
                val nextS = forgetStability(nextD, st.stability, r)
                if (enableShortTerm && relearningStepsSeconds.isNotEmpty()) {
                    st.copy(
                        phase = CardPhase.RELEARNING,
                        dueEpochMs = nowEpochMs + relearningStepsSeconds[0] * MILLIS_PER_SECOND,
                        stability = nextS,
                        difficulty = nextD,
                        reps = st.reps + 1,
                        lapses = st.lapses + 1,
                        lastReviewEpochMs = nowEpochMs,
                        step = 0
                    )
                } else {
                    val days = 1
                    st.copy(
                        phase = CardPhase.REVIEW,
                        dueEpochMs = nowEpochMs + days * MILLIS_PER_DAY,
                        stability = nextS,
                        difficulty = nextD,
                        reps = st.reps + 1,
                        lapses = st.lapses + 1,
                        lastReviewEpochMs = nowEpochMs,
                        step = 0
                    )
                }
            }
            FsrsRating.HARD, FsrsRating.GOOD, FsrsRating.EASY -> {
                val nextS = recallStability(nextD, st.stability, r, rating)
                val days = intervalDays(nextS)
                st.copy(
                    phase = CardPhase.REVIEW,
                    dueEpochMs = nowEpochMs + days * MILLIS_PER_DAY,
                    stability = nextS,
                    difficulty = nextD,
                    reps = st.reps + 1,
                    lastReviewEpochMs = nowEpochMs,
                    step = 0
                )
            }
        }
    }

    private fun repairState(s: FsrsReviewState): FsrsReviewState {
        val stab = if (s.stability <= 0.0) initialStability(FsrsRating.AGAIN) else s.stability.coerceIn(MIN_STABILITY, MAX_STABILITY)
        val diff = if (s.difficulty <= 0.0) initialDifficulty(FsrsRating.GOOD) else s.difficulty.coerceIn(1.0, 10.0)
        return s.copy(
            stability = stab,
            difficulty = diff,
            reps = max(0, s.reps),
            lapses = max(0, s.lapses)
        )
    }
}

typealias numberOrDefault = Double
