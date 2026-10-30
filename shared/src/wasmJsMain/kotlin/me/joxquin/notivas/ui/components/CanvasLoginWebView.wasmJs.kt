@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.joxquin.notivas.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@JsFun("() => window.NotiVasBridge ? Boolean(window.NotiVasBridge.isAvailable) : false")
private external fun jsIsBridgeAvailable(): Boolean

@JsFun("() => window.postMessage({ type: 'NOTIVAS_PING' }, '*')")
private external fun jsSendPing()

@JsFun("(domain) => window.NotiVasBridge && window.NotiVasBridge.requestSession(domain)")
private external fun jsRequestSession(domain: String)

@JsFun("() => window.NotiVasBridge ? String(window.NotiVasBridge.sessionState || '') : ''")
private external fun jsGetSessionState(): String

@JsFun("() => window.NotiVasBridge ? String(window.NotiVasBridge.cookieHeader || '') : ''")
private external fun jsGetCookieHeader(): String

@JsFun("() => window.NotiVasBridge ? String(window.NotiVasBridge.csrfToken || '') : ''")
private external fun jsGetCsrfToken(): String

@JsFun("() => window.NotiVasBridge ? String(window.NotiVasBridge.errorMessage || '') : ''")
private external fun jsGetErrorMessage(): String

@Composable
actual fun CanvasLoginWebView(
    url: String,
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    modifier: Modifier,
    onPageStarted: (() -> Unit)?,
    onPageFinished: (() -> Unit)?
) {
    var isBridgeAvailable by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cleanDomain = remember(url) {
        url.removePrefix("https://")
            .removePrefix("http://")
            .split("/")[0]
            .trim()
    }

    LaunchedEffect(Unit) {
        onPageFinished?.invoke()
        for (i in 0..20) {
            jsSendPing()
            val available = jsIsBridgeAvailable()
            if (available) {
                isBridgeAvailable = true
                break
            }
            delay(500)
        }
    }

    LaunchedEffect(isFetching) {
        if (!isFetching) return@LaunchedEffect
        onPageStarted?.invoke()

        var attempts = 0
        while (attempts < 60) {
            delay(300)
            val state = jsGetSessionState()
            if (state == "SUCCESS") {
                val cookies = jsGetCookieHeader()
                val csrf = jsGetCsrfToken().takeIf { it.isNotBlank() }
                isFetching = false
                onPageFinished?.invoke()
                if (cookies.isNotBlank()) {
                    onLoginSuccess(cookies, csrf)
                    return@LaunchedEffect
                }
            } else if (state == "ERROR") {
                isFetching = false
                onPageFinished?.invoke()
                val err = jsGetErrorMessage()
                errorMessage = if (err.isNotBlank()) err else "No se pudo recuperar la sesion de Canvas."
                return@LaunchedEffect
            }
            attempts++
        }
        isFetching = false
        onPageFinished?.invoke()
        errorMessage = "Tiempo de espera agotado al conectar con Canvas."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBridgeAvailable) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBridgeAvailable) Icons.Outlined.CheckCircle else Icons.Outlined.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isBridgeAvailable) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enlace Web con Canvas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = cleanDomain,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isBridgeAvailable) Color(0xFF14532D) else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isBridgeAvailable) Color(0xFF4ADE80) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBridgeAvailable) "Extension Bridge Conectada" else "Buscando Extension NotiVas Bridge...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isBridgeAvailable) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                if (isBridgeAvailable) {
                    Text(
                        text = "Se ha detectado tu sesion en el navegador. Pulsa el boton para sincronizar tus asignaturas de inmediato.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            isFetching = true
                            jsRequestSession(cleanDomain)
                        },
                        enabled = !isFetching,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isFetching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sincronizando sesion...")
                        } else {
                            Icon(imageVector = Icons.Outlined.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar sesion activa")
                        }
                    }
                } else {
                    Text(
                        text = "Para iniciar sesion de forma automatica sin copiar tokens, asegurate de tener cargada la extension NotiVas Canvas Bridge en este navegador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("https://github.com/Joxquin/NotiVas-KMP/releases")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Extension, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Descargar extension NotiVas Bridge")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            jsSendPing()
                            val available = jsIsBridgeAvailable()
                            if (available) isBridgeAvailable = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificar conexion con extension")
                    }
                }
            }
        }
    }
}
