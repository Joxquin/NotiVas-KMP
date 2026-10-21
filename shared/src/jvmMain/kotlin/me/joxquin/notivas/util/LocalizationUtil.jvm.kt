package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import me.joxquin.notivas.data.local.AppLanguage
import java.util.Locale

@Composable
actual fun ProvideAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    LaunchedEffect(language) {
        val targetLocale = when (language) {
            AppLanguage.SYSTEM -> Locale.getDefault()
            AppLanguage.SPANISH -> Locale("es")
            AppLanguage.ENGLISH -> Locale("en")
        }
        Locale.setDefault(targetLocale)
    }

    content()
}
