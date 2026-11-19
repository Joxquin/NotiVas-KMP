package me.joxquin.notivas.data.remote

import me.joxquin.notivas.data.model.AuthMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccessTokensTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_795_047_786_861L

    @Test
    fun epochMillisToIsoUtc_formatsKnownInstants() {
        assertEquals("1970-01-01T00:00:00Z", epochMillisToIsoUtc(0L))
        assertEquals("2000-02-29T00:00:00Z", epochMillisToIsoUtc(951_782_400_000L))
        assertEquals("2026-11-19T00:23:06Z", epochMillisToIsoUtc(now))
        assertEquals("1969-12-31T00:00:00Z", epochMillisToIsoUtc(-day))
    }

    @Test
    fun parseGeneratedAccessToken_usesExpiresAtFromCanvas() {
        val body = """{"id":91,"token":"7~abc","expires_at":"2026-12-16T19:45:12Z","purpose":"NotiVas App"}"""
        val parsed = parseGeneratedAccessToken(body, requestedExpiresAtMillis = 1L)
        assertEquals("7~abc", parsed?.token)
        assertEquals(1_797_450_312_000L, parsed?.expiresAtMillis)
    }

    @Test
    fun parseGeneratedAccessToken_fallsBackToRequestedExpiryAndVisibleToken() {
        val body = """{"visible_token":"7~xyz","expires_at":null}"""
        val parsed = parseGeneratedAccessToken(body, requestedExpiresAtMillis = 42L)
        assertEquals("7~xyz", parsed?.token)
        assertEquals(42L, parsed?.expiresAtMillis)
    }

    @Test
    fun parseGeneratedAccessToken_rejectsBodiesWithoutToken() {
        assertNull(parseGeneratedAccessToken("""{"errors":[{"message":"unauthorized"}]}""", 1L))
        assertNull(parseGeneratedAccessToken("<html>login</html>", 1L))
        assertNull(parseGeneratedAccessToken("""{"token":""}""", 1L))
    }

    @Test
    fun needsRenewal_onlyForBearerTokensCloseToExpiry() {
        assertFalse(AccessTokenPolicy.needsRenewal(now, now + 10 * day, AuthMode.TOKEN))
        assertTrue(AccessTokenPolicy.needsRenewal(now, now + 2 * day, AuthMode.TOKEN))
        assertTrue(AccessTokenPolicy.needsRenewal(now, now - day, AuthMode.TOKEN))
        // Token pegado a mano: sin fecha conocida no hay nada que renovar.
        assertFalse(AccessTokenPolicy.needsRenewal(now, 0L, AuthMode.TOKEN))
        // La sesion por cookies no usa token Bearer.
        assertFalse(AccessTokenPolicy.needsRenewal(now, now + day, AuthMode.COOKIE_SESSION))
    }
}
