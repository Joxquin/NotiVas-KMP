package me.joxquin.notivas.ui.navigation.miuix

import androidx.compose.runtime.staticCompositionLocalOf
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavKey

/**
 * Controlador de navegación para miuix-nav respaldado por un [NavBackStack].
 * Soporta operaciones idempotentes de push, replace, pop y popUntil.
 */
class MiuixNavigator(
    val backStack: NavBackStack,
) {
    /**
     * Añade una ruta a la pila de navegación de forma idempotente para evitar duplicados.
     */
    fun push(key: NavKey) {
        if (key !in backStack) {
            backStack.add(key)
        }
    }

    /**
     * Reemplaza la ruta superior o añade si la pila está vacía.
     */
    fun replace(key: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }

    /**
     * Remueve la ruta superior si la pila contiene más de un elemento.
     */
    fun pop(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeLastOrNull() != null
        } else {
            false
        }
    }

    /**
     * Remueve rutas hasta que el predicado coincida con la ruta en la cima.
     */
    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current(): NavKey? = backStack.lastOrNull()

    val canPop: Boolean get() = backStack.size > 1

    fun backStackSize(): Int = backStack.size
}

val LocalMiuixNavigator = staticCompositionLocalOf<MiuixNavigator> {
    error("No se ha provisto LocalMiuixNavigator en la jerarquia de composicion.")
}
