# Guía de Arquitectura de UI Dual: Material Design 3 y HyperOS MIUIX

Este documento establece los estándares y patrones obligatorios para la creación e incorporación de nuevas pantallas (`views`) y componentes en **NotiVas-KMP**, garantizando consistencia arquitectónica, compatibilidad dual y respeto absoluto al sistema de color dinámico Monet.

---

## 1. Arquitectura del TopAppBar: Material 3 vs. HyperOS MIUIX

La aplicación opera bajo dos paradigmas de diseño visual con arquitecturas de navegación diferentes:

### A. HyperOS MIUIX (`AppThemeStyle.MIUIX`)
En MIUIX se utiliza la arquitectura oficial de `miuix-nav` basada en la implementación de referencia oficial de `compose-miuix-ui` (`miuix/example`):
0. **Scaffold Raíz Transparente (`MiuixPopupHost`):**
   - El árbol principal de navegación debe estar envuelto en un `top.yukonga.miuix.kmp.basic.Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0))`.
   - Este Scaffold raíz provee `LocalRootDialogStates` y aloja `popupHost = { MiuixPopupHost() }`. Sin este contenedor raíz, los componentes de superposición (`GlassBottomSheet`, `OverlayBottomSheet`, `OverlayDialog`) que utilizan `renderInRootScaffold = true` no pueden renderizarse en pantalla.

1. **Host Principal (`NavDisplay` con `NavTransitions.MiuixDefault`):**
   - Gobernado por `NavBackStack` y `MiuixRoute`.
   - Transiciones oficiales de ventana HyperOS mediante `NavTransitions.MiuixDefault` (o la transición personalizada `CrossActivityTransition` con curva `FastOutExtraSlowIn`, escala a 0.9, desplazamiento de 96dp, `dimAmount` dinámico y resorte `FLING_BOUNCE` al soltar gestos de retroceso).
   - Recorte de esquinas adaptativo al sistema (`NavCornerClipMode.Leading` con `rememberNavSystemCornerRadius()`).

2. **Ruta Raíz (`MiuixRoute.Main`):**
   - Aloja el paginador horizontal (`HorizontalPager`) de las 4 pestañas canónicas (Inicio, Utilidades, Copilot, Ajustes).
   - Cada pestaña canónica aloja su propio encabezado `MiuixBlurredBar` con `MiuixPageTopAppBar` y desenfoque independiente (`rememberMiuixBlurBackdrop`).
   - Aloja la `FloatingNavigationBar` ("Liquid Glass") anclada en `Alignment.BottomCenter`.
   - Al navegar a una pantalla secundaria, `NavDisplay` escala y difumina automáticamente `MiuixRoute.Main` al fondo, retirando de forma natural la barra flotante.

3. **Pantallas Secundarias Modulares (`Screen`):**
   - Cada pantalla secundaria (`RecentActivityScreen`, `ScheduleScreen`, `AssignmentDraftScreen`, `TaskHistoryArchiveScreen`, `ForoDetailScreen`, etc.) es un destino registrado en `NavDisplay`.
   - Cada pantalla secundaria implementa su propio `Scaffold` modular de MIUIX con:
     - `MiuixBlurredBar` con desenfoque (`rememberMiuixBlurBackdrop`).
     - `MiuixPageTopAppBar` con su propio título, `BackNavigationIcon(onClick = onBack)` y acciones contextuales.
     - Barra de desplazamiento háptica nativa `top.yukonga.miuix.kmp.basic.VerticalScrollBar`.
     - Modificadores de scroll overscroll `miuixPageScrollModifiers` y padding `miuixPageContentPadding` de `MiuixPageUtils.kt`.

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

Basado en la implementación de referencia oficial de `compose-miuix-ui` (`example`), se deben utilizar los componentes oficiales de `top.yukonga.miuix.kmp` en lugar de contenedores genéricos con formas simuladas:

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

