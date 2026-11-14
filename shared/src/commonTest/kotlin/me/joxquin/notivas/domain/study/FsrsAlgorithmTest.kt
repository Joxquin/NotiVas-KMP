package me.joxquin.notivas.domain.study

import me.joxquin.notivas.domain.study.fsrs.FsrsAlgorithm
import me.joxquin.notivas.domain.study.model.CardPhase
import me.joxquin.notivas.domain.study.model.FsrsRating
import me.joxquin.notivas.domain.study.model.FsrsReviewState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FsrsAlgorithmTest {

    @Test
    fun testNewCardTransitions() {
        val fsrs = FsrsAlgorithm(enableShortTerm = true)
        val initial = FsrsReviewState(phase = CardPhase.NEW)
        val now = 1700000000000L

        // Easy on new card should graduate straight to Review
        val nextEasy = fsrs.next(initial, FsrsRating.EASY, now)
        assertEquals(CardPhase.REVIEW, nextEasy.phase)
        assertTrue(nextEasy.dueEpochMs > now)
        assertTrue(nextEasy.stability > 10.0)

        // Again on new card enters Learning phase at step 0
        val nextAgain = fsrs.next(initial, FsrsRating.AGAIN, now)
        assertEquals(CardPhase.LEARNING, nextAgain.phase)
        assertEquals(0, nextAgain.step)
        assertEquals(1, nextAgain.reps)
        assertEquals(1, nextAgain.lapses)
        assertEquals(now + 60000L, nextAgain.dueEpochMs) // 60s delay
    }

    @Test
    fun testReviewPhaseProgression() {
        val fsrs = FsrsAlgorithm(requestRetention = 0.9)
        val now = 1700000000000L
        val reviewCard = FsrsReviewState(
            phase = CardPhase.REVIEW,
            dueEpochMs = now,
            stability = 5.0,
            difficulty = 4.0,
            reps = 3,
            lapses = 0,
            lastReviewEpochMs = now - 5 * 86400000L
        )

        // Rating Good should increase stability and push due date forward by multiple days
        val nextGood = fsrs.next(reviewCard, FsrsRating.GOOD, now)
        assertEquals(CardPhase.REVIEW, nextGood.phase)
        assertTrue(nextGood.stability > reviewCard.stability)
        assertTrue(nextGood.dueEpochMs > now + 4 * 86400000L)
        assertEquals(4, nextGood.reps)
        assertEquals(0, nextGood.lapses)

        // Rating Again should trigger forget, increasing lapses
        val nextAgain = fsrs.next(reviewCard, FsrsRating.AGAIN, now)
        assertTrue(nextAgain.lapses == 1)
        assertTrue(nextAgain.stability < reviewCard.stability)
    }

    @Test
    fun testRetrievabilityDecay() {
        val fsrs = FsrsAlgorithm()
        val stability = 10.0 // 10 days
        // At day 0, probability of recall is 1.0
        val r0 = fsrs.calculateRetrievability(0.0, stability)
        assertEquals(1.0, r0)

        // At day 10 (elapsedDays == stability), R should be ~0.9
        val r10 = fsrs.calculateRetrievability(10.0, stability)
        assertTrue(r10 > 0.89 && r10 < 0.91)

        // At day 30, probability of recall should have significantly decayed
        val r30 = fsrs.calculateRetrievability(30.0, stability)
        assertTrue(r30 < 0.80)
    }
}
