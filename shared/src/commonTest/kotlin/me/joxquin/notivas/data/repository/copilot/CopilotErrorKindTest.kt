package me.joxquin.notivas.data.repository.copilot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CopilotErrorKindTest {

    @Test
    fun classifiesTheErrorsSeenOnTheRedmi() {
        // Respuesta real de Google AI Studio en la prueba del 19/11.
        val highDemand = """Error HTTP 503: [{ "error": { "code": 503, "message": "This model is currently experiencing high demand.", "status": "UNAVAILABLE" } }]"""
        assertEquals(CopilotErrorKind.OVERLOADED, CopilotErrorKind.classify(highDemand))
        assertEquals(CopilotErrorKind.TIMEOUT, CopilotErrorKind.classify("Request timeout has expired [url=https://generativelanguage.googleapis.com]"))
        assertEquals(CopilotErrorKind.AUTH, CopilotErrorKind.classify("Missing Authentication header"))
    }

    @Test
    fun separatesQuotaFromOtherErrors() {
        assertEquals(CopilotErrorKind.RATE_LIMITED, CopilotErrorKind.classify("""Error HTTP 429: {"status": "RESOURCE_EXHAUSTED"}"""))
        assertEquals(CopilotErrorKind.AUTH, CopilotErrorKind.classify("""Error HTTP 400: API key not valid. Please pass a valid API key."""))
        assertEquals(CopilotErrorKind.OTHER, CopilotErrorKind.classify("Unexpected JSON token at offset 0"))
    }

    /** Mismo nombre que la excepción de java.nio que lanza Ktor (CIO) sin mensaje cuando no hay red. */
    private class UnresolvedAddressException : Exception()

    @Test
    fun recognisesNetworkFailuresEvenWithoutMessage() {
        // En modo avión el chat mostraba "No se pudo procesar tu solicitud: " sin nada más.
        assertEquals(CopilotErrorKind.NETWORK, CopilotErrorKind.classify(UnresolvedAddressException()))
        assertEquals(CopilotErrorKind.NETWORK, CopilotErrorKind.classify(RuntimeException("envoltura", UnresolvedAddressException())))
        assertEquals(CopilotErrorKind.NETWORK, CopilotErrorKind.classify("Unable to resolve host \"generativelanguage.googleapis.com\""))
        assertFalse(CopilotErrorKind.NETWORK.isTransient)
    }

    @Test
    fun onlyTemporaryFailuresAreRetried() {
        assertTrue(CopilotErrorKind.OVERLOADED.isTransient)
        assertTrue(CopilotErrorKind.TIMEOUT.isTransient)
        assertFalse(CopilotErrorKind.AUTH.isTransient)
        assertFalse(CopilotErrorKind.OTHER.isTransient)
    }
}
