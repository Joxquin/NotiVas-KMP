package me.joxquin.notivas.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import me.joxquin.notivas.data.local.AppLanguage
import java.util.Locale

@Composable
actual fun ProvideAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current

    val targetLocale = remember(language) {
        when (language) {
            AppLanguage.SYSTEM -> {
                context.resources.configuration.locales.get(0) ?: Locale.getDefault()
            }
            AppLanguage.SPANISH -> Locale("es")
            AppLanguage.ENGLISH -> Locale("en")
        }
    }

    androidx.compose.runtime.LaunchedEffect(targetLocale) {
        Locale.setDefault(targetLocale)
    }

    val localizedContext = remember(context, targetLocale) {
        val config = Configuration(context.resources.configuration).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }
        context.createConfigurationContext(config)
    }

    val localizedConfig = remember(currentConfig, targetLocale) {
        Configuration(currentConfig).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig
    ) {
        content()
    }
}
