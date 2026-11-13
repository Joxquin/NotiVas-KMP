package me.joxquin.notivas.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.onboarding_login_sso
import me.joxquin.notivas.shared.resources.onboarding_token_switch_btn
import me.joxquin.notivas.shared.resources.onboarding_token_dialog_title
import me.joxquin.notivas.shared.resources.onboarding_token_dialog_desc
import me.joxquin.notivas.shared.resources.onboarding_token_dialog_btn_generate
import me.joxquin.notivas.shared.resources.onboarding_token_dialog_btn_cookies
import me.joxquin.notivas.ui.components.CanvasLoginWebView
import me.joxquin.notivas.ui.onboarding.components.OnboardingStepIndicator
import me.joxquin.notivas.getPlatform
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWebLoginScreen(
    universityUrl: String,
    onLoginSuccess: (cookies: String, csrfToken: String?, generateStudentToken: Boolean) -> Unit,
    onUseManualToken: () -> Unit,
    onBack: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
    var isLoading by remember { mutableStateOf(true) }
    var pendingLoginCredentials by remember { mutableStateOf<Pair<String, String?>?>(null) }

    val isWebPlatform = remember { getPlatform().name.contains("Web") }

    val formattedLoginUrl = remember(universityUrl) {
        val clean = universityUrl.trim().trimEnd('/')
        if (clean.endsWith("/login/canvas") || clean.endsWith("/login")) {
            clean
        } else {
            "$clean/login/canvas"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.onboarding_login_sso),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = universityUrl.removePrefix("https://").removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atras",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUseManualToken) {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = stringResource(Res.string.onboarding_token_switch_btn),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    OnboardingStepIndicator(
                        currentStep = 2,
                        totalSteps = if (isWebPlatform) 2 else 4,
                        modifier = Modifier.padding(end = 16.dp, start = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Barra informativa sutil
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Inicia sesion con tu cuenta institucional. NotiVas capturara tu sesion automaticamente de forma segura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // WebView embebido con esquinas suaves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                CanvasLoginWebView(
                    url = formattedLoginUrl,
                    onLoginSuccess = { cookies, csrfToken ->
                        val isAlreadyToken = cookies.startsWith("access_token=") || cookies.startsWith("Bearer ")
                        if (isAlreadyToken || isWebPlatform) {
                            onLoginSuccess(cookies, csrfToken, false)
                        } else {
                            pendingLoginCredentials = Pair(cookies, csrfToken)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onPageStarted = { isLoading = true },
                    onPageFinished = { isLoading = false }
                )
            }
        }

        // Diálogo de consentimiento para generar el token de estudiante permanente
        if (pendingLoginCredentials != null) {
            val (cookies, csrf) = pendingLoginCredentials!!
            if (themeStyle == AppThemeStyle.MIUIX) {
                WindowDialog(
                    show = true,
                    title = stringResource(Res.string.onboarding_token_dialog_title),
                    summary = stringResource(Res.string.onboarding_token_dialog_desc),
                    onDismissRequest = { pendingLoginCredentials = null },
                    content = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                        ) {
                            MiuixTextButton(
                                text = stringResource(Res.string.onboarding_token_dialog_btn_generate),
                                onClick = {
                                    val creds = pendingLoginCredentials
                                    pendingLoginCredentials = null
                                    if (creds != null) {
                                        onLoginSuccess(creds.first, creds.second, true)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = MiuixButtonDefaults.textButtonColorsPrimary()
                            )
                            MiuixTextButton(
                                text = stringResource(Res.string.onboarding_token_dialog_btn_cookies),
                                onClick = {
                                    val creds = pendingLoginCredentials
                                    pendingLoginCredentials = null
                                    if (creds != null) {
                                        onLoginSuccess(creds.first, creds.second, false)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            } else {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { pendingLoginCredentials = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(Res.string.onboarding_token_dialog_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.onboarding_token_dialog_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                val creds = pendingLoginCredentials
                                pendingLoginCredentials = null
                                if (creds != null) {
                                    onLoginSuccess(creds.first, creds.second, true)
                                }
                            }
                        ) {
                            Text(stringResource(Res.string.onboarding_token_dialog_btn_generate))
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val creds = pendingLoginCredentials
                                pendingLoginCredentials = null
                                if (creds != null) {
                                    onLoginSuccess(creds.first, creds.second, false)
                                }
                            }
                        ) {
                            Text(stringResource(Res.string.onboarding_token_dialog_btn_cookies))
                        }
                    }
                )
            }
        }
    }
}
