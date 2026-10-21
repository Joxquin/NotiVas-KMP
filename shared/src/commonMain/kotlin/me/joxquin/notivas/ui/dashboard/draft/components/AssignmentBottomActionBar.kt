package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AssignmentBottomActionBar(
    onSaveForLater: () -> Unit,
    onGoToEditor: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isBookmarked by remember { mutableStateOf(false) }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Guardar para después
            Row(
                modifier = Modifier
                    .weight(1f)
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        cornerRadius = 14.dp
                    )
                    .squircleBorder(
                        width = 1.dp,
                        color = MiuixTheme.colorScheme.outline.copy(alpha = 0.08f),
                        cornerRadius = 14.dp
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isBookmarked = !isBookmarked
                        onSaveForLater()
                    }
                    .padding(horizontal = 12.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) Color(0xFFFFB74D) else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                MiuixText(
                    text = stringResource(Res.string.task_action_save_later),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }

            // Botón Ir al Editor
            Row(
                modifier = Modifier
                    .weight(1.1f)
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.primary,
                        cornerRadius = 14.dp
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onGoToEditor()
                    }
                    .padding(horizontal = 12.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                MiuixText(
                    text = stringResource(Res.string.task_action_go_to_editor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    isBookmarked = !isBookmarked
                    onSaveForLater()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.task_action_save_later),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }

            Button(
                onClick = onGoToEditor,
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.task_action_go_to_editor),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }
        }
    }
}
