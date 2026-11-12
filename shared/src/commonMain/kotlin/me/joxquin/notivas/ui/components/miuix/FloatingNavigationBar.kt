// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.copilot_history_title
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import me.joxquin.notivas.shared.resources.nav_ajustes
import me.joxquin.notivas.shared.resources.nav_copilot
import me.joxquin.notivas.shared.resources.nav_herramientas
import me.joxquin.notivas.shared.resources.nav_inicio
import me.joxquin.notivas.ui.navigation.MainDestination
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationBarDefaults
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Book
import top.yukonga.miuix.kmp.icon.glass.Search
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.theme.MiuixTheme

object FloatingNavBarDefaults {
    val Height: Dp = GlassNavigationBarDefaults.Height
    val TotalHeightWithMargins: Dp = 96.dp
}

data class MiuixNavItem(
    val destination: MainDestination,
    val icon: ImageVector,
    val labelRes: org.jetbrains.compose.resources.StringResource,
)

val MiuixNavDestinations = listOf(
    MiuixNavItem(MainDestination.Inicio, MainDestination.Inicio.unselectedIcon, Res.string.nav_inicio),
    MiuixNavItem(MainDestination.Herramientas, MainDestination.Herramientas.unselectedIcon, Res.string.nav_herramientas),
    MiuixNavItem(MainDestination.Copilot, MainDestination.Copilot.unselectedIcon, Res.string.nav_copilot),
    MiuixNavItem(MainDestination.Ajustes, MainDestination.Ajustes.unselectedIcon, Res.string.nav_ajustes),
)

/**
 * FloatingNavigationBar oficial de HyperOS (GlassNavigationBar con acción trailing independiente).
 *
 * Sigue la implementación canónica de compose-miuix-ui:
 * - Cápsula de navegación flotante con indicador chip deslizante animado por resorte.
 * - Acción condicional (botón de búsqueda circular) a la derecha cuando se está en la sección Inicio.
 * - Despliegue y contracción suave con GlassMotion.navShowHide() al cambiar de pestaña.
 */
@Composable
fun FloatingNavigationBar(
    currentDestination: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    backdrop: Backdrop? = null,
    isBlurActive: Boolean = true,
    onSearchClick: (() -> Unit)? = null,
    onHistoryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val destinations = remember {
        listOf(
            MainDestination.Inicio,
            MainDestination.Herramientas,
            MainDestination.Copilot,
            MainDestination.Ajustes,
        )
    }

    val selectedIndex = remember(currentDestination) {
        destinations.indexOf(currentDestination).coerceAtLeast(0)
    }

    val inicioLabel = stringResource(Res.string.nav_inicio)
    val herramientasLabel = stringResource(Res.string.nav_herramientas)
    val copilotLabel = stringResource(Res.string.nav_copilot)
    val ajustesLabel = stringResource(Res.string.nav_ajustes)

    val navItems = remember(inicioLabel, herramientasLabel, copilotLabel, ajustesLabel) {
        listOf(
            GlassNavigationItem(
                icon = MainDestination.Inicio.unselectedIcon,
                label = inicioLabel,
                contentDescription = inicioLabel,
            ),
            GlassNavigationItem(
                icon = MainDestination.Herramientas.unselectedIcon,
                label = herramientasLabel,
                contentDescription = herramientasLabel,
            ),
            GlassNavigationItem(
                icon = MainDestination.Copilot.unselectedIcon,
                label = copilotLabel,
                contentDescription = copilotLabel,
            ),
            GlassNavigationItem(
                icon = MainDestination.Ajustes.unselectedIcon,
                label = ajustesLabel,
                contentDescription = ajustesLabel,
            ),
        )
    }

    val navigationBarBottom = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
        .asPaddingValues().calculateBottomPadding()
    val bottomMargin = if (navigationBarBottom != 0.dp) 16.dp + navigationBarBottom else 32.dp

    val hasTrailingAction = (currentDestination == MainDestination.Inicio && onSearchClick != null) ||
            (currentDestination == MainDestination.Copilot && onHistoryClick != null)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = bottomMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassNavigationBar(
            items = navItems,
            selectedIndex = selectedIndex,
            onSelect = { index ->
                if (index in destinations.indices) {
                    onNavigate(destinations[index])
                }
            },
            backdrop = if (isBlurActive) backdrop else null,
            defaultWindowInsetsPadding = false,
            modifier = Modifier.layout { measurable, constraints ->
                val actionWidth = if (hasTrailingAction) {
                    (GlassNavigationBarDefaults.Height + GlassNavigationBarDefaults.ActionSpacing).roundToPx()
                } else {
                    0
                }
                val available = constraints.maxWidth - actionWidth + (10.dp * (navItems.size - 1) - 6.dp).roundToPx()
                val width = if (constraints.hasBoundedWidth && available > 400.dp.roundToPx()) {
                    constraints.constrainWidth(344.dp.roundToPx() + actionWidth)
                } else {
                    constraints.maxWidth
                }
                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
            },
            shadow = GlassShadows.Float,
            trailingAction = when {
                currentDestination == MainDestination.Inicio && onSearchClick != null -> {
                    {
                        GlassIconButton(
                            onClick = onSearchClick,
                            backdrop = if (isBlurActive) backdrop else null,
                            size = GlassNavigationBarDefaults.Height,
                            shadow = GlassShadows.Float,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Glass.Search,
                                contentDescription = stringResource(Res.string.dashboard_search_hint),
                                modifier = Modifier.size(28.dp),
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                currentDestination == MainDestination.Copilot && onHistoryClick != null -> {
                    {
                        GlassIconButton(
                            onClick = onHistoryClick,
                            backdrop = if (isBlurActive) backdrop else null,
                            size = GlassNavigationBarDefaults.Height,
                            shadow = GlassShadows.Float,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Glass.Book,
                                contentDescription = stringResource(Res.string.copilot_history_title),
                                modifier = Modifier.size(28.dp),
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                else -> null
            },
        )
    }
}
