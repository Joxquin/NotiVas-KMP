package me.joxquin.notivas.util

object CourseUtil {

    private val STOP_WORDS = setOf(
        "de", "del", "la", "las", "el", "los", "en", "y", "e", "para", "por", "con", "a"
    )

    /**
     * Limpia sufijos de sección típicos de Canvas/Tecsup como " - C24 4to C-A - C24 4to D-A-A"
     */
    fun cleanCourseName(courseName: String?): String {
        if (courseName.isNullOrBlank()) return ""
        val withoutSection = courseName.replace(Regex("""\s*-\s*C\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*-\s*20\d\d.*$""", RegexOption.IGNORE_CASE), "")
            .trim()
        return withoutSection.ifBlank { courseName.trim() }
    }

    /**
     * Extrae una sigla o etiqueta representativa para el curso (ej. DAE, TE, PM, etc.)
     * evitando absolutamente el uso de valores hardcodeados estáticos.
     */
    fun extractCourseTag(courseCode: String?, courseName: String?): String {
        val trimmedCode = courseCode?.trim()
        val cleanName = cleanCourseName(courseName)

        // Si viene un courseCode conciso y significativo de la API de Canvas
        if (!trimmedCode.isNullOrBlank() && trimmedCode.length <= 10 && !trimmedCode.equals(courseName, ignoreCase = true)) {
            return trimmedCode
        }

        if (cleanName.isBlank()) {
            return "CURSO"
        }

        // Si el nombre limpio es corto, usarlo directamente
        if (cleanName.length <= 14) {
            return cleanName
        }

        // Generar acrónimo con palabras clave significativas
        val words = cleanName.split(Regex("""[\s\-_/]+""")).filter { it.isNotBlank() }
        val significant = words.filter { it.lowercase() !in STOP_WORDS }

        return if (significant.size >= 2) {
            significant.take(4).map { it.first().uppercaseChar() }.joinToString("")
        } else if (significant.isNotEmpty()) {
            val single = significant.first()
            if (single.length <= 10) single else single.take(8)
        } else {
            cleanName.take(8).uppercase()
        }
    }
}
