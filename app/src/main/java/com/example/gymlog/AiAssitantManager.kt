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

    suspend fun getRawAdvice(fullPrompt: String): String? {
        return try {
            val response = geminiApiService.getWorkoutAdvice(AiPromptRequest(fullPrompt))
            if (response.isSuccessful) {
                response.body()?.advice
            } else {
                Log.e("AI_ERROR", "Backend Error: ${response.code()}")
                "I'm sorry, I'm having trouble connecting to my coaching brain right now."
            }
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Network Failed: ${e.message}")
            "Network error. Please check your connection."
        }
    }
}
