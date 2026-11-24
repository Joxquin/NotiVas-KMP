package me.joxquin.notivas.data.repository.copilot

/** Tipo de fallo al hablar con el modelo, para decidir si reintentar y qué mensaje mostrar. */
enum class CopilotErrorKind {
    /** El modelo está saturado o el servicio no responde (503/502/504, "high demand"). Suele pasar en segundos. */
    OVERLOADED,

    /** Se agotó la cuota o el límite por minuto de la key (429, RESOURCE_EXHAUSTED). */
    RATE_LIMITED,

    /** La llamada no respondió a tiempo. */
    TIMEOUT,

    /** La key no es válida para el proveedor elegido (401/403, "Missing Authentication header"). */
    AUTH,

    /** Sin conexión: no se pudo resolver o alcanzar el servidor (modo avión, sin datos). */
    NETWORK,

    OTHER;

    val isTransient: Boolean get() = this == OVERLOADED || this == RATE_LIMITED || this == TIMEOUT

    companion object {
        /**
         * Fallos de red que Ktor (motor CIO) lanza sin mensaje: en modo avión llegaba
         * UnresolvedAddressException con message null y el chat mostraba "No se pudo procesar tu solicitud: ".
         */
        private val networkExceptions = setOf(
            "UnresolvedAddressException", "UnknownHostException", "ConnectException", "NoRouteToHostException", "SocketException"
        )
        private val network = Regex(
            """Unable to resolve host|No address associated|Network is unreachable|Failed to connect|UnresolvedAddress""",
            RegexOption.IGNORE_CASE
        )
        private val overloaded = Regex("""HTTP 50[234]|"code":\s*50[234]|UNAVAILABLE|high demand|overloaded""", RegexOption.IGNORE_CASE)
        private val rateLimited = Regex("""HTTP 429|"code":\s*429|RESOURCE_EXHAUSTED|rate limit|quota""", RegexOption.IGNORE_CASE)
        private val auth = Regex(
            """HTTP 40[13]|"code":\s*40[13]|Missing Authentication|API key not valid|invalid api key|PERMISSION_DENIED|UNAUTHENTICATED|No auth credentials""",
            RegexOption.IGNORE_CASE
        )

        fun classify(error: Throwable?): CopilotErrorKind {
            if (error is io.ktor.client.plugins.HttpRequestTimeoutException) return TIMEOUT
            var cause = error
            repeat(5) {
                if (cause?.let { it::class.simpleName } in networkExceptions) return NETWORK
                cause = cause?.cause
            }
            return classify(error?.message)
        }

        fun classify(message: String?): CopilotErrorKind {
            val text = message.orEmpty()
            return when {
                text.contains("timeout", ignoreCase = true) -> TIMEOUT
                network.containsMatchIn(text) -> NETWORK
                rateLimited.containsMatchIn(text) -> RATE_LIMITED
                overloaded.containsMatchIn(text) -> OVERLOADED
                auth.containsMatchIn(text) -> AUTH
                else -> OTHER
            }
        }
    }
}
