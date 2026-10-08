package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import me.joxquin.notivas.data.local.AppLanguage

@Composable
expect fun ProvideAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit
)
