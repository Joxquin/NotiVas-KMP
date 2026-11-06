package me.joxquin.notivas.util

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.joxquin.notivas.di.AppModule

actual class GoogleAuthLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launchSignIn() {
        onLaunch()
    }
}

@Composable
actual fun rememberGoogleAuthLauncher(
    onAuthSuccess: (accessToken: String, refreshToken: String?, userEmail: String?) -> Unit,
    onAuthError: (String) -> Unit
): GoogleAuthLauncher {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentOnSuccess = rememberUpdatedState(onAuthSuccess)
    val currentOnError = rememberUpdatedState(onAuthError)

    val webClientId = AppModule.simulationCloudSync.googleWebClientId
    val gso = remember(webClientId) { driveSignInOptions(webClientId) }

    val googleSignInClient = remember(context, gso) {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email
                val accountObj = account?.account

                if (accountObj != null && email != null) {
                    account.idToken?.let(AppModule.simulationCloudSync::onGoogleIdToken)
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val scopeStr = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/userinfo.email"
                            val token = GoogleAuthUtil.getToken(context, accountObj, scopeStr)
                            withContext(Dispatchers.Main) {
                                currentOnSuccess.value(token, null, email)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                currentOnError.value("Error obteniendo token: ${e.message}")
                            }
                        }
                    }
                } else {
                    currentOnError.value("No se pudo obtener la cuenta seleccionada.")
                }
            } catch (e: ApiException) {
                currentOnError.value("Error de Google Sign-In (${e.statusCode}): ${e.message}")
            } catch (e: Exception) {
                currentOnError.value("Error inesperado en Google Sign-In: ${e.message}")
            }
        } else {
            currentOnError.value("Inicio de sesión cancelado.")
        }
    }

    return remember(launcher, googleSignInClient) {
        GoogleAuthLauncher {
            try {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            } catch (e: Exception) {
                currentOnError.value("Error al iniciar selector de Google: ${e.message}")
            }
        }
    }
}

/**
 * Opciones del login de Google Drive. Con Firebase activo también se pide el ID token,
 * que sirve para vincular la cuenta anónima del respaldo sin un segundo inicio de sesión.
 */
private fun driveSignInOptions(webClientId: String?): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(
            Scope("https://www.googleapis.com/auth/drive.file"),
            Scope("https://www.googleapis.com/auth/drive.readonly")
        )
        .apply { if (webClientId != null) requestIdToken(webClientId) }
        .build()

/**
 * Para quien ya había conectado Google Drive antes de existir el respaldo: pide un ID token
 * en silencio y vincula la cuenta sin volver a mostrar el selector.
 */
fun linkCloudBackupWithLastGoogleAccount(context: Context) {
    val cloudSync = AppModule.simulationCloudSync
    val webClientId = cloudSync.googleWebClientId ?: return
    if (cloudSync.isLinkedWithGoogle || GoogleSignIn.getLastSignedInAccount(context) == null) return
    GoogleSignIn.getClient(context, driveSignInOptions(webClientId)).silentSignIn()
        .addOnSuccessListener { account -> account.idToken?.let(cloudSync::onGoogleIdToken) }
}
