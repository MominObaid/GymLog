package com.example.gymlog.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApiService {
    @POST("ai/advice")
    suspend fun getWorkoutAdvice(
        @Body request: AiPromptRequest
    ): Response<AiAdviceResponse>
}