### 2. Campos de Búsqueda y Entrada (`top.yukonga.miuix.kmp.basic.TextField` vs `InputField`)
- **Campos de formulario y diálogo:** Utilizar `TextField` nativo con título/etiqueta flotante (`label = "..."`), basado en el patrón `TextField with title` del módulo de ejemplo oficial de `compose-miuix-ui`.
  ```kotlin
  val fieldColors = if (themeStyle == AppThemeStyle.MIUIX) {
      TextFieldDefaults.textFieldColors()
  } else {
      TextFieldDefaults.textFieldColors(
          backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
          borderColor = MaterialTheme.colorScheme.primary
      )
  }

  TextField(
      value = textValue,
      onValueChange = { textValue = it },
      label = stringResource(Res.string.mi_campo_label),
      modifier = Modifier.fillMaxWidth(),
      colors = fieldColors
  )
  ```
  *Nota sobre parámetros de `TextFieldDefaults.textFieldColors` en MIUIX:* solo acepta `backgroundColor`, `labelColor` y `borderColor`. No usar `color`, `textColor` ni `cursorColor`.
- **Búsqueda exclusivamente:** Reservar `InputField` únicamente para barras de búsqueda (como en el overlay de búsqueda del Dashboard). No utilizar `InputField` para formularios, creación o edición de entidades.

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

### 5. Hojas y Diálogos Rápidos (GlassBottomSheet vs WindowBottomSheet)
- **HyperOS MIUIX (`GlassBottomSheet`):**
  - Utilizar el componente oficial HyperOS Liquid Glass `top.yukonga.miuix.kmp.glass.GlassBottomSheet`.
  - Debe recibir el fondo difuminado en tiempo real (`backdrop: Backdrop?`) provisto por la pantalla o `rememberMiuixBlurBackdrop()`.
  - El contenedor hijo scrollable debe incorporar `.navigationBarsPadding()` para desplazar con holgura los márgenes del sistema.
  - **Botones de acción y cierre en cabecera de BottomSheet (Prohibido GlassIconButton en Sheets):** Los iconos de cierre (`startAction`) o de acción contextual (`endAction`) dentro de `GlassBottomSheet` deben usar exclusivamente `top.yukonga.miuix.kmp.basic.IconButton` (sin fondo ni contenedor de cápsula, `backgroundColor = Color.Unspecified`). El componente `GlassIconButton` (cápsula circular con sombra y desenfoque) está estrictamente reservado para la barra superior (`TopAppBar`) y la barra flotante (`FloatingNavigationBar`). Nunca debe utilizarse en BottomSheets ni diálogos emergentes.
  - El botón de cierre en `startAction` utiliza `val dismissState = LocalDismissState.current` y `dismissState?.invoke()`.
  - `GlassBottomSheet` maneja sus propios márgenes laterales internos oficiales (`GlassBottomSheetDefaults.InsideMargin = DpSize(12.dp, 0.dp)`).
- **Material Design 3 (`WindowBottomSheet` o `ModalBottomSheet`):**
  - En la rama Material se puede utilizar `WindowBottomSheet` con colores de `MaterialTheme.colorScheme` o `ModalBottomSheet`.
- **Márgenes consistentes y prevención de doble padding:**
  - `WindowBottomSheet` ya incluye por defecto un margen horizontal interno de 24.dp (`insideMargin: DpSize = BottomSheetDefaults.insideMargin`).
  - **Prohibido agregar padding horizontal adicional al contenedor raíz:** La `Column` o `LazyColumn` que aloja el contenido dentro del sheet **NO** debe tener `.padding(horizontal = 16.dp)`, ya que esto suma márgenes innecesarios comprimiendo antiestéticamente las tarjetas y botones. Usar únicamente:
    ```kotlin
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { ... }
    ```
  - No añadir espaciados superiores innecesarios (`padding(top = ...)` excesivo), ya que la cabecera ya incluye su propio drag handle y título.
- **Botones de cabecera condicionales:** Si una vista implementa un estado vacío (`EmptyState`) con un botón de acción principal destacado, el botón equivalente en la cabecera (ej. "Crear grupo") debe condicionarse a `if (items.isNotEmpty()) { ... }` para evitar duplicación innecesaria en la pantalla.

