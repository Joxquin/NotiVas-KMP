package me.joxquin.notivas.resources

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compose Multiplatform no convierte \' en ' como hacen los recursos de Android:
 * la barra se ve tal cual en pantalla ("Can\'t find your university domain?").
 */
class StringResourcesTest {

    private val catalogs = listOf("values", "values-es").map { File("src/commonMain/composeResources/$it/strings.xml") }

    @Test
    fun testNoEscapedApostrophesInStrings() {
        catalogs.forEach { file ->
            assertTrue(file.exists(), "no se encontró ${file.absolutePath}")
            val escaped = file.readLines().filter { "\\'" in it }.map { it.trim() }
            assertTrue(escaped.isEmpty(), "${file.parentFile.name}/strings.xml tiene \\' que se verían en pantalla: $escaped")
        }
    }

    @Test
    fun testEnglishAndSpanishHaveTheSameKeys() {
        val keyPattern = Regex("""<string name="([^"]+)"""")
        val (english, spanish) = catalogs.map { file -> keyPattern.findAll(file.readText()).map { it.groupValues[1] }.toSet() }
        assertEquals(emptySet(), english - spanish, "claves sin traducir al español")
        assertEquals(emptySet(), spanish - english, "claves en español que no existen en inglés")
    }
}
