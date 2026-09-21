package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CopilotInputBar(
    inputText: String,
    isLoading: Boolean,
    enabled: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onTriggerMention: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        // Estilo Stitch Miuix: Cápsula Flotante con '@' y botón de envío azul HyperOS
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        cornerRadius = 9999.dp
                    )
                    .squircleBorder(
                        width = 1.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 9999.dp
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de Mención @
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                cornerRadius = 9999.dp
                            )
                            .clickable(onClick = onTriggerMention),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "@",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Campo de Texto Miuix
                    Box(modifier = Modifier.weight(1f)) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Pregunta a Copilot o usa @curso...",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.7f)
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { if (!isLoading && enabled) onSend() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Botón de Envío Miuix
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .squircleSurface(
                                color = if (inputText.isNotBlank() && !isLoading && enabled) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.surfaceContainerHigh
                                },
                                cornerRadius = 9999.dp
                            )
                            .clickable(
                                enabled = inputText.isNotBlank() && !isLoading && enabled,
                                onClick = onSend
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (inputText.isNotBlank() && !isLoading && enabled) {
                                MiuixTheme.colorScheme.onPrimary
                            } else {
                                MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Modo Material Design 3 Expressive
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onTriggerMention,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Mencionar curso",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Pregunta a Copilot...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = inputText,
                                onValueChange = onInputChange,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { if (!isLoading && enabled) onSend() }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (inputText.isNotBlank() && !isLoading && enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = onSend,
                        enabled = inputText.isNotBlank() && !isLoading && enabled
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (inputText.isNotBlank() && !isLoading && enabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
