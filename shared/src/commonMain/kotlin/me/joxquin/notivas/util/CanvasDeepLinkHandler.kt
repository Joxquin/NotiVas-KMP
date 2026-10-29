package me.joxquin.notivas.util

/**
 * Gestor multiplataforma para redireccionamiento de enlaces de Canvas.
 * En Android intenta abrir Canvas Student (canvas-courses://dominio/ruta)
 * si la aplicación está instalada (con sesión iniciada).
 * Si no está instalada, o en Desktop/iOS, abre en el navegador web por defecto.
 */
expect object CanvasDeepLinkHandler {
    fun openCanvasUrl(url: String, fallbackToBrowser: Boolean = true)
}

