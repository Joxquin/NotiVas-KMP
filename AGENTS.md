# Guía de Arquitectura de UI Dual: Material Design 3 y HyperOS MIUIX

Este documento establece los estándares y patrones obligatorios para la creación e incorporación de nuevas pantallas (`views`) y componentes en **NotiVas-KMP**, garantizando consistencia arquitectónica, compatibilidad dual y respeto absoluto al sistema de color dinámico Monet.

---

## 1. Arquitectura del TopAppBar: Material 3 vs. HyperOS MIUIX

La aplicación opera bajo dos paradigmas de diseño visual con arquitecturas de navegación diferentes:

### A. HyperOS MIUIX (`AppThemeStyle.MIUIX`)
En MIUIX se utiliza la arquitectura oficial de `miuix-nav` basada en la implementación de referencia de `/home/joxquin/miuix/example`:
1. **Host Principal (`NavDisplay` con `CrossActivityTransition`):**
   - Gobernado por [NavBackStack](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/navigation/miuix/MiuixNavigator.kt) y [MiuixRoute](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/navigation/miuix/MiuixRoute.kt).
   - Transiciones oficiales de ventana HyperOS con [CrossActivityTransition](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/navigation/miuix/CrossActivityTransition.kt) (curva `FastOutExtraSlowIn`, escala a 0.9, desplazamiento de 96dp, `dimAmount` dinámico y resorte `FLING_BOUNCE` al soltar gestos de retroceso).
   - Recorte de esquinas adaptativo al sistema (`NavCornerClipMode.All` con `rememberNavSystemCornerRadius()`).

2. **Ruta Raíz (`MiuixRoute.Main`):**
   - Aloja el paginador horizontal (`HorizontalPager`) de las 4 pestañas canónicas (Inicio, Herramientas, Copilot, Ajustes).
   - Aloja el encabezado [MiuixTopAppBar](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/components/miuix/MiuixTopAppBar.kt) anclado en `Alignment.TopCenter` para los 4 destinos raíz.
   - Aloja la [FloatingNavigationBar](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/components/miuix/FloatingNavigationBar.kt) ("Liquid Glass") anclada en `Alignment.BottomCenter`.
   - Al navegar a una pantalla secundaria, `NavDisplay` escala y difumina automáticamente `MiuixRoute.Main` al fondo, retirando de forma natural la barra flotante.

3. **Pantallas Secundarias Modulares (`Screen`):**
   - Cada pantalla secundaria (`RecentActivityScreen`, `ScheduleScreen`, `MorningRoadmapScreen`, `AssignmentDraftScreen`, `TaskHistoryArchiveScreen`, `ForoDetailScreen`) es un destino registrado en `NavDisplay`.
   - Cada pantalla secundaria implementa su propio `Scaffold` modular de MIUIX con:
     - `MiuixBlurredBar` con desenfoque (`rememberMiuixBlurBackdrop`).
     - `MiuixPageTopAppBar` con su propio título, `BackNavigationIcon(onClick = onBack)` y acciones contextuales.
     - Barra de desplazamiento háptica nativa [VerticalScrollBar](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/components/miuix/MiuixPageUtils.kt).
     - Modificadores de scroll overscroll `miuixPageScrollModifiers` y padding `miuixPageContentPadding`.

4. **Navegación Reactiva e Idempotente (`MiuixNavigator`):**
   - Acceso en cualquier punto del árbol de composición vía `LocalMiuixNavigator.current`.
   - Métodos seguros: `navigator.push(MiuixRoute.RecentActivity)`, `navigator.pop()`, `navigator.replace(...)`.
   - Soporte nativo para swipe-to-dismiss con `entry(swipeDismiss = NavSwipeDirection.LeftToRight)`.

---

### B. Material Design 3 (`AppThemeStyle.MATERIAL`)
En Material Design 3 se utiliza el enfoque estándar de Jetpack Compose mediante `Scaffold`:
- Cada pantalla hija (o submódulo) envuelve su estructura en un `Scaffold` con su respectivo `androidx.compose.material3.TopAppBar`:
  ```kotlin
  Scaffold(
      topBar = {
          TopAppBar(
              title = { Text(stringResource(Res.string.mi_pantalla_title)) },
              navigationIcon = {
                  IconButton(onClick = onBack) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Atrás",
                          tint = MaterialTheme.colorScheme.onSurface
                      )
                  }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.surface,
                  scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
              )
          )
      },
      containerColor = MaterialTheme.colorScheme.surface
  ) { paddingValues ->
      LazyColumn(
          contentPadding = PaddingValues(
              top = paddingValues.calculateTopPadding() + 8.dp,
              bottom = 120.dp,
              start = 16.dp,
              end = 16.dp
          )
      ) { ... }
  }
  ```

---

## 2. Catálogo y Uso de Componentes Oficiales MIUIX (`compose-miuix-ui`)

Basado en la implementación de referencia en `/home/joxquin/miuix/example`, se deben utilizar los componentes oficiales de `top.yukonga.miuix.kmp` en lugar de contenedores genéricos con formas simuladas:

