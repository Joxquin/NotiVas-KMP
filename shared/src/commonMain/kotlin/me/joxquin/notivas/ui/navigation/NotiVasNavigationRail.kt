package me.joxquin.notivas.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotiVasNavigationRail(
    currentDestination: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
    badgeCountForHerramientas: Int = 3,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally { -it },
        exit = fadeOut() + slideOutHorizontally { -it },
        modifier = modifier
    ) {
        val outlineVariant = MaterialTheme.colorScheme.outlineVariant
        NavigationRail(
            modifier = Modifier.drawBehind {
                drawLine(
                    color = outlineVariant.copy(alpha = 0.4f),
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            header = {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                    label = "navRailScale_${destination.name}"
                )

                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        val iconComposable = @Composable {
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
                        }

                        if (destination == MainDestination.Herramientas && badgeCountForHerramientas > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(badgeCountForHerramientas.toString())
                                    }
                                }
                            ) {
                                iconComposable()
                            }
                        } else {
                            iconComposable()
                        }
                    },
                    label = {
                        Text(
                            text = itemTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
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

