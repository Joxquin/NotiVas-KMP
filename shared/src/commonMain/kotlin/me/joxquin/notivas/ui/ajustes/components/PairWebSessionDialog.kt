package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.InputField as MiuixInputField
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PairWebSessionDialog(
    themeStyle: AppThemeStyle,
    onDismissRequest: () -> Unit,
    onAuthorizePairing: suspend (pin: String) -> Result<Unit>
) {
    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val handleAuthorize = {
        if (pin.length == 6) {
            isLoading = true
            errorMessage = null
            scope.launch {
                val res = onAuthorizePairing(pin)
                isLoading = false
                if (res.isSuccess) {
                    isSuccess = true
                } else {
                    errorMessage = res.exceptionOrNull()?.message
                }
            }
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowDialog(
            show = true,
            onDismissRequest = onDismissRequest,
            title = stringResource(Res.string.pair_web_dialog_title),
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                            Text(
                                text = stringResource(Res.string.pair_web_success),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                        MiuixTextButton(
                            text = stringResource(Res.string.cancel),
                            onClick = onDismissRequest,
                            modifier = Modifier.fillMaxWidth(),
                            colors = MiuixButtonDefaults.textButtonColorsPrimary()
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.pair_web_dialog_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )

                        MiuixInputField(
                            query = pin,
                            onQueryChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                            onSearch = { handleAuthorize() },
                            expanded = false,
                            onExpandedChange = {},
                            label = stringResource(Res.string.pair_web_input_placeholder),
                            modifier = Modifier.fillMaxWidth(),
                            color = MiuixTheme.colorScheme.surfaceContainerHigh
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: stringResource(Res.string.pair_web_error_invalid_pin),
                                style = MaterialTheme.typography.bodySmall,
                                color = MiuixTheme.colorScheme.error
                            )
                        }

                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(
                                    text = stringResource(Res.string.pair_web_authorizing),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiuixTextButton(
                                text = stringResource(Res.string.cancel),
                                onClick = onDismissRequest,
                                modifier = Modifier.weight(1f)
                            )
                            MiuixTextButton(
                                text = stringResource(Res.string.pair_web_action_authorize),
                                onClick = { handleAuthorize() },
                                modifier = Modifier.weight(1f),
                                enabled = pin.length == 6 && !isLoading,
                                colors = MiuixButtonDefaults.textButtonColorsPrimary()
                            )
                        }
                    }
                }
            }
        )
    } else {
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
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
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
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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

