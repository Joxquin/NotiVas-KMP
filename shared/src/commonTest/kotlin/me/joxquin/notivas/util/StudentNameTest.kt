package me.joxquin.notivas.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StudentNameTest {

    @Test
    fun usesGivenNameWhenCanvasSendsSurnameCommaGivenName() {
        assertEquals("Alejandro", canvasFirstName("Alvarez Sayaverde, Alejandro Aaron"))
        assertEquals("Maria", canvasFirstName("PEREZ, MARIA JOSE"))
    }

    @Test
    fun usesFirstWordWhenNameHasNoComma() {
        assertEquals("Alejandro", canvasFirstName("Alejandro Alvarez"))
        assertEquals("Ana", canvasFirstName("  ana  "))
    }

    @Test
    fun returnsNullWithoutUsableName() {
        assertNull(canvasFirstName(null))
        assertNull(canvasFirstName("   "))
        assertNull(canvasFirstName("Alvarez Sayaverde, "))
    }
}
