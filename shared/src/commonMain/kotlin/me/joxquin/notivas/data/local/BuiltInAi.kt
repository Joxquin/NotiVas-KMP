package me.joxquin.notivas.data.local

/**
 * IA incluida en la app: una key de Google AI Studio que entra al compilar, desde el secreto
 * `NOTIVAS_AI_KEY` de GitHub Actions o `notivas.aiKey` en local.properties. Nunca está en el
 * repositorio. Si el build no la trae, la app pide la key del estudiante como antes.
 */
object BuiltInAi {
    var googleAiStudioKey: String = ""
        private set

    val isAvailable: Boolean get() = googleAiStudioKey.isNotBlank()

    fun install(key: String?) {
        googleAiStudioKey = key?.trim().orEmpty()
    }
}

/** Proveedor y key que de verdad usa la IA. */
data class AiCredentials(
    val provider: CopilotProvider,
    val apiKey: String?,
    val usesBuiltInKey: Boolean,
)

/**
 * La key que puso el estudiante manda. Si el proveedor elegido no tiene key y la app trae una
 * incluida, se usa Gemini (Google AI Studio) con la key incluida.
 */
fun resolveAiCredentials(
    selectedProvider: CopilotProvider,
    googleAiStudioKey: String?,
    openRouterKey: String?,
    builtInGoogleKey: String,
): AiCredentials {
    val userKey = when (selectedProvider) {
        CopilotProvider.GOOGLE_AI_STUDIO -> googleAiStudioKey
        CopilotProvider.OPENROUTER -> openRouterKey
    }?.takeIf { it.isNotBlank() }
    if (userKey != null) return AiCredentials(selectedProvider, userKey, usesBuiltInKey = false)

    val builtIn = builtInGoogleKey.takeIf { it.isNotBlank() }
        ?: return AiCredentials(selectedProvider, null, usesBuiltInKey = false)
    return AiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, builtIn, usesBuiltInKey = true)
}
