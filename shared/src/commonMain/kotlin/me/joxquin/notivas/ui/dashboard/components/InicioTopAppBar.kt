package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import me.joxquin.notivas.ui.components.brand.NotiVasOutlineLogo
import me.joxquin.notivas.ui.splash.LocalSplashLogoTarget
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTopAppBar(
    isScrolled: Boolean,
    title: String = "Inicio",
    onRefresh: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToAjustes: () -> Unit = {},
    actionsEnabled: Boolean = true,
    isEntranceTriggered: Boolean = true,
    modifier: Modifier = Modifier
) {
    val topAppBarContainerColor by animateColorAsState(
        targetValue = if (isScrolled) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250),
        label = "inicioTopBarContainerColor"
    )

    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            val onTargetPositioned = LocalSplashLogoTarget.current
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .graphicsLayer {
                        alpha = if (isEntranceTriggered) 1f else 0f
                    }
                    .onGloballyPositioned { coords ->
                        onTargetPositioned?.invoke(coords.boundsInRoot())
                    },
                contentAlignment = Alignment.Center
            ) {
                NotiVasOutlineLogo(
                    size = 28.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    accentTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                )
            }
        },
        title = {
            AnimatedVisibility(
                visible = isScrolled,
                enter = fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                        slideInVertically(animationSpec = tween(250)) { it / 2 },
                exit = fadeOut(animationSpec = tween(150)) +
                        slideOutVertically(animationSpec = tween(180)) { it / 2 }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (actionsEnabled) {
                IconButton(onClick = onNavigateToSchedule) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Agenda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onNavigateToActivity) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "Actividad Reciente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topAppBarContainerColor
        )
    )
}
