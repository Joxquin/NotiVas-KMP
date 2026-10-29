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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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

data class ConsignaBullet(
    val label: String,
    val value: String,
    val isHighlighted: Boolean = false
)

data class ConsignaSection(
    val title: String,
    val iconType: ConsignaIconType,
    val introText: String,
    val downloadLink: Pair<String, String>? = null,
    val bullets: List<ConsignaBullet> = emptyList(),
    val otherLinks: List<Pair<String, String>> = emptyList(),
    val imageUrls: List<String> = emptyList()
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
            Regex("""^(https?://[^/]+)""").find(url)?.value
        } ?: AppModule.preferencesManager.getUniversityUrl()
    }
    val sections = remember(assignment.description, baseUrl) {
        parseConsignaSections(assignment.description, baseUrl)
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
        // Título de la Sección de Consigna
        Row(
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
            // Renderizar cada sección estructurada
            for (section in sections) {
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
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
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
                                    MiuixText(
                                        text = section.introText,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = onSurfaceVariantColor
                                    )
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

                                // Lista de viñetas estructuradas
                                if (section.bullets.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (bullet in section.bullets) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (bullet.isHighlighted) errorContainerColor else primaryColor)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (bullet.label.isNotBlank()) {
                                                        MiuixText(
                                                            text = bullet.label + ":",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = onSurfaceColor
                                                        )
                                                    }
                                                    if (bullet.isHighlighted) {
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
                                                        MiuixText(
                                                            text = bullet.value,
                                                            fontSize = 13.sp,
                                                            lineHeight = 18.sp,
                                                            color = onSurfaceVariantColor
                                                        )
                                                    }
                                                }
                                            }
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
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                        color = onSurfaceColor
                                    )
                                }

                                if (section.introText.isNotBlank()) {
                                    Text(
                                        text = section.introText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                                        color = onSurfaceVariantColor
                                    )
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

                                if (section.bullets.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (bullet in section.bullets) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (bullet.isHighlighted) errorContainerColor else primaryColor)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (bullet.label.isNotBlank()) {
                                                        Text(
                                                            text = bullet.label + ":",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                                            color = onSurfaceColor
                                                        )
                                                    }
                                                    if (bullet.isHighlighted) {
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
                                                        Text(
                                                            text = bullet.value,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                                            color = onSurfaceVariantColor
                                                        )
                                                    }
                                                }
                                            }
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
        }
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
    val platformContext = LocalPlatformContext.current
    val isCanvasUrl = remember(imageUrl, baseUrl) {
        (baseUrl != null && imageUrl.startsWith(baseUrl)) ||
            imageUrl.contains(".instructure.com") ||
            imageUrl.contains("canvas.")
    }
    val imageRequest = remember(imageUrl, token, cookies, isCanvasUrl) {
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onOpenUrl(imageUrl) }
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun extractImageUrls(html: String, baseUrl: String?): List<String> {
    val imgMatches = Regex("""(?i)<img[^>]+src=["']([^"']+)["'][^>]*>""").findAll(html)
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

internal fun parseConsignaSections(html: String?, baseUrl: String? = null): List<ConsignaSection> {
    if (html.isNullOrBlank()) return emptyList()

    val sectionMatches = Regex("""(?s)<h3[^>]*>(.*?)</h3>(.*?)((?=<h3)|$)""").findAll(html).toList()

    if (sectionMatches.isNotEmpty()) {
        val firstH3Index = html.indexOf("<h3", ignoreCase = true)
        val prelude = if (firstH3Index > 0) html.substring(0, firstH3Index) else ""
        val preludeImages = if (prelude.isNotBlank()) extractImageUrls(prelude, baseUrl) else emptyList()

        return sectionMatches.mapIndexed { index, match ->
            val rawTitle = match.groupValues[1]
            val rawBody = match.groupValues[2]

            val cleanTitle = rawTitle.replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
            val iconType = when {
                cleanTitle.contains("Capacidad", ignoreCase = true) -> ConsignaIconType.CAPACITY
                cleanTitle.contains("Procedimiento", ignoreCase = true) -> ConsignaIconType.PROCEDURE
                cleanTitle.contains("Prerrequisito", ignoreCase = true) || cleanTitle.contains("Requisito", ignoreCase = true) -> ConsignaIconType.PREREQUISITE
                cleanTitle.contains("Especificaci", ignoreCase = true) || cleanTitle.contains("Entrega", ignoreCase = true) -> ConsignaIconType.SPECIFICATION
                else -> ConsignaIconType.GENERAL
            }

            val allLinks = Regex("""(?s)<a[^>]+href=["']([^"']+)["'][^>]*>(.*?)</a>""").findAll(rawBody).map {
                val url = it.groupValues[1]
                val text = it.groupValues[2].replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
                text to url
            }.toList()

            val downloadLink = allLinks.firstOrNull { (text, url) ->
                text.contains("Descargar", ignoreCase = true) ||
                text.contains("Guia", ignoreCase = true) ||
                text.contains("Guía", ignoreCase = true) ||
                text.endsWith(".docx", ignoreCase = true) ||
                text.endsWith(".pdf", ignoreCase = true) ||
                url.contains("docs.google.com") ||
                url.contains("/files/")
            }

            val otherLinks = allLinks.filter { it != downloadLink && !it.second.startsWith("\$CANVAS") }

            val lis = Regex("""(?s)<li[^>]*>(.*?)</li>""").findAll(rawBody).map { liMatch ->
                val liHtml = liMatch.groupValues[1]
                val isHighlighted = liHtml.contains("feb2b2", ignoreCase = true) ||
                        liHtml.contains("c53030", ignoreCase = true) ||
                        liHtml.contains("Hasta el domingo", ignoreCase = true) ||
                        liHtml.contains("Fecha limite", ignoreCase = true) ||
                        liHtml.contains("Fecha límite", ignoreCase = true)

                val colonIndex = liHtml.indexOf(':')
                val label: String
                val value: String
                if (colonIndex != -1) {
                    val rawLabel = liHtml.substring(0, colonIndex).replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
                    val rawValue = liHtml.substring(colonIndex + 1).replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
                    label = rawLabel
                    value = rawValue
                } else {
                    label = ""
                    value = liHtml.replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
                }
                ConsignaBullet(label = label, value = value, isHighlighted = isHighlighted)
            }.toList()

            var cleanText = rawBody
                .replace(Regex("""(?s)<ul[^>]*>.*?</ul>"""), "")
                .replace(Regex("""(?s)<a[^>]*>.*?</a>"""), "")
                .replace(Regex("<[^>]*>"), " ")
                .cleanEmoji()
                .trim()
            cleanText = cleanText.replace(Regex("""\s+"""), " ")

            val bodyImages = extractImageUrls(rawBody, baseUrl)
            val sectionImages = if (index == 0) preludeImages + bodyImages else bodyImages

            ConsignaSection(
                title = cleanTitle,
                iconType = iconType,
                introText = cleanText,
                downloadLink = downloadLink,
                bullets = lis,
                otherLinks = otherLinks,
                imageUrls = sectionImages
            )
        }
    }

    val allLinks = Regex("""(?s)<a[^>]+href=["']([^"']+)["'][^>]*>(.*?)</a>""").findAll(html).map {
        val url = it.groupValues[1]
        val text = it.groupValues[2].replace(Regex("<[^>]*>"), "").cleanEmoji().trim()
        text to url
    }.toList()

    val downloadLink = allLinks.firstOrNull { (text, url) ->
        text.contains("Descargar", ignoreCase = true) ||
        text.endsWith(".docx", ignoreCase = true) ||
        text.endsWith(".pdf", ignoreCase = true) ||
        url.contains("/files/")
    }

    val imageUrls = extractImageUrls(html, baseUrl)
    val cleanText = html.replace(Regex("<[^>]*>"), " ").cleanEmoji().trim().replace(Regex("""\s+"""), " ")

    if (cleanText.isBlank() && allLinks.isEmpty() && imageUrls.isEmpty()) {
        return emptyList()
    }

    return listOf(
        ConsignaSection(
            title = "Instrucciones de la Actividad",
            iconType = ConsignaIconType.GENERAL,
            introText = cleanText,
            downloadLink = downloadLink,
            bullets = emptyList(),
            otherLinks = allLinks.filter { it != downloadLink },
            imageUrls = imageUrls
        )
    )
}

private fun String.cleanEmoji(): String {
    return this.replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "")
        .replace("📥", "")
        .replace("🔬", "")
        .replace("💡", "")
        .replace("📌", "")
        .replace("🚀", "")
        .trim()
}
