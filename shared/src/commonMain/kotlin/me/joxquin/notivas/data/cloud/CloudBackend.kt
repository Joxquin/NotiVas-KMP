package me.joxquin.notivas.data.cloud

/**
 * Servicios en la nube que dependen de la plataforma. Hoy solo Android lo implementa (Firebase);
 * en iOS, escritorio y web no se instala ninguno y el respaldo queda apagado.
 */
interface CloudBackend {
    /** Client ID web de OAuth para pedir el ID token de Google, o null si el proyecto no lo tiene. */
    val googleWebClientId: String?

    /** true si la sesión actual ya es una cuenta de Google (no anónima). */
    val isLinkedWithGoogle: Boolean

    /** Devuelve el uid actual; si no hay sesión entra como usuario anónimo. */
    suspend fun ensureSignedIn(): String

    /** Vincula la cuenta anónima con Google (o entra con Google si esa cuenta ya existe) y devuelve el uid. */
    suspend fun linkWithGoogle(idToken: String): String

    /** Lee del servidor los cursos respaldados, por id de curso de Canvas. Falla si no hay red. */
    suspend fun fetchCourses(userId: String): Map<Long, Map<String, Any?>>

    /** Encola la escritura del curso; el SDK la envía cuando haya red. */
    fun saveCourse(userId: String, courseId: Long, data: Map<String, Any?>)

    fun recordError(error: Throwable)

    suspend fun <T> trace(name: String, block: suspend () -> T): T
}
