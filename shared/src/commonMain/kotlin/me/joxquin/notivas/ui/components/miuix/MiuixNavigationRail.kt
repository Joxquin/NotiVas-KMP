package me.joxquin.notivas.ui.components.miuix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import me.joxquin.notivas.ui.navigation.MainDestination
import me.joxquin.notivas.ui.util.shouldExpandNavigationRail
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixPersistentNavigationRail(
    destinations: List<MainDestination>,
    currentDestination: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
    badgeCountForHerramientas: Int = 3,
) {
    val expandRail = shouldExpandNavigationRail()
    val railState = rememberNavigationRailState(
        initialValue = if (expandRail) NavigationRailValue.Expanded else NavigationRailValue.Collapsed,
    )

    LaunchedEffect(expandRail) {
        if (expandRail) {
            railState.expand()
        } else {
            railState.collapse()
        }
    }

    NavigationRail(
        state = railState,
        color = MiuixTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                label = stringResource(destination.titleRes),
                badge = if (destination == MainDestination.Herramientas && badgeCountForHerramientas > 0) {
                    {
                        Badge {
                            Text(badgeCountForHerramientas.toString())
                        }
                    }
                } else null,
            )
        }
    }
}

