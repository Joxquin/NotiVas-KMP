package me.joxquin.notivas.util

import me.joxquin.notivas.data.remote.epochMillisToIsoUtc
import kotlin.math.roundToInt

/** Vencimiento del token tal como se muestra: fecha local y días de calendario que faltan. */
data class TokenExpiryDisplay(
    val daysRemaining: Int,
    val date: DateComponents
)

/**
 * Convierte el vencimiento (milisegundos epoch) a la fecha local del dispositivo. Antes se
 * redondeaba (vencimiento - ahora) hacia abajo y se sumaba a hoy, lo que mostraba un día menos
 * (un token que vence el jueves 17 a las 09:06 aparecía como "miércoles 16").
 */
fun tokenExpiryDisplay(expiresAtMillis: Long, nowMillis: Long = DateTimeUtil.nowEpochMillis()): TokenExpiryDisplay? {
    if (expiresAtMillis <= 0L) return null
    val expiryDate = DateTimeUtil.parseIsoToComponents(epochMillisToIsoUtc(expiresAtMillis))?.date ?: return null
    val today = DateTimeUtil.parseIsoToComponents(epochMillisToIsoUtc(nowMillis))?.date ?: return null
    // Mediodía de ambos días: la resta da días de calendario aunque haya cambio de horario.
    val diffMillis = DateTimeUtil.toEpochMillis(expiryDate.year, expiryDate.month, expiryDate.day, 12, 0) -
        DateTimeUtil.toEpochMillis(today.year, today.month, today.day, 12, 0)
    val days = (diffMillis.toDouble() / 86_400_000.0).roundToInt().coerceAtLeast(0)
    return TokenExpiryDisplay(daysRemaining = days, date = expiryDate)
}
