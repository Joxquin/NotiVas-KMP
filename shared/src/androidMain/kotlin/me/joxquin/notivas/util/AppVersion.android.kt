package me.joxquin.notivas.util

import android.app.Application
import android.content.Context
import android.os.Build

actual object AppVersion {
    actual fun get(): AppVersionInfo {
        return try {
            val context = getAndroidApplicationContext()
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            val versionName = packageInfo.versionName ?: "1.0.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            AppVersionInfo(versionName = versionName, versionCode = versionCode)
        } catch (_: Throwable) {
            AppVersionInfo(versionName = "1.0.0", versionCode = 1L)
        }
    }

    private fun getAndroidApplicationContext(): Context {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentAppMethod = activityThreadClass.getMethod("currentApplication")
        val app = currentAppMethod.invoke(null) as? Application
        return app?.applicationContext ?: throw IllegalStateException("Android Context is null")
    }
}
