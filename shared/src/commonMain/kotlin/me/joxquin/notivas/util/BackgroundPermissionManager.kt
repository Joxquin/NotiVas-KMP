package me.joxquin.notivas.util

import androidx.compose.runtime.Composable

expect class BackgroundPermissionManager {
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
