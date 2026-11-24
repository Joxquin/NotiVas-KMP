package me.joxquin.notivas.ui.dashboard.draft

import me.joxquin.notivas.ui.dashboard.draft.components.ConsignaIconType
import me.joxquin.notivas.ui.dashboard.draft.components.detectExtensionsFromHtml
import me.joxquin.notivas.ui.dashboard.draft.components.parseConsignaSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AssignmentConsignaAndFormatTest {

    @Test
    fun testDetectExtensionsFromHtml_pdfOnly() {
        val html = "<p>Formato: Documento en formato PDF (debidamente identificado con sus datos).</p>"
        val detected = detectExtensionsFromHtml(html)
        assertEquals(listOf("pdf"), detected)
    }

    @Test
    fun testDetectExtensionsFromHtml_multipleFormats() {
        val html = "<p>Formatos permitidos: pdf, docx, zip</p>"
        val detected = detectExtensionsFromHtml(html)
        assertTrue(detected.contains("pdf"))
        assertTrue(detected.contains("docx"))
        assertTrue(detected.contains("zip"))
    }

    @Test
    fun testDetectExtensionsFromHtml_blankOrNoMention() {
        val html = "<p>Realizar la actividad en clase</p>"
        val detected = detectExtensionsFromHtml(html)
        assertTrue(detected.isEmpty())
    }

    @Test
    fun testParseConsignaSections_structuredLab() {
        val sampleHtml = """
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <h3>Capacidad</h3>
                <div style="margin-top: 10px;">Aprender y aplicar los conocimientos prácticos asignados en esta sesión de laboratorio.</div>
            </div>
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <h3>Procedimientos</h3>
                <div>
                    <p>Descargue la guía adjunta e implemente los ejercicios propuestos:</p>
                    <a href="https://docs.google.com/document/d/sample/edit">Descargar Guía de Laboratorio</a>
                </div>
            </div>
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <h3>Prerrequisito</h3>
                <div>Revisar previamente el material de lectura.</div>
            </div>
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <h3>Especificaciones de la Entrega</h3>
                <div>
                    <ul>
                        <li><strong>Modalidad:</strong> Individual.</li>
                        <li><strong>Formato:</strong> Documento en formato PDF.</li>
                        <li style="color: #c53030;"><strong>Fecha límite de entrega:</strong> <span style="border: 1px solid #feb2b2;">Hasta el domingo (23:59 hrs)</span>.</li>
                    </ul>
                </div>
            </div>
        """.trimIndent()

        val sections = parseConsignaSections(sampleHtml)
        assertEquals(4, sections.size)

        // 1. Capacidad
        assertEquals("Capacidad", sections[0].title)
        assertEquals(ConsignaIconType.CAPACITY, sections[0].iconType)
        assertTrue(sections[0].introText.contains("Aprender y aplicar"))

        // 2. Procedimientos
        assertEquals("Procedimientos", sections[1].title)
        assertEquals(ConsignaIconType.PROCEDURE, sections[1].iconType)
        assertNotNull(sections[1].downloadLink)
        assertEquals("https://docs.google.com/document/d/sample/edit", sections[1].downloadLink?.second)

        // 3. Prerrequisito
        assertEquals("Prerrequisito", sections[2].title)
        assertEquals(ConsignaIconType.PREREQUISITE, sections[2].iconType)

        // 4. Especificaciones de la Entrega
        assertEquals("Especificaciones de la Entrega", sections[3].title)
        assertEquals(ConsignaIconType.SPECIFICATION, sections[3].iconType)
        assertEquals(3, sections[3].bullets.size)
        assertEquals("Modalidad", sections[3].bullets[0].label)
        assertEquals("Individual.", sections[3].bullets[0].value)
        assertTrue(sections[3].bullets[2].isHighlighted)
    }

    @Test
    fun testParseConsignaSections_genericAssignment() {
        val genericHtml = "<p>Revisar el material adjunto y descargar <a href=\"https://canvas.test/files/123/download\">archivo.docx</a></p>"
        val sections = parseConsignaSections(genericHtml)
        assertEquals(1, sections.size)
        assertEquals("Instrucciones de la Actividad", sections[0].title)
        assertNotNull(sections[0].downloadLink)
        assertEquals("https://canvas.test/files/123/download", sections[0].downloadLink?.second)
    }

    @Test
    fun testParseConsignaSections_withImages() {
        val imageHtml = """<p><img src="/courses/72704/files/9876/preview" alt="diagrama"></p>"""
        val sections = parseConsignaSections(imageHtml, baseUrl = "https://tecsup.instructure.com")
        assertEquals(1, sections.size)
        assertEquals(1, sections[0].imageUrls.size)
        assertEquals("https://tecsup.instructure.com/courses/72704/files/9876/preview", sections[0].imageUrls[0])
    }

    @Test
    fun testParseConsignaSections_withHeaderIconsAndPreservedLinkText() {
        val sampleHtml = """
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <div style="display: table-cell; vertical-align: middle; width: 45px;">
                    <img src="https://canvas.test/icon_capacidad.png" alt="Capacidad" width="35" height="35">
                </div>
                <div style="display: table-cell; vertical-align: middle; padding-left: 12px;">
                    <h3>Capacidad</h3>
                </div>
                <div style="margin-top: 10px;">Aprender y aplicar los conocimientos prácticos asignados en esta sesión de laboratorio.</div>
            </div>
            <div style="background-color: #f8fafc; border-left: 5px solid #003366;">
                <div style="display: table-cell; vertical-align: middle; width: 45px;">
                    <img src="https://canvas.test/icon_proc.png" alt="Procedimientos" width="35" height="35">
                </div>
                <div style="display: table-cell; vertical-align: middle; padding-left: 12px;">
                    <h3>Procedimientos</h3>
                </div>
                <div>
                    <p>Descargue la guía adjunta e implemente los ejercicios propuestos:</p>
                    <a href="https://docs.google.com/document/d/sample/edit">Descargar Guía de Laboratorio</a>
                    <p>Para consultas, ingresar al <a href="https://forum.test">Foro de Consulta del Curso</a>.</p>
                </div>
            </div>
        """.trimIndent()

        val sections = parseConsignaSections(sampleHtml)
        assertEquals(2, sections.size)

        // Capacidad
        assertEquals("Capacidad", sections[0].title)
        assertEquals("https://canvas.test/icon_capacidad.png", sections[0].iconUrl)
        assertTrue(sections[0].imageUrls.isEmpty(), "Header icon should not leak into imageUrls")
        assertTrue(sections[0].introText.contains("Aprender y aplicar"))

        // Procedimientos
        assertEquals("Procedimientos", sections[1].title)
        assertEquals("https://canvas.test/icon_proc.png", sections[1].iconUrl)
        assertTrue(sections[1].imageUrls.isEmpty(), "Header icon should not leak into imageUrls")
        assertNotNull(sections[1].downloadLink)
        assertEquals("https://docs.google.com/document/d/sample/edit", sections[1].downloadLink?.second)
        // Link text is preserved
        assertTrue(sections[1].introText.contains("Foro de Consulta del Curso"), "Link text must be preserved")
    }

    @Test
    fun testParseConsignaSections_canvasAssignmentWithOrderedListAndInlineLinks() {
        val pc03Html = """
            <link rel="stylesheet" href="https://canvas.test/theme.css">
            <p><span>La </span><strong><span>PC-03</span></strong><span> debe desarrollarse tomando como base el proyecto trabajado en el laboratorio.</span></p>
            <ol start="1">
                <li>
                    <p><strong><span>Desarrollo del proyecto:</span></strong><span> presenten las cuatro primeras etapas de Design Thinking: </span><strong><span>empatización, definir, ideación y prototipado</span></strong><span>.</span></p>
                </li>
                <li>
                    <p><strong><span>Material de apoyo:</span></strong><span> revisen la </span><a href="https://external.test/guia"><span>Guía de Investigación e Innovación Tecnológica</span></a><span>. </span><strong><span>Lean y apliquen la teoría</span></strong><span> a su proyecto.</span></p>
                </li>
                <li>
                    <p><strong><span>Formato del Perfil de Proyecto:</span></strong><span> utilicen el documento disponible en este enlace: </span><a href="https://docs.google.com/document/d/sample123/edit"><span><strong>Acceder al formato del Perfil de Proyecto</strong></span></a><span>.</span></p>
                </li>
            </ol>
            <p><span>📅 </span><strong><span>Fecha límite: 8 de noviembre de 2026, a las 21:00 horas.</span></strong></p>
            <p><strong><span>Docente:</span></strong><span> Harry Elvis Ccoaquira Enríquez</span><br><strong><span>Curso:</span></strong><span> Investigación e Innovación Tecnológica – TEORÍA</span></p>
            <script src="https://canvas.test/logo.js"></script>
        """.trimIndent()

        val sections = parseConsignaSections(pc03Html)
        assertEquals(1, sections.size)
        val section = sections[0]
        assertEquals("Instrucciones de la Actividad", section.title)
        assertTrue(section.introText.contains("La **PC-03** debe desarrollarse"))
        assertEquals(3, section.bullets.size)

        // Bullet 1
        assertEquals("1.", section.bullets[0].index)
        assertEquals("Desarrollo del proyecto", section.bullets[0].label)
        assertTrue(section.bullets[0].value.contains("empatización, definir, ideación y prototipado"))

        // Bullet 2 (Inline Link preserved)
        assertEquals("2.", section.bullets[1].index)
        assertEquals("Material de apoyo", section.bullets[1].label)
        assertTrue(section.bullets[1].value.contains("[Guía de Investigación e Innovación Tecnológica](https://external.test/guia)"))

        // Bullet 3 (Download Link & Inline Link)
        assertEquals("3.", section.bullets[2].index)
        assertEquals("Formato del Perfil de Proyecto", section.bullets[2].label)
        assertNotNull(section.downloadLink)
        assertEquals("https://docs.google.com/document/d/sample123/edit", section.downloadLink?.second)

        // Footer notes (Deadline and Teacher/Course)
        assertTrue(section.footerNotes.isNotEmpty())
        assertTrue(section.footerNotes.any { it.contains("Fecha límite") })
        assertTrue(section.footerNotes.any { it.contains("Docente:") })
    }

    @Test
    fun testParseConsignaSections_canvasAssignmentWithPreludeAndPostListNotes() {
        val assignmentHtml = """
            <p>En equipos de hasta cinco integrantes, investiguen cómo se utilizan las pruebas estáticas para evaluar la calidad del código fuente. A partir de lo investigado, elaboren una propuesta de revisión para un proyecto de software elegido por el equipo. Puede ser un proyecto propio, uno de código abierto o un proyecto ficticio que describan brevemente.</p>
            <h3>Puntos que deben desarrollar</h3>
            <ol>
                <li><strong>Conceptos fundamentales.</strong> Expliquen qué son las pruebas estáticas, qué tipos de defectos permiten identificar y cómo se complementan con las pruebas dinámicas.</li>
                <li><strong>Herramientas.</strong> Investiguen y comparen tres herramientas de análisis estático apropiadas para el lenguaje del proyecto elegido.</li>
                <li><strong>Métricas de calidad.</strong> Expliquen al menos tres métricas relevantes para revisar el código.</li>
                <li><strong>Criterios de revisión.</strong> Propongan una lista breve de aspectos que el equipo debería revisar antes de integrar un cambio.</li>
                <li><strong>Puerta de calidad.</strong> Definan al menos tres condiciones para aceptar un cambio de código.</li>
                <li><strong>Conclusión.</strong> Recomienden una de las herramientas investigadas y expliquen sus ventajas y limitaciones.</li>
            </ol>
            <p>Consulten al menos tres fuentes confiables e incluyan las referencias en formato APA 7.</p>
            <h3>Presentación y entrega</h3>
            <p>Durante la clase, cada equipo presentará su investigación en el formato que considere más adecuado: PPT, infografía, póster digital u otro recurso visual. Todos los integrantes deben participar y poder justificar las decisiones del equipo.</p>
            <p>Como tarea, deberán desarrollar un documento por equipo que reúna todos los puntos desarrollados y sus referencias. El documento es el entregable, cada integrante debera subir el documento a Canvas; el formato visual queda a elección del equipo para la exposición en clase.</p>
        """.trimIndent()

        val sections = parseConsignaSections(assignmentHtml)
        assertEquals(3, sections.size)

        // 1. Preludio / Descripción
        assertEquals("Descripción", sections[0].title)
        assertEquals(ConsignaIconType.GENERAL, sections[0].iconType)
        assertTrue(sections[0].introText.contains("En equipos de hasta cinco integrantes"))
        assertTrue(sections[0].bullets.isEmpty())
        assertTrue(sections[0].footerNotes.isEmpty())

        // 2. Puntos que deben desarrollar
        assertEquals("Puntos que deben desarrollar", sections[1].title)
        assertTrue(sections[1].introText.isBlank(), "Intro text should be blank because there is no text before the list")
        assertEquals(6, sections[1].bullets.size)
        assertEquals("1.", sections[1].bullets[0].index)
        assertEquals("Conceptos fundamentales", sections[1].bullets[0].label)
        assertTrue(sections[1].bullets[0].value.contains("Expliquen qué son las pruebas estáticas"))
        assertEquals("6.", sections[1].bullets[5].index)
        assertEquals("Conclusión", sections[1].bullets[5].label)

        // Footer note must be at the bottom (after list)
        assertEquals(1, sections[1].footerNotes.size)
        assertTrue(sections[1].footerNotes[0].contains("Consulten al menos tres fuentes confiables"))

        // 3. Presentación y entrega
        assertEquals("Presentación y entrega", sections[2].title)
        assertTrue(sections[2].bullets.isEmpty())
        assertTrue(sections[2].introText.contains("Durante la clase, cada equipo presentará su investigación"))
        assertTrue(sections[2].introText.contains("Como tarea, deberán desarrollar un documento por equipo"))
    }
}