### 6. Sistema Modular de Materiales y Shaders (`miuix-material`)
En las versiones recientes de `compose-miuix-ui` (desde `0.9.4-ca674992` en adelante), los estilos visuales, shaders y sombras se dividieron en el módulo independiente `top.yukonga.miuix.kmp:miuix-material`.
- **Regla estricta de paquetes:**
  - **Sombras y Defaults:** Importar desde `top.yukonga.miuix.kmp.material.*` (ej. `GlassShadows`, `GlassDefaults`, `GlassStyles`, `GlassMaterials`, `GlassShape`). No importar desde `top.yukonga.miuix.kmp.glass.*`.
  - **Shaders y compatibilidad de GPU:** `RuntimeShader`, `asBrush` e `isRuntimeShaderSupported` deben importarse exclusivamente desde `top.yukonga.miuix.kmp.material.*`. No importar desde `top.yukonga.miuix.kmp.blur.*`.
  - **Bordes y relieve:** Para contornos translúcidos con reflejo de luz, utilizar `Modifier.edgeStroke(...)` con `EdgeStrokes.MiddleDark` o `EdgeStrokes.MiddleLight`. La antigua clase `GlassStroke` está deprecada.

### 7. Barra de Navegación Flotante (`top.yukonga.miuix.kmp.glass.GlassNavigationBar`)
- La barra flotante oficial de HyperOS utiliza el componente nativo `GlassNavigationBar` alojado en `FloatingNavigationBar.kt`:
  - Se alimenta de `GlassNavigationItem(icon, label, contentDescription)`.
  - Debe omitir los insets automáticos con `defaultWindowInsetsPadding = false` cuando esté contenida dentro de un contenedor alineado con paddings calculados (`WindowInsets.navigationBars`).
  - Utilizar `shadow = GlassShadows.Float` para la elevación estándar de la cápsula flotante.

### 8. Popups Flotantes y Menús Contextuales (`top.yukonga.miuix.kmp.glass.GlassTransformPopup`)
- Para menús contextuales en cabeceras o listas (como el selector de cursos en Notas o acciones en Borradores):
  - Utilizar `rememberGlassPopupAnchor()` sobre el elemento disparador y enlazar con `Modifier.glassPopupAnchor(anchor)`.
  - Implementar `GlassTransformPopup` o `GlassPopup` que calcula automáticamente coordenadas de ventana sin desbordar el viewport seguro.

### 9. Pull-to-Refresh Nativo (`top.yukonga.miuix.kmp.basic.PullToRefresh`)
- Para vistas con actualización por deslizamiento en MIUIX (ej. `InicioMiuixContent`):
  - Utilizar `val pullToRefreshState = rememberPullToRefreshState()` y envolver con `PullToRefresh(pullToRefreshState = pullToRefreshState, onRefresh = { ... })`.
  - Sincronizar con el scroll overscroll elástico vertical (`Modifier.overScrollVertical()`), permitiendo amortiguación y desvanecimiento suave de texto de completado sin interrupciones.

### 10. Selectores de Fecha y Hora Unificados (`top.yukonga.miuix.kmp.basic.NumberPicker`)
Para diálogos y hojas emergentes de selección de fecha y hora (como recordatorios personalizados):
- **Una Sola Tarjeta Compacta:** No fragmentar en múltiples tarjetas separadas ni 5 ruedas individuales (Día, Mes, Año, Hora, Minuto). Alojar todo en un único `Card` de MIUIX (`surfaceContainer`) con `insideMargin = PaddingValues(0.dp)`.
- **Rango Compacto (`visibleItemCount = 3`):** Usar siempre `visibleItemCount = 3` en lugar del valor por defecto de 5, reduciendo la altura vertical para que encaje de forma cómoda dentro del `WindowBottomSheet`.
- **Columnas y Ruedas:**
  - **Fecha Relativa Continua (Columna 1, weight 2.5f):** Rango de días (`0..180`) a partir de hoy con etiquetas localizadas (`lun, 24 de nov`, `mar, 25 de nov`), eliminando fechas inválidas (como 31 de febrero) y bloqueando desplazamientos hacia el pasado (`wrapAround = false`).
  - **Formato 12 Horas (Columnas 2, 3 y 4):**
    - Hora: `1..12` (`wrapAround = true`, weight 1f).
    - Minutos: `0..59` con formato `00..59` (`wrapAround = true`, weight 1f).
    - Período: `0..1` para `a.m.` / `p.m.` (`wrapAround = false`, weight 1.1f).
