package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.remote.GoogleDriveAuthService
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import com.sun.net.httpserver.HttpServer

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
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { GoogleDriveAuthService() }

    return remember {
        GoogleAuthLauncher {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val server = HttpServer.create(InetSocketAddress(8888), 0)
                    val redirectUri = "http://localhost:8888/callback"
                    
                    server.createContext("/callback") { exchange ->
                        val query = exchange.requestURI.query ?: ""
                        val code = query.split("&")
                            .firstOrNull { it.startsWith("code=") }
                            ?.removePrefix("code=")

                        val responseHtml = """
                            <!DOCTYPE html>
                            <html>
                            <head><meta charset="utf-8"><title>NotiVas - Google Drive</title></head>
                            <body style="font-family: sans-serif; text-align: center; padding: 40px;">
                                <h2 style="color: #4CAF50;">¡Vinculación Exitosa!</h2>
                                <p>Tu cuenta de Google Drive se conectó con NotiVas. Puedes cerrar esta ventana.</p>
                            </body>
                            </html>
                        """.trimIndent()

                        exchange.sendResponseHeaders(200, responseHtml.toByteArray().size.toLong())
                        exchange.responseBody.use { it.write(responseHtml.toByteArray()) }
                        server.stop(1)

                        if (!code.isNullOrBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val tokenResponse = authService.exchangeCodeForTokens(code, redirectUri)
                                if (tokenResponse != null) {
                                    val userInfo = authService.fetchUserInfo(tokenResponse.accessToken)
                                    onAuthSuccess(
                                        tokenResponse.accessToken,
                                        tokenResponse.refreshToken,
                                        userInfo?.email
                                    )
                                } else {
                                    onAuthError("Error al canjear el código de autorización de Google.")
                                }
                            }
                        }
                    }

                    server.start()
                    val authUrl = authService.getAuthorizationUrl(redirectUri)
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(URI(authUrl))
                    }
                } catch (e: Exception) {
                    onAuthError("No se pudo iniciar el flujo de autenticación: ${e.message}")
                }
            }
        }
    }
}
