@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.joxquin.notivas.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Refresh
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

@JsFun("() => window.NotiVasPairing && window.NotiVasPairing.createChannel()")
private external fun jsCreatePairingChannel()

@JsFun("() => window.NotiVasPairing ? String(window.NotiVasPairing.pin || '') : ''")
private external fun jsGetPairingPin(): String

@JsFun("() => window.NotiVasPairing ? String(window.NotiVasPairing.status || '') : ''")
private external fun jsGetPairingStatus(): String

@JsFun("() => window.NotiVasPairing ? String(window.NotiVasPairing.receivedPayload || '') : ''")
private external fun jsGetPairingPayload(): String

@JsFun("() => window.NotiVasPairing ? String(window.NotiVasPairing.errorMessage || '') : ''")
private external fun jsGetPairingErrorMessage(): String

@Composable
actual fun CanvasLoginWebView(
    url: String,
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    modifier: Modifier,
    onPageStarted: (() -> Unit)?,
    onPageFinished: (() -> Unit)?
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado del PIN efímero para NotiVas Web
    var pairingPin by remember { mutableStateOf("") }
    var pairingStatus by remember { mutableStateOf("IDLE") }
    var pairingTimer by remember { mutableStateOf(120) }

    LaunchedEffect(Unit) {
        onPageFinished?.invoke()
        jsCreatePairingChannel()
    }

    // Monitoreo del estado de vinculación Web (PIN de 6 dígitos)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val st = jsGetPairingStatus()
            pairingStatus = st
            val pin = jsGetPairingPin()
            if (pin.isNotBlank()) {
                pairingPin = pin
            }

            if (st == "WAITING") {
                if (pairingTimer > 0) pairingTimer--
            } else if (st == "SUCCESS") {
                val payloadJson = jsGetPairingPayload()
                if (payloadJson.isNotBlank()) {
                    try {
                        val payload = kotlinx.serialization.json.Json.decodeFromString(
                            me.joxquin.notivas.data.model.SessionPairingPayload.serializer(),
                            payloadJson
                        )
                        if (!payload.googleDriveAccessToken.isNullOrBlank()) {
                            try {
                                me.joxquin.notivas.di.AppModule.preferencesManager.saveGoogleDriveTokens(
                                    accessToken = payload.googleDriveAccessToken,
                                    refreshToken = payload.googleDriveRefreshToken,
                                    userEmail = payload.googleDriveEmail
                                )
                            } catch (_: Exception) { }
                        }

                        if (!payload.accessToken.isNullOrBlank()) {
                            val tokenWithUrl = if (payload.universityUrl.isNotBlank()) {
                                "access_token=${payload.accessToken}&university_url=${payload.universityUrl}"
                            } else {
                                "access_token=${payload.accessToken}"
                            }
                            onLoginSuccess(tokenWithUrl, null)
                            break
                        } else if (!payload.sessionCookies.isNullOrBlank()) {
                            val cookiesWithUrl = if (payload.universityUrl.isNotBlank()) {
                                "${payload.sessionCookies}&university_url=${payload.universityUrl}"
                            } else {
                                payload.sessionCookies
                            }
                            onLoginSuccess(cookiesWithUrl, payload.csrfToken)
                            break
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error al decodificar sesión: " + e.message
                    }
                }
            } else if (st == "EXPIRED") {
                pairingTimer = 0
            }
        }
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
                // Tarjeta Principal: Vinculación Rápida con Móvil (Sin necesidad de extensión)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vincular con tu móvil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Abre NotiVas en tu celular > Ajustes > Vincular NotiVas Web e ingresa este código:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (pairingPin.isNotBlank()) {
                            Text(
                                text = pairingPin,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 8.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Generando código...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (pairingStatus == "WAITING" && pairingTimer > 0) {
                    Text(
                        text = "El código expira en $pairingTimer s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (pairingStatus == "EXPIRED" || pairingTimer <= 0) {
                    Text(
                        text = "Código expirado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            pairingTimer = 120
                            jsCreatePairingChannel()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar nuevo código")
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
            }
        }
    }
}
