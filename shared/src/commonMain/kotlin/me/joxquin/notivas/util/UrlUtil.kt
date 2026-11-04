package me.joxquin.notivas.util

/**
 * Codifica una URL para ser usada de manera segura como parámetro de consulta (RFC 3986).
 * No depende de librerías externas para evitar incompatibilidades entre targets KMP.
 */
fun encodeUrlComponent(url: String): String {
    return buildString {
        for (c in url) {
            when (c) {
                in 'a'..'z', in 'A'..'Z', in '0'..'9', '-', '_', '.', '~' -> append(c)
                else -> {
                    val bytes = c.toString().encodeToByteArray()
                    for (b in bytes) {
                        append('%')
                        append(b.toUByte().toString(16).uppercase().padStart(2, '0'))
                    }
                }
            }
        }
    }
}
