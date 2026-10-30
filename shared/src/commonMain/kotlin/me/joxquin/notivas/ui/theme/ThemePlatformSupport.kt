package me.joxquin.notivas.ui.theme

/**
 * Determina si la plataforma y versión actual del sistema operativo soporta
 * el sistema visual y los efectos avanzados de HyperOS MIUIX.
 *
 * En Android requiere Android 13+ (API 33+) debido al uso de RuntimeShader/AGSL.
 * En versiones anteriores (Android 8 a 12), el tema debe permanecer forzado a Material Design 3.
 */
expect fun isMiuixThemeSupported(): Boolean