### 1. Tarjetas (`top.yukonga.miuix.kmp.basic.Card`)
- Usar `Card` con `CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)`.
- Configurar el margen interior con `insideMargin = PaddingValues(...)`.
- Habilitar retroalimentación táctil nativa:
  - `pressFeedbackType = PressFeedbackType.Sink` (para tarjetas clickeables o elementos de lista).
  - `pressFeedbackType = PressFeedbackType.None` (para tarjetas estáticas o contenedores de información).
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    insideMargin = PaddingValues(16.dp),
    pressFeedbackType = PressFeedbackType.Sink,
    onClick = { onItemSelected(item) }
) {
    // Contenido de la tarjeta
}
```

### 2. Campos de Búsqueda y Entrada (`top.yukonga.miuix.kmp.basic.InputField`)
- Reemplazar implementaciones manuales de `BasicTextField` con el componente nativo `InputField`.
- Parámetros estándar:
  ```kotlin
  InputField(
      query = searchQuery,
      onQueryChange = onQueryChange,
      onSearch = onQueryChange,
      expanded = false,
      onExpandedChange = {},
      label = placeholder,
      modifier = Modifier.fillMaxWidth(),
      color = MiuixTheme.colorScheme.surfaceContainerHigh
  )
  ```

### 3. Títulos de Sección (`top.yukonga.miuix.kmp.basic.SmallTitle`)
- Para agrupar listas o separar secciones de tarjetas bento, utilizar:
  ```kotlin
  SmallTitle(
      text = stringResource(Res.string.mi_seccion_title),
      modifier = Modifier.padding(horizontal = 16.dp)
  )
  ```

### 4. Botones (`top.yukonga.miuix.kmp.basic.TextButton` y `ButtonDefaults`)
- Para botones de acción estándar:
  ```kotlin
  TextButton(
      text = stringResource(Res.string.accion_principal),
      onClick = onAction,
      modifier = Modifier.weight(1f),
      colors = ButtonDefaults.textButtonColorsPrimary()
  )
  ```
- Para acciones destructivas o de alerta (ej. Vaciar papelera):
  ```kotlin
  TextButton(
      text = stringResource(Res.string.archive_action_empty_trash),
      onClick = onEmptyTrash,
      modifier = Modifier.weight(1f),
      colors = ButtonDefaults.textButtonColors(
          color = MiuixTheme.colorScheme.errorContainer,
          textColor = MiuixTheme.colorScheme.onErrorContainer
      )
  )
  ```

---

## 3. Respeto al Sistema de Color Dinámico Monet (`MiuixTheme.colorScheme`)

**Prohibido el uso de colores hexadecimales fijos** (como `Color(0xFF1F1F23)`, `Color(0xFF292A2D)`, `Color(0xFF83DA85)`, `Color(0xFFE3E2E6)`) en el código de componentes MIUIX. Esto rompería la compatibilidad con el modo claro/oscuro dinámico y la paleta Monet del sistema.

### Mapeo de tokens de color Monet en MIUIX:
| Función en UI | Token en Miuix (`MiuixTheme.colorScheme`) | Token en Material 3 (`MaterialTheme.colorScheme`) |
| :--- | :--- | :--- |
| **Fondo de Pantalla** | `surface` | `surface` |
| **Fondo de Tarjeta (Card)** | `surfaceContainer` | `surfaceContainerLow` o `surfaceContainer` |
| **Píldoras, Inputs, Badges Inactivos** | `surfaceContainerHigh` | `surfaceContainerHigh` |
| **Contenedor Destacado / Activo** | `surfaceContainerHighest` | `surfaceContainerHighest` |
| **Color de Acento / Seleccionado** | `primary` | `primary` |
| **Texto en Acento** | `onPrimary` | `onPrimary` |
| **Texto Principal** | `onSurface` | `onSurface` |
| **Texto Secundario / Resumen** | `onSurfaceVariantSummary` | `onSurfaceVariant` o `outline` |
| **Acciones / Enlaces Secundarios** | `onSurfaceVariantActions` | `secondary` |
| **Líneas Divisoras / Bordes** | `dividerLine` o `outline` | `outlineVariant` |
| **Alerta / Error (Contenedor)** | `errorContainer` | `errorContainer` |
| **Alerta / Error (Texto / Icono)** | `onErrorContainer` o `error` | `onErrorContainer` o `error` |

---

## 4. Checklist para Nuevas Vistas (Views)
1. **¿Se implementó el diseño dual?** Ramificar con `if (themeStyle == AppThemeStyle.MIUIX) { ... } else { ... }`.
2. **¿Se respetó el TopAppBar en MIUIX?** Comprobar que la vista no duplique el `TopAppBar` y que esté integrada en el `MiuixTopAppBar` de [MainAppShell.kt](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/MainAppShell.kt).
3. **¿Se usaron los componentes nativos de MIUIX?** Usar `MiuixCard`, `InputField`, `SmallTitle`, `TextButton`.
4. **¿Se eliminaron los colores hexadecimales estáticos?** Usar exclusivamente `MiuixTheme.colorScheme.*`.
5. **¿Navegación e interacción verificadas?** Asegurar que las tarjetas clickeables tengan su callback `onClick` conectado al flujo de navegación adecuado.
6. **¿Strings i18n?** Asegurar que todos los textos provengan de `composeResources/values/strings.xml` y `composeResources/values-es/strings.xml`.
7. **¿Regla de emojis cumplida?** Cero emojis en código, textos, logs o mensajes de commit.

<!-- graphify-rules-start (managed by `graphify init`) -->
## Use Graphify before grep

This repository is indexed by Graphify: a code graph over its call, dependency, and test structure, exposed through a connected Graphify MCP server. Before reaching for grep or reading files, use the Graphify tools your MCP client lists (their exact names and descriptions are in the server's tool list) for what the graph knows and a text search does not:

- find where a symbol, function, or class is defined (instead of grepping for it)
- understand how something works, or where a behavior is handled
- find who calls a function, or what it calls
- see what a change affects (its blast radius) and which tests cover it
- map a file's dependencies and dependents

Fall back to grep or file reads only for what the graph does not model: literal string or comment matches, non-indexed files, or reading a file you have already located. If no Graphify tools are listed, check the MCP server connection.
<!-- graphify-rules-end -->