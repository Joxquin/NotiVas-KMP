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
import com.google.firebase.FirebaseApp
import me.joxquin.notivas.background.BackgroundSyncScheduler
import me.joxquin.notivas.cloud.FirebaseCloudBackend
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.util.linkCloudBackupWithLastGoogleAccount

import me.joxquin.notivas.ui.navigation.NotificationNavHandler
import me.joxquin.notivas.ui.navigation.NotificationNavTarget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        me.joxquin.notivas.data.local.FileStorageProvider.init(this)

        // FirebaseApp solo existe si el build incluyó google-services.json.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            AppModule.simulationCloudSync.start(FirebaseCloudBackend(applicationContext))
            linkCloudBackupWithLastGoogleAccount(applicationContext)
        }

        if (AppModule.preferencesManager.getIsOnboardingCompleted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
        }

        val syncInterval = AppModule.preferencesManager.syncIntervalMinutes
        BackgroundSyncScheduler.schedulePeriodicSync(15L)

        handleIntent(intent)

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
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return
        if (intent.getStringExtra("action") == "relogin") {
            AppModule.preferencesManager.setSessionExpired(true)
        }
        val navAction = intent.getStringExtra("extra_nav_action") ?: return
        val targetId = intent.getLongExtra("extra_target_id", -1L).takeIf { it != -1L }
        val secondaryId = intent.getLongExtra("extra_secondary_id", -1L).takeIf { it != -1L }
        val courseName = intent.getStringExtra("extra_course_name")

        val target = when (navAction) {
            "assignment" -> targetId?.let { NotificationNavTarget.Assignment(it, secondaryId, courseName) }
            "forum" -> targetId?.let { NotificationNavTarget.Forum(it, secondaryId) }
            "recent_activity" -> NotificationNavTarget.RecentActivity
            "schedule" -> NotificationNavTarget.Schedule
            "course" -> targetId?.let { NotificationNavTarget.Course(it) }
            else -> null
        }
        if (target != null) {
            NotificationNavHandler.navigateTo(target)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}