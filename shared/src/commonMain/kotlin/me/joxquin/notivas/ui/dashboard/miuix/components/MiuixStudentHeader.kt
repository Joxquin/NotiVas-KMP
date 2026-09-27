package me.joxquin.notivas.ui.dashboard.miuix.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.ui.dashboard.SyncStatus
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixBlue = Color(0xFF3482FF)
private val MiuixGreen = Color(0xFF66BB6A)
private val MiuixOrange = Color(0xFFFF9800)
private val MiuixRed = Color(0xFFEF5350)

@Composable
fun MiuixStudentHeader(
    profile: UserProfile?,
    institution: String,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    onRefresh: () -> Unit,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rawName = profile?.name ?: ""
    val studentFirstName = rawName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: ""
    val initials = rawName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifBlank { "AV" }

    val institutionOrCareer = if (institution.isNotBlank() && institution != "Canvas LMS") {
        institution
    } else {
        ""
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar Squircle (18dp)
                me.joxquin.notivas.ui.components.AsyncAvatarImage(
                    url = profile?.avatarUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(46.dp)
                        .squircleSurface(
                            color = Color(0xFF1E293B),
                            cornerRadius = 18.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            cornerRadius = 18.dp
                        )
                        .clickable(onClick = onAvatarClick)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .squircleSurface(
                                color = Color(0xFF1E293B),
                                cornerRadius = 18.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                cornerRadius = 18.dp
                            )
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val greetingText = if (studentFirstName.isNotBlank()) "Hola, $studentFirstName" else "Hola"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = greetingText,
                            style = MiuixTheme.textStyles.title2,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                    if (institutionOrCareer.isNotBlank()) {
                        Text(
                            text = institutionOrCareer,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }

            // Sync Refresh Action Pill
            val infiniteTransition = rememberInfiniteTransition()
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val (statusText, statusColor) = when (syncStatus) {
                SyncStatus.SYNCING -> "Sincronizando..." to MiuixOrange
                SyncStatus.SYNCED -> "Sincronizado" to MiuixGreen
                SyncStatus.FAILED -> "Sin conexión" to MiuixRed
            }

            Box(
                modifier = Modifier
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        cornerRadius = 14.dp
                    )
                    .squircleBorder(
                        width = 0.5.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 14.dp
                    )
                    .clickable(onClick = onRefresh)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .graphicsLayer {
                                if (syncStatus == SyncStatus.SYNCING) {
                                    alpha = pulseAlpha
                                }
                            }
                            .background(statusColor)
                    )
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
