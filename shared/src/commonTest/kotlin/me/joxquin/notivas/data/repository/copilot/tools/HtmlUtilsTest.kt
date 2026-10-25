package me.joxquin.notivas.data.repository.copilot.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlUtilsTest {

    @Test
    fun testTextToHtml_fencedCodeBlockWithJsxDoesNotBreakOrInjectHeading() {
        val markdownInput = """
            1. Los «props» son mecanismos en ReactJS.

            2. Para comunicar componentes se eleva el estado.

            3. Fragmento del componente Saludo:
            ```jsx
            function Saludo({ nombre }) {
              return <h2>¡Hola, {nombre}!</h2>;
            }
            ```

            Fuentes consultadas:
            - Documentación oficial de React.
        """.trimIndent()

        val htmlOutput = HtmlUtils.textToHtml(markdownInput)

        // Verificaciones críticas para evitar el problema de la Imagen 2:
        // 1. No debe existir una etiqueta <h2> viva en el HTML (debe ser texto de código escapado)
        assertFalse(htmlOutput.contains("<h2>¡Hola"), "No debe inyectar la etiqueta <h2> como elemento DOM HTML")
        assertTrue(htmlOutput.contains("&lt;h2&gt;¡Hola, {nombre}!&lt;/h2&gt;"), "Debe escapar las etiquetas JSX dentro de pre code")

        // 2. El identificador de lenguaje `jsx` no debe aparecer dentro del bloque de código
        assertFalse(htmlOutput.contains("<code>jsx"), "No debe incluir el tag del lenguaje 'jsx' dentro de <code>")

        // 3. El bloque de código debe ser un elemento <pre><code> independiente
        assertTrue(htmlOutput.contains("<pre><code>function Saludo({ nombre }) {"), "Debe generar bloque <pre><code> independiente")
        assertFalse(htmlOutput.contains("<p><pre>"), "<pre> no debe estar envuelto dentro de <p>")

        // 4. Los párrafos deben estar separados en etiquetas <p>
        assertTrue(htmlOutput.contains("<p>1. Los «props» son mecanismos en ReactJS.</p>"))
        assertTrue(htmlOutput.contains("<p>2. Para comunicar componentes se eleva el estado.</p>"))
        assertTrue(htmlOutput.contains("<p>3. Fragmento del componente Saludo:</p>"))
    }

    @Test
    fun testTextToHtml_boldItalicAndInlineCode() {
        val markdownInput = "Texto con **negrita**, *cursiva* y código inline `<Saludo nombre=\"Juan\" />`."
        val htmlOutput = HtmlUtils.textToHtml(markdownInput)

        assertEquals(
            "<p>Texto con <strong>negrita</strong>, <em>cursiva</em> y código inline <code>&lt;Saludo nombre=&quot;Juan&quot; /&gt;</code>.</p>",
            htmlOutput
        )
    }

    @Test
    fun testTextToHtml_singleLineBreaksConvertedToBr() {
        val markdownInput = """
            Fuentes consultadas:
            - React Documentation
            - MDN Web Docs
        """.trimIndent()

        val htmlOutput = HtmlUtils.textToHtml(markdownInput)

        assertEquals(
            "<p>Fuentes consultadas:<br/>- React Documentation<br/>- MDN Web Docs</p>",
            htmlOutput
        )
    }

    @Test
    fun testCleanHtml_convertsPreCodeAndFormattingToMarkdown() {
        val canvasHtml = """
            <p><strong>Consigna:</strong> Desarrollar el ejercicio.</p>
            <pre class="enhanceable_content"><code>function Saludo() {
              return &lt;h2&gt;Hola&lt;/h2&gt;;
            }</code></pre>
            <p>Verificar los <em>requisitos</em> y el código <code>npm start</code>.</p>
        """.trimIndent()

        val cleaned = HtmlUtils.cleanHtml(canvasHtml)

        assertTrue(cleaned.contains("**Consigna:**"))
        assertTrue(cleaned.contains("```\nfunction Saludo() {\n  return <h2>Hola</h2>;\n}\n```"))
        assertTrue(cleaned.contains("*requisitos*"))
        assertTrue(cleaned.contains("`npm start`"))
    }

    @Test
    fun testCleanHtml_tableWithHeadersAndRowsConvertedToMarkdownTable() {
        val htmlTableInput = """
            <p>Casos de prueba:</p>
            <table border="1">
              <thead>
                <tr>
                  <th>Caso</th>
                  <th>Datos de entrada</th>
                  <th>Resultado esperado</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>1</td>
                  <td>Tipo de cliente: estudiante</td>
                  <td>S/ 10</td>
                </tr>
                <tr>
                  <td>2</td>
                  <td>Tipo de cliente: público general</td>
                  <td>S/ 20</td>
                </tr>
              </tbody>
            </table>
        """.trimIndent()

        val cleaned = HtmlUtils.cleanHtml(htmlTableInput)

        assertTrue(cleaned.contains("| Caso | Datos de entrada | Resultado esperado |"), "Debe contener encabezados de tabla")
        assertTrue(cleaned.contains("| :--- | :--- | :--- |"), "Debe contener separador Markdown")
        assertTrue(cleaned.contains("| 1 | Tipo de cliente: estudiante | S/ 10 |"), "Debe contener fila 1")
        assertTrue(cleaned.contains("| 2 | Tipo de cliente: público general | S/ 20 |"), "Debe contener fila 2")
    }

    @Test
    fun testTextToHtml_markdownTableConvertedToCanvasHtmlTable() {
        val markdownInput = """
            1. Revisar el trabajo en grupo.

            | Caso | Datos de Entrada | Resultado Esperado |
            | :--- | :--- | :--- |
            | 1 | Estudiante | S/ 10 |
            | 2 | Público general | S/ 20 |

            Fuentes consultadas.
        """.trimIndent()

        val htmlOutput = HtmlUtils.textToHtml(markdownInput)

        assertTrue(htmlOutput.contains("<table border=\"1\" style=\"border-collapse: collapse; width: 100%; margin: 12px 0;\">"))
        assertTrue(htmlOutput.contains("<thead><tr style=\"background-color: #f2f2f2;\"><th style=\"padding: 8px; border: 1px solid #ddd; text-align: left;\">Caso</th>"))
        assertTrue(htmlOutput.contains("<tbody><tr><td style=\"padding: 8px; border: 1px solid #ddd;\">1</td>"))
        assertFalse(htmlOutput.contains("<p><table"), "La tabla no debe estar encerrada en un párrafo <p>")
    }
}

