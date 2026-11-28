package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.util.MathFormatter
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

/**
 * Componente visual para renderizar fórmulas y cálculos matemáticos en su propio párrafo o bloque
 * contenedor independiente ("párrafo propio"), garantizando alta legibilidad, estética visual dual
 * (HyperOS MIUIX y Material Design 3) y respeto estricto al sistema dinámico de color Monet.
 */
@Composable
fun CopilotMathBlockComposable(
    formulaText: String,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val cleanMathText = remember(formulaText) {
        MathFormatter.formatLatexToMathText(formulaText)
    }

    val onCopyClick: () -> Unit = {
        clipboardManager.setText(AnnotatedString(cleanMathText))
        copied = true
        scope.launch {
            delay(2000)
            copied = false
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        // Implementación HyperOS MIUIX con Squircle Surface y tokens Monet oficiales
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 14.dp
                )
                .squircleBorder(
                    color = MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.5f),
                    width = 1.dp,
                    cornerRadius = 14.dp
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    cornerRadius = 6.dp
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = stringResource(Res.string.cd_formula),
                                    tint = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.copilot_math_badge),
                                    style = MiuixTheme.textStyles.footnote2.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Botón para copiar fórmula limpia
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                cornerRadius = 6.dp
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onCopyClick
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = stringResource(Res.string.cd_copy_formula),
                                tint = if (copied) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (copied) "Copiado" else "Copiar",
                                style = MiuixTheme.textStyles.footnote2.copy(fontSize = 11.sp),
                                color = if (copied) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Text(
                        text = cleanMathText,
                        style = MiuixTheme.textStyles.body1.copy(
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.3.sp
                        ),
                        color = MiuixTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    } else {
        // Implementación Material Design 3 con Surface y tokens Monet oficiales
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = stringResource(Res.string.cd_formula),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(Res.string.copilot_math_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onCopyClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = stringResource(Res.string.cd_copy_formula),
                                tint = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (copied) "Copiado" else "Copiar",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Text(
                        text = cleanMathText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.3.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}
