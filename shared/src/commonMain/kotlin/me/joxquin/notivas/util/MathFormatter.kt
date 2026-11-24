package me.joxquin.notivas.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Utilidad multiplataforma para detectar, limpiar y formatear expresiones matemáticas
 * escritas en sintaxis LaTeX o notación aritmética a texto Unicode legible y estilizado en Compose.
 */
object MathFormatter {

    private val SUPERSCRIPT_MAP = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ'
    )

    private val SUBSCRIPT_MAP = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'o' to 'ₒ', 'x' to 'ₓ', 'i' to 'ᵢ', 'j' to 'ⱼ'
    )

    private val GREEK_MAP = mapOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\Gamma" to "Γ",
        "\\delta" to "δ",
        "\\Delta" to "Δ",
        "\\epsilon" to "ε",
        "\\zeta" to "ζ",
        "\\eta" to "η",
        "\\theta" to "θ",
        "\\Theta" to "Θ",
        "\\lambda" to "λ",
        "\\Lambda" to "Λ",
        "\\mu" to "μ",
        "\\nu" to "ν",
        "\\xi" to "ξ",
        "\\pi" to "π",
        "\\Pi" to "Π",
        "\\rho" to "ρ",
        "\\sigma" to "σ",
        "\\Sigma" to "Σ",
        "\\tau" to "τ",
        "\\phi" to "φ",
        "\\Phi" to "Φ",
        "\\chi" to "χ",
        "\\psi" to "ψ",
        "\\omega" to "ω",
        "\\Omega" to "Ω"
    )

    /**
     * Determina si una línea o texto contiene una fórmula matemática relevante que deba
     * ser aislada o formateada como bloque de cálculo.
     */
    fun isMathExpression(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.startsWith("$$") && trimmed.endsWith("$$")) return true
        if (trimmed.startsWith("$") && trimmed.endsWith("$") && trimmed.length > 2) return true
        if (trimmed.contains("\\frac") || trimmed.contains("\\times") || trimmed.contains("\\mathbf")) return true
        if (trimmed.contains(" = ") && (trimmed.contains(" / ") || trimmed.contains(" * ") || trimmed.contains(" + "))) {
            val digitsCount = trimmed.count { it.isDigit() }
            if (digitsCount >= 3) return true
        }
        return false
    }

    /**
     * Limpia y traduce una fórmula matemática de LaTeX a texto Unicode formateado y limpio.
     */
    fun formatLatexToMathText(rawLatex: String): String {
        var text = rawLatex.trim()

        // Eliminar delimitadores de bloque o en línea $$ o $
        if (text.startsWith("$$") && text.endsWith("$$") && text.length >= 4) {
            text = text.substring(2, text.length - 2).trim()
        } else if (text.startsWith("$") && text.endsWith("$") && text.length >= 2) {
            text = text.substring(1, text.length - 1).trim()
        }

        // 1. Delimitadores y delimitadores de tamaño LaTeX
        text = text
            .replace("\\left(", "(")
            .replace("\\right)", ")")
            .replace("\\left[", "[")
            .replace("\\right]", "]")
            .replace("\\left\\{", "{")
            .replace("\\right\\}", "}")
            .replace("\\{", "{")
            .replace("\\}", "}")
            .replace("\\,", " ")
            .replace("\\;", " ")
            .replace("\\!", "")
            .replace("\\quad", "  ")
            .replace("\\qquad", "    ")

        // 2. Extraer texto plano y estilos tipográficos iterativamente
        // Manejar \text{...}, \mathrm{...}, \textbf{...}, \mathbf{...}, \mathit{...}
        var prevText: String
        do {
            prevText = text
            text = text
                .replace(Regex("""\\text\{([^{}]*)\}""")) { it.groupValues[1] }
                .replace(Regex("""\\mathrm\{([^{}]*)\}""")) { it.groupValues[1] }
                .replace(Regex("""\\mathbf\{([^{}]*)\}""")) { it.groupValues[1] }
                .replace(Regex("""\\textbf\{([^{}]*)\}""")) { it.groupValues[1] }
                .replace(Regex("""\\mathit\{([^{}]*)\}""")) { it.groupValues[1] }
                .replace(Regex("""\\boldsymbol\{([^{}]*)\}""")) { it.groupValues[1] }
        } while (text != prevText)

        // 3. Convertir fracciones \frac{num}{den} iterativamente
        do {
            prevText = text
            text = text.replace(Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")) { match ->
                val num = match.groupValues[1].trim()
                val den = match.groupValues[2].trim()

                val formattedNum = if (needsParentheses(num)) "($num)" else num
                val formattedDen = if (needsParentheses(den)) "($den)" else den
                "$formattedNum / $formattedDen"
            }
        } while (text != prevText)

        // 4. Raíces cuadradas \sqrt{x}
        do {
            prevText = text
            text = text.replace(Regex("""\\sqrt\{([^{}]+)\}""")) { match ->
                val content = match.groupValues[1].trim()
                "√($content)"
            }
        } while (text != prevText)

        // 5. Operadores y símbolos matemáticos comunes
        text = text
            .replace("\\times", "×")
            .replace("\\cdot", "·")
            .replace("\\div", "÷")
            .replace("\\pm", "±")
            .replace("\\mp", "∓")
            .replace("\\approx", "≈")
            .replace("\\sim", "~")
            .replace("\\neq", "≠")
            .replace("\\ne", "≠")
            .replace("\\leq", "≤")
            .replace("\\le", "≤")
            .replace("\\geq", "≥")
            .replace("\\ge", "≥")
            .replace("\\sum", "∑")
            .replace("\\prod", "∏")
            .replace("\\int", "∫")
            .replace("\\infty", "∞")
            .replace("\\to", "→")
            .replace("\\rightarrow", "→")
            .replace("\\leftarrow", "←")

        // 6. Letras griegas
        for ((latex, unicode) in GREEK_MAP) {
            text = text.replace(latex, unicode)
        }

        // 7. Superíndices y subíndices
        text = text.replace(Regex("""\^\{([^{}]+)\}""")) { match ->
            val inner = match.groupValues[1]
            convertToSuperScript(inner)
        }
        text = text.replace(Regex("""\^([0-9a-zA-Z+-])""")) { match ->
            val char = match.groupValues[1].firstOrNull()
            if (char != null && SUPERSCRIPT_MAP.containsKey(char)) {
                SUPERSCRIPT_MAP[char].toString()
            } else {
                "^" + match.groupValues[1]
            }
        }

        text = text.replace(Regex("""_\{([^{}]+)\}""")) { match ->
            val inner = match.groupValues[1]
            convertToSubScript(inner)
        }
        text = text.replace(Regex("""_([0-9a-zA-Z+-])""")) { match ->
            val char = match.groupValues[1].firstOrNull()
            if (char != null && SUBSCRIPT_MAP.containsKey(char)) {
                SUBSCRIPT_MAP[char].toString()
            } else {
                "_" + match.groupValues[1]
            }
        }

        // 8. Limpiar espacios múltiples y barras residuales
        text = text
            .replace(Regex("""\\([a-zA-Z]+)""")) { it.groupValues[1] } // Eliminar comandos desconocidos de barra
            .replace(Regex("""\s+"""), " ")
            .trim()

        return text
    }

    private fun needsParentheses(expr: String): Boolean {
        val trimmed = expr.trim()
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) return false
        return trimmed.contains("+") || trimmed.contains("-") || trimmed.contains("×") || trimmed.contains("*")
    }

    private fun convertToSuperScript(input: String): String {
        val converted = input.map { SUPERSCRIPT_MAP[it] ?: return "^($input)" }.joinToString("")
        return converted
    }

    private fun convertToSubScript(input: String): String {
        val converted = input.map { SUBSCRIPT_MAP[it] ?: return "_($input)" }.joinToString("")
        return converted
    }

    /**
     * Construye un [AnnotatedString] estilizado para una fórmula matemática
     * destacando números, variables y operadores.
     */
    fun formatMathAnnotatedString(formulaText: String): AnnotatedString {
        val clean = formatLatexToMathText(formulaText)
        return buildAnnotatedString {
            var i = 0
            while (i < clean.length) {
                val c = clean[i]
                when {
                    c.isDigit() || c == '.' -> {
                        // Números en negrita o peso semi-bold
                        val start = i
                        while (i < clean.length && (clean[i].isDigit() || clean[i] == '.')) {
                            i++
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(clean.substring(start, i))
                        }
                    }
                    c in listOf('=', '+', '-', '×', '÷', '·', '/', '≤', '≥', '≠', '≈', '±') -> {
                        // Operadores con espacio agradable
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" $c ")
                        }
                        i++
                    }
                    c.isLetter() -> {
                        // Variables en cursiva matemática si son de un solo carácter
                        val start = i
                        while (i < clean.length && clean[i].isLetter()) {
                            i++
                        }
                        val word = clean.substring(start, i)
                        if (word.length == 1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif)) {
                                append(word)
                            }
                        } else {
                            append(word)
                        }
                    }
                    else -> {
                        append(c)
                        i++
                    }
                }
            }
        }
    }
}
