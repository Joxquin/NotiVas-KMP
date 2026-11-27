// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged
import top.yukonga.miuix.kmp.basic.TextFieldColors
import top.yukonga.miuix.kmp.glass.GlassTextField
import top.yukonga.miuix.kmp.glass.GlassTextFieldDefaults

/**
 * Puente de compatibilidad para formularios que aún gestionan el texto como [String].
 * Internamente usa el [GlassTextField] oficial con `TextFieldState`.
 */
@Composable
fun MiuixGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors? = null,
    useLabelAsPlaceholder: Boolean = false,
    outputTransformation: OutputTransformation? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val state = rememberTextFieldState(value)
    val currentOnValueChange = rememberUpdatedState(onValueChange)

    LaunchedEffect(value) {
        if (state.text.toString() != value) {
            state.setTextAndPlaceCursorAtEnd(value)
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .distinctUntilChanged()
            .collect { currentOnValueChange.value(it) }
    }

    GlassTextField(
        state = state,
        modifier = modifier,
        label = label,
        useLabelAsPlaceholder = useLabelAsPlaceholder,
        enabled = enabled,
        readOnly = readOnly,
        lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.Default,
        keyboardOptions = keyboardOptions,
        colors = colors ?: GlassTextFieldDefaults.textFieldColors(),
        outputTransformation = outputTransformation,
        trailingIcon = trailingIcon,
    )
}
