package me.joxquin.notivas.util

/**
 * Resuelve la URL para llamadas de red según la plataforma.
 * En Web (Wasm), redirige hacia el proxy local de desarrollo para evitar restricciones CORS impuestas por el navegador.
 * En Android, JVM y iOS, devuelve la URL original sin modificaciones.
 */
expect fun resolveApiUrl(targetUrl: String): String

