package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class BackgroundPermissionManager {
    actual fun isNotificationPermissionGranted(): Boolean = true
    actual fun openNotificationSettings() {}
    actual fun isBatteryOptimizationIgnored(): Boolean = true
    actual fun requestIgnoreBatteryOptimization() {}
    actual fun canScheduleExactAlarms(): Boolean = true
    actual fun requestScheduleExactAlarms() {}
    actual fun isMiuiOrHyperOs(): Boolean = false
    actual fun openMiuiAutostartSettings() {}
    actual fun openMiuiBatterySaverSettings() {}
    actual fun openAppSettings() {}
}

@Composable
actual fun rememberBackgroundPermissionManager(): BackgroundPermissionManager {
    return remember { BackgroundPermissionManager() }
}

@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    return { onResult(true) }
}
