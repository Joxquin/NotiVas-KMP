package me.joxquin.notivas.data.local

/**
 * Reconoce de qué servicio es una API key por su prefijo. En la prueba con un Redmi, una key de
 * Google AI Studio se pegó en el campo de OpenRouter y el Copiloto respondía "Missing Authentication
 * header": con esto la key se guarda en su campo y se activa su proveedor.
 */
object CopilotKeyDetector {
    fun providerFor(key: String?): CopilotProvider? {
        val k = key?.trim().orEmpty()
        return when {
            k.startsWith("sk-or-") -> CopilotProvider.OPENROUTER
            k.startsWith("AIza") || k.startsWith("AQ.") -> CopilotProvider.GOOGLE_AI_STUDIO
            else -> null
        }
    }
}
