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
}
