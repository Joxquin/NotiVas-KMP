package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.UserProfile
import me.joxquin.notivas.ui.dashboard.SyncStatus

import me.joxquin.notivas.ui.components.AsyncAvatarImage

@Composable
fun GreetingSection(
    profile: UserProfile?,
    institution: String,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    onRefresh: () -> Unit = {},
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
        .ifBlank { "NV" }

    val institutionOrCareer = if (institution.isNotBlank() && institution != "Canvas LMS") {
        institution
    } else {
        ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar Circular Material You con foto o iniciales
            AsyncAvatarImage(
                url = profile?.avatarUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(46.dp)
                    .clickable(onClick = onAvatarClick),
                shape = CircleShape
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onAvatarClick)
                ) {
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (institutionOrCareer.isNotBlank()) {
                    Text(
                        text = institutionOrCareer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sync Status Capsule
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
            SyncStatus.SYNCING -> "Sincronizando..." to MaterialTheme.colorScheme.tertiary
            SyncStatus.SYNCED -> "Sincronizado" to Color(0xFF4CAF50)
            SyncStatus.FAILED -> "Sin conexión" to MaterialTheme.colorScheme.error
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clickable(onClick = onRefresh)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
