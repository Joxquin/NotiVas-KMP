package me.joxquin.notivas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterChatRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<OpenRouterMessage>,
    @SerialName("tools") val tools: List<OpenRouterTool>? = null,
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class OpenRouterMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenRouterToolCall>? = null
)

@Serializable
data class OpenRouterTool(
    @SerialName("type") val type: String = "function",
    @SerialName("function") val function: OpenRouterFunction
)

@Serializable
data class OpenRouterFunction(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("parameters") val parameters: OpenRouterParameters
)

@Serializable
data class OpenRouterParameters(
    @SerialName("type") val type: String = "object",
    @SerialName("properties") val properties: Map<String, OpenRouterProperty>,
    @SerialName("required") val required: List<String> = emptyList()
)

@Serializable
data class OpenRouterProperty(
    @SerialName("type") val type: String,
    @SerialName("description") val description: String,
    @SerialName("enum") val enumValues: List<String>? = null
)

@Serializable
data class OpenRouterToolCall(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String = "function",
    @SerialName("function") val function: OpenRouterFunctionCall
)

@Serializable
data class OpenRouterFunctionCall(
    @SerialName("name") val name: String,
    @SerialName("arguments") val arguments: String
)

@Serializable
data class OpenRouterChatResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("choices") val choices: List<OpenRouterChoice>? = null,
    @SerialName("usage") val usage: OpenRouterUsage? = null,
    @SerialName("error") val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterChoice(
    @SerialName("index") val index: Int? = null,
    @SerialName("message") val message: OpenRouterChoiceMessage? = null,
    @SerialName("delta") val delta: OpenRouterChoiceDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class OpenRouterChoiceMessage(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenRouterToolCall>? = null
)

@Serializable
data class OpenRouterChoiceDelta(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenRouterToolCallDelta>? = null
)

@Serializable
data class OpenRouterToolCallDelta(
    @SerialName("index") val index: Int? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("function") val function: OpenRouterFunctionCallDelta? = null
)

@Serializable
data class OpenRouterFunctionCallDelta(
    @SerialName("name") val name: String? = null,
    @SerialName("arguments") val arguments: String? = null
)

@Serializable
data class OpenRouterUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
)

@Serializable
data class OpenRouterError(
    @SerialName("message") val message: String? = null,
    @SerialName("code") val code: Int? = null,
    @SerialName("metadata") val metadata: OpenRouterErrorMetadata? = null
)

@Serializable
data class OpenRouterErrorMetadata(
    @SerialName("raw") val raw: String? = null,
    @SerialName("provider_name") val providerName: String? = null
)

@Serializable
data class OpenRouterCreditsResponse(
    @SerialName("data") val data: OpenRouterCreditsData? = null
)

@Serializable
data class OpenRouterCreditsData(
    @SerialName("total_credits") val totalCredits: Double? = null,
    @SerialName("total_usage") val totalUsage: Double? = null
)

@Serializable
data class OpenRouterKeyResponse(
    @SerialName("data") val data: OpenRouterKeyData? = null
)

@Serializable
data class OpenRouterKeyData(
    @SerialName("label") val label: String? = null,
    @SerialName("usage") val usage: Double? = null,
    @SerialName("limit") val limit: Double? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean? = null
)
