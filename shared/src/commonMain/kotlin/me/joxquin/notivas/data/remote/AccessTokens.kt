package me.joxquin.notivas.data.remote

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import me.joxquin.notivas.data.model.AuthMode
import me.joxquin.notivas.util.DateTimeUtil

/** Token de acceso de Canvas recien creado junto con su vencimiento en milisegundos epoch. */
data class GeneratedAccessToken(
    val token: String,
    val expiresAtMillis: Long
)

/** Reglas de vida y renovacion del token de estudiante que NotiVas genera en Canvas. */
object AccessTokenPolicy {
    private const val DAY_MS = 24L * 60 * 60 * 1000

    /** Vigencia que se pide a Canvas al crear un token de estudiante. */
    const val LIFETIME_MS = 28L * DAY_MS

    /** Margen antes del vencimiento a partir del cual se intenta renovar. */
    const val RENEW_WINDOW_MS = 3L * DAY_MS

    /**
     * Solo los tokens Bearer con vencimiento conocido se renuevan. Un token pegado a mano
     * (vencimiento 0) o una sesion por cookies no tienen fecha que vigilar.
     */
    fun needsRenewal(nowMillis: Long, expiresAtMillis: Long, authMode: AuthMode): Boolean {
        if (authMode != AuthMode.TOKEN) return false
        if (expiresAtMillis <= 0L) return false
        return expiresAtMillis - nowMillis <= RENEW_WINDOW_MS
    }
}

/**
 * Lee la respuesta de `POST /api/v1/users/self/tokens`. Usa `expires_at` si Canvas lo devuelve
 * y, si no, el vencimiento que se pidio al crear el token.
 */
internal fun parseGeneratedAccessToken(body: String, requestedExpiresAtMillis: Long): GeneratedAccessToken? {
    if (!body.trimStart().startsWith("{")) return null
    val json = try {
        KtorHttpClientProvider.jsonConfig.parseToJsonElement(body) as? JsonObject
    } catch (_: Exception) {
        null
    } ?: return null

    val token = json.stringOrNull("token") ?: json.stringOrNull("visible_token")
    if (token.isNullOrBlank()) return null

    val expiresAt = json.stringOrNull("expires_at")
        ?.let { DateTimeUtil.parseIsoToComponents(it)?.epochMillis }
        ?: requestedExpiresAtMillis
    return GeneratedAccessToken(token = token, expiresAtMillis = expiresAt)
}

private fun JsonObject.stringOrNull(key: String): String? =
    (get(key) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() && it != "null" }

/** Formatea milisegundos epoch como ISO 8601 en UTC (`2026-12-16T19:45:12Z`), sin depender de la plataforma. */
fun epochMillisToIsoUtc(epochMillis: Long): String {
    val totalSeconds = epochMillis.floorDiv(1000L)
    val days = totalSeconds.floorDiv(86_400L)
    val secondsOfDay = totalSeconds.mod(86_400L)

    // Algoritmo civil_from_days (Howard Hinnant): dias desde 1970-01-01 a fecha gregoriana.
    val z = days + 719_468L
    val era = z.floorDiv(146_097L)
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1_460L + doe / 36_524L - doe / 146_096L) / 365L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = doy - (153L * mp + 2L) / 5L + 1L
    val month = if (mp < 10L) mp + 3L else mp - 9L
    val year = yoe + era * 400L + if (month <= 2L) 1L else 0L

    val hour = secondsOfDay / 3_600L
    val minute = (secondsOfDay % 3_600L) / 60L
    val second = secondsOfDay % 60L
    fun Long.pad(width: Int) = toString().padStart(width, '0')
    return "${year.pad(4)}-${month.pad(2)}-${day.pad(2)}T${hour.pad(2)}:${minute.pad(2)}:${second.pad(2)}Z"
}
