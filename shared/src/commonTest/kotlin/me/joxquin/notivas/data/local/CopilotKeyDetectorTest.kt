package me.joxquin.notivas.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CopilotKeyDetectorTest {

    @Test
    fun recognizesEachServiceByItsPrefix() {
        assertEquals(CopilotProvider.OPENROUTER, CopilotKeyDetector.providerFor("sk-or-v1-abc"))
        assertEquals(CopilotProvider.GOOGLE_AI_STUDIO, CopilotKeyDetector.providerFor("AIzaSyExample"))
        // Las keys nuevas de Google AI Studio empiezan con "AQ.".
        assertEquals(CopilotProvider.GOOGLE_AI_STUDIO, CopilotKeyDetector.providerFor("  AQ.Ab8RN6Example "))
    }

    @Test
    fun unknownOrEmptyKeysKeepTheFieldTheUserChose() {
        assertNull(CopilotKeyDetector.providerFor("mi-clave-personal"))
        assertNull(CopilotKeyDetector.providerFor(""))
        assertNull(CopilotKeyDetector.providerFor(null))
    }
}
