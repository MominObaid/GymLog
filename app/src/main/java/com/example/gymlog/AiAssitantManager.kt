package com.example.gymlog

import android.util.Log

class AiAssistantManager() {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun getWorkoutAdvice(userPrompt: String):String?{
        return try {
            val response = model.generateContent(userPrompt)
            response.text
        }catch (e: Exception){
            Log.e("AI_ERROR", "Gemini Fialed: ${e.message}")
            return "Error: ${e.message}"
        }
    }
}
