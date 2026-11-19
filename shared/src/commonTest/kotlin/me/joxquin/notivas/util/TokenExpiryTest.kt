package me.joxquin.notivas.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenExpiryTest {

    @Test
    fun showsTheRealExpiryDayInsteadOfOneDayEarlier() {
        // Caso visto en el Redmi: renovado el 19/11 a las 09:06, vence el 17/12 a las 09:05.
        val now = DateTimeUtil.toEpochMillis(2026, 11, 19, 9, 6)
        val expiresAt = DateTimeUtil.toEpochMillis(2026, 12, 17, 9, 5)
        val display = tokenExpiryDisplay(expiresAt, now)!!
        assertEquals(DateComponents(2026, 12, 17), display.date)
        assertEquals(28, display.daysRemaining)
    }

    @Test
    fun countsCalendarDaysNotFullTwentyFourHourBlocks() {
        // 23 horas después ya es mañana: antes salía "0 días" con la fecha de hoy.
        val now = DateTimeUtil.toEpochMillis(2026, 11, 19, 10, 0)
        val expiresAt = DateTimeUtil.toEpochMillis(2026, 11, 20, 9, 0)
        val display = tokenExpiryDisplay(expiresAt, now)!!
        assertEquals(DateComponents(2026, 11, 20), display.date)
        assertEquals(1, display.daysRemaining)
    }

    @Test
    fun unknownExpiryHasNothingToShow() {
        assertNull(tokenExpiryDisplay(0L))
    }
}
