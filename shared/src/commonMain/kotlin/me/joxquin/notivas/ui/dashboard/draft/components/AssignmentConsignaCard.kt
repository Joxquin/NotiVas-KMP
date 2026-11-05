package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class ConsignaIconType {
    CAPACITY,
    PROCEDURE,
    PREREQUISITE,
    SPECIFICATION,
    GENERAL
}

private val BASE_URL_REGEX = Regex("""^(https?://[^/]+)""")
private val H3_REGEX = Regex("""(?s)<h3[^>]*>(.*?)</h3>""")
private val NEXT_CARD_REGEX = Regex("""(?i)(<div[^>]*style=["'][^"']*(?:border-|background-color|display:\s*table)|<div[^>]*class=["'][^"']*card)""")
private val HTML_TAG_REGEX = Regex("<[^>]*>")
private val A_HREF_REGEX = Regex("""(?s)<a[^>]+href=["']([^"']+)["'][^>]*>(.*?)</a>""")
private val A_TAG_BODY_REGEX = Regex("""(?s)<a[^>]*>(.*?)</a>""")
private val LI_REGEX = Regex("""(?s)<li[^>]*>(.*?)</li>""")
private val LIST_CONTAINER_REGEX = Regex("""(?is)<(ol|ul)[^>]*>(.*?)</\1>""")
private val SCRIPT_STYLE_LINK_REGEX = Regex("""(?is)<script[^>]*>.*?</script>|<style[^>]*>.*?</style>|<link[^>]*>""")
private val SPAN_FONT_REGEX = Regex("""(?is)</?(?:span|font)[^>]*>""")
private val BR_REGEX = Regex("""(?is)<br\s*/?>""")
private val P_CLOSE_REGEX = Regex("""(?is)</p>""")
private val P_OPEN_REGEX = Regex("""(?is)<p[^>]*>""")
private val BOLD_TAG_REGEX = Regex("""(?is)<(?:strong|b)[^>]*>(.*?)</(?:strong|b)>""")
private val ITALIC_TAG_REGEX = Regex("""(?is)<(?:em|i)[^>]*>(.*?)</(?:em|i)>""")
private val BULLET_LABEL_SPLIT_REGEX = Regex("""^(?:\*\*(.*?)(?::\*\*|\*\*:)|(.*?):)\s*(.*)$""", RegexOption.DOT_MATCHES_ALL)
private val INLINE_MARKDOWN_LINK_REGEX = Regex("""(\*\*([^*]+)\*\*)|(\[([^\]]+)\]\(([^)]+)\))|(`([^`]+)`)|(\*([^*]+)\*)""")
private val IMG_REGEX = Regex("""(?i)<img[^>]+src=["']([^"']+)["'][^>]*>""")
private val WHITESPACE_REGEX = Regex("""\s+""")
private val EMOJI_REGEX = Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+""")

private val CONSIGNA_SECTIONS_CACHE = mutableMapOf<String, List<ConsignaSection>>()


data class ConsignaBullet(
    val label: String,
    val value: String,
    val isHighlighted: Boolean = false,
    val index: String? = null,
    val rawHtml: String? = null
)

data class ConsignaSection(
    val title: String,
    val iconType: ConsignaIconType,
    val introText: String,
    val downloadLink: Pair<String, String>? = null,
    val bullets: List<ConsignaBullet> = emptyList(),
    val otherLinks: List<Pair<String, String>> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val iconUrl: String? = null,
    val footerNotes: List<String> = emptyList(),
    val rawIntroHtml: String? = null
)

@Composable
fun AssignmentConsignaCard(
    assignment: Assignment,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val token = remember { AppModule.preferencesManager.getAccessToken() }
    val cookies = remember { AppModule.preferencesManager.getSessionCookies() }
    val baseUrl = remember(assignment.htmlUrl) {
        assignment.htmlUrl?.let { url ->
            BASE_URL_REGEX.find(url)?.value
        } ?: AppModule.preferencesManager.getUniversityUrl()
    }
    val sections = remember(assignment.description, baseUrl) {
        val key = "${assignment.id}_${assignment.description?.hashCode() ?: 0}_$baseUrl"
        CONSIGNA_SECTIONS_CACHE.getOrPut(key) {
            parseConsignaSections(assignment.description, baseUrl)
        }
    }

    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onPrimaryColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val errorContainerColor = if (isMiuix) MiuixTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.errorContainer
    val onErrorContainerColor = if (isMiuix) MiuixTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onErrorContainer

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ConsignaHeaderTitle(themeStyle = themeStyle)

        if (sections.isEmpty()) {
            val emptyBoxModifier = if (isMiuix) {
                Modifier
                    .fillMaxWidth()
                    .squircleSurface(MiuixTheme.colorScheme.surfaceContainer, 16.dp)
                    .squircleBorder(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.08f), 16.dp)
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(16.dp)
            }
            Box(modifier = emptyBoxModifier) {
                Text(
                    text = stringResource(Res.string.task_no_instructions),
                    color = onSurfaceVariantColor,
                    fontSize = 13.sp
                )
            }
        } else {
            for (section in sections) {
                ConsignaSectionItem(
                    section = section,
                    themeStyle = themeStyle,
                    baseUrl = baseUrl,
                    token = token,
                    cookies = cookies
                )
            }
        }
    }
}

@Composable
fun ConsignaHeaderTitle(
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
        if (isMiuix) {
            MiuixText(
                text = stringResource(Res.string.task_consigna_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
        } else {
            Text(
                text = stringResource(Res.string.task_consigna_title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = onSurfaceColor
            )
        }
    }
}

@Composable
fun ConsignaSectionItem(
    section: ConsignaSection,
    themeStyle: AppThemeStyle,
    baseUrl: String?,
    token: String? = null,
    cookies: String? = null,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onPrimaryColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val errorContainerColor = if (isMiuix) MiuixTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.errorContainer
    val onErrorContainerColor = if (isMiuix) MiuixTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onErrorContainer

    val icon = when (section.iconType) {
        ConsignaIconType.CAPACITY -> Icons.Default.School
        ConsignaIconType.PROCEDURE -> Icons.Default.Build
        ConsignaIconType.PREREQUISITE -> Icons.Default.MenuBook
        ConsignaIconType.SPECIFICATION -> Icons.Default.Checklist
        ConsignaIconType.GENERAL -> Icons.Default.Description
    }


                if (isMiuix) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainer,
                                cornerRadius = 18.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = MiuixTheme.colorScheme.outline.copy(alpha = 0.08f),
                                cornerRadius = 18.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Línea lateral de acento Monet
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(primaryColor)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Encabezado de la Sección con Icono
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = primaryColor.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (!section.iconUrl.isNullOrBlank()) {
                                                val headerImageRequest = rememberConsignaImageRequest(
                                                    imageUrl = section.iconUrl,
                                                    baseUrl = baseUrl,
                                                    token = token,
                                                    cookies = cookies
                                                )
                                                AsyncImage(
                                                    model = headerImageRequest,
                                                    contentDescription = section.title,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    MiuixText(
                                        text = section.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor
                                    )
                                }

                                // Texto introductorio / cuerpo
                                if (section.introText.isNotBlank()) {
                                    RichConsignaText(
                                        markdown = section.introText,
                                        onSurfaceColor = onSurfaceColor,
                                        onSurfaceVariantColor = onSurfaceVariantColor,
                                        primaryColor = primaryColor,
                                        uriHandler = uriHandler
                                    )
                                }

                                // Botón interactivo para Descargar Guía / Archivo
                                section.downloadLink?.let { (label, url) ->
                                    Box(
                                        modifier = Modifier
                                            .squircleSurface(primaryColor, 12.dp)
                                            .clickable {
                                                try { uriHandler.openUri(url) } catch (_: Exception) {}
                                            }
                                            .padding(horizontal = 14.dp, vertical = 9.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = onPrimaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            MiuixText(
                                                text = label.ifBlank { "Descargar Guia de Laboratorio" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = onPrimaryColor
                                            )
                                        }
                                    }
                                }

                                // Imagenes de la consigna
                                if (section.imageUrls.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        for (imageUrl in section.imageUrls) {
                                            ConsignaImageItem(
                                                imageUrl = imageUrl,
                                                baseUrl = baseUrl,
                                                token = token,
                                                cookies = cookies,
                                                backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh,
                                                onOpenUrl = { url ->
                                                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                                                }
                                            )
                                        }
                                    }
                                }

                                // Lista de viñetas estructuradas
                                if (section.bullets.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        for (bullet in section.bullets) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (!bullet.index.isNullOrBlank()) {
                                                    MiuixText(
                                                        text = bullet.index,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (bullet.isHighlighted) errorContainerColor else primaryColor,
                                                        modifier = Modifier.widthIn(min = 22.dp)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 7.dp)
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (bullet.isHighlighted) errorContainerColor else primaryColor)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (bullet.isHighlighted) {
                                                        if (bullet.label.isNotBlank()) {
                                                            MiuixText(
                                                                text = bullet.label + ":",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = onSurfaceColor
                                                            )
                                                        }
                                                        Spacer(Modifier.height(2.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = errorContainerColor
                                                        ) {
                                                            MiuixText(
                                                                text = bullet.value,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = onErrorContainerColor,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    } else {
                                                        val fullBulletMarkdown = if (bullet.label.isNotBlank()) {
                                                            "**${bullet.label}:** ${bullet.value}"
                                                        } else {
                                                            bullet.value
                                                        }
                                                        RichConsignaText(
                                                            markdown = fullBulletMarkdown,
                                                            onSurfaceColor = onSurfaceColor,
                                                            onSurfaceVariantColor = onSurfaceVariantColor,
                                                            primaryColor = primaryColor,
                                                            uriHandler = uriHandler
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Notas de pie / Párrafos de cierre (Fecha límite, Docente, etc.)
                                if (section.footerNotes.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        for (note in section.footerNotes) {
                                            RichConsignaText(
                                                markdown = note,
                                                onSurfaceColor = onSurfaceColor,
                                                onSurfaceVariantColor = onSurfaceVariantColor,
                                                primaryColor = primaryColor,
                                                uriHandler = uriHandler
                                            )
                                        }
                                    }
                                }

                                // Enlaces secundarios (ej. Foro de consultas)
                                if (section.otherLinks.isNotEmpty()) {
                                    for ((label, url) in section.otherLinks) {
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            MiuixText(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Material Design 3
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Línea lateral de acento Monet
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(primaryColor)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = primaryColor.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (!section.iconUrl.isNullOrBlank()) {
                                                val headerImageRequest = rememberConsignaImageRequest(
                                                    imageUrl = section.iconUrl,
                                                    baseUrl = baseUrl,
                                                    token = token,
                                                    cookies = cookies
                                                )
                                                AsyncImage(
                                                    model = headerImageRequest,
                                                    contentDescription = section.title,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                        color = onSurfaceColor
                                    )
                                }

                                if (section.introText.isNotBlank()) {
                                    RichConsignaText(
                                        markdown = section.introText,
                                        onSurfaceColor = onSurfaceColor,
                                        onSurfaceVariantColor = onSurfaceVariantColor,
                                        primaryColor = primaryColor,
                                        uriHandler = uriHandler
                                    )
                                }

                                section.downloadLink?.let { (label, url) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = primaryColor,
                                        modifier = Modifier.clickable {
                                            try { uriHandler.openUri(url) } catch (_: Exception) {}
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = onPrimaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = label.ifBlank { "Descargar Guia de Laboratorio" },
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                                color = onPrimaryColor
                                            )
                                        }
                                    }
                                }

                                if (section.imageUrls.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        for (imageUrl in section.imageUrls) {
                                            ConsignaImageItem(
                                                imageUrl = imageUrl,
                                                baseUrl = baseUrl,
                                                token = token,
                                                cookies = cookies,
                                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                onOpenUrl = { url ->
                                                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                                                }
                                            )
                                        }
                                    }
                                }

                                if (section.bullets.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        for (bullet in section.bullets) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (!bullet.index.isNullOrBlank()) {
                                                    Text(
                                                        text = bullet.index,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (bullet.isHighlighted) errorContainerColor else primaryColor,
                                                        modifier = Modifier.widthIn(min = 22.dp)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 7.dp)
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (bullet.isHighlighted) errorContainerColor else primaryColor)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (bullet.isHighlighted) {
                                                        if (bullet.label.isNotBlank()) {
                                                            Text(
                                                                text = bullet.label + ":",
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                                                color = onSurfaceColor
                                                            )
                                                        }
                                                        Spacer(Modifier.height(2.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = errorContainerColor
                                                        ) {
                                                            Text(
                                                                text = bullet.value,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                                color = onErrorContainerColor,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    } else {
                                                        val fullBulletMarkdown = if (bullet.label.isNotBlank()) {
                                                            "**${bullet.label}:** ${bullet.value}"
                                                        } else {
                                                            bullet.value
                                                        }
                                                        RichConsignaText(
                                                            markdown = fullBulletMarkdown,
                                                            onSurfaceColor = onSurfaceColor,
                                                            onSurfaceVariantColor = onSurfaceVariantColor,
                                                            primaryColor = primaryColor,
                                                            uriHandler = uriHandler
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (section.footerNotes.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        for (note in section.footerNotes) {
                                            RichConsignaText(
                                                markdown = note,
                                                onSurfaceColor = onSurfaceColor,
                                                onSurfaceVariantColor = onSurfaceVariantColor,
                                                primaryColor = primaryColor,
                                                uriHandler = uriHandler
                                            )
                                        }
                                    }
                                }

                                if (section.otherLinks.isNotEmpty()) {
                                    for ((label, url) in section.otherLinks) {
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                                color = primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
}


@Composable
private fun rememberConsignaImageRequest(
    imageUrl: String,
    baseUrl: String?,
    token: String?,
    cookies: String?
): ImageRequest {
    val platformContext = LocalPlatformContext.current
    val isCanvasUrl = remember(imageUrl, baseUrl) {
        (baseUrl != null && imageUrl.startsWith(baseUrl)) ||
            imageUrl.contains(".instructure.com") ||
            imageUrl.contains("canvas.")
    }
    return remember(imageUrl, token, cookies, isCanvasUrl) {
        val reqBuilder = ImageRequest.Builder(platformContext)
            .data(imageUrl)
            .crossfade(true)
        if (isCanvasUrl) {
            val headersBuilder = NetworkHeaders.Builder()
            if (!token.isNullOrBlank()) {
                headersBuilder.set("Authorization", "Bearer $token")
            }
            if (!cookies.isNullOrBlank()) {
                headersBuilder.set("Cookie", cookies)
            }
            reqBuilder.httpHeaders(headersBuilder.build())
        }
        reqBuilder.build()
    }
}

@Composable
private fun ConsignaImageItem(
    imageUrl: String,
    baseUrl: String?,
    token: String?,
    cookies: String?,
    backgroundColor: Color,
    onOpenUrl: (String) -> Unit
) {
    val imageRequest = rememberConsignaImageRequest(
        imageUrl = imageUrl,
        baseUrl = baseUrl,
        token = token,
        cookies = cookies
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.5f))
            .clickable { onOpenUrl(imageUrl) }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 320.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

private fun extractImageUrls(html: String, baseUrl: String?): List<String> {
    val imgMatches = IMG_REGEX.findAll(html)
    return imgMatches.mapNotNull { match ->
        val rawSrc = match.groupValues[1].replace("&amp;", "&").trim()
        if (rawSrc.isBlank() || rawSrc.startsWith("data:")) return@mapNotNull null
        if (rawSrc.startsWith("http://") || rawSrc.startsWith("https://")) {
            rawSrc
        } else if (rawSrc.startsWith("/") && !baseUrl.isNullOrBlank()) {
            "${baseUrl.trimEnd('/')}$rawSrc"
        } else {
            rawSrc
        }
    }.toList()
}

@Composable
fun RichConsignaText(
    markdown: String,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color,
    uriHandler: UriHandler,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
    lineHeight: TextUnit = 19.sp
) {
    val annotatedString = remember(markdown, onSurfaceColor, onSurfaceVariantColor, primaryColor) {
        buildConsignaAnnotatedString(markdown, onSurfaceColor, onSurfaceVariantColor, primaryColor)
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = onSurfaceVariantColor
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (_: Exception) {}
                }
        }
    )
}

internal fun buildConsignaAnnotatedString(
    markdown: String,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in INLINE_MARKDOWN_LINK_REGEX.findAll(markdown)) {
            if (match.range.first > lastIndex) {
                withStyle(SpanStyle(color = onSurfaceVariantColor)) {
                    append(markdown.substring(lastIndex, match.range.first))
                }
            }
            when {
                // Bold: **text**
                match.groups[2] != null -> {
                    val text = match.groups[2]!!.value
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
                        append(text)
                    }
                }
                // Link: [text](url)
                match.groups[4] != null && match.groups[5] != null -> {
                    val text = match.groups[4]!!.value
                    val url = match.groups[5]!!.value
                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(text)
                    }
                    pop()
                }
                // Code: `code`
                match.groups[7] != null -> {
                    val code = match.groups[7]!!.value
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            background = primaryColor.copy(alpha = 0.08f),
                            color = onSurfaceColor
                        )
                    ) {
                        append(" $code ")
                    }
                }
                // Italic: *text*
                match.groups[9] != null -> {
                    val text = match.groups[9]!!.value
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = onSurfaceVariantColor)) {
                        append(text)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < markdown.length) {
            withStyle(SpanStyle(color = onSurfaceVariantColor)) {
                append(markdown.substring(lastIndex))
            }
        }
    }
}

internal fun cleanHtmlToMarkdown(html: String?, baseUrl: String? = null): String {
    if (html.isNullOrBlank()) return ""
    var s = html.replace(SCRIPT_STYLE_LINK_REGEX, "")
    s = s.replace(SPAN_FONT_REGEX, "")
    s = A_HREF_REGEX.replace(s) { match ->
        var url = match.groupValues[1].replace("&amp;", "&").trim()
        val text = match.groupValues[2].replace(HTML_TAG_REGEX, " ").trim()
        if (url.startsWith("/") && !baseUrl.isNullOrBlank()) {
            url = "${baseUrl.trimEnd('/')}$url"
        }
        "[$text]($url)"
    }
    s = s.replace(BOLD_TAG_REGEX) { "**${it.groupValues[1].trim()}**" }
    s = s.replace(ITALIC_TAG_REGEX) { "*${it.groupValues[1].trim()}*" }
    s = s.replace(BR_REGEX, "\n")
    s = s.replace(P_CLOSE_REGEX, "\n\n")
    s = s.replace(P_OPEN_REGEX, "")
    s = s.replace(HTML_TAG_REGEX, " ")
    s = s.replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&#x27;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
    val lines = s.split("\n").map { it.replace(Regex("""[ \t]+"""), " ").trim() }
    val res = lines.joinToString("\n").replace(Regex("""\n{3,}"""), "\n\n")
    return res.trim()
}

internal fun parseConsignaBulletItem(liHtml: String, indexStr: String?, baseUrl: String? = null): ConsignaBullet {
    val isHighlighted = liHtml.contains("feb2b2", ignoreCase = true) ||
            liHtml.contains("c53030", ignoreCase = true) ||
            liHtml.contains("Hasta el domingo", ignoreCase = true) ||
            liHtml.contains("Fecha limite", ignoreCase = true) ||
            liHtml.contains("Fecha límite", ignoreCase = true)

    val cleaned = cleanHtmlToMarkdown(liHtml, baseUrl)
    val match = BULLET_LABEL_SPLIT_REGEX.find(cleaned)
    val label: String
    val value: String
    if (match != null) {
        val rawLabel = (match.groups[1]?.value ?: match.groups[2]?.value ?: "").replace("**", "").replace("*", "").trim()
        val rawValue = match.groups[3]?.value?.trim() ?: ""
        label = rawLabel
        value = rawValue
    } else {
        label = ""
        value = cleaned
    }
    return ConsignaBullet(
        label = label,
        value = value,
        isHighlighted = isHighlighted,
        index = indexStr,
        rawHtml = liHtml
    )
}

internal fun parseConsignaSections(html: String?, baseUrl: String? = null): List<ConsignaSection> {
    if (html.isNullOrBlank()) return emptyList()

    val sectionMatches = H3_REGEX.findAll(html).toList()

    if (sectionMatches.isNotEmpty()) {
        return sectionMatches.mapIndexed { index, match ->
            val rawTitle = match.groupValues[1]
            val currH3Start = match.range.first
            val currH3End = match.range.last + 1
            val prevH3End = if (index > 0) sectionMatches[index - 1].range.last + 1 else 0
            val nextH3Start = if (index < sectionMatches.size - 1) sectionMatches[index + 1].range.first else html.length

            val leadIn = html.substring(prevH3End, currH3Start)
            val leadInImages = extractImageUrls(leadIn, baseUrl)
            val iconUrl = leadInImages.lastOrNull()
            val preludeImages = if (index == 0 && leadInImages.size > 1) leadInImages.dropLast(1) else emptyList()

            val rawBodyChunk = html.substring(currH3End, nextH3Start)
            val cardBoundaryMatch = if (index < sectionMatches.size - 1) {
                NEXT_CARD_REGEX.find(rawBodyChunk)
            } else null
            val rawBody = if (cardBoundaryMatch != null) {
                rawBodyChunk.substring(0, cardBoundaryMatch.range.first)
            } else {
                rawBodyChunk
            }

            val cleanTitle = rawTitle.replace(HTML_TAG_REGEX, "").cleanEmoji().trim()
            val iconType = when {
                cleanTitle.contains("Capacidad", ignoreCase = true) -> ConsignaIconType.CAPACITY
                cleanTitle.contains("Procedimiento", ignoreCase = true) -> ConsignaIconType.PROCEDURE
                cleanTitle.contains("Prerrequisito", ignoreCase = true) || cleanTitle.contains("Requisito", ignoreCase = true) -> ConsignaIconType.PREREQUISITE
                cleanTitle.contains("Especificaci", ignoreCase = true) || cleanTitle.contains("Entrega", ignoreCase = true) -> ConsignaIconType.SPECIFICATION
                else -> ConsignaIconType.GENERAL
            }

            val allLinks = A_HREF_REGEX.findAll(rawBody).map {
                val rawUrl = it.groupValues[1]
                val url = if (rawUrl.startsWith("/") && !baseUrl.isNullOrBlank()) "${baseUrl.trimEnd('/')}$rawUrl" else rawUrl
                val text = it.groupValues[2].replace(HTML_TAG_REGEX, "").cleanEmoji().trim()
                text to url
            }.toList()

            val downloadLink = allLinks.firstOrNull { (text, url) ->
                text.contains("Descargar", ignoreCase = true) ||
                text.endsWith(".docx", ignoreCase = true) ||
                text.endsWith(".pdf", ignoreCase = true) ||
                text.endsWith(".xlsx", ignoreCase = true) ||
                text.endsWith(".pptx", ignoreCase = true) ||
                text.endsWith(".zip", ignoreCase = true) ||
                url.contains("/files/") ||
                (text.contains("Guía", ignoreCase = true) && (url.contains("docs.google.com") || url.contains("/files/")))
            }

            val otherLinks = allLinks.filter { it != downloadLink && !it.second.startsWith("\$CANVAS") }

            val lis = mutableListOf<ConsignaBullet>()
            var listIndex = 1
            for (listMatch in LIST_CONTAINER_REGEX.findAll(rawBody)) {
                val isOrdered = listMatch.groupValues[1].equals("ol", ignoreCase = true)
                for (liMatch in LI_REGEX.findAll(listMatch.groupValues[2])) {
                    val idxStr = if (isOrdered) "${listIndex++}." else null
                    lis.add(parseConsignaBulletItem(liMatch.groupValues[1], idxStr, baseUrl))
                }
            }

            val bodyWithoutLists = rawBody.replace(LIST_CONTAINER_REGEX, "")
            var cleanText = cleanHtmlToMarkdown(bodyWithoutLists, baseUrl)

            if (downloadLink != null && downloadLink.first.isNotBlank()) {
                val cleanDlLabel = downloadLink.first.cleanEmoji().trim()
                if (cleanDlLabel.isNotBlank()) {
                    cleanText = cleanText.replace(cleanDlLabel, "").trim().replace(WHITESPACE_REGEX, " ")
                }
            }

            val bodyImages = extractImageUrls(rawBody, baseUrl)
            val sectionImages = if (index == 0) preludeImages + bodyImages else bodyImages

            ConsignaSection(
                title = cleanTitle,
                iconType = iconType,
                introText = cleanText,
                downloadLink = downloadLink,
                bullets = lis,
                otherLinks = otherLinks,
                imageUrls = sectionImages,
                iconUrl = iconUrl
            )
        }
    }

    // Fallback: Generic assignment without <h3>
    val allLinks = A_HREF_REGEX.findAll(html).map {
        val rawUrl = it.groupValues[1]
        val url = if (rawUrl.startsWith("/") && !baseUrl.isNullOrBlank()) "${baseUrl.trimEnd('/')}$rawUrl" else rawUrl
        val text = it.groupValues[2].replace(HTML_TAG_REGEX, "").cleanEmoji().trim()
        text to url
    }.toList()

    val downloadLink = allLinks.firstOrNull { (text, url) ->
        text.contains("Descargar", ignoreCase = true) ||
        text.endsWith(".docx", ignoreCase = true) ||
        text.endsWith(".pdf", ignoreCase = true) ||
        text.endsWith(".xlsx", ignoreCase = true) ||
        text.endsWith(".pptx", ignoreCase = true) ||
        text.endsWith(".zip", ignoreCase = true) ||
        url.contains("/files/") ||
        (text.contains("Guía", ignoreCase = true) && (url.contains("docs.google.com") || url.contains("/files/")))
    }

    val imageUrls = extractImageUrls(html, baseUrl)

    val listMatches = LIST_CONTAINER_REGEX.findAll(html).toList()
    val bullets = mutableListOf<ConsignaBullet>()
    val introText: String
    val footerNotes = mutableListOf<String>()

    if (listMatches.isNotEmpty()) {
        val firstStart = listMatches.first().range.first
        val lastEnd = listMatches.last().range.last + 1

        val leadInHtml = html.substring(0, firstStart)
        introText = cleanHtmlToMarkdown(leadInHtml, baseUrl)

        var orderIndex = 1
        for (lMatch in listMatches) {
            val isOrdered = lMatch.groupValues[1].equals("ol", ignoreCase = true)
            for (liMatch in LI_REGEX.findAll(lMatch.groupValues[2])) {
                val idxStr = if (isOrdered) "${orderIndex++}." else null
                bullets.add(parseConsignaBulletItem(liMatch.groupValues[1], idxStr, baseUrl))
            }
        }

        val footerHtml = html.substring(lastEnd)
        val cleanFooter = cleanHtmlToMarkdown(footerHtml, baseUrl)
        if (cleanFooter.isNotBlank()) {
            cleanFooter.split("\n\n").forEach { paragraph ->
                val p = paragraph.trim()
                if (p.isNotBlank()) {
                    footerNotes.add(p)
                }
            }
        }
    } else {
        introText = cleanHtmlToMarkdown(html, baseUrl)
    }

    if (introText.isBlank() && bullets.isEmpty() && allLinks.isEmpty() && imageUrls.isEmpty()) {
        return emptyList()
    }

    return listOf(
        ConsignaSection(
            title = "Instrucciones de la Actividad",
            iconType = ConsignaIconType.GENERAL,
            introText = introText,
            downloadLink = downloadLink,
            bullets = bullets,
            otherLinks = allLinks.filter { it != downloadLink && !it.second.startsWith("\$CANVAS") },
            imageUrls = imageUrls,
            iconUrl = null,
            footerNotes = footerNotes
        )
    )
}

private fun String.cleanEmoji(): String {
    return this.replace(EMOJI_REGEX, "")
        .replace("📥", "")
        .replace("🔬", "")
        .replace("💡", "")
        .replace("📌", "")
        .replace("🚀", "")
        .trim()
}
