import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Firebase se activa solo si existe google-services.json (proyecto de Google Cloud notivas-510404).
// Sin el archivo la app compila igual y el respaldo en la nube queda apagado.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebaseCrashlytics.get().pluginId)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "me.joxquin.notivas"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.joxquin.notivas"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 8
        versionName = "1.6.0"

        // IA incluida: la key llega al compilar desde el secreto NOTIVAS_AI_KEY (CI) o
        // notivas.aiKey en local.properties. Nunca va en el repositorio; vacía = cada
        // estudiante pone su propia key en Ajustes, como antes.
        val notivasAiKey = (System.getenv("NOTIVAS_AI_KEY")
            ?: rootProject.file("local.properties").takeIf { it.isFile }?.let { file ->
                Properties().apply { file.inputStream().use { load(it) } }.getProperty("notivas.aiKey")
            })?.trim().orEmpty()
        require(notivasAiKey.all { it.isLetterOrDigit() || it in "._-" }) { "NOTIVAS_AI_KEY has unexpected characters" }
        buildConfigField("String", "NOTIVAS_AI_KEY", "\"$notivasAiKey\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        getByName("debug") {
            val keystoreFile = file("debug.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("DEBUG_KEYSTORE_STORE_PASSWORD") ?: "android"
                keyAlias = System.getenv("DEBUG_KEYSTORE_KEY_ALIAS") ?: "androiddebugkey"
                keyPassword = System.getenv("DEBUG_KEYSTORE_KEY_PASSWORD") ?: "android"
            }
        }
        create("release") {
            // Release passwords come only from the environment (GitHub secrets in CI).
            // Without them the release build falls back to the debug signing config below.
            val keystoreFile = file("release.keystore")
            val releaseStorePassword = System.getenv("KEYSTORE_STORE_PASSWORD")
            val releaseKeyPassword = System.getenv("KEYSTORE_KEY_PASSWORD")
            if (keystoreFile.exists() && !releaseStorePassword.isNullOrBlank() && !releaseKeyPassword.isNullOrBlank()) {
                storeFile = keystoreFile
                storePassword = releaseStorePassword
                keyAlias = System.getenv("KEYSTORE_KEY_ALIAS")?.takeIf { it.isNotBlank() } ?: "releaseKey"
                keyPassword = releaseKeyPassword
            }
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")?.takeIf { it.storeFile != null }
                ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}