- **Indicadores Centrales de Selección:** Dibujar dos líneas horizontales sutiles usando `Modifier.drawBehind` con `color = MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.45f)` a la altura de `itemHeight` e `itemHeight * 2`, enmarcando la fila activa.
- **Inicialización Segura en el Futuro:** Calcular la selección inicial a +1 hora en el futuro (o 9:00 a.m. del día siguiente si se abre en la noche), evitando que se disparen avisos de "fecha pasada" al abrir el diálogo.

### 11. Renderizado de Fórmulas y Bloques Matemáticos (`CopilotMathBlockComposable`)
- **Párrafo Propio y Dedicado:** Cualquier cálculo, fórmula LaTeX o expresión matemática en Copilot IA debe aislarse de los bloques de texto ordinarios y renderizarse en su propio contenedor visual dedicado (`CopilotContentBlock.MathBlock`).
- **Limpieza de LaTeX (`MathFormatter`):** Convertir la notación LaTeX a texto Unicode matemático limpio (`MathFormatter.formatLatexToMathText`), traduciendo fracciones (`\frac{a}{b}` a `(a) / (b)`), raíces (`\sqrt`), operadores (`\times` a `×`, `\cdot` a `·`), letras griegas (`\mu`, `\alpha`, `\beta`) y estilos tipográficos (`\mathbf`, `\text`).
- **Estética Dual:**
  - **HyperOS MIUIX:** Contenedor con `Modifier.squircleSurface(color = MiuixTheme.colorScheme.surfaceContainer, cornerRadius = 14.dp)`, borde suave `squircleBorder`, insignia de "Cálculo" en acento primario y botón háptico para copiar la fórmula limpia al portapapeles.
  - **Material Design 3:** `androidx.compose.material3.Card` con `surfaceContainerHigh` y esquinas redondeadas de 14.dp.

### 12. Conjugación y Transiciones de Búsqueda HyperOS (`me.joxquin.notivas.ui.components.miuix`)
Al implementar overlays o pantallas de búsqueda con el `TopAppBar`:
- Utilizar la curva elástica oficial de MIUIX / HyperOS `SearchModeEasing` (400ms spring con amortiguación nativa).
- Conjugar la barra de navegación y el toolbar con `Modifier.glassSearchBarTransition(isOverlayExpanded, ...)`:
  - Escalar la barra superior suavemente a 0.9.
  - Aplicar desenfoque progresivo hasta 50f.
  - Desvanecer opacidad con `rememberGlassSearchTitleAlpha(isOverlayExpanded)`.
- El botón circular de cancelar (`MiuixIcons.Glass.Close`) debe animarse lateralmente (traslación de 66dp) con escalado y desvanecimiento sincronizado.

---

## 3. Navegación Idempotente y Prevención de Doble Retroceso (`onBack`)

Un error recurrente al eliminar o modificar entidades desde una pantalla de detalle es la ejecución duplicada de `onBack()`, lo que desapila dos pantallas consecutivas en el `NavDisplay` (ej. saltando de Detalle a Progreso y de inmediato retrocediendo a Utilidades).

### Reglas obligatorias:
1. **Cero `LaunchedEffect(entity) { if (entity == null) onBack() }` cuando ya hay acción explícita:** Si el usuario confirma una acción destructiva desde un diálogo de confirmación, la navegación de retorno debe ejecutarse **exclusivamente en el callback del botón** y nunca duplicarse mediante un `LaunchedEffect` que reaccione al cambio de estado en la base de datos.
2. **Bandera de guardia `isNavigatingBack` o `isDeleted`:** Proteger el callback para asegurar que `onBack()` se invoque exactamente una sola vez:
   ```kotlin
   var isDeleted by remember { mutableStateOf(false) }

   confirmButton = {
       TextButton(
           onClick = {
               if (!isDeleted) {
                   isDeleted = true
                   showConfirmDialog = false
                   viewModel.deleteItem(item)
                   onBack()
               }
           }
       ) { ... }
   }
   ```
