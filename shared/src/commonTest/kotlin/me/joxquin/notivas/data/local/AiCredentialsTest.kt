package me.joxquin.notivas.data.local

import kotlin.test.Test
import kotlin.test.assertEquals

class AiCredentialsTest {

    @Test
    fun studentGoogleKeyWinsOverBuiltInKey() {
        val credentials = resolveAiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, "AQ.mine", null, "AQ.app")
        assertEquals(AiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, "AQ.mine", usesBuiltInKey = false), credentials)
    }

    @Test
    fun studentOpenRouterKeyKeepsOpenRouter() {
        val credentials = resolveAiCredentials(CopilotProvider.OPENROUTER, null, "sk-or-mine", "AQ.app")
        assertEquals(AiCredentials(CopilotProvider.OPENROUTER, "sk-or-mine", usesBuiltInKey = false), credentials)
    }

    @Test
    fun withoutStudentKeyUsesBuiltInGemini() {
        val credentials = resolveAiCredentials(CopilotProvider.OPENROUTER, null, "", "AQ.app")
        assertEquals(AiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, "AQ.app", usesBuiltInKey = true), credentials)
    }

    @Test
    fun blankStudentKeyCountsAsMissing() {
        val credentials = resolveAiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, "   ", null, "AQ.app")
        assertEquals(AiCredentials(CopilotProvider.GOOGLE_AI_STUDIO, "AQ.app", usesBuiltInKey = true), credentials)
    }

    @Test
    fun withoutAnyKeyNothingIsInvented() {
        val credentials = resolveAiCredentials(CopilotProvider.OPENROUTER, null, null, "")
        assertEquals(AiCredentials(CopilotProvider.OPENROUTER, null, usesBuiltInKey = false), credentials)
    }
}
