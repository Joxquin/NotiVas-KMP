package me.joxquin.notivas.cloud

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.firebase.perf.performance
import kotlinx.coroutines.tasks.await
import me.joxquin.notivas.data.cloud.CloudBackend

/**
 * Respaldo en la nube con Firebase: Authentication (anónimo + Google), Firestore,
 * Crashlytics y Performance Monitoring.
 * Cada curso de Canvas es un documento en `users/{uid}/courses/{canvasCourseId}`.
 */
class FirebaseCloudBackend(context: Context) : CloudBackend {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val crashlytics = Firebase.crashlytics
    private val performance = Firebase.performance

    // El plugin de google-services genera este recurso solo si el proyecto tiene Google habilitado.
    @SuppressLint("DiscouragedApi")
    override val googleWebClientId: String? = context.resources
        .getIdentifier("default_web_client_id", "string", context.packageName)
        .takeIf { it != 0 }
        ?.let(context::getString)

    override val isLinkedWithGoogle: Boolean
        get() = auth.currentUser?.providerData.orEmpty().any { it.providerId == GoogleAuthProvider.PROVIDER_ID }

    override suspend fun ensureSignedIn(): String =
        auth.currentUser?.uid ?: requireNotNull(auth.signInAnonymously().await().user).uid

    override suspend fun linkWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val current = auth.currentUser
        val result = if (current != null && current.isAnonymous) {
            try {
                current.linkWithCredential(credential).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                // Esa cuenta de Google ya tiene respaldo (otro dispositivo): se entra a ella.
                auth.signInWithCredential(credential).await()
            }
        } else {
            auth.signInWithCredential(credential).await()
        }
        return requireNotNull(result.user).uid
    }

    override suspend fun fetchCourses(userId: String): Map<Long, Map<String, Any?>> =
        // Source.SERVER: una lectura de caché vacía haría creer que no hay respaldo.
        courses(userId).get(Source.SERVER).await().documents
            .mapNotNull { doc -> doc.id.toLongOrNull()?.let { courseId -> courseId to doc.data.orEmpty() } }
            .toMap()

    override fun saveCourse(userId: String, courseId: Long, data: Map<String, Any?>) {
        courses(userId).document(courseId.toString())
            .set(data + (FIELD_UPDATED_AT to FieldValue.serverTimestamp()))
            .addOnFailureListener(::recordError)
    }

    override fun recordError(error: Throwable) {
        // Sin red no es un error de la app: Firestore reintenta solo.
        if (error.isOffline()) return
        crashlytics.recordException(error)
    }

    override suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val trace = performance.newTrace(name)
        trace.start()
        try {
            return block()
        } finally {
            trace.stop()
        }
    }

    private fun courses(userId: String) =
        firestore.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_COURSES)

    private fun Throwable.isOffline(): Boolean =
        this is FirebaseNetworkException ||
            (this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE)

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_COURSES = "courses"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
