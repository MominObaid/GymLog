package com.example.gymlog

import android.util.Log
import com.example.gymlog.api.AiPromptRequest
import com.example.gymlog.api.GeminiApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAssistantManager @Inject constructor(
    private val geminiApiService: GeminiApiService
) {

    suspend fun getWorkoutAdvice(userPrompt: String): String? {
        return try {
            val response = geminiApiService.getWorkoutAdvice(AiPromptRequest(userPrompt))
            if (response.isSuccessful) {
                response.body()?.advice
            } else {
                val errorMsg = "Backend Error: ${response.code()}"
                Log.e("AI_ERROR", errorMsg)
                "Error: $errorMsg"
            }
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Network Failed: ${e.message}")
            "Error: ${e.message}"
        }
    }
}
