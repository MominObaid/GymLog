package com.example.gymlog

import android.util.Log
import com.example.gymlog.api.AiPromptRequest
import com.example.gymlog.api.GeminiApiService
import com.example.gymlog.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAssistantManager @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val repository: RoutineRepository
) {

    suspend fun getCoachingAdvice(userMessage: String): String? {
        val profile = repository.getProfile() ?: UserProfile()
        val totalWorkouts = repository.getWorkoutCount()
        val strongest = repository.getStrongestExercises()
        
        val contextPrompt = """
            You are an expert fitness coach. 
            User Profile: Goal=${profile.goal}, Level=${profile.experienceLevel}, Days/Week=${profile.workoutDaysPerWeek}, Equipment=${profile.availableEquipment}.
            Stats: Total Workouts=$totalWorkouts.
            Strongest Exercises: ${strongest.joinToString { "${it.exerciseName} (${it.maxWeight}kg)" }}.
            
            User says: $userMessage
            
            Provide personalized, data-driven coaching advice.
        """.trimIndent()

        return getWorkoutAdvice(contextPrompt)
    }

    suspend fun generateWorkoutPlan(userRequest: String): String? {
        val profile = repository.getProfile() ?: UserProfile()
        val prompt = """
            Create a structured workout plan in JSON format.
            User Profile: Goal=${profile.goal}, Level=${profile.experienceLevel}, Equipment=${profile.availableEquipment}.
            User Request: $userRequest
            
            Return ONLY a JSON array of objects with this structure:
            [
              {
                "name": "Routine Name",
                "goal": "Routine Goal",
                "exercises": [
                  {
                    "exerciseName": "Exercise Name",
                    "targetSets": 3,
                    "targetReps": 10
                  }
                ]
              }
            ]
        """.trimIndent()
        
        return getWorkoutAdvice(prompt)
    }

    private suspend fun getWorkoutAdvice(userPrompt: String): String? {
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
