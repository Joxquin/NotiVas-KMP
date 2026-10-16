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

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .build()
    }

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
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val scopeStr = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/userinfo.email"
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
