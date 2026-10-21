package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.joxquin.notivas.data.local.AppLanguage
import platform.Foundation.NSUserDefaults

@Composable
actual fun ProvideAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    LaunchedEffect(language) {
        val appleLanguages = when (language) {
            AppLanguage.SYSTEM -> null
            AppLanguage.SPANISH -> listOf("es")
            AppLanguage.ENGLISH -> listOf("en")
        }

        if (appleLanguages != null) {
            NSUserDefaults.standardUserDefaults.setObject(appleLanguages, "AppleLanguages")
            NSUserDefaults.standardUserDefaults.synchronize()
        }
    }

    content()
}
