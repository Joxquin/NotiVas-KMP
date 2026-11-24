package me.joxquin.notivas.util

import me.joxquin.notivas.ui.copilot.components.CopilotContentBlock
import me.joxquin.notivas.ui.copilot.components.parseMessageBlocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathFormatterTest {

    @Test
    fun testFormatLatexToMathText_fractionsAndBold() {
        val raw = """$\frac{14.0 + 14.0}{2} = \mathbf{14.0} / \mathbf{20.0}$"""
        val formatted = MathFormatter.formatLatexToMathText(raw)
        assertEquals("(14.0 + 14.0) / 2 = 14.0 / 20.0", formatted)
    }

    @Test
    fun testFormatLatexToMathText_simpleFraction() {
        val raw = """$\frac{144.5}{9} = \mathbf{16.05} / \mathbf{20.0}$"""
        val formatted = MathFormatter.formatLatexToMathText(raw)
        assertEquals("144.5 / 9 = 16.05 / 20.0", formatted)
    }

    @Test
    fun testFormatLatexToMathText_multiplicationAndText() {
        val raw = """$16.05 \times 0.70 = \mathbf{11.24 \text{ puntos}}$"""
        val formatted = MathFormatter.formatLatexToMathText(raw)
        assertEquals("16.05 × 0.70 = 11.24 puntos", formatted)
    }

    @Test
    fun testFormatLatexToMathText_squareRootAndGreek() {
        val raw = """\sqrt{a^2 + b^2} \ge \mu"""
        val formatted = MathFormatter.formatLatexToMathText(raw)
        assertEquals("√(a² + b²) ≥ μ", formatted)
    }

    @Test
    fun testParseMessageBlocks_separatesFormulaIntoOwnParagraph() {
        val text = """
            • Nota más baja eliminada: El 0.0 del Lab 12 queda descartado.
            • Promedio de laboratorios: $\frac{144.5}{9} = \mathbf{16.05} / \mathbf{20.0}$
            • Aporte de Laboratorios (70%): $16.05 \times 0.70 = \mathbf{11.24 \text{ puntos}}$
        """.trimIndent()

        val blocks = parseMessageBlocks(text)

        // Verificamos que se hayan generado bloques matemáticos dedicados
        val mathBlocks = blocks.filterIsInstance<CopilotContentBlock.MathBlock>()
        assertEquals(2, mathBlocks.size)

        assertEquals("144.5 / 9 = 16.05 / 20.0", mathBlocks[0].formattedFormula)
        assertEquals("16.05 × 0.70 = 11.24 puntos", mathBlocks[1].formattedFormula)

        // Verificamos que los textos previos existan como TextBlock
        val textBlocks = blocks.filterIsInstance<CopilotContentBlock.TextBlock>()
        assertTrue(textBlocks.any { it.content.contains("Promedio de laboratorios:") })
        assertTrue(textBlocks.any { it.content.contains("Aporte de Laboratorios (70%):") })
    }

    @Test
    fun testParseMessageBlocks_displayMathBlock() {
        val text = """
            A continuación el cálculo:
            $$
            \frac{10 + 20}{2} = 15
            $$
            Resultado final confirmado.
        """.trimIndent()

        val blocks = parseMessageBlocks(text)
        val mathBlocks = blocks.filterIsInstance<CopilotContentBlock.MathBlock>()
        assertEquals(1, mathBlocks.size)
        assertEquals("(10 + 20) / 2 = 15", mathBlocks[0].formattedFormula)
    }
}
