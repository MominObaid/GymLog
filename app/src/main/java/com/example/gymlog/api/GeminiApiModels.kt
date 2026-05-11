package com.example.gymlog.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AiPromptRequest(
    val prompt: String
)

@JsonClass(generateAdapter = true)
data class AiAdviceResponse(
    val advice: String? = null,
    val error: String? = null
)
