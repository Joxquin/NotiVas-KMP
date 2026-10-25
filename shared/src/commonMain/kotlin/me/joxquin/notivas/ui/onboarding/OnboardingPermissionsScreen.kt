package me.joxquin.notivas.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.onboarding_alarms_desc
import me.joxquin.notivas.shared.resources.onboarding_alarms_title
import me.joxquin.notivas.shared.resources.onboarding_battery_desc
import me.joxquin.notivas.shared.resources.onboarding_battery_title
import me.joxquin.notivas.shared.resources.onboarding_domain_continue_btn
import me.joxquin.notivas.shared.resources.onboarding_finish_btn
import me.joxquin.notivas.shared.resources.onboarding_miui_autostart_btn
import me.joxquin.notivas.shared.resources.onboarding_miui_battery_btn
import me.joxquin.notivas.shared.resources.onboarding_miui_desc
import me.joxquin.notivas.shared.resources.onboarding_miui_title
import me.joxquin.notivas.shared.resources.onboarding_notifications_desc
import me.joxquin.notivas.shared.resources.onboarding_notifications_title
import me.joxquin.notivas.ui.onboarding.components.OnboardingStepIndicator
import me.joxquin.notivas.util.rememberBackgroundPermissionManager
import me.joxquin.notivas.util.rememberNotificationPermissionLauncher
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingPermissionsScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionManager = rememberBackgroundPermissionManager()

    var isNotificationGranted by remember { mutableStateOf(permissionManager.isNotificationPermissionGranted()) }
    var isBatteryIgnored by remember { mutableStateOf(permissionManager.isBatteryOptimizationIgnored()) }
    var canScheduleAlarms by remember { mutableStateOf(permissionManager.canScheduleExactAlarms()) }
    val isXiaomiDevice = remember { permissionManager.isMiuiOrHyperOs() }

    var showXiaomiDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val requestNotification = rememberNotificationPermissionLauncher { granted ->
        isNotificationGranted = granted
    }

    // Comprobacion reactiva continua cada vez que el usuario vuelve a primer plano
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isNotificationGranted = permissionManager.isNotificationPermissionGranted()
        isBatteryIgnored = permissionManager.isBatteryOptimizationIgnored()
        canScheduleAlarms = permissionManager.canScheduleExactAlarms()
    }

    val scrollState = rememberScrollState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnboardingStepIndicator(currentStep = 3, totalSteps = 4)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Badge Hero Scalloped decorativo con icono de credencial / identificacion
                ScallopedHeroBadge(
                    icon = Icons.Outlined.Badge,
                    modifier = Modifier.size(112.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Titulo expresivo y subtitulo claro
                Text(
                    text = "Permisos",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NotiVas requiere los siguientes permisos para mantener tu horario y cursos siempre al dia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 3. Tarjeta 1: Notificaciones
                ExpressivePermissionRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(Res.string.onboarding_notifications_title),
                    description = stringResource(Res.string.onboarding_notifications_desc),
                    isGranted = isNotificationGranted,
                    onClick = {
                        if (!isNotificationGranted) {
                            requestNotification()
                        } else {
                            permissionManager.openNotificationSettings()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Tarjeta 2: Alarmas Exactas
                ExpressivePermissionRow(
                    icon = Icons.Outlined.Alarm,
                    title = stringResource(Res.string.onboarding_alarms_title),
                    description = stringResource(Res.string.onboarding_alarms_desc),
                    isGranted = canScheduleAlarms,
                    onClick = {
                        permissionManager.requestScheduleExactAlarms()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Tarjeta 3: Bateria en segundo plano
                ExpressivePermissionRow(
                    icon = Icons.Outlined.BatteryChargingFull,
                    title = stringResource(Res.string.onboarding_battery_title),
                    description = stringResource(Res.string.onboarding_battery_desc),
                    isGranted = isBatteryIgnored,
                    onClick = {
                        permissionManager.requestIgnoreBatteryOptimization()
                    }
                )

                // 6. Tarjeta 4: Ajustes HyperOS / MIUI (Solo si es dispositivo Xiaomi/Redmi/Poco)
                if (isXiaomiDevice) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExpressivePermissionRow(
                        icon = Icons.Outlined.Security,
                        title = stringResource(Res.string.onboarding_miui_title),
                        description = stringResource(Res.string.onboarding_miui_desc),
                        isGranted = false,
                        onClick = {
                            showXiaomiDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 7. Barra Inferior al estilo de la referencia (Boton circular a la izquierda + Boton pill grande a la derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(56.dp)
                ) {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Informacion sobre permisos",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_domain_continue_btn),
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

    if (showXiaomiDialog) {
        AlertDialog(
            onDismissRequest = { showXiaomiDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.onboarding_miui_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.onboarding_miui_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionManager.openMiuiAutostartSettings()
                        showXiaomiDialog = false
                    }
                ) {
                    Text(stringResource(Res.string.onboarding_miui_autostart_btn))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        permissionManager.openMiuiBatterySaverSettings()
                        showXiaomiDialog = false
                    }
                ) {
                    Text(stringResource(Res.string.onboarding_miui_battery_btn))
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("¿Por que estos permisos?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "NotiVas sincroniza tus tareas de Canvas LMS de forma autonoma y offline-first. Las notificaciones te alertan cuando un profesor publica una calificacion, mientras que las alarmas exactas te recuerdan entregar a tiempo antes de la hora limite.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

/**
 * Tarjeta horizontal de permisos con realce tonal al otorgarse y checkmark a la derecha (estilo Imagen 3).
 */
@Composable
private fun ExpressivePermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(300),
        label = "perm_row_bg"
    )

    val iconBgColor = if (isGranted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val iconTint = if (isGranted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono en circulo tonal
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Titulo y descripcion
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Checkmark limpio a la derecha cuando esta concedido, o flecha/indicador si esta pendiente
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Concedido",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Configurar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Badge Scalloped decorativo con geometria sinusoidal de 12 petalos suaves (estilo Imagen 3).
 */
@Composable
private fun ScallopedHeroBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val badgeColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    val iconColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2f
            val innerRadius = outerRadius * 0.84f
            val petals = 12

            val path = Path()
            val totalSteps = petals * 4
            for (step in 0..totalSteps) {
                val angle = (step.toFloat() / totalSteps) * (2f * PI.toFloat())
                // Curva sinusoidal suave para los petalos scalloped
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

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(46.dp)
        )
    }
}
