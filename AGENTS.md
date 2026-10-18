# Guía de Arquitectura de UI Dual: Material Design 3 y HyperOS MIUIX

Este documento establece los estándares y patrones obligatorios para la creación e incorporación de nuevas pantallas (`views`) y componentes en **NotiVas-KMP**, garantizando consistencia arquitectónica, compatibilidad dual y respeto absoluto al sistema de color dinámico Monet.

---

## 1. Arquitectura del TopAppBar: Material 3 vs. HyperOS MIUIX

La aplicación opera bajo dos paradigmas de diseño visual con arquitecturas de navegación diferentes:

### A. HyperOS MIUIX (`AppThemeStyle.MIUIX`)
En MIUIX se utiliza una **arquitectura desacoplada en 3 capas** en [MainAppShell.kt](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/MainAppShell.kt):
1. **Capa 1 (Fondo y Contenido):** Paginador principal (`HorizontalPager`) o vistas a pantalla completa (`AssignmentDraftScreen`).
2. **Capa 2 (Encabezado Unificado - Top Overlay):** Un único [MiuixTopAppBar](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/components/miuix/MiuixTopAppBar.kt) anclado en `Alignment.TopCenter`, fuera del paginador y de las vistas hijas.
3. **Capa 3 (Navegación Flotante):** [FloatingNavigationBar](file:///home/joxquin/AndroidStudioProjects/NotiVas-KMP-Rebuilt/shared/src/commonMain/kotlin/me/joxquin/notivas/ui/components/miuix/FloatingNavigationBar.kt) ("Liquid Glass") anclada en `Alignment.BottomCenter`.

#### Reglas obligatorias para nuevas vistas en MIUIX:
- **No instanciar un TopAppBar dentro de la pantalla hija en modo MIUIX.**
  La vista hija (`Screen`) no debe crear su propio `TopAppBar` si ya está dentro del flujo principal gobernado por `MainAppShell`. Su contenedor raíz debe ser un simple `Box` o `Column` con padding superior compensatorio:
  ```kotlin
  val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  LazyColumn(
      state = lazyListState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
          top = statusBarTop + 68.dp, // Espacio reservado para el MiuixTopAppBar flotante
          bottom = 120.dp,            // Espacio reservado para la barra de navegación flotante
          start = 16.dp,
          end = 16.dp
      )
  )
  ```
- **Control centralizado en `MainAppShell.kt`:**
  - **Título dinámico:** Vincular el título al estado del submódulo activo (ej. `showingArchiveDetail`, `showingNotasDetail`, `showingForosDetail`, `selectedAssignmentForDraft`).
  - **Botón de regreso (`navigationIcon`):** Proporcionar `BackNavigationIcon` invocando el callback de cierre del submódulo.
  - **Visibilidad forzada del título (`titleAlwaysVisible`):** Habilitar `titleAlwaysVisible = true` cuando se encuentre en una sub-pantalla de detalle.
  - **Acciones contextuales (`actions`):** Registrar los iconos de acción (ej. menús desplegables `WindowIconDropdownMenu`, filtros o acciones de sincronización) dentro del bloque `actions` de `MiuixTopAppBar`.
- **Vistas modales / Detalle a pantalla completa (ej. `AssignmentDraftScreen`):**
  - Deben ser elevadas a la Capa 1 de `MainAppShell` para que al abrirse desde cualquier submódulo (Inicio, Herramientas, Archivo, etc.) se rendericen inmediatamente en el nivel raíz.
  - Durante la visualización de una vista modal/borrador, se debe ocultar la `FloatingNavigationBar` y actualizar `MiuixTopAppBar` con su botón de retroceso correspondiente.

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