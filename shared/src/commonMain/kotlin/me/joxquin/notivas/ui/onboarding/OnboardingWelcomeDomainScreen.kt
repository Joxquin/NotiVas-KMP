package me.joxquin.notivas.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.onboarding_domain_continue_btn
import me.joxquin.notivas.shared.resources.onboarding_domain_help
import me.joxquin.notivas.shared.resources.onboarding_domain_input_label
import me.joxquin.notivas.shared.resources.onboarding_domain_input_placeholder
import me.joxquin.notivas.shared.resources.onboarding_domain_quick_suggestions
import me.joxquin.notivas.shared.resources.onboarding_welcome_subtitle
import me.joxquin.notivas.shared.resources.onboarding_welcome_title
import me.joxquin.notivas.shared.resources.student_auth_mode_token
import me.joxquin.notivas.ui.components.aura.MonetAuraBackground
import me.joxquin.notivas.ui.components.brand.NotiVasBrandLogo
import me.joxquin.notivas.ui.components.brand.NotiVasOutlineLogo
import me.joxquin.notivas.ui.onboarding.components.OnboardingStepIndicator
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWelcomeDomainScreen(
    currentUrl: String,
    currentToken: String = "",
    onContinue: (String) -> Unit,
    onContinueWithToken: ((url: String, token: String) -> Unit)? = null,
    onUseManualToken: (() -> Unit)? = null
) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        animationStarted = true
    }

    val animProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "splash_to_welcome_choreography"
    )

    // El aura se activa gradualmente como una ilusión fluida después del splash (de 0f a 0.50f)
    val auraAlpha = (animProgress / 0.40f).coerceIn(0f, 1f) * 0.50f

    // 1. Crossfade suave: del birrete relleno (splash idéntico) hacia el trazo outline Monet
    val outlineFraction = if (animProgress <= 0.12f) 0f else ((animProgress - 0.12f) / 0.38f).coerceIn(0f, 1f)
    val filledAlpha = (1f - outlineFraction).coerceIn(0f, 1f)
    val outlineAlpha = outlineFraction

    // 2. Desplazamiento continuo hacia la cabecera cuando el aura ya está encendida
    val moveFraction = if (animProgress <= 0.22f) 0f else ((animProgress - 0.22f) / 0.65f).coerceIn(0f, 1f)
    val easedMove = FastOutSlowInEasing.transform(moveFraction)

    // 3. Entrada de arriba hacia abajo y desvanecimiento del contenido del Welcome
    val contentFraction = if (animProgress <= 0.50f) 0f else ((animProgress - 0.50f) / 0.50f).coerceIn(0f, 1f)
    val contentAlpha = contentFraction
    val contentOffsetY = (-24.dp) * (1f - contentFraction)

    var inputSubdomain by remember(currentUrl) {
        val clean = currentUrl.removePrefix("https://").removePrefix("http://")
        val sub = if (clean.contains(".instructure.com")) {
            clean.substringBefore(".instructure.com")
        } else if (clean.contains("canvas.utp.edu.pe")) {
            "utp"
        } else {
            clean
        }
        mutableStateOf(if (sub.isNotBlank()) sub else "utp")
    }

    var showTokenInput by remember(currentToken) {
        mutableStateOf(currentToken.isNotBlank() || me.joxquin.notivas.getPlatform().name.contains("Web"))
    }
    var inputToken by remember(currentToken) { mutableStateOf(currentToken) }
    var isTokenVisible by remember { mutableStateOf(false) }

    val presets = listOf(
        "UTP" to "https://canvas.utp.edu.pe",
        "PUCP" to "pucp",
        "UPC" to "upc",
        "UNMSM" to "unmsm",
        "Tecsup" to "tecsup"
    )

    val scrollState = rememberScrollState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var headerState by remember { mutableStateOf(HeaderState.EXPANDED) }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress) {
            if (scrollState.value > 25) {
                headerState = HeaderState.COLLAPSED
            } else if (scrollState.value == 0) {
                headerState = HeaderState.EXPANDED
            }
        }
    }

    MonetAuraBackground(
        alpha = auraAlpha,
        isTransitioning = animProgress < 1f,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!animationStarted) {
                    animationStarted = true
                }
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val totalHeight = maxHeight

            val isImeVisible = WindowInsets.ime.getBottom(density) > 0
            val isCollapsed = animProgress >= 1f && (headerState == HeaderState.COLLAPSED || isImeVisible)
            val collapseProgress by animateFloatAsState(
                targetValue = if (isCollapsed) 1f else 0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "welcome_card_collapse"
            )

            // Medidas del logo: 189.dp produce exactamente los 356px del splash nativo (contenedor 288dp del sistema)
            val startLogoSize = 189.dp
            val targetLogoSize = 76.dp
            val badgeSize = 140.dp
            val topBadgeY = statusBarTop + 8.dp + 36.dp

            // El birrete tiene su anclaje visual en (x=0.5, y=75.5/140).
            // Lo escalamos mediante graphicsLayer de GPU para evitar 60 relayouts por segundo en Compose.
            val anchorYFraction = 75.5f / 140f
            val startCenterY = totalHeight * 0.5f
            val targetCenterY = topBadgeY + badgeSize * 0.5f
            val currentCenterY = startCenterY + (targetCenterY - startCenterY) * easedMove
            val currentTopY = currentCenterY - startLogoSize * anchorYFraction
            val logoScale = 1f + (targetLogoSize / startLogoSize - 1f) * easedMove

            // 1. Roseta / Hero Scalloped Badge (Pre-compuesto desde frame 0, animado en GPU)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        with(density) {
                            IntOffset(x = 0, y = topBadgeY.toPx().roundToInt())
                        }
                    }
                    .graphicsLayer {
                        alpha = moveFraction * (1f - collapseProgress)
                        val scale = (0.82f + 0.18f * moveFraction) * (1f - 0.15f * collapseProgress)
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                WelcomeScallopedHeroBadge(
                    modifier = Modifier.size(badgeSize)
                ) {}
            }

            // 2. Logo Dinámico que transiciona de relleno (splash) a outline Monet y vuela a su lugar
            // El Box se mantiene fijo en startLogoSize (189.dp) y el escalado ocurre 100% en GPU
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        with(density) {
                            IntOffset(x = 0, y = currentTopY.toPx().roundToInt())
                        }
                    }
                    .graphicsLayer {
                        alpha = if (animProgress < 1f) 1f else (1f - collapseProgress)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(startLogoSize)
                        .graphicsLayer {
                            scaleX = logoScale * (1f - 0.15f * collapseProgress)
                            scaleY = logoScale * (1f - 0.15f * collapseProgress)
                            transformOrigin = TransformOrigin(0.5f, anchorYFraction)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Logo relleno (idéntico al splash) - pre-compuesto
                    NotiVasBrandLogo(
                        size = startLogoSize,
                        transparentBackground = true,
                        elevation = 0.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = filledAlpha }
                    )

                    // Logo outline Monet - pre-compuesto
                    NotiVasOutlineLogo(
                        size = startLogoSize,
                        tint = MaterialTheme.colorScheme.primary,
                        accentTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = outlineAlpha }
                    )
                }
            }


            // 3. Contenido del Welcome (Pre-compuesto desde frame 0 para eliminar el jank en low-end)
            val isContentInteractive = contentFraction >= 0.95f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .graphicsLayer {
                        alpha = contentAlpha
                        translationY = with(density) { contentOffsetY.toPx() }
                    },
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Fila Superior FIJA: Boton Token API a la izquierda, Indicador de paso a la derecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (showTokenInput) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (showTokenInput) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            }
                        ),
                        modifier = Modifier.clickable {
                            showTokenInput = !showTokenInput
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = if (showTokenInput) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(Res.string.student_auth_mode_token),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (showTokenInput) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }

                    OnboardingStepIndicator(currentStep = 1, totalSteps = 4)
                }

                // 2. Contenedor desplazable con seccion colapsable (0% vs 100%) y tarjeta
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Colapsable: Espacio del Badge + Titulo + Subtitulo (Desvanecimiento a 0% / 100%)
                    AnimatedVisibility(
                        visible = !isCollapsed,
                        enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
                        exit = shrinkVertically(tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Espacio reservado para el badge del logo que flota arriba
                            Spacer(modifier = Modifier.height(badgeSize + 16.dp))

                            // Titulo Expresivo y Subtitulo
                            Text(
                                text = stringResource(Res.string.onboarding_welcome_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = stringResource(Res.string.onboarding_welcome_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    if (isCollapsed) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                        // Tarjeta de Entrada de Institucion / Campus y Token Opcional
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = stringResource(Res.string.onboarding_domain_input_label),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = inputSubdomain,
                                    onValueChange = { inputSubdomain = it.trim() },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        Text(
                                            text = if (inputSubdomain.contains("utp", ignoreCase = true)) {
                                                ".edu.pe"
                                            } else {
                                                ".instructure.com"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            text = stringResource(Res.string.onboarding_domain_input_placeholder),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    )
                                )

                                AnimatedVisibility(
                                    visible = showTokenInput,
                                    enter = expandVertically(
                                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(350)),
                                    exit = shrinkVertically(
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(250))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.student_auth_mode_token).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                        )

                                        OutlinedTextField(
                                            value = inputToken,
                                            onValueChange = { inputToken = it.trim() },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Key,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                                    Icon(
                                                        imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = if (isTokenVisible) "Ocultar" else "Mostrar",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            },
                                            placeholder = {
                                                Text(
                                                    text = "Pega tu token de Canvas aquí",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            },
                                            shape = RoundedCornerShape(18.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = stringResource(Res.string.onboarding_domain_quick_suggestions),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    presets.forEach { (label, value) ->
                                        val isSelected = inputSubdomain.equals(label, ignoreCase = true) ||
                                                (label == "UTP" && inputSubdomain.contains("utp", ignoreCase = true))

                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerHighest
                                            },
                                            modifier = Modifier.clickable {
                                                inputSubdomain = if (label == "UTP") "canvas.utp.edu.pe" else value
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(Res.string.onboarding_domain_help),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable { showTokenInput = !showTokenInput }
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Boton Continuar en Parte Inferior (Fijo sobre IME)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val finalUrl = when {
                                    inputSubdomain.startsWith("http://") || inputSubdomain.startsWith("https://") -> inputSubdomain
                                    inputSubdomain.contains("utp", ignoreCase = true) -> "https://canvas.utp.edu.pe"
                                    inputSubdomain.contains(".") -> "https://$inputSubdomain"
                                    else -> "https://${inputSubdomain}.instructure.com"
                                }
                                if (showTokenInput && inputToken.isNotBlank() && onContinueWithToken != null) {
                                    onContinueWithToken(finalUrl, inputToken)
                                } else {
                                    onContinue(finalUrl)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = if (showTokenInput && inputToken.isNotBlank()) "Verificar Token" else stringResource(Res.string.onboarding_domain_continue_btn),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

/**
 * Insignia Scalloped decorativa con roseta de 12 petalos suaves e inspiracion Material 3 Expressive.
 */
@Composable
private fun WelcomeScallopedHeroBadge(
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2f
            val innerRadius = outerRadius * 0.85f
            val petals = 12

            val path = Path()
            val totalSteps = petals * 4
            for (step in 0..totalSteps) {
                val angle = (step.toFloat() / totalSteps) * (2f * PI.toFloat())
                val wave = (cos(step.toFloat() / totalSteps * petals * 2f * PI.toFloat()) + 1f) / 2f
                val r = innerRadius + (outerRadius - innerRadius) * wave
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (step == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(path = path, color = badgeColor)
        }

        content()
    }
}

private enum class HeaderState {
    EXPANDED,
    COLLAPSED
}
