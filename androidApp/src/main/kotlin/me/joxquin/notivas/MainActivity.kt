package me.joxquin.notivas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.joxquin.notivas.background.BackgroundSyncScheduler
import me.joxquin.notivas.di.AppModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        me.joxquin.notivas.data.local.FileStorageProvider.init(this)

        if (AppModule.preferencesManager.getIsOnboardingCompleted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
        }

        val syncInterval = AppModule.preferencesManager.syncIntervalMinutes
        BackgroundSyncScheduler.schedulePeriodicSync(15L)

        if (intent?.getStringExtra("action") == "relogin") {
            AppModule.preferencesManager.setSessionExpired(true)
        }

        setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this
            ) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("action") == "relogin") {
            AppModule.preferencesManager.setSessionExpired(true)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}