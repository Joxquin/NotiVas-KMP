package me.joxquin.notivas.util

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class BackgroundPermissionManager(
    private val context: Context
) {
    actual fun isBatteryOptimizationIgnored(): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } catch (_: Exception) {
            true
        }
    }

    actual fun requestIgnoreBatteryOptimization() {
        try {
            if (isBatteryOptimizationIgnored()) return
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppSettings()
        }
    }

    actual fun canScheduleExactAlarms(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.canScheduleExactAlarms() ?: true
            } else {
                true
            }
        } catch (_: Exception) {
            true
        }
    }

    actual fun requestScheduleExactAlarms() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            openAppSettings()
        }
    }

    actual fun isMiuiOrHyperOs(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            return true
        }
        var hasMiuiProp = false
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java)
            val result = getMethod.invoke(null, "ro.miui.ui.version.name") as? String
            hasMiuiProp = !result.isNullOrBlank()
        } catch (_: Exception) {
            hasMiuiProp = false
        }
        return hasMiuiProp
    }

    actual fun openMiuiAutostartSettings() {
        val intentList = listOf(
            Intent().apply {
                component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            },
            Intent().apply {
                component = ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")
            }
        )
        for (intent in intentList) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        openAppSettings()
    }

    actual fun openMiuiBatterySaverSettings() {
        try {
            val intent = Intent().apply {
                component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                putExtra("package_name", context.packageName)
                putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppSettings()
        }
    }

    actual fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

@Composable
actual fun rememberBackgroundPermissionManager(): BackgroundPermissionManager {
    val context = LocalContext.current
    return remember(context) { BackgroundPermissionManager(context) }
}
