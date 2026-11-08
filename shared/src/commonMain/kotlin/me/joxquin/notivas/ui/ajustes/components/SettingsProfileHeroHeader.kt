package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_action_drive
import me.joxquin.notivas.shared.resources.settings_action_drive_connected
import me.joxquin.notivas.shared.resources.settings_action_drive_disconnected
import me.joxquin.notivas.shared.resources.settings_action_logout
import me.joxquin.notivas.shared.resources.settings_action_logout_desc
import me.joxquin.notivas.shared.resources.settings_action_web
import me.joxquin.notivas.shared.resources.settings_action_web_desc
import me.joxquin.notivas.shared.resources.settings_status_online
import me.joxquin.notivas.shared.resources.settings_status_synced
import me.joxquin.notivas.shared.resources.student_avatar_cd
import me.joxquin.notivas.shared.resources.student_unlinked
import me.joxquin.notivas.ui.components.AsyncAvatarImage
import me.joxquin.notivas.ui.profile.ProfileUiState
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun SettingsProfileHeroHeader(
    uiState: ProfileUiState,
    onVerifyToken: () -> Unit,
    onOpenDrive: () -> Unit,
    onPairWeb: () -> Unit,
    onRequestLogout: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixSettingsProfileHeroHeader(
            uiState = uiState,
            onVerifyToken = onVerifyToken,
            onOpenDrive = onOpenDrive,
            onPairWeb = onPairWeb,
            onRequestLogout = onRequestLogout,
            modifier = modifier
        )
    } else {
        MaterialSettingsProfileHeroHeader(
            uiState = uiState,
            onVerifyToken = onVerifyToken,
            onOpenDrive = onOpenDrive,
            onPairWeb = onPairWeb,
            onRequestLogout = onRequestLogout,
            modifier = modifier
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODO MIUIX
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MiuixSettingsProfileHeroHeader(
    uiState: ProfileUiState,
    onVerifyToken: () -> Unit,
    onOpenDrive: () -> Unit,
    onPairWeb: () -> Unit,
    onRequestLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile
    val rawName = profile?.name?.trim().orEmpty()
    val initials = rawName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")

    val defaultStudentLabel = stringResource(Res.string.student_unlinked)
    val displayName = if (rawName.isNotBlank()) rawName else defaultStudentLabel
    val host = uiState.universityHost.ifBlank { "canvas" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Avatar Grande
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        cornerRadius = 28.dp
                    )
                    .squircleBorder(
                        width = 1.5.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 28.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncAvatarImage(
                    url = profile?.avatarUrl,
                    contentDescription = stringResource(Res.string.student_avatar_cd),
                    modifier = Modifier
                        .fillMaxSize()
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant,
                            cornerRadius = 28.dp
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (initials.isNotBlank()) {
                            Text(
                                text = initials,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // Sync Status Badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .squircleSurface(
                        color = if (uiState.isVerifyingToken) MiuixTheme.colorScheme.surfaceContainerHigh else MiuixTheme.colorScheme.primary,
                        cornerRadius = 9999.dp
                    )
                    .clickable(enabled = !uiState.isVerifyingToken, onClick = onVerifyToken),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isVerifyingToken) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = MiuixTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Nombre del estudiante
        Text(
            text = displayName,
            style = MiuixTheme.textStyles.title1.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Subtítulo: En línea • Host • ID
        val onlineText = stringResource(Res.string.settings_status_online)
        val studentId = profile?.id?.let { "ID: $it" } ?: ""
        val subtitleParts = listOfNotNull(
            onlineText,
            host.takeIf { it.isNotBlank() },
            studentId.takeIf { it.isNotBlank() }
        ).joinToString(" • ")

        Text(
            text = subtitleParts,
            style = MiuixTheme.textStyles.subtitle,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Bento Trío de Acciones Rápidas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Botón 1: Google Drive
            val driveConnected = uiState.googleDriveConnected
            MiuixQuickActionButton(
                title = stringResource(Res.string.settings_action_drive),
                status = if (driveConnected) stringResource(Res.string.settings_action_drive_connected) else stringResource(Res.string.settings_action_drive_disconnected),
                icon = if (driveConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                accentColor = if (driveConnected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                onClick = onOpenDrive,
                modifier = Modifier.weight(1f)
            )

            // Botón 2: Vincular Web PIN
            MiuixQuickActionButton(
                title = stringResource(Res.string.settings_action_web),
                status = stringResource(Res.string.settings_action_web_desc),
                icon = Icons.Outlined.Devices,
                accentColor = MiuixTheme.colorScheme.primary,
                onClick = onPairWeb,
                modifier = Modifier.weight(1f)
            )

            // Botón 3: Cerrar sesión
            MiuixQuickActionButton(
                title = stringResource(Res.string.settings_action_logout),
                status = stringResource(Res.string.settings_action_logout_desc),
                icon = Icons.AutoMirrored.Filled.Logout,
                accentColor = MiuixTheme.colorScheme.error,
                onClick = onRequestLogout,
                isDestructive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiuixQuickActionButton(
    title: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val bgColor = if (isDestructive) {
        MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    } else {
        MiuixTheme.colorScheme.surfaceContainer
    }

    MiuixCard(
        modifier = modifier,
        colors = MiuixCardDefaults.defaultColors(color = bgColor),
        insideMargin = PaddingValues(12.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .squircleSurface(
                        color = if (isDestructive) MiuixTheme.colorScheme.errorContainer else MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 14.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MiuixTheme.textStyles.title2.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isDestructive) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = status,
                style = MiuixTheme.textStyles.subtitle.copy(
                    fontSize = 11.sp
                ),
                color = if (isDestructive) MiuixTheme.colorScheme.error.copy(alpha = 0.8f) else MiuixTheme.colorScheme.onSurfaceSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODO MATERIAL DESIGN 3
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaterialSettingsProfileHeroHeader(
    uiState: ProfileUiState,
    onVerifyToken: () -> Unit,
    onOpenDrive: () -> Unit,
    onPairWeb: () -> Unit,
    onRequestLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile
    val rawName = profile?.name?.trim().orEmpty()
    val initials = rawName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")

    val defaultStudentLabel = stringResource(Res.string.student_unlinked)
    val displayName = if (rawName.isNotBlank()) rawName else defaultStudentLabel
    val host = uiState.universityHost.ifBlank { "canvas" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Avatar Grande Material 3
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                AsyncAvatarImage(
                    url = profile?.avatarUrl,
                    contentDescription = stringResource(Res.string.student_avatar_cd),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (initials.isNotBlank()) {
                            Text(
                                text = initials,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // Sync Check Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !uiState.isVerifyingToken, onClick = onVerifyToken)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (uiState.isVerifyingToken) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Nombre
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Subtítulo estado / host
        val onlineText = stringResource(Res.string.settings_status_online)
        val studentId = profile?.id?.let { "ID: $it" } ?: ""
        val subtitleParts = listOfNotNull(
            onlineText,
            host.takeIf { it.isNotBlank() },
            studentId.takeIf { it.isNotBlank() }
        ).joinToString(" • ")

        Text(
            text = subtitleParts,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Trío de Acciones Material
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val driveConnected = uiState.googleDriveConnected
            MaterialQuickActionButton(
                title = stringResource(Res.string.settings_action_drive),
                status = if (driveConnected) stringResource(Res.string.settings_action_drive_connected) else stringResource(Res.string.settings_action_drive_disconnected),
                icon = if (driveConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                accentColor = if (driveConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onOpenDrive,
                modifier = Modifier.weight(1f)
            )

            MaterialQuickActionButton(
                title = stringResource(Res.string.settings_action_web),
                status = stringResource(Res.string.settings_action_web_desc),
                icon = Icons.Outlined.Devices,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = onPairWeb,
                modifier = Modifier.weight(1f)
            )

            MaterialQuickActionButton(
                title = stringResource(Res.string.settings_action_logout),
                status = stringResource(Res.string.settings_action_logout_desc),
                icon = Icons.AutoMirrored.Filled.Logout,
                accentColor = MaterialTheme.colorScheme.error,
                onClick = onRequestLogout,
                isDestructive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MaterialQuickActionButton(
    title: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDestructive) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
