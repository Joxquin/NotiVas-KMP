package me.joxquin.notivas.util

import androidx.compose.runtime.Composable

expect class BackgroundPermissionManager {
    fun isNotificationPermissionGranted(): Boolean
    fun openNotificationSettings()
    fun isBatteryOptimizationIgnored(): Boolean
    fun requestIgnoreBatteryOptimization()
    fun canScheduleExactAlarms(): Boolean
    fun requestScheduleExactAlarms()
    fun isMiuiOrHyperOs(): Boolean
    fun openMiuiAutostartSettings()
    fun openMiuiBatterySaverSettings()
    fun openAppSettings()
}

@Composable
expect fun rememberBackgroundPermissionManager(): BackgroundPermissionManager

@Composable
expect fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit
