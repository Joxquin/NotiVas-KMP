package me.joxquin.notivas.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.nav_inicio
import me.joxquin.notivas.shared.resources.nav_herramientas
import me.joxquin.notivas.shared.resources.nav_copilot
import me.joxquin.notivas.shared.resources.nav_ajustes

enum class MainDestination(
    val titleRes: StringResource,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Inicio(Res.string.nav_inicio, Icons.Filled.Home, Icons.Outlined.Home),
    Herramientas(Res.string.nav_herramientas, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    Copilot(Res.string.nav_copilot, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    Ajustes(Res.string.nav_ajustes, Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun NotiVasNavigationBar(
    currentDestination: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(250)) { it },
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(200)) { it },
        modifier = modifier
    ) {
        val outlineVariant = MaterialTheme.colorScheme.outlineVariant
        NavigationBar(
            modifier = Modifier.drawBehind {
                drawLine(
                    color = outlineVariant.copy(alpha = 0.4f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
        MainDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination
            val itemTitle = stringResource(destination.titleRes)
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "navItemScale_${destination.name}"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = itemTitle,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(
                                scaleX = iconScale,
                                scaleY = iconScale
                            )
                    )
                },
                label = {
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        }
    }
}
