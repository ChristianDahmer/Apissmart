package com.example.data.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.3f,
    val topP: Float? = 0.95f,
    val maxOutputTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// Data structure parsed from field inspection
data class StructuredInspectionResult(
    val queenSeen: Boolean = true,
    val broodPattern: String = "Excelente uniforme",
    val eggsSeen: Boolean = true,
    val larvaeSeen: Boolean = true,
    val cappedBroodSeen: Boolean = true,
    val honeyStores: String = "Suficiente",
    val pollenStores: String = "Suficiente",
    val pestDetected: String = "Nenhuma",
    val pestSeverity: String = "Nenhuma",
    val temperament: String = "Mansa",
    val feedCondition: String = "Não instalado",
    val diagnosticMarkdown: String = "",
    val isSanitaryAlert: Boolean = false,
    val recommendedTasks: List<String> = emptyList(),
    val queenStatusAssessment: String = "Presente e Saudável",
    val imageAnalysisDetails: String? = null
)
