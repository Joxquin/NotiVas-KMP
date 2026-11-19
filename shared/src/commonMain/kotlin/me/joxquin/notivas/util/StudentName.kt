package me.joxquin.notivas.util

/**
 * Primer nombre del estudiante a partir del nombre que devuelve Canvas. Tecsup usa el formato
 * "Apellidos, Nombres" ("Alvarez Sayaverde, Alejandro Aaron" -> "Alejandro"); otras instituciones
 * "Nombre Apellido" ("Alejandro Alvarez" -> "Alejandro"). Devuelve null si no hay un nombre usable.
 */
fun canvasFirstName(rawName: String?): String? {
    val name = rawName?.trim().orEmpty()
    if (name.isEmpty()) return null
    val givenPart = if ("," in name) name.substringAfter(",").trim() else name
    val first = givenPart.split(" ").firstOrNull { it.isNotBlank() } ?: return null
    return first.lowercase().replaceFirstChar { it.uppercase() }
}