3. **Caché del modelo visual durante la transición de salida (`lastValidModel`):**
   Para evitar parpadeos o pantallas en blanco durante la animación de salida de la ventana (~300ms de `CrossActivityTransition`), mantener en memoria el último estado válido de la entidad:
   ```kotlin
   val currentModel = uiState.items.find { it.id == itemId }
   var lastValidModel by remember { mutableStateOf(currentModel) }
   if (currentModel != null) {
       lastValidModel = currentModel
   }
   val model = currentModel ?: lastValidModel

   if (model == null) return
   ```

---

## 4. Reglas de Formateo de Strings e i18n (`composeResources`)

Compose Multiplatform procesa los recursos de strings mediante un motor propio y no utiliza el formateador estándar de Java `String.format` que exige escapar el porcentaje con `%%`.

### Reglas estrictas para `strings.xml`:
1. **Símbolo de porcentaje (`%`):** Usar siempre un único `%`.
   - **Incorrecto:** `<string name="total_weight">Total: %1$d%%</string>` (Produce `Total: 0%%` en pantalla).
   - **Correcto:** `<string name="total_weight">Total: %1$d%</string>` (Produce `Total: 0%` en pantalla).
2. **Sincronización exacta:** Cada string añadido en `values-es/strings.xml` DEBE estar presente con la misma clave en `values/strings.xml`.
3. **Prohibido hardcodear textos:** Ningún texto visible para el usuario puede estar en el código Kotlin; todos deben referenciarse vía `stringResource(Res.string.clave)`.

---

## 5. Respeto al Sistema de Color Dinámico Monet (`MiuixTheme.colorScheme`)

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

## 6. Checklist para Nuevas Vistas (Views)
1. **¿Se implementó el diseño dual?** Ramificar con `if (themeStyle == AppThemeStyle.MIUIX) { ... } else { ... }`.
2. **¿Se respetó el TopAppBar en MIUIX?** Comprobar que la vista no duplique el `TopAppBar` y que esté integrada con `MiuixBlurredBar` y `MiuixPageTopAppBar` de `MiuixPageUtils.kt`.
3. **¿Se usaron los componentes nativos correctos?** Usar `Card`, `TextField` (with title para formularios, no `InputField`), `SmallTitle`, `TextButton`.
4. **¿Márgenes y paddings consistentes?** Padding horizontal de 16.dp en pantallas completas; en `WindowBottomSheet`, **NO** añadir `.padding(horizontal = ...)` al contenedor raíz, ya que el bottom sheet ya aplica 24.dp por defecto (`BottomSheetDefaults.insideMargin`).
5. **¿Selectores de fecha/hora unificados?** Utilizar una sola tarjeta con `visibleItemCount = 3`, fecha relativa continua e inicialización en el futuro (+1h).
6. **¿Expresiones matemáticas aisladas?** Usar `CopilotMathBlockComposable` en un párrafo propio para fórmulas de Copilot.
7. **¿Navegación protegida contra doble `onBack`?** No usar `LaunchedEffect` para retroceder tras eliminar; proteger con banderas y guardar caché `lastValidModel`.
8. **¿Botones no redundantes?** Los botones de cabecera deben ocultarse cuando el estado vacío ya provee el CTA principal.
9. **¿Formato de strings e i18n correcto?** Comprobar que los porcentajes usen `%1$d%` (no `%%`), y que todas las claves existan en `values` y `values-es`.
10. **¿Se eliminaron los colores hexadecimales estáticos?** Usar exclusivamente `MiuixTheme.colorScheme.*`.
11. **¿Paquetes de materiales e imports correctos?** Importar `GlassShadows`, `GlassDefaults`, `RuntimeShader` e `isRuntimeShaderSupported` desde `top.yukonga.miuix.kmp.material.*` (no desde `.glass` ni `.blur`).
12. **¿Regla de emojis cumplida?** Cero emojis en código, textos, logs o mensajes de commit.
13. **¿Iconos en BottomSheet sin contenedor?** En `GlassBottomSheet` con `progressiveBlur = true`, usar `GlassIconButton` con `MiuixIcons.Glass.*` en `startAction`/`endAction` para heredar el material, el backdrop y la animación del header, como en el ejemplo oficial de Miuix 0.9.5-rc01. En `WindowBottomSheet` y `ModalBottomSheet`, usar `IconButton` de `top.yukonga.miuix.kmp.basic`.

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