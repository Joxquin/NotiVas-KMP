package me.joxquin.notivas.ui.course.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CourseServicesBentoSection(
    latestAnnouncement: CanvasDiscussionTopic?,
    unreadAnnouncementsCount: Int,
    coachTip: String,
    studentsCount: Int,
    studentAvatars: List<UserProfile>,
    themeStyle: AppThemeStyle,
    onOpenAnnouncements: () -> Unit,
    onConsultCoach: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenProgreso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        // Título de Sección
        Text(
            text = stringResource(Res.string.course_detail_services_header),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Tablón de Anuncios
            val announcementSnippet = latestAnnouncement?.message?.let {
                val clean = it.replace(Regex("<[^>]*>"), " ").trim()
                if (clean.length > 110) clean.take(107) + "..." else clean
            } ?: latestAnnouncement?.title ?: stringResource(Res.string.course_detail_no_announcements)

            BentoCardItem(
                icon = Icons.Default.Campaign,
                iconBg = Color(0xFFE57373).copy(alpha = 0.18f),
                iconTint = Color(0xFFE57373),
                topBadgeText = if (latestAnnouncement != null) "Ayer, 18:30" else null,
                title = stringResource(Res.string.course_detail_announcements_title),
                bodyText = "\"$announcementSnippet\"",
                bottomActionText = stringResource(Res.string.course_detail_unread_format, unreadAnnouncementsCount.coerceAtLeast(1)),
                onClick = onOpenAnnouncements,
                themeStyle = themeStyle
            )

            // 2. Tecsup Coach
            val coachText = coachTip.ifBlank {
                stringResource(Res.string.course_detail_coach_default_tip)
            }
            BentoCardItem(
                icon = Icons.Default.SmartToy,
                iconBg = Color(0xFF4CAF50).copy(alpha = 0.18f),
                iconTint = Color(0xFF4CAF50),
                topBadgeText = stringResource(Res.string.course_detail_coach_badge),
                title = stringResource(Res.string.course_detail_coach_title),
                bodyText = "\"$coachText\"",
                bottomActionText = stringResource(Res.string.course_detail_coach_action),
                onClick = onConsultCoach,
                themeStyle = themeStyle
            )

            // 3. Compañeros y Docentes
            BentoPeersCardItem(
                studentsCount = studentsCount,
                studentAvatars = studentAvatars,
                onClick = onOpenDirectory,
                themeStyle = themeStyle
            )

            // 4. Sílabo del Curso (Acceso directo a Progreso y Ponderación)
            BentoCardItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconBg = Color(0xFF64B5F6).copy(alpha = 0.18f),
                iconTint = Color(0xFF64B5F6),
                topBadgeText = stringResource(Res.string.course_detail_syllabus_badge),
                title = stringResource(Res.string.course_detail_syllabus_title),
                bodyText = stringResource(Res.string.course_detail_syllabus_desc),
                bottomActionText = stringResource(Res.string.course_detail_syllabus_action),
                onClick = onOpenProgreso,
                themeStyle = themeStyle
            )
        }
    }
}

@Composable
private fun BentoCardItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    topBadgeText: String?,
    title: String,
    bodyText: String,
    bottomActionText: String,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    val borderColor = if (isMiuix) MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        // Fila Superior: Icono y Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (topBadgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardColor.copy(alpha = 0.6f))
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = topBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariantColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Título
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Texto del cuerpo
        Text(
            text = bodyText,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = onSurfaceVariantColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Acción Inferior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bottomActionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BentoPeersCardItem(
    studentsCount: Int,
    studentAvatars: List<UserProfile>,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    val borderColor = if (isMiuix) MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        // Fila Superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFBA68C8).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFFBA68C8),
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardColor.copy(alpha = 0.6f))
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(Res.string.course_detail_students_badge, studentsCount),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Título
        Text(
            text = stringResource(Res.string.course_detail_peers_title),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Facepile de Avatares Solapados
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            val sampleInitials = listOf("MA", "CL", "JH")
            val avatarColors = listOf(
                Color(0xFF42A5F5),
                Color(0xFF26A69A),
                Color(0xFFAB47BC)
            )

            sampleInitials.forEachIndexed { idx, init ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(avatarColors.getOrElse(idx) { primaryColor })
                        .border(2.dp, cardColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = init,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width((-6).dp))
            }

            // Píldora de restantes "+25"
            val remainingCount = (studentsCount - sampleInitials.size).coerceAtLeast(1)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(onSurfaceVariantColor.copy(alpha = 0.35f))
                    .border(2.dp, cardColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Acción Inferior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.course_detail_peers_action),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
