package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLengthReject
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator as M3CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.pair_web_action_authorize
import me.joxquin.notivas.shared.resources.pair_web_authorizing
import me.joxquin.notivas.shared.resources.pair_web_dialog_desc
import me.joxquin.notivas.shared.resources.pair_web_dialog_title
import me.joxquin.notivas.shared.resources.pair_web_error_invalid_pin
import me.joxquin.notivas.shared.resources.pair_web_input_placeholder
import me.joxquin.notivas.shared.resources.pair_web_success
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.glass.GlassTextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PairWebSessionDialog(
    themeStyle: AppThemeStyle,
    onDismissRequest: () -> Unit,
    onAuthorizePairing: suspend (pin: String) -> Result<Unit>,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    val pinState = rememberTextFieldState()
    val pin = pinState.text.toString()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(show) {
        if (show) dismissed = false
    }

    val handleDismiss = {
        dismissed = true
        pinState.edit { replace(0, length, "") }
        isLoading = false
        errorMessage = null
        isSuccess = false
        onDismissRequest()
    }

    val handleAuthorize = {
        if (pin.length == 6) {
            isLoading = true
            errorMessage = null
            scope.launch {
                val res = onAuthorizePairing(pin)
                if (!dismissed) {
                    isLoading = false
                    if (res.isSuccess) {
                        isSuccess = true
                    } else {
                        errorMessage = res.exceptionOrNull()?.message
                    }
                }
            }
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            onDismissRequest = handleDismiss,
            title = stringResource(Res.string.pair_web_dialog_title),
            summary = stringResource(Res.string.pair_web_dialog_desc),
            onDismissFinished = handleDismiss,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSuccess) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.pair_web_success),
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                    GlassDialogActions {
                        GlassDialogButton(
                            onClick = handleDismiss,
                            modifier = Modifier.weight(1f),
                            role = GlassDialogButtonRole.Primary,
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.cancel),
                                style = MiuixTheme.textStyles.button,
                            )
                        }
                    }
                } else {
                    GlassTextField(
                        state = pinState,
                        label = stringResource(Res.string.pair_web_input_placeholder),
                        useLabelAsPlaceholder = true,
                        inputTransformation = PinInputTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (errorMessage != null) {
                        MiuixText(
                            text = errorMessage ?: stringResource(Res.string.pair_web_error_invalid_pin),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.error
                        )
                    }

                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiuixCircularProgressIndicator(modifier = Modifier.size(16.dp), size = 16.dp)
                            MiuixText(
                                text = stringResource(Res.string.pair_web_authorizing),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }

                    GlassDialogActions {
                        GlassDialogButton(
                            onClick = handleDismiss,
                            modifier = Modifier.weight(1f),
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.cancel),
                                style = MiuixTheme.textStyles.button,
                            )
                        }
                        GlassDialogButton(
                            onClick = { handleAuthorize() },
                            modifier = Modifier.weight(1f),
                            enabled = pin.length == 6 && !isLoading,
                            role = GlassDialogButtonRole.Primary,
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.pair_web_action_authorize),
                                style = MiuixTheme.textStyles.button,
                            )
                        }
                    }
                }
            }
        }
    } else if (themeStyle != AppThemeStyle.MIUIX && show) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = {
                Icon(
                    imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Devices,
                    contentDescription = null,
                    tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.pair_web_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isSuccess) {
                        Text(
                            text = stringResource(Res.string.pair_web_success),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.pair_web_dialog_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = pin,
                            onValueChange = { value ->
                                pinState.edit {
                                    replace(0, length, value.filter { it.isDigit() }.take(6))
                                }
                            },
                            placeholder = {
                                Text(
                                    stringResource(Res.string.pair_web_input_placeholder),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                textAlign = TextAlign.Center,
                                letterSpacing = 6.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: stringResource(Res.string.pair_web_error_invalid_pin),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                M3CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(
                                    text = stringResource(Res.string.pair_web_authorizing),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isSuccess) {
                    Button(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }
                } else {
                    Button(
                        onClick = { handleAuthorize() },
                        enabled = pin.length == 6 && !isLoading
                    ) {
                        Text(stringResource(Res.string.pair_web_action_authorize))
                    }
                }
            },
            dismissButton = {
                if (!isSuccess) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        )
    }
}

/** PIN de 6 dígitos: rechaza caracteres no numéricos y limita la longitud como en el ejemplo. */
private val PinInputTransformation = InputTransformation {
    if (!asCharSequence().all { it.isDigit() }) revertAllChanges()
}.maxLengthReject(6)
