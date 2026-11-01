package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.repository.CopilotMessageItem
import me.joxquin.notivas.data.repository.CopilotRole
import me.joxquin.notivas.data.repository.CopilotSource
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.util.DateTimeUtil
import me.joxquin.notivas.util.PlatformShareHandler

sealed interface CopilotContentBlock {
    data class TextBlock(val content: String) : CopilotContentBlock
    object DividerBlock : CopilotContentBlock
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : CopilotContentBlock
    data class ResourceCardBlock(
        val type: ResourceCardType,
        val title: String,
        val courseName: String? = null,
        val assignmentId: Long? = null,
        val forumTopicId: Long? = null,
        val url: String? = null,
        val dueDate: String? = null,
        val pointsPossible: Double? = null
    ) : CopilotContentBlock
}

@Composable
fun CopilotMessageBubble(
    message: CopilotMessageItem,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onRegenerate: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
    animateReveal: Boolean = false,
    onAnimationFinished: ((String) -> Unit)? = null,
    onOpenAssignmentDraft: ((Long, String) -> Unit)? = null,
    onOpenForoDetail: ((Long) -> Unit)? = null,
    knownAssignments: List<Assignment> = emptyList(),
    courses: List<Course> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isUser = message.role == CopilotRole.USER
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Animación progresiva suave para respuestas recién emitidas por el asistente
    var revealedLength by remember(message.id) {
        mutableStateOf(if (animateReveal && !isUser) 0 else message.text.length)
    }

    LaunchedEffect(message.id, message.text, animateReveal) {
        if (animateReveal && !isUser && revealedLength < message.text.length) {
            val totalChars = message.text.length
            val step = (totalChars / 25).coerceIn(3, 22)
            while (revealedLength < totalChars) {
                delay(18)
                revealedLength = (revealedLength + step).coerceAtMost(totalChars)
            }
            onAnimationFinished?.invoke(message.id)
        } else {
            revealedLength = message.text.length
            onAnimationFinished?.invoke(message.id)
        }
    }

    val displayedText = remember(message.text, revealedLength) {
        if (revealedLength >= message.text.length) message.text else message.text.take(revealedLength)
    }
    val blocks = remember(displayedText, knownAssignments, courses) {
        parseMessageBlocks(displayedText, knownAssignments, courses)
    }

    val onCopyClick: () -> Unit = {
        clipboardManager.setText(AnnotatedString(message.text))
        copied = true
        scope.launch {
            delay(2000)
            copied = false
        }
    }

    if (isUser) {
        // Mensaje del usuario: Conserva la burbuja flotante alineada a la derecha
        if (themeStyle == AppThemeStyle.MIUIX) {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .wrapContentWidth(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary,
                                cornerRadius = 18.dp
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            SelectionContainer {
                                Text(
                                    text = message.text,
                                    style = MiuixTheme.textStyles.body1.copy(fontSize = 15.5.sp),
                                    color = MiuixTheme.colorScheme.onPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .squircleSurface(
                                            color = Color.White.copy(alpha = 0.2f),
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
                                            contentDescription = "Copiar mensaje",
                                            tint = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (copied) "Copiado" else "Copiar",
                                            style = MiuixTheme.textStyles.footnote2.copy(fontSize = 11.sp),
                                            color = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Material 3 User Bubble
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .wrapContentWidth(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            SelectionContainer {
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
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
                                            contentDescription = "Copiar mensaje",
                                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (copied) "Copiado" else "Copiar",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Respuesta del Copilot: FUERA DE BURBUJA (Estilo Google Gemini / NotiVas Redesign)
        // Se extiende a lo ancho de la vista sin recuadros ni bordes artificiales
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 1. Bloques de contenido Markdown (Texto, Separadores, Tablas)
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blocks.forEach { block ->
                        when (block) {
                            is CopilotContentBlock.TextBlock -> {
                                Text(
                                    text = formatMarkdown(block.content),
                                    style = if (themeStyle == AppThemeStyle.MIUIX) {
                                        MiuixTheme.textStyles.body1.copy(fontSize = 16.sp)
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                    },
                                    color = if (themeStyle == AppThemeStyle.MIUIX) {
                                        MiuixTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    lineHeight = 24.sp
                                )
                            }
                            is CopilotContentBlock.DividerBlock -> {
                                CopilotDividerComposable(themeStyle = themeStyle)
                            }
                            is CopilotContentBlock.TableBlock -> {
                                CopilotTableComposable(
                                    table = block,
                                    themeStyle = themeStyle
                                )
                            }
                            is CopilotContentBlock.ResourceCardBlock -> {
                                CopilotResourceCard(
                                    card = block,
                                    themeStyle = themeStyle,
                                    onOpenAssignmentDraft = onOpenAssignmentDraft,
                                    onOpenForoDetail = onOpenForoDetail
                                )
                            }
                        }
                    }
                }
            }

            // 2. Feedback de acción si aplica (ej: "Grupo de simulación creado")
            if (!message.actionFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                if (themeStyle == AppThemeStyle.MIUIX) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                cornerRadius = 8.dp
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = message.actionFeedback,
                                style = MiuixTheme.textStyles.body2.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = message.actionFeedback,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 3. Fuentes citadas de Canvas LMS (Acordeón desplegable)
            if (message.sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CopilotSourcesAccordion(
                    sources = message.sources,
                    themeStyle = themeStyle
                )
            }

            // 4. Barra de Acciones debajo del mensaje (Copiar, Compartir, Regenerar) y Disclaimer
            // Revelación fluida una vez que el texto termina de fluir
            AnimatedVisibility(
                visible = revealedLength >= message.text.length,
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Copiar
                        CopilotActionButton(
                            icon = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            label = if (copied) "Copiado" else "Copiar",
                            themeStyle = themeStyle,
                            onClick = onCopyClick
                        )

                        // Botón Compartir
                        CopilotActionButton(
                            icon = Icons.Default.Share,
                            label = "Compartir",
                            themeStyle = themeStyle,
                            onClick = {
                                if (onShare != null) {
                                    onShare(message.text)
                                } else {
                                    PlatformShareHandler.shareText(message.text)
                                }
                            }
                        )

                        // Botón Regenerar
                        if (onRegenerate != null) {
                            CopilotActionButton(
                                icon = Icons.Default.Refresh,
                                label = "Regenerar",
                                themeStyle = themeStyle,
                                onClick = onRegenerate
                            )
                        }
                    }

                    // 5. Descargo de responsabilidad sutil (Disclaimer)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "NotiVas Copilot es una IA y puede cometer errores.",
                        style = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.textStyles.footnote2.copy(fontSize = 11.5.sp)
                        } else {
                            MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp)
                        },
                        color = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CopilotActionButton(
    icon: ImageVector,
    label: String,
    themeStyle: AppThemeStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = modifier
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceVariant,
                    cornerRadius = 8.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.footnote2.copy(fontSize = 11.sp),
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CopilotDividerComposable(
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(1.dp)
                .background(MiuixTheme.colorScheme.dividerLine)
        )
    } else {
        HorizontalDivider(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun CopilotTableComposable(
    table: CopilotContentBlock.TableBlock,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val borderColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.dividerLine
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val headerBg = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val alternateBg = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
    }

    val textColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .horizontalScroll(scrollState)
    ) {
        Column {
            // Fila de encabezado
            Row(
                modifier = Modifier
                    .background(headerBg)
                    .border(
                        width = 0.5.dp,
                        color = borderColor
                    )
            ) {
                table.headers.forEachIndexed { colIndex, header ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 100.dp, max = 220.dp)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = formatInlineText(header),
                            style = if (themeStyle == AppThemeStyle.MIUIX) {
                                MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            },
                            color = textColor
                        )
                    }
                }
            }

            // Filas de datos
            table.rows.forEachIndexed { rowIndex, row ->
                val rowBg = if (rowIndex % 2 == 1) alternateBg else Color.Transparent
                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .border(
                            width = 0.5.dp,
                            color = borderColor
                        )
                ) {
                    val colCount = table.headers.size
                    for (colIndex in 0 until colCount) {
                        val cellContent = row.getOrNull(colIndex) ?: ""
                        Box(
                            modifier = Modifier
                                .widthIn(min = 100.dp, max = 220.dp)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = formatInlineText(cellContent),
                                style = if (themeStyle == AppThemeStyle.MIUIX) {
                                    MiuixTheme.textStyles.footnote2
                                } else {
                                    MaterialTheme.typography.bodySmall
                                },
                                color = textColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parsea el texto en bloques estructurados: párrafos, divisores, tablas Markdown y tarjetas interactivas.
 */
fun parseMessageBlocks(
    rawText: String,
    knownAssignments: List<Assignment> = emptyList(),
    courses: List<Course> = emptyList()
): List<CopilotContentBlock> {
    val lines = rawText.lines()
    val blocks = mutableListOf<CopilotContentBlock>()
    val currentParagraph = mutableListOf<String>()

    fun flushParagraph() {
        if (currentParagraph.isNotEmpty()) {
            val text = currentParagraph.joinToString("\n").trim()
            if (text.isNotEmpty()) {
                blocks.add(CopilotContentBlock.TextBlock(text))
            }
            currentParagraph.clear()
        }
    }

    val emittedAssignmentIds = mutableSetOf<Long>()
    val emittedTitles = mutableSetOf<String>()
    var pendingAssignmentCard: CopilotContentBlock.ResourceCardBlock? = null
    var currentCourseContext: Course? = null

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Detectar si la línea establece un contexto de curso (ej: ### 🧪 1. Tecnologías Emergentes)
        if (trimmed.startsWith("#") || trimmed.contains("curso:", ignoreCase = true)) {
            val normHeading = normalizeSearchString(trimmed)
            val matchedCourse = courses.find { c ->
                val normC = normalizeSearchString(c.name)
                normC.length >= 4 && normHeading.contains(normC)
            }
            if (matchedCourse != null) {
                currentCourseContext = matchedCourse
            }
        }

        // 2. Detectar etiqueta explícita de tarjeta: ::card[...]:: (incluso si viene precedida por viñetas •, *, - o espacios)
        val cardTagRegex = Regex("""(?:\s*[•*\-]?\s*)?::card\[(.*?)\]::\s*""")
        val cardTagMatch = cardTagRegex.find(trimmed)
        if (cardTagMatch != null) {
            val lineWithoutCard = trimmed.replace(cardTagRegex, "").trim()
            if (lineWithoutCard.isNotEmpty() && lineWithoutCard !in listOf("•", "*", "-", "• ", "* ", "- ")) {
                currentParagraph.add(lineWithoutCard)
            }
            flushParagraph()
            if (pendingAssignmentCard != null) {
                blocks.add(pendingAssignmentCard!!)
                pendingAssignmentCard = null
            }
            val cardBlock = parseCardTag(cardTagMatch.groupValues[1], knownAssignments, courses, currentCourseContext)
            if (cardBlock != null) {
                cardBlock.assignmentId?.let { emittedAssignmentIds.add(it) }
                emittedTitles.add(normalizeSearchString(cardBlock.title))
                blocks.add(cardBlock)
            }
            i++
            continue
        }

        // 3. Detectar divisor horizontal: 3 o más guiones, asteriscos o guiones bajos
        val isHorizontalRule = trimmed.matches(Regex("^([\\-*_])(\\s*\\1){2,}$"))
        if (isHorizontalRule) {
            flushParagraph()
            if (pendingAssignmentCard != null) {
                blocks.add(pendingAssignmentCard!!)
                pendingAssignmentCard = null
            }
            blocks.add(CopilotContentBlock.DividerBlock)
            i++
            continue
        }

        // 4. Detectar posible tabla Markdown
        if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size) {
            val nextTrimmed = lines[i + 1].trim()
            val isSeparator = isTableSeparatorRow(nextTrimmed)
            if (isSeparator) {
                flushParagraph()
                if (pendingAssignmentCard != null) {
                    blocks.add(pendingAssignmentCard!!)
                    pendingAssignmentCard = null
                }
                val headers = parseTableRow(trimmed)
                i += 2 // Saltar encabezado y separador

                val tableRows = mutableListOf<List<String>>()
                while (i < lines.size) {
                    val rowLine = lines[i].trim()
                    if (rowLine.startsWith("|") && rowLine.endsWith("|")) {
                        tableRows.add(parseTableRow(rowLine))
                        i++
                    } else {
                        break
                    }
                }
                blocks.add(CopilotContentBlock.TableBlock(headers = headers, rows = tableRows))
                continue
            }
        }

        // 5. Autodetección de tareas en viñetas o títulos
        val isNewSectionOrBullet = trimmed.startsWith("#") ||
                line.startsWith("* ") || line.startsWith("- ") ||
                line.matches(Regex("""^\s{0,2}\d+\.\s+.*"""))

        if (isNewSectionOrBullet && pendingAssignmentCard != null) {
            // Se inicia un nuevo elemento o sección; emitir la tarjeta pendiente acumulada
            flushParagraph()
            blocks.add(pendingAssignmentCard!!)
            pendingAssignmentCard = null
        }

        // Cotejar si esta línea menciona alguna tarea conocida aún no emitida
        if (knownAssignments.isNotEmpty()) {
            val matchedAssignment = knownAssignments.firstOrNull { assignment ->
                assignment.id !in emittedAssignmentIds &&
                        normalizeSearchString(assignment.name) !in emittedTitles &&
                        isAssignmentMentionedInLine(trimmed, assignment, currentCourseContext)
            }
            if (matchedAssignment != null) {
                val course = courses.find { it.id == matchedAssignment.courseId } ?: currentCourseContext
                val cleanDue = formatCleanDueDate(matchedAssignment.dueAt)
                pendingAssignmentCard = CopilotContentBlock.ResourceCardBlock(
                    type = ResourceCardType.ASSIGNMENT,
                    title = matchedAssignment.name,
                    courseName = course?.name,
                    assignmentId = matchedAssignment.id,
                    url = matchedAssignment.htmlUrl,
                    dueDate = cleanDue,
                    pointsPossible = matchedAssignment.pointsPossible
                )
                emittedAssignmentIds.add(matchedAssignment.id)
                emittedTitles.add(normalizeSearchString(matchedAssignment.name))
            }
        }

        currentParagraph.add(line)
        i++
    }

    flushParagraph()
    if (pendingAssignmentCard != null) {
        blocks.add(pendingAssignmentCard!!)
    }

    return blocks
}

private fun parseCardTag(
    rawTag: String,
    knownAssignments: List<Assignment>,
    courses: List<Course>,
    currentCourseContext: Course?
): CopilotContentBlock.ResourceCardBlock? {
    val regex = Regex("""(\w+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^,\s\]]+))""")
    val matches = regex.findAll(rawTag)
    val map = mutableMapOf<String, String>()
    for (m in matches) {
        val key = m.groupValues[1].lowercase()
        val value = m.groupValues[2].ifEmpty { m.groupValues[3].ifEmpty { m.groupValues[4] } }
        map[key] = value
    }

    val typeStr = map["type"]?.lowercase() ?: "assignment"
    val type = when (typeStr) {
        "forum", "foro", "discussion" -> ResourceCardType.FORUM
        "material", "module", "modulo" -> ResourceCardType.MATERIAL
        "link", "url", "web" -> ResourceCardType.LINK
        else -> ResourceCardType.ASSIGNMENT
    }

    val title = map["title"] ?: map["name"] ?: ""
    if (title.isBlank()) return null

    var courseName = map["course"] ?: map["course_name"] ?: currentCourseContext?.name
    var assignmentId = map["id"]?.toLongOrNull() ?: map["assignment_id"]?.toLongOrNull()
    var forumTopicId = map["id"]?.toLongOrNull() ?: map["topic_id"]?.toLongOrNull()
    var url = map["url"] ?: map["link"]
    var dueDate = map["due"] ?: map["due_date"] ?: map["due_at"]
    var pointsPossible = map["points"]?.toDoubleOrNull() ?: map["points_possible"]?.toDoubleOrNull()

    if (type == ResourceCardType.ASSIGNMENT) {
        val explicitCourse = if (!courseName.isNullOrBlank()) {
            courses.find { c ->
                val normC = normalizeSearchString(c.name)
                val normTarget = normalizeSearchString(courseName ?: "")
                normC.isNotEmpty() && normTarget.isNotEmpty() && (normC.contains(normTarget) || normTarget.contains(normC))
            }
        } else currentCourseContext

        val matchedAssignment = knownAssignments.find { a ->
            (assignmentId != null && a.id == assignmentId) ||
            isAssignmentMentionedInLine(title, a, explicitCourse)
        }
        if (matchedAssignment != null) {
            if (assignmentId == null) assignmentId = matchedAssignment.id
            if (url.isNullOrBlank()) url = matchedAssignment.htmlUrl
            if (dueDate.isNullOrBlank() && !matchedAssignment.dueAt.isNullOrBlank()) {
                dueDate = formatCleanDueDate(matchedAssignment.dueAt)
            }
            if (courseName.isNullOrBlank()) {
                courseName = explicitCourse?.name ?: courses.find { it.id == matchedAssignment.courseId }?.name
            }
            if (pointsPossible == null) {
                pointsPossible = matchedAssignment.pointsPossible
            }
        }
    }

    return CopilotContentBlock.ResourceCardBlock(
        type = type,
        title = title,
        courseName = courseName,
        assignmentId = if (type == ResourceCardType.ASSIGNMENT) assignmentId else null,
        forumTopicId = if (type == ResourceCardType.FORUM) forumTopicId else null,
        url = url,
        dueDate = dueDate,
        pointsPossible = pointsPossible
    )
}

private fun isAssignmentMentionedInLine(
    line: String,
    assignment: Assignment,
    currentCourseContext: Course?
): Boolean {
    // Si tenemos contexto de curso y la tarea es de otro curso, no asociar
    if (currentCourseContext != null && assignment.courseId != currentCourseContext.id) {
        return false
    }

    val normLine = normalizeSearchString(line)
    val normName = normalizeSearchString(assignment.name)

    // Si ambos tienen números explícitos (ej: S11 vs S1, o Lab 1 vs Lab 11), deben coincidir
    val lineNumbers = Regex("""\b\d+\b""").findAll(normLine).map { it.value }.toSet()
    val nameNumbers = Regex("""\b\d+\b""").findAll(normName).map { it.value }.toSet()
    if (nameNumbers.isNotEmpty() && lineNumbers.isNotEmpty()) {
        val sharedNumbers = nameNumbers.intersect(lineNumbers)
        if (sharedNumbers.isEmpty()) {
            return false
        }
    }

    if (normName.length >= 6 && normLine.contains(normName)) {
        return true
    }
    val stopWords = setOf("del", "las", "los", "por", "para", "con", "de", "la", "el", "en", "lab", "laboratorio", "entrega", "consigna", "semana")
    val tokens = normName.split(" ").filter { it.length >= 3 && it !in stopWords }
    if (tokens.size >= 2) {
        val matchedTokens = tokens.count { normLine.contains(it) }
        val requiredTokens = (tokens.size * 0.7).toInt().coerceAtLeast(2)
        if (matchedTokens >= requiredTokens) {
            if (currentCourseContext == null || currentCourseContext.id == assignment.courseId) {
                return true
            }
        }
    }
    return false
}

private fun normalizeSearchString(text: String): String {
    return text.lowercase()
        .replace(Regex("""[*#_`~:>\-]"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
}

fun formatCleanDueDate(dueAt: String?): String {
    if (dueAt.isNullOrBlank()) return "Sin fecha límite"
    val comp = DateTimeUtil.parseIsoToComponents(dueAt) ?: return dueAt
    val dayName = when (DateTimeUtil.dayOfWeek(comp.date)) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> ""
    }
    val timeStr = "${comp.hour.toString().padStart(2, '0')}:${comp.minute.toString().padStart(2, '0')}"
    return if (dayName.isNotEmpty()) {
        "$dayName ${comp.date.toIsoDateString()} $timeStr"
    } else {
        "${comp.date.toIsoDateString()} $timeStr"
    }
}

private fun isTableSeparatorRow(line: String): Boolean {
    if (!line.startsWith("|") || !line.endsWith("|")) return false
    val inner = line.removeSurrounding("|")
    val cells = inner.split("|")
    if (cells.isEmpty()) return false
    return cells.all { cell ->
        val c = cell.trim()
        c.matches(Regex("^:?-+:?$"))
    }
}

private fun parseTableRow(line: String): List<String> {
    val inner = line.removeSurrounding("|")
    return inner.split("|").map { it.trim() }
}

private fun formatInlineText(content: String): AnnotatedString {
    return buildAnnotatedString {
        appendInlineMarkdown(content)
    }
}

@Composable
fun CopilotSourcesAccordion(
    sources: List<CopilotSource>,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    cornerRadius = 10.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = !expanded }
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Fuentes de Canvas LMS (${sources.size})",
                        style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Bold),
                        color = MiuixTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sources.forEach { source ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceVariant,
                                    cornerRadius = 6.dp
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Column {
                                Text(
                                    text = source.title,
                                    style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Medium),
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = source.detail,
                                    style = MiuixTheme.textStyles.footnote2.copy(fontSize = 10.sp),
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Fuentes consultadas (${sources.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sources.forEach { source ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = source.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = source.detail,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parsea el texto en Markdown a un [AnnotatedString] de Compose con soporte para negritas (**texto**),
 * cursivas (*texto*), código en línea (`código`), bloques de código (```) y listas (•).
 */
fun formatMarkdown(text: String): AnnotatedString {
    val lines = text.split("\n")
    return buildAnnotatedString {
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        lines.forEachIndexed { index, line ->
            if (line.trim().startsWith("```")) {
                if (!inCodeBlock) {
                    inCodeBlock = true
                    codeBlockLines.clear()
                } else {
                    inCodeBlock = false
                    val codeContent = codeBlockLines.joinToString("\n")
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.5.sp,
                            background = Color(0x22888888)
                        )
                    ) {
                        append("  $codeContent  \n")
                    }
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                return@forEachIndexed
            }

            var formattedLine = line
            var headerLevel = 0

            val trimmedLeading = formattedLine.trimStart()
            if (trimmedLeading.startsWith("###### ")) {
                formattedLine = trimmedLeading.removePrefix("###### ")
                headerLevel = 6
            } else if (trimmedLeading.startsWith("##### ")) {
                formattedLine = trimmedLeading.removePrefix("##### ")
                headerLevel = 5
            } else if (trimmedLeading.startsWith("#### ")) {
                formattedLine = trimmedLeading.removePrefix("#### ")
                headerLevel = 4
            } else if (trimmedLeading.startsWith("### ")) {
                formattedLine = trimmedLeading.removePrefix("### ")
                headerLevel = 3
            } else if (trimmedLeading.startsWith("## ")) {
                formattedLine = trimmedLeading.removePrefix("## ")
                headerLevel = 2
            } else if (trimmedLeading.startsWith("# ")) {
                formattedLine = trimmedLeading.removePrefix("# ")
                headerLevel = 1
            }

            val bulletRegex = Regex("^(\\s*)([*-])\\s+(.*)$")
            val bulletMatch = bulletRegex.find(formattedLine)
            val leadingIndent: String
            val lineContent: String
            if (bulletMatch != null) {
                leadingIndent = bulletMatch.groupValues[1]
                lineContent = bulletMatch.groupValues[3]
                append("$leadingIndent• ")
            } else {
                lineContent = formattedLine
            }

            if (headerLevel > 0) {
                val headerFontSize = when (headerLevel) {
                    1 -> 19.sp
                    2 -> 18.sp
                    3 -> 17.5.sp
                    4 -> 16.5.sp
                    5 -> 16.sp
                    else -> 15.5.sp
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = headerFontSize)) {
                    appendInlineMarkdown(lineContent)
                }
            } else {
                appendInlineMarkdown(lineContent)
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            val codeContent = codeBlockLines.joinToString("\n")
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.5.sp,
                    background = Color(0x22888888)
                )
            ) {
                append("  $codeContent  ")
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(content: String) {
    val inlinePattern = Regex("(\\*\\*([^*]+)\\*\\*)|(`([^`]+)`)|(\\*([^*]+)\\*)")
    var lastIndex = 0

    for (match in inlinePattern.findAll(content)) {
        if (match.range.first > lastIndex) {
            append(content.substring(lastIndex, match.range.first))
        }

        when {
            match.groups[2] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groups[2]!!.value)
                }
            }
            match.groups[4] != null -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        background = Color(0x22888888)
                    )
                ) {
                    append(" ${match.groups[4]!!.value} ")
                }
            }
            match.groups[6] != null -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groups[6]!!.value)
                }
            }
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < content.length) {
        append(content.substring(lastIndex))
    }
}
